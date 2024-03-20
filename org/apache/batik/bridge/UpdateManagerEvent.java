package org.apache.batik.bridge;

import java.awt.image.BufferedImage;
import java.util.EventObject;
import java.util.List;

public class UpdateManagerEvent extends EventObject {
   protected BufferedImage image;
   protected List dirtyAreas;
   protected boolean clearPaintingTransform;

   public UpdateManagerEvent(Object source, BufferedImage bi, List das) {
      super(source);
      this.image = bi;
      this.dirtyAreas = das;
      this.clearPaintingTransform = false;
   }

   public UpdateManagerEvent(Object source, BufferedImage bi, List das, boolean cpt) {
      super(source);
      this.image = bi;
      this.dirtyAreas = das;
      this.clearPaintingTransform = cpt;
   }

   public BufferedImage getImage() {
      return this.image;
   }

   public List getDirtyAreas() {
      return this.dirtyAreas;
   }

   public boolean getClearPaintingTransform() {
      return this.clearPaintingTransform;
   }
}
