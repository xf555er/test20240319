package org.apache.batik.swing.gvt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import org.apache.batik.bridge.ConcreteTextSelector;
import org.apache.batik.bridge.Mark;
import org.apache.batik.gvt.Selectable;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.event.GraphicsNodeMouseEvent;
import org.apache.batik.gvt.event.GraphicsNodeMouseListener;
import org.apache.batik.gvt.event.SelectionEvent;
import org.apache.batik.gvt.event.SelectionListener;

public class TextSelectionManager {
   public static final Cursor TEXT_CURSOR = new Cursor(2);
   protected ConcreteTextSelector textSelector;
   protected JGVTComponent component;
   protected Overlay selectionOverlay = new SelectionOverlay();
   protected MouseListener mouseListener;
   protected Cursor previousCursor;
   protected Shape selectionHighlight;
   protected SelectionListener textSelectionListener;
   protected Color selectionOverlayColor = new Color(100, 100, 255, 100);
   protected Color selectionOverlayStrokeColor;
   protected boolean xorMode;
   Object selection;

   public TextSelectionManager(JGVTComponent comp, EventDispatcher ed) {
      this.selectionOverlayStrokeColor = Color.white;
      this.xorMode = false;
      this.selection = null;
      this.textSelector = new ConcreteTextSelector();
      this.textSelectionListener = new TextSelectionListener();
      this.textSelector.addSelectionListener(this.textSelectionListener);
      this.mouseListener = new MouseListener();
      this.component = comp;
      this.component.getOverlays().add(this.selectionOverlay);
      ed.addGraphicsNodeMouseListener(this.mouseListener);
   }

   public void addSelectionListener(SelectionListener sl) {
      this.textSelector.addSelectionListener(sl);
   }

   public void removeSelectionListener(SelectionListener sl) {
      this.textSelector.removeSelectionListener(sl);
   }

   public void setSelectionOverlayColor(Color color) {
      this.selectionOverlayColor = color;
   }

   public Color getSelectionOverlayColor() {
      return this.selectionOverlayColor;
   }

   public void setSelectionOverlayStrokeColor(Color color) {
      this.selectionOverlayStrokeColor = color;
   }

   public Color getSelectionOverlayStrokeColor() {
      return this.selectionOverlayStrokeColor;
   }

   public void setSelectionOverlayXORMode(boolean state) {
      this.xorMode = state;
   }

   public boolean isSelectionOverlayXORMode() {
      return this.xorMode;
   }

   public Overlay getSelectionOverlay() {
      return this.selectionOverlay;
   }

   public Object getSelection() {
      return this.selection;
   }

   public void setSelection(Mark start, Mark end) {
      this.textSelector.setSelection(start, end);
   }

   public void clearSelection() {
      this.textSelector.clearSelection();
   }

   protected Rectangle outset(Rectangle r, int amount) {
      r.x -= amount;
      r.y -= amount;
      r.width += 2 * amount;
      r.height += 2 * amount;
      return r;
   }

   protected Rectangle getHighlightBounds() {
      AffineTransform at = this.component.getRenderingTransform();
      Shape s = at.createTransformedShape(this.selectionHighlight);
      return this.outset(s.getBounds(), 1);
   }

   protected class SelectionOverlay implements Overlay {
      public void paint(Graphics g) {
         if (TextSelectionManager.this.selectionHighlight != null) {
            AffineTransform at = TextSelectionManager.this.component.getRenderingTransform();
            Shape s = at.createTransformedShape(TextSelectionManager.this.selectionHighlight);
            Graphics2D g2d = (Graphics2D)g;
            if (TextSelectionManager.this.xorMode) {
               g2d.setColor(Color.black);
               g2d.setXORMode(Color.white);
               g2d.fill(s);
            } else {
               g2d.setColor(TextSelectionManager.this.selectionOverlayColor);
               g2d.fill(s);
               if (TextSelectionManager.this.selectionOverlayStrokeColor != null) {
                  g2d.setStroke(new BasicStroke(1.0F));
                  g2d.setColor(TextSelectionManager.this.selectionOverlayStrokeColor);
                  g2d.draw(s);
               }
            }
         }

      }
   }

   protected class TextSelectionListener implements SelectionListener {
      public void selectionDone(SelectionEvent e) {
         this.selectionChanged(e);
         TextSelectionManager.this.selection = e.getSelection();
      }

      public void selectionCleared(SelectionEvent e) {
         this.selectionStarted(e);
      }

      public void selectionStarted(SelectionEvent e) {
         if (TextSelectionManager.this.selectionHighlight != null) {
            Rectangle r = TextSelectionManager.this.getHighlightBounds();
            TextSelectionManager.this.selectionHighlight = null;
            TextSelectionManager.this.component.repaint(r);
         }

         TextSelectionManager.this.selection = null;
      }

      public void selectionChanged(SelectionEvent e) {
         Rectangle r = null;
         AffineTransform at = TextSelectionManager.this.component.getRenderingTransform();
         if (TextSelectionManager.this.selectionHighlight != null) {
            r = at.createTransformedShape(TextSelectionManager.this.selectionHighlight).getBounds();
            TextSelectionManager.this.outset(r, 1);
         }

         TextSelectionManager.this.selectionHighlight = e.getHighlightShape();
         if (TextSelectionManager.this.selectionHighlight != null) {
            if (r != null) {
               Rectangle r2 = TextSelectionManager.this.getHighlightBounds();
               r2.add(r);
               TextSelectionManager.this.component.repaint(r2);
            } else {
               TextSelectionManager.this.component.repaint(TextSelectionManager.this.getHighlightBounds());
            }
         } else if (r != null) {
            TextSelectionManager.this.component.repaint(r);
         }

      }
   }

   protected class MouseListener implements GraphicsNodeMouseListener {
      public void mouseClicked(GraphicsNodeMouseEvent evt) {
         if (evt.getSource() instanceof Selectable) {
            TextSelectionManager.this.textSelector.mouseClicked(evt);
         }

      }

      public void mousePressed(GraphicsNodeMouseEvent evt) {
         if (evt.getSource() instanceof Selectable) {
            TextSelectionManager.this.textSelector.mousePressed(evt);
         } else if (TextSelectionManager.this.selectionHighlight != null) {
            TextSelectionManager.this.textSelector.clearSelection();
         }

      }

      public void mouseReleased(GraphicsNodeMouseEvent evt) {
         TextSelectionManager.this.textSelector.mouseReleased(evt);
      }

      public void mouseEntered(GraphicsNodeMouseEvent evt) {
         if (evt.getSource() instanceof Selectable) {
            TextSelectionManager.this.textSelector.mouseEntered(evt);
            TextSelectionManager.this.previousCursor = TextSelectionManager.this.component.getCursor();
            if (TextSelectionManager.this.previousCursor.getType() == 0) {
               TextSelectionManager.this.component.setCursor(TextSelectionManager.TEXT_CURSOR);
            }
         }

      }

      public void mouseExited(GraphicsNodeMouseEvent evt) {
         if (evt.getSource() instanceof Selectable) {
            TextSelectionManager.this.textSelector.mouseExited(evt);
            if (TextSelectionManager.this.component.getCursor() == TextSelectionManager.TEXT_CURSOR) {
               TextSelectionManager.this.component.setCursor(TextSelectionManager.this.previousCursor);
            }
         }

      }

      public void mouseDragged(GraphicsNodeMouseEvent evt) {
         if (evt.getSource() instanceof Selectable) {
            TextSelectionManager.this.textSelector.mouseDragged(evt);
         }

      }

      public void mouseMoved(GraphicsNodeMouseEvent evt) {
      }
   }
}
