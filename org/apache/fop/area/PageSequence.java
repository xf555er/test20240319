package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PageSequence extends AreaTreeObject {
   private List pages = new ArrayList();
   private LineArea title;
   private Locale locale;

   public PageSequence(LineArea title) {
      this.setTitle(title);
   }

   public LineArea getTitle() {
      return this.title;
   }

   public void setTitle(LineArea title) {
      this.title = title;
   }

   public void addPage(PageViewport page) {
      this.pages.add(page);
   }

   public int getPageCount() {
      return this.pages.size();
   }

   public PageViewport getPage(int idx) {
      return (PageViewport)this.pages.get(idx);
   }

   public boolean isFirstPage(PageViewport page) {
      return page.equals(this.getPage(0));
   }

   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   public Locale getLocale() {
      return this.locale;
   }
}
