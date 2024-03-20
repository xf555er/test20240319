package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;
import org.apache.fop.fo.pagination.Region;

public class RegionReference extends Area {
   private static final long serialVersionUID = -298980963268244238L;
   private int regionClass;
   private String regionName;
   private CTM ctm;
   private ArrayList blocks;
   protected RegionViewport regionViewport;

   public RegionReference(Region regionFO, RegionViewport parent) {
      this(regionFO.getNameId(), regionFO.getRegionName(), parent);
   }

   public RegionReference(int regionClass, String regionName, RegionViewport parent) {
      this.blocks = new ArrayList();
      this.regionClass = regionClass;
      this.regionName = regionName;
      this.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
      this.regionViewport = parent;
   }

   public void addChildArea(Area child) {
      this.blocks.add(child);
   }

   public void setCTM(CTM ctm) {
      this.ctm = ctm;
   }

   public RegionViewport getRegionViewport() {
      return this.regionViewport;
   }

   public CTM getCTM() {
      return this.ctm;
   }

   public List getBlocks() {
      return this.blocks;
   }

   public int getRegionClass() {
      return this.regionClass;
   }

   public String getRegionName() {
      return this.regionName;
   }

   public void addBlock(Block block) {
      this.addChildArea(block);
   }

   public boolean isEmpty() {
      return true;
   }

   public Object clone() throws CloneNotSupportedException {
      RegionReference rr = (RegionReference)super.clone();
      rr.blocks = (ArrayList)this.blocks.clone();
      return rr;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(super.toString());
      sb.append(" {regionName=").append(this.regionName);
      sb.append(", regionClass=").append(this.regionClass);
      sb.append(", ctm=").append(this.ctm);
      sb.append("}");
      return sb.toString();
   }
}
