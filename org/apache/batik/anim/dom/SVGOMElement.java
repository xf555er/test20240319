package org.apache.batik.anim.dom;

import java.util.Iterator;
import java.util.LinkedList;
import org.apache.batik.anim.values.AnimatableNumberOptionalNumberValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSNavigableNode;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.dom.svg.ExtendedTraitAccess;
import org.apache.batik.dom.svg.LiveAttributeValue;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGOMException;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.parser.UnitProcessor;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedInteger;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGFitToViewBox;
import org.w3c.dom.svg.SVGSVGElement;

public abstract class SVGOMElement extends AbstractElement implements SVGElement, ExtendedTraitAccess, AnimationTarget {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected transient boolean readonly;
   protected String prefix;
   protected transient SVGContext svgContext;
   protected DoublyIndexedTable targetListeners;
   protected UnitProcessor.Context unitContext;

   protected SVGOMElement() {
   }

   protected SVGOMElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   protected void initializeAllLiveAttributes() {
   }

   public String getId() {
      if (((SVGOMDocument)this.ownerDocument).isSVG12) {
         Attr a = this.getAttributeNodeNS("http://www.w3.org/XML/1998/namespace", "id");
         if (a != null) {
            return a.getNodeValue();
         }
      }

      return this.getAttributeNS((String)null, "id");
   }

   public void setId(String id) {
      if (((SVGOMDocument)this.ownerDocument).isSVG12) {
         this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:id", id);
         Attr a = this.getAttributeNodeNS((String)null, "id");
         if (a != null) {
            a.setNodeValue(id);
         }
      } else {
         this.setAttributeNS((String)null, "id", id);
      }

   }

   public String getXMLbase() {
      return this.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base");
   }

   public void setXMLbase(String xmlbase) throws DOMException {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", xmlbase);
   }

   public SVGSVGElement getOwnerSVGElement() {
      for(Element e = CSSEngine.getParentCSSStylableElement(this); e != null; e = CSSEngine.getParentCSSStylableElement(e)) {
         if (e instanceof SVGSVGElement) {
            return (SVGSVGElement)e;
         }
      }

      return null;
   }

   public SVGElement getViewportElement() {
      for(Element e = CSSEngine.getParentCSSStylableElement(this); e != null; e = CSSEngine.getParentCSSStylableElement(e)) {
         if (e instanceof SVGFitToViewBox) {
            return (SVGElement)e;
         }
      }

      return null;
   }

   public String getNodeName() {
      return this.prefix != null && !this.prefix.equals("") ? this.prefix + ':' + this.getLocalName() : this.getLocalName();
   }

   public String getNamespaceURI() {
      return "http://www.w3.org/2000/svg";
   }

