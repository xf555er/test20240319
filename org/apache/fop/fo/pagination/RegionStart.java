package org.apache.fop.fo.pagination;

import java.awt.Rectangle;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;

public class RegionStart extends RegionSE {
   public RegionStart(FONode parent) {
      super(parent);
   }

   public Rectangle getViewportRectangle(FODimension reldims) {
      PercentBaseContext pageWidthContext = this.getPageWidthContext(0);
      PercentBaseContext pageHeightContext = this.getPageHeightContext(0);
      PercentBaseContext neighbourContext;
      Rectangle vpRect;
      switch (this.getWritingMode().getEnumValue()) {
         case 79:
         default:
            neighbourContext = pageHeightContext;
            vpRect = new Rectangle(0, 0, this.getExtent().getValue(pageWidthContext), reldims.bpd);
            break;
         case 121:
            neighbourContext = pageHeightContext;
            vpRect = new Rectangle(reldims.ipd - this.getExtent().getValue(pageWidthContext), 0, this.getExtent().getValue(pageWidthContext), reldims.bpd);
            break;
         case 140:
         case 203:
            neighbourContext = pageWidthContext;
            vpRect = new Rectangle(0, 0, reldims.bpd, this.getExtent().getValue(pageHeightContext));
      }

      this.adjustIPD(vpRect, this.layoutMaster.getWritingMode(), neighbourContext);
      return vpRect;
   }

   protected String getDefaultRegionName() {
      return "xsl-region-start";
   }

   public String getLocalName() {
      return "region-start";
   }

   public int getNameId() {
      return 61;
   }
}
