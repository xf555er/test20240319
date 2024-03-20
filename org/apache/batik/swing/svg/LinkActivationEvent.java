package org.apache.batik.swing.svg;

import java.util.EventObject;
import org.w3c.dom.svg.SVGAElement;

public class LinkActivationEvent extends EventObject {
   protected String referencedURI;

   public LinkActivationEvent(Object source, SVGAElement link, String uri) {
      super(source);
      this.referencedURI = uri;
   }

   public String getReferencedURI() {
      return this.referencedURI;
   }
}
