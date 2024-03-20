package org.apache.fop.render.ps.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExtensionAttachment;

public class PSPageTrailerCodeBeforeElement extends AbstractPSCommentElement {
   protected static final String ELEMENT = "ps-page-trailer-code-before";

   public PSPageTrailerCodeBeforeElement(FONode parent) {
      super(parent);
   }

   public String getLocalName() {
      return "ps-page-trailer-code-before";
   }

   protected ExtensionAttachment instantiateExtensionAttachment() {
      return new PSPageTrailerCodeBefore();
   }
}
