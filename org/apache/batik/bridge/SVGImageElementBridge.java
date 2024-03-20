package org.apache.batik.bridge;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ICC_Profile;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.anim.dom.AbstractSVGAnimatedLength;
import org.apache.batik.anim.dom.AnimatedLiveAttributeValue;
import org.apache.batik.anim.dom.SVGOMAnimatedPreserveAspectRatio;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGOMElement;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.spi.BrokenLinkProvider;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ImageNode;
import org.apache.batik.gvt.RasterImageNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.HaltingThread;
import org.apache.batik.util.MimeTypeConstants;
import org.apache.batik.util.ParsedURL;
import org.apache.xmlgraphics.java2d.color.ICCColorSpaceWithIntent;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGImageElement;
import org.w3c.dom.svg.SVGSVGElement;

public class SVGImageElementBridge extends AbstractGraphicsNodeBridge {
   protected SVGDocument imgDocument;
   protected EventListener listener = null;
   protected BridgeContext subCtx = null;
   protected boolean hitCheckChildren = false;
   static SVGBrokenLinkProvider brokenLinkProvider = new SVGBrokenLinkProvider();

   public String getLocalName() {
      return "image";
   }

   public Bridge getInstance() {
      return new SVGImageElementBridge();
   }

   public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
      ImageNode imageNode = (ImageNode)super.createGraphicsNode(ctx, e);
      if (imageNode == null) {
         return null;
      } else {
         this.associateSVGContext(ctx, e, imageNode);
         this.hitCheckChildren = false;
         GraphicsNode node = this.buildImageGraphicsNode(ctx, e);
         if (node == null) {
            SVGImageElement ie = (SVGImageElement)e;
            String uriStr = ie.getHref().getAnimVal();
            throw new BridgeException(ctx, e, "uri.image.invalid", new Object[]{uriStr});
         } else {
            imageNode.setImage(node);
            imageNode.setHitCheckChildren(this.hitCheckChildren);
            RenderingHints hints = null;
            hints = CSSUtilities.convertImageRendering(e, hints);
            hints = CSSUtilities.convertColorRendering(e, hints);
            if (hints != null) {
               imageNode.setRenderingHints(hints);
            }

            return imageNode;
         }
      }
   }

   protected GraphicsNode buildImageGraphicsNode(BridgeContext ctx, Element e) {
      SVGImageElement ie = (SVGImageElement)e;
      String uriStr = ie.getHref().getAnimVal();
      if (uriStr.length() == 0) {
         throw new BridgeException(ctx, e, "attribute.missing", new Object[]{"xlink:href"});
      } else if (uriStr.indexOf(35) != -1) {
         throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"xlink:href", uriStr});
      } else {
         String baseURI = AbstractNode.getBaseURI(e);
         ParsedURL purl;
         if (baseURI == null) {
            purl = new ParsedURL(uriStr);
         } else {
            purl = new ParsedURL(baseURI, uriStr);
         }

         return this.createImageGraphicsNode(ctx, e, purl);
      }
   }

   protected GraphicsNode createImageGraphicsNode(BridgeContext ctx, Element e, ParsedURL purl) {
      Rectangle2D bounds = getImageBounds(ctx, e);
      if (bounds.getWidth() != 0.0 && bounds.getHeight() != 0.0) {
         SVGDocument svgDoc = (SVGDocument)e.getOwnerDocument();
         String docURL = svgDoc.getURL();
         ParsedURL pDocURL = null;
         if (docURL != null) {
            pDocURL = new ParsedURL(docURL);
         }

         UserAgent userAgent = ctx.getUserAgent();

         try {
            userAgent.checkLoadExternalResource(purl, pDocURL);
         } catch (SecurityException var41) {
            throw new BridgeException(ctx, e, var41, "uri.unsecure", new Object[]{purl});
         }

         DocumentLoader loader = ctx.getDocumentLoader();
         ImageTagRegistry reg = ImageTagRegistry.getRegistry();
         ICCColorSpaceWithIntent colorspace = extractColorSpace(e, ctx);

         Document reference;
         try {
            reference = loader.checkCache(purl.toString());
            if (reference != null) {
               this.imgDocument = (SVGDocument)reference;
               return this.createSVGImageNode(ctx, e, this.imgDocument);
            }
         } catch (BridgeException var39) {
            throw var39;
         } catch (Exception var40) {
         }

         Filter img = reg.checkCache(purl, colorspace);
         if (img != null) {
            return this.createRasterImageNode(ctx, e, img, purl);
         } else {
            reference = null;

            ProtectedStream reference;
            try {
               reference = this.openStream(e, purl);
            } catch (SecurityException var37) {
               throw new BridgeException(ctx, e, var37, "uri.unsecure", new Object[]{purl});
            } catch (IOException var38) {
               return this.createBrokenImageNode(ctx, e, purl.toString(), var38.getLocalizedMessage());
            }

            Filter img = reg.readURL(reference, purl, colorspace, false, false);
            if (img != null) {
               try {
                  reference.tie();
               } catch (IOException var34) {
               }

               return this.createRasterImageNode(ctx, e, img, purl);
            } else {
               try {
                  reference.retry();
               } catch (IOException var36) {
                  reference.release();
                  reference = null;

                  try {
                     reference = this.openStream(e, purl);
                  } catch (IOException var35) {
                     return this.createBrokenImageNode(ctx, e, purl.toString(), var35.getLocalizedMessage());
                  }
               }

               try {
                  Document doc = loader.loadDocument(purl.toString(), reference);
                  reference.release();
                  this.imgDocument = (SVGDocument)doc;
                  return this.createSVGImageNode(ctx, e, this.imgDocument);
               } catch (BridgeException var43) {
                  reference.release();
                  throw var43;
               } catch (SecurityException var44) {
                  reference.release();
                  throw new BridgeException(ctx, e, var44, "uri.unsecure", new Object[]{purl});
               } catch (InterruptedIOException var45) {
                  reference.release();
                  if (HaltingThread.hasBeenHalted()) {
                     throw new InterruptedBridgeException();
                  }
               } catch (InterruptedBridgeException var46) {
                  reference.release();
                  throw var46;
               } catch (Exception var47) {
               }

               try {
                  reference.retry();
               } catch (IOException var33) {
                  reference.release();
                  reference = null;

                  try {
                     reference = this.openStream(e, purl);
                  } catch (IOException var32) {
                     return this.createBrokenImageNode(ctx, e, purl.toString(), var32.getLocalizedMessage());
                  }
               }

               try {
                  img = reg.readURL(reference, purl, colorspace, true, true);
                  if (img != null) {
                     GraphicsNode var14 = this.createRasterImageNode(ctx, e, img, purl);
                     return var14;
                  }
               } finally {
                  reference.release();
               }

               return null;
            }
         }
      } else {
         ShapeNode sn = new ShapeNode();
         sn.setShape(bounds);
         return sn;
      }
   }

   protected ProtectedStream openStream(Element e, ParsedURL purl) throws IOException {
      List mimeTypes = new ArrayList(ImageTagRegistry.getRegistry().getRegisteredMimeTypes());
      mimeTypes.addAll(MimeTypeConstants.MIME_TYPES_SVG_LIST);
      InputStream reference = purl.openStream(mimeTypes.iterator());
      return new ProtectedStream(reference);
   }

   protected GraphicsNode instantiateGraphicsNode() {
      return new ImageNode();
   }

   public boolean isComposite() {
      return false;
   }

   protected void initializeDynamicSupport(BridgeContext ctx, Element e, GraphicsNode node) {
      if (ctx.isInteractive()) {
         ctx.bind(e, node);
         if (ctx.isDynamic()) {
            this.e = e;
            this.node = node;
            this.ctx = ctx;
            ((SVGOMElement)e).setSVGContext(this);
         }

      }
   }

   public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
      try {
         String ns = alav.getNamespaceURI();
         String ln = alav.getLocalName();
         if (ns == null) {
            label50: {
               if (!ln.equals("x") && !ln.equals("y")) {
                  if (!ln.equals("width") && !ln.equals("height")) {
                     if (ln.equals("preserveAspectRatio")) {
                        this.updateImageBounds();
                        return;
                     }
                     break label50;
                  }

                  SVGImageElement ie = (SVGImageElement)this.e;
                  ImageNode imageNode = (ImageNode)this.node;
                  AbstractSVGAnimatedLength _attr;
                  if (ln.charAt(0) == 'w') {
                     _attr = (AbstractSVGAnimatedLength)ie.getWidth();
                  } else {
                     _attr = (AbstractSVGAnimatedLength)ie.getHeight();
                  }

                  float val = _attr.getCheckedValue();
                  if (val != 0.0F && !(imageNode.getImage() instanceof ShapeNode)) {
                     this.updateImageBounds();
                  } else {
                     this.rebuildImageNode();
                  }

                  return;
               }

               this.updateImageBounds();
               return;
            }
         } else if (ns.equals("http://www.w3.org/1999/xlink") && ln.equals("href")) {
            this.rebuildImageNode();
            return;
         }
      } catch (LiveAttributeException var8) {
         throw new BridgeException(this.ctx, var8);
      }

      super.handleAnimatedAttributeChanged(alav);
   }

   protected void updateImageBounds() {
      Rectangle2D bounds = getImageBounds(this.ctx, this.e);
      GraphicsNode imageNode = ((ImageNode)this.node).getImage();
      float[] vb = null;
      if (imageNode instanceof RasterImageNode) {
         Rectangle2D imgBounds = ((RasterImageNode)imageNode).getImageBounds();
         vb = new float[]{0.0F, 0.0F, (float)imgBounds.getWidth(), (float)imgBounds.getHeight()};
      } else if (this.imgDocument != null) {
         Element svgElement = this.imgDocument.getRootElement();
         String viewBox = svgElement.getAttributeNS((String)null, "viewBox");
         vb = ViewBox.parseViewBoxAttribute(this.e, viewBox, this.ctx);
      }

      if (imageNode != null) {
         initializeViewport(this.ctx, this.e, imageNode, vb, bounds);
      }

   }

   protected void rebuildImageNode() {
      if (this.imgDocument != null && this.listener != null) {
         NodeEventTarget tgt = (NodeEventTarget)this.imgDocument.getRootElement();
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "click", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keydown", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keypress", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keyup", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mousedown", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mousemove", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseup", this.listener, false);
         this.listener = null;
      }

      if (this.imgDocument != null) {
         SVGSVGElement svgElement = this.imgDocument.getRootElement();
         disposeTree(svgElement);
      }

      this.imgDocument = null;
      this.subCtx = null;
      GraphicsNode inode = this.buildImageGraphicsNode(this.ctx, this.e);
      ImageNode imgNode = (ImageNode)this.node;
      imgNode.setImage(inode);
      if (inode == null) {
         SVGImageElement ie = (SVGImageElement)this.e;
         String uriStr = ie.getHref().getAnimVal();
         throw new BridgeException(this.ctx, this.e, "uri.image.invalid", new Object[]{uriStr});
      }
   }

   protected void handleCSSPropertyChanged(int property) {
      switch (property) {
         case 6:
         case 30:
            RenderingHints hints = CSSUtilities.convertImageRendering(this.e, (RenderingHints)null);
            hints = CSSUtilities.convertColorRendering(this.e, hints);
            if (hints != null) {
               this.node.setRenderingHints(hints);
            }
            break;
         default:
            super.handleCSSPropertyChanged(property);
      }

   }

   protected GraphicsNode createRasterImageNode(BridgeContext ctx, Element e, Filter img, ParsedURL purl) {
      Rectangle2D bounds = getImageBounds(ctx, e);
      if (bounds.getWidth() != 0.0 && bounds.getHeight() != 0.0) {
         if (BrokenLinkProvider.hasBrokenLinkProperty(img)) {
            Object o = img.getProperty("org.apache.batik.BrokenLinkImage");
            String msg = "unknown";
            if (o instanceof String) {
               msg = (String)o;
            }

            SVGDocument doc = ctx.getUserAgent().getBrokenLinkDocument(e, purl.toString(), msg);
            return this.createSVGImageNode(ctx, e, doc);
         } else {
            RasterImageNode node = new RasterImageNode();
            node.setImage(img);
            Rectangle2D imgBounds = img.getBounds2D();
            float[] vb = new float[]{0.0F, 0.0F, (float)imgBounds.getWidth(), (float)imgBounds.getHeight()};
            initializeViewport(ctx, e, node, vb, bounds);
            return node;
         }
      } else {
         ShapeNode sn = new ShapeNode();
         sn.setShape(bounds);
         return sn;
      }
   }

   protected GraphicsNode createSVGImageNode(BridgeContext ctx, Element e, SVGDocument imgDocument) {
      CSSEngine eng = ((SVGOMDocument)imgDocument).getCSSEngine();
      this.subCtx = ctx.createSubBridgeContext((SVGOMDocument)imgDocument);
      CompositeGraphicsNode result = new CompositeGraphicsNode();
      Rectangle2D bounds = getImageBounds(ctx, e);
      if (bounds.getWidth() != 0.0 && bounds.getHeight() != 0.0) {
         Rectangle2D r = CSSUtilities.convertEnableBackground(e);
         if (r != null) {
            result.setBackgroundEnable(r);
         }

         SVGSVGElement svgElement = imgDocument.getRootElement();
         CanvasGraphicsNode node = (CanvasGraphicsNode)this.subCtx.getGVTBuilder().build(this.subCtx, (Element)svgElement);
         if (eng == null && ctx.isInteractive()) {
            this.subCtx.addUIEventListeners(imgDocument);
         }

         node.setClip((ClipRable)null);
         node.setViewingTransform(new AffineTransform());
         result.getChildren().add(node);
         String viewBox = svgElement.getAttributeNS((String)null, "viewBox");
         float[] vb = ViewBox.parseViewBoxAttribute(e, viewBox, ctx);
         initializeViewport(ctx, e, result, vb, bounds);
         if (ctx.isInteractive()) {
            this.listener = new ForwardEventListener(svgElement, e);
            NodeEventTarget tgt = (NodeEventTarget)svgElement;
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "click", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "click", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "keydown", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "keydown", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "keypress", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "keypress", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "keyup", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "keyup", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "mousedown", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "mousedown", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "mousemove", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "mousemove", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "mouseout", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "mouseover", this.listener, false);
            tgt.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseup", this.listener, false, (Object)null);
            this.subCtx.storeEventListenerNS(tgt, "http://www.w3.org/2001/xml-events", "mouseup", this.listener, false);
         }

         return result;
      } else {
         ShapeNode sn = new ShapeNode();
         sn.setShape(bounds);
         result.getChildren().add(sn);
         return result;
      }
   }

   public void dispose() {
      if (this.imgDocument != null && this.listener != null) {
         NodeEventTarget tgt = (NodeEventTarget)this.imgDocument.getRootElement();
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "click", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keydown", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keypress", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "keyup", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mousedown", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mousemove", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.listener, false);
         tgt.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseup", this.listener, false);
         this.listener = null;
      }

      if (this.imgDocument != null) {
         SVGSVGElement svgElement = this.imgDocument.getRootElement();
         disposeTree(svgElement);
         this.imgDocument = null;
         this.subCtx = null;
      }

      super.dispose();
   }

   protected static void initializeViewport(BridgeContext ctx, Element e, GraphicsNode node, float[] vb, Rectangle2D bounds) {
      float x = (float)bounds.getX();
      float y = (float)bounds.getY();
      float w = (float)bounds.getWidth();
      float h = (float)bounds.getHeight();

      try {
         SVGImageElement ie = (SVGImageElement)e;
         SVGOMAnimatedPreserveAspectRatio _par = (SVGOMAnimatedPreserveAspectRatio)ie.getPreserveAspectRatio();
         _par.check();
         AffineTransform at = ViewBox.getPreserveAspectRatioTransform(e, vb, w, h, _par, ctx);
         at.preConcatenate(AffineTransform.getTranslateInstance((double)x, (double)y));
         node.setTransform(at);
         Shape clip = null;
         if (CSSUtilities.convertOverflow(e)) {
            float[] offsets = CSSUtilities.convertClip(e);
            if (offsets == null) {
               clip = new Rectangle2D.Float(x, y, w, h);
            } else {
               clip = new Rectangle2D.Float(x + offsets[3], y + offsets[0], w - offsets[1] - offsets[3], h - offsets[2] - offsets[0]);
            }
         }

         if (clip != null) {
            try {
               at = at.createInverse();
               Filter filter = node.getGraphicsNodeRable(true);
               Shape clip = at.createTransformedShape(clip);
               node.setClip(new ClipRable8Bit(filter, clip));
            } catch (NoninvertibleTransformException var14) {
            }
         }

      } catch (LiveAttributeException var15) {
         throw new BridgeException(ctx, var15);
      }
   }

   protected static ICCColorSpaceWithIntent extractColorSpace(Element element, BridgeContext ctx) {
      String colorProfileProperty = CSSUtilities.getComputedStyle(element, 8).getStringValue();
      ICCColorSpaceWithIntent colorSpace = null;
      if ("srgb".equalsIgnoreCase(colorProfileProperty)) {
         colorSpace = new ICCColorSpaceWithIntent(ICC_Profile.getInstance(1000), RenderingIntent.AUTO, "sRGB", (String)null);
      } else if (!"auto".equalsIgnoreCase(colorProfileProperty) && !"".equalsIgnoreCase(colorProfileProperty)) {
         SVGColorProfileElementBridge profileBridge = (SVGColorProfileElementBridge)ctx.getBridge("http://www.w3.org/2000/svg", "color-profile");
         if (profileBridge != null) {
            colorSpace = profileBridge.createICCColorSpaceWithIntent(ctx, element, colorProfileProperty);
         }
      }

      return colorSpace;
   }

   protected static Rectangle2D getImageBounds(BridgeContext ctx, Element element) {
      try {
         SVGImageElement ie = (SVGImageElement)element;
         AbstractSVGAnimatedLength _x = (AbstractSVGAnimatedLength)ie.getX();
         float x = _x.getCheckedValue();
         AbstractSVGAnimatedLength _y = (AbstractSVGAnimatedLength)ie.getY();
         float y = _y.getCheckedValue();
         AbstractSVGAnimatedLength _width = (AbstractSVGAnimatedLength)ie.getWidth();
         float w = _width.getCheckedValue();
         AbstractSVGAnimatedLength _height = (AbstractSVGAnimatedLength)ie.getHeight();
         float h = _height.getCheckedValue();
         return new Rectangle2D.Float(x, y, w, h);
      } catch (LiveAttributeException var11) {
         throw new BridgeException(ctx, var11);
      }
   }

   GraphicsNode createBrokenImageNode(BridgeContext ctx, Element e, String uri, String message) {
      SVGDocument doc = ctx.getUserAgent().getBrokenLinkDocument(e, uri, Messages.formatMessage("uri.image.error", new Object[]{message}));
      return this.createSVGImageNode(ctx, e, doc);
   }

   static {
      ImageTagRegistry.setBrokenLinkProvider(brokenLinkProvider);
   }

   protected static class ForwardEventListener implements EventListener {
      protected Element svgElement;
      protected Element imgElement;

      public ForwardEventListener(Element svgElement, Element imgElement) {
         this.svgElement = svgElement;
         this.imgElement = imgElement;
      }

      public void handleEvent(Event e) {
         DOMMouseEvent evt = (DOMMouseEvent)e;
         DOMMouseEvent newMouseEvent = (DOMMouseEvent)((DocumentEvent)this.imgElement.getOwnerDocument()).createEvent("MouseEvents");
         newMouseEvent.initMouseEventNS("http://www.w3.org/2001/xml-events", evt.getType(), evt.getBubbles(), evt.getCancelable(), evt.getView(), evt.getDetail(), evt.getScreenX(), evt.getScreenY(), evt.getClientX(), evt.getClientY(), evt.getButton(), (EventTarget)this.imgElement, evt.getModifiersString());
         ((EventTarget)this.imgElement).dispatchEvent(newMouseEvent);
      }
   }

   public static class ProtectedStream extends BufferedInputStream {
      static final int BUFFER_SIZE = 8192;
      boolean wasClosed = false;
      boolean isTied = false;

      ProtectedStream(InputStream is) {
         super(is, 8192);
         super.mark(8192);
      }

      ProtectedStream(InputStream is, int size) {
         super(is, size);
         super.mark(size);
      }

      public boolean markSupported() {
         return false;
      }

      public void mark(int sz) {
      }

      public void reset() throws IOException {
         throw new IOException("Reset unsupported");
      }

      public synchronized void retry() throws IOException {
         super.reset();
         this.wasClosed = false;
         this.isTied = false;
      }

      public synchronized void close() throws IOException {
         this.wasClosed = true;
         if (this.isTied) {
            super.close();
         }

      }

      public synchronized void tie() throws IOException {
         this.isTied = true;
         if (this.wasClosed) {
            super.close();
         }

      }

      public void release() {
         try {
            super.close();
         } catch (IOException var2) {
         }

      }
   }
}
