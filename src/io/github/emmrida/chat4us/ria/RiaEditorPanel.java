/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.ria;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.CustomSaveFileChooser;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

/**
 * The Class RiaEditorPanel. Manages the chat flows nodes diagram.
 *
 * @author El Mhadder Mohamed Rida
 */
public class RiaEditorPanel extends JPanel {
	private static final long serialVersionUID = -717972515600757002L;

	private static final int SCROLL_PADDING = 30;
	public static final String EDITOR_TITLE = ""; //$NON-NLS-1$
	public static final String NEW_DOC_NAME = Messages.getString("RiaEditorPanel.DOC_NO_NAME"); //$NON-NLS-1$

	private int nextId = 1;
	private boolean modified = false;
	private String riaFile = null;

	private int entryId = 1;
	private String botLocale = Messages.getString("RiaEditorPanel.DEF_LOCALE"); //$NON-NLS-1$
	private String botName = ""; //$NON-NLS-1$
	private String botGuidelines = ""; //$NON-NLS-1$
	private String ls = System.lineSeparator();
	private String botScript = String.format("function onUserMessage(msg) {%s\treturn -999; // Ignore & continue...%s}%s%sfunction onAIMessage(msg) {%s\treturn -999; // Ignore & continue...%s}%s", ls, ls, ls, ls, ls, ls, ls); //$NON-NLS-1$
	private Map<String, String> botParams = null;

	private int tabIndex = -1;
	private JTabbedPane parentTab = null;

	private CustomSaveFileChooser saveFileChooser = null;
	private JFileChooser openFileChooser = null;
	private NodePanel routeItemToLink = null;
	private JPopupMenu popupMenu = null;

