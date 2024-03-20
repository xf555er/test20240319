package org.apache.batik.gvt;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.renderable.RenderableImage;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.event.GraphicsNodeChangeEvent;
import org.apache.batik.gvt.event.GraphicsNodeChangeListener;
import org.apache.batik.gvt.filter.GraphicsNodeRable;
import org.apache.batik.gvt.filter.GraphicsNodeRable8Bit;
import org.apache.batik.util.HaltingThread;

public abstract class AbstractGraphicsNode implements GraphicsNode {
   protected EventListenerList listeners;
   protected AffineTransform transform;
   protected AffineTransform inverseTransform;
   protected Composite composite;
   protected boolean isVisible = true;
   protected ClipRable clip;
   protected RenderingHints hints;
   protected CompositeGraphicsNode parent;
   protected RootGraphicsNode root;
   protected org.apache.batik.gvt.filter.Mask mask;
   protected Filter filter;
   protected int pointerEventType = 0;
   protected WeakReference graphicsNodeRable;
   protected WeakReference enableBackgroundGraphicsNodeRable;
   protected WeakReference weakRef;
   private Rectangle2D bounds;
   protected GraphicsNodeChangeEvent changeStartedEvent = null;
   protected GraphicsNodeChangeEvent changeCompletedEvent = null;
   static double EPSILON = 1.0E-6;

   protected AbstractGraphicsNode() {
   }

   public WeakReference getWeakReference() {
      if (this.weakRef == null) {
         this.weakRef = new WeakReference(this);
      }

      return this.weakRef;
   }

   public int getPointerEventType() {
      return this.pointerEventType;
   }

   public void setPointerEventType(int pointerEventType) {
      this.pointerEventType = pointerEventType;
   }

