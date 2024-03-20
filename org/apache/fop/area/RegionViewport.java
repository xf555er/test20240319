package org.apache.fop.area;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeMap;
import org.apache.fop.traits.WritingModeTraitsGetter;

public class RegionViewport extends Area implements Viewport {
   private static final long serialVersionUID = 505781815165102572L;
   private RegionReference regionReference;
   private Rectangle2D viewArea;
   private boolean clip;

   public RegionViewport(Rectangle2D viewArea) {
      this.viewArea = viewArea;
      this.addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
   }

   public void setRegionReference(RegionReference reg) {
      this.regionReference = reg;
   }

   public RegionReference getRegionReference() {
      return this.regionReference;
   }

   public void setClip(boolean c) {
      this.clip = c;
   }

   public boolean hasClip() {
      return this.clip;
   }

   public Rectangle getClipRectangle() {
      return this.clip ? new Rectangle(this.getIPD(), this.getBPD()) : null;
   }

   public Rectangle2D getViewArea() {
      return this.viewArea;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeFloat((float)this.viewArea.getX());
      out.writeFloat((float)this.viewArea.getY());
      out.writeFloat((float)this.viewArea.getWidth());
      out.writeFloat((float)this.viewArea.getHeight());
      out.writeBoolean(this.clip);
      out.writeObject(this.traits);
      out.writeObject(this.regionReference);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.viewArea = new Rectangle2D.Float(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
      this.clip = in.readBoolean();
      this.traits = (TreeMap)in.readObject();
      this.setRegionReference((RegionReference)in.readObject());
   }

   public Object clone() throws CloneNotSupportedException {
      RegionViewport rv = (RegionViewport)super.clone();
      rv.regionReference = (RegionReference)this.regionReference.clone();
      rv.viewArea = (Rectangle2D)this.viewArea.clone();
      return rv;
   }

   public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
      if (this.regionReference != null) {
         this.regionReference.setWritingModeTraits(wmtg);
      }

   }
}
