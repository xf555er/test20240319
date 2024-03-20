package org.apache.fop.area;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.WritingModeTraitsGetter;

public class Area extends AreaTreeObject implements Serializable {
   private static final long serialVersionUID = 6342888466142626492L;
   public static final int ORIENT_0 = 0;
   public static final int ORIENT_90 = 1;
   public static final int ORIENT_180 = 2;
   public static final int ORIENT_270 = 3;
   public static final int CLASS_NORMAL = 0;
   public static final int CLASS_FIXED = 1;
   public static final int CLASS_ABSOLUTE = 2;
   public static final int CLASS_BEFORE_FLOAT = 3;
   public static final int CLASS_FOOTNOTE = 4;
   public static final int CLASS_SIDE_FLOAT = 5;
   public static final int CLASS_MAX = 6;
   private int areaClass = 0;
   protected int ipd;
   protected int bpd;
   protected int effectiveIPD = -1;
   protected int bidiLevel = -1;
   protected TreeMap traits;
   protected static final Log log = LogFactory.getLog(Area.class);
   private List changeBarList;

   public List getChangeBarList() {
      return this.changeBarList;
   }

   public void setChangeBarList(List changeBarList) {
      this.changeBarList = changeBarList;
   }

   public int getAreaClass() {
      return this.areaClass;
   }

   public Object clone() throws CloneNotSupportedException {
      Area area = (Area)super.clone();
      if (this.traits != null) {
         area.traits = (TreeMap)this.traits.clone();
      }

      return area;
   }

   public void setAreaClass(int areaClass) {
      this.areaClass = areaClass;
   }

   public void setIPD(int ipd) {
      this.ipd = ipd;
   }

   public int getIPD() {
      return this.ipd;
   }

   public void setBPD(int bpd) {
      this.bpd = bpd;
   }

   public int getBPD() {
      return this.bpd;
   }

   public int getAllocIPD() {
      return this.getBorderAndPaddingWidthStart() + this.getIPD() + this.getBorderAndPaddingWidthEnd();
   }

   public int getEffectiveAllocIPD() {
      return this.getBorderAndPaddingWidthStart() + this.getEffectiveIPD() + this.getBorderAndPaddingWidthEnd();
   }

   public int getAllocBPD() {
      return this.getSpaceBefore() + this.getBorderAndPaddingWidthBefore() + this.getBPD() + this.getBorderAndPaddingWidthAfter() + this.getSpaceAfter();
   }

   public void setBidiLevel(int bidiLevel) {
      this.bidiLevel = bidiLevel;
   }

   public void resetBidiLevel() {
      this.setBidiLevel(-1);
   }

   public int getBidiLevel() {
      return this.bidiLevel;
   }

   public int getBorderAndPaddingWidthBefore() {
      int margin = 0;
      BorderProps bps = (BorderProps)this.getTrait(Trait.BORDER_BEFORE);
      if (bps != null) {
         margin = bps.width;
      }

      Integer padWidth = (Integer)this.getTrait(Trait.PADDING_BEFORE);
      if (padWidth != null) {
         margin += padWidth;
      }

      return margin;
   }

   public int getBorderAndPaddingWidthAfter() {
      int margin = 0;
      BorderProps bps = (BorderProps)this.getTrait(Trait.BORDER_AFTER);
      if (bps != null) {
         margin = bps.width;
      }

      Integer padWidth = (Integer)this.getTrait(Trait.PADDING_AFTER);
      if (padWidth != null) {
         margin += padWidth;
      }

      return margin;
   }

   public int getBorderAndPaddingWidthStart() {
      int margin = 0;
      BorderProps bps = (BorderProps)this.getTrait(Trait.BORDER_START);
      if (bps != null) {
         margin = bps.width;
      }

      Integer padWidth = (Integer)this.getTrait(Trait.PADDING_START);
      if (padWidth != null) {
         margin += padWidth;
      }

      return margin;
   }

   public int getBorderAndPaddingWidthEnd() {
      int margin = 0;
      BorderProps bps = (BorderProps)this.getTrait(Trait.BORDER_END);
      if (bps != null) {
         margin = bps.width;
      }

      Integer padWidth = (Integer)this.getTrait(Trait.PADDING_END);
      if (padWidth != null) {
         margin += padWidth;
      }

      return margin;
   }

   public int getSpaceBefore() {
      int margin = 0;
      Integer space = (Integer)this.getTrait(Trait.SPACE_BEFORE);
      if (space != null) {
         margin = space;
      }

      return margin;
   }

   public int getSpaceAfter() {
      int margin = 0;
      Integer space = (Integer)this.getTrait(Trait.SPACE_AFTER);
      if (space != null) {
         margin = space;
      }

      return margin;
   }

   public int getSpaceStart() {
      int margin = 0;
      Integer space = (Integer)this.getTrait(Trait.SPACE_START);
      if (space != null) {
         margin = space;
      }

      return margin;
   }

   public int getSpaceEnd() {
      int margin = 0;
      Integer space = (Integer)this.getTrait(Trait.SPACE_END);
      if (space != null) {
         margin = space;
      }

      return margin;
   }

   public void addChildArea(Area child) {
   }

   public void addTrait(Integer traitCode, Object prop) {
      if (this.traits == null) {
         this.traits = new TreeMap();
      }

      this.traits.put(traitCode, prop);
   }

   public void setTraits(Map traits) {
      if (traits != null) {
         this.traits = new TreeMap(traits);
      } else {
         this.traits = null;
      }

   }

   public Map getTraits() {
      return this.traits;
   }

   public boolean hasTraits() {
      return this.traits != null;
   }

   public Object getTrait(Integer traitCode) {
      return this.traits != null ? this.traits.get(traitCode) : null;
   }

   public boolean hasTrait(Integer traitCode) {
      return this.getTrait(traitCode) != null;
   }

   public boolean getTraitAsBoolean(Integer traitCode) {
      return Boolean.TRUE.equals(this.getTrait(traitCode));
   }

   public int getTraitAsInteger(Integer traitCode) {
      Object obj = this.getTrait(traitCode);
      if (obj instanceof Integer) {
         return (Integer)obj;
      } else {
         throw new IllegalArgumentException("Trait " + traitCode.getClass().getName() + " could not be converted to an integer");
      }
   }

   public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(super.toString());
      sb.append(" {ipd=").append(Integer.toString(this.getIPD()));
      sb.append(", bpd=").append(Integer.toString(this.getBPD()));
      sb.append("}");
      return sb.toString();
   }

   public int getEffectiveIPD() {
      return 0;
   }

   public void activateEffectiveIPD() {
      if (this.effectiveIPD != -1) {
         this.ipd = this.effectiveIPD;
      }

   }
}
