package org.apache.batik.anim.dom;

import java.awt.geom.AffineTransform;
import java.util.List;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.AbstractSVGMatrix;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGOMAngle;
import org.apache.batik.dom.svg.SVGOMPoint;
import org.apache.batik.dom.svg.SVGOMRect;
import org.apache.batik.dom.svg.SVGOMTransform;
import org.apache.batik.dom.svg.SVGSVGContext;
import org.apache.batik.dom.svg.SVGTestsSupport;
import org.apache.batik.dom.svg.SVGZoomAndPanSupport;
import org.apache.batik.dom.util.ListNodeList;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.stylesheets.DocumentStyle;
import org.w3c.dom.stylesheets.StyleSheetList;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGStringList;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGViewSpec;
import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

public class SVGOMSVGElement extends SVGStylableElement implements SVGSVGElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final AttributeInitializer attributeInitializer;
   protected SVGOMAnimatedLength x;
   protected SVGOMAnimatedLength y;
   protected SVGOMAnimatedLength width;
   protected SVGOMAnimatedLength height;
   protected SVGOMAnimatedBoolean externalResourcesRequired;
   protected SVGOMAnimatedPreserveAspectRatio preserveAspectRatio;
   protected SVGOMAnimatedRect viewBox;

   protected SVGOMSVGElement() {
   }

   public SVGOMSVGElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.x = this.createLiveAnimatedLength((String)null, "x", "0", (short)2, false);
      this.y = this.createLiveAnimatedLength((String)null, "y", "0", (short)1, false);
      this.width = this.createLiveAnimatedLength((String)null, "width", "100%", (short)2, true);
      this.height = this.createLiveAnimatedLength((String)null, "height", "100%", (short)1, true);
      this.externalResourcesRequired = this.createLiveAnimatedBoolean((String)null, "externalResourcesRequired", false);
      this.preserveAspectRatio = this.createLiveAnimatedPreserveAspectRatio();
      this.viewBox = this.createLiveAnimatedRect((String)null, "viewBox", (String)null);
   }

   public String getLocalName() {
      return "svg";
   }

   public SVGAnimatedLength getX() {
      return this.x;
   }

   public SVGAnimatedLength getY() {
      return this.y;
   }

   public SVGAnimatedLength getWidth() {
      return this.width;
   }

   public SVGAnimatedLength getHeight() {
      return this.height;
   }

   public String getContentScriptType() {
      return this.getAttributeNS((String)null, "contentScriptType");
   }

   public void setContentScriptType(String type) {
      this.setAttributeNS((String)null, "contentScriptType", type);
   }

   public String getContentStyleType() {
      return this.getAttributeNS((String)null, "contentStyleType");
   }

   public void setContentStyleType(String type) {
      this.setAttributeNS((String)null, "contentStyleType", type);
   }

   public SVGRect getViewport() {
      SVGContext ctx = this.getSVGContext();
      return new SVGOMRect(0.0F, 0.0F, ctx.getViewportWidth(), ctx.getViewportHeight());
   }

   public float getPixelUnitToMillimeterX() {
      return this.getSVGContext().getPixelUnitToMillimeter();
   }

   public float getPixelUnitToMillimeterY() {
      return this.getSVGContext().getPixelUnitToMillimeter();
   }

   public float getScreenPixelToMillimeterX() {
      return this.getSVGContext().getPixelUnitToMillimeter();
   }

   public float getScreenPixelToMillimeterY() {
      return this.getSVGContext().getPixelUnitToMillimeter();
   }

   public boolean getUseCurrentView() {
      throw new UnsupportedOperationException("SVGSVGElement.getUseCurrentView is not implemented");
   }

   public void setUseCurrentView(boolean useCurrentView) throws DOMException {
      throw new UnsupportedOperationException("SVGSVGElement.setUseCurrentView is not implemented");
   }

   public SVGViewSpec getCurrentView() {
      throw new UnsupportedOperationException("SVGSVGElement.getCurrentView is not implemented");
   }

   public float getCurrentScale() {
      AffineTransform scrnTrans = this.getSVGContext().getScreenTransform();
      return scrnTrans != null ? (float)Math.sqrt(scrnTrans.getDeterminant()) : 1.0F;
   }

   public void setCurrentScale(float currentScale) throws DOMException {
      SVGContext context = this.getSVGContext();
      AffineTransform scrnTrans = context.getScreenTransform();
      float scale = 1.0F;
      if (scrnTrans != null) {
         scale = (float)Math.sqrt(scrnTrans.getDeterminant());
      }

      float delta = currentScale / scale;
      scrnTrans = new AffineTransform(scrnTrans.getScaleX() * (double)delta, scrnTrans.getShearY() * (double)delta, scrnTrans.getShearX() * (double)delta, scrnTrans.getScaleY() * (double)delta, scrnTrans.getTranslateX(), scrnTrans.getTranslateY());
      context.setScreenTransform(scrnTrans);
   }

   public SVGPoint getCurrentTranslate() {
      return new SVGPoint() {
         protected AffineTransform getScreenTransform() {
            SVGContext context = SVGOMSVGElement.this.getSVGContext();
            return context.getScreenTransform();
         }

         public float getX() {
            AffineTransform scrnTrans = this.getScreenTransform();
            return (float)scrnTrans.getTranslateX();
         }

         public float getY() {
            AffineTransform scrnTrans = this.getScreenTransform();
            return (float)scrnTrans.getTranslateY();
         }

         public void setX(float newX) {
            SVGContext context = SVGOMSVGElement.this.getSVGContext();
            AffineTransform scrnTrans = context.getScreenTransform();
            scrnTrans = new AffineTransform(scrnTrans.getScaleX(), scrnTrans.getShearY(), scrnTrans.getShearX(), scrnTrans.getScaleY(), (double)newX, scrnTrans.getTranslateY());
            context.setScreenTransform(scrnTrans);
         }

         public void setY(float newY) {
            SVGContext context = SVGOMSVGElement.this.getSVGContext();
            AffineTransform scrnTrans = context.getScreenTransform();
            scrnTrans = new AffineTransform(scrnTrans.getScaleX(), scrnTrans.getShearY(), scrnTrans.getShearX(), scrnTrans.getScaleY(), scrnTrans.getTranslateX(), (double)newY);
            context.setScreenTransform(scrnTrans);
         }

         public SVGPoint matrixTransform(SVGMatrix mat) {
            AffineTransform scrnTrans = this.getScreenTransform();
            float x = (float)scrnTrans.getTranslateX();
            float y = (float)scrnTrans.getTranslateY();
            float newX = mat.getA() * x + mat.getC() * y + mat.getE();
            float newY = mat.getB() * x + mat.getD() * y + mat.getF();
            return new SVGOMPoint(newX, newY);
         }
      };
   }

   public int suspendRedraw(int max_wait_milliseconds) {
      if (max_wait_milliseconds > 60000) {
         max_wait_milliseconds = 60000;
      } else if (max_wait_milliseconds < 0) {
         max_wait_milliseconds = 0;
      }

      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      return ctx.suspendRedraw(max_wait_milliseconds);
   }

   public void unsuspendRedraw(int suspend_handle_id) throws DOMException {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      if (!ctx.unsuspendRedraw(suspend_handle_id)) {
         throw this.createDOMException((short)8, "invalid.suspend.handle", new Object[]{suspend_handle_id});
      }
   }

   public void unsuspendRedrawAll() {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      ctx.unsuspendRedrawAll();
   }

   public void forceRedraw() {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      ctx.forceRedraw();
   }

   public void pauseAnimations() {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      ctx.pauseAnimations();
   }

   public void unpauseAnimations() {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      ctx.unpauseAnimations();
   }

   public boolean animationsPaused() {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      return ctx.animationsPaused();
   }

   public float getCurrentTime() {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      return ctx.getCurrentTime();
   }

   public void setCurrentTime(float seconds) {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      ctx.setCurrentTime(seconds);
   }

   public NodeList getIntersectionList(SVGRect rect, SVGElement referenceElement) {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      List list = ctx.getIntersectionList(rect, referenceElement);
      return new ListNodeList(list);
   }

   public NodeList getEnclosureList(SVGRect rect, SVGElement referenceElement) {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      List list = ctx.getEnclosureList(rect, referenceElement);
      return new ListNodeList(list);
   }

   public boolean checkIntersection(SVGElement element, SVGRect rect) {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      return ctx.checkIntersection(element, rect);
   }

   public boolean checkEnclosure(SVGElement element, SVGRect rect) {
      SVGSVGContext ctx = (SVGSVGContext)this.getSVGContext();
      return ctx.checkEnclosure(element, rect);
   }

   public void deselectAll() {
      ((SVGSVGContext)this.getSVGContext()).deselectAll();
   }

   public SVGNumber createSVGNumber() {
      return new SVGNumber() {
         protected float value;

         public float getValue() {
            return this.value;
         }

         public void setValue(float f) {
            this.value = f;
         }
      };
   }

   public SVGLength createSVGLength() {
      return new SVGOMLength(this);
   }

   public SVGAngle createSVGAngle() {
      return new SVGOMAngle();
   }

   public SVGPoint createSVGPoint() {
      return new SVGOMPoint(0.0F, 0.0F);
   }

   public SVGMatrix createSVGMatrix() {
      return new AbstractSVGMatrix() {
         protected AffineTransform at = new AffineTransform();

         protected AffineTransform getAffineTransform() {
            return this.at;
         }
      };
   }

   public SVGRect createSVGRect() {
      return new SVGOMRect(0.0F, 0.0F, 0.0F, 0.0F);
   }

   public SVGTransform createSVGTransform() {
      SVGOMTransform ret = new SVGOMTransform();
      ret.setType((short)1);
      return ret;
   }

   public SVGTransform createSVGTransformFromMatrix(SVGMatrix matrix) {
      SVGOMTransform tr = new SVGOMTransform();
      tr.setMatrix(matrix);
      return tr;
   }

   public Element getElementById(String elementId) {
      return this.ownerDocument.getChildElementById(this, elementId);
   }

   public SVGElement getNearestViewportElement() {
      return SVGLocatableSupport.getNearestViewportElement(this);
   }

   public SVGElement getFarthestViewportElement() {
      return SVGLocatableSupport.getFarthestViewportElement(this);
   }

   public SVGRect getBBox() {
      return SVGLocatableSupport.getBBox(this);
   }

   public SVGMatrix getCTM() {
      return SVGLocatableSupport.getCTM(this);
   }

   public SVGMatrix getScreenCTM() {
      return SVGLocatableSupport.getScreenCTM(this);
   }

   public SVGMatrix getTransformToElement(SVGElement element) throws SVGException {
      return SVGLocatableSupport.getTransformToElement(this, element);
   }

   public DocumentView getDocument() {
      return (DocumentView)this.getOwnerDocument();
   }

   public CSSStyleDeclaration getComputedStyle(Element elt, String pseudoElt) {
      AbstractView av = ((DocumentView)this.getOwnerDocument()).getDefaultView();
      return ((ViewCSS)av).getComputedStyle(elt, pseudoElt);
   }

   public Event createEvent(String eventType) throws DOMException {
      return ((DocumentEvent)this.getOwnerDocument()).createEvent(eventType);
   }

   public boolean canDispatch(String namespaceURI, String type) throws DOMException {
      AbstractDocument doc = (AbstractDocument)this.getOwnerDocument();
      return doc.canDispatch(namespaceURI, type);
   }

   public StyleSheetList getStyleSheets() {
      return ((DocumentStyle)this.getOwnerDocument()).getStyleSheets();
   }

   public CSSStyleDeclaration getOverrideStyle(Element elt, String pseudoElt) {
      return ((DocumentCSS)this.getOwnerDocument()).getOverrideStyle(elt, pseudoElt);
   }

   public String getXMLlang() {
      return XMLSupport.getXMLLang(this);
   }

   public void setXMLlang(String lang) {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", lang);
   }

   public String getXMLspace() {
      return XMLSupport.getXMLSpace(this);
   }

   public void setXMLspace(String space) {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", space);
   }

   public short getZoomAndPan() {
      return SVGZoomAndPanSupport.getZoomAndPan(this);
   }

   public void setZoomAndPan(short val) {
      SVGZoomAndPanSupport.setZoomAndPan(this, val);
   }

   public SVGAnimatedRect getViewBox() {
      return this.viewBox;
   }

   public SVGAnimatedPreserveAspectRatio getPreserveAspectRatio() {
      return this.preserveAspectRatio;
   }

   public SVGAnimatedBoolean getExternalResourcesRequired() {
      return this.externalResourcesRequired;
   }

   public SVGStringList getRequiredFeatures() {
      return SVGTestsSupport.getRequiredFeatures(this);
   }

   public SVGStringList getRequiredExtensions() {
      return SVGTestsSupport.getRequiredExtensions(this);
   }

   public SVGStringList getSystemLanguage() {
      return SVGTestsSupport.getSystemLanguage(this);
   }

   public boolean hasExtension(String extension) {
      return SVGTestsSupport.hasExtension(this, extension);
   }

   protected AttributeInitializer getAttributeInitializer() {
      return attributeInitializer;
   }

   protected Node newNode() {
      return new SVGOMSVGElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
      t.put((Object)null, "x", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "y", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "width", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "height", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "preserveAspectRatio", new TraitInformation(true, 32));
      t.put((Object)null, "viewBox", new TraitInformation(true, 50));
      t.put((Object)null, "externalResourcesRequired", new TraitInformation(true, 49));
      xmlTraitInformation = t;
      attributeInitializer = new AttributeInitializer(7);
      attributeInitializer.addAttribute("http://www.w3.org/2000/xmlns/", (String)null, "xmlns", "http://www.w3.org/2000/svg");
      attributeInitializer.addAttribute("http://www.w3.org/2000/xmlns/", "xmlns", "xlink", "http://www.w3.org/1999/xlink");
      attributeInitializer.addAttribute((String)null, (String)null, "preserveAspectRatio", "xMidYMid meet");
      attributeInitializer.addAttribute((String)null, (String)null, "zoomAndPan", "magnify");
      attributeInitializer.addAttribute((String)null, (String)null, "version", "1.0");
      attributeInitializer.addAttribute((String)null, (String)null, "contentScriptType", "text/ecmascript");
      attributeInitializer.addAttribute((String)null, (String)null, "contentStyleType", "text/css");
   }
}
