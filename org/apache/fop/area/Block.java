package org.apache.fop.area;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Locale;

public class Block extends BlockParent {
   private static final long serialVersionUID = 6843727817993665788L;
   public static final int STACK = 0;
   public static final int RELATIVE = 1;
   public static final int ABSOLUTE = 2;
   public static final int FIXED = 3;
   private int positioning = 0;
   protected transient boolean allowBPDUpdate = true;
   private Locale locale;
   private String location;

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
   }

   public void addBlock(Block block) {
      this.addBlock(block, true);
   }

   public void addBlock(Block block, boolean autoHeight) {
      if (autoHeight && this.allowBPDUpdate && block.isStacked()) {
         this.bpd += block.getAllocBPD();
      }

      this.addChildArea(block);
   }

   public void addLineArea(LineArea line) {
      this.bpd += line.getAllocBPD();
      this.addChildArea(line);
   }

   public void setPositioning(int pos) {
      this.positioning = pos;
   }

   public int getPositioning() {
      return this.positioning;
   }

   public boolean isStacked() {
      return this.getPositioning() == 0 || this.getPositioning() == 1;
   }

   public int getStartIndent() {
      Integer startIndent = (Integer)this.getTrait(Trait.START_INDENT);
      return startIndent != null ? startIndent : 0;
   }

   public int getEndIndent() {
      Integer endIndent = (Integer)this.getTrait(Trait.END_INDENT);
      return endIndent != null ? endIndent : 0;
   }

   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   public Locale getLocale() {
      return this.locale;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public String getLocation() {
      return this.location;
   }

   public int getEffectiveIPD() {
      int eIPD = super.getEffectiveIPD();
      if (eIPD != 0) {
         this.effectiveIPD = eIPD;
      }

      return eIPD;
   }

   public void activateEffectiveIPD() {
      super.activateEffectiveIPD();
      if (this.effectiveIPD != -1) {
         this.ipd = this.effectiveIPD;
      }

   }
}
