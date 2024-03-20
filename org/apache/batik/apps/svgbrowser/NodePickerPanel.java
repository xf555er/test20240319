package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.gui.xmleditor.XMLTextEditor;
import org.apache.batik.util.resources.ResourceManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class NodePickerPanel extends JPanel implements ActionMap {
   private static final int VIEW_MODE = 1;
   private static final int EDIT_MODE = 2;
   private static final int ADD_NEW_ELEMENT = 3;
   private static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.NodePickerPanelMessages";
   private static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.NodePickerPanelMessages", Locale.getDefault());
   private static ResourceManager resources;
   private JTable attributesTable;
   private TableModelListener tableModelListener;
   private JScrollPane attributePane;
   private JPanel attributesPanel;
   private ButtonFactory buttonFactory;
   private JButton addButton;
   private JButton removeButton;
   private JLabel attributesLabel;
   private JButton applyButton;
   private JButton resetButton;
   private JPanel choosePanel;
   private SVGInputPanel svgInputPanel;
   private JLabel isWellFormedLabel;
   private JLabel svgInputPanelNameLabel;
   private boolean shouldProcessUpdate = true;
   private Element previewElement;
   private Element clonedElement;
   private Node parentElement;
   private int mode;
   private boolean isDirty;
   private EventListenerList eventListeners = new EventListenerList();
   private NodePickerController controller;
   private Map listeners = new HashMap(10);

   public NodePickerPanel(NodePickerController controller) {
      super(new GridBagLayout());
      this.controller = controller;
      this.initialize();
   }

   private void initialize() {
      this.addButtonActions();
      GridBagConstraints grid = new GridBagConstraints();
      grid.gridx = 1;
      grid.gridy = 1;
      grid.anchor = 18;
      grid.fill = 0;
      grid.insets = new Insets(5, 5, 0, 5);
      this.attributesLabel = new JLabel();
      String attributesLabelValue = resources.getString("AttributesTable.name");
      this.attributesLabel.setText(attributesLabelValue);
      this.add(this.attributesLabel, grid);
      grid.gridx = 1;
      grid.gridy = 2;
      grid.gridwidth = 2;
      grid.weightx = 1.0;
      grid.weighty = 0.3;
      grid.fill = 1;
      grid.anchor = 10;
      grid.insets = new Insets(0, 0, 0, 5);
      this.add(this.getAttributesPanel(), grid);
      grid.weightx = 0.0;
      grid.weighty = 0.0;
      grid.gridwidth = 1;
      grid.gridx = 1;
      grid.gridy = 3;
      grid.anchor = 18;
      grid.fill = 0;
      grid.insets = new Insets(0, 5, 0, 5);
      this.svgInputPanelNameLabel = new JLabel();
      String svgInputLabelValue = resources.getString("InputPanelLabel.name");
      this.svgInputPanelNameLabel.setText(svgInputLabelValue);
      this.add(this.svgInputPanelNameLabel, grid);
      grid.gridx = 1;
      grid.gridy = 4;
      grid.gridwidth = 2;
      grid.weightx = 1.0;
      grid.weighty = 1.0;
      grid.fill = 1;
      grid.anchor = 10;
      grid.insets = new Insets(0, 5, 0, 10);
      this.add(this.getSvgInputPanel(), grid);
      grid.weightx = 0.0;
      grid.weighty = 0.0;
      grid.gridwidth = 1;
      grid.gridx = 1;
      grid.gridy = 5;
      grid.anchor = 18;
      grid.fill = 0;
      grid.insets = new Insets(5, 5, 0, 5);
      this.isWellFormedLabel = new JLabel();
      String isWellFormedLabelVal = resources.getString("IsWellFormedLabel.wellFormed");
      this.isWellFormedLabel.setText(isWellFormedLabelVal);
      this.add(this.isWellFormedLabel, grid);
      grid.weightx = 0.0;
      grid.weighty = 0.0;
      grid.gridwidth = 1;
      grid.gridx = 2;
      grid.gridy = 5;
      grid.anchor = 13;
      grid.insets = new Insets(0, 0, 0, 5);
      this.add(this.getChoosePanel(), grid);
      this.enterViewMode();
   }

   private ButtonFactory getButtonFactory() {
      if (this.buttonFactory == null) {
         this.buttonFactory = new ButtonFactory(bundle, this);
      }

      return this.buttonFactory;
   }

   private void addButtonActions() {
      this.listeners.put("ApplyButtonAction", new ApplyButtonAction());
      this.listeners.put("ResetButtonAction", new ResetButtonAction());
      this.listeners.put("AddButtonAction", new AddButtonAction());
      this.listeners.put("RemoveButtonAction", new RemoveButtonAction());
   }

   private JButton getAddButton() {
      if (this.addButton == null) {
         this.addButton = this.getButtonFactory().createJButton("AddButton");
         this.addButton.addFocusListener(new NodePickerEditListener());
      }

      return this.addButton;
   }

   private JButton getRemoveButton() {
      if (this.removeButton == null) {
         this.removeButton = this.getButtonFactory().createJButton("RemoveButton");
         this.removeButton.addFocusListener(new NodePickerEditListener());
      }

      return this.removeButton;
   }

   private JButton getApplyButton() {
      if (this.applyButton == null) {
         this.applyButton = this.getButtonFactory().createJButton("ApplyButton");
      }

      return this.applyButton;
   }

   private JButton getResetButton() {
      if (this.resetButton == null) {
         this.resetButton = this.getButtonFactory().createJButton("ResetButton");
      }

      return this.resetButton;
   }

   private JPanel getAttributesPanel() {
      if (this.attributesPanel == null) {
         this.attributesPanel = new JPanel(new GridBagLayout());
         GridBagConstraints g11 = new GridBagConstraints();
         g11.gridx = 1;
         g11.gridy = 1;
         g11.fill = 1;
         g11.anchor = 10;
         g11.weightx = 4.0;
         g11.weighty = 1.0;
         g11.gridheight = 5;
         g11.gridwidth = 2;
         g11.insets = new Insets(5, 5, 5, 0);
         GridBagConstraints g12 = new GridBagConstraints();
         g12.gridx = 3;
         g12.gridy = 1;
         g12.fill = 2;
         g12.anchor = 11;
         g12.insets = new Insets(5, 20, 0, 5);
         g12.weightx = 1.0;
         GridBagConstraints g32 = new GridBagConstraints();
         g32.gridx = 3;
         g32.gridy = 3;
         g32.fill = 2;
         g32.anchor = 11;
         g32.insets = new Insets(5, 20, 0, 5);
         g32.weightx = 1.0;
         this.attributesTable = new JTable();
         this.attributesTable.setModel(new AttributesTableModel(10, 2));
         this.tableModelListener = new AttributesTableModelListener();
         this.attributesTable.getModel().addTableModelListener(this.tableModelListener);
         this.attributesTable.addFocusListener(new NodePickerEditListener());
         this.attributePane = new JScrollPane();
         this.attributePane.getViewport().add(this.attributesTable);
         this.attributesPanel.add(this.attributePane, g11);
         this.attributesPanel.add(this.getAddButton(), g12);
         this.attributesPanel.add(this.getRemoveButton(), g32);
      }

      return this.attributesPanel;
   }

   private SVGInputPanel getSvgInputPanel() {
      if (this.svgInputPanel == null) {
         this.svgInputPanel = new SVGInputPanel();
         this.svgInputPanel.getNodeXmlArea().getDocument().addDocumentListener(new XMLAreaListener());
         this.svgInputPanel.getNodeXmlArea().addFocusListener(new NodePickerEditListener());
      }

      return this.svgInputPanel;
   }

   private JPanel getChoosePanel() {
      if (this.choosePanel == null) {
         this.choosePanel = new JPanel(new GridBagLayout());
         GridBagConstraints g11 = new GridBagConstraints();
         g11.gridx = 1;
         g11.gridy = 1;
         g11.weightx = 0.5;
         g11.anchor = 17;
         g11.fill = 2;
         g11.insets = new Insets(5, 5, 5, 5);
         GridBagConstraints g12 = new GridBagConstraints();
         g12.gridx = 2;
         g12.gridy = 1;
         g12.weightx = 0.5;
         g12.anchor = 13;
         g12.fill = 2;
         g12.insets = new Insets(5, 5, 5, 5);
         this.choosePanel.add(this.getApplyButton(), g11);
         this.choosePanel.add(this.getResetButton(), g12);
      }

      return this.choosePanel;
   }

   public String getResults() {
      return this.getSvgInputPanel().getNodeXmlArea().getText();
   }

   private void updateViewAfterSvgInput(Element referentElement, Element elementToUpdate) {
      String isWellFormedLabelVal;
      if (referentElement != null) {
         isWellFormedLabelVal = resources.getString("IsWellFormedLabel.wellFormed");
         this.isWellFormedLabel.setText(isWellFormedLabelVal);
         this.getApplyButton().setEnabled(true);
         this.attributesTable.setEnabled(true);
         this.updateElementAttributes(elementToUpdate, referentElement);
         this.shouldProcessUpdate = false;
         this.updateAttributesTable(elementToUpdate);
         this.shouldProcessUpdate = true;
      } else {
         isWellFormedLabelVal = resources.getString("IsWellFormedLabel.notWellFormed");
         this.isWellFormedLabel.setText(isWellFormedLabelVal);
         this.getApplyButton().setEnabled(false);
         this.attributesTable.setEnabled(false);
      }

   }

   private void updateElementAttributes(Element elem, Element referentElement) {
      this.removeAttributes(elem);
      NamedNodeMap newNodeMap = referentElement.getAttributes();

      for(int i = newNodeMap.getLength() - 1; i >= 0; --i) {
         Node newAttr = newNodeMap.item(i);
         String qualifiedName = newAttr.getNodeName();
         String attributeValue = newAttr.getNodeValue();
         String prefix = DOMUtilities.getPrefix(qualifiedName);
         String namespaceURI = this.getNamespaceURI(prefix);
         elem.setAttributeNS(namespaceURI, qualifiedName, attributeValue);
      }

   }

   private void updateElementAttributes(Element element, AttributesTableModel tableModel) {
      this.removeAttributes(element);

      for(int i = 0; i < tableModel.getRowCount(); ++i) {
         String newAttrName = (String)tableModel.getAttrNameAt(i);
         String newAttrValue = (String)tableModel.getAttrValueAt(i);
         if (newAttrName != null && newAttrName.length() > 0) {
            String namespaceURI;
            if (newAttrName.equals("xmlns")) {
               namespaceURI = "http://www.w3.org/2000/xmlns/";
            } else {
               String prefix = DOMUtilities.getPrefix(newAttrName);
               namespaceURI = this.getNamespaceURI(prefix);
            }

            if (newAttrValue != null) {
               element.setAttributeNS(namespaceURI, newAttrName, newAttrValue);
            } else {
               element.setAttributeNS(namespaceURI, newAttrName, "");
            }
         }
      }

   }

   private void removeAttributes(Element element) {
      NamedNodeMap oldNodeMap = element.getAttributes();
      int n = oldNodeMap.getLength();

      for(int i = n - 1; i >= 0; --i) {
         element.removeAttributeNode((Attr)oldNodeMap.item(i));
      }

   }

   private String getNamespaceURI(String prefix) {
      String namespaceURI = null;
      if (prefix != null) {
         if (prefix.equals("xmlns")) {
            namespaceURI = "http://www.w3.org/2000/xmlns/";
         } else {
            AbstractNode n;
            if (this.mode == 2) {
               n = (AbstractNode)this.previewElement;
               namespaceURI = n.lookupNamespaceURI(prefix);
            } else if (this.mode == 3) {
               n = (AbstractNode)this.parentElement;
               namespaceURI = n.lookupNamespaceURI(prefix);
            }
         }
      }

      return namespaceURI;
   }

   private void updateAttributesTable(Element elem) {
      NamedNodeMap map = elem.getAttributes();
      AttributesTableModel tableModel = (AttributesTableModel)this.attributesTable.getModel();

      int i;
      String newAttrValue;
      for(i = tableModel.getRowCount() - 1; i >= 0; --i) {
         String attrName = (String)tableModel.getValueAt(i, 0);
         newAttrValue = "";
         if (attrName != null) {
            newAttrValue = elem.getAttributeNS((String)null, attrName);
         }

         if (attrName == null || newAttrValue.length() == 0) {
            tableModel.removeRow(i);
         }

         if (newAttrValue.length() > 0) {
            tableModel.setValueAt(newAttrValue, i, 1);
         }
      }

      for(i = 0; i < map.getLength(); ++i) {
         Node attr = map.item(i);
         newAttrValue = attr.getNodeName();
         String attrValue = attr.getNodeValue();
         if (tableModel.getValueForName(newAttrValue) == null) {
            Vector rowData = new Vector();
            rowData.add(newAttrValue);
            rowData.add(attrValue);
            tableModel.addRow(rowData);
         }
      }

   }

   private void updateNodeXmlArea(Node node) {
      this.getSvgInputPanel().getNodeXmlArea().setText(DOMUtilities.getXML(node));
   }

   private Element getPreviewElement() {
      return this.previewElement;
   }

   public void setPreviewElement(Element elem) {
      if (this.previewElement == elem || !this.isDirty || this.promptForChanges()) {
         this.previewElement = elem;
         this.enterViewMode();
         this.updateNodeXmlArea(elem);
         this.updateAttributesTable(elem);
      }
   }

   boolean panelHiding() {
      return !this.isDirty || this.promptForChanges();
   }

   private int getMode() {
      return this.mode;
   }

   public void enterViewMode() {
      if (this.mode != 1) {
         this.mode = 1;
         this.getApplyButton().setEnabled(false);
         this.getResetButton().setEnabled(false);
         this.getRemoveButton().setEnabled(true);
         this.getAddButton().setEnabled(true);
         String isWellFormedLabelVal = resources.getString("IsWellFormedLabel.wellFormed");
         this.isWellFormedLabel.setText(isWellFormedLabelVal);
      }

   }

   public void enterEditMode() {
      if (this.mode != 2) {
         this.mode = 2;
         this.clonedElement = (Element)this.previewElement.cloneNode(true);
         this.getApplyButton().setEnabled(true);
         this.getResetButton().setEnabled(true);
      }

   }

   public void enterAddNewElementMode(Element newElement, Node parent) {
      if (this.mode != 3) {
         this.mode = 3;
         this.previewElement = newElement;
         this.clonedElement = (Element)newElement.cloneNode(true);
         this.parentElement = parent;
         this.updateNodeXmlArea(newElement);
         this.getApplyButton().setEnabled(true);
         this.getResetButton().setEnabled(true);
      }

   }

   public void updateOnDocumentChange(String mutationEventType, Node targetNode) {
      if (this.mode == 1 && this.isShowing() && this.shouldUpdate(mutationEventType, targetNode, this.getPreviewElement())) {
         this.setPreviewElement(this.getPreviewElement());
      }

   }

   private boolean shouldUpdate(String mutationEventType, Node affectedNode, Node currentNode) {
      if (mutationEventType.equals("DOMNodeInserted")) {
         if (DOMUtilities.isAncestorOf(currentNode, affectedNode)) {
            return true;
         }
      } else if (mutationEventType.equals("DOMNodeRemoved")) {
         if (DOMUtilities.isAncestorOf(currentNode, affectedNode)) {
            return true;
         }
      } else if (mutationEventType.equals("DOMAttrModified")) {
         if (DOMUtilities.isAncestorOf(currentNode, affectedNode) || currentNode == affectedNode) {
            return true;
         }
      } else if (mutationEventType.equals("DOMCharDataModified") && DOMUtilities.isAncestorOf(currentNode, affectedNode)) {
         return true;
      }

      return false;
   }

   private Element parseXml(String xmlString) {
      Document doc = null;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      try {
         factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
         factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
         factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         DocumentBuilder parser = factory.newDocumentBuilder();
         parser.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
            }

            public void fatalError(SAXParseException exception) throws SAXException {
            }

            public void warning(SAXParseException exception) throws SAXException {
            }
         });
         doc = parser.parse(new InputSource(new StringReader(xmlString)));
      } catch (ParserConfigurationException var5) {
      } catch (SAXException var6) {
      } catch (IOException var7) {
      }

      return doc != null ? doc.getDocumentElement() : null;
   }

   public void setEditable(boolean editable) {
      this.getSvgInputPanel().getNodeXmlArea().setEditable(editable);
      this.getResetButton().setEnabled(editable);
      this.getApplyButton().setEnabled(editable);
      this.getAddButton().setEnabled(editable);
      this.getRemoveButton().setEnabled(editable);
      this.attributesTable.setEnabled(editable);
   }

   private boolean isANodePickerComponent(Component component) {
      return SwingUtilities.getAncestorOfClass(NodePickerPanel.class, component) != null;
   }

   public boolean promptForChanges() {
      if (this.getApplyButton().isEnabled() && this.isElementModified()) {
         String confirmString = resources.getString("ConfirmDialog.message");
         int option = JOptionPane.showConfirmDialog(this.getSvgInputPanel(), confirmString);
         if (option == 0) {
            this.getApplyButton().doClick();
         } else {
            if (option == 2) {
               return false;
            }

            this.getResetButton().doClick();
         }
      } else {
         this.getResetButton().doClick();
      }

      this.isDirty = false;
      return true;
   }

   private boolean isElementModified() {
      if (this.getMode() == 2) {
         return !DOMUtilities.getXML(this.previewElement).equals(this.getSvgInputPanel().getNodeXmlArea().getText());
      } else {
         return this.getMode() == 3;
      }
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   public void fireUpdateElement(NodePickerEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == NodePickerListener.class) {
            ((NodePickerListener)listeners[i + 1]).updateElement(event);
         }
      }

   }

   public void fireAddNewElement(NodePickerEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == NodePickerListener.class) {
            ((NodePickerListener)listeners[i + 1]).addNewElement(event);
         }
      }

   }

   public void addListener(NodePickerListener listener) {
      this.eventListeners.add(NodePickerListener.class, listener);
   }

   static {
      resources = new ResourceManager(bundle);
   }

   public static class NameEditorDialog extends JDialog implements ActionMap {
      public static final int OK_OPTION = 0;
      public static final int CANCEL_OPTION = 1;
      protected static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.NameEditorDialogMessages";
      protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.NameEditorDialogMessages", Locale.getDefault());
      protected static ResourceManager resources;
      protected int returnCode;
      protected JPanel mainPanel;
      protected ButtonFactory buttonFactory;
      protected JLabel nodeNameLabel;
      protected JTextField nodeNameField;
      protected JButton okButton;
      protected JButton cancelButton;
      protected Map listeners = new HashMap(10);

      public NameEditorDialog(Frame frame) {
         super(frame, true);
         this.setResizable(false);
         this.setModal(true);
         this.initialize();
      }

      protected void initialize() {
         this.setSize(resources.getInteger("Dialog.width"), resources.getInteger("Dialog.height"));
         this.setTitle(resources.getString("Dialog.title"));
         this.addButtonActions();
         this.setContentPane(this.getMainPanel());
      }

      protected ButtonFactory getButtonFactory() {
         if (this.buttonFactory == null) {
            this.buttonFactory = new ButtonFactory(bundle, this);
         }

         return this.buttonFactory;
      }

      protected void addButtonActions() {
         this.listeners.put("OKButtonAction", new OKButtonAction());
         this.listeners.put("CancelButtonAction", new CancelButtonAction());
      }

      public int showDialog() {
         this.setVisible(true);
         return this.returnCode;
      }

      protected JButton getOkButton() {
         if (this.okButton == null) {
            this.okButton = this.getButtonFactory().createJButton("OKButton");
            this.getRootPane().setDefaultButton(this.okButton);
         }

         return this.okButton;
      }

      protected JButton getCancelButton() {
         if (this.cancelButton == null) {
            this.cancelButton = this.getButtonFactory().createJButton("CancelButton");
         }

         return this.cancelButton;
      }

      protected JPanel getMainPanel() {
         if (this.mainPanel == null) {
            this.mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gridBag = new GridBagConstraints();
            gridBag.gridx = 1;
            gridBag.gridy = 1;
            gridBag.fill = 0;
            gridBag.insets = new Insets(5, 5, 5, 5);
            this.mainPanel.add(this.getNodeNameLabel(), gridBag);
            gridBag.gridx = 2;
            gridBag.weightx = 1.0;
            gridBag.weighty = 1.0;
            gridBag.fill = 2;
            gridBag.anchor = 10;
            this.mainPanel.add(this.getNodeNameField(), gridBag);
            gridBag.gridx = 1;
            gridBag.gridy = 2;
            gridBag.weightx = 0.0;
            gridBag.weighty = 0.0;
            gridBag.anchor = 13;
            gridBag.fill = 2;
            this.mainPanel.add(this.getOkButton(), gridBag);
            gridBag.gridx = 2;
            gridBag.gridy = 2;
            gridBag.anchor = 13;
            this.mainPanel.add(this.getCancelButton(), gridBag);
         }

         return this.mainPanel;
      }

      public JLabel getNodeNameLabel() {
         if (this.nodeNameLabel == null) {
            this.nodeNameLabel = new JLabel();
            this.nodeNameLabel.setText(resources.getString("Dialog.label"));
         }

         return this.nodeNameLabel;
      }

      protected JTextField getNodeNameField() {
         if (this.nodeNameField == null) {
            this.nodeNameField = new JTextField();
         }

         return this.nodeNameField;
      }

      public String getResults() {
         return this.nodeNameField.getText();
      }

      public Action getAction(String key) throws MissingListenerException {
         return (Action)this.listeners.get(key);
      }

      static {
         resources = new ResourceManager(bundle);
      }

      protected class CancelButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            NameEditorDialog.this.returnCode = 1;
            NameEditorDialog.this.dispose();
         }
      }

      protected class OKButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            NameEditorDialog.this.returnCode = 0;
            NameEditorDialog.this.dispose();
         }
      }
   }

   protected static class SVGInputPanel extends JPanel {
      protected XMLTextEditor nodeXmlArea;

      public SVGInputPanel() {
         super(new BorderLayout());
         this.add(new JScrollPane(this.getNodeXmlArea()));
      }

      protected XMLTextEditor getNodeXmlArea() {
         if (this.nodeXmlArea == null) {
            this.nodeXmlArea = new XMLTextEditor();
            this.nodeXmlArea.setEditable(true);
         }

         return this.nodeXmlArea;
      }
   }

   public static class NodePickerAdapter implements NodePickerListener {
      public void addNewElement(NodePickerEvent event) {
      }

      public void updateElement(NodePickerEvent event) {
      }
   }

   public interface NodePickerListener extends EventListener {
      void updateElement(NodePickerEvent var1);

      void addNewElement(NodePickerEvent var1);
   }

   public static class NodePickerEvent extends EventObject {
      public static final int EDIT_ELEMENT = 1;
      public static final int ADD_NEW_ELEMENT = 2;
      private int type;
      private String result;
      private Node contextNode;

      public NodePickerEvent(Object source, String result, Node contextNode, int type) {
         super(source);
         this.result = result;
         this.contextNode = contextNode;
      }

      public String getResult() {
         return this.result;
      }

      public Node getContextNode() {
         return this.contextNode;
      }

      public int getType() {
         return this.type;
      }
   }

   public static class AttributesTableModel extends DefaultTableModel {
      public AttributesTableModel(int rowCount, int columnCount) {
         super(rowCount, columnCount);
      }

      public String getColumnName(int column) {
         return column == 0 ? NodePickerPanel.resources.getString("AttributesTable.column1") : NodePickerPanel.resources.getString("AttributesTable.column2");
      }

      public Object getValueForName(Object attrName) {
         for(int i = 0; i < this.getRowCount(); ++i) {
            if (this.getValueAt(i, 0) != null && this.getValueAt(i, 0).equals(attrName)) {
               return this.getValueAt(i, 1);
            }
         }

         return null;
      }

      public Object getAttrNameAt(int i) {
         return this.getValueAt(i, 0);
      }

      public Object getAttrValueAt(int i) {
         return this.getValueAt(i, 1);
      }

      public int getRow(Object attrName) {
         for(int i = 0; i < this.getRowCount(); ++i) {
            if (this.getValueAt(i, 0) != null && this.getValueAt(i, 0).equals(attrName)) {
               return i;
            }
         }

         return -1;
      }
   }

   protected class RemoveButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (NodePickerPanel.this.getMode() == 1) {
            NodePickerPanel.this.enterEditMode();
         }

         Element contextElement = NodePickerPanel.this.clonedElement;
         if (NodePickerPanel.this.getMode() == 3) {
            contextElement = NodePickerPanel.this.previewElement;
         }

         DefaultTableModel model = (DefaultTableModel)NodePickerPanel.this.attributesTable.getModel();
         int[] selectedRows = NodePickerPanel.this.attributesTable.getSelectedRows();
         int[] var5 = selectedRows;
         int var6 = selectedRows.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            int selectedRow = var5[var7];
            String attrName = (String)model.getValueAt(selectedRow, 0);
            if (attrName != null) {
               String prefix = DOMUtilities.getPrefix(attrName);
               String localName = DOMUtilities.getLocalName(attrName);
               String namespaceURI = NodePickerPanel.this.getNamespaceURI(prefix);
               contextElement.removeAttributeNS(namespaceURI, localName);
            }
         }

         NodePickerPanel.this.shouldProcessUpdate = false;
         NodePickerPanel.this.updateAttributesTable(contextElement);
         NodePickerPanel.this.shouldProcessUpdate = true;
         NodePickerPanel.this.updateNodeXmlArea(contextElement);
      }
   }

   protected class AddButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (NodePickerPanel.this.getMode() == 1) {
            NodePickerPanel.this.enterEditMode();
         }

         DefaultTableModel model = (DefaultTableModel)NodePickerPanel.this.attributesTable.getModel();
         NodePickerPanel.this.shouldProcessUpdate = false;
         model.addRow((Vector)null);
         NodePickerPanel.this.shouldProcessUpdate = true;
      }
   }

   protected class ResetButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         NodePickerPanel.this.isDirty = false;
         NodePickerPanel.this.setPreviewElement(NodePickerPanel.this.getPreviewElement());
      }
   }

   protected class ApplyButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         NodePickerPanel.this.isDirty = false;
         String xmlAreaText = NodePickerPanel.this.getResults();
         if (NodePickerPanel.this.getMode() == 2) {
            NodePickerPanel.this.fireUpdateElement(new NodePickerEvent(NodePickerPanel.this, xmlAreaText, NodePickerPanel.this.previewElement, 1));
         } else if (NodePickerPanel.this.getMode() == 3) {
            NodePickerPanel.this.fireAddNewElement(new NodePickerEvent(NodePickerPanel.this, xmlAreaText, NodePickerPanel.this.parentElement, 2));
         }

         NodePickerPanel.this.enterViewMode();
      }
   }

   protected class AttributesTableModelListener implements TableModelListener {
      public void tableChanged(TableModelEvent e) {
         if (e.getType() == 0 && NodePickerPanel.this.shouldProcessUpdate) {
            this.updateNodePicker(e);
         }

      }

      private void updateNodePicker(TableModelEvent e) {
         if (NodePickerPanel.this.getMode() == 2) {
            NodePickerPanel.this.updateElementAttributes(NodePickerPanel.this.clonedElement, (AttributesTableModel)((AttributesTableModel)e.getSource()));
            NodePickerPanel.this.updateNodeXmlArea(NodePickerPanel.this.clonedElement);
         } else if (NodePickerPanel.this.getMode() == 3) {
            NodePickerPanel.this.updateElementAttributes(NodePickerPanel.this.previewElement, (AttributesTableModel)((AttributesTableModel)e.getSource()));
            NodePickerPanel.this.updateNodeXmlArea(NodePickerPanel.this.previewElement);
         }

      }
   }

   protected class XMLAreaListener implements DocumentListener {
      public void changedUpdate(DocumentEvent e) {
         NodePickerPanel.this.isDirty = NodePickerPanel.this.isElementModified();
      }

      public void insertUpdate(DocumentEvent e) {
         this.updateNodePicker(e);
         NodePickerPanel.this.isDirty = NodePickerPanel.this.isElementModified();
      }

      public void removeUpdate(DocumentEvent e) {
         this.updateNodePicker(e);
         NodePickerPanel.this.isDirty = NodePickerPanel.this.isElementModified();
      }

      private void updateNodePicker(DocumentEvent e) {
         if (NodePickerPanel.this.getMode() == 2) {
            NodePickerPanel.this.updateViewAfterSvgInput(NodePickerPanel.this.parseXml(NodePickerPanel.this.svgInputPanel.getNodeXmlArea().getText()), NodePickerPanel.this.clonedElement);
         } else if (NodePickerPanel.this.getMode() == 3) {
            NodePickerPanel.this.updateViewAfterSvgInput(NodePickerPanel.this.parseXml(NodePickerPanel.this.svgInputPanel.getNodeXmlArea().getText()), NodePickerPanel.this.previewElement);
         }

      }
   }

   protected class NodePickerEditListener extends FocusAdapter {
      public void focusGained(FocusEvent e) {
         if (NodePickerPanel.this.getMode() == 1) {
            NodePickerPanel.this.enterEditMode();
         }

         NodePickerPanel.this.setEditable(NodePickerPanel.this.controller.isEditable() && NodePickerPanel.this.controller.canEdit(NodePickerPanel.this.previewElement));
         NodePickerPanel.this.isDirty = NodePickerPanel.this.isElementModified();
      }
   }
}
