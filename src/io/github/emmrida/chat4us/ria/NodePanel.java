/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.ria;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.emmrida.chat4us.core.ChatBotClient;
import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

/**
 * The Class NodePanel. Represent graphic node in the nodes editor.
 *
 * @author Firstname Lastname
 */
public class NodePanel extends JPanel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6874769735250935945L;

    /** Default resize handle size */
    public static final int RESIZE_HANDLE_SIZE = 7;

    /** Default link handle size */
    public static final int LINK_HANDLE_SIZE = 10;

    public static final Color SELECTED_SUCCESS_LINK_COLOR = new Color(0, 255, 0);
    public static final Color UNSELECTED_SUCCESS_LINK_COLOR = new Color(0, 160, 0);

    public static final Color SELECTED_ERROR_LINK_COLOR = new Color(255, 0, 0);
    public static final Color UNSELECTED_ERROR_LINK_COLOR = new Color(160, 0, 0);

    /**
     * The Enum RESIZE_DIR.
     */
    private static enum RESIZE_DIR {
		/** None. */
		NONE,
		/** Top left handle. */
		TOP_LEFT,
		/** Top right handle. */
		TOP_RIGHT,
		/** Bottom left handle. */
		BOTTOM_LEFT,
		/** Bottom right handle. */
		BOTTOM_RIGHT
    };

    /**
     * The Enum LINK_DIR.
     */
    private static enum LINK_DIR {
		/** None. */
        NONE,
		/** Success link. */
		SUCCESS,
		/** Error link. */
		ERROR
    };

    /** The img settings. */
    private static BufferedImage imgSettings = null;

    /** The img AI. */
    private static BufferedImage imgAI = null;

    /** The img exit. */
    private static BufferedImage imgExit = null;

    /** The img replay. */
    private static BufferedImage imgReplay = null;

    /** The img restart. */
    private static BufferedImage imgRestart = null;

    /** The img agent. */
    private static BufferedImage imgAgent = null;

    /** The img locale. */
    private static BufferedImage imgLocale = null;

    /** The img matching list. */
	private static BufferedImage imgMatchList = null;

    /** RIA editor. */
    private RiaEditorPanel editor;

    /** The id. */
    private int id = 0;

    /** Mouse start positions */
    private int startX, startY;

    /** Node start width and height */
    private int startW, startH;

    /** Currently moving the node */
    private boolean moving = false;

    /** Now linking to another node. */
    private LINK_DIR linking = LINK_DIR.NONE;

    /** Now resizing current node. */
    private RESIZE_DIR resizing = RESIZE_DIR.NONE;

    /** The this data. */
    private Data thisData = null;

    /** Current node is selected. */
    private boolean selected = false;

    /** The rel success panel. */
    private NodePanel relSuccessPanel = null;

    /** The rel error panel. */
    private NodePanel relErrorPanel = null;

    /** The popup menu. */
    private JPopupMenu popupMenu = null;

    /** The info label. */
    private JTextArea infoLabel = null;

    /**
     * Instantiates a new node panel.
     *
     * @param editor the editor panel
     * @param id the id of the node.
     */
    public NodePanel(RiaEditorPanel editor, int id) {
        super();

        this.id = id;
		this.editor = editor;
        thisData = new Data(id, Messages.getString("NodePanel.NO_NAME_NODE")); //$NON-NLS-1$
        setLayout(null);
        setFocusable(true);
        setOpaque(false);

        infoLabel = createLabel();
        infoLabel.setBounds(RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE, getWidth()-RESIZE_HANDLE_SIZE*2, getHeight()-RESIZE_HANDLE_SIZE*2);
        add(infoLabel);
        thisData.setBounds(getX(), getY(), getWidth(), getHeight());

        popupMenu = createPopupMenu();

        if(imgSettings == null || imgAI == null) {
            try {
                imgSettings = ImageIO.read(getClass().getResource("/reditor/script.png")); //$NON-NLS-1$
                imgAI = ImageIO.read(getClass().getResource("/reditor/ai_assistant.png")); //$NON-NLS-1$
                imgExit = ImageIO.read(getClass().getResource("/reditor/chat_exit.png")); //$NON-NLS-1$
                imgReplay = ImageIO.read(getClass().getResource("/reditor/repeat.png")); //$NON-NLS-1$
                imgRestart = ImageIO.read(getClass().getResource("/reditor/restart.png")); //$NON-NLS-1$
                imgLocale = ImageIO.read(getClass().getResource("/reditor/locale.png")); //$NON-NLS-1$
				imgMatchList = ImageIO.read(getClass().getResource("/reditor/match_list.png")); //$NON-NLS-1$
				imgAgent = ImageIO.read(getClass().getResource("/agent.png")); //$NON-NLS-1$
            } catch (IOException ex) {
                Helper.logError(ex, Messages.getString("NodePanel.LOG_ERR_RES_LOAD"), true); //$NON-NLS-1$
            }
        }

        /*
         *
         */
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                infoLabel.setBounds(RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE, getWidth()-RESIZE_HANDLE_SIZE*2, getHeight()-RESIZE_HANDLE_SIZE*2);
            }
        });

        /*
         *
         */
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
            	selected = true;
                repaint();
                editor.repaint();
                editor.fillChatBotMenu();
            }

            @Override
            public void focusLost(FocusEvent e) {
				selected = false;
                repaint();
                editor.repaint();
            }
        });

        /*
         *
         */
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            	selected = true;
				requestFocus();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    if(e.getClickCount() == 2) {
                        ((JMenuItem)popupMenu.getComponent(0)).doClick();
                        e.consume();
                        return;
                    }
                    startX = e.getX();
                    startY = e.getY();
                    if(!moving) {
                        if(linking == LINK_DIR.NONE) {
                            if(startX >= getWidth()-LINK_HANDLE_SIZE && startX <= getWidth()) {
                                if(startY >= getHeight()/2 - LINK_HANDLE_SIZE - 1 && startY <= getHeight()/2 - 1) {
                                    if(relSuccessPanel == null && "nop".equals(thisData.getSucAction())) { //$NON-NLS-1$
                                        moving = false;
                                        linking = LINK_DIR.SUCCESS;
                                        resizing = RESIZE_DIR.NONE;
                                        editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_SUCCESS_CHANGED, NodePanel.this);
                                        editor.startNodeLinking(NodePanel.this);
                                    } else {
                                        int ret = Helper.showConfirmDialog(MainWindow.getFrame(), Messages.getString("NodePanel.MB_REM_LINK_MSG"), Messages.getString("NodePanel.MB_REM_LINK_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
                                        if(ret == JOptionPane.YES_OPTION) {
                                            editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_SUCCESS_CHANGED, NodePanel.this);
                                            relSuccessPanel = null;
                                            thisData.setSucMessage(""); //$NON-NLS-1$
                                            thisData.setSucAction("nop"); //$NON-NLS-1$
                                            thisData.setSucValue(""); //$NON-NLS-1$
                                            thisData.setSucMoveTo(0);
                                            moving = false;
                                            linking = LINK_DIR.NONE;
                                            resizing = RESIZE_DIR.NONE;
                                            repaint();
                                            editor.repaint();
                                            setCursor(Cursor.getDefaultCursor());
                                        }
                                    }
                                }
                                if(startY >= getHeight()/2 + 1 && startY <= getHeight()/2 + LINK_HANDLE_SIZE + 1) {
                                    if(relErrorPanel == null && "nop".equals(thisData.getErrAction())) { //$NON-NLS-1$
                                        moving = false;
                                        linking = LINK_DIR.ERROR;
                                        resizing = RESIZE_DIR.NONE;
                                        editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_ERROR_CHANGED, NodePanel.this);
                                        editor.startNodeLinking(NodePanel.this);
                                    } else {
                                        int ret = Helper.showConfirmDialog(MainWindow.getFrame(), Messages.getString("NodePanel.MB_REM_LINK_MSG"), Messages.getString("NodePanel.MB_REM_LINK_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
                                        if(ret == JOptionPane.YES_OPTION) {
                                            editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_ERROR_CHANGED, NodePanel.this);
                                            relErrorPanel = null;
                                            thisData.setErrMessage(""); //$NON-NLS-1$
                                            thisData.setErrAction("nop"); //$NON-NLS-1$
                                            thisData.setErrValue(""); //$NON-NLS-1$
                                            thisData.setErrMoveTo(0);
                                            moving = false;
                                            linking = LINK_DIR.NONE;
                                            resizing = RESIZE_DIR.NONE;
                                            repaint();
                                            editor.repaint();
                                            setCursor(Cursor.getDefaultCursor());
                                        }
                                    }
                                }
                            }
                        }
                        if(startX <= RESIZE_HANDLE_SIZE) {
                            if(startY <= RESIZE_HANDLE_SIZE) {
                                resizing = RESIZE_DIR.TOP_LEFT;
                                setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                            } else if(startY >= getHeight() - RESIZE_HANDLE_SIZE) {
                                resizing = RESIZE_DIR.BOTTOM_LEFT;
                                setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                            }
                        } else if(startX >= getWidth() - RESIZE_HANDLE_SIZE) {
                            if(startY <= RESIZE_HANDLE_SIZE) {
                                resizing = RESIZE_DIR.TOP_RIGHT;
                                setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                            } else if(startY >= getHeight() - RESIZE_HANDLE_SIZE) {
                                resizing = RESIZE_DIR.BOTTOM_RIGHT;
                                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                            }
                        }
                        if(resizing != RESIZE_DIR.NONE) {
                        	editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_ROUTE_RESIZED, NodePanel.this);
                            startW = getWidth();
                            startH = getHeight();
                        }
                    }
                    if(resizing == RESIZE_DIR.NONE && linking == LINK_DIR.NONE) {
                        moving = true;
                        editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_ROUTE_MOVED, NodePanel.this);
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                    requestFocus();
                } else if(e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    moving = false;
                    resizing = RESIZE_DIR.NONE;
                    if(linking != LINK_DIR.NONE) {
                        RiaEditorPanel editor = (RiaEditorPanel)getParent();
                        Point pos = editor.getMousePosition();
                        NodePanel rel = editor.getNodeAt(pos.x, pos.y);
                        if(rel != null && !NodePanel.this.equals(rel)) {
                            if(linking == LINK_DIR.SUCCESS) {
                            	editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_SUCCESS_CHANGED, NodePanel.this);
                                relSuccessPanel = rel;
                                thisData.setSucMoveTo(rel.getData().getId());
                            } else if(linking == LINK_DIR.ERROR) {
                            	editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_ERROR_CHANGED, NodePanel.this);
                                relErrorPanel = rel;
                                thisData.setErrMoveTo(rel.getData().getId());
                            }
                        } else Toolkit.getDefaultToolkit().beep();
                        editor.endNodeLinking();
                        linking = LINK_DIR.NONE;
                        editor.setModified(true);
                        editor.repaint();
                    }
                    setCursor(Cursor.getDefaultCursor());
                } else if(e.isPopupTrigger()) {
                    NodePanel.this.requestFocus();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        /*
         *
         */
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                boolean done = false;
                final int dx = e.getX() - startX;
                final int dy = e.getY() - startY;
                if(moving) {
                    setLocation(getX() + dx, getY() + dy);
                    thisData.setBounds(getX(), getY(), getWidth(), getHeight());
                    editor.setModified(true);
                    done = true;
                } else if(resizing != RESIZE_DIR.NONE) {
                    final int x = e.getX();
                    final int y = e.getY();
                    final int dw = Math.max(25, startW + e.getX() - startX);
                    final int dh = Math.max(25, startH + e.getY() - startY);
                    switch(resizing) {
                    case TOP_LEFT:
                        setLocation(getX()+x, getY()+y);
                        setSize(dw-x, dh-y);
                        startW = dw-x;
                        startH = dh-y;
                        startX = x;
                        startY = y;
                        break;
                    case TOP_RIGHT:
                        setLocation(getX(), getY()+y);
                        setSize(dw, dh);
                        startH = dh-y;
                        startY = y;
                        break;
                    case BOTTOM_LEFT:
                        setLocation(getX()+x, getY());
                        setSize(dw, dh);
                        startW = dw-x;
                        startX = x;
                        break;
                    case BOTTOM_RIGHT:
                        setSize(dw, dw);
                        break;
                    default:
                        break;
                    }
                    thisData.setBounds(getX(), getY(), getWidth(), getHeight());
                    editor.setModified(true);
                    done = true;
                } else if(linking != LINK_DIR.NONE) {
                    done = true;
                }
                if(done) {
                    RiaEditorPanel editor = (RiaEditorPanel)getParent();
                    editor.updateSize();
                    editor.repaint();
                }
            }
        });

        Helper.enableRtlWhenNeeded(this);
    }

    /**
     * Creates the popup menu.
     *
     * @return the popup menu
     */
    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mnuEdit = new JMenuItem(Messages.getString("NodePanel.MNU_EDIT")); //$NON-NLS-1$
		mnuEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        mnuEdit.addActionListener(e -> {
            edit();
        });
        popupMenu.add(mnuEdit);
        popupMenu.addSeparator();
        JMenuItem mnuCopy = new JMenuItem(Messages.getString("NodePanel.MNU_COPY")); //$NON-NLS-1$
        mnuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        mnuCopy.addActionListener(e -> {
            copy();
        });
        popupMenu.add(mnuCopy);
        JMenuItem mnuCut = new JMenuItem(Messages.getString("NodePanel.MNU_CUT")); //$NON-NLS-1$
        mnuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        mnuCut.addActionListener(e -> {
            cut();
        });
        popupMenu.add(mnuCut);
        popupMenu.addSeparator();
        JMenuItem mnuDelete = new JMenuItem(Messages.getString("NodePanel.MNU_DELETE")); //$NON-NLS-1$
        mnuDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        mnuDelete.addActionListener(e -> {
            delete();
        });
        popupMenu.add(mnuDelete);
        popupMenu.addSeparator();

        JMenuItem mnuSetEid = new JMenuItem(Messages.getString("NodePanel.MNU_SET_DEF_ENTRY")); //$NON-NLS-1$
        mnuSetEid.addActionListener(e -> {
            editor.setEntryId(NodePanel.this.getData().getId());
        });
        popupMenu.add(mnuSetEid);

        return popupMenu;
    }

    /**
     * Edits current node via the NodeSettingsDialog.
     */
	public void edit() {
		List<Integer> ids = editor.getNodesIds();
		NodeSettingsDialog dialog = new NodeSettingsDialog(MainWindow.getFrame(), thisData, ids);
		if(MainWindow.getFrame().isAlwaysOnTop()) dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		if(dialog.isModified()) {
			editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_EDIT_DIALOG, NodePanel.this);
		    thisData = dialog.getData();
		    infoLabel.setText(String.format("[ID:%d] %s", thisData.getId(), thisData.getMessage().replaceAll("<[^>]+>",""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    relSuccessPanel = editor.getNodeById(thisData.getSucMoveTo());
		    relErrorPanel = editor.getNodeById(thisData.getErrMoveTo());
		    editor.setModified(true);
		    getParent().repaint();
		}
	}

    /**
     * Delete selected node.
     */
	public void delete() {
		if(JOptionPane.YES_OPTION == Helper.showConfirmDialog(MainWindow.getFrame(), Messages.getString("NodePanel.MB_REM_NODE_MSG"), Messages.getString("NodePanel.MB_REM_NODE_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1)) { //$NON-NLS-1$ //$NON-NLS-2$
			editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_ROUTE_DELETED, NodePanel.this);
			RiaEditorPanel editor = (RiaEditorPanel)getParent();
		    editor.removeNode(NodePanel.this);
		    editor.setModified(true);
		}
	}

    /**
     * Copy current node into clipboard
     */
	public void copy() {
		String xml = getData().toXml();
		Helper.copyToClipboard(xml);
	}

	/**
	 * Cut current node into clipboard.
	 */
	public void cut() {
		String xml = getData().toXml();
		Helper.copyToClipboard(xml);
		RiaEditorPanel editor = (RiaEditorPanel)getParent();
		if(editor != null) {
			editor.removeNode(NodePanel.this);
			editor.setModified(true);
			editor.addNodeState(RiaEditorPanel.NodeState.OPERATION.OP_ROUTE_CUTTED, NodePanel.this);
		} else Helper.logError(Messages.getString("NodePanel.LOG_ERR_NODE_ISNULL")); //$NON-NLS-1$
	}

    /**
     * Creates the label that show the node data.
     *
     * @return the text area
     */
    private JTextArea createLabel() {
        JTextArea textArea = new JTextArea(String.format(Messages.getString("NodePanel.NODE_DEF_CONTENT"), id)); //$NON-NLS-1$
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFocusable(false);
        textArea.setBackground(UIManager.getColor("Label.background")); //$NON-NLS-1$
        textArea.setFont(new JLabel().getFont());
        textArea.setBorder(null);
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)  { dispatchEventToParent(e); }
            @Override
            public void mousePressed(MouseEvent e)  { dispatchEventToParent(e); }
            @Override
            public void mouseReleased(MouseEvent e) { dispatchEventToParent(e); }
            @Override
            public void mouseEntered(MouseEvent e)  { dispatchEventToParent(e); }
            @Override
            public void mouseExited(MouseEvent e)   { dispatchEventToParent(e); }
            @Override
            public void mouseDragged(MouseEvent e)  { dispatchEventToParent(e); }
            @Override
            public void mouseMoved(MouseEvent e)    { dispatchEventToParent(e); }

            private void dispatchEventToParent(MouseEvent e) {
                NodePanel parent = (NodePanel)textArea.getParent();
                Point parentPoint = SwingUtilities.convertPoint(textArea, e.getPoint(), parent);
                MouseEvent parentEvent = new MouseEvent(
                    parent,
                    e.getID(),
                    e.getWhen(),
                    e.getModifiersEx(),
                    parentPoint.x,
                    parentPoint.y,
                    e.getClickCount(),
                    e.isPopupTrigger(),
                    e.getButton()
                );
                parent.dispatchEvent(parentEvent);
            }
        };
        textArea.addMouseListener(mouseListener);
        textArea.addMouseMotionListener(mouseListener);
        return textArea;
    }

    /**
     * Paint arrows and node states on parent editor.
     *
     * @param g the graphics object
     * @param rects the list of rectangles to avoid collisions
     */
    public void paintOnParent(Graphics g, List<Rectangle> rects) {
    	Color sucColor = this.hasFocus() ? SELECTED_SUCCESS_LINK_COLOR : UNSELECTED_SUCCESS_LINK_COLOR;
    	Color errColor = this.hasFocus() ? SELECTED_ERROR_LINK_COLOR : UNSELECTED_ERROR_LINK_COLOR;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(relSuccessPanel != null) {
        	assert thisData.getSucMoveTo() == relSuccessPanel.getData().getId();
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(relSuccessPanel.getX(), relSuccessPanel.getY() + relSuccessPanel.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            drawLink(g2d, sucColor, startPoint, endPoint, rects);
        }
        if(relErrorPanel != null) {
        	assert thisData.getErrMoveTo() == relErrorPanel.getData().getId();
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            Point endPoint = new Point(relErrorPanel.getX(), relErrorPanel.getY() + relErrorPanel.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            drawLink(g2d, errColor, startPoint, endPoint, rects);
        }
        if("script".equals(thisData.getValType())) { //$NON-NLS-1$
            g2d.drawImage(imgSettings, this.getX() + this.getWidth() + 25, this.getY() + this.getHeight()/2 - imgSettings.getHeight()/2/2, imgSettings.getWidth()/2,  imgSettings.getHeight()/2, null);
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, sucColor, startPoint, endPoint, rects);
            startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, errColor, startPoint, endPoint, rects);
        }

        if("switch_to_ai".equals(thisData.getSucAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
           	drawLink(g2d, sucColor, startPoint, endPoint, rects);
            g2d.drawImage(imgAI, endPoint.x - 25, endPoint.y-imgAI.getHeight()/2-5, imgAI.getWidth()/2, imgAI.getHeight()/2, null);
        }
        if("switch_to_ai".equals(thisData.getErrAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
           	drawLink(g2d, errColor, startPoint, endPoint, rects);
            g2d.drawImage(imgAI, endPoint.x - 25, endPoint.y+5, imgAI.getWidth()/2, imgAI.getHeight()/2, null);
        }
        if("switch_to_agent".equals(thisData.getSucAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
           	drawLink(g2d, sucColor, startPoint, endPoint, rects);
            g2d.drawImage(imgAgent, endPoint.x - 25, endPoint.y-imgAgent.getHeight()/2-5, imgAgent.getWidth()/2, imgAgent.getHeight()/2, null);
        }
        if("switch_to_agent".equals(thisData.getErrAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
           	drawLink(g2d, errColor, startPoint, endPoint, rects);
            g2d.drawImage(imgAgent, endPoint.x - 25, endPoint.y+5, imgAgent.getWidth()/2, imgAgent.getHeight()/2, null);
        }
        if("end".equals(thisData.getSucAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, sucColor, startPoint, endPoint, rects);
            g2d.drawImage(imgExit, endPoint.x - 25, endPoint.y-imgExit.getHeight()/2-5, imgExit.getWidth()/2, imgExit.getHeight()/2, null);
        }
        if("end".equals(thisData.getErrAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, errColor, startPoint, endPoint, rects);
            g2d.drawImage(imgExit, endPoint.x - 25, endPoint.y+5, imgExit.getWidth()/2, imgExit.getHeight()/2, null);
        }
        if("repeat".equals(thisData.getSucAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, sucColor, startPoint, endPoint, rects);
            g2d.drawImage(imgReplay, endPoint.x - 25, endPoint.y-imgReplay.getHeight()/2-5, imgReplay.getWidth()/2, imgReplay.getHeight()/2, null);
        }
        if("repeat".equals(thisData.getErrAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, errColor, startPoint, endPoint, rects);
            g2d.drawImage(imgReplay, endPoint.x - 25, endPoint.y+5, imgReplay.getWidth()/2, imgReplay.getHeight()/2, null);
        }
        if("restart".equals(thisData.getSucAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, sucColor, startPoint, endPoint, rects);
            g2d.drawImage(imgRestart, endPoint.x - 25, endPoint.y-imgRestart.getHeight()/2-5, imgRestart.getWidth()/2, imgRestart.getHeight()/2, null);
        }
        if("restart".equals(thisData.getErrAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, errColor, startPoint, endPoint, rects);
            g2d.drawImage(imgRestart, endPoint.x - 25, endPoint.y+5, imgRestart.getWidth()/2, imgRestart.getHeight()/2, null);
        }
        if("matching_list".equals(thisData.getValType())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, sucColor, startPoint, endPoint, rects);
            g2d.drawImage(imgMatchList, endPoint.x - 25, endPoint.y-imgMatchList.getHeight()/2-5, imgMatchList.getWidth()/2, imgMatchList.getHeight()/2, null);
	        Map<String, Integer> map = ChatBotClient.extractMatchingList(thisData.getValCondition());
	        if(map.size() > 0) {
	        	int v;
	        	NodePanel rip;
	        	RiaEditorPanel parent = (RiaEditorPanel)getParent();
		        for(Map.Entry<String, Integer> e : map.entrySet()) {
			       v = e.getValue();
			       if(v > 0) {
				       rip = parent.getNodeById(v);
				       if(rip != null) {
					       startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
					       endPoint = new Point(rip.getX(), rip.getY() + rip.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
					       drawLink(g2d, sucColor, startPoint, endPoint, rects);
				       }
			       }
		        }
	        }
        }
        if("user_locale:user_value".equals(thisData.getSucAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, sucColor, startPoint, endPoint, rects);
            g2d.drawImage(imgLocale, endPoint.x - 25, endPoint.y-imgLocale.getHeight()/2-5, imgLocale.getWidth()/2, imgLocale.getHeight()/2, null);
        }
		if("user_locale:user_value".equals(thisData.getErrAction())) { //$NON-NLS-1$
            Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
            Point endPoint = new Point(startPoint.x + 25, startPoint.y);
            drawLink(g2d, errColor, startPoint, endPoint, rects);
            g2d.drawImage(imgLocale, endPoint.x - 25, endPoint.y+5, imgLocale.getWidth()/2, imgLocale.getHeight()/2, null);
		}
        if(linking != LINK_DIR.NONE) {
            Point endPoint = editor.getMousePosition();
            if(linking == LINK_DIR.SUCCESS) {
                Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 - LINK_HANDLE_SIZE/2-1);
                drawLink(g2d, sucColor, startPoint, endPoint, rects);
            } else if(linking == LINK_DIR.ERROR) {
                Point startPoint = new Point(this.getX() + this.getWidth(), this.getY() + this.getHeight()/2 + LINK_HANDLE_SIZE/2+1);
                drawLink(g2d, errColor, startPoint, endPoint, rects);
            }
        }
    }

    /**
     * Draw link arrow termination on parent editor.
     *
     * @param g2d the graphics object
     * @param color the color of the arrow
     * @param startPoint the start point
     * @param endPoint the end point
     *//*
    private void drawLink(Graphics2D g2d, Color color, Point startPoint, Point endPoint) {
        // Calculate control points for the Bezier curve
        int controlOffsetX = (int)Math.round((double)Math.abs(startPoint.x - endPoint.x) * 0.5d);
        Point control1 = new Point(startPoint.x + controlOffsetX, startPoint.y);
        Point control2 = new Point(endPoint.x - controlOffsetX, endPoint.y);
        // Draw the Bezier curve
        CubicCurve2D curve = new CubicCurve2D.Float(
                startPoint.x, startPoint.y,
                control1.x, control1.y,
                control2.x, control2.y,
                endPoint.x, endPoint.y
        );
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(curve);

        // Draw arrowhead
        drawArrowHead(g2d, endPoint, control2, 10);
    }*/

    /**
     * Paint the node graphics content.
     *
     * @param g the graphics object.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.hasFocus() ? 1.0f : 0.75f));
		g2d.setColor(Color.WHITE); // TODO : Load from settings
		g2d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
		g2d.setColor(Color.LIGHT_GRAY); // TODO : Load from settings
		g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);

        g2d.setColor(Color.BLACK);
        if(hasFocus()) {
            g2d.fillRect(0, 0, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
            g2d.fillRect(getWidth() - RESIZE_HANDLE_SIZE, 0, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
            g2d.fillRect(0, getHeight() - RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
            g2d.fillRect(getWidth() - RESIZE_HANDLE_SIZE, getHeight() - RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
        }
        RiaEditorPanel parent = (RiaEditorPanel)getParent();
        if(thisData.getId() == parent.getEntryId()) {
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(0, getHeight()/2 - LINK_HANDLE_SIZE/2-1, LINK_HANDLE_SIZE, LINK_HANDLE_SIZE);
        }
        g2d.setColor(isSelected() ? SELECTED_SUCCESS_LINK_COLOR : UNSELECTED_SUCCESS_LINK_COLOR);
        g2d.fillRect(getWidth()-LINK_HANDLE_SIZE, getHeight()/2 - LINK_HANDLE_SIZE-1, LINK_HANDLE_SIZE, LINK_HANDLE_SIZE);
        g2d.setColor(isSelected() ? SELECTED_ERROR_LINK_COLOR : UNSELECTED_ERROR_LINK_COLOR);
        g2d.fillRect(getWidth()-LINK_HANDLE_SIZE, getHeight()/2 + 1, LINK_HANDLE_SIZE, LINK_HANDLE_SIZE);
    }

    /**
     * Draw arrow head.
     *
     * @param g2d the graphics object.
     * @param tip the tip
     * @param tail the tail
     * @param arrowHeadSize the arrow head size
     *//*
    private void drawArrowHead(Graphics2D g2d, Point tip, Point tail, int arrowHeadSize) {
        // Ensure there is a minimum distance for stable angle calculation
        double dx = tip.x - tail.x;
        double dy = tip.y - tail.y;
        double lineLength = Math.max(1, Math.sqrt(dx * dx + dy * dy)); // Avoid division by zero

        // Normalize the arrow direction
        double unitDx = dx / lineLength;
        double unitDy = dy / lineLength;

        // Calculate the base angle of the arrow line
        double angle = Math.atan2(unitDy, unitDx);

        // Define the angles for the arrowhead wings (25 degrees for a more stable look)
        double wingAngle1 = angle - Math.PI / 7.2; // approx. 25 degrees
        double wingAngle2 = angle + Math.PI / 7.2;

        // If the tip is behind the tail, reverse the angle for the wings
        if (lineLength <= 1) {
            wingAngle1 -= Math.PI;
            wingAngle2 -= Math.PI;
        }

        // Calculate the points for the arrowhead wings
        int x1 = (int) Math.round(tip.x - arrowHeadSize * Math.cos(wingAngle1));
        int y1 = (int) Math.round(tip.y - arrowHeadSize * Math.sin(wingAngle1));
        int x2 = (int) Math.round(tip.x - arrowHeadSize * Math.cos(wingAngle2));
        int y2 = (int) Math.round(tip.y - arrowHeadSize * Math.sin(wingAngle2));

        // Create the arrowhead as a filled polygon
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(tip.x, tip.y); // Tip of the arrow
        arrowHead.addPoint(x1, y1);       // One wing point
        arrowHead.addPoint(x2, y2);       // Other wing point

        // Fill the arrowhead
        g2d.fill(arrowHead);
    }*/

    /**
     * Sets the related success panel.
     *
     * @param rip the related node panel.
     */
    public void setRelSuccessPanel(NodePanel rip) {
        relSuccessPanel = rip;
        thisData.setSucMoveTo(rip==null ? 0 : rip.getData().getId());
        editor.setModified(true);
    }

    /**
     * Sets the related error node panel.
     *
     * @param rip the new related error node panel
     */
    public void setRelErrorPanel(NodePanel rip) {
        relErrorPanel = rip;
        thisData.setErrMoveTo(rip==null ? 0 : rip.getData().getId());
        editor.setModified(true);
    }

    /**
     * Gets the related success node panel.
     *
     * @return the related success node panel
     */
    public NodePanel getRelSuccessPanel() { return relSuccessPanel; }

    /**
     * Gets the related error node panel.
     *
     * @return the related error node panel
     */
    public NodePanel getRelErrorPanel() { return relErrorPanel; }

    /**
     * Gets the data of current node.
     *
     * @return the data of current node
     */
    public Data getData() { return thisData; }

    /**
     * Sets the data, overwrite current node data.
     *
     * @param data the new data
     */
    public void setData(Data data) {
        Objects.requireNonNull(data);
        thisData = data;
        infoLabel.setText(String.format("[ID:%d] %s", data.id, data.message.replaceAll("<[^>]+>",""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.setBounds(data.xPos, data.yPos, data.width, data.height);
        editor.setModified(true);
        repaint();
        revalidate();
    }

    /**
     * Sets the data, overwrite current node data and set the id.
     * To use when pasting a node from clipboard.
     * @param data The new data
     * @param id The new id
     */
    public void setData(Data data, int id) {
    	if(id < 0)
    		throw new IllegalArgumentException();
    	setData(data);
    	thisData.id = id;
    }

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     */
    public boolean isSelected() { return selected; }

    ///////////////////////////////////////////////////////////////////////////
    // Connections drawing methodes
    ///////////////////////////////////////////////////////////////////////////
    private void drawLink(Graphics2D g2d, Color color, Point startPoint, Point endPoint, List<Rectangle> rectangles) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));

        // If there are no obstacles or line doesn't cross any rectangles, draw straight diagonal line
        if (rectangles.isEmpty() || !doesLineCrossAnyRectangle(startPoint, endPoint, rectangles)) {
            drawArrowLine(g2d, startPoint, endPoint);
            return;
        }

        // Find a path that avoids rectangles using diagonal routing
        List<Point> path = findDiagonalPath(startPoint, endPoint, rectangles, 3); // 3 levels max

        // Draw the polyline with arrows
        drawPolylineWithArrows(g2d, path);
    }

    private void drawArrowLine(Graphics2D g2d, Point start, Point end) {
        g2d.drawLine(start.x, start.y, end.x, end.y);
        drawArrowHead(g2d, end, start);
    }

    private void drawPolylineWithArrows(Graphics2D g2d, List<Point> path) {
        // Draw the connecting lines
        for (int i = 0; i < path.size() - 1; i++) {
            g2d.drawLine(path.get(i).x, path.get(i).y, path.get(i + 1).x, path.get(i + 1).y);
        }

        // Draw arrow at the end
        if (path.size() >= 2) {
            Point lastSegmentStart = path.get(path.size() - 2);
            Point lastSegmentEnd = path.get(path.size() - 1);
            drawArrowHead(g2d, lastSegmentEnd, lastSegmentStart);
        }
    }

    private void drawArrowHead(Graphics2D g2d, Point tip, Point tail) {
        double angle = Math.atan2(tip.y - tail.y, tip.x - tail.x);
        int arrowLength = 12;
        int arrowWidth = 5;

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(tip.x, tip.y);
        arrowHead.addPoint(
            (int) (tip.x - arrowLength * Math.cos(angle) - arrowWidth * Math.sin(angle)),
            (int) (tip.y - arrowLength * Math.sin(angle) + arrowWidth * Math.cos(angle))
        );
        arrowHead.addPoint(
            (int) (tip.x - arrowLength * Math.cos(angle) + arrowWidth * Math.sin(angle)),
            (int) (tip.y - arrowLength * Math.sin(angle) - arrowWidth * Math.cos(angle))
        );

        g2d.fill(arrowHead);
    }

    // Diagonal pathfinding - avoids horizontal/vertical lines
    private List<Point> findDiagonalPath(Point start, Point end, List<Rectangle> obstacles, int maxLevel) {
        // Try straight diagonal line first
        if (!doesLineCrossAnyRectangle(start, end, obstacles)) {
            List<Point> path = new ArrayList<>();
            path.add(start);
            path.add(end);
            return path;
        }

        // Try diagonal routing with intermediate points
        List<Point> diagonalPath = findSimpleDiagonalPath(start, end, obstacles);
        if (diagonalPath != null && !doesPathCrossAnyRectangle(diagonalPath, obstacles)) {
            return diagonalPath;
        }

        // Try multi-segment diagonal routing with increasing complexity
        return findMultiSegmentDiagonalPath(start, end, obstacles, maxLevel, 0);
    }

    private List<Point> findSimpleDiagonalPath(Point start, Point end, List<Rectangle> obstacles) {
        List<Point> path = new ArrayList<>();
        path.add(start);

        // Generate diagonal intermediate points
        List<Point> diagonalPoints = generateDiagonalPoints(start, end, obstacles, 1);

        for (Point intermediate : diagonalPoints) {
            if (!doesLineCrossAnyRectangle(start, intermediate, obstacles) &&
                !doesLineCrossAnyRectangle(intermediate, end, obstacles)) {
                path.add(intermediate);
                path.add(end);
                return path;
            }
        }

        return null;
    }

    private List<Point> findMultiSegmentDiagonalPath(Point start, Point end, List<Rectangle> obstacles, int maxLevel, int currentLevel) {
        if (currentLevel >= maxLevel) {
            // Fallback: use curved path
            return findCurvedPath(start, end, obstacles);
        }

        List<Point> bestPath = null;
        double bestScore = Double.MAX_VALUE;

        // Generate multiple candidate diagonal paths
        List<List<Point>> candidatePaths = generateDiagonalCandidates(start, end, obstacles, currentLevel + 1);

        for (List<Point> candidate : candidatePaths) {
            if (!doesPathCrossAnyRectangle(candidate, obstacles)) {
                double score = calculateDiagonalPathScore(candidate);
                if (score < bestScore) {
                    bestScore = score;
                    bestPath = candidate;
                }
            }
        }

        if (bestPath != null) {
            return bestPath;
        }

        // If no good path found, try with more segments
        return findMultiSegmentDiagonalPath(start, end, obstacles, maxLevel, currentLevel + 1);
    }

    private List<Point> generateDiagonalPoints(Point start, Point end, List<Rectangle> obstacles, int numPoints) {
        List<Point> points = new ArrayList<>();
        int padding = 20;

        // Calculate the main diagonal direction
        int dx = end.x - start.x;
        int dy = end.y - start.y;

        // Generate points along diagonal directions from obstacles
        for (Rectangle rect : obstacles) {
            if (lineIntersectsRectangle(start, end, rect)) {
                // Add diagonal points around obstacles (avoiding horizontal/vertical)
                points.add(new Point(rect.x - padding, rect.y - padding)); // NW
                points.add(new Point(rect.x + rect.width + padding, rect.y - padding)); // NE
                points.add(new Point(rect.x - padding, rect.y + rect.height + padding)); // SW
                points.add(new Point(rect.x + rect.width + padding, rect.y + rect.height + padding)); // SE

                // Diagonal midpoints
                points.add(new Point(rect.x - padding, rect.y + rect.height / 2)); // W-center (diagonal approach)
                points.add(new Point(rect.x + rect.width + padding, rect.y + rect.height / 2)); // E-center
                points.add(new Point(rect.x + rect.width / 2, rect.y - padding)); // N-center
                points.add(new Point(rect.x + rect.width / 2, rect.y + rect.height + padding)); // S-center
            }
        }

        // Add diagonal grid points
        int gridSteps = 3;
        for (int i = 1; i < gridSteps; i++) {
            double t = (double) i / gridSteps;
            int baseX = start.x + (int)(t * (end.x - start.x));
            int baseY = start.y + (int)(t * (end.y - start.y));

            // Add points in diagonal directions (no pure horizontal/vertical)
            int offset = 30;
            points.add(new Point(baseX + offset, baseY + offset)); // SE
            points.add(new Point(baseX + offset, baseY - offset)); // NE
            points.add(new Point(baseX - offset, baseY + offset)); // SW
            points.add(new Point(baseX - offset, baseY - offset)); // NW

            // Longer diagonal offsets
            points.add(new Point(baseX + offset * 2, baseY + offset));
            points.add(new Point(baseX + offset, baseY + offset * 2));
            points.add(new Point(baseX - offset * 2, baseY - offset));
            points.add(new Point(baseX - offset, baseY - offset * 2));
        }

        return points;
    }

    private List<List<Point>> generateDiagonalCandidates(Point start, Point end, List<Rectangle> obstacles, int segmentCount) {
        List<List<Point>> candidates = new ArrayList<>();

        List<Point> diagonalPoints = generateDiagonalPoints(start, end, obstacles, segmentCount);

        // Generate 2-segment diagonal paths
        for (Point intermediate : diagonalPoints) {
            List<Point> path2 = new ArrayList<>();
            path2.add(start);
            path2.add(intermediate);
            path2.add(end);
            candidates.add(path2);

            // Generate 3-segment diagonal paths
            if (segmentCount >= 2) {
                for (Point intermediate2 : diagonalPoints) {
                    if (!intermediate2.equals(intermediate)) {
                        // Check if this creates a diagonal path (no horizontal/vertical segments)
                        if (isDiagonalSegment(start, intermediate) &&
                            isDiagonalSegment(intermediate, intermediate2) &&
                            isDiagonalSegment(intermediate2, end)) {

                            List<Point> path3 = new ArrayList<>();
                            path3.add(start);
                            path3.add(intermediate);
                            path3.add(intermediate2);
                            path3.add(end);
                            candidates.add(path3);
                        }
                    }
                }
            }
        }

        // Add bezier curve approximation as candidate
        List<Point> bezierPath = generateBezierPath(start, end, obstacles);
        if (bezierPath != null) {
            candidates.add(bezierPath);
        }

        return candidates;
    }

    private boolean isDiagonalSegment(Point p1, Point p2) {
        return p1.x != p2.x && p1.y != p2.y;
    }

    private boolean isHorizontalOrVertical(Point p1, Point p2) {
        return p1.x == p2.x || p1.y == p2.y;
    }

    private double calculateDiagonalPathScore(List<Point> path) {
        double totalLength = 0;
        int diagonalSegments = 0;
        int straightSegments = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            totalLength += p1.distance(p2);

            if (isDiagonalSegment(p1, p2)) {
                diagonalSegments++;
            } else if (isHorizontalOrVertical(p1, p2)) {
                straightSegments++; // Penalize horizontal/vertical
            }
        }

        // Heavy penalty for horizontal/vertical segments
        double straightPenalty = straightSegments * 1000;

        // Penalize too many segments
        double segmentPenalty = (path.size() - 2) * 10;

        // Reward smooth diagonal paths
        double smoothnessBonus = calculateSmoothnessBonus(path);

        return totalLength + straightPenalty + segmentPenalty - smoothnessBonus;
    }

    private double calculateSmoothnessBonus(List<Point> path) {
        double bonus = 0;

        for (int i = 1; i < path.size() - 1; i++) {
            Point prev = path.get(i - 1);
            Point curr = path.get(i);
            Point next = path.get(i + 1);

            // Calculate angles between segments
            double angle1 = Math.atan2(curr.y - prev.y, curr.x - prev.x);
            double angle2 = Math.atan2(next.y - curr.y, next.x - curr.x);
            double angleDiff = Math.abs(angle1 - angle2);

            // Normalize angle difference
            while (angleDiff > Math.PI) {
                angleDiff = 2 * Math.PI - angleDiff;
            }

            // Reward smooth turns (angles close to 135 or 45 degrees)
            if (angleDiff > Math.toRadians(30) && angleDiff < Math.toRadians(150)) {
                bonus += 20;
            }
        }

        return bonus;
    }

    private List<Point> generateBezierPath(Point start, Point end, List<Rectangle> obstacles) {
        // Simple bezier curve approximation with control points
        List<Point> path = new ArrayList<>();
        path.add(start);

        // Calculate control points that avoid obstacles
        int dx = end.x - start.x;
        int dy = end.y - start.y;

        // Control points at 1/3 and 2/3 of the distance, offset diagonally
        Point control1 = new Point(
            start.x + dx / 3 + dy / 6,
            start.y + dy / 3 - dx / 6
        );

        Point control2 = new Point(
            start.x + 2 * dx / 3 - dy / 6,
            start.y + 2 * dy / 3 + dx / 6
        );

        // Sample bezier curve at multiple points
        int samples = 8;
        for (int i = 1; i < samples; i++) {
            double t = (double) i / samples;
            Point point = calculateBezierPoint(start, control1, control2, end, t);
            path.add(point);
        }

        path.add(end);

        // Check if this bezier path avoids obstacles
        if (!doesPathCrossAnyRectangle(path, obstacles)) {
            return path;
        }

        return null;
    }

    private Point calculateBezierPoint(Point p0, Point p1, Point p2, Point p3, double t) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        double x = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x;
        double y = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y;

        return new Point((int) x, (int) y);
    }

    private List<Point> findCurvedPath(Point start, Point end, List<Rectangle> obstacles) {
        // Final fallback - use a curved path around the center
        List<Point> path = new ArrayList<>();
        path.add(start);

        Point center = new Point((start.x + end.x) / 2, (start.y + end.y) / 2);

        // Try different curved paths
        List<Point> curveVariations = new ArrayList<>();

        // Arc above
        curveVariations.add(new Point(center.x - Math.abs(end.y - start.y) / 3, center.y - Math.abs(end.x - start.x) / 3));
        // Arc below
        curveVariations.add(new Point(center.x + Math.abs(end.y - start.y) / 3, center.y + Math.abs(end.x - start.x) / 3));
        // S-curve
        curveVariations.add(new Point(start.x + (end.x - start.x) / 3, start.y + (end.y - start.y) * 2 / 3));
        curveVariations.add(new Point(start.x + (end.x - start.x) * 2 / 3, start.y + (end.y - start.y) / 3));

        for (Point curvePoint : curveVariations) {
            if (!doesLineCrossAnyRectangle(start, curvePoint, obstacles) &&
                !doesLineCrossAnyRectangle(curvePoint, end, obstacles)) {
                path.add(curvePoint);
                path.add(end);
                return path;
            }
        }

        // Ultimate fallback - direct line (will cross obstacles)
        path.clear();
        path.add(start);
        path.add(end);
        return path;
    }

    private boolean doesPathCrossAnyRectangle(List<Point> path, List<Rectangle> obstacles) {
        for (int i = 0; i < path.size() - 1; i++) {
            if (doesLineCrossAnyRectangle(path.get(i), path.get(i + 1), obstacles)) {
                return true;
            }
        }
        return false;
    }

    // Existing collision detection methods (unchanged)
    private boolean doesLineCrossAnyRectangle(Point p1, Point p2, List<Rectangle> rectangles) {
        for (Rectangle rect : rectangles) {
            if (lineIntersectsRectangle(p1, p2, rect)) {
                return true;
            }
        }
        return false;
    }

    private boolean lineIntersectsRectangle(Point p1, Point p2, Rectangle rect) {
        return lineIntersectsLine(p1, p2,
                new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y)) ||
               lineIntersectsLine(p1, p2,
                new Point(rect.x + rect.width, rect.y), new Point(rect.x + rect.width, rect.y + rect.height)) ||
               lineIntersectsLine(p1, p2,
                new Point(rect.x, rect.y + rect.height), new Point(rect.x + rect.width, rect.y + rect.height)) ||
               lineIntersectsLine(p1, p2,
                new Point(rect.x, rect.y), new Point(rect.x, rect.y + rect.height));
    }

    private boolean lineIntersectsLine(Point p1, Point p2, Point p3, Point p4) {
        int d1 = direction(p3, p4, p1);
        int d2 = direction(p3, p4, p2);
        int d3 = direction(p1, p2, p3);
        int d4 = direction(p1, p2, p4);

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        return false;
    }

    private int direction(Point p1, Point p2, Point p3) {
        return (p3.x - p1.x) * (p2.y - p1.y) - (p3.y - p1.y) * (p2.x - p1.x);
    }
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * The Class Data for a node that is loaded from RIA files.
     */
    public static class Data {

        /** The id. */
        private int    id;

        /** The message. */
        private String message;

        /** The value type. */
        private String valType;

        /** The value condition. */
        private String valCondition;

        /** The success message. */
        private String sucMessage;

        /** The success action. */
        private String sucAction;

        /** The success value. */
        private String sucValue;

        /** The success move to. */
        private int    sucMoveTo;

        /** The error message. */
        private String errMessage;

        /** The error action. */
        private String errAction;

        /** The error value. */
        private String errValue;

        /** The error move to. */
        private int    errMoveTo;

        /** The script. */
        private String script;

        /** The x pos. */
        private int xPos;

        /** The y pos. */
        private int yPos;

        /** The width. */
        private int width;

        /** The height. */
        private int height;

        /**
         * Instantiates a new data.
         */
        private Data() { }

        /**
         * Instantiates a new Data instance from an old one.
         *
         * @param data the data to use to create the new instance.
         */
        public Data(Data data) {
            this.id = data.id;
            this.message = data.message;
            this.valType = data.valType;
            this.valCondition = data.valCondition;
            this.sucMessage = data.sucMessage;
            this.sucAction = data.sucAction;
            this.sucValue = data.sucValue;
            this.sucMoveTo = data.sucMoveTo;
            this.errMessage = data.errMessage;
            this.errAction = data.errAction;
            this.errValue = data.errValue;
            this.errMoveTo = data.errMoveTo;
            this.script = data.script;
            this.xPos = data.xPos;
            this.yPos = data.yPos;
            this.width = data.width;
            this.height = data.height;
        }

        /**
         * Instantiates a new Data instance with minimal data.
         *
         * @param id the id of the node
         * @param msg the message of the node.
         */
        public Data(int id, String msg) {
            if(id <= 0 || msg == null)
                throw new InvalidParameterException(Messages.getString("NodePanel.EX_INVALID_PARAMS")); //$NON-NLS-1$
            this.id = id;
            this.message = msg;
            this.valType = "nop"; //$NON-NLS-1$
            this.valCondition = ""; //$NON-NLS-1$
            this.sucMessage = ""; //$NON-NLS-1$
            this.sucAction = "nop"; //$NON-NLS-1$
            this.sucValue = ""; //$NON-NLS-1$
            this.sucMoveTo = 0;
            this.errMessage = ""; //$NON-NLS-1$
            this.errAction = "nop"; //$NON-NLS-1$
            this.errValue = ""; //$NON-NLS-1$
            this.errMoveTo = 0;
            this.script = ""; //$NON-NLS-1$

            this.xPos = 0;
            this.yPos = 0;
            this.width = 100;
            this.height = 100;
        }

        /**
         * Sets the validation part of the node
         *
         * @param type the value type
         * @param condition the value condition
         * @param script the script that will be executed if defined.
         */
        public void setValidation(String type, String condition, String script) {
            if(type == null || condition == null || script == null)
                throw new InvalidParameterException(Messages.getString("NodePanel.EX_INVALID_PARAMS")); //$NON-NLS-1$
            this.valType = type;
            this.valCondition = condition;
            this.script = script;
        }

        /**
         * Sets the action to perform on condition success.
         *
         * @param msg the success msg
         * @param action the success action
         * @param value the value
         * @param moveTo the node id to move to.
         */
        public void setSuccess(String msg, String action, String value, int moveTo) {
            if(msg == null || action == null || value == null || moveTo < 0)
                throw new InvalidParameterException(Messages.getString("NodePanel.EX_INVALID_PARAMS")); //$NON-NLS-1$
            this.sucMessage = msg;
            this.sucAction = action;
            this.sucValue = value;
            this.sucMoveTo = moveTo;
        }

        /**
         * Sets the action to perform on error.
         *
         * @param msg the error msg
         * @param action the error action
         * @param value the value
         * @param moveTo the node id to move to
         */
        public void setError(String msg, String action, String value, int moveTo) {
            if(msg == null || action == null || value == null || moveTo < 0)
                throw new InvalidParameterException(Messages.getString("NodePanel.EX_INVALID_PARAMS")); //$NON-NLS-1$
            this.errMessage = msg;
            this.errAction = action;
            this.errValue = value;
            this.errMoveTo = moveTo;
        }

        /**
         * Sets the bounds of the node in the editor.
         *
         * @param x the x position of the node.
         * @param y the y position of the node.
         * @param w the width of the node.
         * @param h the height of the node.
         */
        public void setBounds(int x, int y, int w, int h) {
            this.xPos = x;
            this.yPos = y;
            this.width = w;
            this.height = h;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public int getId() { return id; }

        /**
         * Gets the message.
         *
         * @return the message
         */
        public String getMessage() { return message; }

        /**
         * Gets the val type.
         *
         * @return the val type
         */
        public String getValType() { return valType; }

        /**
         * Gets the value condition.
         *
         * @return the value condition
         */
        public String getValCondition() { return valCondition; }

        /**
         * Gets the success message.
         *
         * @return the success message
         */
        public String getSucMessage() { return sucMessage; }

        /**
         * Gets the success action.
         *
         * @return the success action
         */
        public String getSucAction() { return sucAction; }

        /**
         * Gets the success value.
         *
         * @return the success value
         */
        public String getSucValue() { return sucValue; }

        /**
         * Gets the success move to node with id.
         *
         * @return the success move to
         */
        public int getSucMoveTo() { return sucMoveTo; }

        /**
         * Gets the error message.
         *
         * @return the error message
         */
        public String getErrMessage() { return errMessage; }

        /**
         * Gets the error action.
         *
         * @return the error action
         */
        public String getErrAction() { return errAction; }

        /**
         * Gets the error value.
         *
         * @return the error value
         */
        public String getErrValue() { return errValue; }

        /**
         * Gets the error move to.
         *
         * @return the error move to
         */
        public int getErrMoveTo() { return errMoveTo; }

        /**
         * Gets the script.
         *
         * @return the script
         */
        public String getScript() { return script; }

        /**
         * Gets the x pos.
         *
         * @return the x pos
         */
        public int getXPos() { return this.xPos; }

        /**
         * Gets the y pos.
         *
         * @return the y pos
         */
        public int getYPos() { return this.yPos; }

        /**
         * Gets the width.
         *
         * @return the width
         */
        public int getWidth() { return this.width; }

        /**
         * Gets the height.
         *
         * @return the height
         */
        public int getHeight() { return this.height; }

        /**
         * Sets the message.
         *
         * @param msg the new message
         */
        public void setMessage(String msg) { this.message = msg; }

        /**
         * Sets the value type.
         *
         * @param type the new value type
         */
        public void setValType(String type) { this.valType = type; }

        /**
         * Sets the value condition.
         *
         * @param condition the new value condition
         */
        public void setValCondition(String condition) { this.valCondition = condition; }

        /**
         * Sets the success message.
         *
         * @param msg the new success message
         */
        public void setSucMessage(String msg) { this.sucMessage = msg; }

        /**
         * Sets the success action.
         *
         * @param action the new success action
         */
        public void setSucAction(String action) { this.sucAction = action; }

        /**
         * Sets the success value.
         *
         * @param value the new success value
         */
        public void setSucValue(String value) { this.sucValue = value; }

        /**
         * Sets the success move to node id.
         *
         * @param moveTo the new success move to node id.
         */
        public void setSucMoveTo(int moveTo) { this.sucMoveTo = moveTo; }

        /**
         * Sets the error message.
         *
         * @param msg the new error message
         */
        public void setErrMessage(String msg) { this.errMessage = msg; }

        /**
         * Sets the error action.
         *
         * @param action the new error action
         */
        public void setErrAction(String action) { this.errAction = action; }

        /**
         * Sets the error value.
         *
         * @param value the new error value
         */
        public void setErrValue(String value) { this.errValue = value; }

        /**
         * Sets the error move to node id.
         *
         * @param moveTo the new error move to node id.
         */
        public void setErrMoveTo(int moveTo) { this.errMoveTo = moveTo; }

        /**
         * Sets the script to execute.
         *
         * @param script the new script
         */
        public void setScript(String script) { this.script = script; }

        /**
         * Sets the x pos.
         *
         * @param x the new x pos
         */
        public void setXPos(int x) { this.xPos = x; }

        /**
         * Sets the y pos.
         *
         * @param y the new y pos
         */
        public void setYPos(int y) { this.yPos = y; }

        /**
         * Sets the width.
         *
         * @param w the new width
         */
        public void setWidth(int w) { this.width = w; }

        /**
         * Sets the height.
         *
         * @param h the new height
         */
        public void setHeight(int h) { this.height = h; }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return "Data [id=" + id + ", message=" + message + ", sucMoveTo=" + sucMoveTo + ", errMoveTo=" + errMoveTo + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }

        /**
         * Gets an XML content that represents the node.
         *
         * @return the node XML.
         */
        public String toXml() {
            StringBuilder sb = new StringBuilder();
            sb.append("\t\t<question>\n"); //$NON-NLS-1$
            sb.append("\t\t\t<id>").append(id).append("</id>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t<bounds>").append(xPos).append(";").append(yPos).append(";").append(width).append(";").append(height).append("</bounds>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            sb.append("\t\t\t<message>").append(Helper.escapeHtml(message.trim())).append("</message>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t<response>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t<condition>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t\t<type>").append(valType.trim()).append("</type>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t<validation>").append(valCondition.trim()).append("</validation>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t<on_success>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t\t\t<message>").append(Helper.escapeHtml(sucMessage.trim())).append("</message>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t\t<action>").append(sucAction.trim()).append("</action>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t\t<value>").append(sucValue.trim()).append("</value>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t\t<move>").append(sucMoveTo).append("</move>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t</on_success>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t\t<on_error>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t\t\t<message>").append(Helper.escapeHtml(errMessage.trim())).append("</message>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t\t<action>").append(errAction.trim()).append("</action>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t\t<value>").append(errValue.trim()).append("</value>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t\t<move>").append(errMoveTo).append("</move>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t\t</on_error>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t\t<script>\n").append(Helper.escapeHtml(script.trim())).append("\t\t\t\t\t</script>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t</condition>\n"); //$NON-NLS-1$
            sb.append("\t\t\t</response>\n"); //$NON-NLS-1$
            sb.append("\t\t</question>\n"); //$NON-NLS-1$
            return sb.toString();
        }

        /**
         * Creates a new Data instance from an XML content.
         * Use only for copy/paste purposes.
         *
         * @param xmlQTag the node XML content.
         * @return A new Data instance.
         */
        public static Data fromXml(String xmlQTag) {
            // TODO : Fine tune this.
            Data data = new Data();
            data.id = Integer.parseInt(getXmlValue(xmlQTag, "id")); //$NON-NLS-1$
            String[] bounds = getXmlValue(xmlQTag, "bounds").split(";"); //$NON-NLS-1$ //$NON-NLS-2$
            data.xPos = Integer.parseInt(bounds[0]);
            data.yPos = Integer.parseInt(bounds[1]);
            data.width = Integer.parseInt(bounds[2]);
            data.height = Integer.parseInt(bounds[3]);
            data.message = Helper.unescapeHtml(getXmlValue(xmlQTag, "message")); //$NON-NLS-1$
            data.valType = getXmlValue(xmlQTag, "type"); //$NON-NLS-1$
            data.valCondition = getXmlValue(xmlQTag, "validation"); //$NON-NLS-1$
            String xmlSTag = xmlQTag.substring(xmlQTag.indexOf("<on_success>"), xmlQTag.indexOf("</on_success>")+13); //$NON-NLS-1$ //$NON-NLS-2$
            data.sucMessage = Helper.unescapeHtml(getXmlValue(xmlSTag, "message")); //$NON-NLS-1$
            data.sucAction = getXmlValue(xmlSTag, "action"); //$NON-NLS-1$
            data.sucValue = getXmlValue(xmlSTag, "value"); //$NON-NLS-1$
            data.sucMoveTo = Integer.parseInt(getXmlValue(xmlSTag, "move")); //$NON-NLS-1$
            String xmlETag = xmlQTag.substring(xmlQTag.indexOf("<on_error>"), xmlQTag.indexOf("</on_error>")+11); //$NON-NLS-1$ //$NON-NLS-2$
            data.errMessage = Helper.unescapeHtml(getXmlValue(xmlETag, "message")); //$NON-NLS-1$
            data.errAction = getXmlValue(xmlETag, "action"); //$NON-NLS-1$
            data.errValue = getXmlValue(xmlETag, "value"); //$NON-NLS-1$
            data.errMoveTo = Integer.parseInt(getXmlValue(xmlETag, "move")); //$NON-NLS-1$
            data.script = Helper.unescapeHtml(getXmlValue(xmlQTag, "script")); //$NON-NLS-1$
            return data;
        }

        /**
         * Creates a new Data instance from an XML node object.
         *
         * @param node the XML node
         * @return the new Data instance or null on error.
         */
        public static Data fromXml(Node node) {
        	try {
	        	Data data = new Data();
	        	NodeList parentNodes = node.getChildNodes();
	        	data.id = Integer.parseInt(Helper.getNodeValue(parentNodes, "id")); //$NON-NLS-1$
				String[] bounds = Helper.getNodeValue(parentNodes, "bounds").split(";"); //$NON-NLS-1$ //$NON-NLS-2$
				data.xPos = Integer.parseInt(bounds[0]);
				data.yPos = Integer.parseInt(bounds[1]);
				data.width = Integer.parseInt(bounds[2]);
				data.height = Integer.parseInt(bounds[3]);
				data.message = Helper.unescapeHtml(Helper.getNodeValue(parentNodes, "message")); //$NON-NLS-1$
				parentNodes = Helper.getNodeByName(parentNodes, "response").getChildNodes(); //$NON-NLS-1$
				parentNodes = Helper.getNodeByName(parentNodes, "condition").getChildNodes(); //$NON-NLS-1$
				data.valType = Helper.getNodeValue(parentNodes, "type"); //$NON-NLS-1$
				data.valCondition = Helper.getNodeValue(parentNodes, "validation"); //$NON-NLS-1$
				NodeList nodes = Helper.getNodeByName(parentNodes, "on_success").getChildNodes(); //$NON-NLS-1$
				data.sucMessage = Helper.unescapeHtml(Helper.getNodeValue(nodes, "message")); //$NON-NLS-1$
				data.sucAction = Helper.getNodeValue(nodes, "action"); //$NON-NLS-1$
				data.sucValue = Helper.getNodeValue(nodes, "value"); //$NON-NLS-1$
				data.sucMoveTo = Integer.parseInt(Helper.getNodeValue(nodes, "move")); //$NON-NLS-1$
				nodes = Helper.getNodeByName(parentNodes, "on_error").getChildNodes(); //$NON-NLS-1$
				data.errMessage = Helper.unescapeHtml(Helper.getNodeValue(nodes, "message")); //$NON-NLS-1$
				data.errAction = Helper.getNodeValue(nodes, "action"); //$NON-NLS-1$
				data.errValue = Helper.getNodeValue(nodes, "value"); //$NON-NLS-1$
				data.errMoveTo = Integer.parseInt(Helper.getNodeValue(nodes, "move")); //$NON-NLS-1$
				data.script = Helper.unescapeHtml(Helper.getNodeValue(parentNodes, "script")); //$NON-NLS-1$
				return data;
        	} catch (Exception ex) {
	        	Helper.logError(ex, Messages.getString("NodePanel.LOG_ERR_NODE_LOAD"), true); //$NON-NLS-1$
        	}
        	return null;
        }

        /**
         * Gets the XML value from a text XML content.
         *
         * @param xmlQTag the XML tag
         * @param tag the XML tag
         * @return the XML value of the givent tag.
         */
        private static String getXmlValue(String xmlQTag, String tag) {
            try {
                return xmlQTag.substring(xmlQTag.indexOf("<"+tag+">")+tag.length()+2, xmlQTag.indexOf("</"+tag+">")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } catch (Exception ex) {
                return ""; //$NON-NLS-1$
            }
        }

        /**
         * Fill success action combo box.
         *
         * @param comboBox the combo box
         */
        public static void fillSuccessActionComboBox(JComboBox<String> comboBox) {
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
            model.removeAllElements();
			model.addElement("nop"); //$NON-NLS-1$
            model.addElement("user_locale:user_value"); //$NON-NLS-1$
            model.addElement("variable:user_value"); //$NON-NLS-1$
            model.addElement("variable:operation"); //$NON-NLS-1$
            model.addElement("repeat"); //$NON-NLS-1$
            model.addElement("switch_to_ai"); //$NON-NLS-1$
            model.addElement("switch_to_agent"); //$NON-NLS-1$
            model.addElement("restart"); //$NON-NLS-1$
            model.addElement("end"); //$NON-NLS-1$
        }

        /**
         * Fill error action combo box.
         *
         * @param comboBox the combo box
         */
        public static void fillErrorActionComboBox(JComboBox<String> comboBox) {
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
            model.removeAllElements();
			model.addElement("nop"); //$NON-NLS-1$
            model.addElement("variable:user_value"); //$NON-NLS-1$
            model.addElement("variable:operation"); //$NON-NLS-1$
            model.addElement("repeat"); //$NON-NLS-1$
            model.addElement("switch_to_ai"); //$NON-NLS-1$
            model.addElement("switch_to_agent"); //$NON-NLS-1$
            model.addElement("restart"); //$NON-NLS-1$
            model.addElement("end"); //$NON-NLS-1$
        }

        /**
         * Fill validation combo box.
         *
         * @param comboBox the combo box
         */
        public static void fillValidationComboBox(JComboBox<String> comboBox) {
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
            model.removeAllElements();
            model.addElement("nop"); //$NON-NLS-1$
            model.addElement("text:any"); //$NON-NLS-1$
            model.addElement("text:equal"); //$NON-NLS-1$
            model.addElement("text:in_list"); //$NON-NLS-1$
            model.addElement("text:email"); //$NON-NLS-1$
            //model.addElement("text:url");		// TODO : Implement this
            //model.addElement("text:phone");		// TODO : Implement this
            model.addElement("number:any"); //$NON-NLS-1$
            model.addElement("number:equal"); //$NON-NLS-1$
            model.addElement("number:interval"); //$NON-NLS-1$
            //model.addElement("date:any");		// TODO : Implement this
            //model.addElement("date:interval");	// TODO : Implement this
            //model.addElement("time:any");		// TODO : Implement this
            //model.addElement("time:interval");	// TODO : Implement this
            model.addElement("date:dd/MM/yyyy"); //$NON-NLS-1$
            model.addElement("date:MM/dd/yyyy"); //$NON-NLS-1$
            model.addElement("boolean:any"); //$NON-NLS-1$
            model.addElement("matching_list"); //$NON-NLS-1$
            model.addElement("matching_values"); //$NON-NLS-1$
            model.addElement("script"); //$NON-NLS-1$
        }
    }

}












