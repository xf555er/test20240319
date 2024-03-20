package org.apache.batik.swing.svg;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.BridgeExtension;
import org.apache.batik.bridge.DefaultFontFamilyResolver;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.bridge.Mark;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.script.Interpreter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.JGVTComponent;
import org.apache.batik.swing.gvt.JGVTComponentListener;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.RunnableQueue;
import org.apache.batik.util.SVGFeatureStrings;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

public class JSVGComponent extends JGVTComponent {
   public static final int AUTODETECT = 0;
   public static final int ALWAYS_DYNAMIC = 1;
   public static final int ALWAYS_STATIC = 2;
   public static final int ALWAYS_INTERACTIVE = 3;
   public static final String SCRIPT_ALERT = "script.alert";
   public static final String SCRIPT_PROMPT = "script.prompt";
   public static final String SCRIPT_CONFIRM = "script.confirm";
   public static final String BROKEN_LINK_TITLE = "broken.link.title";
   protected SVGDocumentLoader documentLoader;
   protected SVGDocumentLoader nextDocumentLoader;
   protected DocumentLoader loader;
   protected GVTTreeBuilder gvtTreeBuilder;
   protected GVTTreeBuilder nextGVTTreeBuilder;
   protected SVGLoadEventDispatcher svgLoadEventDispatcher;
   protected UpdateManager updateManager;
   protected UpdateManager nextUpdateManager;
   protected SVGDocument svgDocument;
   protected List svgDocumentLoaderListeners;
   protected List gvtTreeBuilderListeners;
   protected List svgLoadEventDispatcherListeners;
   protected List linkActivationListeners;
   protected List updateManagerListeners;
   protected UserAgent userAgent;
   protected SVGUserAgent svgUserAgent;
   protected BridgeContext bridgeContext;
   protected String fragmentIdentifier;
   protected boolean isDynamicDocument;
   protected boolean isInteractiveDocument;
   protected boolean selfCallingDisableInteractions;
   protected boolean userSetDisableInteractions;
   protected int documentState;
   protected Dimension prevComponentSize;
   protected Runnable afterStopRunnable;
   protected SVGUpdateOverlay updateOverlay;
   protected boolean recenterOnResize;
   protected AffineTransform viewingTransform;
   protected int animationLimitingMode;
   protected float animationLimitingAmount;
   protected JSVGComponentListener jsvgComponentListener;
   protected static final Set FEATURES = new HashSet();

   public JSVGComponent() {
      this((SVGUserAgent)null, false, false);
   }

   public JSVGComponent(SVGUserAgent ua, boolean eventsEnabled, boolean selectableText) {
      super(eventsEnabled, selectableText);
      this.svgDocumentLoaderListeners = new LinkedList();
      this.gvtTreeBuilderListeners = new LinkedList();
      this.svgLoadEventDispatcherListeners = new LinkedList();
      this.linkActivationListeners = new LinkedList();
      this.updateManagerListeners = new LinkedList();
      this.selfCallingDisableInteractions = false;
      this.userSetDisableInteractions = false;
      this.afterStopRunnable = null;
      this.recenterOnResize = true;
      this.viewingTransform = null;
      this.jsvgComponentListener = new JSVGComponentListener();
      this.svgUserAgent = ua;
      this.userAgent = new BridgeUserAgentWrapper(this.createUserAgent());
      this.addSVGDocumentLoaderListener((SVGListener)this.listener);
      this.addGVTTreeBuilderListener((SVGListener)this.listener);
      this.addSVGLoadEventDispatcherListener((SVGListener)this.listener);
      if (this.updateOverlay != null) {
         this.getOverlays().add(this.updateOverlay);
      }

   }

   public void dispose() {
      this.setSVGDocument((SVGDocument)null);
   }

   public void setDisableInteractions(boolean b) {
      super.setDisableInteractions(b);
      if (!this.selfCallingDisableInteractions) {
         this.userSetDisableInteractions = true;
      }

   }

   public void clearUserSetDisableInteractions() {
      this.userSetDisableInteractions = false;
      this.updateZoomAndPanEnable(this.svgDocument);
   }

   public void updateZoomAndPanEnable(Document doc) {
      if (!this.userSetDisableInteractions) {
         if (doc != null) {
            try {
               Element root = doc.getDocumentElement();
               String znp = root.getAttributeNS((String)null, "zoomAndPan");
               boolean enable = "magnify".equals(znp);
               this.selfCallingDisableInteractions = true;
               this.setDisableInteractions(!enable);
            } finally {
               this.selfCallingDisableInteractions = false;
            }

         }
      }
   }

   public boolean getRecenterOnResize() {
      return this.recenterOnResize;
   }

   public void setRecenterOnResize(boolean recenterOnResize) {
      this.recenterOnResize = recenterOnResize;
   }

   public boolean isDynamic() {
      return this.isDynamicDocument;
   }

   public boolean isInteractive() {
      return this.isInteractiveDocument;
   }

   public void setDocumentState(int state) {
      this.documentState = state;
   }

   public UpdateManager getUpdateManager() {
      if (this.svgLoadEventDispatcher != null) {
         return this.svgLoadEventDispatcher.getUpdateManager();
      } else {
         return this.nextUpdateManager != null ? this.nextUpdateManager : this.updateManager;
      }
   }

   public void resumeProcessing() {
      if (this.updateManager != null) {
         this.updateManager.resume();
      }

   }

   public void suspendProcessing() {
      if (this.updateManager != null) {
         this.updateManager.suspend();
      }

   }

   public void stopProcessing() {
      this.nextDocumentLoader = null;
      this.nextGVTTreeBuilder = null;
      if (this.documentLoader != null) {
         this.documentLoader.halt();
      }

      if (this.gvtTreeBuilder != null) {
         this.gvtTreeBuilder.halt();
      }

      if (this.svgLoadEventDispatcher != null) {
         this.svgLoadEventDispatcher.halt();
      }

      if (this.nextUpdateManager != null) {
         this.nextUpdateManager.interrupt();
         this.nextUpdateManager = null;
      }

      if (this.updateManager != null) {
         this.updateManager.interrupt();
      }

      super.stopProcessing();
   }

   public void loadSVGDocument(String url) {
      String oldURI = null;
      if (this.svgDocument != null) {
         oldURI = this.svgDocument.getURL();
      }

      final ParsedURL newURI = new ParsedURL(oldURI, url);
      this.stopThenRun(new Runnable() {
         public void run() {
            String url = newURI.toString();
            JSVGComponent.this.fragmentIdentifier = newURI.getRef();
            JSVGComponent.this.loader = new DocumentLoader(JSVGComponent.this.userAgent);
            JSVGComponent.this.nextDocumentLoader = new SVGDocumentLoader(url, JSVGComponent.this.loader);
            JSVGComponent.this.nextDocumentLoader.setPriority(1);
            Iterator var2 = JSVGComponent.this.svgDocumentLoaderListeners.iterator();

            while(var2.hasNext()) {
               Object svgDocumentLoaderListener = var2.next();
               JSVGComponent.this.nextDocumentLoader.addSVGDocumentLoaderListener((SVGDocumentLoaderListener)svgDocumentLoaderListener);
            }

            JSVGComponent.this.startDocumentLoader();
         }
      });
   }

   private void startDocumentLoader() {
      this.documentLoader = this.nextDocumentLoader;
      this.nextDocumentLoader = null;
      this.documentLoader.start();
   }

   public void setDocument(Document doc) {
      if (doc != null && !(doc.getImplementation() instanceof SVGDOMImplementation)) {
         DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
         Document d = DOMUtilities.deepCloneDocument(doc, impl);
         doc = d;
      }

      this.setSVGDocument((SVGDocument)doc);
   }

   public void setSVGDocument(final SVGDocument doc) {
      if (doc != null && !(doc.getImplementation() instanceof SVGDOMImplementation)) {
         DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
         Document d = DOMUtilities.deepCloneDocument(doc, impl);
         doc = (SVGDocument)d;
      }

      this.stopThenRun(new Runnable() {
         public void run() {
            JSVGComponent.this.installSVGDocument(doc);
         }
      });
   }

