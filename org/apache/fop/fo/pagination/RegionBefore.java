package org.apache.fop.fo.pagination;

import java.awt.Rectangle;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;

public class RegionBefore extends RegionBA {
   public RegionBefore(FONode parent) {
      super(parent);
   }

   protected String getDefaultRegionName() {
      return "xsl-region-before";
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
            vpRect = new Rectangle(0, 0, reldims.ipd, this.getExtent().getValue(pageHeightContext));
            break;
         case 140:
         case 203:
            neighbourContext = pageHeightContext;
            vpRect = new Rectangle(0, 0, this.getExtent().getValue(pageWidthContext), reldims.ipd);
      }

      if (this.getPrecedence() == 48) {
         this.adjustIPD(vpRect, this.layoutMaster.getWritingMode(), neighbourContext);
      }

      return vpRect;
   }

   public String getLocalName() {
      return "region-before";
   }

   public int getNameId() {
      return 57;
   }
}
