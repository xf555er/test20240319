package org.apache.fop.fo.pagination;

import java.awt.Rectangle;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;

public class RegionAfter extends RegionBA {
   public RegionAfter(FONode parent) {
      super(parent);
   }

   public Rectangle getViewportRectangle(FODimension reldims) {
      PercentBaseContext pageWidthContext = this.getPageWidthContext(0);
      PercentBaseContext pageHeightContext = this.getPageHeightContext(0);
      PercentBaseContext neighbourContext;
      Rectangle vpRect;
      switch (this.getWritingMode().getEnumValue()) {
         case 79:
         case 121:
         default:
            neighbourContext = pageWidthContext;
            vpRect = new Rectangle(0, reldims.bpd - this.getExtent().getValue(pageHeightContext), reldims.ipd, this.getExtent().getValue(pageHeightContext));
            break;
         case 140:
         case 203:
            neighbourContext = pageHeightContext;
            vpRect = new Rectangle(0, reldims.bpd - this.getExtent().getValue(pageWidthContext), this.getExtent().getValue(pageWidthContext), reldims.ipd);
      }

      if (this.getPrecedence() == 48) {
         this.adjustIPD(vpRect, this.layoutMaster.getWritingMode(), neighbourContext);
      }

      return vpRect;
   }

   protected String getDefaultRegionName() {
      return "xsl-region-after";
   }

   public String getLocalName() {
      return "region-after";
   }

   public int getNameId() {
      return 56;
   }
}
