package org.apache.fop.render.ps.extensions;

public class PSPageTrailerCodeBefore extends PSExtensionAttachment {
   protected static final String ELEMENT = "ps-page-trailer-code-before";

   public PSPageTrailerCodeBefore(String content) {
      super(content);
   }

   public PSPageTrailerCodeBefore() {
   }

   protected String getElement() {
      return "ps-page-trailer-code-before";
   }
}
