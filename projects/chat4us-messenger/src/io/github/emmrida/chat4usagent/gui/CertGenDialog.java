package io.github.emmrida.chat4usagent.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JPasswordField;
import java.awt.Color;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSeparator;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;
import java.awt.Font;
import java.awt.Frame;

/**
 * Auto-signed certificate generation dialog
 *
 * @author El Mhadder Mohamed Rida
 */
public class CertGenDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	public static final String KEYSTORE_PFX = "./cert/keystore.pfx"; //$NON-NLS-1$

	private boolean cancelled = true;
	private final JPanel contentPanel = new JPanel();
	private JTextField tfCountry;
	private JTextField tfState;
	private JTextField tfCity;
	private JTextField tfOrg;
	private JTextField tfDepart;
	private JTextField tfDomain;
	private JPasswordField tfCurPswd;
	private JPasswordField tfNewPswd;
	private JPasswordField tfConfPswd;
	private JComboBox<String> cmbBits;
	private JComboBox<String> cmbDuration;
	private JLabel lblNewPswd;
	private JLabel lblConfPswd;
	private JProgressBar progressBar;
	private JTextField tfIPs;
	private JTextField tfDnsNames;

	/**
	 * Checks if the dialog was cancelled
	 */
	public boolean isCancelled() { return cancelled; }

	/**
	 *
	 * @param dnsNames
	 * @param ipAddresses
	 * @return
	 */
   private GeneralNames createGeneralNames(List<String> dnsNames, List<String> ipAddresses) {
       List<GeneralName> generalNameList = new ArrayList<>();

       // Add DNS names
       if (dnsNames != null)
           for (String dnsName : dnsNames)
               generalNameList.add(new GeneralName(GeneralName.dNSName, dnsName));

       // Add IP addresses
       if (ipAddresses != null)
           for (String ipAddress : ipAddresses)
               generalNameList.add(new GeneralName(GeneralName.iPAddress, ipAddress));

       return new GeneralNames(generalNameList.toArray(new GeneralName[0]));
   }

	/**
	 * Generate an auto-signed certificate.
	 * @return true on success.
	 */
	private boolean generateCertificate() {
	    try {
	        // Add BouncyCastle as a security provider
	        Security.addProvider(new BouncyCastleProvider());

	        // Generate a key pair
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); //$NON-NLS-1$
	        keyPairGenerator.initialize(Integer.parseInt(cmbBits.getSelectedItem().toString().split(" ")[0])); //$NON-NLS-1$
	        KeyPair keyPair = keyPairGenerator.generateKeyPair();

	        // Certificate details
	        String issuer = String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s", //$NON-NLS-1$
	                tfDomain.getText(), tfDepart.getText(), tfOrg.getText(), tfCity.getText(), tfState.getText(), tfCountry.getText());
	        String subject = issuer; // Self-signed, so issuer == subject
	        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
	        Date notBefore = new Date();
	        int di = cmbDuration.getSelectedIndex();
	        Date notAfter = new Date(notBefore.getTime() +
	                (di == 0 ? 30L * 24 * 60 * 60 * 1000 :
	                        (di == 1 ? 3L * 30 * 24 * 60 * 60 * 1000 :
	                                6L * 30 * 24 * 60 * 60 * 1000)));

	        // Create a ContentSigner
	        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA") //$NON-NLS-1$
	                .build(keyPair.getPrivate());

	        // Build the certificate
	        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
	                new org.bouncycastle.asn1.x500.X500Name(issuer),
	                serialNumber,
	                notBefore,
	                notAfter,
	                new org.bouncycastle.asn1.x500.X500Name(subject),
	                keyPair.getPublic()
	        );

	        /**/
	        // Add Subject Alternative Names (SANs)
	        List<String> dnsNames = Arrays.asList(tfDnsNames.getText().split(";")); //$NON-NLS-2$ //$NON-NLS-1$
	        List<String> ipAddresses = Arrays.asList(tfIPs.getText().split(";")); //$NON-NLS-2$ //$NON-NLS-1$
	        GeneralNames generalNames = createGeneralNames(dnsNames, ipAddresses);
	        certificateBuilder.addExtension(Extension.subjectAlternativeName, false, generalNames);

	        // Add Key Usage extension
	        certificateBuilder.addExtension(Extension.keyUsage, false,
	            new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

	        // Add Extended Key Usage
	        certificateBuilder.addExtension(Extension.extendedKeyUsage, false,
	            new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
	        /**/
	        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
	        X509Certificate certificate = new JcaX509CertificateConverter()
	                .setProvider("BC") //$NON-NLS-1$
	                .getCertificate(certificateHolder);

	        // Save the private key and certificate in a KeyStore
	        KeyStore keyStore = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
	        keyStore.load(null, null); // Initialize the KeyStore
	        keyStore.setKeyEntry("chat4us.cert", keyPair.getPrivate(), tfNewPswd.getPassword(), //$NON-NLS-1$
	                new Certificate[]{certificate});

	        // Write the KeyStore to a file
	        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(KEYSTORE_PFX)) {
	            keyStore.store(fos, tfNewPswd.getPassword());
	        }

	        // Export the public certificate to a file in the same folder as the KeyStore
	        File keystoreFile = new File(KEYSTORE_PFX);
	        String certificatePath = keystoreFile.getParent() + File.separator + "chat4us-public.cer"; //$NON-NLS-1$
	        try (FileOutputStream certOut = new FileOutputStream(certificatePath)) {
	            certOut.write(certificate.getEncoded());
	        }

	        Helper.logInfo(Messages.getString("CertGenDialog.CERT_CREATION_SUCCESS")); //$NON-NLS-1$
	        return true;
	    } catch (Exception ex) {
	        Helper.logWarning(ex, Messages.getString("CertGenDialog.CERT_GENERATION_ERROR")); //$NON-NLS-1$
	        return false;
	    }
	}

    /**
     * Checks the strength of a password
     * @param password The password to check
     * @return The strength score
     */
    public static String checkStrength(String password) {
        int length = password.length();

        if (length <= 8)
            return Color.RED.getRGB() + Messages.getString("CertGenDialog.PSWD_STRENGTH_WEAK"); //$NON-NLS-1$

        int strengthScore = 0;
        // Check for different character types
        if (password.matches(".*[a-z].*")) strengthScore++; // Lowercase //$NON-NLS-1$
        if (password.matches(".*[A-Z].*")) strengthScore++; // Uppercase //$NON-NLS-1$
        if (password.matches(".*\\d.*")) strengthScore++;   // Digit //$NON-NLS-1$
        if (password.matches(".*[@#$%^&+=!?.*()-_].*")) strengthScore++; // Special Character //$NON-NLS-1$

        // Assess strength based on score
        if (strengthScore < 3) {
            return Color.RED.getRGB() + Messages.getString("CertGenDialog.PSWD_STRENGTH_WEAK"); //$NON-NLS-1$
        } else if (strengthScore == 3 && length >= 12) {
            return Color.ORANGE.getRGB() + Messages.getString("CertGenDialog.PSWD_STRENGTH_MEDIUM"); //$NON-NLS-1$
        } else if (strengthScore == 4 && length >= 16) {
            return Color.GREEN.getRGB() + Messages.getString("CertGenDialog.PSWD_STRENGTH_STRONG"); //$NON-NLS-1$
        } else {
            return Color.ORANGE.getRGB() + Messages.getString("CertGenDialog.PSWD_STRENGTH_MEDIUM"); //$NON-NLS-1$
        }
    }

	/**
	 * Create the dialog.
	 */
	public CertGenDialog(Frame parent) {
		super(parent, Messages.getString("CertGenDialog.CERT_GEN_DLG_TITLE"), true); //$NON-NLS-1$
		setResizable(false);
		setBounds(100, 100, 478, 377);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFocusable(false);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(3)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
					.addGap(3))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(3)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
					.addGap(3))
		);

		JPanel pnlAuth = new JPanel();
		tabbedPane.addTab(Messages.getString("CertGenDialog.TAB_AUTH_TITLE"), null, pnlAuth, null); //$NON-NLS-1$

		tfCurPswd = new JPasswordField();
		tfCurPswd.setColumns(10);

		JLabel lblNewLabel_7 = new JLabel(Messages.getString("CertGenDialog.LBL_CUR_PSWD")); //$NON-NLS-1$

		tfNewPswd = new JPasswordField();
		tfNewPswd.setColumns(10);
		tfNewPswd.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {  checkPassword(); }
			@Override
			public void removeUpdate(DocumentEvent e) {  checkPassword(); }
			@Override
			public void changedUpdate(DocumentEvent e) { checkPassword(); }
			private void checkPassword() {
				String strength = checkStrength(new String(tfNewPswd.getPassword()));
				if(tfNewPswd.getPassword().length > 0) {
					String[] parts = strength.split(";"); //$NON-NLS-1$
					lblNewPswd.setBackground(Color.decode(parts[0]));
					lblNewPswd.setText(parts[1]);
				} else {
					lblNewPswd.setText(""); //$NON-NLS-1$
				}
			}
		});

		JLabel lblNewLabel_8 = new JLabel(Messages.getString("CertGenDialog.LBL_NEW_PSWD")); //$NON-NLS-1$

		tfConfPswd = new JPasswordField();
		tfConfPswd.setColumns(10);
		tfConfPswd.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { checkPassword(); }
			@Override
			public void removeUpdate(DocumentEvent e) { checkPassword(); }
			@Override
			public void changedUpdate(DocumentEvent e) { checkPassword(); }
			private void checkPassword() {
				if(tfConfPswd.getPassword().length == 0) {
					lblConfPswd.setText(""); //$NON-NLS-1$
					return;
				}
				if(new String(tfNewPswd.getPassword()).equals(new String(tfConfPswd.getPassword()))) {
					lblConfPswd.setBackground(Color.GREEN);
					lblConfPswd.setText(Messages.getString("CertGenDialog.LBL_PSWD_STRENGTH_OK")); //$NON-NLS-1$
				} else {
					lblConfPswd.setBackground(Color.RED);
					lblConfPswd.setText(Messages.getString("CertGenDialog.LBL_PSWD_STRENGTH_ERROR")); //$NON-NLS-1$
				}
			}
		});

		JLabel lblNewLabel_9 = new JLabel(Messages.getString("CertGenDialog.LBL_CONFIRMATION")); //$NON-NLS-1$

		lblNewPswd = new JLabel(""); //$NON-NLS-1$
		lblNewPswd.setOpaque(true);
		lblNewPswd.setForeground(Color.WHITE);

		lblConfPswd = new JLabel(""); //$NON-NLS-1$
		lblConfPswd.setOpaque(true);
		lblConfPswd.setForeground(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_pnlAuth = new GroupLayout(pnlAuth);
		gl_pnlAuth.setHorizontalGroup(
			gl_pnlAuth.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlAuth.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnlAuth.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
						.addGroup(gl_pnlAuth.createSequentialGroup()
							.addGroup(gl_pnlAuth.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblNewLabel_7)
								.addComponent(lblNewLabel_8)
								.addComponent(lblNewLabel_9))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_pnlAuth.createParallelGroup(Alignment.TRAILING)
								.addComponent(tfCurPswd, GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
								.addGroup(gl_pnlAuth.createSequentialGroup()
									.addGroup(gl_pnlAuth.createParallelGroup(Alignment.TRAILING)
										.addComponent(tfConfPswd, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
										.addComponent(tfNewPswd, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_pnlAuth.createParallelGroup(Alignment.LEADING)
										.addComponent(lblNewPswd, Alignment.TRAILING)
										.addComponent(lblConfPswd, Alignment.TRAILING))))))
					.addContainerGap())
		);
		gl_pnlAuth.setVerticalGroup(
			gl_pnlAuth.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlAuth.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
					.addGap(18)
					.addGroup(gl_pnlAuth.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_7)
						.addComponent(tfCurPswd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlAuth.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_8)
						.addComponent(tfNewPswd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewPswd))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlAuth.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_9)
						.addComponent(tfConfPswd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblConfPswd))
					.addContainerGap())
		);

		JTextArea taWarning = new JTextArea();
		taWarning.setFont(new Font("Arial", Font.PLAIN, 13)); //$NON-NLS-1$
		taWarning.setBackground(SystemColor.control);
		taWarning.setText(Messages.getString("CertGenDialog.CERT_GEN_WARNING")); //$NON-NLS-1$
		taWarning.setForeground(Color.RED);
		taWarning.setLineWrap(true);
		taWarning.setWrapStyleWord(true);
		taWarning.setEditable(false);
		scrollPane.setViewportView(taWarning);
		pnlAuth.setLayout(gl_pnlAuth);

		JPanel pnlInfo = new JPanel();
		tabbedPane.addTab(Messages.getString("CertGenDialog.TAB_TITLE_INFO"), null, pnlInfo, null); //$NON-NLS-1$

		tfCountry = new JTextField();
		tfCountry.setColumns(10);

		JLabel lblNewLabel = new JLabel(Messages.getString("CertGenDialog.LBL_COUNTRY")); //$NON-NLS-1$

		tfState = new JTextField();
		tfState.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel(Messages.getString("CertGenDialog.LBL_STATE")); //$NON-NLS-1$

		JLabel lblNewLabel_2 = new JLabel(Messages.getString("CertGenDialog.LBL_TOWN")); //$NON-NLS-1$

		JLabel lblNewLabel_3 = new JLabel(Messages.getString("CertGenDialog.LBL_ORG")); //$NON-NLS-1$

		JLabel lblNewLabel_4 = new JLabel(Messages.getString("CertGenDialog.LBL_DEPARTMENT")); //$NON-NLS-1$

		JLabel lblNewLabel_5 = new JLabel(Messages.getString("CertGenDialog.LBL_DOMAIN")); //$NON-NLS-1$

		JLabel lblNewLabel_6 = new JLabel(Messages.getString("CertGenDialog.LBL_DURATION")); //$NON-NLS-1$

		tfCity = new JTextField();
		tfCity.setColumns(10);

		tfOrg = new JTextField();
		tfOrg.setColumns(10);

		tfDepart = new JTextField();
		tfDepart.setColumns(10);

		tfDomain = new JTextField();
		tfDomain.setColumns(10);

		cmbDuration = new JComboBox();
		cmbDuration.setModel(new DefaultComboBoxModel(new String[] {Messages.getString("CertGenDialog.LST_1MONTH"), Messages.getString("CertGenDialog.LST_3MONTHS"), Messages.getString("CertGenDialog.LST_6MONTHS")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		cmbBits = new JComboBox();
		cmbBits.setModel(new DefaultComboBoxModel(new String[] {Messages.getString("CertGenDialog.LST_2048BITS"), Messages.getString("CertGenDialog.LST_4096BITS"), Messages.getString("CertGenDialog.LST_8192BITS")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lblNewLabel_12 = new JLabel(Messages.getString("CertGenDialog.LBL_BITS")); //$NON-NLS-1$

		JSeparator separator = new JSeparator();

		tfDnsNames = new JTextField();
		tfDnsNames.setColumns(10);

		tfIPs = new JTextField();
		tfIPs.setColumns(10);

		JLabel lblNewLabel_10 = new JLabel(Messages.getString("CertGenDialog.LBL_DNS_NAMES")); //$NON-NLS-1$

		JLabel lblNewLabel_11 = new JLabel(Messages.getString("CertGenDialog.LBL_IPS")); //$NON-NLS-1$
		GroupLayout gl_pnlInfo = new GroupLayout(pnlInfo);
		gl_pnlInfo.setHorizontalGroup(
			gl_pnlInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlInfo.createSequentialGroup()
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, gl_pnlInfo.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_pnlInfo.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnlInfo.createSequentialGroup()
									.addGroup(gl_pnlInfo.createParallelGroup(Alignment.TRAILING)
										.addComponent(lblNewLabel_11)
										.addComponent(lblNewLabel_10)
										.addComponent(lblNewLabel_1)
										.addComponent(lblNewLabel_2)
										.addComponent(lblNewLabel_3)
										.addComponent(lblNewLabel_4)
										.addComponent(lblNewLabel_5)
										.addComponent(lblNewLabel))
									.addGap(6)
									.addGroup(gl_pnlInfo.createParallelGroup(Alignment.LEADING)
										.addComponent(tfOrg, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
										.addComponent(tfCity, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
										.addComponent(tfCountry, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
										.addComponent(tfDepart, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
										.addComponent(tfState, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
										.addComponent(tfDomain, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
										.addComponent(tfDnsNames, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
										.addComponent(tfIPs, GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)))
								.addGroup(Alignment.TRAILING, gl_pnlInfo.createSequentialGroup()
									.addGroup(gl_pnlInfo.createParallelGroup(Alignment.TRAILING)
										.addComponent(lblNewLabel_12)
										.addComponent(lblNewLabel_6))
									.addGap(6)
									.addGroup(gl_pnlInfo.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(cmbDuration, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(cmbBits, 0, 343, Short.MAX_VALUE)))))
						.addComponent(separator, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 342, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_pnlInfo.setVerticalGroup(
			gl_pnlInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlInfo.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfCountry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1)
						.addComponent(tfState, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfCity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfOrg, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_3))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfDepart, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_4))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfDomain, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_5))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfDnsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_10))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfIPs, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_11))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbDuration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_6))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbBits, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_12))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		pnlInfo.setLayout(gl_pnlInfo);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("CertGenDialog.BTN_GENERATE")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(Color.ORANGE.equals(lblNewPswd.getBackground())) {
							if(Messages.getString("CertGenDialog.PSWD_OK_VALUE").equals(lblConfPswd.getText()) && tfCurPswd.getPassword().length > 0) { //$NON-NLS-1$
								if(new String(tfNewPswd.getPassword()).equals(new String(tfConfPswd.getPassword()))) {
									int ret = JOptionPane.showConfirmDialog(CertGenDialog.this, Messages.getString("CertGenDialog.CERT_GEN_MSG_BOX"), Messages.getString("CertGenDialog.MSG_BOX_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
									if(ret == JOptionPane.YES_OPTION) {
										progressBar.setVisible(true);
										new Thread(() -> {
											try (InputStream fos = new java.io.FileInputStream(KEYSTORE_PFX)) {
										        KeyStore keyStore = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
										        keyStore.load(fos, tfCurPswd.getPassword());
											} catch(Exception ex) {
												SwingUtilities.invokeLater(() -> {
													progressBar.setVisible(false);
													Helper.logError(ex, Messages.getString("CertGenDialog.WRONG_PSWD_ERROR_LOG"), true); //$NON-NLS-1$
												});
												return;
											}

											File ksOld = new File(KEYSTORE_PFX);
											if(ksOld.exists()) {
												ksOld.renameTo(new File(KEYSTORE_PFX + "_" + Helper.toDate(Instant.now(), "dd-MM-yyyy_HH-mm-ss") + ".old")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											}
											File certOld = new File("./cert/chat4us-public.cer"); //$NON-NLS-1$
											if(certOld.exists()) {
												certOld.renameTo(new File("./cert/chat4us-public.cer_" + Helper.toDate(Instant.now(), "dd-MM-yyyy_HH-mm-ss") + ".old")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											}

											if(generateCertificate()) {
												SwingUtilities.invokeLater(() -> {
													Helper.logInfo(Messages.getString("CertGenDialog.CERT_GEN_SUCCESS_MSG"), true); //$NON-NLS-1$
													cancelled = false;
													setVisible(false);
												});
												return;
											}
											SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
										}).start();
										return;
									}
								}
							}
						}
						Helper.logInfo(Messages.getString("CertGenDialog.INVALID_DATA_ERROR"), true); //$NON-NLS-1$
					}
				});

				progressBar = new JProgressBar();
				progressBar.setVisible(false);
				progressBar.setIndeterminate(true);
				buttonPane.add(progressBar);
				okButton.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(Messages.getString("CertGenDialog.BTN_CANCEL")); //$NON-NLS-1$
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
				buttonPane.add(cancelButton);
				Helper.registerCancelByEsc(this, cancelButton);
			}
		}
		Helper.enableRtlWhenNeeded(this);
		pack();
	}
}
