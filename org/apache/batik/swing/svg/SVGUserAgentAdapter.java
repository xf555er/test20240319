package org.apache.batik.swing.svg;

import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;

public class SVGUserAgentAdapter implements SVGUserAgent {
   public void displayError(String message) {
      System.err.println(message);
   }

   public void displayError(Exception ex) {
      ex.printStackTrace();
   }

   public void displayMessage(String message) {
      System.out.println(message);
   }

   public void showAlert(String message) {
      System.err.println(message);
   }

   public String showPrompt(String message) {
      return "";
   }

   public String showPrompt(String message, String defaultValue) {
      return defaultValue;
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
      return "Serif";
   }

   public float getMediumFontSize() {
      return 228.59999F / (72.0F * this.getPixelUnitToMillimeter());
   }

   public float getLighterFontWeight(float f) {
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

   public float getBolderFontWeight(float f) {
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

   public String getLanguages() {
      return "en";
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

   public String getMedia() {
      return "screen";
   }

   public String getAlternateStyleSheet() {
      return null;
   }

   public void openLink(String uri, boolean newc) {
   }

   public boolean supportExtension(String s) {
      return false;
   }

   public void handleElement(Element elt, Object data) {
   }

   public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
      return new RelaxedScriptSecurity(scriptType, scriptURL, docURL);
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
}
