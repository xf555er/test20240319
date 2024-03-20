package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;

public class PDFNavigatorElement extends PDFDictionaryElement {
   PDFNavigatorElement(FONode parent) {
      super(parent, PDFDictionaryType.Navigator);
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      if (this.parent.getNameId() != 13) {
         this.invalidChildError(this.getLocator(), this.parent.getName(), this.getNamespaceURI(), this.getName(), "rule.childOfDeclarations");
      }

   }
}
