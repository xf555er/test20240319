package org.apache.batik.transcoder;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.NoLoadExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadScriptSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.Rectangle2DKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGSVGElement;

public abstract class SVGAbstractTranscoder extends XMLAbstractTranscoder {
   public static final String DEFAULT_DEFAULT_FONT_FAMILY = "Arial, Helvetica, sans-serif";
   protected Rectangle2D curAOI;
   protected AffineTransform curTxf;
   protected GraphicsNode root;
   protected BridgeContext ctx;
   protected GVTBuilder builder;
   protected float width = 400.0F;
   protected float height = 400.0F;
   protected UserAgent userAgent = this.createUserAgent();
   public static final TranscodingHints.Key KEY_WIDTH = new LengthKey();
   public static final TranscodingHints.Key KEY_HEIGHT = new LengthKey();
   public static final TranscodingHints.Key KEY_MAX_WIDTH = new LengthKey();
   public static final TranscodingHints.Key KEY_MAX_HEIGHT = new LengthKey();
   public static final TranscodingHints.Key KEY_AOI = new Rectangle2DKey();
   public static final TranscodingHints.Key KEY_LANGUAGE = new StringKey();
   public static final TranscodingHints.Key KEY_MEDIA = new StringKey();
   public static final TranscodingHints.Key KEY_DEFAULT_FONT_FAMILY = new StringKey();
   public static final TranscodingHints.Key KEY_ALTERNATE_STYLESHEET = new StringKey();
   public static final TranscodingHints.Key KEY_USER_STYLESHEET_URI = new StringKey();
   public static final TranscodingHints.Key KEY_PIXEL_UNIT_TO_MILLIMETER = new FloatKey();
   /** @deprecated */
   public static final TranscodingHints.Key KEY_PIXEL_TO_MM;
   public static final TranscodingHints.Key KEY_EXECUTE_ONLOAD;
   public static final TranscodingHints.Key KEY_SNAPSHOT_TIME;
   public static final TranscodingHints.Key KEY_ALLOWED_SCRIPT_TYPES;
   public static final String DEFAULT_ALLOWED_SCRIPT_TYPES = "text/ecmascript, application/ecmascript, text/javascript, application/javascript, application/java-archive";
   public static final TranscodingHints.Key KEY_CONSTRAIN_SCRIPT_ORIGIN;
   public static final TranscodingHints.Key KEY_ALLOW_EXTERNAL_RESOURCES;

   protected SVGAbstractTranscoder() {
      this.hints.put(KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, "http://www.w3.org/2000/svg");
      this.hints.put(KEY_DOCUMENT_ELEMENT, "svg");
      this.hints.put(KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation());
      this.hints.put(KEY_MEDIA, "screen");
      this.hints.put(KEY_DEFAULT_FONT_FAMILY, "Arial, Helvetica, sans-serif");
      this.hints.put(KEY_EXECUTE_ONLOAD, Boolean.FALSE);
      this.hints.put(KEY_ALLOWED_SCRIPT_TYPES, "text/ecmascript, application/ecmascript, text/javascript, application/javascript, application/java-archive");
   }

   protected UserAgent createUserAgent() {
      return new SVGAbstractTranscoderUserAgent();
   }

   protected DocumentFactory createDocumentFactory(DOMImplementation domImpl, String parserClassname) {
      return new SAXSVGDocumentFactory(parserClassname);
   }

