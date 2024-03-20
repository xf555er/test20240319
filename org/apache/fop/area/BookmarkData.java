package org.apache.fop.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.fop.fo.pagination.bookmarks.Bookmark;
import org.apache.fop.fo.pagination.bookmarks.BookmarkTree;

public class BookmarkData extends AbstractOffDocumentItem implements Resolvable {
   private List subData = new ArrayList();
   private String bookmarkTitle;
   private boolean showChildren = true;
   private String idRef;
   private PageViewport pageRef;
   private Map unresolvedIDRefs = new HashMap();

   public BookmarkData(BookmarkTree bookmarkTree) {
      this.idRef = null;
      this.whenToProcess = 2;
      this.showChildren = true;

      for(int count = 0; count < bookmarkTree.getBookmarks().size(); ++count) {
         Bookmark bkmk = (Bookmark)bookmarkTree.getBookmarks().get(count);
         this.addSubData(this.createBookmarkData(bkmk));
      }

   }

   public BookmarkData(Bookmark bookmark) {
      this.bookmarkTitle = bookmark.getBookmarkTitle();
      this.showChildren = bookmark.showChildItems();
      this.idRef = bookmark.getInternalDestination();
   }

   private void putUnresolved(String id, BookmarkData bd) {
      List refs = (List)this.unresolvedIDRefs.get(id);
      if (refs == null) {
         refs = new ArrayList();
         this.unresolvedIDRefs.put(id, refs);
      }

      ((List)refs).add(bd);
   }

   public BookmarkData() {
      this.idRef = null;
      this.whenToProcess = 2;
      this.showChildren = true;
   }

   public BookmarkData(String title, boolean showChildren, PageViewport pv, String idRef) {
      this.bookmarkTitle = title;
      this.showChildren = showChildren;
      this.pageRef = pv;
      this.idRef = idRef;
   }

   public String getIDRef() {
      return this.idRef;
   }

   public void addSubData(BookmarkData sub) {
      this.subData.add(sub);
      if (sub.pageRef == null) {
         this.putUnresolved(sub.getIDRef(), sub);
         String[] ids = sub.getIDRefs();
         String[] var3 = ids;
         int var4 = ids.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String id = var3[var5];
            this.putUnresolved(id, sub);
         }
      }

   }

   public String getBookmarkTitle() {
      return this.bookmarkTitle;
   }

   public boolean showChildItems() {
      return this.showChildren;
   }

   public int getCount() {
      return this.subData.size();
   }

   public BookmarkData getSubData(int count) {
      return (BookmarkData)this.subData.get(count);
   }

   public PageViewport getPageViewport() {
      return this.pageRef;
   }

   public boolean isResolved() {
      return this.unresolvedIDRefs == null || this.unresolvedIDRefs.size() == 0;
   }

   public String[] getIDRefs() {
      return (String[])this.unresolvedIDRefs.keySet().toArray(new String[this.unresolvedIDRefs.keySet().size()]);
   }

   public void resolveIDRef(String id, List pages) {
      if (id.equals(this.idRef)) {
         this.pageRef = (PageViewport)pages.get(0);
      }

      List refs = (List)this.unresolvedIDRefs.get(id);
      if (refs != null) {
         Iterator var4 = refs.iterator();

         while(var4.hasNext()) {
            Resolvable res = (Resolvable)var4.next();
            res.resolveIDRef(id, pages);
         }
      }

      this.unresolvedIDRefs.remove(id);
   }

   public String getName() {
      return "Bookmarks";
   }

   private BookmarkData createBookmarkData(Bookmark bookmark) {
      BookmarkData data = new BookmarkData(bookmark);

      for(int count = 0; count < bookmark.getChildBookmarks().size(); ++count) {
         Bookmark bkmk = (Bookmark)bookmark.getChildBookmarks().get(count);
         data.addSubData(this.createBookmarkData(bkmk));
      }

      return data;
   }
}