   public void setPrefix(String prefix) throws DOMException {
      if (this.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName()});
      } else if (prefix != null && !prefix.equals("") && !DOMUtilities.isValidName(prefix)) {
         throw this.createDOMException((short)5, "prefix", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), prefix});
      } else {
         this.prefix = prefix;
      }
   }

   protected String getCascadedXMLBase(Node node) {
      String base = null;
      Node n = node.getParentNode();

      while(n != null) {
         if (n.getNodeType() == 1) {
            base = this.getCascadedXMLBase(n);
            break;
         }

         if (n instanceof CSSNavigableNode) {
            n = ((CSSNavigableNode)n).getCSSParentNode();
         } else {
            n = n.getParentNode();
         }
      }

      if (base == null) {
         AbstractDocument doc;
         if (node.getNodeType() == 9) {
            doc = (AbstractDocument)node;
         } else {
            doc = (AbstractDocument)node.getOwnerDocument();
         }

         base = doc.getDocumentURI();
      }

      while(node != null && node.getNodeType() != 1) {
         node = node.getParentNode();
      }

      if (node == null) {
         return base;
      } else {
         Element e = (Element)node;
         Attr attr = e.getAttributeNodeNS("http://www.w3.org/XML/1998/namespace", "base");
         if (attr != null) {
            if (base == null) {
               base = attr.getNodeValue();
            } else {
               base = (new ParsedURL(base, attr.getNodeValue())).toString();
            }
         }

         return base;
      }
   }

   public void setSVGContext(SVGContext ctx) {
      this.svgContext = ctx;
   }

   public SVGContext getSVGContext() {
      return this.svgContext;
   }

   public SVGException createSVGException(short type, String key, Object[] args) {
      try {
         return new SVGOMException(type, this.getCurrentDocument().formatMessage(key, args));
      } catch (Exception var5) {
         return new SVGOMException(type, key);
      }
   }

   public boolean isReadonly() {
      return this.readonly;
   }

   public void setReadonly(boolean v) {
      this.readonly = v;
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   protected SVGOMAnimatedTransformList createLiveAnimatedTransformList(String ns, String ln, String def) {
      SVGOMAnimatedTransformList v = new SVGOMAnimatedTransformList(this, ns, ln, def);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedBoolean createLiveAnimatedBoolean(String ns, String ln, boolean def) {
      SVGOMAnimatedBoolean v = new SVGOMAnimatedBoolean(this, ns, ln, def);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedString createLiveAnimatedString(String ns, String ln) {
      SVGOMAnimatedString v = new SVGOMAnimatedString(this, ns, ln);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedPreserveAspectRatio createLiveAnimatedPreserveAspectRatio() {
      SVGOMAnimatedPreserveAspectRatio v = new SVGOMAnimatedPreserveAspectRatio(this);
      this.liveAttributeValues.put((Object)null, "preserveAspectRatio", v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedMarkerOrientValue createLiveAnimatedMarkerOrientValue(String ns, String ln) {
      SVGOMAnimatedMarkerOrientValue v = new SVGOMAnimatedMarkerOrientValue(this, ns, ln);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedPathData createLiveAnimatedPathData(String ns, String ln, String def) {
      SVGOMAnimatedPathData v = new SVGOMAnimatedPathData(this, ns, ln, def);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedNumber createLiveAnimatedNumber(String ns, String ln, float def) {
      return this.createLiveAnimatedNumber(ns, ln, def, false);
   }

   protected SVGOMAnimatedNumber createLiveAnimatedNumber(String ns, String ln, float def, boolean allowPercentage) {
      SVGOMAnimatedNumber v = new SVGOMAnimatedNumber(this, ns, ln, def, allowPercentage);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedNumberList createLiveAnimatedNumberList(String ns, String ln, String def, boolean canEmpty) {
      SVGOMAnimatedNumberList v = new SVGOMAnimatedNumberList(this, ns, ln, def, canEmpty);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedPoints createLiveAnimatedPoints(String ns, String ln, String def) {
      SVGOMAnimatedPoints v = new SVGOMAnimatedPoints(this, ns, ln, def);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedLengthList createLiveAnimatedLengthList(String ns, String ln, String def, boolean emptyAllowed, short dir) {
      SVGOMAnimatedLengthList v = new SVGOMAnimatedLengthList(this, ns, ln, def, emptyAllowed, dir);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedInteger createLiveAnimatedInteger(String ns, String ln, int def) {
      SVGOMAnimatedInteger v = new SVGOMAnimatedInteger(this, ns, ln, def);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedEnumeration createLiveAnimatedEnumeration(String ns, String ln, String[] val, short def) {
      SVGOMAnimatedEnumeration v = new SVGOMAnimatedEnumeration(this, ns, ln, val, def);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedLength createLiveAnimatedLength(String ns, String ln, String val, short dir, boolean nonneg) {
      SVGOMAnimatedLength v = new SVGOMAnimatedLength(this, ns, ln, val, dir, nonneg);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   protected SVGOMAnimatedRect createLiveAnimatedRect(String ns, String ln, String value) {
      SVGOMAnimatedRect v = new SVGOMAnimatedRect(this, ns, ln, value);
      this.liveAttributeValues.put(ns, ln, v);
      v.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
      return v;
   }

   public boolean hasProperty(String pn) {
      AbstractStylableDocument doc = (AbstractStylableDocument)this.ownerDocument;
      CSSEngine eng = doc.getCSSEngine();
      return eng.getPropertyIndex(pn) != -1 || eng.getShorthandIndex(pn) != -1;
   }

   public boolean hasTrait(String ns, String ln) {
      return false;
   }

   public boolean isPropertyAnimatable(String pn) {
      AbstractStylableDocument doc = (AbstractStylableDocument)this.ownerDocument;
      CSSEngine eng = doc.getCSSEngine();
      int idx = eng.getPropertyIndex(pn);
      if (idx != -1) {
         ValueManager[] vms = eng.getValueManagers();
         return vms[idx].isAnimatableProperty();
      } else {
         idx = eng.getShorthandIndex(pn);
         if (idx != -1) {
            ShorthandManager[] sms = eng.getShorthandManagers();
            return sms[idx].isAnimatableProperty();
         } else {
            return false;
         }
      }
   }

   public final boolean isAttributeAnimatable(String ns, String ln) {
      DoublyIndexedTable t = this.getTraitInformationTable();
      TraitInformation ti = (TraitInformation)t.get(ns, ln);
      return ti != null ? ti.isAnimatable() : false;
   }

   public boolean isPropertyAdditive(String pn) {
      AbstractStylableDocument doc = (AbstractStylableDocument)this.ownerDocument;
      CSSEngine eng = doc.getCSSEngine();
      int idx = eng.getPropertyIndex(pn);
      if (idx != -1) {
         ValueManager[] vms = eng.getValueManagers();
         return vms[idx].isAdditiveProperty();
      } else {
         idx = eng.getShorthandIndex(pn);
         if (idx != -1) {
            ShorthandManager[] sms = eng.getShorthandManagers();
            return sms[idx].isAdditiveProperty();
         } else {
            return false;
         }
      }
   }

   public boolean isAttributeAdditive(String ns, String ln) {
      return true;
   }

   public boolean isTraitAnimatable(String ns, String tn) {
      return false;
   }

   public boolean isTraitAdditive(String ns, String tn) {
      return false;
   }

   public int getPropertyType(String pn) {
      AbstractStylableDocument doc = (AbstractStylableDocument)this.ownerDocument;
      CSSEngine eng = doc.getCSSEngine();
      int idx = eng.getPropertyIndex(pn);
      if (idx != -1) {
         ValueManager[] vms = eng.getValueManagers();
         return vms[idx].getPropertyType();
      } else {
         return 0;
      }
   }

   public final int getAttributeType(String ns, String ln) {
      DoublyIndexedTable t = this.getTraitInformationTable();
      TraitInformation ti = (TraitInformation)t.get(ns, ln);
      return ti != null ? ti.getType() : 0;
   }

   public Element getElement() {
      return this;
   }

   public void updatePropertyValue(String pn, AnimatableValue val) {
   }

   public void updateAttributeValue(String ns, String ln, AnimatableValue val) {
      LiveAttributeValue a = this.getLiveAttributeValue(ns, ln);
      ((AbstractSVGAnimatedValue)a).updateAnimatedValue(val);
   }

   public void updateOtherValue(String type, AnimatableValue val) {
   }

   public AnimatableValue getUnderlyingValue(String ns, String ln) {
      LiveAttributeValue a = this.getLiveAttributeValue(ns, ln);
      return !(a instanceof AnimatedLiveAttributeValue) ? null : ((AnimatedLiveAttributeValue)a).getUnderlyingValue(this);
   }

   protected AnimatableValue getBaseValue(SVGAnimatedInteger n, SVGAnimatedInteger on) {
      return new AnimatableNumberOptionalNumberValue(this, (float)n.getBaseVal(), (float)on.getBaseVal());
   }

   protected AnimatableValue getBaseValue(SVGAnimatedNumber n, SVGAnimatedNumber on) {
      return new AnimatableNumberOptionalNumberValue(this, n.getBaseVal(), on.getBaseVal());
   }

   public short getPercentageInterpretation(String ns, String an, boolean isCSS) {
      if ((isCSS || ns == null) && (an.equals("baseline-shift") || an.equals("font-size"))) {
         return 0;
      } else if (!isCSS) {
         DoublyIndexedTable t = this.getTraitInformationTable();
         TraitInformation ti = (TraitInformation)t.get(ns, an);
         return ti != null ? ti.getPercentageInterpretation() : 3;
      } else {
         return 3;
      }
   }

   protected final short getAttributePercentageInterpretation(String ns, String ln) {
      return 3;
   }

   public boolean useLinearRGBColorInterpolation() {
      return false;
   }

   public float svgToUserSpace(float v, short type, short pcInterp) {
      if (this.unitContext == null) {
         this.unitContext = new UnitContext();
      }

      return pcInterp == 0 && type == 2 ? 0.0F : UnitProcessor.svgToUserSpace(v, type, (short)(3 - pcInterp), this.unitContext);
   }

   public void addTargetListener(String ns, String an, boolean isCSS, AnimationTargetListener l) {
      if (!isCSS) {
         if (this.targetListeners == null) {
            this.targetListeners = new DoublyIndexedTable();
         }

         LinkedList ll = (LinkedList)this.targetListeners.get(ns, an);
         if (ll == null) {
            ll = new LinkedList();
            this.targetListeners.put(ns, an, ll);
         }

         ll.add(l);
      }

   }

   public void removeTargetListener(String ns, String an, boolean isCSS, AnimationTargetListener l) {
      if (!isCSS) {
         LinkedList ll = (LinkedList)this.targetListeners.get(ns, an);
         ll.remove(l);
      }

   }

   void fireBaseAttributeListeners(String ns, String ln) {
      if (this.targetListeners != null) {
         LinkedList ll = (LinkedList)this.targetListeners.get(ns, ln);
         Iterator var4 = ll.iterator();

         while(var4.hasNext()) {
            Object aLl = var4.next();
            AnimationTargetListener l = (AnimationTargetListener)aLl;
            l.baseValueChanged(this, ns, ln, false);
         }
      }

   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      SVGOMElement e = (SVGOMElement)n;
      e.prefix = this.prefix;
      e.initializeAllLiveAttributes();
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      SVGOMElement e = (SVGOMElement)n;
      e.prefix = this.prefix;
      e.initializeAllLiveAttributes();
      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      SVGOMElement e = (SVGOMElement)n;
      e.prefix = this.prefix;
      e.initializeAllLiveAttributes();
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      SVGOMElement e = (SVGOMElement)n;
      e.prefix = this.prefix;
      e.initializeAllLiveAttributes();
      return n;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable();
      t.put((Object)null, "id", new TraitInformation(false, 16));
      t.put("http://www.w3.org/XML/1998/namespace", "base", new TraitInformation(false, 10));
      t.put("http://www.w3.org/XML/1998/namespace", "space", new TraitInformation(false, 15));
      t.put("http://www.w3.org/XML/1998/namespace", "id", new TraitInformation(false, 16));
      t.put("http://www.w3.org/XML/1998/namespace", "lang", new TraitInformation(false, 45));
      xmlTraitInformation = t;
   }

   protected class UnitContext implements UnitProcessor.Context {
      public Element getElement() {
         return SVGOMElement.this;
      }

      public float getPixelUnitToMillimeter() {
         return SVGOMElement.this.getSVGContext().getPixelUnitToMillimeter();
      }

      public float getPixelToMM() {
         return this.getPixelUnitToMillimeter();
      }

      public float getFontSize() {
         return SVGOMElement.this.getSVGContext().getFontSize();
      }

      public float getXHeight() {
         return 0.5F;
      }

      public float getViewportWidth() {
         return SVGOMElement.this.getSVGContext().getViewportWidth();
      }

      public float getViewportHeight() {
         return SVGOMElement.this.getSVGContext().getViewportHeight();
      }
   }
}
