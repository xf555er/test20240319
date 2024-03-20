package org.apache.fop.layoutmgr.inline;

import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.flow.PageNumberCitation;

public class PageNumberCitationLayoutManager extends AbstractPageNumberCitationLayoutManager {
   public PageNumberCitationLayoutManager(PageNumberCitation node) {
      super(node);
   }

   protected PageViewport getCitedPage() {
      return this.getPSLM().getFirstPVWithID(this.citation.getRefId());
   }

   protected boolean getReferenceType() {
      return true;
   }
}
