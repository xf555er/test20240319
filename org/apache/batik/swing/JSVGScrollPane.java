package org.apache.batik.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.gvt.JGVTComponentListener;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

public class JSVGScrollPane extends JPanel {
   protected JSVGCanvas canvas;
   protected JPanel horizontalPanel;
   protected JScrollBar vertical;
   protected JScrollBar horizontal;
   protected Component cornerBox;
   protected boolean scrollbarsAlwaysVisible = false;
   protected SBListener hsbListener;
   protected SBListener vsbListener;
   protected Rectangle2D viewBox = null;
   protected boolean ignoreScrollChange = false;

   public JSVGScrollPane(JSVGCanvas canvas) {
      this.canvas = canvas;
      canvas.setRecenterOnResize(false);
      this.vertical = new JScrollBar(1, 0, 0, 0, 0);
      this.horizontal = new JScrollBar(0, 0, 0, 0, 0);
      this.horizontalPanel = new JPanel(new BorderLayout());
      this.horizontalPanel.add(this.horizontal, "Center");
      this.cornerBox = Box.createRigidArea(new Dimension(this.vertical.getPreferredSize().width, this.horizontal.getPreferredSize().height));
      this.horizontalPanel.add(this.cornerBox, "East");
      this.hsbListener = this.createScrollBarListener(false);
      this.horizontal.getModel().addChangeListener(this.hsbListener);
      this.vsbListener = this.createScrollBarListener(true);
      this.vertical.getModel().addChangeListener(this.vsbListener);
      this.updateScrollbarState(false, false);
      this.setLayout(new BorderLayout());
      this.add(canvas, "Center");
      this.add(this.vertical, "East");
      this.add(this.horizontalPanel, "South");
      canvas.addSVGDocumentLoaderListener(this.createLoadListener());
      ScrollListener xlistener = this.createScrollListener();
      this.addComponentListener(xlistener);
      canvas.addGVTTreeRendererListener(xlistener);
      canvas.addJGVTComponentListener(xlistener);
      canvas.addGVTTreeBuilderListener(xlistener);
      canvas.addUpdateManagerListener(xlistener);
   }

   public boolean getScrollbarsAlwaysVisible() {
      return this.scrollbarsAlwaysVisible;
   }

   public void setScrollbarsAlwaysVisible(boolean vis) {
      this.scrollbarsAlwaysVisible = vis;
      this.resizeScrollBars();
   }

   protected SBListener createScrollBarListener(boolean isVertical) {
      return new SBListener(isVertical);
   }

   protected ScrollListener createScrollListener() {
      return new ScrollListener();
   }

   protected SVGDocumentLoaderListener createLoadListener() {
      return new SVGScrollDocumentLoaderListener();
   }

   public JSVGCanvas getCanvas() {
      return this.canvas;
   }

   public void reset() {
      this.viewBox = null;
      this.updateScrollbarState(false, false);
      this.revalidate();
   }

   protected void setScrollPosition() {
      this.checkAndSetViewBoxRect();
      if (this.viewBox != null) {
         AffineTransform crt = this.canvas.getRenderingTransform();
         AffineTransform vbt = this.canvas.getViewBoxTransform();
         if (crt == null) {
            crt = new AffineTransform();
         }

         if (vbt == null) {
            vbt = new AffineTransform();
         }

         Rectangle r2d = vbt.createTransformedShape(this.viewBox).getBounds();
         int tx = 0;
         int ty = 0;
         if (r2d.x < 0) {
            tx -= r2d.x;
         }

         if (r2d.y < 0) {
            ty -= r2d.y;
         }

         int deltaX = this.horizontal.getValue() - tx;
         int deltaY = this.vertical.getValue() - ty;
         crt.preConcatenate(AffineTransform.getTranslateInstance((double)(-deltaX), (double)(-deltaY)));
         this.canvas.setRenderingTransform(crt);
      }
   }

   protected void resizeScrollBars() {
      this.ignoreScrollChange = true;
      this.checkAndSetViewBoxRect();
      if (this.viewBox != null) {
         AffineTransform vbt = this.canvas.getViewBoxTransform();
         if (vbt == null) {
            vbt = new AffineTransform();
         }

         Rectangle r2d = vbt.createTransformedShape(this.viewBox).getBounds();
         int maxW = r2d.width;
         int maxH = r2d.height;
         int tx = 0;
         int ty = 0;
         if (r2d.x > 0) {
            maxW += r2d.x;
         } else {
            tx -= r2d.x;
         }

         if (r2d.y > 0) {
            maxH += r2d.y;
         } else {
            ty -= r2d.y;
         }

         Dimension vpSize = this.updateScrollbarVisibility(tx, ty, maxW, maxH);
         this.vertical.setValues(ty, vpSize.height, 0, maxH);
         this.horizontal.setValues(tx, vpSize.width, 0, maxW);
         this.vertical.setBlockIncrement((int)(0.9F * (float)vpSize.height));
         this.horizontal.setBlockIncrement((int)(0.9F * (float)vpSize.width));
         this.vertical.setUnitIncrement((int)(0.2F * (float)vpSize.height));
         this.horizontal.setUnitIncrement((int)(0.2F * (float)vpSize.width));
         this.doLayout();
         this.horizontalPanel.doLayout();
         this.horizontal.doLayout();
         this.vertical.doLayout();
         this.ignoreScrollChange = false;
      }
   }

