package org.apache.batik.apps.svgbrowser;

import java.util.ArrayList;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HistoryBrowserInterface {
   private static final String ATTRIBUTE_ADDED_COMMAND = "Attribute added: ";
   private static final String ATTRIBUTE_REMOVED_COMMAND = "Attribute removed: ";
   private static final String ATTRIBUTE_MODIFIED_COMMAND = "Attribute modified: ";
   private static final String NODE_INSERTED_COMMAND = "Node inserted: ";
   private static final String NODE_REMOVED_COMMAND = "Node removed: ";
   private static final String CHAR_DATA_MODIFIED_COMMAND = "Node value changed: ";
   private static final String OUTER_EDIT_COMMAND = "Document changed outside DOM Viewer";
   private static final String COMPOUND_TREE_NODE_DROP = "Node moved";
   private static final String REMOVE_SELECTED_NODES = "Nodes removed";
   protected HistoryBrowser historyBrowser;
   protected AbstractCompoundCommand currentCompoundCommand;

   public HistoryBrowserInterface(HistoryBrowser.CommandController commandController) {
      this.historyBrowser = new HistoryBrowser(commandController);
   }

   public void setCommmandController(HistoryBrowser.CommandController newCommandController) {
      this.historyBrowser.setCommandController(newCommandController);
   }

   public CompoundUpdateCommand createCompoundUpdateCommand(String commandName) {
      CompoundUpdateCommand cmd = new CompoundUpdateCommand(commandName);
      return cmd;
   }

   public CompoundUpdateCommand createNodeChangedCommand(Node node) {
      return new CompoundUpdateCommand(this.getNodeChangedCommandName(node));
   }

   public CompoundUpdateCommand createNodesDroppedCommand(ArrayList nodes) {
      return new CompoundUpdateCommand("Node moved");
   }

   public CompoundUpdateCommand createRemoveSelectedTreeNodesCommand(ArrayList nodes) {
      return new CompoundUpdateCommand("Nodes removed");
   }

   public void performCompoundUpdateCommand(UndoableCommand command) {
      this.historyBrowser.addCommand(command);
   }

   public HistoryBrowser getHistoryBrowser() {
      return this.historyBrowser;
   }

   public void nodeInserted(Node newParent, Node newSibling, Node contextNode) {
      this.historyBrowser.addCommand(this.createNodeInsertedCommand(newParent, newSibling, contextNode));
   }

   public NodeInsertedCommand createNodeInsertedCommand(Node newParent, Node newSibling, Node contextNode) {
      return new NodeInsertedCommand("Node inserted: " + this.getBracketedNodeName(contextNode), newParent, newSibling, contextNode);
   }

   public void nodeRemoved(Node oldParent, Node oldSibling, Node contextNode) {
      this.historyBrowser.addCommand(this.createNodeRemovedCommand(oldParent, oldSibling, contextNode));
   }

   public NodeRemovedCommand createNodeRemovedCommand(Node oldParent, Node oldSibling, Node contextNode) {
      return new NodeRemovedCommand("Node removed: " + this.getBracketedNodeName(contextNode), oldParent, oldSibling, contextNode);
   }

   public void attributeAdded(Element contextElement, String attributeName, String newAttributeValue, String namespaceURI) {
      this.historyBrowser.addCommand(this.createAttributeAddedCommand(contextElement, attributeName, newAttributeValue, namespaceURI));
   }

   public AttributeAddedCommand createAttributeAddedCommand(Element contextElement, String attributeName, String newAttributeValue, String namespaceURI) {
      return new AttributeAddedCommand("Attribute added: " + this.getBracketedNodeName(contextElement), contextElement, attributeName, newAttributeValue, namespaceURI);
   }

   public void attributeRemoved(Element contextElement, String attributeName, String prevAttributeValue, String namespaceURI) {
      this.historyBrowser.addCommand(this.createAttributeRemovedCommand(contextElement, attributeName, prevAttributeValue, namespaceURI));
   }

   public AttributeRemovedCommand createAttributeRemovedCommand(Element contextElement, String attributeName, String prevAttributeValue, String namespaceURI) {
      return new AttributeRemovedCommand("Attribute removed: " + this.getBracketedNodeName(contextElement), contextElement, attributeName, prevAttributeValue, namespaceURI);
   }

   public void attributeModified(Element contextElement, String attributeName, String prevAttributeValue, String newAttributeValue, String namespaceURI) {
      this.historyBrowser.addCommand(this.createAttributeModifiedCommand(contextElement, attributeName, prevAttributeValue, newAttributeValue, namespaceURI));
   }

   public AttributeModifiedCommand createAttributeModifiedCommand(Element contextElement, String attributeName, String prevAttributeValue, String newAttributeValue, String namespaceURI) {
      return new AttributeModifiedCommand("Attribute modified: " + this.getBracketedNodeName(contextElement), contextElement, attributeName, prevAttributeValue, newAttributeValue, namespaceURI);
   }

   public void charDataModified(Node contextNode, String oldValue, String newValue) {
      this.historyBrowser.addCommand(this.createCharDataModifiedCommand(contextNode, oldValue, newValue));
   }

   public CharDataModifiedCommand createCharDataModifiedCommand(Node contextNode, String oldValue, String newValue) {
      return new CharDataModifiedCommand("Node value changed: " + this.getBracketedNodeName(contextNode), contextNode, oldValue, newValue);
   }

   public void appendChild(Node parent, Node child) {
      this.historyBrowser.addCommand(this.createAppendChildCommand(parent, child));
   }

   public AppendChildCommand createAppendChildCommand(Node parent, Node child) {
      return new AppendChildCommand(this.getAppendChildCommandName(parent, child), parent, child);
   }

   public void insertChildBefore(Node parent, Node sibling, Node child) {
      if (sibling == null) {
         this.historyBrowser.addCommand(this.createAppendChildCommand(parent, child));
      } else {
         this.historyBrowser.addCommand(this.createInsertNodeBeforeCommand(parent, sibling, child));
      }

   }

   public UndoableCommand createInsertChildCommand(Node parent, Node sibling, Node child) {
      return (UndoableCommand)(sibling == null ? this.createAppendChildCommand(parent, child) : this.createInsertNodeBeforeCommand(parent, sibling, child));
   }

   public InsertNodeBeforeCommand createInsertNodeBeforeCommand(Node parent, Node sibling, Node child) {
      return new InsertNodeBeforeCommand(this.getInsertBeforeCommandName(parent, child, sibling), parent, sibling, child);
   }

   public void replaceChild(Node parent, Node newChild, Node oldChild) {
   }

   public void removeChild(Node parent, Node child) {
      this.historyBrowser.addCommand(this.createRemoveChildCommand(parent, child));
   }

   public RemoveChildCommand createRemoveChildCommand(Node parent, Node child) {
      return new RemoveChildCommand(this.getRemoveChildCommandName(parent, child), parent, child);
   }

   public void setNodeValue(Node contextNode, String newValue) {
      this.historyBrowser.addCommand(this.createChangeNodeValueCommand(contextNode, newValue));
   }

   public ChangeNodeValueCommand createChangeNodeValueCommand(Node contextNode, String newValue) {
      return new ChangeNodeValueCommand(this.getChangeNodeValueCommandName(contextNode, newValue), contextNode, newValue);
   }

   public AbstractCompoundCommand getCurrentCompoundCommand() {
      if (this.currentCompoundCommand == null) {
         this.currentCompoundCommand = this.createCompoundUpdateCommand("Document changed outside DOM Viewer");
      }

      return this.currentCompoundCommand;
   }

   public void addToCurrentCompoundCommand(AbstractUndoableCommand cmd) {
      this.getCurrentCompoundCommand().addCommand(cmd);
      this.historyBrowser.fireDoCompoundEdit(new HistoryBrowser.HistoryBrowserEvent(this.getCurrentCompoundCommand()));
   }

   public void performCurrentCompoundCommand() {
      if (this.getCurrentCompoundCommand().getCommandNumber() > 0) {
         this.historyBrowser.addCommand(this.getCurrentCompoundCommand());
         this.historyBrowser.fireCompoundEditPerformed(new HistoryBrowser.HistoryBrowserEvent(this.currentCompoundCommand));
         this.currentCompoundCommand = null;
      }

   }

   private String getNodeAsString(Node node) {
      String id = "";
      if (node.getNodeType() == 1) {
         Element e = (Element)node;
         id = e.getAttributeNS((String)null, "id");
      }

      return id.length() != 0 ? node.getNodeName() + " \"" + id + "\"" : node.getNodeName();
   }

   private String getBracketedNodeName(Node node) {
      return "(" + this.getNodeAsString(node) + ")";
   }

   private String getAppendChildCommandName(Node parentNode, Node childNode) {
      return "Append " + this.getNodeAsString(childNode) + " to " + this.getNodeAsString(parentNode);
   }

   private String getInsertBeforeCommandName(Node parentNode, Node childNode, Node siblingNode) {
      return "Insert " + this.getNodeAsString(childNode) + " to " + this.getNodeAsString(parentNode) + " before " + this.getNodeAsString(siblingNode);
   }

   private String getRemoveChildCommandName(Node parent, Node child) {
      return "Remove " + this.getNodeAsString(child) + " from " + this.getNodeAsString(parent);
   }

   private String getChangeNodeValueCommandName(Node contextNode, String newValue) {
      return "Change " + this.getNodeAsString(contextNode) + " value to " + newValue;
   }

   private String getNodeChangedCommandName(Node node) {
      return "Node " + this.getNodeAsString(node) + " changed";
   }

   public static class ChangeNodeValueCommand extends AbstractUndoableCommand {
      protected Node contextNode;
      protected String newValue;

      public ChangeNodeValueCommand(String commandName, Node contextNode, String newValue) {
         this.setName(commandName);
         this.contextNode = contextNode;
         this.newValue = newValue;
      }

      public void execute() {
         String oldNodeValue = this.contextNode.getNodeValue();
         this.contextNode.setNodeValue(this.newValue);
         this.newValue = oldNodeValue;
      }

      public void undo() {
         this.execute();
      }

      public void redo() {
         this.execute();
      }

      public boolean shouldExecute() {
         return this.contextNode != null;
      }
   }

   public static class RemoveChildCommand extends AbstractUndoableCommand {
      protected Node parentNode;
      protected Node childNode;
      protected int indexInChildrenArray;

      public RemoveChildCommand(String commandName, Node parentNode, Node childNode) {
         this.setName(commandName);
         this.parentNode = parentNode;
         this.childNode = childNode;
      }

      public void execute() {
         this.indexInChildrenArray = DOMUtilities.getChildIndex(this.childNode, this.parentNode);
         this.parentNode.removeChild(this.childNode);
      }

      public void undo() {
         Node refChild = this.parentNode.getChildNodes().item(this.indexInChildrenArray);
         this.parentNode.insertBefore(this.childNode, refChild);
      }

      public void redo() {
         this.parentNode.removeChild(this.childNode);
      }

      public boolean shouldExecute() {
         return this.parentNode != null && this.childNode != null;
      }
   }

   public static class ReplaceChildCommand extends AbstractUndoableCommand {
      protected Node oldParent;
      protected Node oldNextSibling;
      protected Node newNextSibling;
      protected Node parent;
      protected Node child;

      public ReplaceChildCommand(String commandName, Node parent, Node sibling, Node child) {
         this.setName(commandName);
         this.oldParent = child.getParentNode();
         this.oldNextSibling = child.getNextSibling();
         this.parent = parent;
         this.child = child;
         this.newNextSibling = sibling;
      }

      public void execute() {
         if (this.newNextSibling != null) {
            this.parent.insertBefore(this.child, this.newNextSibling);
         } else {
            this.parent.appendChild(this.child);
         }

      }

      public void undo() {
         if (this.oldParent != null) {
            this.oldParent.insertBefore(this.child, this.oldNextSibling);
         } else {
            this.parent.removeChild(this.child);
         }

      }

      public void redo() {
         this.execute();
      }

      public boolean shouldExecute() {
         return this.parent != null && this.child != null;
      }
   }

   public static class InsertNodeBeforeCommand extends AbstractUndoableCommand {
      protected Node oldParent;
      protected Node oldNextSibling;
      protected Node newNextSibling;
      protected Node parent;
      protected Node child;

      public InsertNodeBeforeCommand(String commandName, Node parent, Node sibling, Node child) {
         this.setName(commandName);
         this.oldParent = child.getParentNode();
         this.oldNextSibling = child.getNextSibling();
         this.parent = parent;
         this.child = child;
         this.newNextSibling = sibling;
      }

      public void execute() {
         if (this.newNextSibling != null) {
            this.parent.insertBefore(this.child, this.newNextSibling);
         } else {
            this.parent.appendChild(this.child);
         }

      }

      public void undo() {
         if (this.oldParent != null) {
            this.oldParent.insertBefore(this.child, this.oldNextSibling);
         } else {
            this.parent.removeChild(this.child);
         }

      }

      public void redo() {
         this.execute();
      }

      public boolean shouldExecute() {
         return this.parent != null && this.child != null;
      }
   }

   public static class AppendChildCommand extends AbstractUndoableCommand {
      protected Node oldParentNode;
      protected Node oldNextSibling;
      protected Node parentNode;
      protected Node childNode;

      public AppendChildCommand(String commandName, Node parentNode, Node childNode) {
         this.setName(commandName);
         this.oldParentNode = childNode.getParentNode();
         this.oldNextSibling = childNode.getNextSibling();
         this.parentNode = parentNode;
         this.childNode = childNode;
      }

      public void execute() {
         this.parentNode.appendChild(this.childNode);
      }

      public void undo() {
         if (this.oldParentNode != null) {
            this.oldParentNode.insertBefore(this.childNode, this.oldNextSibling);
         } else {
            this.parentNode.removeChild(this.childNode);
         }

      }

      public void redo() {
         this.execute();
      }

      public boolean shouldExecute() {
         return this.parentNode != null && this.childNode != null;
      }
   }

   public static class CharDataModifiedCommand extends AbstractUndoableCommand {
      protected Node contextNode;
      protected String oldValue;
      protected String newValue;

      public CharDataModifiedCommand(String commandName, Node contextNode, String oldValue, String newValue) {
         this.setName(commandName);
         this.contextNode = contextNode;
         this.oldValue = oldValue;
         this.newValue = newValue;
      }

      public void execute() {
      }

      public void undo() {
         this.contextNode.setNodeValue(this.oldValue);
      }

      public void redo() {
         this.contextNode.setNodeValue(this.newValue);
      }

      public boolean shouldExecute() {
         return this.contextNode != null;
      }
   }

   public static class AttributeModifiedCommand extends AbstractUndoableCommand {
      protected Element contextElement;
      protected String attributeName;
      protected String prevAttributeValue;
      protected String newAttributeValue;
      protected String namespaceURI;

      public AttributeModifiedCommand(String commandName, Element contextElement, String attributeName, String prevAttributeValue, String newAttributeValue, String namespaceURI) {
         this.setName(commandName);
         this.contextElement = contextElement;
         this.attributeName = attributeName;
         this.prevAttributeValue = prevAttributeValue;
         this.newAttributeValue = newAttributeValue;
         this.namespaceURI = namespaceURI;
      }

      public void execute() {
      }

      public void undo() {
         this.contextElement.setAttributeNS(this.namespaceURI, this.attributeName, this.prevAttributeValue);
      }

      public void redo() {
         this.contextElement.setAttributeNS(this.namespaceURI, this.attributeName, this.newAttributeValue);
      }

      public boolean shouldExecute() {
         return this.contextElement != null && this.attributeName.length() != 0;
      }
   }

   public static class AttributeRemovedCommand extends AbstractUndoableCommand {
      protected Element contextElement;
      protected String attributeName;
      protected String prevValue;
      protected String namespaceURI;

      public AttributeRemovedCommand(String commandName, Element contextElement, String attributeName, String prevAttributeValue, String namespaceURI) {
         this.setName(commandName);
         this.contextElement = contextElement;
         this.attributeName = attributeName;
         this.prevValue = prevAttributeValue;
         this.namespaceURI = namespaceURI;
      }

      public void execute() {
      }

      public void undo() {
         this.contextElement.setAttributeNS(this.namespaceURI, this.attributeName, this.prevValue);
      }

      public void redo() {
         this.contextElement.removeAttributeNS(this.namespaceURI, this.attributeName);
      }

      public boolean shouldExecute() {
         return this.contextElement != null && this.attributeName.length() != 0;
      }
   }

   public static class AttributeAddedCommand extends AbstractUndoableCommand {
      protected Element contextElement;
      protected String attributeName;
      protected String newValue;
      protected String namespaceURI;

      public AttributeAddedCommand(String commandName, Element contextElement, String attributeName, String newAttributeValue, String namespaceURI) {
         this.setName(commandName);
         this.contextElement = contextElement;
         this.attributeName = attributeName;
         this.newValue = newAttributeValue;
         this.namespaceURI = namespaceURI;
      }

      public void execute() {
      }

      public void undo() {
         this.contextElement.removeAttributeNS(this.namespaceURI, this.attributeName);
      }

      public void redo() {
         this.contextElement.setAttributeNS(this.namespaceURI, this.attributeName, this.newValue);
      }

      public boolean shouldExecute() {
         return this.contextElement != null && this.attributeName.length() != 0;
      }
   }

   public static class NodeRemovedCommand extends AbstractUndoableCommand {
      protected Node oldSibling;
      protected Node oldParent;
      protected Node contextNode;

      public NodeRemovedCommand(String commandName, Node oldParent, Node oldSibling, Node contextNode) {
         this.setName(commandName);
         this.oldParent = oldParent;
         this.contextNode = contextNode;
         this.oldSibling = oldSibling;
      }

      public void execute() {
      }

      public void undo() {
         if (this.oldSibling != null) {
            this.oldParent.insertBefore(this.contextNode, this.oldSibling);
         } else {
            this.oldParent.appendChild(this.contextNode);
         }

      }

      public void redo() {
         this.oldParent.removeChild(this.contextNode);
      }

      public boolean shouldExecute() {
         return this.oldParent != null && this.contextNode != null;
      }
   }

   public static class NodeInsertedCommand extends AbstractUndoableCommand {
      protected Node newSibling;
      protected Node newParent;
      protected Node contextNode;

      public NodeInsertedCommand(String commandName, Node parent, Node sibling, Node contextNode) {
         this.setName(commandName);
         this.newParent = parent;
         this.contextNode = contextNode;
         this.newSibling = sibling;
      }

      public void execute() {
      }

      public void undo() {
         this.newParent.removeChild(this.contextNode);
      }

      public void redo() {
         if (this.newSibling != null) {
            this.newParent.insertBefore(this.contextNode, this.newSibling);
         } else {
            this.newParent.appendChild(this.contextNode);
         }

      }

      public boolean shouldExecute() {
         return this.newParent != null && this.contextNode != null;
      }
   }

   public static class CompoundUpdateCommand extends AbstractCompoundCommand {
      public CompoundUpdateCommand(String commandName) {
         this.setName(commandName);
      }
   }
}