   public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
      super.transcode(input, output);
      if (this.ctx != null) {
         this.ctx.dispose();
      }

   }

   protected void transcode(Document document, String uri, TranscoderOutput output) throws TranscoderException {
      if (document != null && !(document.getImplementation() instanceof SVGDOMImplementation)) {
         DOMImplementation impl = (DOMImplementation)this.hints.get(KEY_DOM_IMPLEMENTATION);
         document = DOMUtilities.deepCloneDocument(document, impl);
         if (uri != null) {
            ParsedURL url = new ParsedURL(uri);
            ((SVGOMDocument)document).setParsedURL(url);
         }
      }

      if (this.hints.containsKey(KEY_WIDTH)) {
         this.width = (Float)this.hints.get(KEY_WIDTH);
      }

      if (this.hints.containsKey(KEY_HEIGHT)) {
         this.height = (Float)this.hints.get(KEY_HEIGHT);
      }

      SVGOMDocument svgDoc = (SVGOMDocument)document;
      SVGSVGElement root = svgDoc.getRootElement();
      this.ctx = this.createBridgeContext(svgDoc);
      this.builder = new GVTBuilder();
      boolean isDynamic = this.hints.containsKey(KEY_EXECUTE_ONLOAD) && (Boolean)this.hints.get(KEY_EXECUTE_ONLOAD);

      GraphicsNode gvtRoot;
      float docHeight;
      try {
         if (isDynamic) {
            this.ctx.setDynamicState(2);
         }

         gvtRoot = this.builder.build(this.ctx, (Document)svgDoc);
         if (this.ctx.isDynamic()) {
            BaseScriptingEnvironment se = new BaseScriptingEnvironment(this.ctx);
            se.loadScripts();
            se.dispatchSVGLoadEvent();
            if (this.hints.containsKey(KEY_SNAPSHOT_TIME)) {
               docHeight = (Float)this.hints.get(KEY_SNAPSHOT_TIME);
               this.ctx.getAnimationEngine().setCurrentTime(docHeight);
            } else if (this.ctx.isSVG12()) {
               docHeight = SVGUtilities.convertSnapshotTime(root, (BridgeContext)null);
               this.ctx.getAnimationEngine().setCurrentTime(docHeight);
            }
         }
      } catch (BridgeException var22) {
         throw new TranscoderException(var22);
      }

      float docWidth = (float)this.ctx.getDocumentSize().getWidth();
      docHeight = (float)this.ctx.getDocumentSize().getHeight();
      this.setImageSize(docWidth, docHeight);
      AffineTransform Px;
      if (this.hints.containsKey(KEY_AOI)) {
         Rectangle2D aoi = (Rectangle2D)this.hints.get(KEY_AOI);
         Px = new AffineTransform();
         double sx = (double)this.width / aoi.getWidth();
         double sy = (double)this.height / aoi.getHeight();
         double scale = Math.min(sx, sy);
         Px.scale(scale, scale);
         double tx = -aoi.getX() + ((double)this.width / scale - aoi.getWidth()) / 2.0;
         double ty = -aoi.getY() + ((double)this.height / scale - aoi.getHeight()) / 2.0;
         Px.translate(tx, ty);
         this.curAOI = aoi;
      } else {
         String ref = (new ParsedURL(uri)).getRef();
         String viewBox = root.getAttributeNS((String)null, "viewBox");
         if (ref != null && ref.length() != 0) {
            Px = ViewBox.getViewTransform(ref, root, this.width, this.height, this.ctx);
         } else if (viewBox != null && viewBox.length() != 0) {
            String aspectRatio = root.getAttributeNS((String)null, "preserveAspectRatio");
            Px = ViewBox.getPreserveAspectRatioTransform(root, (String)viewBox, (String)aspectRatio, this.width, this.height, this.ctx);
         } else {
            float xscale = this.width / docWidth;
            float yscale = this.height / docHeight;
            float scale = Math.min(xscale, yscale);
            Px = AffineTransform.getScaleInstance((double)scale, (double)scale);
         }

         this.curAOI = new Rectangle2D.Float(0.0F, 0.0F, this.width, this.height);
      }

      CanvasGraphicsNode cgn = this.getCanvasGraphicsNode(gvtRoot);
      if (cgn != null) {
         cgn.setViewingTransform(Px);
         this.curTxf = new AffineTransform();
      } else {
         this.curTxf = Px;
      }

      this.root = gvtRoot;
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

   protected BridgeContext createBridgeContext(SVGOMDocument doc) {
      return this.createBridgeContext(doc.isSVG12() ? "1.2" : "1.x");
   }

   protected BridgeContext createBridgeContext() {
      return this.createBridgeContext("1.x");
   }

   protected BridgeContext createBridgeContext(String svgVersion) {
      return (BridgeContext)("1.2".equals(svgVersion) ? new SVG12BridgeContext(this.userAgent) : new BridgeContext(this.userAgent));
   }

   protected void setImageSize(float docWidth, float docHeight) {
      float imgWidth = -1.0F;
      if (this.hints.containsKey(KEY_WIDTH)) {
         imgWidth = (Float)this.hints.get(KEY_WIDTH);
      }

      float imgHeight = -1.0F;
      if (this.hints.containsKey(KEY_HEIGHT)) {
         imgHeight = (Float)this.hints.get(KEY_HEIGHT);
      }

      if (imgWidth > 0.0F && imgHeight > 0.0F) {
         this.width = imgWidth;
         this.height = imgHeight;
      } else if (imgHeight > 0.0F) {
         this.width = docWidth * imgHeight / docHeight;
         this.height = imgHeight;
      } else if (imgWidth > 0.0F) {
         this.width = imgWidth;
         this.height = docHeight * imgWidth / docWidth;
      } else {
         this.width = docWidth;
         this.height = docHeight;
      }

      float imgMaxWidth = -1.0F;
      if (this.hints.containsKey(KEY_MAX_WIDTH)) {
         imgMaxWidth = (Float)this.hints.get(KEY_MAX_WIDTH);
      }

      float imgMaxHeight = -1.0F;
      if (this.hints.containsKey(KEY_MAX_HEIGHT)) {
         imgMaxHeight = (Float)this.hints.get(KEY_MAX_HEIGHT);
      }

      if (imgMaxHeight > 0.0F && this.height > imgMaxHeight) {
         this.width = docWidth * imgMaxHeight / docHeight;
         this.height = imgMaxHeight;
      }

      if (imgMaxWidth > 0.0F && this.width > imgMaxWidth) {
         this.width = imgMaxWidth;
         this.height = docHeight * imgMaxWidth / docWidth;
      }

   }

   static {
      KEY_PIXEL_TO_MM = KEY_PIXEL_UNIT_TO_MILLIMETER;
      KEY_EXECUTE_ONLOAD = new BooleanKey();
      KEY_SNAPSHOT_TIME = new FloatKey();
      KEY_ALLOWED_SCRIPT_TYPES = new StringKey();
      KEY_CONSTRAIN_SCRIPT_ORIGIN = new BooleanKey();
      KEY_ALLOW_EXTERNAL_RESOURCES = new BooleanKey();
   }

   protected class SVGAbstractTranscoderUserAgent extends UserAgentAdapter {
      protected List scripts;

      public SVGAbstractTranscoderUserAgent() {
         this.addStdFeatures();
      }

      public AffineTransform getTransform() {
         return SVGAbstractTranscoder.this.curTxf;
      }

      public void setTransform(AffineTransform at) {
         SVGAbstractTranscoder.this.curTxf = at;
      }

      public Dimension2D getViewportSize() {
         return new Dimension((int)SVGAbstractTranscoder.this.width, (int)SVGAbstractTranscoder.this.height);
      }

      public void displayError(String message) {
         try {
            SVGAbstractTranscoder.this.handler.error(new TranscoderException(message));
         } catch (TranscoderException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      public void displayError(Exception e) {
         try {
            e.printStackTrace();
            SVGAbstractTranscoder.this.handler.error(new TranscoderException(e));
         } catch (TranscoderException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      public void displayMessage(String message) {
         try {
            SVGAbstractTranscoder.this.handler.warning(new TranscoderException(message));
         } catch (TranscoderException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      public float getPixelUnitToMillimeter() {
         Object obj = SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER);
         return obj != null ? (Float)obj : super.getPixelUnitToMillimeter();
      }

      public String getLanguages() {
         return SVGAbstractTranscoder.this.hints.containsKey(SVGAbstractTranscoder.KEY_LANGUAGE) ? (String)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_LANGUAGE) : super.getLanguages();
      }

      public String getMedia() {
         String s = (String)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_MEDIA);
         return s != null ? s : super.getMedia();
      }

      public String getDefaultFontFamily() {
         String s = (String)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_DEFAULT_FONT_FAMILY);
         return s != null ? s : super.getDefaultFontFamily();
      }

      public String getAlternateStyleSheet() {
         String s = (String)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_ALTERNATE_STYLESHEET);
         return s != null ? s : super.getAlternateStyleSheet();
      }

      public String getUserStyleSheetURI() {
         String s = (String)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_USER_STYLESHEET_URI);
         return s != null ? s : super.getUserStyleSheetURI();
      }

      public String getXMLParserClassName() {
         String s = (String)SVGAbstractTranscoder.this.hints.get(XMLAbstractTranscoder.KEY_XML_PARSER_CLASSNAME);
         return s != null ? s : super.getXMLParserClassName();
      }

      public boolean isXMLParserValidating() {
         Boolean b = (Boolean)SVGAbstractTranscoder.this.hints.get(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING);
         return b != null ? b : super.isXMLParserValidating();
      }

      public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptPURL, ParsedURL docPURL) {
         if (this.scripts == null) {
            this.computeAllowedScripts();
         }

         if (!this.scripts.contains(scriptType)) {
            return new NoLoadScriptSecurity(scriptType);
         } else {
            boolean constrainOrigin = true;
            if (SVGAbstractTranscoder.this.hints.containsKey(SVGAbstractTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN)) {
               constrainOrigin = (Boolean)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN);
            }

            return (ScriptSecurity)(constrainOrigin ? new DefaultScriptSecurity(scriptType, scriptPURL, docPURL) : new RelaxedScriptSecurity(scriptType, scriptPURL, docPURL));
         }
      }

      protected void computeAllowedScripts() {
         this.scripts = new LinkedList();
         if (SVGAbstractTranscoder.this.hints.containsKey(SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES)) {
            String allowedScripts = (String)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES);
            StringTokenizer st = new StringTokenizer(allowedScripts, ",");

            while(st.hasMoreTokens()) {
               this.scripts.add(st.nextToken());
            }

         }
      }

      public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL) {
         return (ExternalResourceSecurity)(this.isAllowExternalResources() ? super.getExternalResourceSecurity(resourceURL, docURL) : new NoLoadExternalResourceSecurity());
      }

      public boolean isAllowExternalResources() {
         Boolean b = (Boolean)SVGAbstractTranscoder.this.hints.get(SVGAbstractTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES);
         return b != null ? b : true;
      }
   }
}
