package org.apache.batik.swing.svg;

import java.util.EventObject;
import org.w3c.dom.svg.SVGDocument;

public class SVGDocumentLoaderEvent extends EventObject {
   protected SVGDocument svgDocument;

   public SVGDocumentLoaderEvent(Object source, SVGDocument doc) {
      super(source);
      this.svgDocument = doc;
   }

   public SVGDocument getSVGDocument() {
      return this.svgDocument;
   }
}
