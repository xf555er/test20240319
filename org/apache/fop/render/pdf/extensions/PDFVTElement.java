package org.apache.fop.render.pdf.extensions;

import org.apache.fop.fo.FONode;

public class PDFVTElement extends PDFDictionaryElement {
   PDFVTElement(FONode parent) {
      super(parent, PDFDictionaryType.VT);
   }
}
