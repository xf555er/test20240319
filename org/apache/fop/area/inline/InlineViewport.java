package org.apache.fop.area.inline;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeMap;
import org.apache.fop.area.Area;
import org.apache.fop.area.Viewport;

public class InlineViewport extends InlineArea implements Viewport {
   private static final long serialVersionUID = 813338534627918689L;
   private Area content;
   private boolean clip;
   private Rectangle2D contentPosition;

   public InlineViewport(Area child) {
      this(child, -1);
   }

   public InlineViewport(Area child, int bidiLevel) {
      super(0, bidiLevel);
      this.content = child;
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

   public void setContentPosition(Rectangle2D cp) {
      this.contentPosition = cp;
   }

   public Rectangle2D getContentPosition() {
      return this.contentPosition;
   }

   public void setContent(Area content) {
      this.content = content;
   }

   public Area getContent() {
      return this.content;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeBoolean(this.contentPosition != null);
      if (this.contentPosition != null) {
         out.writeFloat((float)this.contentPosition.getX());
         out.writeFloat((float)this.contentPosition.getY());
         out.writeFloat((float)this.contentPosition.getWidth());
         out.writeFloat((float)this.contentPosition.getHeight());
      }

      out.writeBoolean(this.clip);
      out.writeObject(this.traits);
      out.writeObject(this.content);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      if (in.readBoolean()) {
         this.contentPosition = new Rectangle2D.Float(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
      }

      this.clip = in.readBoolean();
      this.traits = (TreeMap)in.readObject();
      this.content = (Area)in.readObject();
   }

   public int getEffectiveIPD() {
      return this.getIPD();
   }
}