   protected Dimension updateScrollbarVisibility(int tx, int ty, int maxW, int maxH) {
      Dimension vpSize = this.canvas.getSize();
      int maxVPW = vpSize.width;
      int minVPW = vpSize.width;
      int maxVPH = vpSize.height;
      int minVPH = vpSize.height;
      if (this.vertical.isVisible()) {
         maxVPW += this.vertical.getPreferredSize().width;
      } else {
         minVPW -= this.vertical.getPreferredSize().width;
      }

      if (this.horizontalPanel.isVisible()) {
         maxVPH += this.horizontal.getPreferredSize().height;
      } else {
         minVPH -= this.horizontal.getPreferredSize().height;
      }

      Dimension ret = new Dimension();
      boolean hNeeded;
      boolean vNeeded;
      if (this.scrollbarsAlwaysVisible) {
         hNeeded = maxW > minVPW;
         vNeeded = maxH > minVPH;
         ret.width = minVPW;
         ret.height = minVPH;
      } else {
         hNeeded = maxW > maxVPW || tx != 0;
         vNeeded = maxH > maxVPH || ty != 0;
         if (vNeeded && !hNeeded) {
            hNeeded = maxW > minVPW;
         } else if (hNeeded && !vNeeded) {
            vNeeded = maxH > minVPH;
         }

         ret.width = hNeeded ? minVPW : maxVPW;
         ret.height = vNeeded ? minVPH : maxVPH;
      }

      this.updateScrollbarState(hNeeded, vNeeded);
      return ret;
   }

   protected void updateScrollbarState(boolean hNeeded, boolean vNeeded) {
      this.horizontal.setEnabled(hNeeded);
      this.vertical.setEnabled(vNeeded);
      if (this.scrollbarsAlwaysVisible) {
         this.horizontalPanel.setVisible(true);
         this.vertical.setVisible(true);
         this.cornerBox.setVisible(true);
      } else {
         this.horizontalPanel.setVisible(hNeeded);
         this.vertical.setVisible(vNeeded);
         this.cornerBox.setVisible(hNeeded && vNeeded);
      }

   }

   protected void checkAndSetViewBoxRect() {
      if (this.viewBox == null) {
         Rectangle2D newview = this.getViewBoxRect();
         if (newview != null) {
            this.viewBox = newview;
         }
      }
   }

   protected Rectangle2D getViewBoxRect() {
      SVGDocument doc = this.canvas.getSVGDocument();
      if (doc == null) {
         return null;
      } else {
         SVGSVGElement el = doc.getRootElement();
         if (el == null) {
            return null;
         } else {
            String viewBoxStr = el.getAttributeNS((String)null, "viewBox");
            if (viewBoxStr.length() != 0) {
               float[] rect = ViewBox.parseViewBoxAttribute(el, viewBoxStr, (BridgeContext)null);
               return new Rectangle2D.Float(rect[0], rect[1], rect[2], rect[3]);
            } else {
               GraphicsNode gn = this.canvas.getGraphicsNode();
               if (gn == null) {
                  return null;
               } else {
                  Rectangle2D bounds = gn.getBounds();
                  return bounds == null ? null : (Rectangle2D)bounds.clone();
               }
            }
         }
      }
   }

   public void scaleChange(float scale) {
   }

   protected class ScrollListener extends ComponentAdapter implements JGVTComponentListener, GVTTreeBuilderListener, GVTTreeRendererListener, UpdateManagerListener {
      protected boolean isReady = false;

      public void componentTransformChanged(ComponentEvent evt) {
         if (this.isReady) {
            JSVGScrollPane.this.resizeScrollBars();
         }

      }

      public void componentResized(ComponentEvent evt) {
         if (this.isReady) {
            JSVGScrollPane.this.resizeScrollBars();
         }

      }

      public void gvtBuildStarted(GVTTreeBuilderEvent e) {
         this.isReady = false;
         JSVGScrollPane.this.updateScrollbarState(false, false);
      }

      public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
         this.isReady = true;
         JSVGScrollPane.this.viewBox = null;
      }

