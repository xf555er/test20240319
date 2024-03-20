package org.apache.fop.layoutmgr.inline;

import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.flow.PageNumberCitationLast;

public class PageNumberCitationLastLayoutManager extends AbstractPageNumberCitationLayoutManager {
   public PageNumberCitationLastLayoutManager(PageNumberCitationLast node) {
      super(node);
   }

   protected PageViewport getCitedPage() {
      return this.getPSLM().associateLayoutManagerID(this.citation.getRefId()) ? this.getPSLM().getLastPVWithID(this.citation.getRefId()) : null;
   }

   protected boolean getReferenceType() {
      return false;
   }
}