   protected void stopThenRun(Runnable r) {
      if (this.afterStopRunnable != null) {
         this.afterStopRunnable = r;
      } else {
         this.afterStopRunnable = r;
         this.stopProcessing();
         if (this.documentLoader == null && this.gvtTreeBuilder == null && this.gvtTreeRenderer == null && this.svgLoadEventDispatcher == null && this.nextUpdateManager == null && this.updateManager == null) {
            Runnable asr = this.afterStopRunnable;
            this.afterStopRunnable = null;
            asr.run();
         }

      }
   }

   protected void installSVGDocument(SVGDocument doc) {
      this.svgDocument = doc;
      if (this.bridgeContext != null) {
         this.bridgeContext.dispose();
         this.bridgeContext = null;
      }

      this.releaseRenderingReferences();
      if (doc == null) {
         this.isDynamicDocument = false;
         this.isInteractiveDocument = false;
         this.disableInteractions = true;
         this.initialTransform = new AffineTransform();
         this.setRenderingTransform(this.initialTransform, false);
         Rectangle vRect = this.getRenderRect();
         this.repaint(vRect.x, vRect.y, vRect.width, vRect.height);
      } else {
         this.bridgeContext = this.createBridgeContext((SVGOMDocument)doc);
         switch (this.documentState) {
            case 0:
               this.isDynamicDocument = this.bridgeContext.isDynamicDocument(doc);
               this.isInteractiveDocument = this.isDynamicDocument || this.bridgeContext.isInteractiveDocument(doc);
               break;
            case 1:
               this.isDynamicDocument = true;
               this.isInteractiveDocument = true;
               break;
            case 2:
               this.isDynamicDocument = false;
               this.isInteractiveDocument = false;
               break;
            case 3:
               this.isDynamicDocument = false;
               this.isInteractiveDocument = true;
         }

         if (this.isInteractiveDocument) {
            if (this.isDynamicDocument) {
               this.bridgeContext.setDynamicState(2);
            } else {
               this.bridgeContext.setDynamicState(1);
            }
         }

         this.setBridgeContextAnimationLimitingMode();
         this.updateZoomAndPanEnable(doc);
         this.nextGVTTreeBuilder = new GVTTreeBuilder(doc, this.bridgeContext);
         this.nextGVTTreeBuilder.setPriority(1);
         Iterator var2 = this.gvtTreeBuilderListeners.iterator();

         while(var2.hasNext()) {
            Object gvtTreeBuilderListener = var2.next();
            this.nextGVTTreeBuilder.addGVTTreeBuilderListener((GVTTreeBuilderListener)gvtTreeBuilderListener);
         }

         this.initializeEventHandling();
         if (this.gvtTreeBuilder == null && this.documentLoader == null && this.gvtTreeRenderer == null && this.svgLoadEventDispatcher == null && this.updateManager == null) {
            this.startGVTTreeBuilder();
         }

      }
   }

   protected void startGVTTreeBuilder() {
      this.gvtTreeBuilder = this.nextGVTTreeBuilder;
      this.nextGVTTreeBuilder = null;
      this.gvtTreeBuilder.start();
   }

   public SVGDocument getSVGDocument() {
      return this.svgDocument;
   }

   public Dimension2D getSVGDocumentSize() {
      return this.bridgeContext.getDocumentSize();
   }

   public String getFragmentIdentifier() {
      return this.fragmentIdentifier;
   }

   public void setFragmentIdentifier(String fi) {
      this.fragmentIdentifier = fi;
      if (this.computeRenderingTransform()) {
         this.scheduleGVTRendering();
      }

   }

   public void flushImageCache() {
      ImageTagRegistry reg = ImageTagRegistry.getRegistry();
      reg.flushCache();
   }

   public void setGraphicsNode(GraphicsNode gn, boolean createDispatcher) {
      Dimension2D dim = this.bridgeContext.getDocumentSize();
      Dimension mySz = new Dimension((int)dim.getWidth(), (int)dim.getHeight());
      this.setMySize(mySz);
      SVGSVGElement elt = this.svgDocument.getRootElement();
      this.prevComponentSize = this.getSize();
      AffineTransform at = this.calculateViewingTransform(this.fragmentIdentifier, elt);
      CanvasGraphicsNode cgn = this.getCanvasGraphicsNode(gn);
      if (cgn != null) {
         cgn.setViewingTransform(at);
      }

      this.viewingTransform = null;
      this.initialTransform = new AffineTransform();
      this.setRenderingTransform(this.initialTransform, false);
      this.jsvgComponentListener.updateMatrix(this.initialTransform);
      this.addJGVTComponentListener(this.jsvgComponentListener);
      this.addComponentListener(this.jsvgComponentListener);
      super.setGraphicsNode(gn, createDispatcher);
   }

   protected BridgeContext createBridgeContext(SVGOMDocument doc) {
      if (this.loader == null) {
         this.loader = new DocumentLoader(this.userAgent);
      }

      Object result;
      if (doc.isSVG12()) {
         result = new SVG12BridgeContext(this.userAgent, this.loader);
      } else {
         result = new BridgeContext(this.userAgent, this.loader);
      }

      return (BridgeContext)result;
   }

   protected void startSVGLoadEventDispatcher(GraphicsNode root) {
      UpdateManager um = new UpdateManager(this.bridgeContext, root, this.svgDocument);
      this.svgLoadEventDispatcher = new SVGLoadEventDispatcher(root, this.svgDocument, this.bridgeContext, um);
      Iterator var3 = this.svgLoadEventDispatcherListeners.iterator();

      while(var3.hasNext()) {
         Object svgLoadEventDispatcherListener = var3.next();
         this.svgLoadEventDispatcher.addSVGLoadEventDispatcherListener((SVGLoadEventDispatcherListener)svgLoadEventDispatcherListener);
      }

      this.svgLoadEventDispatcher.start();
   }

   protected ImageRenderer createImageRenderer() {
      return this.isDynamicDocument ? this.rendererFactory.createDynamicImageRenderer() : this.rendererFactory.createStaticImageRenderer();
   }

