/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.Instant;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.emmrida.chat4usagent.util.CertificateConverter;
import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;
import java.awt.Frame;
import javax.swing.border.EtchedBorder;

/**
 * This is used to generate a new certificate to secure communication between chat server instances, IFrames and messenger apps.
 *
 * @author El Mhadder Mohamed Rida
 */
public class CertImportDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	public static final String KEYSTORE_PFX = "./cert/keystore.pfx"; //$NON-NLS-1$

	private boolean cancelled = true;
	private final JPanel contentPanel = new JPanel();
	private JPasswordField tfCertPswd;
	private JPasswordField tfNewPswd;
	private JPasswordField tfConfPswd;
	private JLabel lblConfPswd;
	private JProgressBar progressBar;
	private JButton cancelButton;
	private JLabel lblNewPswd;
	private JTextField tfCertPath;
	private JTextField tfAlias;
	private JTextField tfKeyPath;
	private JPasswordField tfCurPsWd;

	/**
	 * Checks whether user cancelled the dialog.
	 * @return true if user clicked on Cancel button.
	 */
	public boolean isCancelled() { return cancelled; }

	/**
	 *
	 * @return true on success.
	 */
	private boolean importCertificate() {
		try {
			if(!Helper.checkKeystorePassword(tfCurPsWd.getPassword())) {
				Helper.logInfo(Messages.getString("CertImportDialog.WRONG_AUTH_ERROR"), true); //$NON-NLS-1$
				return false;
			}

			File ksOld = new File(KEYSTORE_PFX);
			if(ksOld.exists()) {
				ksOld.renameTo(new File(KEYSTORE_PFX + "_" + Helper.toDate(Instant.now(), "dd-MM-yyyy_HH-mm-ss") + ".old")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			File certOld = new File("./cert/chat4us-public.cer"); //$NON-NLS-1$
			if(certOld.exists()) {
				certOld.renameTo(new File("./cert/chat4us-public.cer_" + Helper.toDate(Instant.now(), "dd-MM-yyyy_HH-mm-ss") + ".old")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			String inputPassword = new String(tfCertPswd.getPassword());
			if(inputPassword.length() == 0)
				inputPassword = null;
			CertificateConverter.convertToPKCS12AndExportPublic(
					tfCertPath.getText(),
					tfKeyPath.getText(),
					inputPassword,
					"./cert/keystore.pfx", //$NON-NLS-1$
					new String(tfNewPswd.getPassword()),
					tfAlias.getText(),
					"./cert/chat4us-public.cer", //$NON-NLS-1$
					"AUTO" //$NON-NLS-1$
			);
			return true;
		} catch(Exception ex) {
			Helper.logError(ex, Messages.getString("CertImportDialog.CERT_FILE_IMPORT_FAILURE"), true); //$NON-NLS-1$
		}
		return false;
	}

    /**
     * Check strength of a password.
     * @param password String to check
     * @return Strength status on success.
     */
    public static String checkStrength(String password) {
        int length = password.length();

        if (length <= 8)
            return Color.RED.getRGB() + Messages.getString("CertImportDialog.WEAK"); //$NON-NLS-1$

        int strengthScore = 0;
        // Check for different character types
        if (password.matches(".*[a-z].*")) strengthScore++; // Lowercase //$NON-NLS-1$
        if (password.matches(".*[A-Z].*")) strengthScore++; // Uppercase //$NON-NLS-1$
        if (password.matches(".*\\d.*")) strengthScore++;   // Digit //$NON-NLS-1$
        if (password.matches(".*[@#$%^&+=!?.*()-_].*")) strengthScore++; // Special Character //$NON-NLS-1$

        // Assess strength based on score
        if (strengthScore < 3) {
            return Color.RED.getRGB() + Messages.getString("CertImportDialog.WEAK"); //$NON-NLS-1$
        } else if (strengthScore == 3 && length >= 12) {
            return Color.ORANGE.getRGB() + Messages.getString("CertImportDialog.MEDIUM"); //$NON-NLS-1$
        } else if (strengthScore == 4 && length >= 16) {
            return Color.GREEN.getRGB() + Messages.getString("CertImportDialog.STRONG"); //$NON-NLS-1$
        } else {
            return Color.ORANGE.getRGB() + Messages.getString("CertImportDialog.MEDIUM"); //$NON-NLS-1$
        }
    }

	/**
	 * Create the dialog.
	 * @param parent Parent frame
	 */
	public CertImportDialog(Frame parent) {
		super(parent, Messages.getString("CertImportDialog.DLG_TITLE"), true); //$NON-NLS-1$
		setTitle(Messages.getString("CertImportDialog.DLG_TITLE")); //$NON-NLS-1$
		setModalityType(ModalityType.APPLICATION_MODAL);
		//setModal(true);
		//setTitle(Messages.getString("CertImportDialog.DLG_TITLE")); //$NON-NLS-1$
		setResizable(false);
		setBounds(100, 100, 462, 396);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		JLabel lblExternalCert = new JLabel(Messages.getString("CertImportDialog.LBL_CERT_EXTERNAL")); //$NON-NLS-1$

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		JLabel lblImportedCert = new JLabel(Messages.getString("CertImportDialog.LBL_CERT_IMPORTED")); //$NON-NLS-1$

		JLabel lblAuth = new JLabel(Messages.getString("CertImportDialog.LBL_AUTH")); //$NON-NLS-1$

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_1, Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 424, Short.MAX_VALUE)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
						.addComponent(lblAuth)
						.addComponent(lblExternalCert)
						.addComponent(lblImportedCert))
					.addGap(6))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addComponent(lblAuth)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
					.addGap(10)
					.addComponent(lblExternalCert)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
					.addGap(10)
					.addComponent(lblImportedCert)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(6, Short.MAX_VALUE))
		);

		JLabel lblCurPswd = new JLabel(Messages.getString("CertImportDialog.LBL_CUR_PSWD")); //$NON-NLS-1$

		tfCurPsWd = new JPasswordField();
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblCurPswd)
					.addGap(6)
					.addComponent(tfCurPsWd, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
					.addGap(6))
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblCurPswd)
						.addComponent(tfCurPsWd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(6, Short.MAX_VALUE))
		);
		panel_2.setLayout(gl_panel_2);

						JLabel lblNewPswd1 = new JLabel(Messages.getString("CertImportDialog.NEW_PASSWORD")); //$NON-NLS-1$

										tfConfPswd = new JPasswordField();
										tfConfPswd.setColumns(10);

														tfNewPswd = new JPasswordField();
														tfNewPswd.setColumns(10);

																		lblConfPswd = new JLabel((String) null);
																		lblConfPswd.setOpaque(true);
																		lblConfPswd.setForeground(Color.WHITE);

																				lblNewPswd = new JLabel((String) null);
																				lblNewPswd.setOpaque(true);
																				lblNewPswd.setForeground(Color.WHITE);

																								JLabel lblConfPswd1 = new JLabel(Messages.getString("CertImportDialog.LBL_CONFIRMATION")); //$NON-NLS-1$

																								tfAlias = new JTextField();
																								tfAlias.setColumns(10);

																								JLabel lblAlias = new JLabel(Messages.getString("CertImportDialog.LBL_ALIAS")); //$NON-NLS-1$
																								GroupLayout gl_panel_1 = new GroupLayout(panel_1);
																								gl_panel_1.setHorizontalGroup(
																									gl_panel_1.createParallelGroup(Alignment.LEADING)
																										.addGroup(gl_panel_1.createSequentialGroup()
																											.addGap(6)
																											.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
																												.addComponent(lblConfPswd1)
																												.addComponent(lblNewPswd1)
																												.addComponent(lblAlias))
																											.addPreferredGap(ComponentPlacement.RELATED)
																											.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
																												.addGroup(gl_panel_1.createSequentialGroup()
																													.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
																														.addComponent(tfConfPswd, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
																														.addComponent(tfNewPswd, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
																													.addGap(1)
																													.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
																														.addComponent(lblNewPswd, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																														.addComponent(lblConfPswd, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)))
																												.addComponent(tfAlias, GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE))
																											.addGap(6))
																								);
																								gl_panel_1.setVerticalGroup(
																									gl_panel_1.createParallelGroup(Alignment.LEADING)
																										.addGroup(gl_panel_1.createSequentialGroup()
																											.addGap(6)
																											.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
																												.addComponent(tfAlias, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																												.addComponent(lblAlias, GroupLayout.PREFERRED_SIZE, 12, GroupLayout.PREFERRED_SIZE))
																											.addPreferredGap(ComponentPlacement.RELATED)
																											.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
																												.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
																													.addComponent(tfNewPswd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																													.addComponent(lblNewPswd1))
																												.addGroup(gl_panel_1.createSequentialGroup()
																													.addGap(3)
																													.addComponent(lblNewPswd, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)))
																											.addPreferredGap(ComponentPlacement.RELATED)
																											.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
																												.addComponent(lblConfPswd, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
																												.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
																													.addComponent(tfConfPswd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																													.addComponent(lblConfPswd1)))
																											.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																								);
																								panel_1.setLayout(gl_panel_1);
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
																	lblNewPswd.setOpaque(true);
																} else {
																	lblNewPswd.setText(""); //$NON-NLS-1$
																	lblNewPswd.setOpaque(false);
																}
															}
														});
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
													lblConfPswd.setOpaque(false);
													return;
												}
												if(new String(tfNewPswd.getPassword()).equals(new String(tfConfPswd.getPassword()))) {
													lblConfPswd.setBackground(Color.GREEN);
													lblConfPswd.setText(Messages.getString("CertImportDialog.LBL_OK")); //$NON-NLS-1$
												} else {
													lblConfPswd.setBackground(Color.RED);
													lblConfPswd.setText(Messages.getString("CertImportDialog.LBL_ERR")); //$NON-NLS-1$
												}
												lblConfPswd.setOpaque(true);
											}
										});

				JLabel lblCertPath = new JLabel(Messages.getString("CertImportDialog.LBL_CERT_PATH")); //$NON-NLS-1$

						tfCertPath = new JTextField();
						tfCertPath.setColumns(10);

										JLabel lblCertPswd = new JLabel(Messages.getString("CertImportDialog.CURRENT_PASSWORD")); //$NON-NLS-1$

														tfCertPswd = new JPasswordField();
														tfCertPswd.setColumns(10);

																JButton btnCertChooser = new JButton("..."); //$NON-NLS-1$
																btnCertChooser.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		JFileChooser jfc = Helper.createFileChooser(Messages.getString("CertImportDialog.JFC_DLG_TITLE"), "./cert", Messages.getString("CertImportDialog.JFC_FILTER_PEM"), "pem"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
																		jfc.setAcceptAllFileFilterUsed(true);
																		int returnVal = jfc.showOpenDialog(CertImportDialog.this);
																		if(returnVal == JFileChooser.APPROVE_OPTION) {
																			tfCertPath.setText(Helper.getRelativePath(jfc.getSelectedFile(), new File("."))); //$NON-NLS-1$
																			String alias = jfc.getSelectedFile().getName();
																			if(alias.contains(".")) //$NON-NLS-1$
																				alias = alias.substring(0, alias.indexOf('.'));
																			tfAlias.setText(alias);
																		}
																	}
																});
																btnCertChooser.setFocusable(false);

																JButton btnKeyChooser = new JButton("..."); //$NON-NLS-1$
																btnKeyChooser.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		JFileChooser jfc = Helper.createFileChooser(Messages.getString("CertImportDialog.JFC_DLG_TITLE"), "./cert", Messages.getString("CertImportDialog.JFC_FILTER_PEM"), "pem"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
																		jfc.setAcceptAllFileFilterUsed(true);
																		int returnVal = jfc.showOpenDialog(CertImportDialog.this);
																		if(returnVal == JFileChooser.APPROVE_OPTION) {
																			tfKeyPath.setText(Helper.getRelativePath(jfc.getSelectedFile(), new File("."))); //$NON-NLS-1$
																		}
																	}
																});
																btnKeyChooser.setFocusable(false);

																tfKeyPath = new JTextField();
																tfKeyPath.setColumns(10);

																JLabel lblPrivateKey = new JLabel(Messages.getString("CertImportDialog.LBL_PRIVATE_KEY")); //$NON-NLS-1$
																GroupLayout gl_panel = new GroupLayout(panel);
																gl_panel.setHorizontalGroup(
																	gl_panel.createParallelGroup(Alignment.LEADING)
																		.addGroup(gl_panel.createSequentialGroup()
																			.addContainerGap()
																			.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
																				.addComponent(lblCertPath)
																				.addComponent(lblCertPswd)
																				.addComponent(lblPrivateKey))
																			.addPreferredGap(ComponentPlacement.RELATED)
																			.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
																				.addComponent(tfCertPswd, GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
																				.addGroup(gl_panel.createSequentialGroup()
																					.addComponent(tfKeyPath, GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
																					.addPreferredGap(ComponentPlacement.RELATED)
																					.addComponent(btnKeyChooser, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
																				.addGroup(gl_panel.createSequentialGroup()
																					.addComponent(tfCertPath, GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
																					.addPreferredGap(ComponentPlacement.RELATED)
																					.addComponent(btnCertChooser)))
																			.addGap(6))
																);
																gl_panel.setVerticalGroup(
																	gl_panel.createParallelGroup(Alignment.LEADING)
																		.addGroup(gl_panel.createSequentialGroup()
																			.addGap(6)
																			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
																				.addComponent(tfCertPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																				.addComponent(btnCertChooser)
																				.addComponent(lblCertPath))
																			.addPreferredGap(ComponentPlacement.RELATED)
																			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
																				.addComponent(btnKeyChooser)
																				.addComponent(tfKeyPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																				.addComponent(lblPrivateKey))
																			.addPreferredGap(ComponentPlacement.RELATED)
																			.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
																				.addComponent(tfCertPswd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																				.addComponent(lblCertPswd))
																			.addGap(6))
																);
																panel.setLayout(gl_panel);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("CertImportDialog.BTN_GENERATE")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(Color.ORANGE.equals(lblNewPswd.getBackground())) {
							if(Messages.getString("CertImportDialog.OK_VALUE").equals(lblConfPswd.getText())) { //$NON-NLS-1$
								if(!new String(tfNewPswd.getPassword()).equals(new String(tfCurPsWd.getPassword()))) {
									int ret = Helper.showConfirmDialog(CertImportDialog.this, Messages.getString("CertImportDialog.MB_WARNING_CERT_GEN"), Messages.getString("CertImportDialog.MB_WARNING_CERT_GEN_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
									if(ret == JOptionPane.YES_OPTION) {
										progressBar.setVisible(true);
										new Thread(() -> {
											if(importCertificate()) {
												SwingUtilities.invokeLater(() -> {
													Helper.logInfo(Messages.getString("CertImportDialog.NEW_CERT_GEN_SUCCESS"), true); //$NON-NLS-1$
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
						Helper.logInfo(Messages.getString("CertImportDialog.DATA_ERROR"), true); //$NON-NLS-1$
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
				cancelButton = new JButton(Messages.getString("CertImportDialog.BTN_CANCEL")); //$NON-NLS-1$
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
				buttonPane.add(cancelButton);
			}
		}
		Helper.registerCancelByEsc(this, cancelButton);
		Helper.enableRtlWhenNeeded(this);
		pack();
	}
}
