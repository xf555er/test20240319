package org.apache.fop.render.pdf.extensions;

import org.apache.fop.fo.FONode;

public class PDFPagePieceElement extends PDFDictionaryElement {
   PDFPagePieceElement(FONode parent) {
      super(parent, PDFDictionaryType.PagePiece);
   }
}