   public CanvasGraphicsNode getCanvasGraphicsNode() {
      return this.getCanvasGraphicsNode(this.gvtRoot);
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
            gn = (GraphicsNode)children.get(0);
            return !(gn instanceof CanvasGraphicsNode) ? null : (CanvasGraphicsNode)gn;
         }
      }
   }

   public AffineTransform getViewingTransform() {
      synchronized(this) {
         AffineTransform vt = this.viewingTransform;
         if (vt == null) {
            CanvasGraphicsNode cgn = this.getCanvasGraphicsNode();
            if (cgn != null) {
               vt = cgn.getViewingTransform();
            }
         }

         return vt;
      }
   }

   public AffineTransform getViewBoxTransform() {
      AffineTransform at = this.getRenderingTransform();
      if (at == null) {
         at = new AffineTransform();
      } else {
         at = new AffineTransform(at);
      }

      AffineTransform vt = this.getViewingTransform();
      if (vt != null) {
         at.concatenate(vt);
      }

      return at;
   }

   protected boolean computeRenderingTransform() {
      if (this.svgDocument != null && this.gvtRoot != null) {
         boolean ret = this.updateRenderingTransform();
         this.initialTransform = new AffineTransform();
         if (!this.initialTransform.equals(this.getRenderingTransform())) {
            this.setRenderingTransform(this.initialTransform, false);
            ret = true;
         }

         return ret;
      } else {
         return false;
      }
   }

   protected AffineTransform calculateViewingTransform(String fragIdent, SVGSVGElement svgElt) {
      Dimension d = this.getSize();
      if (d.width < 1) {
         d.width = 1;
      }

      if (d.height < 1) {
         d.height = 1;
      }

      return ViewBox.getViewTransform(fragIdent, svgElt, (float)d.width, (float)d.height, this.bridgeContext);
   }

   protected boolean updateRenderingTransform() {
      if (this.svgDocument != null && this.gvtRoot != null) {
         try {
            SVGSVGElement elt = this.svgDocument.getRootElement();
            Dimension d = this.getSize();
            Dimension oldD = this.prevComponentSize;
            if (oldD == null) {
               oldD = d;
            }

            this.prevComponentSize = d;
            if (d.width < 1) {
               d.width = 1;
            }

            if (d.height < 1) {
               d.height = 1;
            }

            final AffineTransform at = this.calculateViewingTransform(this.fragmentIdentifier, elt);
            AffineTransform vt = this.getViewingTransform();
            if (at.equals(vt)) {
               return oldD.width != d.width || oldD.height != d.height;
            }

            if (this.recenterOnResize) {
               Point2D pt = new Point2D.Float((float)oldD.width / 2.0F, (float)oldD.height / 2.0F);
               AffineTransform rendAT = this.getRenderingTransform();
               AffineTransform invVT;
               if (rendAT != null) {
                  try {
                     invVT = rendAT.createInverse();
                     pt = invVT.transform((Point2D)pt, (Point2D)null);
                  } catch (NoninvertibleTransformException var13) {
                  }
               }

               if (vt != null) {
                  try {
                     invVT = vt.createInverse();
                     pt = invVT.transform((Point2D)pt, (Point2D)null);
                  } catch (NoninvertibleTransformException var12) {
                  }
               }

               if (at != null) {
                  pt = at.transform((Point2D)pt, (Point2D)null);
               }

               if (rendAT != null) {
                  pt = rendAT.transform((Point2D)pt, (Point2D)null);
               }

               float dx = (float)((double)((float)d.width / 2.0F) - ((Point2D)pt).getX());
               float dy = (float)((double)((float)d.height / 2.0F) - ((Point2D)pt).getY());
               dx = (float)((int)(dx < 0.0F ? (double)dx - 0.5 : (double)dx + 0.5));
               dy = (float)((int)(dy < 0.0F ? (double)dy - 0.5 : (double)dy + 0.5));
               if (dx != 0.0F || dy != 0.0F) {
                  rendAT.preConcatenate(AffineTransform.getTranslateInstance((double)dx, (double)dy));
                  this.setRenderingTransform(rendAT, false);
               }
            }

            synchronized(this) {
               this.viewingTransform = at;
            }

            Runnable r = new Runnable() {
               AffineTransform myAT = at;
               CanvasGraphicsNode myCGN = JSVGComponent.this.getCanvasGraphicsNode();

               public void run() {
                  synchronized(JSVGComponent.this) {
                     if (this.myCGN != null) {
                        this.myCGN.setViewingTransform(this.myAT);
                     }

                     if (JSVGComponent.this.viewingTransform == this.myAT) {
                        JSVGComponent.this.viewingTransform = null;
                     }

                  }
               }
            };
            UpdateManager um = this.getUpdateManager();
            if (um != null) {
               um.getUpdateRunnableQueue().invokeLater(r);
            } else {
               r.run();
            }
         } catch (BridgeException var14) {
            this.userAgent.displayError(var14);
         }

         return true;
      } else {
         return false;
      }
   }

   protected void renderGVTTree() {
      if (this.isInteractiveDocument && this.updateManager != null && this.updateManager.isRunning()) {
         Rectangle visRect = this.getRenderRect();
         if (this.gvtRoot != null && visRect.width > 0 && visRect.height > 0) {
            AffineTransform inv = null;

            try {
               inv = this.renderingTransform.createInverse();
            } catch (NoninvertibleTransformException var9) {
            }

            Object s;
            if (inv == null) {
               s = visRect;
            } else {
               s = inv.createTransformedShape(visRect);
            }

            RunnableQueue rq = this.updateManager.getUpdateRunnableQueue();

            class UpdateRenderingRunnable implements Runnable {
               AffineTransform at;
               boolean doubleBuf;
               boolean clearPaintTrans;
               Shape aoi;
               int width;
               int height;
               boolean active;

               public UpdateRenderingRunnable(AffineTransform at, boolean doubleBuf, boolean clearPaintTrans, Shape aoi, int width, int height) {
                  this.updateInfo(at, doubleBuf, clearPaintTrans, aoi, width, height);
                  this.active = true;
               }

               public void updateInfo(AffineTransform at, boolean doubleBuf, boolean clearPaintTrans, Shape aoi, int width, int height) {
                  this.at = at;
                  this.doubleBuf = doubleBuf;
                  this.clearPaintTrans = clearPaintTrans;
                  this.aoi = aoi;
                  this.width = width;
                  this.height = height;
                  this.active = true;
               }

               public void deactivate() {
                  this.active = false;
               }

               public void run() {
                  if (this.active) {
                     JSVGComponent.this.updateManager.updateRendering(this.at, this.doubleBuf, this.clearPaintTrans, this.aoi, this.width, this.height);
                  }
               }
            }

            synchronized(rq.getIteratorLock()) {
               Iterator it = rq.iterator();

               while(true) {
                  if (!it.hasNext()) {
                     break;
                  }

                  Object next = it.next();
                  if (next instanceof UpdateRenderingRunnable) {
                     ((UpdateRenderingRunnable)next).deactivate();
                  }
               }
            }

            rq.invokeLater(new UpdateRenderingRunnable(this.renderingTransform, this.doubleBufferedRendering, true, (Shape)s, visRect.width, visRect.height));
         }
      } else {
         super.renderGVTTree();
      }
   }

   protected void handleException(Exception e) {
      this.userAgent.displayError(e);
   }

   public void addSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
      this.svgDocumentLoaderListeners.add(l);
   }

   public void removeSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
      this.svgDocumentLoaderListeners.remove(l);
   }

   public void addGVTTreeBuilderListener(GVTTreeBuilderListener l) {
      this.gvtTreeBuilderListeners.add(l);
   }

   public void removeGVTTreeBuilderListener(GVTTreeBuilderListener l) {
      this.gvtTreeBuilderListeners.remove(l);
   }

   public void addSVGLoadEventDispatcherListener(SVGLoadEventDispatcherListener l) {
      this.svgLoadEventDispatcherListeners.add(l);
   }

   public void removeSVGLoadEventDispatcherListener(SVGLoadEventDispatcherListener l) {
      this.svgLoadEventDispatcherListeners.remove(l);
   }

   public void addLinkActivationListener(LinkActivationListener l) {
      this.linkActivationListeners.add(l);
   }

   public void removeLinkActivationListener(LinkActivationListener l) {
      this.linkActivationListeners.remove(l);
   }

   public void addUpdateManagerListener(UpdateManagerListener l) {
      this.updateManagerListeners.add(l);
   }

   public void removeUpdateManagerListener(UpdateManagerListener l) {
      this.updateManagerListeners.remove(l);
   }

   public void showAlert(String message) {
      JOptionPane.showMessageDialog(this, Messages.formatMessage("script.alert", new Object[]{message}));
   }

   public String showPrompt(String message) {
      return JOptionPane.showInputDialog(this, Messages.formatMessage("script.prompt", new Object[]{message}));
   }

   public String showPrompt(String message, String defaultValue) {
      return (String)JOptionPane.showInputDialog(this, Messages.formatMessage("script.prompt", new Object[]{message}), (String)null, -1, (Icon)null, (Object[])null, defaultValue);
   }

   public boolean showConfirm(String message) {
      return JOptionPane.showConfirmDialog(this, Messages.formatMessage("script.confirm", new Object[]{message}), "Confirm", 0) == 0;
   }

   public void setMySize(Dimension d) {
      this.setPreferredSize(d);
      this.invalidate();
   }

   public void setAnimationLimitingNone() {
      this.animationLimitingMode = 0;
      if (this.bridgeContext != null) {
         this.setBridgeContextAnimationLimitingMode();
      }

   }

   public void setAnimationLimitingCPU(float pc) {
      this.animationLimitingMode = 1;
      this.animationLimitingAmount = pc;
      if (this.bridgeContext != null) {
         this.setBridgeContextAnimationLimitingMode();
      }

   }

   public void setAnimationLimitingFPS(float fps) {
      this.animationLimitingMode = 2;
      this.animationLimitingAmount = fps;
      if (this.bridgeContext != null) {
         this.setBridgeContextAnimationLimitingMode();
      }

   }

   public Interpreter getInterpreter(String type) {
      return this.bridgeContext != null ? this.bridgeContext.getInterpreter(type) : null;
   }

   protected void setBridgeContextAnimationLimitingMode() {
      switch (this.animationLimitingMode) {
         case 0:
            this.bridgeContext.setAnimationLimitingNone();
            break;
         case 1:
            this.bridgeContext.setAnimationLimitingCPU(this.animationLimitingAmount);
            break;
         case 2:
            this.bridgeContext.setAnimationLimitingFPS(this.animationLimitingAmount);
      }

   }

   protected JGVTComponent.Listener createListener() {
      return new SVGListener();
   }

   protected UserAgent createUserAgent() {
      return new BridgeUserAgent();
   }

   static {
      SVGFeatureStrings.addSupportedFeatureStrings(FEATURES);
   }

   protected class BridgeUserAgent implements UserAgent {
      protected Map extensions = new HashMap();

      public Dimension2D getViewportSize() {
         return JSVGComponent.this.getSize();
      }

      public EventDispatcher getEventDispatcher() {
         return JSVGComponent.this.eventDispatcher;
      }

      public void displayError(String message) {
         if (JSVGComponent.this.svgUserAgent != null) {
            JSVGComponent.this.svgUserAgent.displayError(message);
         }

      }

      public void displayError(Exception ex) {
         if (JSVGComponent.this.svgUserAgent != null) {
            JSVGComponent.this.svgUserAgent.displayError(ex);
         }

      }

      public void displayMessage(String message) {
         if (JSVGComponent.this.svgUserAgent != null) {
            JSVGComponent.this.svgUserAgent.displayMessage(message);
         }

      }

      public void showAlert(String message) {
         if (JSVGComponent.this.svgUserAgent != null) {
            JSVGComponent.this.svgUserAgent.showAlert(message);
         } else {
            JSVGComponent.this.showAlert(message);
         }
      }

      public String showPrompt(String message) {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.showPrompt(message) : JSVGComponent.this.showPrompt(message);
      }

      public String showPrompt(String message, String defaultValue) {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.showPrompt(message, defaultValue) : JSVGComponent.this.showPrompt(message, defaultValue);
      }

      public boolean showConfirm(String message) {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.showConfirm(message) : JSVGComponent.this.showConfirm(message);
      }

      public float getPixelUnitToMillimeter() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getPixelUnitToMillimeter() : 0.26458332F;
      }

      public float getPixelToMM() {
         return this.getPixelUnitToMillimeter();
      }

      public String getDefaultFontFamily() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getDefaultFontFamily() : "Arial, Helvetica, sans-serif";
      }

      public float getMediumFontSize() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getMediumFontSize() : 228.59999F / (72.0F * this.getPixelUnitToMillimeter());
      }

      public float getLighterFontWeight(float f) {
         if (JSVGComponent.this.svgUserAgent != null) {
            return JSVGComponent.this.svgUserAgent.getLighterFontWeight(f);
         } else {
            int weight = (int)((f + 50.0F) / 100.0F) * 100;
            switch (weight) {
               case 100:
                  return 100.0F;
               case 200:
                  return 100.0F;
               case 300:
                  return 200.0F;
               case 400:
                  return 300.0F;
               case 500:
                  return 400.0F;
               case 600:
                  return 400.0F;
               case 700:
                  return 400.0F;
               case 800:
                  return 400.0F;
               case 900:
                  return 400.0F;
               default:
                  throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
         }
      }

      public float getBolderFontWeight(float f) {
         if (JSVGComponent.this.svgUserAgent != null) {
            return JSVGComponent.this.svgUserAgent.getBolderFontWeight(f);
         } else {
            int weight = (int)((f + 50.0F) / 100.0F) * 100;
            switch (weight) {
               case 100:
                  return 600.0F;
               case 200:
                  return 600.0F;
               case 300:
                  return 600.0F;
               case 400:
                  return 600.0F;
               case 500:
                  return 600.0F;
               case 600:
                  return 700.0F;
               case 700:
                  return 800.0F;
               case 800:
                  return 900.0F;
               case 900:
                  return 900.0F;
               default:
                  throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
         }
      }

      public String getLanguages() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getLanguages() : "en";
      }

      public String getUserStyleSheetURI() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getUserStyleSheetURI() : null;
      }

      public void openLink(SVGAElement elt) {
         String show = XLinkSupport.getXLinkShow(elt);
         String href = elt.getHref().getAnimVal();
         ParsedURL newURIx;
         if (show.equals("new")) {
            this.fireLinkActivatedEvent(elt, href);
            if (JSVGComponent.this.svgUserAgent != null) {
               String oldURI = JSVGComponent.this.svgDocument.getURL();
               newURIx = null;
               if (elt.getOwnerDocument() != JSVGComponent.this.svgDocument) {
                  SVGDocument doc = (SVGDocument)elt.getOwnerDocument();
                  href = (new ParsedURL(doc.getURL(), href)).toString();
               }

               newURIx = new ParsedURL(oldURI, href);
               href = newURIx.toString();
               JSVGComponent.this.svgUserAgent.openLink(href, true);
            } else {
               JSVGComponent.this.loadSVGDocument(href);
            }

         } else {
            ParsedURL newURI = new ParsedURL(((SVGDocument)elt.getOwnerDocument()).getURL(), href);
            href = newURI.toString();
            if (JSVGComponent.this.svgDocument != null) {
               newURIx = new ParsedURL(JSVGComponent.this.svgDocument.getURL());
               if (newURI.sameFile(newURIx)) {
                  String s = newURI.getRef();
                  if (JSVGComponent.this.fragmentIdentifier != s && (s == null || !s.equals(JSVGComponent.this.fragmentIdentifier))) {
                     JSVGComponent.this.fragmentIdentifier = s;
                     if (JSVGComponent.this.computeRenderingTransform()) {
                        JSVGComponent.this.scheduleGVTRendering();
                     }
                  }

                  this.fireLinkActivatedEvent(elt, href);
                  return;
               }
            }

            this.fireLinkActivatedEvent(elt, href);
            if (JSVGComponent.this.svgUserAgent != null) {
               JSVGComponent.this.svgUserAgent.openLink(href, false);
            } else {
               JSVGComponent.this.loadSVGDocument(href);
            }

         }
      }

      protected void fireLinkActivatedEvent(SVGAElement elt, String href) {
         Object[] ll = JSVGComponent.this.linkActivationListeners.toArray();
         if (ll.length > 0) {
            LinkActivationEvent ev = new LinkActivationEvent(JSVGComponent.this, elt, href);
            Object[] var5 = ll;
            int var6 = ll.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Object aLl = var5[var7];
               LinkActivationListener l = (LinkActivationListener)aLl;
               l.linkActivated(ev);
            }
         }

      }

      public void setSVGCursor(Cursor cursor) {
         if (cursor != JSVGComponent.this.getCursor()) {
            JSVGComponent.this.setCursor(cursor);
         }

      }

      public void setTextSelection(Mark start, Mark end) {
         JSVGComponent.this.select(start, end);
      }

      public void deselectAll() {
         JSVGComponent.this.deselectAll();
      }

      public String getXMLParserClassName() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getXMLParserClassName() : XMLResourceDescriptor.getXMLParserClassName();
      }

      public boolean isXMLParserValidating() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.isXMLParserValidating() : false;
      }

      public AffineTransform getTransform() {
         return JSVGComponent.this.renderingTransform;
      }

      public void setTransform(AffineTransform at) {
         JSVGComponent.this.setRenderingTransform(at);
      }

      public String getMedia() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getMedia() : "screen";
      }

      public String getAlternateStyleSheet() {
         return JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getAlternateStyleSheet() : null;
      }

      public Point getClientAreaLocationOnScreen() {
         return JSVGComponent.this.getLocationOnScreen();
      }

      public boolean hasFeature(String s) {
         return JSVGComponent.FEATURES.contains(s);
      }

      public boolean supportExtension(String s) {
         return JSVGComponent.this.svgUserAgent != null && JSVGComponent.this.svgUserAgent.supportExtension(s) ? true : this.extensions.containsKey(s);
      }

      public void registerExtension(BridgeExtension ext) {
         Iterator i = ext.getImplementedExtensions();

         while(i.hasNext()) {
            this.extensions.put(i.next(), ext);
         }

      }

      public void handleElement(Element elt, Object data) {
         if (JSVGComponent.this.svgUserAgent != null) {
            JSVGComponent.this.svgUserAgent.handleElement(elt, data);
         }

      }

      public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
         return (ScriptSecurity)(JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getScriptSecurity(scriptType, scriptURL, docURL) : new DefaultScriptSecurity(scriptType, scriptURL, docURL));
      }

      public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) throws SecurityException {
         if (JSVGComponent.this.svgUserAgent != null) {
            JSVGComponent.this.svgUserAgent.checkLoadScript(scriptType, scriptURL, docURL);
         } else {
            ScriptSecurity s = this.getScriptSecurity(scriptType, scriptURL, docURL);
            if (s != null) {
               s.checkLoadScript();
            }
         }

      }

      public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL) {
         return (ExternalResourceSecurity)(JSVGComponent.this.svgUserAgent != null ? JSVGComponent.this.svgUserAgent.getExternalResourceSecurity(resourceURL, docURL) : new RelaxedExternalResourceSecurity(resourceURL, docURL));
      }

      public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) throws SecurityException {
         if (JSVGComponent.this.svgUserAgent != null) {
            JSVGComponent.this.svgUserAgent.checkLoadExternalResource(resourceURL, docURL);
         } else {
            ExternalResourceSecurity s = this.getExternalResourceSecurity(resourceURL, docURL);
            if (s != null) {
               s.checkLoadExternalResource();
            }
         }

      }

      public SVGDocument getBrokenLinkDocument(Element e, String url, String message) {
         Class cls = JSVGComponent.class;
         URL blURL = cls.getResource("resources/BrokenLink.svg");
         if (blURL == null) {
            throw new BridgeException(JSVGComponent.this.bridgeContext, e, "uri.image.broken", new Object[]{url, message});
         } else {
            DocumentLoader loader = JSVGComponent.this.bridgeContext.getDocumentLoader();
            SVGDocument doc = null;

            try {
               doc = (SVGDocument)loader.loadDocument(blURL.toString());
               if (doc == null) {
                  return doc;
               } else {
                  DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
                  doc = (SVGDocument)DOMUtilities.deepCloneDocument(doc, impl);
                  Element infoE = doc.getElementById("__More_About");
                  if (infoE == null) {
                     return doc;
                  } else {
                     Element titleE = doc.createElementNS("http://www.w3.org/2000/svg", "title");
                     String title = Messages.formatMessage("broken.link.title", (Object[])null);
                     titleE.appendChild(doc.createTextNode(title));
                     Element descE = doc.createElementNS("http://www.w3.org/2000/svg", "desc");
                     descE.appendChild(doc.createTextNode(message));
                     infoE.insertBefore(descE, infoE.getFirstChild());
                     infoE.insertBefore(titleE, descE);
                     return doc;
                  }
               }
            } catch (Exception var13) {
               throw new BridgeException(JSVGComponent.this.bridgeContext, e, var13, "uri.image.broken", new Object[]{url, message});
            }
         }
      }

      public void loadDocument(String url) {
         JSVGComponent.this.loadSVGDocument(url);
      }

      public FontFamilyResolver getFontFamilyResolver() {
         return DefaultFontFamilyResolver.SINGLETON;
      }
   }

   protected static class BridgeUserAgentWrapper implements UserAgent {
      protected UserAgent userAgent;

      public BridgeUserAgentWrapper(UserAgent ua) {
         this.userAgent = ua;
      }

      public EventDispatcher getEventDispatcher() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getEventDispatcher();
         } else {
            class Query implements Runnable {
               EventDispatcher result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getEventDispatcher();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public Dimension2D getViewportSize() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getViewportSize();
         } else {
            class Query implements Runnable {
               Dimension2D result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getViewportSize();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public void displayError(final Exception ex) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.displayError(ex);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.displayError(ex);
               }
            });
         }

      }

      public void displayMessage(final String message) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.displayMessage(message);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.displayMessage(message);
               }
            });
         }

      }

      public void showAlert(final String message) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.showAlert(message);
         } else {
            this.invokeAndWait(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.showAlert(message);
               }
            });
         }

      }

      public String showPrompt(final String message) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.showPrompt(message);
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.showPrompt(message);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public String showPrompt(final String message, final String defaultValue) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.showPrompt(message, defaultValue);
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.showPrompt(message, defaultValue);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public boolean showConfirm(final String message) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.showConfirm(message);
         } else {
            class Query implements Runnable {
               boolean result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.showConfirm(message);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public float getPixelUnitToMillimeter() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getPixelUnitToMillimeter();
         } else {
            class Query implements Runnable {
               float result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getPixelUnitToMillimeter();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public float getPixelToMM() {
         return this.getPixelUnitToMillimeter();
      }

      public String getDefaultFontFamily() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getDefaultFontFamily();
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getDefaultFontFamily();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public float getMediumFontSize() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getMediumFontSize();
         } else {
            class Query implements Runnable {
               float result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getMediumFontSize();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public float getLighterFontWeight(final float f) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getLighterFontWeight(f);
         } else {
            class Query implements Runnable {
               float result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getLighterFontWeight(f);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public float getBolderFontWeight(final float f) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getBolderFontWeight(f);
         } else {
            class Query implements Runnable {
               float result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getBolderFontWeight(f);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public String getLanguages() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getLanguages();
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getLanguages();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public String getUserStyleSheetURI() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getUserStyleSheetURI();
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getUserStyleSheetURI();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public void openLink(final SVGAElement elt) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.openLink(elt);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.openLink(elt);
               }
            });
         }

      }

      public void setSVGCursor(final Cursor cursor) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.setSVGCursor(cursor);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.setSVGCursor(cursor);
               }
            });
         }

      }

      public void setTextSelection(final Mark start, final Mark end) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.setTextSelection(start, end);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.setTextSelection(start, end);
               }
            });
         }

      }

      public void deselectAll() {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.deselectAll();
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.deselectAll();
               }
            });
         }

      }

      public String getXMLParserClassName() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getXMLParserClassName();
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getXMLParserClassName();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public boolean isXMLParserValidating() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.isXMLParserValidating();
         } else {
            class Query implements Runnable {
               boolean result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.isXMLParserValidating();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public AffineTransform getTransform() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getTransform();
         } else {
            class Query implements Runnable {
               AffineTransform result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getTransform();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public void setTransform(final AffineTransform at) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.setTransform(at);
         } else {
            class Query implements Runnable {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.setTransform(at);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
         }

      }

      public String getMedia() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getMedia();
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getMedia();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public String getAlternateStyleSheet() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getAlternateStyleSheet();
         } else {
            class Query implements Runnable {
               String result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getAlternateStyleSheet();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public Point getClientAreaLocationOnScreen() {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getClientAreaLocationOnScreen();
         } else {
            class Query implements Runnable {
               Point result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getClientAreaLocationOnScreen();
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public boolean hasFeature(final String s) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.hasFeature(s);
         } else {
            class Query implements Runnable {
               boolean result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.hasFeature(s);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public boolean supportExtension(final String s) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.supportExtension(s);
         } else {
            class Query implements Runnable {
               boolean result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.supportExtension(s);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public void registerExtension(final BridgeExtension ext) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.registerExtension(ext);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.registerExtension(ext);
               }
            });
         }

      }

      public void handleElement(final Element elt, final Object data) {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.handleElement(elt, data);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  BridgeUserAgentWrapper.this.userAgent.handleElement(elt, data);
               }
            });
         }

      }

      public ScriptSecurity getScriptSecurity(final String scriptType, final ParsedURL scriptPURL, final ParsedURL docPURL) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getScriptSecurity(scriptType, scriptPURL, docPURL);
         } else {
            class Query implements Runnable {
               ScriptSecurity result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getScriptSecurity(scriptType, scriptPURL, docPURL);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public void checkLoadScript(final String scriptType, final ParsedURL scriptPURL, final ParsedURL docPURL) throws SecurityException {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.checkLoadScript(scriptType, scriptPURL, docPURL);
         } else {
            class Query implements Runnable {
               SecurityException se = null;

               public void run() {
                  try {
                     BridgeUserAgentWrapper.this.userAgent.checkLoadScript(scriptType, scriptPURL, docPURL);
                  } catch (SecurityException var2) {
                     this.se = var2;
                  }

               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            if (q.se != null) {
               q.se.fillInStackTrace();
               throw q.se;
            }
         }

      }

      public ExternalResourceSecurity getExternalResourceSecurity(final ParsedURL resourcePURL, final ParsedURL docPURL) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getExternalResourceSecurity(resourcePURL, docPURL);
         } else {
            class Query implements Runnable {
               ExternalResourceSecurity result;

               public void run() {
                  this.result = BridgeUserAgentWrapper.this.userAgent.getExternalResourceSecurity(resourcePURL, docPURL);
               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            return q.result;
         }
      }

      public void checkLoadExternalResource(final ParsedURL resourceURL, final ParsedURL docURL) throws SecurityException {
         if (EventQueue.isDispatchThread()) {
            this.userAgent.checkLoadExternalResource(resourceURL, docURL);
         } else {
            class Query implements Runnable {
               SecurityException se;

               public void run() {
                  try {
                     BridgeUserAgentWrapper.this.userAgent.checkLoadExternalResource(resourceURL, docURL);
                  } catch (SecurityException var2) {
                     this.se = var2;
                  }

               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            if (q.se != null) {
               q.se.fillInStackTrace();
               throw q.se;
            }
         }

      }

      public SVGDocument getBrokenLinkDocument(final Element e, final String url, final String msg) {
         if (EventQueue.isDispatchThread()) {
            return this.userAgent.getBrokenLinkDocument(e, url, msg);
         } else {
            class Query implements Runnable {
               SVGDocument doc;
               RuntimeException rex = null;

               public void run() {
                  try {
                     this.doc = BridgeUserAgentWrapper.this.userAgent.getBrokenLinkDocument(e, url, msg);
                  } catch (RuntimeException var2) {
                     this.rex = var2;
                  }

               }
            }

            Query q = new Query();
            this.invokeAndWait(q);
            if (q.rex != null) {
               throw q.rex;
            } else {
               return q.doc;
            }
         }
      }

      protected void invokeAndWait(Runnable r) {
         try {
            EventQueue.invokeAndWait(r);
         } catch (Exception var3) {
         }

      }

      public void loadDocument(String url) {
         this.userAgent.loadDocument(url);
      }

      public FontFamilyResolver getFontFamilyResolver() {
         return this.userAgent.getFontFamilyResolver();
      }
   }

   protected class SVGListener extends JGVTComponent.Listener implements SVGDocumentLoaderListener, GVTTreeBuilderListener, SVGLoadEventDispatcherListener, UpdateManagerListener {
      protected SVGListener() {
         super();
      }

      public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
      }

      public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
         if (JSVGComponent.this.nextDocumentLoader != null) {
            JSVGComponent.this.startDocumentLoader();
         } else {
            JSVGComponent.this.documentLoader = null;
            if (JSVGComponent.this.afterStopRunnable != null) {
               EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
               JSVGComponent.this.afterStopRunnable = null;
            } else {
               JSVGComponent.this.setSVGDocument(e.getSVGDocument());
            }
         }
      }

      public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
         if (JSVGComponent.this.nextDocumentLoader != null) {
            JSVGComponent.this.startDocumentLoader();
         } else {
            JSVGComponent.this.documentLoader = null;
            if (JSVGComponent.this.afterStopRunnable != null) {
               EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
               JSVGComponent.this.afterStopRunnable = null;
            } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
               JSVGComponent.this.startGVTTreeBuilder();
            }
         }
      }

      public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
         if (JSVGComponent.this.nextDocumentLoader != null) {
            JSVGComponent.this.startDocumentLoader();
         } else {
            JSVGComponent.this.documentLoader = null;
            JSVGComponent.this.userAgent.displayError(((SVGDocumentLoader)e.getSource()).getException());
            if (JSVGComponent.this.afterStopRunnable != null) {
               EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
               JSVGComponent.this.afterStopRunnable = null;
            } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
               JSVGComponent.this.startGVTTreeBuilder();
            }
         }
      }

      public void gvtBuildStarted(GVTTreeBuilderEvent e) {
         JSVGComponent.this.removeJGVTComponentListener(JSVGComponent.this.jsvgComponentListener);
         JSVGComponent.this.removeComponentListener(JSVGComponent.this.jsvgComponentListener);
      }

      public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
         if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            JSVGComponent.this.startGVTTreeBuilder();
         } else {
            JSVGComponent.this.loader = null;
            JSVGComponent.this.gvtTreeBuilder = null;
            if (JSVGComponent.this.afterStopRunnable != null) {
               EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
               JSVGComponent.this.afterStopRunnable = null;
            } else if (JSVGComponent.this.nextDocumentLoader != null) {
               JSVGComponent.this.startDocumentLoader();
            } else {
               JSVGComponent.this.gvtRoot = null;
               if (JSVGComponent.this.isDynamicDocument && JSVGComponent.this.eventsEnabled) {
                  JSVGComponent.this.startSVGLoadEventDispatcher(e.getGVTRoot());
               } else {
                  if (JSVGComponent.this.isInteractiveDocument) {
                     JSVGComponent.this.nextUpdateManager = new UpdateManager(JSVGComponent.this.bridgeContext, e.getGVTRoot(), JSVGComponent.this.svgDocument);
                  }

                  JSVGComponent.this.setGraphicsNode(e.getGVTRoot(), false);
                  JSVGComponent.this.scheduleGVTRendering();
               }

            }
         }
      }

      public void gvtBuildCancelled(GVTTreeBuilderEvent e) {
         if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            JSVGComponent.this.startGVTTreeBuilder();
         } else {
            JSVGComponent.this.loader = null;
            JSVGComponent.this.gvtTreeBuilder = null;
            if (JSVGComponent.this.afterStopRunnable != null) {
               EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
               JSVGComponent.this.afterStopRunnable = null;
            } else if (JSVGComponent.this.nextDocumentLoader != null) {
               JSVGComponent.this.startDocumentLoader();
            } else {
               JSVGComponent.this.image = null;
               JSVGComponent.this.repaint();
            }
         }
      }

      public void gvtBuildFailed(GVTTreeBuilderEvent e) {
         if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            JSVGComponent.this.startGVTTreeBuilder();
         } else {
            JSVGComponent.this.loader = null;
            JSVGComponent.this.gvtTreeBuilder = null;
            if (JSVGComponent.this.afterStopRunnable != null) {
               EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
               JSVGComponent.this.afterStopRunnable = null;
            } else if (JSVGComponent.this.nextDocumentLoader != null) {
               JSVGComponent.this.startDocumentLoader();
            } else {
               GraphicsNode gn = e.getGVTRoot();
               if (gn == null) {
                  JSVGComponent.this.image = null;
                  JSVGComponent.this.repaint();
               } else {
                  JSVGComponent.this.setGraphicsNode(gn, false);
                  JSVGComponent.this.computeRenderingTransform();
               }

               JSVGComponent.this.userAgent.displayError(((GVTTreeBuilder)e.getSource()).getException());
            }
         }
      }

      public void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e) {
      }

      public void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e) {
         JSVGComponent.this.nextUpdateManager = JSVGComponent.this.svgLoadEventDispatcher.getUpdateManager();
         JSVGComponent.this.svgLoadEventDispatcher = null;
         if (JSVGComponent.this.afterStopRunnable != null) {
            JSVGComponent.this.nextUpdateManager.interrupt();
            JSVGComponent.this.nextUpdateManager = null;
            EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
            JSVGComponent.this.afterStopRunnable = null;
         } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            JSVGComponent.this.nextUpdateManager.interrupt();
            JSVGComponent.this.nextUpdateManager = null;
            JSVGComponent.this.startGVTTreeBuilder();
         } else if (JSVGComponent.this.nextDocumentLoader != null) {
            JSVGComponent.this.nextUpdateManager.interrupt();
            JSVGComponent.this.nextUpdateManager = null;
            JSVGComponent.this.startDocumentLoader();
         } else {
            JSVGComponent.this.setGraphicsNode(e.getGVTRoot(), false);
            JSVGComponent.this.scheduleGVTRendering();
         }
      }

      public void svgLoadEventDispatchCancelled(SVGLoadEventDispatcherEvent e) {
         JSVGComponent.this.nextUpdateManager = JSVGComponent.this.svgLoadEventDispatcher.getUpdateManager();
         JSVGComponent.this.svgLoadEventDispatcher = null;
         JSVGComponent.this.nextUpdateManager.interrupt();
         JSVGComponent.this.nextUpdateManager = null;
         if (JSVGComponent.this.afterStopRunnable != null) {
            EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
            JSVGComponent.this.afterStopRunnable = null;
         } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            JSVGComponent.this.startGVTTreeBuilder();
         } else if (JSVGComponent.this.nextDocumentLoader != null) {
            JSVGComponent.this.startDocumentLoader();
         }
      }

      public void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e) {
         JSVGComponent.this.nextUpdateManager = JSVGComponent.this.svgLoadEventDispatcher.getUpdateManager();
         JSVGComponent.this.svgLoadEventDispatcher = null;
         JSVGComponent.this.nextUpdateManager.interrupt();
         JSVGComponent.this.nextUpdateManager = null;
         if (JSVGComponent.this.afterStopRunnable != null) {
            EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
            JSVGComponent.this.afterStopRunnable = null;
         } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            JSVGComponent.this.startGVTTreeBuilder();
         } else if (JSVGComponent.this.nextDocumentLoader != null) {
            JSVGComponent.this.startDocumentLoader();
         } else {
            GraphicsNode gn = e.getGVTRoot();
            if (gn == null) {
               JSVGComponent.this.image = null;
               JSVGComponent.this.repaint();
            } else {
               JSVGComponent.this.setGraphicsNode(gn, false);
               JSVGComponent.this.computeRenderingTransform();
            }

            JSVGComponent.this.userAgent.displayError(((SVGLoadEventDispatcher)e.getSource()).getException());
         }
      }

      public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
         super.gvtRenderingCompleted(e);
         if (JSVGComponent.this.afterStopRunnable != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
            JSVGComponent.this.afterStopRunnable = null;
         } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            JSVGComponent.this.startGVTTreeBuilder();
         } else if (JSVGComponent.this.nextDocumentLoader != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            JSVGComponent.this.startDocumentLoader();
         } else {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.updateManager = JSVGComponent.this.nextUpdateManager;
               JSVGComponent.this.nextUpdateManager = null;
               JSVGComponent.this.updateManager.addUpdateManagerListener(this);
               JSVGComponent.this.updateManager.manageUpdates(JSVGComponent.this.renderer);
            }

         }
      }

      public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
         super.gvtRenderingCancelled(e);
         if (JSVGComponent.this.afterStopRunnable != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
            JSVGComponent.this.afterStopRunnable = null;
         } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            JSVGComponent.this.startGVTTreeBuilder();
         } else if (JSVGComponent.this.nextDocumentLoader != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            JSVGComponent.this.startDocumentLoader();
         }
      }

      public void gvtRenderingFailed(GVTTreeRendererEvent e) {
         super.gvtRenderingFailed(e);
         if (JSVGComponent.this.afterStopRunnable != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
            JSVGComponent.this.afterStopRunnable = null;
         } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            JSVGComponent.this.startGVTTreeBuilder();
         } else if (JSVGComponent.this.nextDocumentLoader != null) {
            if (JSVGComponent.this.nextUpdateManager != null) {
               JSVGComponent.this.nextUpdateManager.interrupt();
               JSVGComponent.this.nextUpdateManager = null;
            }

            JSVGComponent.this.startDocumentLoader();
         }
      }

      public void managerStarted(final UpdateManagerEvent e) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               JSVGComponent.this.suspendInteractions = false;
               Object[] dll = JSVGComponent.this.updateManagerListeners.toArray();
               if (dll.length > 0) {
                  Object[] var2 = dll;
                  int var3 = dll.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Object aDll = var2[var4];
                     ((UpdateManagerListener)aDll).managerStarted(e);
                  }
               }

            }
         });
      }

      public void managerSuspended(final UpdateManagerEvent e) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               Object[] dll = JSVGComponent.this.updateManagerListeners.toArray();
               if (dll.length > 0) {
                  Object[] var2 = dll;
                  int var3 = dll.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Object aDll = var2[var4];
                     ((UpdateManagerListener)aDll).managerSuspended(e);
                  }
               }

            }
         });
      }

      public void managerResumed(final UpdateManagerEvent e) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               Object[] dll = JSVGComponent.this.updateManagerListeners.toArray();
               if (dll.length > 0) {
                  Object[] var2 = dll;
                  int var3 = dll.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Object aDll = var2[var4];
                     ((UpdateManagerListener)aDll).managerResumed(e);
                  }
               }

            }
         });
      }

      public void managerStopped(final UpdateManagerEvent e) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               JSVGComponent.this.updateManager = null;
               Object[] dll = JSVGComponent.this.updateManagerListeners.toArray();
               if (dll.length > 0) {
                  Object[] var2 = dll;
                  int var3 = dll.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Object aDll = var2[var4];
                     ((UpdateManagerListener)aDll).managerStopped(e);
                  }
               }

               if (JSVGComponent.this.afterStopRunnable != null) {
                  EventQueue.invokeLater(JSVGComponent.this.afterStopRunnable);
                  JSVGComponent.this.afterStopRunnable = null;
               } else if (JSVGComponent.this.nextGVTTreeBuilder != null) {
                  JSVGComponent.this.startGVTTreeBuilder();
               } else if (JSVGComponent.this.nextDocumentLoader != null) {
                  JSVGComponent.this.startDocumentLoader();
               }
            }
         });
      }

      public void updateStarted(final UpdateManagerEvent e) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               if (!JSVGComponent.this.doubleBufferedRendering) {
                  JSVGComponent.this.image = e.getImage();
               }

               Object[] dll = JSVGComponent.this.updateManagerListeners.toArray();
               if (dll.length > 0) {
                  Object[] var2 = dll;
                  int var3 = dll.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Object aDll = var2[var4];
                     ((UpdateManagerListener)aDll).updateStarted(e);
                  }
               }

            }
         });
      }

      public void updateCompleted(final UpdateManagerEvent e) {
         try {
            EventQueue.invokeAndWait(new Runnable() {
               public void run() {
                  JSVGComponent.this.image = e.getImage();
                  if (e.getClearPaintingTransform()) {
                     JSVGComponent.this.paintingTransform = null;
                  }

                  List l = e.getDirtyAreas();
                  if (l != null) {
                     Iterator var2 = l.iterator();

                     while(var2.hasNext()) {
                        Object aL = var2.next();
                        Rectangle r = (Rectangle)aL;
                        if (JSVGComponent.this.updateOverlay != null) {
                           JSVGComponent.this.updateOverlay.addRect(r);
                           r = JSVGComponent.this.getRenderRect();
                        }

                        if (JSVGComponent.this.doubleBufferedRendering) {
                           JSVGComponent.this.repaint(r);
                        } else {
                           JSVGComponent.this.paintImmediately(r);
                        }
                     }

                     if (JSVGComponent.this.updateOverlay != null) {
                        JSVGComponent.this.updateOverlay.endUpdate();
                     }
                  }

                  JSVGComponent.this.suspendInteractions = false;
               }
            });
         } catch (Exception var3) {
         }

         EventQueue.invokeLater(new Runnable() {
            public void run() {
               Object[] dll = JSVGComponent.this.updateManagerListeners.toArray();
               if (dll.length > 0) {
                  Object[] var2 = dll;
                  int var3 = dll.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Object aDll = var2[var4];
                     ((UpdateManagerListener)aDll).updateCompleted(e);
                  }
               }

            }
         });
      }

      public void updateFailed(final UpdateManagerEvent e) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               Object[] dll = JSVGComponent.this.updateManagerListeners.toArray();
               if (dll.length > 0) {
                  Object[] var2 = dll;
                  int var3 = dll.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Object aDll = var2[var4];
                     ((UpdateManagerListener)aDll).updateFailed(e);
                  }
               }

            }
         });
      }

      protected void dispatchKeyTyped(final KeyEvent e) {
         if (!JSVGComponent.this.isDynamicDocument) {
            super.dispatchKeyTyped(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.keyTyped(e);
                  }
               });
            }

         }
      }

      protected void dispatchKeyPressed(final KeyEvent e) {
         if (!JSVGComponent.this.isDynamicDocument) {
            super.dispatchKeyPressed(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.keyPressed(e);
                  }
               });
            }

         }
      }

      protected void dispatchKeyReleased(final KeyEvent e) {
         if (!JSVGComponent.this.isDynamicDocument) {
            super.dispatchKeyReleased(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.keyReleased(e);
                  }
               });
            }

         }
      }

      protected void dispatchMouseClicked(final MouseEvent e) {
         if (!JSVGComponent.this.isInteractiveDocument) {
            super.dispatchMouseClicked(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.mouseClicked(e);
                  }
               });
            }

         }
      }

      protected void dispatchMousePressed(final MouseEvent e) {
         if (!JSVGComponent.this.isDynamicDocument) {
            super.dispatchMousePressed(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.mousePressed(e);
                  }
               });
            }

         }
      }

      protected void dispatchMouseReleased(final MouseEvent e) {
         if (!JSVGComponent.this.isDynamicDocument) {
            super.dispatchMouseReleased(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.mouseReleased(e);
                  }
               });
            }

         }
      }

      protected void dispatchMouseEntered(final MouseEvent e) {
         if (!JSVGComponent.this.isInteractiveDocument) {
            super.dispatchMouseEntered(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.mouseEntered(e);
                  }
               });
            }

         }
      }

      protected void dispatchMouseExited(final MouseEvent e) {
         if (!JSVGComponent.this.isInteractiveDocument) {
            super.dispatchMouseExited(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.mouseExited(e);
                  }
               });
            }

         }
      }

      protected void dispatchMouseDragged(MouseEvent e) {
         if (!JSVGComponent.this.isDynamicDocument) {
            super.dispatchMouseDragged(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               RunnableQueue rq = JSVGComponent.this.updateManager.getUpdateRunnableQueue();

               class MouseDraggedRunnable implements Runnable {
                  MouseEvent event;

                  MouseDraggedRunnable(MouseEvent evt) {
                     this.event = evt;
                  }

                  public void run() {
                     JSVGComponent.this.eventDispatcher.mouseDragged(this.event);
                  }
               }

               synchronized(rq.getIteratorLock()) {
                  Iterator it = rq.iterator();

                  while(true) {
                     if (!it.hasNext()) {
                        break;
                     }

                     Object next = it.next();
                     if (next instanceof MouseDraggedRunnable) {
                        MouseDraggedRunnable mdr = (MouseDraggedRunnable)next;
                        MouseEvent mev = mdr.event;
                        if (mev.getModifiersEx() == e.getModifiersEx()) {
                           mdr.event = e;
                        }

                        return;
                     }
                  }
               }

               rq.invokeLater(new MouseDraggedRunnable(e));
            }

         }
      }

      protected void dispatchMouseMoved(MouseEvent e) {
         if (!JSVGComponent.this.isInteractiveDocument) {
            super.dispatchMouseMoved(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               RunnableQueue rq = JSVGComponent.this.updateManager.getUpdateRunnableQueue();
               int i = 0;

               class MouseMovedRunnable implements Runnable {
                  MouseEvent event;

                  MouseMovedRunnable(MouseEvent evt) {
                     this.event = evt;
                  }

                  public void run() {
                     JSVGComponent.this.eventDispatcher.mouseMoved(this.event);
                  }
               }

               synchronized(rq.getIteratorLock()) {
                  Iterator it = rq.iterator();

                  while(true) {
                     if (!it.hasNext()) {
                        break;
                     }

                     Object next = it.next();
                     if (next instanceof MouseMovedRunnable) {
                        MouseMovedRunnable mmr = (MouseMovedRunnable)next;
                        MouseEvent mev = mmr.event;
                        if (mev.getModifiersEx() == e.getModifiersEx()) {
                           mmr.event = e;
                        }

                        return;
                     }

                     ++i;
                  }
               }

               rq.invokeLater(new MouseMovedRunnable(e));
            }

         }
      }

      protected void dispatchMouseWheelMoved(final MouseWheelEvent e) {
         if (!JSVGComponent.this.isInteractiveDocument) {
            super.dispatchMouseWheelMoved(e);
         } else {
            if (JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
               JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
                  public void run() {
                     JSVGComponent.this.eventDispatcher.mouseWheelMoved(e);
                  }
               });
            }

         }
      }
   }

   protected class JSVGComponentListener extends ComponentAdapter implements JGVTComponentListener {
      float prevScale = 0.0F;
      float prevTransX = 0.0F;
      float prevTransY = 0.0F;

      public void componentResized(ComponentEvent ce) {
         if (JSVGComponent.this.isDynamicDocument && JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
            JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
               public void run() {
                  try {
                     JSVGComponent.this.updateManager.dispatchSVGResizeEvent();
                  } catch (InterruptedException var2) {
                  }

               }
            });
         }

      }

      public void componentTransformChanged(ComponentEvent event) {
         AffineTransform at = JSVGComponent.this.getRenderingTransform();
         float currScale = (float)Math.sqrt(at.getDeterminant());
         float currTransX = (float)at.getTranslateX();
         float currTransY = (float)at.getTranslateY();
         final boolean dispatchZoom = currScale != this.prevScale;
         final boolean dispatchScroll = currTransX != this.prevTransX || currTransY != this.prevTransY;
         if (JSVGComponent.this.isDynamicDocument && JSVGComponent.this.updateManager != null && JSVGComponent.this.updateManager.isRunning()) {
            JSVGComponent.this.updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
               public void run() {
                  try {
                     if (dispatchZoom) {
                        JSVGComponent.this.updateManager.dispatchSVGZoomEvent();
                     }

                     if (dispatchScroll) {
                        JSVGComponent.this.updateManager.dispatchSVGScrollEvent();
                     }
                  } catch (InterruptedException var2) {
                  }

               }
            });
         }

         this.prevScale = currScale;
         this.prevTransX = currTransX;
         this.prevTransY = currTransY;
      }

      public void updateMatrix(AffineTransform at) {
         this.prevScale = (float)Math.sqrt(at.getDeterminant());
         this.prevTransX = (float)at.getTranslateX();
         this.prevTransY = (float)at.getTranslateY();
      }
   }
}
