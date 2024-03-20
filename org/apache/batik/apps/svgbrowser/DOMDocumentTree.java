package org.apache.batik.apps.svgbrowser;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.Node;

public class DOMDocumentTree extends JTree implements Autoscroll {
   protected EventListenerList eventListeners = new EventListenerList();
   protected Insets autoscrollInsets = new Insets(20, 20, 20, 20);
   protected Insets scrollUnits = new Insets(25, 25, 25, 25);
   protected DOMDocumentTreeController controller;

   public DOMDocumentTree(TreeNode root, DOMDocumentTreeController controller) {
      super(root);
      this.controller = controller;
      new TreeDragSource(this, 3);
      new DropTarget(this, new TreeDropTargetListener(this));
   }

   public void autoscroll(Point point) {
      JViewport viewport = (JViewport)SwingUtilities.getAncestorOfClass(JViewport.class, this);
      if (viewport != null) {
         Point viewportPos = viewport.getViewPosition();
         int viewHeight = viewport.getExtentSize().height;
         int viewWidth = viewport.getExtentSize().width;
         if (point.y - viewportPos.y < this.autoscrollInsets.top) {
            viewport.setViewPosition(new Point(viewportPos.x, Math.max(viewportPos.y - this.scrollUnits.top, 0)));
            this.fireOnAutoscroll(new DOMDocumentTreeEvent(this));
         } else if (viewportPos.y + viewHeight - point.y < this.autoscrollInsets.bottom) {
            viewport.setViewPosition(new Point(viewportPos.x, Math.min(viewportPos.y + this.scrollUnits.bottom, this.getHeight() - viewHeight)));
            this.fireOnAutoscroll(new DOMDocumentTreeEvent(this));
         } else if (point.x - viewportPos.x < this.autoscrollInsets.left) {
            viewport.setViewPosition(new Point(Math.max(viewportPos.x - this.scrollUnits.left, 0), viewportPos.y));
            this.fireOnAutoscroll(new DOMDocumentTreeEvent(this));
         } else if (viewportPos.x + viewWidth - point.x < this.autoscrollInsets.right) {
            viewport.setViewPosition(new Point(Math.min(viewportPos.x + this.scrollUnits.right, this.getWidth() - viewWidth), viewportPos.y));
            this.fireOnAutoscroll(new DOMDocumentTreeEvent(this));
         }

      }
   }

   public Insets getAutoscrollInsets() {
      int topAndBottom = this.getHeight();
      int leftAndRight = this.getWidth();
      return new Insets(topAndBottom, leftAndRight, topAndBottom, leftAndRight);
   }

   public void addListener(DOMDocumentTreeListener listener) {
      this.eventListeners.add(DOMDocumentTreeListener.class, listener);
   }