	/**
	 * Instantiates a new ria editor panel.
	 *
	 * @param parentTab the parent tab
	 * @param tabIndex the tab index
	 */
	public RiaEditorPanel(JTabbedPane parentTab, int tabIndex) {
		super();

		this.modified = false;
		this.tabIndex = tabIndex;
		this.parentTab = parentTab;
		this.botParams = new HashMap<>();

        setLayout(null);
		setFocusable(true);
		setOpaque(false);
		setDoubleBuffered(true);

		initPopupMenu();

		saveFileChooser = new CustomSaveFileChooser();
		saveFileChooser.setCurrentDirectory(new java.io.File(MainWindow.CHATBOTS_ROOT_FOLDER)); //$NON-NLS-1$
		saveFileChooser.setFileSelectionMode(CustomSaveFileChooser.FILES_ONLY);
		saveFileChooser.setMultiSelectionEnabled(false);
		saveFileChooser.setAcceptAllFileFilterUsed(false);
		saveFileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("RiaEditorPanel.FCS_RIA_FILTER"), "ria")); //$NON-NLS-1$ //$NON-NLS-2$
		saveFileChooser.setDialogTitle(Messages.getString("RiaEditorPanel.FCS_TITLE")); //$NON-NLS-1$
		openFileChooser = new JFileChooser();
		openFileChooser.setCurrentDirectory(new java.io.File(MainWindow.CHATBOTS_ROOT_FOLDER)); //$NON-NLS-1$
		openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		openFileChooser.setMultiSelectionEnabled(false);
		openFileChooser.setAcceptAllFileFilterUsed(false);
		openFileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("RiaEditorPanel.FCO_RIA_FILTER"), "ria")); //$NON-NLS-1$ //$NON-NLS-2$
		openFileChooser.setDialogTitle(Messages.getString("RiaEditorPanel.FCO_TITLE")); //$NON-NLS-1$

		SwingUtilities.invokeLater(() -> {
			Container c = getParent();
			if(c instanceof JViewport) {
				((JViewport)c).addChangeListener(e -> updateSize());
			}
		});

		/*
		 *
		 */
        addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocus();
				showPopupMenu(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}

			private void showPopupMenu(MouseEvent e) {
				if(e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

        /*
         *
         */
        addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				fillChatBotMenu();
			}
			@Override
			public void focusLost(FocusEvent e) { }
        });

        Helper.enableRtlWhenNeeded(this);
	}

	/**
	 * Fill chat bot menu.
	 */
	public void fillChatBotMenu() {
		JMenu menu = MainWindow.getChatBotMenuItem();
		for(int i = menu.getItemCount()-1; i > 2; i--)
			menu.remove(i);
		JMenuItem mnuSave = new JMenuItem(Messages.getString("RiaEditorPanel.MNU_SAVE")); //$NON-NLS-1$
		mnuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mnuSave.addActionListener(ev -> {
			saveRiaDocument();
		});
		menu.add(mnuSave);
		menu.addSeparator();
		JMenuItem mnuPaste = null;
		for(int i = 0; i < popupMenu.getComponentCount(); i++) {
           if(popupMenu.getComponent(i) instanceof JMenuItem) {
                JMenuItem originalItem = (JMenuItem) popupMenu.getComponent(i);
                JMenuItem clonedItem = new JMenuItem(originalItem.getText());
                if(clonedItem.getText().equals(Messages.getString("RiaEditorPanel.MNU_PASTE"))) //$NON-NLS-1$
                    mnuPaste = clonedItem;
                for (ActionListener al : originalItem.getActionListeners())
                    clonedItem.addActionListener(al);
                clonedItem.setAccelerator(originalItem.getAccelerator());
                menu.add(clonedItem);
            } else menu.addSeparator();
		}
		menu.addSeparator();
		JMenuItem mnuClose = new JMenuItem(Messages.getString("RiaEditorPanel.MNU_CLOSE")); //$NON-NLS-1$
		mnuClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		mnuClose.addActionListener(ev -> {
			RiaEditorPanel.this.onClose();
		});
		menu.add(mnuClose);

		final JMenuItem mnuPasteF = mnuPaste;
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				mnuSave.setEnabled(modified);
				if(mnuPasteF != null) {
					String xml = Helper.getTextFromClipboard();
					if(xml != null && xml.startsWith("\t\t<question>")) { //$NON-NLS-1$
						mnuPasteF.setEnabled(true);
					} else mnuPasteF.setEnabled(false);
				}
			}
			@Override
			public void menuDeselected(MenuEvent e) { }
			@Override
			public void menuCanceled(MenuEvent e) { }
		});
	}

	/**
	 * Save ria document.
	 * @return true if successful
	 */
	public boolean saveRiaDocument() {
		if(modified) {
			if(riaFile == null) {
				saveFileChooser.setDialogTitle(Messages.getString("RiaEditorPanel.FCS_TITLE_LONG")); //$NON-NLS-1$
				int ret = saveFileChooser.showSaveDialog(MainWindow.getFrame());
				if(ret == CustomSaveFileChooser.APPROVE_OPTION) {
					riaFile = saveFileChooser.getSelectedFile().getAbsolutePath();
					if(!riaFile.endsWith(".ria")) //$NON-NLS-1$
						riaFile += ".ria"; //$NON-NLS-1$
				} else return false;
			}
			return saveDocument(riaFile);
		}
		return false;
	}

	/**
	 * Save ria document as.
	 * @return true if successful
	 */
	public boolean saveRiaDocumentAs() {
		saveFileChooser.setDialogTitle(Messages.getString("RiaEditorPanel.FCS_AS_TITLE")); //$NON-NLS-1$
		int ret = saveFileChooser.showSaveDialog(MainWindow.getFrame());
		if(ret == CustomSaveFileChooser.APPROVE_OPTION) {
			riaFile = saveFileChooser.getSelectedFile().getAbsolutePath();
			if(!riaFile.endsWith(".ria")) //$NON-NLS-1$
				riaFile += ".ria"; //$NON-NLS-1$
		} else return false;
		setModified(true);
		return saveDocument(riaFile);
	}

	/**
	 * Inits the popup menu.
	 */
	private void initPopupMenu() {
		popupMenu = new JPopupMenu();
		JMenuItem mnuAdd = new JMenuItem(Messages.getString("RiaEditorPanel.MNU_ADD")); //$NON-NLS-1$
		mnuAdd.addActionListener(e -> {
			addNew();
		});
		popupMenu.add(mnuAdd);
		JMenuItem mnuPaste = new JMenuItem(Messages.getString("RiaEditorPanel.MNU_PASTE")); //$NON-NLS-1$
		mnuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		mnuPaste.addActionListener(e -> {
			paste();
		});
		popupMenu.add(mnuPaste);

		popupMenu.addSeparator();

		JMenuItem mnuRemoveAll = new JMenuItem(Messages.getString("RiaEditorPanel.MNU_DEL_ALL")); //$NON-NLS-1$
		mnuRemoveAll.addActionListener(e -> {
			if(JOptionPane.YES_OPTION == Helper.showConfirmDialog(MainWindow.getFrame(), Messages.getString("RiaEditorPanel.MB_DELALL_MSG"), Messages.getString("RiaEditorPanel.MB_DELALL_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1)) { //$NON-NLS-1$ //$NON-NLS-2$
				for(Component c : getComponents()) {
					if(c instanceof NodePanel) {
						NodePanel node = (NodePanel)c;
						addNodeState(NodeState.OPERATION.OP_ROUTE_DELETED, node);
					}
				}
				removeAll();
				updateSize();
				setModified(false);
				riaFile = null;
				nextId = 1;
				botName = ""; //$NON-NLS-1$
				botGuidelines = ""; //$NON-NLS-1$
				botScript = ""; //$NON-NLS-1$
				botParams.clear();
				//parentTab.setTitleAt(tabIndex, EDITOR_TITLE + NEW_DOC_NAME);
				MainWindow.getInstance().setTabTitle(tabIndex, EDITOR_TITLE + NEW_DOC_NAME);
				repaint();
			}
		});
		popupMenu.add(mnuRemoveAll);

		popupMenu.addSeparator();

		JMenuItem mnuOptions = new JMenuItem(Messages.getString("RiaEditorPanel.MNU_OPTIONS")); //$NON-NLS-1$
		mnuOptions.addActionListener(e -> {
			ChatBotSettingsDialog dialog = new ChatBotSettingsDialog(MainWindow.getFrame(), this.botName, this.botLocale, this.botGuidelines, this.botScript, this.botParams);
			dialog.setVisible(true);
			if(dialog.isModified) {
				this.botName = dialog.botName;
				this.botLocale = dialog.botLocale;
				this.botGuidelines = dialog.botGuidelines;
				this.botScript = dialog.botScript;
				this.botParams = (HashMap<String, String>)((HashMap<String, String>)dialog.botParams).clone();
				this.setModified(true);
				// TODO : Add undo/redo support for this
			}
			dialog.dispose();
		});
		popupMenu.add(mnuOptions);

		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				String xml = Helper.getTextFromClipboard();
				if(xml != null && xml.startsWith("\t\t<question>")) { //$NON-NLS-1$
					mnuPaste.setEnabled(true);
				} else mnuPaste.setEnabled(false);
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) { }

		});
	}

	/**
	 * Adds a new Node to the editor.
	 */
	public void addNew() {
		Point pos = RiaEditorPanel.this.getMousePosition();
		if(pos == null) {
			JViewport vp = (JViewport)getParent();
			pos = new Point(vp.getViewPosition().x, vp.getViewPosition().y);
			pos.x = pos.x + vp.getWidth()/2;
			pos.y = pos.y + vp.getHeight()/2;
		}
		addNode(pos.x, pos.y);
		updateSize();
		setModified(true);
	}

	/**
	 * Paste a Node from the clipboard.
	 */
	public void paste() {
		String xml = Helper.getTextFromClipboard();
		if(xml != null && xml.startsWith("\t\t<question>")) { //$NON-NLS-1$
			xml = xml.replaceFirst("<id>\\d+</id>", "<id>" + nextId + "</id>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			NodePanel.Data data = NodePanel.Data.fromXml(xml);
			Point pos = RiaEditorPanel.this.getMousePosition();
			if(pos == null) {
				JViewport vp = (JViewport)getParent();
				pos = new Point(vp.getViewPosition().x, vp.getViewPosition().y);
				pos.x = pos.x + vp.getWidth()/2;
				pos.y = pos.y + vp.getHeight()/2;
			}
			NodePanel node = new NodePanel(this, nextId);
			data.setBounds(pos.x, pos.y, data.getWidth(), data.getHeight());
			node.setBounds(pos.x, pos.y, data.getWidth(), data.getHeight());
			node.setData(data);
			add(node);
			updateSize();
			NodePanel suc = getNodeById(data.getSucMoveTo());
			if(suc != null)
				node.setRelSuccessPanel(suc);
			NodePanel err = getNodeById(data.getErrMoveTo());
			if(err != null)
				node.setRelErrorPanel(err);
			setModified(true);
			nextId++;
			repaint();
			setComponentZOrder(node, 0);
			addNodeState(NodeState.OPERATION.OP_ROUTE_PASTED, node);
		} else Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Adds a Node to the editor at the specified location.
	 *
	 * @param x the x position
	 * @param y the y position
	 * @return the Node panel object
	 */
	private NodePanel addNode(int x, int y) {
		NodePanel node = new NodePanel(this, nextId++);
		node.getData().setBounds(x, y, 100, 100);
		node.setBounds(x, y, 100, 100);
		node.requestFocus();
		add(node);
		setModified(true);
		repaint();
		addNodeState(NodeState.OPERATION.OP_ROUTE_CREATED, node);
		return node;
	}

	/**
	 * Paint component.
	 *
	 * @param g the graphic object
	 */
	@Override
	public void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);

		Rectangle clip = g.getClipBounds();
		for(Component c : getComponents())
			if(c instanceof NodePanel)
				if(clip.intersects(clip.getBounds()))
					((NodePanel)c).paintOnParent(g);
		if(routeItemToLink != null)
			routeItemToLink.paintOnParent(g);
	}

	/**
	 * Gets the Node at the specified location.
	 *
	 * @param x the x position
	 * @param y the y position
	 * @return the Node at the specified location
	 */
	public NodePanel getNodeAt(int x, int y) {
		for(Component c : getComponents()) {
			if(c instanceof NodePanel) {
				if(((NodePanel)c).getBounds().contains(x, y)) {
					return (NodePanel)c;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the Nodes ids.
	 *
	 * @return the Nodes ids
	 */
	public List<Integer> getNodesIds() {
		List<Integer> ids = new java.util.ArrayList<>();
		for(Component c : getComponents()) {
			if(c instanceof NodePanel) {
				ids.add(((NodePanel)c).getData().getId());
			}
		}
		return ids;
	}

	/**
	 * Gets a Node by its id.
	 *
	 * @param id the id of the Node
	 * @return A Node object or null
	 */
	public NodePanel getNodeById(int id) {
		for(Component c : getComponents()) {
			if(c instanceof NodePanel) {
				if(((NodePanel)c).getData().getId() == id) {
					return (NodePanel)c;
				}
			}
		}
		return null;
	}

	/**
	 * Load a RIA document from file.
	 *
	 * @param riaFile The RIA file name/path.
	 * @throws Exception
	 */
	public void loadDocument(String xmlFile) throws Exception {
		this.removeAll();
		this.riaFile = xmlFile;
		File file = new File(xmlFile);
		MainWindow.getInstance().setTabTitle(tabIndex, EDITOR_TITLE + file.getName() + " "); //$NON-NLS-1$
		if(botParams == null)
			botParams = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        document.getDocumentElement().normalize();

        NodeList nodes = document.getElementsByTagName("info").item(0).getChildNodes(); //$NON-NLS-1$
        this.entryId = Integer.parseInt(Helper.getNodeValue(nodes, "entry_id")); //$NON-NLS-1$
        this.botLocale = Helper.getNodeValue(nodes, "locale"); //$NON-NLS-1$
        nodes = document.getElementsByTagName("ai_model").item(0).getChildNodes(); //$NON-NLS-1$
        this.botName = Helper.getNodeValue(nodes, "name"); //$NON-NLS-1$
        this.botGuidelines = Helper.getNodeValue(nodes, "guidelines"); //$NON-NLS-1$
        this.botScript = Helper.unescapeHtml(Helper.getNodeValue(nodes, "script")); //$NON-NLS-1$
        nodes = document.getElementsByTagName("params").item(0).getChildNodes(); //$NON-NLS-1$
        for(int i = 0; i < nodes.getLength(); i++) {
        	Node node = nodes.item(i);
        	if(node.getNodeType() == Node.ELEMENT_NODE) {
        		Node item = Helper.getNodeByName(node.getChildNodes(), "key").getChildNodes().item(0); //$NON-NLS-1$
	        	String param = ""; //$NON-NLS-1$
	        	if(item != null)
	        		param = item.getNodeValue();
	        	item = Helper.getNodeByName(node.getChildNodes(), "value").getChildNodes().item(0); //$NON-NLS-1$
	        	String value = ""; //$NON-NLS-1$
	        	if(item != null)
	        		value = item.getNodeValue();
	        	this.botParams.put(param, value);
        	}
        }

        nodes = document.getElementsByTagName("question"); //$NON-NLS-1$
        for(int i = 0; i < nodes.getLength(); i++) {
        	Node node = nodes.item(i);
        	NodePanel.Data data = NodePanel.Data.fromXml(node);
			NodePanel np = addNode(data.getXPos(), data.getYPos());
			np.setData(data);
			nextId = data.getId() + 1;
        }

		for(Component c : getComponents()) {
			if(c instanceof NodePanel) {
				NodePanel node = (NodePanel)c;
				NodePanel.Data data = node.getData();
				if(data.getSucMoveTo() != 0) {
					NodePanel nodeTo = getNodeById(data.getSucMoveTo());
					if(nodeTo != null)
						node.setRelSuccessPanel(nodeTo);
				}
				if(data.getErrMoveTo() != 0) {
					NodePanel nodeTo = getNodeById(data.getErrMoveTo());
					if(nodeTo != null)
						node.setRelErrorPanel(nodeTo);
				}
			}
		}
		setModified(false);
		repaint();
	}

	/**
	 * Save current RIA document into a file.
	 *
	 * @param riaFile File path of the RIA document.
	 * @return true if successful
	 */
	public boolean saveDocument(String xmlFile) {
		Helper.requiresNotEmpty(xmlFile);
		// TODO : Check file name/extension
		this.riaFile = xmlFile;
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(xmlFile))) {
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("<route>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("\t<info>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("\t\t<entry_id>" + entryId + "</entry_id>" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
			bw.write("\t\t<locale>" + botLocale + "</locale>" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
			bw.write("\t\t<ai_model>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("\t\t\t<name>" + botName + "</name>" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
			bw.write("\t\t\t<guidelines>" + botGuidelines + "</guidelines>" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
			bw.write("\t\t\t<script>" + Helper.escapeHtml(botScript) + "</script>" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
			bw.write("\t\t\t<params>" + System.lineSeparator()); //$NON-NLS-1$
			for(Map.Entry<String, String> entry : botParams.entrySet()) {
				if(!entry.getKey().isBlank()) {
					bw.write("\t\t\t\t<param>" + System.lineSeparator()); //$NON-NLS-1$
					bw.write("\t\t\t\t\t<key>" + entry.getKey() + "</key>" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
					bw.write("\t\t\t\t\t<value>" + entry.getValue() + "</value>" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
					bw.write("\t\t\t\t</param>" + System.lineSeparator()); //$NON-NLS-1$
				}
			}
			bw.write("\t\t\t</params>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("\t\t</ai_model>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("\t</info>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("\t<questions>" + System.lineSeparator()); //$NON-NLS-1$
			for(Component c : getComponents()) {
				if(c instanceof NodePanel) {
					bw.write(((NodePanel)c).getData().toXml());
				}
			}
			bw.write("\t</questions>" + System.lineSeparator()); //$NON-NLS-1$
			bw.write("</route>" + System.lineSeparator()); //$NON-NLS-1$
			bw.flush();
			setModified(false);
			return true;
		} catch (IOException ex) {
			Helper.logError(ex, Messages.getString("RiaEditorPanel.EX_LOADING_RIA_FILE"), true); //$NON-NLS-1$
			return false;
		}
	}

	/**
	 * Removes the route item.
	 *
	 * @param node the node
	 */
	public void removeNode(NodePanel node) {
		if(node == null) return;
		node.setRelSuccessPanel(null);
		node.setRelErrorPanel(null);
		for(Component c : getComponents()) {
			if(c instanceof NodePanel) {
				NodePanel r = (NodePanel)c;
				if(!node.equals(r)) {
					NodePanel.Data data = r.getData();
					if(data.getSucMoveTo() == node.getData().getId()) {
						//addRipState(NodeState.OPERATION.OP_LINK_DELETED, r);
						data.setSucMoveTo(0);
						r.setRelSuccessPanel(null);
					}
					if(data.getErrMoveTo() == node.getData().getId()) {
						//addRipState(NodeState.OPERATION.OP_LINK_DELETED, r);
						data.setErrMoveTo(0);
						r.setRelErrorPanel(null);
					}
				}
			}
		}
		remove(node);
		setModified(true);
		repaint();
	}

	/**
	 * Update the editor size based on the nodes diagram.
	 */
	public void updateSize() {
		JViewport vp = (JViewport)getParent();
		Dimension d = vp.getSize();
		for(Component c : getComponents()) {
			if(c instanceof NodePanel) {
				if(c.getX() + c.getWidth() > d.getWidth() - SCROLL_PADDING)
					d.width = c.getX() + c.getWidth() + SCROLL_PADDING;
				if(c.getY() + c.getHeight() > d.getHeight() - SCROLL_PADDING)
					d.height = c.getY() + c.getHeight() + SCROLL_PADDING;
			}
		}
		setSize(d);
		setPreferredSize(d);
		revalidate();
		repaint();

		float v = 16.0f / Math.max(getHeight(), getWidth());
        ((JScrollPane)vp.getParent()).getVerticalScrollBar().setUnitIncrement((int)(getHeight() * v));
        ((JScrollPane)vp.getParent()).getHorizontalScrollBar().setUnitIncrement((int)(getWidth() * v));
	}

	/**
	 * Open a RIA file.
	 *
	 * @param riaFile the ria file
	 */
	public void openRiaFile(String riaFile) {
		try {
			loadDocument(riaFile);
			updateSize();
		} catch (Exception ex) {
			MainWindow.getInstance().closeRouteEditorTab(tabIndex);
			JMenu menu = MainWindow.getChatBotMenuItem();
			for(int i = menu.getItemCount()-1; i > 2; i--)
				menu.remove(i);
			Helper.logWarning(ex, Messages.getString("RiaEditorPanel.LOG_WA_RIA_OPEN_ERROR") + riaFile, true); //$NON-NLS-1$
		}
	}

	/**
	 * Called to close the editor.
	 *
	 * @return JOptionPane.CANCEL_OPTION if the user cancelled the operation, JOptionPane.YES_OPTION if the operation was successful
	 */
	public int onClose() {
		if(modified) {
			int ret = Helper.showConfirmDialog(MainWindow.getFrame(), Messages.getString("RiaEditorPanel.MB_SAVE_MSG"), Messages.getString("RiaEditorPanel.MB_SAVE_TITLE"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, 2); //$NON-NLS-1$ //$NON-NLS-2$
			if(ret == JOptionPane.YES_OPTION) {
				if(riaFile == null) {
					int ret2 = saveFileChooser.showSaveDialog(MainWindow.getFrame());
					if(ret2 == CustomSaveFileChooser.APPROVE_OPTION) {
						riaFile = saveFileChooser.getSelectedFile().getAbsolutePath();
						if(!riaFile.endsWith(".ria")) //$NON-NLS-1$
							riaFile += ".ria"; //$NON-NLS-1$
					} else return JOptionPane.CANCEL_OPTION;
				}
				saveDocument(riaFile);
			} else if(ret == JOptionPane.CANCEL_OPTION) {
				return JOptionPane.CANCEL_OPTION;
			}
		}
		JMenu menu = MainWindow.getChatBotMenuItem();
		for(int i = menu.getItemCount()-1; i > 2; i--)
			menu.remove(i);
		removeAll();
		setModified(false);
		riaFile = null;
		nextId = 1;
		botName = ""; //$NON-NLS-1$
		botGuidelines = ""; //$NON-NLS-1$
		botScript = ""; //$NON-NLS-1$
		botParams.clear();
		//parentTab.setTitleAt(tabIndex, EDITOR_TITLE + NEW_DOC_NAME);
		MainWindow.getInstance().setTabTitle(tabIndex, EDITOR_TITLE + NEW_DOC_NAME);
		MainWindow.getInstance().closeRouteEditorTab(tabIndex);
		return JOptionPane.YES_OPTION;
	}

	/**
	 * Gets the entry node id.
	 *
	 * @return the entry node id
	 */
	public int getEntryId() { return entryId; }

	/**
	 * Sets the entry node id.
	 *
	 * @param entryId the new entry node id
	 */
	public void setEntryId(int entryId) {
		NodePanel node = getNodeById(entryId);
		if(node == null)
			throw new IllegalArgumentException(Messages.getString("RiaEditorPanel.EX_INVALID_ENTRY_ID")); //$NON-NLS-1$
		this.entryId = entryId;
		setModified(true);
	}

	/**
	 * Gets the selected node.
	 *
	 * @return the selected node
	 */
	public NodePanel getSelectedRouteItem() {
		NodePanel node = null;
		for(Component c : getComponents()) {
			if(c instanceof NodePanel) {
				if(((NodePanel)c).isSelected()) {
					node = (NodePanel)c;
					break;
				}
			}
		}
		return node;
	}

	/**
	 * Sets the editor as modified.
	 *
	 * @param modified the new modified state
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
		MainWindow mw = MainWindow.getInstance();
		String title;
		if(riaFile != null) {
			if(modified) {
				title = EDITOR_TITLE + new File(riaFile).getName() + "* "; //$NON-NLS-1$
			} else title = EDITOR_TITLE + new File(riaFile).getName() + " "; //$NON-NLS-1$
		} else {
			if(modified) {
				title = EDITOR_TITLE + Messages.getString("RiaEditorPanel.DOC_NO_NAME") + "* "; //$NON-NLS-1$ //$NON-NLS-2$
			} else title = EDITOR_TITLE + Messages.getString("RiaEditorPanel.DOC_NO_NAME"); //$NON-NLS-1$
		}
		mw.setTabTitle(tabIndex, title);
	}

	/**
	 * Checks if the editor is modified.
	 *
	 * @return true, if the editor is modified
	 */
	public boolean isModified() { return modified; }

	/**
	 * Gets the tab index of the editor in the parent tab pane.
	 *
	 * @return the tab index
	 */
	public int getTabIndex() { return tabIndex; }

	/**
	 * Sets the tab index of the editor in the parent tab pane.
	 *
	 * @param tabIndex the new tab index
	 */
	public void setTabIndex(int tabIndex) { this.tabIndex = tabIndex; }

	/**
	 * Gets the node to link.
	 *
	 * @return the node to link
	 */
	public NodePanel getNodeToLink() { return routeItemToLink; }

	/**
	 * Start node linking.
	 *
	 * @param nodeToLink the node to link
	 */
	public void startNodeLinking(NodePanel nodeToLink) { this.routeItemToLink = nodeToLink; }

	/**
	 * End node linking.
	 */
	public void endNodeLinking() { this.routeItemToLink = null; }

	/**
	 * Gets the ria file.
	 *
	 * @return the ria file
	 */
	public String getRiaFile() { return riaFile; }

	/*
	 * Undo/Redo section
	 */

	private Stack<NodeState> undoStack = new Stack<>();
	private Stack<NodeState> redoStack = new Stack<>();

	/**
	 * Adds a node with its state to the undo stack.
	 *
	 * @param opid the opid
	 * @param node the node
	 */
	public void addNodeState(NodeState.OPERATION opid, NodePanel node) {
		//System.out.println("Undo : " + undoStack.size() + " Redo : " + redoStack.size());
		undoStack.push(new NodeState(opid, node));
		redoStack.clear();
	}

	/**
	 * Performs the undo action on the editor.
	 */
	public void undo() {
		// TODO : This triggers a call to addRipState
		if(undoStack.size() > 0) {
			NodeState state = undoStack.pop();
			redoStack.push(state);
			NodePanel node = state.getNode();
			NodePanel.Data data = state.getData();
			switch(state.getOperationId()) {
				case OP_ROUTE_PASTED:
				case OP_ROUTE_CREATED:
					NodePanel.Data old = new NodePanel.Data(node.getData());
					removeNode(node);
					state.setData(old);
					break;
				case OP_ROUTE_CUTTED:
				case OP_ROUTE_DELETED:
					old = new NodePanel.Data(node.getData());
					restoreNode(node, data);
					state.setData(old);
					break;
				case OP_EDIT_DIALOG:
					old = new NodePanel.Data(node.getData());
					node.setData(data);
					state.setData(old);
					break;
				case OP_ROUTE_MOVED:
					int x = node.getX();
					int y = node.getY();
					node.setBounds(data.getXPos(), data.getYPos(), node.getWidth(), node.getHeight());
					data.setXPos(x);
					data.setYPos(y);
					break;
				case OP_ROUTE_RESIZED:
					int w = node.getWidth();
					int h = node.getHeight();
					node.setBounds(node.getX(), node.getY(), data.getWidth(), data.getHeight());
					data.setWidth(w);
					data.setHeight(h);
					break;
				case OP_SUCCESS_CHANGED: {
						String msg = node.getData().getSucMessage();
						String action = node.getData().getSucAction();
						String value = node.getData().getSucValue();
						int moveTo = node.getData().getSucMoveTo();
						node.getData().setSuccess(data.getSucMessage(), data.getSucAction(), data.getSucValue(), data.getSucMoveTo());
						data.setSuccess(msg, action, value, moveTo);
						moveTo = node.getData().getSucMoveTo();
						if(moveTo > 0) {
							NodePanel r = getNodeById(moveTo);
							if(r != null)
								node.setRelSuccessPanel(r);
						} else node.setRelSuccessPanel(null);
					}
					break;
				case OP_ERROR_CHANGED: {
						String msg = node.getData().getErrMessage();
						String action = node.getData().getErrAction();
						String value = node.getData().getErrValue();
						int moveTo = node.getData().getErrMoveTo();
						node.getData().setError(data.getErrMessage(), data.getErrAction(), data.getErrValue(), data.getErrMoveTo());
						data.setError(msg, action, value, moveTo);
						moveTo = node.getData().getErrMoveTo();
						if(moveTo > 0) {
							NodePanel r = getNodeById(moveTo);
							if(r != null)
								node.setRelErrorPanel(r);
						} else node.setRelErrorPanel(null);
					}
					break;
				default:
					Helper.logWarning(String.format(Messages.getString("RiaEditorPanel.LOG_WA_UNDO_OP"), state.getOperationId().toString())); //$NON-NLS-1$ //$NON-NLS-2$
					break;
			}
			repaint();
			revalidate();
			updateSize();
			setModified(true);
		} else Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Performs the redo action on the editor.
	 */
	public void redo() {
		// TODO : This triggers a call to addRipState
		if(redoStack.size() > 0) {
			NodeState state = redoStack.pop();
			undoStack.push(state);
			NodePanel node = state.getNode();
			NodePanel.Data data = state.getData();
			switch(state.getOperationId()) {
				case OP_ROUTE_PASTED:
				case OP_ROUTE_CREATED:
					NodePanel.Data old = new NodePanel.Data(node.getData());
					restoreNode(node, data);
					state.setData(old);
					break;
				case OP_ROUTE_CUTTED:
				case OP_ROUTE_DELETED:
					old = new NodePanel.Data(node.getData());
					removeNode(node);
					state.setData(old);
					break;
				case OP_EDIT_DIALOG:
					old = new NodePanel.Data(node.getData());
					node.setData(data);
					state.setData(old);
					break;
				case OP_ROUTE_MOVED:
					int x = node.getX();
					int y = node.getY();
					node.setBounds(data.getXPos(), data.getYPos(), node.getWidth(), node.getHeight());
					data.setXPos(x);
					data.setYPos(y);
					break;
				case OP_ROUTE_RESIZED:
					int w = node.getWidth();
					int h = node.getHeight();
					node.setBounds(node.getX(), node.getY(), data.getWidth(), data.getHeight());
					data.setWidth(w);
					data.setHeight(h);
					break;
				case OP_SUCCESS_CHANGED: {
						String msg = node.getData().getSucMessage();
						String action = node.getData().getSucAction();
						String value = node.getData().getSucValue();
						int moveTo = node.getData().getSucMoveTo();
						node.getData().setSuccess(data.getSucMessage(), data.getSucAction(), data.getSucValue(), data.getSucMoveTo());
						data.setSuccess(msg, action, value, moveTo);
						moveTo = node.getData().getSucMoveTo();
						if(moveTo > 0) {
							NodePanel r = getNodeById(moveTo);
							if(r != null)
								node.setRelSuccessPanel(r);
						} else node.setRelSuccessPanel(null);
					}
					break;
				case OP_ERROR_CHANGED: {
					String msg = node.getData().getErrMessage();
					String action = node.getData().getErrAction();
					String value = node.getData().getErrValue();
					int moveTo = node.getData().getErrMoveTo();
					node.getData().setError(data.getErrMessage(), data.getErrAction(), data.getErrValue(), data.getErrMoveTo());
					data.setError(msg, action, value, moveTo);
					moveTo = node.getData().getErrMoveTo();
					if(moveTo > 0) {
						NodePanel r = getNodeById(moveTo);
						if(r != null)
							node.setRelErrorPanel(r);
					} else node.setRelErrorPanel(null);
				}
				break;
				default:
					Helper.logWarning(String.format(Messages.getString("RiaEditorPanel.LOG_WA_REDO_OP"), state.getOperationId().toString())); //$NON-NLS-1$ //$NON-NLS-2$
					break;
			}
			repaint();
			revalidate();
			updateSize();
			setModified(true);
		} else Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Restore a node removed by an undo or redo operation.
	 *
	 * @param node the node to restore
	 * @param data the data of the node to restore
	 */
	private void restoreNode(NodePanel node, NodePanel.Data data) {
		add(node);
		node.setData(data);
		setComponentZOrder(node, 0);
		int moveTo = data.getSucMoveTo();
		if(moveTo > 0) {
			NodePanel r = getNodeById(moveTo);
			if(r != null)
				node.setRelSuccessPanel(r);
		} else node.setRelSuccessPanel(null);
		moveTo = data.getErrMoveTo();
		if(moveTo > 0) {
			NodePanel r = getNodeById(moveTo);
			if(r != null)
				node.setRelErrorPanel(r);
		} else node.setRelErrorPanel(null);
	}

	///////////////////////////////////////////////////////////////////////////
	///
	///////////////////////////////////////////////////////////////////////////
	/**
	 * The Class NodeState that holds the undo/redo action for a node.
	 */
	public static class NodeState {
		public static enum OPERATION {OP_ROUTE_CREATED, OP_ROUTE_DELETED, OP_ROUTE_MOVED, OP_ROUTE_RESIZED, OP_ROUTE_CUTTED, OP_SUCCESS_CHANGED, OP_ERROR_CHANGED, OP_EDIT_DIALOG, OP_ROUTE_PASTED };

		private OPERATION operation;
		private NodePanel node;
		private NodePanel.Data data;

		/**
		 * Instantiates a new node state.
		 *
		 * @param op the op
		 * @param node the node
		 */
		public NodeState(OPERATION op, NodePanel node) {
			this.operation = op;
			this.node = node;
			this.data = new NodePanel.Data(node.getData());
		}

		/**
		 * Gets the operation id.
		 *
		 * @return the operation id
		 */
		public OPERATION getOperationId() { return operation; }

		/**
		 * Gets the node.
		 *
		 * @return the node
		 */
		public NodePanel getNode() { return node; }

		/**
		 * Gets the data.
		 *
		 * @return the data
		 */
		public NodePanel.Data getData() { return data; }

		/**
		 * Sets the data.
		 *
		 * @param data the new data
		 */
		public void setData(NodePanel.Data data) { this.data = data; }
	}
}











