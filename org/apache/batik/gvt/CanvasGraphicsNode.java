package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class CanvasGraphicsNode extends CompositeGraphicsNode {
   protected AffineTransform positionTransform;
   protected AffineTransform viewingTransform;
   protected Paint backgroundPaint;

   public void setBackgroundPaint(Paint newBackgroundPaint) {
      this.backgroundPaint = newBackgroundPaint;
   }

   public Paint getBackgroundPaint() {
      return this.backgroundPaint;
   }

   public void setPositionTransform(AffineTransform at) {
      this.fireGraphicsNodeChangeStarted();
      this.invalidateGeometryCache();
      this.positionTransform = at;
      if (this.positionTransform != null) {
         this.transform = new AffineTransform(this.positionTransform);
         if (this.viewingTransform != null) {
            this.transform.concatenate(this.viewingTransform);
         }
      } else if (this.viewingTransform != null) {
         this.transform = new AffineTransform(this.viewingTransform);
      } else {
         this.transform = new AffineTransform();
      }

      if (this.transform.getDeterminant() != 0.0) {
         try {
            this.inverseTransform = this.transform.createInverse();
         } catch (NoninvertibleTransformException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      } else {
         this.inverseTransform = this.transform;
      }

      this.fireGraphicsNodeChangeCompleted();
   }

   public AffineTransform getPositionTransform() {
      return this.positionTransform;
   }

   public void setViewingTransform(AffineTransform at) {
      this.fireGraphicsNodeChangeStarted();
      this.invalidateGeometryCache();
      this.viewingTransform = at;
      if (this.positionTransform != null) {
         this.transform = new AffineTransform(this.positionTransform);
         if (this.viewingTransform != null) {
            this.transform.concatenate(this.viewingTransform);
         }
      } else if (this.viewingTransform != null) {
         this.transform = new AffineTransform(this.viewingTransform);
      } else {
         this.transform = new AffineTransform();
      }

      if (this.transform.getDeterminant() != 0.0) {
         try {
            this.inverseTransform = this.transform.createInverse();
         } catch (NoninvertibleTransformException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      } else {
         this.inverseTransform = this.transform;
      }

      this.fireGraphicsNodeChangeCompleted();
   }

   public AffineTransform getViewingTransform() {
      return this.viewingTransform;
   }

   public void primitivePaint(Graphics2D g2d) {
      if (this.backgroundPaint != null) {
         g2d.setPaint(this.backgroundPaint);
         g2d.fill(g2d.getClip());
      }

      super.primitivePaint(g2d);
   }
}
