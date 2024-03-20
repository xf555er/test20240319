package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFReferenceElement extends PDFCollectionEntryElement {
   public static final String ATT_REFID = "refid";

   PDFReferenceElement(FONode parent) {
      super(parent, PDFObjectType.Reference);
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      super.processNode(elementName, locator, attlist, propertyList);
      String refid = attlist.getValue("refid");
      if (refid == null) {
         this.missingPropertyError("refid");
      } else if (refid.length() == 0) {
         this.invalidPropertyValueError("refid", refid, (Exception)null);
      } else {
         PDFCollectionEntryExtension extension = this.getExtension();

         assert extension instanceof PDFReferenceExtension;

         ((PDFReferenceExtension)extension).setReferenceId(refid);
      }

   }
}