   public void setTransform(AffineTransform newTransform) {
      this.fireGraphicsNodeChangeStarted();
      this.transform = newTransform;
      if (this.transform.getDeterminant() != 0.0) {
         try {
            this.inverseTransform = this.transform.createInverse();
         } catch (NoninvertibleTransformException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      } else {
         this.inverseTransform = this.transform;
      }

      if (this.parent != null) {
         this.parent.invalidateGeometryCache();
      }

      this.fireGraphicsNodeChangeCompleted();
   }

   public AffineTransform getTransform() {
      return this.transform;
   }

   public AffineTransform getInverseTransform() {
      return this.inverseTransform;
   }

   public AffineTransform getGlobalTransform() {
      AffineTransform ctm = new AffineTransform();

      for(GraphicsNode node = this; node != null; node = ((GraphicsNode)node).getParent()) {
         if (((GraphicsNode)node).getTransform() != null) {
            ctm.preConcatenate(((GraphicsNode)node).getTransform());
         }
      }

      return ctm;
   }

   public void setComposite(Composite newComposite) {
      this.fireGraphicsNodeChangeStarted();
      this.invalidateGeometryCache();
      this.composite = newComposite;
      this.fireGraphicsNodeChangeCompleted();
   }

   public Composite getComposite() {
      return this.composite;
   }

   public void setVisible(boolean isVisible) {
      this.fireGraphicsNodeChangeStarted();
      this.isVisible = isVisible;
      this.invalidateGeometryCache();
      this.fireGraphicsNodeChangeCompleted();
   }

   public boolean isVisible() {
      return this.isVisible;
   }

   public void setClip(ClipRable newClipper) {
      if (newClipper != null || this.clip != null) {
         this.fireGraphicsNodeChangeStarted();
         this.invalidateGeometryCache();
         this.clip = newClipper;
         this.fireGraphicsNodeChangeCompleted();
      }
   }

   public ClipRable getClip() {
      return this.clip;
   }

   public void setRenderingHint(RenderingHints.Key key, Object value) {
      this.fireGraphicsNodeChangeStarted();
      if (this.hints == null) {
         this.hints = new RenderingHints(key, value);
      } else {
         this.hints.put(key, value);
      }

      this.fireGraphicsNodeChangeCompleted();
   }

   public void setRenderingHints(Map hints) {
      this.fireGraphicsNodeChangeStarted();
      if (this.hints == null) {
         this.hints = new RenderingHints(hints);
      } else {
         this.hints.putAll(hints);
      }

      this.fireGraphicsNodeChangeCompleted();
   }

   public void setRenderingHints(RenderingHints newHints) {
      this.fireGraphicsNodeChangeStarted();
      this.hints = newHints;
      this.fireGraphicsNodeChangeCompleted();
   }

   public RenderingHints getRenderingHints() {
      return this.hints;
   }

   public void setMask(org.apache.batik.gvt.filter.Mask newMask) {
      if (newMask != null || this.mask != null) {
         this.fireGraphicsNodeChangeStarted();
         this.invalidateGeometryCache();
         this.mask = newMask;
         this.fireGraphicsNodeChangeCompleted();
      }
   }

   public org.apache.batik.gvt.filter.Mask getMask() {
      return this.mask;
   }

   public void setFilter(Filter newFilter) {
      if (newFilter != null || this.filter != null) {
         this.fireGraphicsNodeChangeStarted();
         this.invalidateGeometryCache();
         this.filter = newFilter;
         this.fireGraphicsNodeChangeCompleted();
      }
   }

   public Filter getFilter() {
      return this.filter;
   }

   public Filter getGraphicsNodeRable(boolean createIfNeeded) {
      GraphicsNodeRable ret = null;
      if (this.graphicsNodeRable != null) {
         ret = (GraphicsNodeRable)this.graphicsNodeRable.get();
         if (ret != null) {
            return (Filter)ret;
         }
      }

      if (createIfNeeded) {
         ret = new GraphicsNodeRable8Bit(this);
         this.graphicsNodeRable = new WeakReference(ret);
      }

      return (Filter)ret;
   }

   public Filter getEnableBackgroundGraphicsNodeRable(boolean createIfNeeded) {
      GraphicsNodeRable ret = null;
      if (this.enableBackgroundGraphicsNodeRable != null) {
         ret = (GraphicsNodeRable)this.enableBackgroundGraphicsNodeRable.get();
         if (ret != null) {
            return (Filter)ret;
         }
      }

      if (createIfNeeded) {
         ret = new GraphicsNodeRable8Bit(this);
         ((GraphicsNodeRable)ret).setUsePrimitivePaint(false);
         this.enableBackgroundGraphicsNodeRable = new WeakReference(ret);
      }

      return (Filter)ret;
   }

   public void paint(Graphics2D g2d) {
      if (this.composite != null && this.composite instanceof AlphaComposite) {
         AlphaComposite ac = (AlphaComposite)this.composite;
         if ((double)ac.getAlpha() < 0.001) {
            return;
         }
      }

      Rectangle2D bounds = this.getBounds();
      if (bounds != null) {
         Composite defaultComposite = null;
         AffineTransform defaultTransform = null;
         RenderingHints defaultHints = null;
         Graphics2D baseG2d = null;
         if (this.clip != null) {
            baseG2d = g2d;
            g2d = (Graphics2D)g2d.create();
            if (this.hints != null) {
               g2d.addRenderingHints(this.hints);
            }

            if (this.transform != null) {
               g2d.transform(this.transform);
            }

            if (this.composite != null) {
               g2d.setComposite(this.composite);
            }

            g2d.clip(this.clip.getClipPath());
         } else {
            if (this.hints != null) {
               defaultHints = g2d.getRenderingHints();
               g2d.addRenderingHints(this.hints);
            }

            if (this.transform != null) {
               defaultTransform = g2d.getTransform();
               g2d.transform(this.transform);
            }

            if (this.composite != null) {
               defaultComposite = g2d.getComposite();
               g2d.setComposite(this.composite);
            }
         }

         Shape curClip = g2d.getClip();
         g2d.setRenderingHint(RenderingHintsKeyExt.KEY_AREA_OF_INTEREST, curClip);
         boolean paintNeeded = true;
         if (curClip != null) {
            Rectangle2D cb = curClip.getBounds2D();
            if (!bounds.intersects(cb.getX(), cb.getY(), cb.getWidth(), cb.getHeight())) {
               paintNeeded = false;
            }
         }

         if (paintNeeded) {
            boolean antialiasedClip = false;
            if (this.clip != null && this.clip.getUseAntialiasedClip()) {
               antialiasedClip = this.isAntialiasedClip(g2d.getTransform(), g2d.getRenderingHints(), this.clip.getClipPath());
            }

            boolean useOffscreen = this.isOffscreenBufferNeeded();
            useOffscreen |= antialiasedClip;
            if (!useOffscreen) {
               this.primitivePaint(g2d);
            } else {
               Filter filteredImage = null;
               if (this.filter == null) {
                  filteredImage = this.getGraphicsNodeRable(true);
               } else {
                  filteredImage = this.filter;
               }

               if (this.mask != null) {
                  if (this.mask.getSource() != filteredImage) {
                     this.mask.setSource((Filter)filteredImage);
                  }

                  filteredImage = this.mask;
               }

               if (this.clip != null && antialiasedClip) {
                  if (this.clip.getSource() != filteredImage) {
                     this.clip.setSource((Filter)filteredImage);
                  }

                  filteredImage = this.clip;
               }

               baseG2d = g2d;
               g2d = (Graphics2D)g2d.create();
               if (antialiasedClip) {
                  g2d.setClip((Shape)null);
               }

               Rectangle2D filterBounds = ((Filter)filteredImage).getBounds2D();
               g2d.clip(filterBounds);
               GraphicsUtil.drawImage(g2d, (RenderableImage)filteredImage);
               g2d.dispose();
               g2d = baseG2d;
               baseG2d = null;
            }
         }

         if (baseG2d != null) {
            g2d.dispose();
         } else {
            if (defaultHints != null) {
               g2d.setRenderingHints(defaultHints);
            }

            if (defaultTransform != null) {
               g2d.setTransform(defaultTransform);
            }

            if (defaultComposite != null) {
               g2d.setComposite(defaultComposite);
            }
         }

      }
   }

   private void traceFilter(Filter filter, String prefix) {
      System.out.println(prefix + filter.getClass().getName());
      System.out.println(prefix + filter.getBounds2D());
      List sources = filter.getSources();
      int nSources = sources != null ? sources.size() : 0;
      prefix = prefix + "\t";

      for(int i = 0; i < nSources; ++i) {
         Filter source = (Filter)sources.get(i);
         this.traceFilter(source, prefix);
      }

      System.out.flush();
   }

   protected boolean isOffscreenBufferNeeded() {
      return this.filter != null || this.mask != null || this.composite != null && !AlphaComposite.SrcOver.equals(this.composite);
   }

   protected boolean isAntialiasedClip(AffineTransform usr2dev, RenderingHints hints, Shape clip) {
      if (clip == null) {
         return false;
      } else {
         Object val = hints.get(RenderingHintsKeyExt.KEY_TRANSCODING);
         if (val != "Printing" && val != "Vector") {
            return !(clip instanceof Rectangle2D) || usr2dev.getShearX() != 0.0 || usr2dev.getShearY() != 0.0;
         } else {
            return false;
         }
      }
   }

   public void fireGraphicsNodeChangeStarted(GraphicsNode changeSrc) {
      if (this.changeStartedEvent == null) {
         this.changeStartedEvent = new GraphicsNodeChangeEvent(this, 9800);
      }

      this.changeStartedEvent.setChangeSrc(changeSrc);
      this.fireGraphicsNodeChangeStarted(this.changeStartedEvent);
      this.changeStartedEvent.setChangeSrc((GraphicsNode)null);
   }

   public void fireGraphicsNodeChangeStarted() {
      if (this.changeStartedEvent == null) {
         this.changeStartedEvent = new GraphicsNodeChangeEvent(this, 9800);
      } else {
         this.changeStartedEvent.setChangeSrc((GraphicsNode)null);
      }

      this.fireGraphicsNodeChangeStarted(this.changeStartedEvent);
   }

   public void fireGraphicsNodeChangeStarted(GraphicsNodeChangeEvent changeStartedEvent) {
      RootGraphicsNode rootGN = this.getRoot();
      if (rootGN != null) {
         List l = rootGN.getTreeGraphicsNodeChangeListeners();
         if (l != null) {
            Iterator i = l.iterator();

            while(i.hasNext()) {
               GraphicsNodeChangeListener gncl = (GraphicsNodeChangeListener)i.next();
               gncl.changeStarted(changeStartedEvent);
            }

         }
      }
   }

   public void fireGraphicsNodeChangeCompleted() {
      if (this.changeCompletedEvent == null) {
         this.changeCompletedEvent = new GraphicsNodeChangeEvent(this, 9801);
      }

      RootGraphicsNode rootGN = this.getRoot();
      if (rootGN != null) {
         List l = rootGN.getTreeGraphicsNodeChangeListeners();
         if (l != null) {
            Iterator i = l.iterator();

            while(i.hasNext()) {
               GraphicsNodeChangeListener gncl = (GraphicsNodeChangeListener)i.next();
               gncl.changeCompleted(this.changeCompletedEvent);
            }

         }
      }
   }

   public CompositeGraphicsNode getParent() {
      return this.parent;
   }

   public RootGraphicsNode getRoot() {
      return this.root;
   }

   protected void setRoot(RootGraphicsNode newRoot) {
      this.root = newRoot;
   }

   protected void setParent(CompositeGraphicsNode newParent) {
      this.parent = newParent;
   }

   protected void invalidateGeometryCache() {
      if (this.parent != null) {
         this.parent.invalidateGeometryCache();
      }

      this.bounds = null;
   }

   public Rectangle2D getBounds() {
      if (this.bounds == null) {
         if (this.filter == null) {
            this.bounds = this.getPrimitiveBounds();
         } else {
            this.bounds = this.filter.getBounds2D();
         }

         if (this.bounds != null) {
            Rectangle2D maskR;
            if (this.clip != null) {
               maskR = this.clip.getClipPath().getBounds2D();
               if (maskR.intersects(this.bounds)) {
                  Rectangle2D.intersect(this.bounds, maskR, this.bounds);
               }
            }

            if (this.mask != null) {
               maskR = this.mask.getBounds2D();
               if (maskR.intersects(this.bounds)) {
                  Rectangle2D.intersect(this.bounds, maskR, this.bounds);
               }
            }
         }

         this.bounds = this.normalizeRectangle(this.bounds);
         if (HaltingThread.hasBeenHalted()) {
            this.invalidateGeometryCache();
         }
      }

      return this.bounds;
   }

   public Rectangle2D getTransformedBounds(AffineTransform txf) {
      AffineTransform t = txf;
      if (this.transform != null) {
         t = new AffineTransform(txf);
         t.concatenate(this.transform);
      }

      Rectangle2D tBounds = null;
      if (this.filter == null) {
         tBounds = this.getTransformedPrimitiveBounds(txf);
      } else {
         tBounds = t.createTransformedShape(this.filter.getBounds2D()).getBounds2D();
      }

      if (tBounds != null) {
         if (this.clip != null) {
            Rectangle2D.intersect(tBounds, t.createTransformedShape(this.clip.getClipPath()).getBounds2D(), tBounds);
         }

         if (this.mask != null) {
            Rectangle2D.intersect(tBounds, t.createTransformedShape(this.mask.getBounds2D()).getBounds2D(), tBounds);
         }
      }

      return tBounds;
   }

   public Rectangle2D getTransformedPrimitiveBounds(AffineTransform txf) {
      Rectangle2D tpBounds = this.getPrimitiveBounds();
      if (tpBounds == null) {
         return null;
      } else {
         AffineTransform t = txf;
         if (this.transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(this.transform);
         }

         return t.createTransformedShape(tpBounds).getBounds2D();
      }
   }

   public Rectangle2D getTransformedGeometryBounds(AffineTransform txf) {
      Rectangle2D tpBounds = this.getGeometryBounds();
      if (tpBounds == null) {
         return null;
      } else {
         AffineTransform t = txf;
         if (this.transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(this.transform);
         }

         return t.createTransformedShape(tpBounds).getBounds2D();
      }
   }

   public Rectangle2D getTransformedSensitiveBounds(AffineTransform txf) {
      Rectangle2D sBounds = this.getSensitiveBounds();
      if (sBounds == null) {
         return null;
      } else {
         AffineTransform t = txf;
         if (this.transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(this.transform);
         }

         return t.createTransformedShape(sBounds).getBounds2D();
      }
   }

   public boolean contains(Point2D p) {
      Rectangle2D b = this.getSensitiveBounds();
      if (b != null && b.contains(p)) {
         switch (this.pointerEventType) {
            case 0:
            case 1:
            case 2:
            case 3:
               return this.isVisible;
            case 4:
            case 5:
            case 6:
            case 7:
               return true;
            case 8:
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public boolean intersects(Rectangle2D r) {
      Rectangle2D b = this.getBounds();
      return b == null ? false : b.intersects(r);
   }

   public GraphicsNode nodeHitAt(Point2D p) {
      return this.contains(p) ? this : null;
   }

   protected Rectangle2D normalizeRectangle(Rectangle2D bounds) {
      if (bounds == null) {
         return null;
      } else {
         double tmpH;
         if (bounds.getWidth() < EPSILON) {
            if (bounds.getHeight() < EPSILON) {
               AffineTransform gt = this.getGlobalTransform();
               double det = Math.sqrt(gt.getDeterminant());
               return new Rectangle2D.Double(bounds.getX(), bounds.getY(), EPSILON / det, EPSILON / det);
            } else {
               tmpH = bounds.getHeight() * EPSILON;
               if (tmpH < bounds.getWidth()) {
                  tmpH = bounds.getWidth();
               }

               return new Rectangle2D.Double(bounds.getX(), bounds.getY(), tmpH, bounds.getHeight());
            }
         } else if (bounds.getHeight() < EPSILON) {
            tmpH = bounds.getWidth() * EPSILON;
            if (tmpH < bounds.getHeight()) {
               tmpH = bounds.getHeight();
            }

            return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), tmpH);
         } else {
            return bounds;
         }
      }
   }
}
