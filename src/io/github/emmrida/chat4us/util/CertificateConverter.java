/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Certificate converter using BouncyCastle and PKCS#12 format
 * Requires BouncyCastle dependency: org.bouncycastle:bcprov-jdk18on:1.77
 *
 * @author El Mhadder Mohamed Rida
 */
public class CertificateConverter {

    static {
        // Add BouncyCastle provider
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Converts various certificate formats to PKCS#12 and exports public certificate
     *
     * @param certificateInput Path to certificate file or certificate content as string
     * @param privateKeyInput Path to private key file or private key content as string (null if included in certificate)
     * @param inputPassword Password for encrypted input (null if not encrypted)
     * @param outputKeystorePath Path for output PKCS#12 file
     * @param outputPassword Password for output PKCS#12
     * @param alias Alias for the certificate in PKCS#12
     * @param publicCertPath Path for exported public certificate (.cer)
     * @param inputFormat Format hint: "PEM", "PKCS12", "DER", or "AUTO" for auto-detection
     * @throws Exception if conversion fails
     */
    public static void convertToPKCS12AndExportPublic(
            String certificateInput,
            String privateKeyInput,
            String inputPassword,
            String outputKeystorePath,
            String outputPassword,
            String alias,
            String publicCertPath,
            String inputFormat) throws Exception {

        CertificateData certData = loadCertificateData(
            certificateInput, privateKeyInput, inputPassword, inputFormat
        );

        // Create PKCS#12 KeyStore
        KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME); //$NON-NLS-1$
        pkcs12KeyStore.load(null, null);

        // Add private key and certificate chain to KeyStore
        pkcs12KeyStore.setKeyEntry(
            alias,
            certData.privateKey,
            outputPassword.toCharArray(),
            certData.certificateChain
        );

        // Save PKCS#12 file
        try (FileOutputStream fos = new FileOutputStream(outputKeystorePath)) {
            pkcs12KeyStore.store(fos, outputPassword.toCharArray());
        }

        // Export public certificate
        exportPublicCertificate(certData.certificateChain[0], publicCertPath);

        Helper.logInfo(Messages.getString("CertificateConverter.CERT_CONVERSION_SUCCESS") + outputKeystorePath); //$NON-NLS-1$
        Helper.logInfo(Messages.getString("CertificateConverter.PUB_CERT_EXPORT_SUCCESS") + publicCertPath); //$NON-NLS-1$
        Helper.logInfo(Messages.getString("CertificateConverter.CERT_SUBJECT") + //$NON-NLS-1$
            ((X509Certificate) certData.certificateChain[0]).getSubjectDN());
        Helper.logInfo(Messages.getString("CertificateConverter.CERT_ISSUER") + //$NON-NLS-1$
            ((X509Certificate) certData.certificateChain[0]).getIssuerDN());
    }

    /**
     * Loads certificate data from various formats using BouncyCastle
     */
    private static CertificateData loadCertificateData(
            String certInput, String keyInput, String password, String format) throws Exception {

        // Auto-detect format if needed
        if ("AUTO".equals(format)) { //$NON-NLS-1$
            format = detectFormat(certInput);
        }

        switch (format.toUpperCase()) {
            case "PEM": //$NON-NLS-1$
                return loadFromPEM(certInput, keyInput, password);
            case "PKCS12": //$NON-NLS-1$
            case "P12": //$NON-NLS-1$
                return loadFromPKCS12(certInput, password);
            case "DER": //$NON-NLS-1$
                return loadFromDER(certInput, keyInput);
            default:
                throw new IllegalArgumentException(Messages.getString("CertificateConverter.CERT_UNSUPPORTED_FORMAT") + format); //$NON-NLS-1$
        }
    }

