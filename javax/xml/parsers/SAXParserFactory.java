package javax.xml.parsers;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class SAXParserFactory {
   private boolean validating = false;
   private boolean namespaceAware = false;

   protected SAXParserFactory() {
   }

   public static SAXParserFactory newInstance() throws FactoryConfigurationError {
      try {
         return (SAXParserFactory)FactoryFinder.find("javax.xml.parsers.SAXParserFactory", (String)null);
      } catch (FactoryFinder.ConfigurationError var1) {
         throw new FactoryConfigurationError(var1.getException(), var1.getMessage());
      }
   }

   public abstract SAXParser newSAXParser() throws ParserConfigurationException, SAXException;

   public void setNamespaceAware(boolean var1) {
      this.namespaceAware = var1;
   }

   public void setValidating(boolean var1) {
      this.validating = var1;
   }

   public boolean isNamespaceAware() {
      return this.namespaceAware;
   }

   public boolean isValidating() {
      return this.validating;
   }

   public abstract void setFeature(String var1, boolean var2) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

   public abstract boolean getFeature(String var1) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;
}
