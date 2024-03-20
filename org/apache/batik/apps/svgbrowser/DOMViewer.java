package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.XBLOMContentElement;
import org.apache.batik.bridge.svg12.ContentManager;
import org.apache.batik.bridge.svg12.DefaultXBLManager;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.gui.DropDownComponent;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

public class DOMViewer extends JFrame implements ActionMap {
   protected static final String RESOURCE = "org.apache.batik.apps.svgbrowser.resources.DOMViewerMessages";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.DOMViewerMessages", Locale.getDefault());
   protected static ResourceManager resources;
   protected Map listeners = new HashMap();
   protected ButtonFactory buttonFactory;
   protected Panel panel;
   protected boolean showWhitespace = true;
   protected boolean isCapturingClickEnabled;
   protected DOMViewerController domViewerController;
   protected ElementOverlayManager elementOverlayManager;
   protected boolean isElementOverlayEnabled;
   protected HistoryBrowserInterface historyBrowserInterface;
   protected boolean canEdit = true;
   protected JToggleButton overlayButton;

   public DOMViewer(DOMViewerController controller) {
      super(resources.getString("Frame.title"));
      this.setSize(resources.getInteger("Frame.width"), resources.getInteger("Frame.height"));
      this.domViewerController = controller;
      this.elementOverlayManager = this.domViewerController.createSelectionManager();
      if (this.elementOverlayManager != null) {
         this.elementOverlayManager.setController(new DOMViewerElementOverlayController());
      }

      this.historyBrowserInterface = new HistoryBrowserInterface(new HistoryBrowser.DocumentCommandController(controller));
      this.listeners.put("CloseButtonAction", new CloseButtonAction());
      this.listeners.put("UndoButtonAction", new UndoButtonAction());
      this.listeners.put("RedoButtonAction", new RedoButtonAction());
      this.listeners.put("CapturingClickButtonAction", new CapturingClickButtonAction());
      this.listeners.put("OverlayButtonAction", new OverlayButtonAction());
      this.panel = new Panel();
      this.getContentPane().add(this.panel);
      JPanel p = new JPanel(new BorderLayout());
      JCheckBox cb = new JCheckBox(resources.getString("ShowWhitespaceCheckbox.text"));
      cb.setSelected(this.showWhitespace);
      cb.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent ie) {
            DOMViewer.this.setShowWhitespace(ie.getStateChange() == 1);
         }
      });
      p.add(cb, "West");
      p.add(this.getButtonFactory().createJButton("CloseButton"), "East");
      this.getContentPane().add(p, "South");
      Document document = this.domViewerController.getDocument();
      if (document != null) {
         this.panel.setDocument(document, (ViewCSS)null);
      }

   }

   public void setShowWhitespace(boolean state) {
      this.showWhitespace = state;
      if (this.panel.document != null) {
         this.panel.setDocument(this.panel.document);
      }

   }

   public void setDocument(Document doc) {
      this.panel.setDocument(doc);
   }

   public void setDocument(Document doc, ViewCSS view) {
      this.panel.setDocument(doc, view);
   }

   public boolean canEdit() {
      return this.domViewerController.canEdit() && this.canEdit;
   }

   public void setEditable(boolean canEdit) {
      this.canEdit = canEdit;
   }

   public void selectNode(Node node) {
      this.panel.selectNode(node);
   }

   public void resetHistory() {
      this.historyBrowserInterface.getHistoryBrowser().resetHistory();
   }

   private ButtonFactory getButtonFactory() {
      if (this.buttonFactory == null) {
         this.buttonFactory = new ButtonFactory(bundle, this);
      }

      return this.buttonFactory;
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   private void addChangesToHistory() {
      this.historyBrowserInterface.performCurrentCompoundCommand();
   }

   protected void toggleOverlay() {
      this.isElementOverlayEnabled = this.overlayButton.isSelected();
      if (!this.isElementOverlayEnabled) {
         this.overlayButton.setToolTipText(resources.getString("OverlayButton.tooltip"));
      } else {
         this.overlayButton.setToolTipText(resources.getString("OverlayButton.disableText"));
      }

      if (this.elementOverlayManager != null) {
         this.elementOverlayManager.setOverlayEnabled(this.isElementOverlayEnabled);
         this.elementOverlayManager.repaint();
      }

   }

   static {
      resources = new ResourceManager(bundle);
   }

   protected static class ContentNodeInfo extends NodeInfo {
      public ContentNodeInfo(Node n) {
         super(n);
      }

      public String toString() {
         return "selected content";
      }
   }

   protected static class ShadowNodeInfo extends NodeInfo {
      public ShadowNodeInfo(Node n) {
         super(n);
      }

      public String toString() {
         return "shadow tree";
      }
   }

   protected static class NodeInfo {
      protected Node node;

      public NodeInfo(Node n) {
         this.node = n;
      }

      public Node getNode() {
         return this.node;
      }

      public String toString() {
         if (this.node instanceof Element) {
            Element e = (Element)this.node;
            String id = e.getAttribute("id");
            if (id.length() != 0) {
               return this.node.getNodeName() + " \"" + id + "\"";
            }
         }

         return this.node.getNodeName();
      }
   }

   public class Panel extends JPanel {
      public static final String NODE_INSERTED = "DOMNodeInserted";
      public static final String NODE_REMOVED = "DOMNodeRemoved";
      public static final String ATTRIBUTE_MODIFIED = "DOMAttrModified";
      public static final String CHAR_DATA_MODIFIED = "DOMCharacterDataModified";
      protected Document document;
      protected EventListener nodeInsertion;
      protected EventListener nodeRemoval;
      protected EventListener attrModification;
      protected EventListener charDataModification;
      protected EventListener capturingListener;
      protected ViewCSS viewCSS;
      protected DOMDocumentTree tree;
      protected JSplitPane splitPane;
      protected JPanel rightPanel = new JPanel(new BorderLayout());
      protected JTable propertiesTable = new JTable();
      protected NodePickerPanel attributePanel = new NodePickerPanel(DOMViewer.this.new DOMViewerNodePickerController());
      protected GridBagConstraints attributePanelLayout;
      protected GridBagConstraints propertiesTableLayout;
      protected JPanel elementPanel;
      protected CharacterPanel characterDataPanel;
      protected JTextArea documentInfo;
      protected JPanel documentInfoPanel;

      public Panel() {
         super(new BorderLayout());
         this.attributePanel.addListener(new NodePickerPanel.NodePickerAdapter() {
            public void updateElement(NodePickerPanel.NodePickerEvent event) {
               String result = event.getResult();
               Element targetElement = (Element)event.getContextNode();
               Element newElem = this.wrapAndParse(result, targetElement);
               DOMViewer.this.addChangesToHistory();
               AbstractCompoundCommand cmd = DOMViewer.this.historyBrowserInterface.createNodeChangedCommand(newElem);
               Node parent = targetElement.getParentNode();
               Node nextSibling = targetElement.getNextSibling();
               cmd.addCommand(DOMViewer.this.historyBrowserInterface.createRemoveChildCommand(parent, targetElement));
               cmd.addCommand(DOMViewer.this.historyBrowserInterface.createInsertChildCommand(parent, nextSibling, newElem));
               DOMViewer.this.historyBrowserInterface.performCompoundUpdateCommand(cmd);
               Panel.this.attributePanel.setPreviewElement(newElem);
            }

            public void addNewElement(NodePickerPanel.NodePickerEvent event) {
               String result = event.getResult();
               Element targetElement = (Element)event.getContextNode();
               Element newElem = this.wrapAndParse(result, targetElement);
               DOMViewer.this.addChangesToHistory();
               DOMViewer.this.historyBrowserInterface.appendChild(targetElement, newElem);
               Panel.this.attributePanel.setPreviewElement(newElem);
            }

            private Element wrapAndParse(String toParse, Node startingNode) {
               Map prefixMap = new HashMap();
               int j = 0;

               for(Node currentNode = startingNode; currentNode != null; currentNode = currentNode.getParentNode()) {
                  NamedNodeMap nMap = currentNode.getAttributes();

                  for(int i = 0; nMap != null && i < nMap.getLength(); ++i) {
                     Attr atr = (Attr)nMap.item(i);
                     String prefix = atr.getPrefix();
                     String localName = atr.getLocalName();
                     String namespaceURI = atr.getValue();
                     if (prefix != null && prefix.equals("xmlns")) {
                        String attrName = "xmlns:" + localName;
                        if (!prefixMap.containsKey(attrName)) {
                           prefixMap.put(attrName, namespaceURI);
                        }
                     }

                     if ((j != 0 || currentNode == Panel.this.document.getDocumentElement()) && atr.getNodeName().equals("xmlns") && !prefixMap.containsKey("xmlns")) {
                        prefixMap.put("xmlns", atr.getNodeValue());
                     }
                  }

                  ++j;
               }

               Document doc = DOMViewer.this.panel.document;
               SAXDocumentFactory df = new SAXDocumentFactory(doc.getImplementation(), XMLResourceDescriptor.getXMLParserClassName());
               URL urlObj = null;
               if (doc instanceof SVGOMDocument) {
                  urlObj = ((SVGOMDocument)doc).getURLObject();
               }

               String uri = urlObj == null ? "" : urlObj.toString();
               Node node = DOMUtilities.parseXML(toParse, doc, uri, prefixMap, "svg", df);
               return (Element)node.getFirstChild();
            }

            private void selectNewNode(final Element elem) {
               DOMViewer.this.domViewerController.performUpdate(new Runnable() {
                  public void run() {
                     Panel.this.selectNode(elem);
                  }
               });
            }
         });
         this.attributePanelLayout = new GridBagConstraints();
         this.attributePanelLayout.gridx = 1;
         this.attributePanelLayout.gridy = 1;
         this.attributePanelLayout.gridheight = 2;
         this.attributePanelLayout.weightx = 1.0;
         this.attributePanelLayout.weighty = 1.0;
         this.attributePanelLayout.fill = 1;
         this.propertiesTableLayout = new GridBagConstraints();
         this.propertiesTableLayout.gridx = 1;
         this.propertiesTableLayout.gridy = 3;
         this.propertiesTableLayout.weightx = 1.0;
         this.propertiesTableLayout.weighty = 1.0;
         this.propertiesTableLayout.fill = 1;
         this.elementPanel = new JPanel(new GridBagLayout());
         JScrollPane pane = new JScrollPane();
         pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), DOMViewer.resources.getString("CSSValuesPanel.title")), BorderFactory.createLoweredBevelBorder())));
         pane.getViewport().add(this.propertiesTable);
         this.elementPanel.add(this.attributePanel, this.attributePanelLayout);
         this.elementPanel.add(pane, this.propertiesTableLayout);
         this.characterDataPanel = new CharacterPanel(new BorderLayout());
         this.characterDataPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), DOMViewer.resources.getString("CDataPanel.title")), BorderFactory.createLoweredBevelBorder())));
         pane = new JScrollPane();
         JTextArea textArea = new JTextArea();
         this.characterDataPanel.setTextArea(textArea);
         pane.getViewport().add(textArea);
         this.characterDataPanel.add(pane);
         textArea.setEditable(true);
         textArea.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
               if (DOMViewer.this.canEdit()) {
                  Node contextNode = Panel.this.characterDataPanel.getNode();
                  String newValue = Panel.this.characterDataPanel.getTextArea().getText();
                  switch (contextNode.getNodeType()) {
                     case 3:
                     case 4:
                     case 8:
                        DOMViewer.this.addChangesToHistory();
                        DOMViewer.this.historyBrowserInterface.setNodeValue(contextNode, newValue);
                  }
               }

            }
         });
         this.documentInfo = new JTextArea();
         this.documentInfoPanel = new JPanel(new BorderLayout());
         this.documentInfoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), DOMViewer.resources.getString("DocumentInfoPanel.title")), BorderFactory.createLoweredBevelBorder())));
         pane = new JScrollPane();
         pane.getViewport().add(this.documentInfo);
         this.documentInfoPanel.add(pane);
         this.documentInfo.setEditable(false);
         this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), DOMViewer.resources.getString("DOMViewerPanel.title")));
         JToolBar tb = new JToolBar(DOMViewer.resources.getString("DOMViewerToolbar.name"));
         tb.setFloatable(false);
         JButton undoButton = DOMViewer.this.getButtonFactory().createJToolbarButton("UndoButton");
         undoButton.setDisabledIcon(new ImageIcon(this.getClass().getResource(DOMViewer.resources.getString("UndoButton.disabledIcon"))));
         DropDownComponent undoDD = new DropDownComponent(undoButton);
         undoDD.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
         undoDD.setMaximumSize(new Dimension(44, 25));
         undoDD.setPreferredSize(new Dimension(44, 25));
         tb.add(undoDD);
         DropDownHistoryModel.UndoPopUpMenuModel undoModel = new DropDownHistoryModel.UndoPopUpMenuModel(undoDD.getPopupMenu(), DOMViewer.this.historyBrowserInterface);
         undoDD.getPopupMenu().setModel(undoModel);
         JButton redoButton = DOMViewer.this.getButtonFactory().createJToolbarButton("RedoButton");
         redoButton.setDisabledIcon(new ImageIcon(this.getClass().getResource(DOMViewer.resources.getString("RedoButton.disabledIcon"))));
         DropDownComponent redoDD = new DropDownComponent(redoButton);
         redoDD.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
         redoDD.setMaximumSize(new Dimension(44, 25));
         redoDD.setPreferredSize(new Dimension(44, 25));
         tb.add(redoDD);
         DropDownHistoryModel.RedoPopUpMenuModel redoModel = new DropDownHistoryModel.RedoPopUpMenuModel(redoDD.getPopupMenu(), DOMViewer.this.historyBrowserInterface);
         redoDD.getPopupMenu().setModel(redoModel);
         JToggleButton capturingClickButton = DOMViewer.this.getButtonFactory().createJToolbarToggleButton("CapturingClickButton");
         capturingClickButton.setEnabled(true);
         capturingClickButton.setPreferredSize(new Dimension(32, 25));
         tb.add(capturingClickButton);
         DOMViewer.this.overlayButton = DOMViewer.this.getButtonFactory().createJToolbarToggleButton("OverlayButton");
         DOMViewer.this.overlayButton.setEnabled(true);
         DOMViewer.this.overlayButton.setPreferredSize(new Dimension(32, 25));
         tb.add(DOMViewer.this.overlayButton);
         this.add(tb, "North");
         TreeNode root = new DefaultMutableTreeNode(DOMViewer.resources.getString("EmptyDocument.text"));
         this.tree = new DOMDocumentTree(root, DOMViewer.this.new DOMViewerDOMDocumentTreeController());
         this.tree.setCellRenderer(new NodeRenderer());
         this.tree.putClientProperty("JTree.lineStyle", "Angled");
         this.tree.addListener(new DOMDocumentTree.DOMDocumentTreeAdapter() {
            public void dropCompleted(DOMDocumentTree.DOMDocumentTreeEvent event) {
               DOMDocumentTree.DropCompletedInfo info = (DOMDocumentTree.DropCompletedInfo)event.getSource();
               DOMViewer.this.addChangesToHistory();
               AbstractCompoundCommand cmd = DOMViewer.this.historyBrowserInterface.createNodesDroppedCommand(info.getChildren());
               int n = info.getChildren().size();

               for(int i = 0; i < n; ++i) {
                  Node node = (Node)info.getChildren().get(i);
                  if (!DOMUtilities.isAnyNodeAncestorOf(info.getChildren(), node)) {
                     cmd.addCommand(DOMViewer.this.historyBrowserInterface.createInsertChildCommand(info.getParent(), info.getSibling(), node));
                  }
               }

               DOMViewer.this.historyBrowserInterface.performCompoundUpdateCommand(cmd);
            }
         });
         this.tree.addTreeSelectionListener(new DOMTreeSelectionListener());
         this.tree.addMouseListener(new TreePopUpListener());
         JScrollPane treePane = new JScrollPane();
         treePane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), DOMViewer.resources.getString("DOMViewer.title")), BorderFactory.createLoweredBevelBorder())));
         treePane.getViewport().add(this.tree);
         this.splitPane = new JSplitPane(1, true, treePane, this.rightPanel);
         int loc = DOMViewer.resources.getInteger("SplitPane.dividerLocation");
         this.splitPane.setDividerLocation(loc);
         this.add(this.splitPane);
      }

      public void setDocument(Document doc) {
         this.setDocument(doc, (ViewCSS)null);
      }

      public void setDocument(Document doc, ViewCSS view) {
         if (this.document != null) {
            if (this.document != doc) {
               this.removeDomMutationListeners(this.document);
               this.addDomMutationListeners(doc);
               this.removeCapturingListener(this.document);
               this.addCapturingListener(doc);
            }
         } else {
            this.addDomMutationListeners(doc);
            this.addCapturingListener(doc);
         }

         DOMViewer.this.resetHistory();
         this.document = doc;
         this.viewCSS = view;
         TreeNode root = this.createTree(doc, DOMViewer.this.showWhitespace);
         ((DefaultTreeModel)this.tree.getModel()).setRoot(root);
         if (this.rightPanel.getComponentCount() != 0) {
            this.rightPanel.remove(0);
            this.splitPane.revalidate();
            this.splitPane.repaint();
         }

      }

      protected void addDomMutationListeners(Document doc) {
         EventTarget target = (EventTarget)doc;
         this.nodeInsertion = new NodeInsertionHandler();
         target.addEventListener("DOMNodeInserted", this.nodeInsertion, true);
         this.nodeRemoval = new NodeRemovalHandler();
         target.addEventListener("DOMNodeRemoved", this.nodeRemoval, true);
         this.attrModification = new AttributeModificationHandler();
         target.addEventListener("DOMAttrModified", this.attrModification, true);
         this.charDataModification = new CharDataModificationHandler();
         target.addEventListener("DOMCharacterDataModified", this.charDataModification, true);
      }

      protected void removeDomMutationListeners(Document doc) {
         if (doc != null) {
            EventTarget target = (EventTarget)doc;
            target.removeEventListener("DOMNodeInserted", this.nodeInsertion, true);
            target.removeEventListener("DOMNodeRemoved", this.nodeRemoval, true);
            target.removeEventListener("DOMAttrModified", this.attrModification, true);
            target.removeEventListener("DOMCharacterDataModified", this.charDataModification, true);
         }

      }

      protected void addCapturingListener(Document doc) {
         EventTarget target = (EventTarget)doc.getDocumentElement();
         this.capturingListener = new CapturingClickHandler();
         target.addEventListener("click", this.capturingListener, true);
      }

      protected void removeCapturingListener(Document doc) {
         if (doc != null) {
            EventTarget target = (EventTarget)doc.getDocumentElement();
            target.removeEventListener("click", this.capturingListener, true);
         }

      }

      protected void refreshGUI(Runnable runnable) {
         if (DOMViewer.this.canEdit()) {
            try {
               SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException var3) {
               var3.printStackTrace();
            } catch (InvocationTargetException var4) {
               var4.printStackTrace();
            }
         }

      }

      protected void registerNodeInserted(MutationEvent mevt) {
         Node targetNode = (Node)mevt.getTarget();
         DOMViewer.this.historyBrowserInterface.addToCurrentCompoundCommand(DOMViewer.this.historyBrowserInterface.createNodeInsertedCommand(targetNode.getParentNode(), targetNode.getNextSibling(), targetNode));
      }

      protected void registerNodeRemoved(MutationEvent mevt) {
         Node targetNode = (Node)mevt.getTarget();
         DOMViewer.this.historyBrowserInterface.addToCurrentCompoundCommand(DOMViewer.this.historyBrowserInterface.createNodeRemovedCommand(mevt.getRelatedNode(), targetNode.getNextSibling(), targetNode));
      }

      protected void registerAttributeAdded(MutationEvent mevt) {
         Element targetElement = (Element)mevt.getTarget();
         DOMViewer.this.historyBrowserInterface.addToCurrentCompoundCommand(DOMViewer.this.historyBrowserInterface.createAttributeAddedCommand(targetElement, mevt.getAttrName(), mevt.getNewValue(), (String)null));
      }

      protected void registerAttributeRemoved(MutationEvent mevt) {
         Element targetElement = (Element)mevt.getTarget();
         DOMViewer.this.historyBrowserInterface.addToCurrentCompoundCommand(DOMViewer.this.historyBrowserInterface.createAttributeRemovedCommand(targetElement, mevt.getAttrName(), mevt.getPrevValue(), (String)null));
      }

      protected void registerAttributeModified(MutationEvent mevt) {
         Element targetElement = (Element)mevt.getTarget();
         DOMViewer.this.historyBrowserInterface.addToCurrentCompoundCommand(DOMViewer.this.historyBrowserInterface.createAttributeModifiedCommand(targetElement, mevt.getAttrName(), mevt.getPrevValue(), mevt.getNewValue(), (String)null));
      }

      protected void registerAttributeChanged(MutationEvent mevt) {
         switch (mevt.getAttrChange()) {
            case 1:
               this.registerAttributeModified(mevt);
               break;
            case 2:
               this.registerAttributeAdded(mevt);
               break;
            case 3:
               this.registerAttributeRemoved(mevt);
               break;
            default:
               this.registerAttributeModified(mevt);
         }

      }

      protected void registerCharDataModified(MutationEvent mevt) {
         Node targetNode = (Node)mevt.getTarget();
         DOMViewer.this.historyBrowserInterface.addToCurrentCompoundCommand(DOMViewer.this.historyBrowserInterface.createCharDataModifiedCommand(targetNode, mevt.getPrevValue(), mevt.getNewValue()));
      }

      protected boolean shouldRegisterDocumentChange() {
         return DOMViewer.this.canEdit() && DOMViewer.this.historyBrowserInterface.getHistoryBrowser().getState() == 4;
      }

      protected void registerDocumentChange(MutationEvent mevt) {
         if (this.shouldRegisterDocumentChange()) {
            String type = mevt.getType();
            if (type.equals("DOMNodeInserted")) {
               this.registerNodeInserted(mevt);
            } else if (type.equals("DOMNodeRemoved")) {
               this.registerNodeRemoved(mevt);
            } else if (type.equals("DOMAttrModified")) {
               this.registerAttributeChanged(mevt);
            } else if (type.equals("DOMCharacterDataModified")) {
               this.registerCharDataModified(mevt);
            }
         }

      }

      protected MutableTreeNode createTree(Node node, boolean showWhitespace) {
         DefaultMutableTreeNode result = new DefaultMutableTreeNode(new NodeInfo(node));

         for(Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!showWhitespace && n instanceof Text) {
               String txt = n.getNodeValue();
               if (txt.trim().length() == 0) {
                  continue;
               }
            }

            result.add(this.createTree(n, showWhitespace));
         }

         if (node instanceof NodeXBL) {
            Element shadowTree = ((NodeXBL)node).getXblShadowTree();
            if (shadowTree != null) {
               DefaultMutableTreeNode shadowNode = new DefaultMutableTreeNode(new ShadowNodeInfo(shadowTree));
               shadowNode.add(this.createTree(shadowTree, showWhitespace));
               result.add(shadowNode);
            }
         }

         if (node instanceof XBLOMContentElement) {
            AbstractDocument doc = (AbstractDocument)node.getOwnerDocument();
            XBLManager xm = doc.getXBLManager();
            if (xm instanceof DefaultXBLManager) {
               DefaultMutableTreeNode selectedContentNode = new DefaultMutableTreeNode(new ContentNodeInfo(node));
               DefaultXBLManager dxm = (DefaultXBLManager)xm;
               ContentManager cm = dxm.getContentManager(node);
               if (cm != null) {
                  NodeList nl = cm.getSelectedContent((XBLOMContentElement)node);

                  for(int i = 0; i < nl.getLength(); ++i) {
                     selectedContentNode.add(this.createTree(nl.item(i), showWhitespace));
                  }

                  result.add(selectedContentNode);
               }
            }
         }

         return result;
      }

      protected DefaultMutableTreeNode findNode(JTree theTree, Node node) {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode)theTree.getModel().getRoot();
         Enumeration treeNodes = root.breadthFirstEnumeration();

         DefaultMutableTreeNode currentNode;
         NodeInfo userObject;
         do {
            if (!treeNodes.hasMoreElements()) {
               return null;
            }

            currentNode = (DefaultMutableTreeNode)treeNodes.nextElement();
            userObject = (NodeInfo)currentNode.getUserObject();
         } while(userObject.getNode() != node);

         return currentNode;
      }

      public void selectNode(final Node targetNode) {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               DefaultMutableTreeNode node = Panel.this.findNode(Panel.this.tree, targetNode);
               if (node != null) {
                  TreeNode[] path = node.getPath();
                  TreePath tp = new TreePath(path);
                  Panel.this.tree.setSelectionPath(tp);
                  Panel.this.tree.scrollPathToVisible(tp);
               }

            }
         });
      }

      protected JMenu createTemplatesMenu(String name) {
         NodeTemplates templates = new NodeTemplates();
         JMenu submenu = new JMenu(name);
         HashMap menuMap = new HashMap();
         ArrayList categoriesList = templates.getCategories();
         int n = categoriesList.size();
         Iterator var7 = categoriesList.iterator();

         while(var7.hasNext()) {
            Object aCategoriesList = var7.next();
            String category = aCategoriesList.toString();
            JMenu currentMenu = new JMenu(category);
            submenu.add(currentMenu);
            menuMap.put(category, currentMenu);
         }

         ArrayList values = new ArrayList(templates.getNodeTemplatesMap().values());
         Collections.sort(values, new Comparator() {
            public int compare(Object o1, Object o2) {
               NodeTemplates.NodeTemplateDescriptor n1 = (NodeTemplates.NodeTemplateDescriptor)o1;
               NodeTemplates.NodeTemplateDescriptor n2 = (NodeTemplates.NodeTemplateDescriptor)o2;
               return n1.getName().compareTo(n2.getName());
            }
         });
         Iterator var18 = values.iterator();

         while(var18.hasNext()) {
            Object value = var18.next();
            NodeTemplates.NodeTemplateDescriptor desc = (NodeTemplates.NodeTemplateDescriptor)value;
            String toParse = desc.getXmlValue();
            short nodeType = desc.getType();
            String nodeCategory = desc.getCategory();
            JMenuItem currentItem = new JMenuItem(desc.getName());
            currentItem.addActionListener(new NodeTemplateParser(toParse, nodeType));
            JMenu currentSubmenu = (JMenu)menuMap.get(nodeCategory);
            currentSubmenu.add(currentItem);
         }

         return submenu;
      }

      protected class NodeCSSValuesModel extends AbstractTableModel {
         protected Node node;
         protected CSSStyleDeclaration style;
         protected List propertyNames;

         public NodeCSSValuesModel(Node n) {
            this.node = n;
            if (Panel.this.viewCSS != null) {
               this.style = Panel.this.viewCSS.getComputedStyle((Element)n, (String)null);
               this.propertyNames = new ArrayList();
               if (this.style != null) {
                  for(int i = 0; i < this.style.getLength(); ++i) {
                     this.propertyNames.add(this.style.item(i));
                  }

                  Collections.sort(this.propertyNames);
               }
            }

         }

         public String getColumnName(int col) {
            return col == 0 ? DOMViewer.resources.getString("CSSValuesTable.column1") : DOMViewer.resources.getString("CSSValuesTable.column2");
         }

         public int getColumnCount() {
            return 2;
         }

         public int getRowCount() {
            return this.style == null ? 0 : this.style.getLength();
         }

         public boolean isCellEditable(int row, int col) {
            return false;
         }

         public Object getValueAt(int row, int col) {
            String prop = (String)this.propertyNames.get(row);
            return col == 0 ? prop : this.style.getPropertyValue(prop);
         }
      }

      protected class NodeRenderer extends DefaultTreeCellRenderer {
         protected ImageIcon elementIcon;
         protected ImageIcon commentIcon;
         protected ImageIcon piIcon;
         protected ImageIcon textIcon;

         public NodeRenderer() {
            String s = DOMViewer.resources.getString("Element.icon");
            this.elementIcon = new ImageIcon(this.getClass().getResource(s));
            s = DOMViewer.resources.getString("Comment.icon");
            this.commentIcon = new ImageIcon(this.getClass().getResource(s));
            s = DOMViewer.resources.getString("PI.icon");
            this.piIcon = new ImageIcon(this.getClass().getResource(s));
            s = DOMViewer.resources.getString("Text.icon");
            this.textIcon = new ImageIcon(this.getClass().getResource(s));
         }

         public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            switch (this.getNodeType(value)) {
               case 1:
                  this.setIcon(this.elementIcon);
               case 2:
               case 5:
               case 6:
               default:
                  break;
               case 3:
               case 4:
                  this.setIcon(this.textIcon);
                  break;
               case 7:
                  this.setIcon(this.piIcon);
                  break;
               case 8:
                  this.setIcon(this.commentIcon);
            }

            return this;
         }

         protected short getNodeType(Object value) {
            DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)value;
            Object obj = mtn.getUserObject();
            if (obj instanceof NodeInfo) {
               Node node = ((NodeInfo)obj).getNode();
               return node.getNodeType();
            } else {
               return -1;
            }
         }
      }

      protected class DOMTreeSelectionListener implements TreeSelectionListener {
         public void valueChanged(TreeSelectionEvent ev) {
            if (DOMViewer.this.elementOverlayManager != null) {
               this.handleElementSelection(ev);
            }

            DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)Panel.this.tree.getLastSelectedPathComponent();
            if (mtn != null) {
               if (Panel.this.rightPanel.getComponentCount() != 0) {
                  Panel.this.rightPanel.remove(0);
               }

               Object nodeInfo = mtn.getUserObject();
               if (nodeInfo instanceof NodeInfo) {
                  Node node = ((NodeInfo)nodeInfo).getNode();
                  switch (node.getNodeType()) {
                     case 1:
                        Panel.this.propertiesTable.setModel(Panel.this.new NodeCSSValuesModel(node));
                        Panel.this.attributePanel.promptForChanges();
                        Panel.this.attributePanel.setPreviewElement((Element)node);
                        Panel.this.rightPanel.add(Panel.this.elementPanel);
                     case 2:
                     case 5:
                     case 6:
                     case 7:
                     default:
                        break;
                     case 3:
                     case 4:
                     case 8:
                        Panel.this.characterDataPanel.setNode(node);
                        Panel.this.characterDataPanel.getTextArea().setText(node.getNodeValue());
                        Panel.this.rightPanel.add(Panel.this.characterDataPanel);
                        break;
                     case 9:
                        Panel.this.documentInfo.setText(this.createDocumentText((Document)node));
                        Panel.this.rightPanel.add(Panel.this.documentInfoPanel);
                  }
               }

               Panel.this.splitPane.revalidate();
               Panel.this.splitPane.repaint();
            }
         }

         protected String createDocumentText(Document doc) {
            StringBuffer sb = new StringBuffer();
            sb.append("Nodes: ");
            sb.append(this.nodeCount(doc));
            return sb.toString();
         }

         protected int nodeCount(Node node) {
            int result = 1;

            for(Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
               result += this.nodeCount(n);
            }

            return result;
         }

         protected void handleElementSelection(TreeSelectionEvent ev) {
            TreePath[] paths = ev.getPaths();
            TreePath[] var3 = paths;
            int var4 = paths.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               TreePath path = var3[var5];
               DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)path.getLastPathComponent();
               Object nodeInfo = mtn.getUserObject();
               if (nodeInfo instanceof NodeInfo) {
                  Node node = ((NodeInfo)nodeInfo).getNode();
                  if (node.getNodeType() == 1) {
                     if (ev.isAddedPath(path)) {
                        DOMViewer.this.elementOverlayManager.addElement((Element)node);
                     } else {
                        DOMViewer.this.elementOverlayManager.removeElement((Element)node);
                     }
                  }
               }
            }

            DOMViewer.this.elementOverlayManager.repaint();
         }
      }

      protected class TreeNodeRemover implements ActionListener {
         public void actionPerformed(ActionEvent e) {
            DOMViewer.this.addChangesToHistory();
            AbstractCompoundCommand cmd = DOMViewer.this.historyBrowserInterface.createRemoveSelectedTreeNodesCommand((ArrayList)null);
            TreePath[] treePaths = Panel.this.tree.getSelectionPaths();

            for(int i = 0; treePaths != null && i < treePaths.length; ++i) {
               TreePath treePath = treePaths[i];
               DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
               NodeInfo nodeInfo = (NodeInfo)node.getUserObject();
               if (DOMUtilities.isParentOf(nodeInfo.getNode(), nodeInfo.getNode().getParentNode())) {
                  cmd.addCommand(DOMViewer.this.historyBrowserInterface.createRemoveChildCommand(nodeInfo.getNode().getParentNode(), nodeInfo.getNode()));
               }
            }

            DOMViewer.this.historyBrowserInterface.performCompoundUpdateCommand(cmd);
         }
      }

      protected class NodeTemplateParser implements ActionListener {
         protected String toParse;
         protected short nodeType;

         public NodeTemplateParser(String toParse, short nodeType) {
            this.toParse = toParse;
            this.nodeType = nodeType;
         }

         public void actionPerformed(ActionEvent e) {
            Node nodeToAdd = null;
            switch (this.nodeType) {
               case 1:
                  URL urlObj = null;
                  if (Panel.this.document instanceof SVGOMDocument) {
                     urlObj = ((SVGOMDocument)Panel.this.document).getURLObject();
                  }

                  String uri = urlObj == null ? "" : urlObj.toString();
                  Map prefixes = new HashMap();
                  prefixes.put("xmlns", "http://www.w3.org/2000/svg");
                  prefixes.put("xmlns:xlink", "http://www.w3.org/1999/xlink");
                  SAXDocumentFactory df = new SAXDocumentFactory(Panel.this.document.getImplementation(), XMLResourceDescriptor.getXMLParserClassName());
                  DocumentFragment documentFragment = (DocumentFragment)DOMUtilities.parseXML(this.toParse, Panel.this.document, uri, prefixes, "svg", df);
                  nodeToAdd = documentFragment.getFirstChild();
               case 2:
               case 5:
               case 6:
               case 7:
               default:
                  break;
               case 3:
                  nodeToAdd = Panel.this.document.createTextNode(this.toParse);
                  break;
               case 4:
                  nodeToAdd = Panel.this.document.createCDATASection(this.toParse);
                  break;
               case 8:
                  nodeToAdd = Panel.this.document.createComment(this.toParse);
            }

            TreePath[] treePaths = Panel.this.tree.getSelectionPaths();
            if (treePaths != null) {
               TreePath treePath = treePaths[treePaths.length - 1];
               DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
               NodeInfo nodeInfo = (NodeInfo)node.getUserObject();
               DOMViewer.this.addChangesToHistory();
               DOMViewer.this.historyBrowserInterface.appendChild(nodeInfo.getNode(), (Node)nodeToAdd);
            }

         }
      }

      protected class TreeNodeAdder implements ActionListener {
         public void actionPerformed(ActionEvent e) {
            NodePickerPanel.NameEditorDialog nameEditorDialog = new NodePickerPanel.NameEditorDialog(DOMViewer.this);
            nameEditorDialog.setLocationRelativeTo(DOMViewer.this);
            int results = nameEditorDialog.showDialog();
            if (results == 0) {
               Element elementToAdd = Panel.this.document.createElementNS("http://www.w3.org/2000/svg", nameEditorDialog.getResults());
               if (Panel.this.rightPanel.getComponentCount() != 0) {
                  Panel.this.rightPanel.remove(0);
               }

               Panel.this.rightPanel.add(Panel.this.elementPanel);
               TreePath[] treePaths = Panel.this.tree.getSelectionPaths();
               if (treePaths != null) {
                  TreePath treePath = treePaths[treePaths.length - 1];
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
                  NodeInfo nodeInfo = (NodeInfo)node.getUserObject();
                  Panel.this.attributePanel.enterAddNewElementMode(elementToAdd, nodeInfo.getNode());
               }
            }

         }
      }

      protected class TreePopUpListener extends MouseAdapter {
         protected JPopupMenu treePopupMenu = new JPopupMenu();

         public TreePopUpListener() {
            this.treePopupMenu.add(Panel.this.createTemplatesMenu(DOMViewer.resources.getString("ContextMenuItem.insertNewNode")));
            JMenuItem item = new JMenuItem(DOMViewer.resources.getString("ContextMenuItem.createNewElement"));
            this.treePopupMenu.add(item);
            item.addActionListener(Panel.this.new TreeNodeAdder());
            item = new JMenuItem(DOMViewer.resources.getString("ContextMenuItem.removeSelection"));
            item.addActionListener(Panel.this.new TreeNodeRemover());
            this.treePopupMenu.add(item);
         }

         public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getClickCount() == 1 && Panel.this.tree.getSelectionPaths() != null) {
               this.showPopUp(e);
            }

         }

         public void mousePressed(MouseEvent e) {
            JTree sourceTree = (JTree)e.getSource();
            TreePath targetPath = sourceTree.getPathForLocation(e.getX(), e.getY());
            if (!e.isControlDown() && !e.isShiftDown()) {
               sourceTree.setSelectionPath(targetPath);
            } else {
               sourceTree.addSelectionPath(targetPath);
            }

            if (e.isPopupTrigger() && e.getClickCount() == 1) {
               this.showPopUp(e);
            }

         }

         private void showPopUp(MouseEvent e) {
            if (DOMViewer.this.canEdit()) {
               TreePath path = Panel.this.tree.getPathForLocation(e.getX(), e.getY());
               if (path != null && path.getPathCount() > 1) {
                  this.treePopupMenu.show((Component)e.getSource(), e.getX(), e.getY());
               }
            }

         }
      }

      protected class CapturingClickHandler implements EventListener {
         public void handleEvent(Event evt) {
            if (DOMViewer.this.isCapturingClickEnabled) {
               Element targetElement = (Element)evt.getTarget();
               Panel.this.selectNode(targetElement);
            }

         }
      }

      protected class CharDataModificationHandler implements EventListener {
         public void handleEvent(final Event evt) {
            Runnable runnable = new Runnable() {
               public void run() {
                  MutationEvent mevt = (MutationEvent)evt;
                  Node targetNode = (Node)mevt.getTarget();
                  if (Panel.this.characterDataPanel.getNode() == targetNode) {
                     Panel.this.characterDataPanel.getTextArea().setText(targetNode.getNodeValue());
                     Panel.this.attributePanel.updateOnDocumentChange(mevt.getType(), targetNode);
                  }

               }
            };
            Panel.this.refreshGUI(runnable);
            if (Panel.this.characterDataPanel.getNode() == evt.getTarget()) {
               Panel.this.registerDocumentChange((MutationEvent)evt);
            }

         }
      }

      protected class AttributeModificationHandler implements EventListener {
         public void handleEvent(final Event evt) {
            Runnable runnable = new Runnable() {
               public void run() {
                  MutationEvent mevt = (MutationEvent)evt;
                  Element targetElement = (Element)mevt.getTarget();
                  DefaultTreeModel model = (DefaultTreeModel)Panel.this.tree.getModel();
                  model.nodeChanged(Panel.this.findNode(Panel.this.tree, targetElement));
                  Panel.this.attributePanel.updateOnDocumentChange(mevt.getType(), targetElement);
               }
            };
            Panel.this.refreshGUI(runnable);
            Panel.this.registerDocumentChange((MutationEvent)evt);
         }
      }

      protected class NodeRemovalHandler implements EventListener {
         public void handleEvent(final Event evt) {
            Runnable runnable = new Runnable() {
               public void run() {
                  MutationEvent mevt = (MutationEvent)evt;
                  Node targetNode = (Node)mevt.getTarget();
                  DefaultMutableTreeNode treeNode = Panel.this.findNode(Panel.this.tree, targetNode);
                  DefaultTreeModel model = (DefaultTreeModel)Panel.this.tree.getModel();
                  if (treeNode != null) {
                     model.removeNodeFromParent(treeNode);
                  }

                  Panel.this.attributePanel.updateOnDocumentChange(mevt.getType(), targetNode);
               }
            };
            Panel.this.refreshGUI(runnable);
            Panel.this.registerDocumentChange((MutationEvent)evt);
         }
      }

      protected class NodeInsertionHandler implements EventListener {
         public void handleEvent(final Event evt) {
            Runnable runnable = new Runnable() {
               public void run() {
                  MutationEvent mevt = (MutationEvent)evt;
                  Node targetNode = (Node)mevt.getTarget();
                  DefaultMutableTreeNode parentNode = Panel.this.findNode(Panel.this.tree, targetNode.getParentNode());
                  DefaultMutableTreeNode insertedNode = (DefaultMutableTreeNode)Panel.this.createTree(targetNode, DOMViewer.this.showWhitespace);
                  DefaultTreeModel model = (DefaultTreeModel)Panel.this.tree.getModel();
                  DefaultMutableTreeNode newParentNode = (DefaultMutableTreeNode)Panel.this.createTree(targetNode.getParentNode(), DOMViewer.this.showWhitespace);
                  int index = NodeInsertionHandler.this.findIndexToInsert(parentNode, newParentNode);
                  if (index != -1) {
                     model.insertNodeInto(insertedNode, parentNode, index);
                  }

                  Panel.this.attributePanel.updateOnDocumentChange(mevt.getType(), targetNode);
               }
            };
            Panel.this.refreshGUI(runnable);
            Panel.this.registerDocumentChange((MutationEvent)evt);
         }

         protected int findIndexToInsert(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode newParentNode) {
            int index = -1;
            if (parentNode != null && newParentNode != null) {
               Enumeration oldChildren = parentNode.children();
               Enumeration newChildren = newParentNode.children();

               int count;
               for(count = 0; oldChildren.hasMoreElements(); ++count) {
                  DefaultMutableTreeNode currentOldChild = (DefaultMutableTreeNode)oldChildren.nextElement();
                  DefaultMutableTreeNode currentNewChild = (DefaultMutableTreeNode)newChildren.nextElement();
                  Node oldChild = ((NodeInfo)currentOldChild.getUserObject()).getNode();
                  Node newChild = ((NodeInfo)currentNewChild.getUserObject()).getNode();
                  if (oldChild != newChild) {
                     return count;
                  }
               }

               return count;
            } else {
               return index;
            }
         }
      }

      protected class CharacterPanel extends JPanel {
         protected Node node;
         protected JTextArea textArea = new JTextArea();

         public CharacterPanel(BorderLayout layout) {
            super(layout);
         }

         public JTextArea getTextArea() {
            return this.textArea;
         }

         public void setTextArea(JTextArea textArea) {
            this.textArea = textArea;
         }

         public Node getNode() {
            return this.node;
         }

         public void setNode(Node node) {
            this.node = node;
         }
      }
   }

   protected class DOMViewerElementOverlayController implements ElementOverlayController {
      public boolean isOverlayEnabled() {
         return DOMViewer.this.canEdit() && DOMViewer.this.isElementOverlayEnabled;
      }
   }

   protected class DOMViewerDOMDocumentTreeController implements DOMDocumentTreeController {
      public boolean isDNDSupported() {
         return DOMViewer.this.canEdit();
      }
   }

   protected class DOMViewerNodePickerController implements NodePickerController {
      public boolean isEditable() {
         return DOMViewer.this.canEdit();
      }

      public boolean canEdit(Element el) {
         return true;
      }
   }

   protected class OverlayButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         DOMViewer.this.toggleOverlay();
      }
   }

   protected class CapturingClickButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JToggleButton btn = (JToggleButton)e.getSource();
         DOMViewer.this.isCapturingClickEnabled = btn.isSelected();
         if (!DOMViewer.this.isCapturingClickEnabled) {
            btn.setToolTipText(DOMViewer.resources.getString("CapturingClickButton.tooltip"));
         } else {
            btn.setToolTipText(DOMViewer.resources.getString("CapturingClickButton.disableText"));
         }

      }
   }

   protected class RedoButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         DOMViewer.this.addChangesToHistory();
         DOMViewer.this.historyBrowserInterface.getHistoryBrowser().redo();
      }
   }

   protected class UndoButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         DOMViewer.this.addChangesToHistory();
         DOMViewer.this.historyBrowserInterface.getHistoryBrowser().undo();
      }
   }

   protected class CloseButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (DOMViewer.this.panel.attributePanel.panelHiding()) {
            DOMViewer.this.panel.tree.setSelectionRow(0);
            DOMViewer.this.dispose();
         }

      }
   }
}
