package org.apache.batik.apps.svgbrowser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.Overlay;
import org.w3c.dom.Element;

public class ElementOverlayManager {
   protected Color elementOverlayStrokeColor;
   protected Color elementOverlayColor;
   protected boolean xorMode;
   protected JSVGCanvas canvas;
   protected Overlay elementOverlay;
   protected ArrayList elements;
   protected ElementOverlayController controller;
   protected boolean isOverlayEnabled;

   public ElementOverlayManager(JSVGCanvas canvas) {
      this.elementOverlayStrokeColor = Color.black;
      this.elementOverlayColor = Color.white;
      this.xorMode = true;
      this.elementOverlay = new ElementOverlay();
      this.isOverlayEnabled = true;
      this.canvas = canvas;
      this.elements = new ArrayList();
      canvas.getOverlays().add(this.elementOverlay);
   }

   public void addElement(Element elem) {
      this.elements.add(elem);
   }

   public void removeElement(Element elem) {
      if (this.elements.remove(elem)) {
      }

   }

   public void removeElements() {
      this.elements.clear();
      this.repaint();
   }

   protected Rectangle getAllElementsBounds() {
      Rectangle resultBound = null;
      int n = this.elements.size();
      Iterator var3 = this.elements.iterator();

      while(var3.hasNext()) {
         Object element = var3.next();
         Element currentElement = (Element)element;
         Rectangle currentBound = this.getElementBounds(currentElement);
         if (resultBound == null) {
            resultBound = currentBound;
         } else {
            resultBound.add(currentBound);
         }
      }

      return resultBound;
   }

   protected Rectangle getElementBounds(Element elem) {
      return this.getElementBounds(this.canvas.getUpdateManager().getBridgeContext().getGraphicsNode(elem));
   }

   protected Rectangle getElementBounds(GraphicsNode node) {
      if (node == null) {
         return null;
      } else {
         AffineTransform at = this.canvas.getRenderingTransform();
         Shape s = at.createTransformedShape(node.getOutline());
         return this.outset(s.getBounds(), 1);
      }
   }

   protected Rectangle outset(Rectangle r, int amount) {
      r.x -= amount;
      r.y -= amount;
      r.width += 2 * amount;
      r.height += 2 * amount;
      return r;
   }

   public void repaint() {
      this.canvas.repaint();
   }

   public Color getElementOverlayColor() {
      return this.elementOverlayColor;
   }

   public void setElementOverlayColor(Color selectionOverlayColor) {
      this.elementOverlayColor = selectionOverlayColor;
   }

   public Color getElementOverlayStrokeColor() {
      return this.elementOverlayStrokeColor;
   }

   public void setElementOverlayStrokeColor(Color selectionOverlayStrokeColor) {
      this.elementOverlayStrokeColor = selectionOverlayStrokeColor;
   }

   public boolean isXorMode() {
      return this.xorMode;
   }

   public void setXorMode(boolean xorMode) {
      this.xorMode = xorMode;
   }

   public Overlay getElementOverlay() {
      return this.elementOverlay;
   }

   public void removeOverlay() {
      this.canvas.getOverlays().remove(this.elementOverlay);
   }

   public void setController(ElementOverlayController controller) {
      this.controller = controller;
   }

   public boolean isOverlayEnabled() {
      return this.isOverlayEnabled;
   }

   public void setOverlayEnabled(boolean isOverlayEnabled) {
      this.isOverlayEnabled = isOverlayEnabled;
   }

   public class ElementOverlay implements Overlay {
      public void paint(Graphics g) {
         if (ElementOverlayManager.this.controller.isOverlayEnabled() && ElementOverlayManager.this.isOverlayEnabled()) {
            int n = ElementOverlayManager.this.elements.size();
            Iterator var3 = ElementOverlayManager.this.elements.iterator();

            while(var3.hasNext()) {
               Object element = var3.next();
               Element currentElement = (Element)element;
               GraphicsNode nodeToPaint = ElementOverlayManager.this.canvas.getUpdateManager().getBridgeContext().getGraphicsNode(currentElement);
               if (nodeToPaint != null) {
                  AffineTransform elementsAt = nodeToPaint.getGlobalTransform();
                  Shape selectionHighlight = nodeToPaint.getOutline();
                  AffineTransform at = ElementOverlayManager.this.canvas.getRenderingTransform();
                  at.concatenate(elementsAt);
                  Shape s = at.createTransformedShape(selectionHighlight);
                  if (s == null) {
                     break;
                  }

                  Graphics2D g2d = (Graphics2D)g;
                  if (ElementOverlayManager.this.xorMode) {
                     g2d.setColor(Color.black);
                     g2d.setXORMode(Color.yellow);
                     g2d.fill(s);
                     g2d.draw(s);
                  } else {
                     g2d.setColor(ElementOverlayManager.this.elementOverlayColor);
                     g2d.setStroke(new BasicStroke(1.8F));
                     g2d.setColor(ElementOverlayManager.this.elementOverlayStrokeColor);
                     g2d.draw(s);
                  }
               }
            }
         }

      }
   }
}
