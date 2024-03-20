package org.apache.batik.apps.svgbrowser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.event.MouseInputAdapter;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.JGVTComponent;
import org.apache.batik.swing.gvt.Overlay;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.resources.ResourceManager;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

public class ThumbnailDialog extends JDialog {
   protected static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.ThumbnailDialog";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.ThumbnailDialog", Locale.getDefault());
   protected static ResourceManager resources;
   protected JSVGCanvas svgCanvas;
   protected JGVTComponent svgThumbnailCanvas;
   protected boolean documentChanged;
   protected AreaOfInterestOverlay overlay;
   protected AreaOfInterestListener aoiListener;
   protected boolean interactionEnabled = true;

   public ThumbnailDialog(Frame owner, JSVGCanvas svgCanvas) {
      super(owner, resources.getString("Dialog.title"));
      this.addWindowListener(new ThumbnailListener());
      this.svgCanvas = svgCanvas;
      svgCanvas.addGVTTreeRendererListener(new ThumbnailGVTListener());
      svgCanvas.addSVGDocumentLoaderListener(new ThumbnailDocumentListener());
      svgCanvas.addComponentListener(new ThumbnailCanvasComponentListener());
      this.svgThumbnailCanvas = new JGVTComponent();
      this.overlay = new AreaOfInterestOverlay();
      this.svgThumbnailCanvas.getOverlays().add(this.overlay);
      this.svgThumbnailCanvas.setPreferredSize(new Dimension(150, 150));
      this.svgThumbnailCanvas.addComponentListener(new ThumbnailComponentListener());
      this.aoiListener = new AreaOfInterestListener();
      this.svgThumbnailCanvas.addMouseListener(this.aoiListener);
      this.svgThumbnailCanvas.addMouseMotionListener(this.aoiListener);
      this.getContentPane().add(this.svgThumbnailCanvas, "Center");
   }

   public void setInteractionEnabled(boolean b) {
      if (b != this.interactionEnabled) {
         this.interactionEnabled = b;
         if (b) {
            this.svgThumbnailCanvas.addMouseListener(this.aoiListener);
            this.svgThumbnailCanvas.addMouseMotionListener(this.aoiListener);
         } else {
            this.svgThumbnailCanvas.removeMouseListener(this.aoiListener);
            this.svgThumbnailCanvas.removeMouseMotionListener(this.aoiListener);
         }

      }
   }

   public boolean getInteractionEnabled() {
      return this.interactionEnabled;
   }

   protected void updateThumbnailGraphicsNode() {
      this.svgThumbnailCanvas.setGraphicsNode(this.svgCanvas.getGraphicsNode());
      this.updateThumbnailRenderingTransform();
   }

   protected CanvasGraphicsNode getCanvasGraphicsNode(GraphicsNode gn) {
      if (!(gn instanceof CompositeGraphicsNode)) {
         return null;
      } else {
         CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
         List children = cgn.getChildren();
         if (children.size() == 0) {
            return null;
         } else {
            gn = (GraphicsNode)cgn.getChildren().get(0);
            return !(gn instanceof CanvasGraphicsNode) ? null : (CanvasGraphicsNode)gn;
         }
      }
   }

   protected void updateThumbnailRenderingTransform() {
      SVGDocument svgDocument = this.svgCanvas.getSVGDocument();
      if (svgDocument != null) {
         SVGSVGElement elt = svgDocument.getRootElement();
         Dimension dim = this.svgThumbnailCanvas.getSize();
         String viewBox = elt.getAttributeNS((String)null, "viewBox");
         AffineTransform Tx;
         if (viewBox.length() != 0) {
            String aspectRatio = elt.getAttributeNS((String)null, "preserveAspectRatio");
            Tx = ViewBox.getPreserveAspectRatioTransform(elt, (String)viewBox, (String)aspectRatio, (float)dim.width, (float)dim.height, (BridgeContext)null);
         } else {
            Dimension2D docSize = this.svgCanvas.getSVGDocumentSize();
            double sx = (double)dim.width / docSize.getWidth();
            double sy = (double)dim.height / docSize.getHeight();
            double s = Math.min(sx, sy);
            Tx = AffineTransform.getScaleInstance(s, s);
         }

         GraphicsNode gn = this.svgCanvas.getGraphicsNode();
         CanvasGraphicsNode cgn = this.getCanvasGraphicsNode(gn);
         if (cgn != null) {
            AffineTransform vTx = cgn.getViewingTransform();
            if (vTx != null && !vTx.isIdentity()) {
               try {
                  AffineTransform invVTx = vTx.createInverse();
                  Tx.concatenate(invVTx);
               } catch (NoninvertibleTransformException var13) {
               }
            }
         }

         this.svgThumbnailCanvas.setRenderingTransform(Tx);
         this.overlay.synchronizeAreaOfInterest();
      }

   }

   static {
      resources = new ResourceManager(bundle);
   }

   protected class AreaOfInterestOverlay implements Overlay {
      protected Shape s;
      protected AffineTransform at;
      protected AffineTransform paintingTransform = new AffineTransform();

      public boolean contains(int x, int y) {
         return this.s != null ? this.s.contains((double)x, (double)y) : false;
      }

      public AffineTransform getOverlayTransform() {
         return this.at;
      }

      public void setPaintingTransform(AffineTransform rt) {
         this.paintingTransform = rt;
      }

      public AffineTransform getPaintingTransform() {
         return this.paintingTransform;
      }

      public void synchronizeAreaOfInterest() {
         this.paintingTransform = new AffineTransform();
         Dimension dim = ThumbnailDialog.this.svgCanvas.getSize();
         this.s = new Rectangle2D.Float(0.0F, 0.0F, (float)dim.width, (float)dim.height);

         try {
            this.at = ThumbnailDialog.this.svgCanvas.getRenderingTransform().createInverse();
            this.at.preConcatenate(ThumbnailDialog.this.svgThumbnailCanvas.getRenderingTransform());
            this.s = this.at.createTransformedShape(this.s);
         } catch (NoninvertibleTransformException var3) {
            dim = ThumbnailDialog.this.svgThumbnailCanvas.getSize();
            this.s = new Rectangle2D.Float(0.0F, 0.0F, (float)dim.width, (float)dim.height);
         }

      }

      public void paint(Graphics g) {
         if (this.s != null) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.transform(this.paintingTransform);
            g2d.setColor(new Color(255, 255, 255, 128));
            g2d.fill(this.s);
            g2d.setColor(Color.black);
            g2d.setStroke(new BasicStroke());
            g2d.draw(this.s);
         }

      }
   }

   protected class ThumbnailCanvasComponentListener extends ComponentAdapter {
      public void componentResized(ComponentEvent e) {
         ThumbnailDialog.this.updateThumbnailRenderingTransform();
      }
   }

   protected class ThumbnailComponentListener extends ComponentAdapter {
      public void componentResized(ComponentEvent e) {
         ThumbnailDialog.this.updateThumbnailRenderingTransform();
      }
   }

   protected class ThumbnailListener extends WindowAdapter {
      public void windowOpened(WindowEvent evt) {
         ThumbnailDialog.this.updateThumbnailGraphicsNode();
      }
   }

   protected class ThumbnailGVTListener extends GVTTreeRendererAdapter {
      public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
         if (ThumbnailDialog.this.documentChanged) {
            ThumbnailDialog.this.updateThumbnailGraphicsNode();
            ThumbnailDialog.this.documentChanged = false;
         } else {
            ThumbnailDialog.this.overlay.synchronizeAreaOfInterest();
            ThumbnailDialog.this.svgThumbnailCanvas.repaint();
         }

      }

      public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
         if (ThumbnailDialog.this.documentChanged) {
            ThumbnailDialog.this.svgThumbnailCanvas.setGraphicsNode((GraphicsNode)null);
            ThumbnailDialog.this.svgThumbnailCanvas.setRenderingTransform(new AffineTransform());
         }

      }

      public void gvtRenderingFailed(GVTTreeRendererEvent e) {
         if (ThumbnailDialog.this.documentChanged) {
            ThumbnailDialog.this.svgThumbnailCanvas.setGraphicsNode((GraphicsNode)null);
            ThumbnailDialog.this.svgThumbnailCanvas.setRenderingTransform(new AffineTransform());
         }

      }
   }

   protected class AreaOfInterestListener extends MouseInputAdapter {
      protected int sx;
      protected int sy;
      protected boolean in;

      public void mousePressed(MouseEvent evt) {
         this.sx = evt.getX();
         this.sy = evt.getY();
         this.in = ThumbnailDialog.this.overlay.contains(this.sx, this.sy);
         ThumbnailDialog.this.overlay.setPaintingTransform(new AffineTransform());
      }

      public void mouseDragged(MouseEvent evt) {
         if (this.in) {
            int dx = evt.getX() - this.sx;
            int dy = evt.getY() - this.sy;
            ThumbnailDialog.this.overlay.setPaintingTransform(AffineTransform.getTranslateInstance((double)dx, (double)dy));
            ThumbnailDialog.this.svgThumbnailCanvas.repaint();
         }

      }

      public void mouseReleased(MouseEvent evt) {
         if (this.in) {
            this.in = false;
            int dx = evt.getX() - this.sx;
            int dy = evt.getY() - this.sy;
            AffineTransform at = ThumbnailDialog.this.overlay.getOverlayTransform();
            Point2D pt0 = new Point2D.Float(0.0F, 0.0F);
            Point2D pt = new Point2D.Float((float)dx, (float)dy);

            try {
               at.inverseTransform(pt0, pt0);
               at.inverseTransform(pt, pt);
               double tx = pt0.getX() - pt.getX();
               double ty = pt0.getY() - pt.getY();
               at = ThumbnailDialog.this.svgCanvas.getRenderingTransform();
               at.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));
               ThumbnailDialog.this.svgCanvas.setRenderingTransform(at);
            } catch (NoninvertibleTransformException var11) {
            }
         }

      }
   }

   protected class ThumbnailDocumentListener extends SVGDocumentLoaderAdapter {
      public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
         ThumbnailDialog.this.documentChanged = true;
      }
   }
}