    /**
     * Loads certificate and key from PEM format using BouncyCastle
     */
    private static CertificateData loadFromPEM(String certInput, String keyInput, String password)
            throws Exception {

        String certContent = readInputContent(certInput);
        String keyContent = keyInput != null ? readInputContent(keyInput) : certContent;

        // Extract certificates using BouncyCastle
        List<X509Certificate> certificates = extractCertificatesFromPEMWithBC(certContent);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("CertificateConverter.NO_CERT_FOUND_IN_PEM")); //$NON-NLS-1$
        }

        // Extract private key using BouncyCastle
        PrivateKey privateKey = extractPrivateKeyFromPEMWithBC(keyContent, password);
        if (privateKey == null) {
            throw new IllegalArgumentException(Messages.getString("CertificateConverter.NO_PRVKEY_IN_PEM")); //$NON-NLS-1$
        }

        return new CertificateData(privateKey, certificates.toArray(new Certificate[0]));
    }

    /**
     * Loads certificate and key from PKCS#12 format
     */
    private static CertificateData loadFromPKCS12(String p12Input, String password) throws Exception {
        byte[] p12Data = isFilePath(p12Input) ?
            Files.readAllBytes(Paths.get(p12Input)) :
            Base64.getDecoder().decode(p12Input);

        KeyStore p12Store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME); //$NON-NLS-1$
        p12Store.load(new ByteArrayInputStream(p12Data),
                     password != null ? password.toCharArray() : null);

        // Find the first private key entry
        Enumeration<String> aliases = p12Store.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (p12Store.isKeyEntry(alias)) {
                PrivateKey privateKey = (PrivateKey) p12Store.getKey(alias,
                    password != null ? password.toCharArray() : null);
                Certificate[] chain = p12Store.getCertificateChain(alias);
                return new CertificateData(privateKey, chain);
            }
        }

        throw new IllegalArgumentException(Messages.getString("CertificateConverter.NO_PRVKEY_FOUND_IN_PKCS12")); //$NON-NLS-1$
    }

    /**
     * Loads certificate and key from DER format using BouncyCastle
     */
    private static CertificateData loadFromDER(String certInput, String keyInput) throws Exception {
        if (keyInput == null) {
            throw new IllegalArgumentException(Messages.getString("CertificateConverter.PRVKEY_REQUIRED_FOR_DER")); //$NON-NLS-1$
        }

        // Load certificate using BouncyCastle
        byte[] certData = isFilePath(certInput) ?
            Files.readAllBytes(Paths.get(certInput)) :
            Base64.getDecoder().decode(certInput);

        X509CertificateHolder certHolder = new X509CertificateHolder(certData);
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter()
            .setProvider(BouncyCastleProvider.PROVIDER_NAME);
        X509Certificate certificate = converter.getCertificate(certHolder);

        // Load private key
        byte[] keyData = isFilePath(keyInput) ?
            Files.readAllBytes(Paths.get(keyInput)) :
            Base64.getDecoder().decode(keyInput);

        PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(keyData);
        JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter()
            .setProvider(BouncyCastleProvider.PROVIDER_NAME);
        PrivateKey privateKey = keyConverter.getPrivateKey(privateKeyInfo);

        return new CertificateData(privateKey, new Certificate[]{certificate});
    }

    /**
     * Extracts certificates from PEM content using BouncyCastle
     */
    private static List<X509Certificate> extractCertificatesFromPEMWithBC(String pemContent)
            throws Exception {
        List<X509Certificate> certificates = new ArrayList<>();

        try (StringReader stringReader = new StringReader(pemContent);
             PEMParser pemParser = new PEMParser(stringReader)) {

            JcaX509CertificateConverter converter = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME);

            Object object;
            while ((object = pemParser.readObject()) != null) {
                if (object instanceof X509CertificateHolder) {
                    X509CertificateHolder certHolder = (X509CertificateHolder) object;
                    X509Certificate cert = converter.getCertificate(certHolder);
                    certificates.add(cert);
                }
            }
        }

        return certificates;
    }

    /**
     * Extracts private key from PEM content using BouncyCastle
     */
    private static PrivateKey extractPrivateKeyFromPEMWithBC(String pemContent, String password)
            throws Exception {

        try (StringReader stringReader = new StringReader(pemContent);
             PEMParser pemParser = new PEMParser(stringReader)) {

            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME);

            Object object;
            while ((object = pemParser.readObject()) != null) {
                if (object instanceof PEMEncryptedKeyPair && password != null) {
                    // Handle encrypted private key pair
                    PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) object;
                    PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .build(password.toCharArray());
                    PEMKeyPair keyPair = encryptedKeyPair.decryptKeyPair(decryptorProvider);
                    return keyConverter.getPrivateKey(keyPair.getPrivateKeyInfo());

                } else if (object instanceof PEMKeyPair) {
                    // Handle unencrypted private key pair
                    PEMKeyPair keyPair = (PEMKeyPair) object;
                    return keyConverter.getPrivateKey(keyPair.getPrivateKeyInfo());

                } else if (object instanceof PrivateKeyInfo) {
                    // Handle PKCS#8 private key
                    PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) object;
                    return keyConverter.getPrivateKey(privateKeyInfo);

                } else if (object instanceof PKCS8EncryptedPrivateKeyInfo && password != null) {
                    // Handle PKCS#8 encrypted private key
                    PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo =
                        (PKCS8EncryptedPrivateKeyInfo) object;
                    PrivateKeyInfo privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(
                        new JcePKCSPBEInputDecryptorProviderBuilder()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .build(password.toCharArray()));
                    return keyConverter.getPrivateKey(privateKeyInfo);
                }
            }
        }

        return null;
    }

    /**
     * Exports public certificate to file
     */
    private static void exportPublicCertificate(Certificate certificate, String outputPath)
            throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(certificate.getEncoded());
        }
    }

    /**
     * Detects certificate format
     */
    private static String detectFormat(String input) {
        String content = readInputContent(input);

        if (content.contains("-----BEGIN CERTIFICATE-----") || //$NON-NLS-1$
            content.contains("-----BEGIN PRIVATE KEY-----") || //$NON-NLS-1$
            content.contains("-----BEGIN RSA PRIVATE KEY-----") || //$NON-NLS-1$
            content.contains("-----BEGIN ENCRYPTED PRIVATE KEY-----")) { //$NON-NLS-1$
            return "PEM"; //$NON-NLS-1$
        }

        if (isFilePath(input)) {
            String fileName = input.toLowerCase();
            if (fileName.endsWith(".p12") || fileName.endsWith(".pfx")) { //$NON-NLS-1$ //$NON-NLS-2$
                return "PKCS12"; //$NON-NLS-1$
            }
            if (fileName.endsWith(".der") || fileName.endsWith(".cer")) { //$NON-NLS-1$ //$NON-NLS-2$
                return "DER"; //$NON-NLS-1$
            }
        }

        return "PEM"; // Default assumption //$NON-NLS-1$
    }

    /**
     * Reads content from file path or returns the string as-is
     */
    private static String readInputContent(String input) {
        if (isFilePath(input)) {
            try {
                return new String(Files.readAllBytes(Paths.get(input)));
            } catch (IOException e) {
                throw new RuntimeException(Messages.getString("CertificateConverter.FILE_READ_ERROR") + input, e); //$NON-NLS-1$
            }
        }
        return input;
    }

    /**
     * Determines if input is a file path or content
     */
    private static boolean isFilePath(String input) {
        return input != null &&
               !input.contains("-----BEGIN") && //$NON-NLS-1$
               !input.contains("\n") && //$NON-NLS-1$
               Files.exists(Paths.get(input));
    }

    /**
     * Utility method to create PKCS#12 from separate certificate chain and private key files
     */
    public static void createPKCS12FromSeparateFiles(
            String[] certificateFiles,
            String privateKeyFile,
            String privateKeyPassword,
            String outputPath,
            String outputPassword,
            String alias) throws Exception {

        // Load private key
        String keyContent = readInputContent(privateKeyFile);
        PrivateKey privateKey = extractPrivateKeyFromPEMWithBC(keyContent, privateKeyPassword);

        // Load certificate chain
        List<X509Certificate> certChain = new ArrayList<>();
        for (String certFile : certificateFiles) {
            String certContent = readInputContent(certFile);
            List<X509Certificate> certs = extractCertificatesFromPEMWithBC(certContent);
            certChain.addAll(certs);
        }

        // Create PKCS#12
        KeyStore pkcs12Store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME); //$NON-NLS-1$
        pkcs12Store.load(null, null);

        pkcs12Store.setKeyEntry(alias, privateKey, outputPassword.toCharArray(),
                               certChain.toArray(new Certificate[0]));

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            pkcs12Store.store(fos, outputPassword.toCharArray());
        }

        Helper.logInfo(Messages.getString("CertificateConverter.PKCS12_FILE_CREATION_SUCCESS") + outputPath); //$NON-NLS-1$
    }

    /**
     * Utility method to list contents of PKCS#12 file
     */
    public static void listPKCS12Contents(String pkcs12Path, String password) throws Exception {
        KeyStore pkcs12Store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME); //$NON-NLS-1$

        try (FileInputStream fis = new FileInputStream(pkcs12Path)) {
            pkcs12Store.load(fis, password != null ? password.toCharArray() : null);
        }

        Helper.logInfo(Messages.getString("CertificateConverter.PKCS12_FILE_CONTENTS") + pkcs12Path); //$NON-NLS-1$
        Helper.logInfo("=========================================="); //$NON-NLS-1$

        Enumeration<String> aliases = pkcs12Store.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_ALIAS") + alias); //$NON-NLS-1$
            Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_KEY_ENTRY") + pkcs12Store.isKeyEntry(alias)); //$NON-NLS-1$
            Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_CERT_ENTRY") + pkcs12Store.isCertificateEntry(alias)); //$NON-NLS-1$

            if (pkcs12Store.isKeyEntry(alias)) {
                Certificate[] chain = pkcs12Store.getCertificateChain(alias);
                Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_CHAIN_LENGTH") + (chain != null ? chain.length : 0)); //$NON-NLS-1$

                if (chain != null && chain.length > 0 && chain[0] instanceof X509Certificate) {
                    X509Certificate cert = (X509Certificate) chain[0];
                    Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_SUBJECT") + cert.getSubjectDN()); //$NON-NLS-1$
                    Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_ISSUER") + cert.getIssuerDN()); //$NON-NLS-1$
                    Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_VALID_FROM") + cert.getNotBefore()); //$NON-NLS-1$
                    Helper.logInfo(Messages.getString("CertificateConverter.CERT_LOG_VALID_TO") + cert.getNotAfter()); //$NON-NLS-1$
                }
            }
            Helper.logInfo("------------------------------------------"); //$NON-NLS-1$
        }
    }

    /**
     * Data class to hold certificate and private key
     */
    private static class CertificateData {
        final PrivateKey privateKey;
        final Certificate[] certificateChain;

        CertificateData(PrivateKey privateKey, Certificate[] certificateChain) {
            this.privateKey = privateKey;
            this.certificateChain = certificateChain;
        }
    }
}
