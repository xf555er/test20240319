package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ImageNode extends CompositeGraphicsNode {
   protected boolean hitCheckChildren = false;

   public void setVisible(boolean isVisible) {
      this.fireGraphicsNodeChangeStarted();
      this.isVisible = isVisible;
      this.invalidateGeometryCache();
      this.fireGraphicsNodeChangeCompleted();
   }

   public Rectangle2D getPrimitiveBounds() {
      return !this.isVisible ? null : super.getPrimitiveBounds();
   }

   public void setHitCheckChildren(boolean hitCheckChildren) {
      this.hitCheckChildren = hitCheckChildren;
   }

   public boolean getHitCheckChildren() {
      return this.hitCheckChildren;
   }

   public void paint(Graphics2D g2d) {
      if (this.isVisible) {
         super.paint(g2d);
      }

   }

   public boolean contains(Point2D p) {
      switch (this.pointerEventType) {
         case 0:
         case 1:
         case 2:
         case 3:
            return this.isVisible && super.contains(p);
         case 4:
         case 5:
         case 6:
         case 7:
            return super.contains(p);
         case 8:
            return false;
         default:
            return false;
      }
   }

   public GraphicsNode nodeHitAt(Point2D p) {
      if (this.hitCheckChildren) {
         return super.nodeHitAt(p);
      } else {
         return this.contains(p) ? this : null;
      }
   }

   public void setImage(GraphicsNode newImage) {
      this.fireGraphicsNodeChangeStarted();
      this.invalidateGeometryCache();
      if (this.count == 0) {
         this.ensureCapacity(1);
      }

      this.children[0] = newImage;
      ((AbstractGraphicsNode)newImage).setParent(this);
      ((AbstractGraphicsNode)newImage).setRoot(this.getRoot());
      this.count = 1;
      this.fireGraphicsNodeChangeCompleted();
   }

   public GraphicsNode getImage() {
      return this.count > 0 ? this.children[0] : null;
   }
}
