package org.apache.fop.layoutmgr;

import java.awt.Rectangle;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.pagination.SimplePageMaster;

public class Page {
   private SimplePageMaster spm;
   private PageViewport pageViewport;
   protected boolean isPagePositionOnly;

   public Page(SimplePageMaster spm, int pageNumber, String pageNumberStr, boolean blank, boolean spanAll, boolean isPagePositionOnly) {
      this.spm = spm;
      this.pageViewport = new PageViewport(spm, pageNumber, pageNumberStr, blank, spanAll);
      this.isPagePositionOnly = isPagePositionOnly;
   }

   public Page(Rectangle viewArea, int pageNumber, String pageNumberStr, boolean blank) {
      this.spm = null;
      this.pageViewport = new PageViewport(viewArea, pageNumber, pageNumberStr, (String)null, blank);
   }

   public SimplePageMaster getSimplePageMaster() {
      return this.spm;
   }

   public PageViewport getPageViewport() {
      return this.pageViewport;
   }
}