      public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
         if (JSVGScrollPane.this.viewBox == null) {
            JSVGScrollPane.this.resizeScrollBars();
         } else {
            Rectangle2D newview = JSVGScrollPane.this.getViewBoxRect();
            if (newview != null) {
               if (newview.getX() != JSVGScrollPane.this.viewBox.getX() || newview.getY() != JSVGScrollPane.this.viewBox.getY() || newview.getWidth() != JSVGScrollPane.this.viewBox.getWidth() || newview.getHeight() != JSVGScrollPane.this.viewBox.getHeight()) {
                  JSVGScrollPane.this.viewBox = newview;
                  JSVGScrollPane.this.resizeScrollBars();
               }

            }
         }
      }

      public void updateCompleted(UpdateManagerEvent e) {
         if (JSVGScrollPane.this.viewBox == null) {
            JSVGScrollPane.this.resizeScrollBars();
         } else {
            Rectangle2D newview = JSVGScrollPane.this.getViewBoxRect();
            if (newview != null) {
               if (newview.getX() != JSVGScrollPane.this.viewBox.getX() || newview.getY() != JSVGScrollPane.this.viewBox.getY() || newview.getWidth() != JSVGScrollPane.this.viewBox.getWidth() || newview.getHeight() != JSVGScrollPane.this.viewBox.getHeight()) {
                  JSVGScrollPane.this.viewBox = newview;
                  JSVGScrollPane.this.resizeScrollBars();
               }

            }
         }
      }

      public void gvtBuildCancelled(GVTTreeBuilderEvent e) {
      }

      public void gvtBuildFailed(GVTTreeBuilderEvent e) {
      }

      public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
      }

      public void gvtRenderingStarted(GVTTreeRendererEvent e) {
      }

      public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
      }

      public void gvtRenderingFailed(GVTTreeRendererEvent e) {
      }

      public void managerStarted(UpdateManagerEvent e) {
      }

      public void managerSuspended(UpdateManagerEvent e) {
      }

      public void managerResumed(UpdateManagerEvent e) {
      }

      public void managerStopped(UpdateManagerEvent e) {
      }

      public void updateStarted(UpdateManagerEvent e) {
      }

      public void updateFailed(UpdateManagerEvent e) {
      }
   }

   protected class SBListener implements ChangeListener {
      protected boolean inDrag = false;
      protected int startValue;
      protected boolean isVertical;

      public SBListener(boolean vertical) {
         this.isVertical = vertical;
      }

      public synchronized void stateChanged(ChangeEvent e) {
         if (!JSVGScrollPane.this.ignoreScrollChange) {
            Object src = e.getSource();
            if (src instanceof BoundedRangeModel) {
               int val = this.isVertical ? JSVGScrollPane.this.vertical.getValue() : JSVGScrollPane.this.horizontal.getValue();
               BoundedRangeModel brm = (BoundedRangeModel)src;
               if (brm.getValueIsAdjusting()) {
                  if (!this.inDrag) {
                     this.inDrag = true;
                     this.startValue = val;
                  } else {
                     AffineTransform at;
                     if (this.isVertical) {
                        at = AffineTransform.getTranslateInstance(0.0, (double)(this.startValue - val));
                     } else {
                        at = AffineTransform.getTranslateInstance((double)(this.startValue - val), 0.0);
                     }

                     JSVGScrollPane.this.canvas.setPaintingTransform(at);
                  }
               } else {
                  if (this.inDrag) {
                     this.inDrag = false;
                     if (val == this.startValue) {
                        JSVGScrollPane.this.canvas.setPaintingTransform(new AffineTransform());
                        return;
                     }
                  }

                  JSVGScrollPane.this.setScrollPosition();
               }

            }
         }
      }
   }

   protected class WheelListener implements MouseWheelListener {
      public void mouseWheelMoved(MouseWheelEvent e) {
         JScrollBar sb = JSVGScrollPane.this.vertical.isVisible() ? JSVGScrollPane.this.vertical : JSVGScrollPane.this.horizontal;
         int amt;
         if (e.getScrollType() == 0) {
            amt = e.getUnitsToScroll() * sb.getUnitIncrement();
            sb.setValue(sb.getValue() + amt);
         } else if (e.getScrollType() == 1) {
            amt = e.getWheelRotation() * sb.getBlockIncrement();
            sb.setValue(sb.getValue() + amt);
         }

      }
   }

   class SVGScrollDocumentLoaderListener extends SVGDocumentLoaderAdapter {
      public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
         NodeEventTarget root = (NodeEventTarget)e.getSVGDocument().getRootElement();
         root.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGZoom", new EventListener() {
            public void handleEvent(Event evt) {
               if (evt.getTarget() instanceof SVGSVGElement) {
                  SVGSVGElement svg = (SVGSVGElement)evt.getTarget();
                  JSVGScrollPane.this.scaleChange(svg.getCurrentScale());
               }
            }
         }, false, (Object)null);
      }
   }
}
