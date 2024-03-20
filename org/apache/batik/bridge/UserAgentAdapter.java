package org.apache.batik.bridge;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGFeatureStrings;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;

public class UserAgentAdapter implements UserAgent {
   protected Set FEATURES = new HashSet();
   protected Set extensions = new HashSet();
   protected BridgeContext ctx;

   public void setBridgeContext(BridgeContext ctx) {
      this.ctx = ctx;
   }

   public void addStdFeatures() {
      SVGFeatureStrings.addSupportedFeatureStrings(this.FEATURES);
   }

   public Dimension2D getViewportSize() {
      return new Dimension(1, 1);
   }

   public void displayMessage(String message) {
   }

   public void displayError(String message) {
      this.displayMessage(message);
   }

   public void displayError(Exception e) {
      this.displayError(e.getMessage());
   }

   public void showAlert(String message) {
   }

   public String showPrompt(String message) {
      return null;
   }

   public String showPrompt(String message, String defaultValue) {
      return null;
   }

   public boolean showConfirm(String message) {
      return false;
   }

   public float getPixelUnitToMillimeter() {
      return 0.26458332F;
   }

   public float getPixelToMM() {
      return this.getPixelUnitToMillimeter();
   }

   public String getDefaultFontFamily() {
      return "Arial, Helvetica, sans-serif";
   }

   public float getMediumFontSize() {
      return 228.59999F / (72.0F * this.getPixelUnitToMillimeter());
   }

   public float getLighterFontWeight(float f) {
      return getStandardLighterFontWeight(f);
   }

   public float getBolderFontWeight(float f) {
      return getStandardBolderFontWeight(f);
   }

   public String getLanguages() {
      return "en";
   }

   public String getMedia() {
      return "all";
   }

   public String getAlternateStyleSheet() {
      return null;
   }

   public String getUserStyleSheetURI() {
      return null;
   }

   public String getXMLParserClassName() {
      return XMLResourceDescriptor.getXMLParserClassName();
   }

   public boolean isXMLParserValidating() {
      return false;
   }

   public EventDispatcher getEventDispatcher() {
      return null;
   }

   public void openLink(SVGAElement elt) {
   }

   public void setSVGCursor(Cursor cursor) {
   }

   public void setTextSelection(Mark start, Mark end) {
   }

   public void deselectAll() {
   }

   public void runThread(Thread t) {
   }

   public AffineTransform getTransform() {
      return null;
   }

   public void setTransform(AffineTransform at) {
   }

   public Point getClientAreaLocationOnScreen() {
      return new Point();
   }

   public boolean hasFeature(String s) {
      return this.FEATURES.contains(s);
   }

   public boolean supportExtension(String s) {
      return this.extensions.contains(s);
   }

   public void registerExtension(BridgeExtension ext) {
      Iterator i = ext.getImplementedExtensions();

      while(i.hasNext()) {
         this.extensions.add(i.next());
      }

   }

   public void handleElement(Element elt, Object data) {
   }

   public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
      return new DefaultScriptSecurity(scriptType, scriptURL, docURL);
   }

   public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) throws SecurityException {
      ScriptSecurity s = this.getScriptSecurity(scriptType, scriptURL, docURL);
      if (s != null) {
         s.checkLoadScript();
      }

   }

   public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL) {
      return new RelaxedExternalResourceSecurity(resourceURL, docURL);
   }

   public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) throws SecurityException {
      ExternalResourceSecurity s = this.getExternalResourceSecurity(resourceURL, docURL);
      if (s != null) {
         s.checkLoadExternalResource();
      }

   }

   public static float getStandardLighterFontWeight(float f) {
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

   public static float getStandardBolderFontWeight(float f) {
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

   public SVGDocument getBrokenLinkDocument(Element e, String url, String message) {
      throw new BridgeException(this.ctx, e, "uri.image.broken", new Object[]{url, message});
   }

   public void loadDocument(String url) {
   }

   public FontFamilyResolver getFontFamilyResolver() {
      return DefaultFontFamilyResolver.SINGLETON;
   }
}
