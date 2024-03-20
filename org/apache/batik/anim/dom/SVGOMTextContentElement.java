package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGTestsSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGStringList;

public abstract class SVGOMTextContentElement extends SVGStylableElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final String[] LENGTH_ADJUST_VALUES;
   protected SVGOMAnimatedBoolean externalResourcesRequired;
   protected AbstractSVGAnimatedLength textLength;
   protected SVGOMAnimatedEnumeration lengthAdjust;

   protected SVGOMTextContentElement() {
   }

   protected SVGOMTextContentElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.externalResourcesRequired = this.createLiveAnimatedBoolean((String)null, "externalResourcesRequired", false);
      this.lengthAdjust = this.createLiveAnimatedEnumeration((String)null, "lengthAdjust", LENGTH_ADJUST_VALUES, (short)1);
      this.textLength = new AbstractSVGAnimatedLength(this, (String)null, "textLength", 2, true) {
         boolean usedDefault;

         protected String getDefaultValue() {
            this.usedDefault = true;
            return String.valueOf(SVGOMTextContentElement.this.getComputedTextLength());
         }

         public SVGLength getBaseVal() {
            if (this.baseVal == null) {
               this.baseVal = new null.SVGTextLength(this.direction);
            }

            return this.baseVal;
         }

         class SVGTextLength extends AbstractSVGAnimatedLength.BaseSVGLength {
            public SVGTextLength(short direction) {
               super(direction);
            }

            protected void revalidate() {
               usedDefault = false;
               super.revalidate();
               if (usedDefault) {
                  this.valid = false;
               }

            }
         }
      };
      this.liveAttributeValues.put((Object)null, "textLength", this.textLength);
      this.textLength.addAnimatedAttributeListener(((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener());
   }

   public SVGAnimatedLength getTextLength() {
      return this.textLength;
   }

   public SVGAnimatedEnumeration getLengthAdjust() {
      return this.lengthAdjust;
   }

   public int getNumberOfChars() {
      return SVGTextContentSupport.getNumberOfChars(this);
   }

   public float getComputedTextLength() {
      return SVGTextContentSupport.getComputedTextLength(this);
   }

   public float getSubStringLength(int charnum, int nchars) throws DOMException {
      return SVGTextContentSupport.getSubStringLength(this, charnum, nchars);
   }

   public SVGPoint getStartPositionOfChar(int charnum) throws DOMException {
      return SVGTextContentSupport.getStartPositionOfChar(this, charnum);
   }

   public SVGPoint getEndPositionOfChar(int charnum) throws DOMException {
      return SVGTextContentSupport.getEndPositionOfChar(this, charnum);
   }

   public SVGRect getExtentOfChar(int charnum) throws DOMException {
      return SVGTextContentSupport.getExtentOfChar(this, charnum);
   }

   public float getRotationOfChar(int charnum) throws DOMException {
      return SVGTextContentSupport.getRotationOfChar(this, charnum);
   }

   public int getCharNumAtPosition(SVGPoint point) {
      return SVGTextContentSupport.getCharNumAtPosition(this, point.getX(), point.getY());
   }

   public void selectSubString(int charnum, int nchars) throws DOMException {
      SVGTextContentSupport.selectSubString(this, charnum, nchars);
   }

   public SVGAnimatedBoolean getExternalResourcesRequired() {
      return this.externalResourcesRequired;
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

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
      t.put((Object)null, "textLength", new TraitInformation(true, 3, (short)3));
      t.put((Object)null, "lengthAdjust", new TraitInformation(true, 15));
      t.put((Object)null, "externalResourcesRequired", new TraitInformation(true, 49));
      xmlTraitInformation = t;
      LENGTH_ADJUST_VALUES = new String[]{"", "spacing", "spacingAndGlyphs"};
   }
}
