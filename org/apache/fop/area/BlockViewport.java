package org.apache.fop.area;

import java.awt.Rectangle;

public class BlockViewport extends Block implements Viewport {
   private static final long serialVersionUID = -7840580922580735157L;
   private boolean clip;
   private CTM viewportCTM;

   public BlockViewport() {
      this(false);
   }

   public BlockViewport(boolean allowBPDUpdate) {
      this.allowBPDUpdate = allowBPDUpdate;
   }

   public void setCTM(CTM ctm) {
      this.viewportCTM = ctm;
   }

   public CTM getCTM() {
      return this.viewportCTM;
   }

   public void setClip(boolean cl) {
      this.clip = cl;
   }

   public boolean hasClip() {
      return this.clip;
   }

   public Rectangle getClipRectangle() {
      return this.clip ? new Rectangle(this.getIPD(), this.getBPD()) : null;
   }

   public int getEffectiveIPD() {
      return this.getIPD();
   }
}
