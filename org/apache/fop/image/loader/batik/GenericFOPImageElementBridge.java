package org.apache.fop.image.loader.batik;

import org.apache.fop.svg.AbstractFOPImageElementBridge;
import org.apache.xmlgraphics.image.loader.ImageFlavor;

class GenericFOPImageElementBridge extends AbstractFOPImageElementBridge {
   private final ImageFlavor[] supportedFlavors;

   public GenericFOPImageElementBridge() {
      this.supportedFlavors = new ImageFlavor[]{ImageFlavor.GRAPHICS2D, BatikImageFlavors.SVG_DOM};
   }

   protected ImageFlavor[] getSupportedFlavours() {
      return this.supportedFlavors;
   }
}