   public void fireDropCompleted(DOMDocumentTreeEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == DOMDocumentTreeListener.class) {
            ((DOMDocumentTreeListener)listeners[i + 1]).dropCompleted(event);
         }
      }

   }

   public void fireOnAutoscroll(DOMDocumentTreeEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == DOMDocumentTreeListener.class) {
            ((DOMDocumentTreeListener)listeners[i + 1]).onAutoscroll(event);
         }
      }

   }

   protected Node getDomNodeFromTreeNode(DefaultMutableTreeNode treeNode) {
      if (treeNode == null) {
         return null;
      } else {
         return treeNode.getUserObject() instanceof DOMViewer.NodeInfo ? ((DOMViewer.NodeInfo)treeNode.getUserObject()).getNode() : null;
      }
   }

   protected ArrayList getNodeListForParent(ArrayList potentialChildren, Node parentNode) {
      ArrayList children = new ArrayList();
      int n = potentialChildren.size();
      Iterator var5 = potentialChildren.iterator();

      while(var5.hasNext()) {
         Object aPotentialChildren = var5.next();
         Node node = (Node)aPotentialChildren;
         if (DOMUtilities.canAppend(node, parentNode)) {
            children.add(node);
         }
      }

      return children;
   }

   public static class DropCompletedInfo {
      protected Node parent;
      protected ArrayList children;
      protected Node sibling;

      public DropCompletedInfo(Node parent, Node sibling, ArrayList children) {
         this.parent = parent;
         this.sibling = sibling;
         this.children = children;
      }

      public ArrayList getChildren() {
         return this.children;
      }

      public Node getParent() {
         return this.parent;
      }

      public Node getSibling() {
         return this.sibling;
      }
   }

   public static class DOMDocumentTreeAdapter implements DOMDocumentTreeListener {
      public void dropCompleted(DOMDocumentTreeEvent event) {
      }

      public void onAutoscroll(DOMDocumentTreeEvent event) {
      }
   }

   public interface DOMDocumentTreeListener extends EventListener {
      void dropCompleted(DOMDocumentTreeEvent var1);

      void onAutoscroll(DOMDocumentTreeEvent var1);
   }

   public static class DOMDocumentTreeEvent extends EventObject {
      public DOMDocumentTreeEvent(Object source) {
         super(source);
      }
   }

   public static class TransferData {
      protected ArrayList nodeList;

      public TransferData(ArrayList nodeList) {
         this.nodeList = nodeList;
      }

      public ArrayList getNodeList() {
         return this.nodeList;
      }

      public String getNodesAsXML() {
         String toReturn = "";

         Node node;
         for(Iterator var2 = this.nodeList.iterator(); var2.hasNext(); toReturn = toReturn + DOMUtilities.getXML(node)) {
            Object aNodeList = var2.next();
            node = (Node)aNodeList;
         }

         return toReturn;
      }
   }

   public static class TransferableTreeNode implements Transferable {
      protected static final DataFlavor NODE_FLAVOR = new DataFlavor(TransferData.class, "TransferData");
      protected static final DataFlavor[] FLAVORS;
      protected TransferData data;

      public TransferableTreeNode(TransferData data) {
         this.data = data;
      }

      public synchronized DataFlavor[] getTransferDataFlavors() {
         return FLAVORS;
      }

      public boolean isDataFlavorSupported(DataFlavor flavor) {
         DataFlavor[] var2 = FLAVORS;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            DataFlavor FLAVOR = var2[var4];
            if (flavor.equals(FLAVOR)) {
               return true;
            }
         }

         return false;
      }

      public synchronized Object getTransferData(DataFlavor flavor) {
         if (!this.isDataFlavorSupported(flavor)) {
            return null;
         } else if (flavor.equals(NODE_FLAVOR)) {
            return this.data;
         } else {
            return flavor.equals(DataFlavor.stringFlavor) ? this.data.getNodesAsXML() : null;
         }
      }

      static {
         FLAVORS = new DataFlavor[]{NODE_FLAVOR, DataFlavor.stringFlavor};
      }
   }

   public class TreeDropTargetListener implements DropTargetListener {
      private static final int BEFORE = 1;
      private static final int AFTER = 2;
      private static final int CURRENT = 3;
      private TransferData transferData;
      private Component originalGlassPane;
      private int visualTipOffset = 5;
      private int visualTipThickness = 2;
      private int positionIndicator;
      private Point startPoint;
      private Point endPoint;
      protected JPanel visualTipGlassPane = new JPanel() {
         public void paint(Graphics g) {
            g.setColor(UIManager.getColor("Tree.selectionBackground"));
            if (TreeDropTargetListener.this.startPoint != null && TreeDropTargetListener.this.endPoint != null) {
               int x1 = TreeDropTargetListener.this.startPoint.x;
               int x2 = TreeDropTargetListener.this.endPoint.x;
               int y1 = TreeDropTargetListener.this.startPoint.y;
               int start = -TreeDropTargetListener.this.visualTipThickness / 2;
               start += TreeDropTargetListener.this.visualTipThickness % 2 == 0 ? 1 : 0;

               for(int i = start; i <= TreeDropTargetListener.this.visualTipThickness / 2; ++i) {
                  g.drawLine(x1 + 2, y1 + i, x2 - 2, y1 + i);
               }

            }
         }
      };
      private Timer expandControlTimer;
      private int expandTimeout = 1500;
      private TreePath dragOverTreePath;
      private TreePath treePathToExpand;

      public TreeDropTargetListener(DOMDocumentTree tree) {
         this.addOnAutoscrollListener(tree);
      }

      public void dragEnter(DropTargetDragEvent dtde) {
         JTree tree = (JTree)dtde.getDropTargetContext().getComponent();
         JRootPane rootPane = tree.getRootPane();
         this.originalGlassPane = rootPane.getGlassPane();
         rootPane.setGlassPane(this.visualTipGlassPane);
         this.visualTipGlassPane.setOpaque(false);
         this.visualTipGlassPane.setVisible(true);
         this.updateVisualTipLine(tree, (TreePath)null);

         try {
            Transferable transferable = (new DropTargetDropEvent(dtde.getDropTargetContext(), dtde.getLocation(), 0, 0)).getTransferable();
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            DataFlavor[] var6 = flavors;
            int var7 = flavors.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               DataFlavor flavor = var6[var8];
               if (transferable.isDataFlavorSupported(flavor)) {
                  this.transferData = (TransferData)transferable.getTransferData(flavor);
                  return;
               }
            }
         } catch (UnsupportedFlavorException var10) {
            var10.printStackTrace();
         } catch (IOException var11) {
            var11.printStackTrace();
         }

      }

      public void dragOver(DropTargetDragEvent dtde) {
         JTree tree = (JTree)dtde.getDropTargetContext().getComponent();
         TreeNode targetTreeNode = this.getNode(dtde);
         if (targetTreeNode != null) {
            this.updatePositionIndicator(dtde);
            Point p = dtde.getLocation();
            TreePath currentPath = tree.getPathForLocation(p.x, p.y);
            TreePath parentPath = this.getParentPathForPosition(currentPath);
            TreeNode parentNode = this.getNodeForPath(parentPath);
            TreePath nextSiblingPath = this.getSiblingPathForPosition(currentPath);
            TreeNode nextSiblingNode = this.getNodeForPath(nextSiblingPath);
            Node potentialParent = DOMDocumentTree.this.getDomNodeFromTreeNode((DefaultMutableTreeNode)parentNode);
            Node potentialSibling = DOMDocumentTree.this.getDomNodeFromTreeNode((DefaultMutableTreeNode)nextSiblingNode);
            if (DOMUtilities.canAppendAny(this.transferData.getNodeList(), potentialParent) && !this.transferData.getNodeList().contains(potentialSibling)) {
               dtde.acceptDrag(dtde.getDropAction());
               this.updateVisualTipLine(tree, currentPath);
               this.dragOverTreePath = currentPath;
               if (!tree.isExpanded(currentPath)) {
                  this.scheduleExpand(currentPath, tree);
               }
            } else {
               dtde.rejectDrag();
            }
         } else {
            dtde.rejectDrag();
         }

      }

      public void dropActionChanged(DropTargetDragEvent dtde) {
      }

      public void drop(DropTargetDropEvent dtde) {
         Point p = dtde.getLocation();
         DropTargetContext dtc = dtde.getDropTargetContext();
         JTree tree = (JTree)dtc.getComponent();
         this.setOriginalGlassPane(tree);
         this.dragOverTreePath = null;
         TreePath currentPath = tree.getPathForLocation(p.x, p.y);
         DefaultMutableTreeNode parent = (DefaultMutableTreeNode)this.getNodeForPath(this.getParentPathForPosition(currentPath));
         Node dropTargetNode = DOMDocumentTree.this.getDomNodeFromTreeNode(parent);
         DefaultMutableTreeNode sibling = (DefaultMutableTreeNode)this.getNodeForPath(this.getSiblingPathForPosition(currentPath));
         Node siblingNode = DOMDocumentTree.this.getDomNodeFromTreeNode(sibling);
         if (this.transferData != null) {
            ArrayList nodelist = DOMDocumentTree.this.getNodeListForParent(this.transferData.getNodeList(), dropTargetNode);
            DOMDocumentTree.this.fireDropCompleted(new DOMDocumentTreeEvent(new DropCompletedInfo(dropTargetNode, siblingNode, nodelist)));
            dtde.dropComplete(true);
         } else {
            dtde.rejectDrop();
         }
      }

      public void dragExit(DropTargetEvent dte) {
         this.setOriginalGlassPane((JTree)dte.getDropTargetContext().getComponent());
         this.dragOverTreePath = null;
      }

      private void updatePositionIndicator(DropTargetDragEvent dtde) {
         Point p = dtde.getLocation();
         DropTargetContext dtc = dtde.getDropTargetContext();
         JTree tree = (JTree)dtc.getComponent();
         TreePath currentPath = tree.getPathForLocation(p.x, p.y);
         Rectangle bounds = tree.getPathBounds(currentPath);
         if (p.y <= bounds.y + this.visualTipOffset) {
            this.positionIndicator = 1;
         } else if (p.y >= bounds.y + bounds.height - this.visualTipOffset) {
            this.positionIndicator = 2;
         } else {
            this.positionIndicator = 3;
         }

      }

      private TreePath getParentPathForPosition(TreePath currentPath) {
         if (currentPath == null) {
            return null;
         } else {
            TreePath parentPath = null;
            if (this.positionIndicator == 2) {
               parentPath = currentPath.getParentPath();
            } else if (this.positionIndicator == 1) {
               parentPath = currentPath.getParentPath();
            } else if (this.positionIndicator == 3) {
               parentPath = currentPath;
            }

            return parentPath;
         }
      }

      private TreePath getSiblingPathForPosition(TreePath currentPath) {
         TreePath parentPath = this.getParentPathForPosition(currentPath);
         TreePath nextSiblingPath = null;
         if (this.positionIndicator == 2) {
            TreeNode parentNode = this.getNodeForPath(parentPath);
            TreeNode currentNode = this.getNodeForPath(currentPath);
            if (parentPath != null && parentNode != null && currentNode != null) {
               int siblingIndex = parentNode.getIndex(currentNode) + 1;
               if (parentNode.getChildCount() > siblingIndex) {
                  nextSiblingPath = parentPath.pathByAddingChild(parentNode.getChildAt(siblingIndex));
               }
            }
         } else if (this.positionIndicator == 1) {
            nextSiblingPath = currentPath;
         } else if (this.positionIndicator == 3) {
            nextSiblingPath = null;
         }

         return nextSiblingPath;
      }

      private TreeNode getNodeForPath(TreePath path) {
         return path != null && path.getLastPathComponent() != null ? (TreeNode)path.getLastPathComponent() : null;
      }

      private TreeNode getNode(DropTargetDragEvent dtde) {
         Point p = dtde.getLocation();
         DropTargetContext dtc = dtde.getDropTargetContext();
         JTree tree = (JTree)dtc.getComponent();
         TreePath path = tree.getPathForLocation(p.x, p.y);
         return path != null && path.getLastPathComponent() != null ? (TreeNode)path.getLastPathComponent() : null;
      }

      private void updateVisualTipLine(JTree tree, TreePath path) {
         if (path == null) {
            this.startPoint = null;
            this.endPoint = null;
         } else {
            Rectangle bounds = tree.getPathBounds(path);
            if (this.positionIndicator == 1) {
               this.startPoint = bounds.getLocation();
               this.endPoint = new Point(this.startPoint.x + bounds.width, this.startPoint.y);
            } else if (this.positionIndicator == 2) {
               this.startPoint = new Point(bounds.x, bounds.y + bounds.height);
               this.endPoint = new Point(this.startPoint.x + bounds.width, this.startPoint.y);
               this.positionIndicator = 2;
            } else if (this.positionIndicator == 3) {
               this.startPoint = null;
               this.endPoint = null;
            }

            if (this.startPoint != null && this.endPoint != null) {
               this.startPoint = SwingUtilities.convertPoint(tree, this.startPoint, this.visualTipGlassPane);
               this.endPoint = SwingUtilities.convertPoint(tree, this.endPoint, this.visualTipGlassPane);
            }
         }

         this.visualTipGlassPane.getRootPane().repaint();
      }

      private void addOnAutoscrollListener(DOMDocumentTree tree) {
         tree.addListener(new DOMDocumentTreeAdapter() {
            public void onAutoscroll(DOMDocumentTreeEvent event) {
               TreeDropTargetListener.this.startPoint = null;
               TreeDropTargetListener.this.endPoint = null;
            }
         });
      }

      private void setOriginalGlassPane(JTree tree) {
         JRootPane rootPane = tree.getRootPane();
         rootPane.setGlassPane(this.originalGlassPane);
         this.originalGlassPane.setVisible(false);
         rootPane.repaint();
      }

      private void scheduleExpand(TreePath treePath, JTree tree) {
         if (treePath != this.treePathToExpand) {
            this.getExpandTreeTimer(tree).stop();
            this.treePathToExpand = treePath;
            this.getExpandTreeTimer(tree).start();
         }

      }

      private Timer getExpandTreeTimer(final JTree tree) {
         if (this.expandControlTimer == null) {
            this.expandControlTimer = new Timer(this.expandTimeout, new ActionListener() {
               public void actionPerformed(ActionEvent arg0) {
                  if (TreeDropTargetListener.this.treePathToExpand != null && TreeDropTargetListener.this.treePathToExpand == TreeDropTargetListener.this.dragOverTreePath) {
                     tree.expandPath(TreeDropTargetListener.this.treePathToExpand);
                  }

                  TreeDropTargetListener.this.getExpandTreeTimer(tree).stop();
               }
            });
         }

         return this.expandControlTimer;
      }
   }

   public class TreeDragSource implements DragSourceListener, DragGestureListener {
      protected DragSource source;
      protected DragGestureRecognizer recognizer;
      protected TransferableTreeNode transferable;
      protected DOMDocumentTree sourceTree;

      public TreeDragSource(DOMDocumentTree tree, int actions) {
         this.sourceTree = tree;
         this.source = new DragSource();
         this.recognizer = this.source.createDefaultDragGestureRecognizer(this.sourceTree, actions, this);
      }

      public void dragGestureRecognized(DragGestureEvent dge) {
         if (DOMDocumentTree.this.controller.isDNDSupported()) {
            TreePath[] paths = this.sourceTree.getSelectionPaths();
            if (paths != null) {
               ArrayList nodeList = new ArrayList();
               TreePath[] var4 = paths;
               int var5 = paths.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  TreePath path = var4[var6];
                  if (path.getPathCount() > 1) {
                     DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                     Node associatedNode = DOMDocumentTree.this.getDomNodeFromTreeNode(node);
                     if (associatedNode != null) {
                        nodeList.add(associatedNode);
                     }
                  }
               }

               if (!nodeList.isEmpty()) {
                  this.transferable = new TransferableTreeNode(new TransferData(nodeList));
                  this.source.startDrag(dge, (Cursor)null, this.transferable, this);
               }
            }
         }
      }

      public void dragEnter(DragSourceDragEvent dsde) {
      }

      public void dragExit(DragSourceEvent dse) {
      }

      public void dragOver(DragSourceDragEvent dsde) {
      }

      public void dropActionChanged(DragSourceDragEvent dsde) {
      }

      public void dragDropEnd(DragSourceDropEvent dsde) {
      }
   }
}
