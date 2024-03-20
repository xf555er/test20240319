package javax.xml.parsers;

public abstract class DocumentBuilderFactory {
   private boolean validating = false;
   private boolean namespaceAware = false;
   private boolean whitespace = false;
   private boolean expandEntityRef = true;
   private boolean ignoreComments = false;
   private boolean coalescing = false;

   protected DocumentBuilderFactory() {
   }

   public static DocumentBuilderFactory newInstance() throws FactoryConfigurationError {
      try {
         return (DocumentBuilderFactory)FactoryFinder.find("javax.xml.parsers.DocumentBuilderFactory", (String)null);
      } catch (FactoryFinder.ConfigurationError var1) {
         throw new FactoryConfigurationError(var1.getException(), var1.getMessage());
      }
   }

   public abstract DocumentBuilder newDocumentBuilder() throws ParserConfigurationException;

   public void setNamespaceAware(boolean var1) {
      this.namespaceAware = var1;
   }

   public void setValidating(boolean var1) {
      this.validating = var1;
   }

   public void setIgnoringElementContentWhitespace(boolean var1) {
      this.whitespace = var1;
   }

   public void setExpandEntityReferences(boolean var1) {
      this.expandEntityRef = var1;
   }

   public void setIgnoringComments(boolean var1) {
      this.ignoreComments = var1;
   }

   public void setCoalescing(boolean var1) {
      this.coalescing = var1;
   }

   public boolean isNamespaceAware() {
      return this.namespaceAware;
   }

   public boolean isValidating() {
      return this.validating;
   }

   public boolean isIgnoringElementContentWhitespace() {
      return this.whitespace;
   }

   public boolean isExpandEntityReferences() {
      return this.expandEntityRef;
   }

   public boolean isIgnoringComments() {
      return this.ignoreComments;
   }

   public boolean isCoalescing() {
      return this.coalescing;
   }

   public abstract void setAttribute(String var1, Object var2) throws IllegalArgumentException;

   public abstract Object getAttribute(String var1) throws IllegalArgumentException;
}
