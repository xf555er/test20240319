package org.apache.fop.svg;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.bridge.UserAgentAdapter;
import org.xml.sax.SAXException;

public class SimpleSVGUserAgent extends UserAgentAdapter {
   private AffineTransform currentTransform;
   private float pixelUnitToMillimeter;
   private final FontFamilyResolver fontFamilyResolver;
   private static final String XML_PARSER_CLASS_NAME;

   public SimpleSVGUserAgent(float pixelUnitToMM, AffineTransform at, FontFamilyResolver fontFamilyResolver) {
      this.fontFamilyResolver = fontFamilyResolver;
      this.pixelUnitToMillimeter = pixelUnitToMM;
      this.currentTransform = at;
   }

   public float getPixelUnitToMillimeter() {
      return this.pixelUnitToMillimeter;
   }

   public String getLanguages() {
      return "en";
   }

   public String getMedia() {
      return "print";
   }

   public String getUserStyleSheetURI() {
      return null;
   }

   public String getXMLParserClassName() {
      return XML_PARSER_CLASS_NAME;
   }

   public boolean isXMLParserValidating() {
      return false;
   }

   public AffineTransform getTransform() {
      return this.currentTransform;
   }

   public void setTransform(AffineTransform at) {
      this.currentTransform = at;
   }

   public Dimension2D getViewportSize() {
      return new Dimension(100, 100);
   }

   public FontFamilyResolver getFontFamilyResolver() {
      return this.fontFamilyResolver;
   }

   static {
      String result;
      try {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         result = factory.newSAXParser().getXMLReader().getClass().getName();
      } catch (SAXException var2) {
         result = null;
      } catch (ParserConfigurationException var3) {
         result = null;
      }

      XML_PARSER_CLASS_NAME = result;
   }
}
