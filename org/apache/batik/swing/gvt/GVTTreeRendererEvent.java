package org.apache.batik.swing.gvt;

import java.awt.image.BufferedImage;
import java.util.EventObject;

public class GVTTreeRendererEvent extends EventObject {
   protected BufferedImage image;

   public GVTTreeRendererEvent(Object source, BufferedImage bi) {
      super(source);
      this.image = bi;
   }

   public BufferedImage getImage() {
      return this.image;
   }
}
