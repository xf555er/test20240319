package org.apache.fop.area;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.flow.AbstractRetrieveMarker;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.Markers;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.traits.WritingModeTraitsGetter;

public class PageViewport extends AreaTreeObject implements Resolvable {
   private Page page;
   private Rectangle viewArea;
   private String simplePageMasterName;
   private String pageKey;
   private int pageNumber;
   private String pageNumberString;
   private int pageIndex;
   private boolean blank;
   private transient PageSequence pageSequence;
   private Set idFirsts;
   private Map unresolvedIDRefs;
   private Map pendingResolved;
   private Markers pageMarkers;
   protected static final Log log = LogFactory.getLog(PageViewport.class);

   public PageViewport(SimplePageMaster spm, int pageNumber, String pageStr, boolean blank, boolean spanAll) {
      this.pageNumber = -1;
      this.pageIndex = -1;
      this.idFirsts = new HashSet();
      this.unresolvedIDRefs = new HashMap();
      this.simplePageMasterName = spm.getMasterName();
      this.setExtensionAttachments(spm.getExtensionAttachments());
      this.setForeignAttributes(spm.getForeignAttributes());
      this.blank = blank;
      int pageWidth = spm.getPageWidth().getValue();
      int pageHeight = spm.getPageHeight().getValue();
      this.pageNumber = pageNumber;
      this.pageNumberString = pageStr;
      this.viewArea = new Rectangle(0, 0, pageWidth, pageHeight);
      this.page = new Page(spm);
      this.createSpan(spanAll);
   }

   public PageViewport(SimplePageMaster spm, int pageNumber, String pageStr, boolean blank) {
      this(spm, pageNumber, pageStr, blank, false);
   }

   public PageViewport(PageViewport original) throws FOPException {
      this.pageNumber = -1;
      this.pageIndex = -1;
      this.idFirsts = new HashSet();
      this.unresolvedIDRefs = new HashMap();
      if (original.extensionAttachments != null) {
         this.setExtensionAttachments(original.extensionAttachments);
      }

      if (original.foreignAttributes != null) {
         this.setForeignAttributes(original.foreignAttributes);
      }

      this.pageIndex = original.pageIndex;
      this.pageNumber = original.pageNumber;
      this.pageNumberString = original.pageNumberString;

      try {
         this.page = (Page)original.page.clone();
      } catch (CloneNotSupportedException var3) {
         throw new FOPException(var3);
      }

      this.viewArea = new Rectangle(original.viewArea);
      this.simplePageMasterName = original.simplePageMasterName;
      this.blank = original.blank;
   }

   public PageViewport(Rectangle viewArea, int pageNumber, String pageStr, String simplePageMasterName, boolean blank) {
      this.pageNumber = -1;
      this.pageIndex = -1;
      this.idFirsts = new HashSet();
      this.unresolvedIDRefs = new HashMap();
      this.viewArea = viewArea;
      this.pageNumber = pageNumber;
      this.pageNumberString = pageStr;
      this.simplePageMasterName = simplePageMasterName;
      this.blank = blank;
   }

   public void setPageSequence(PageSequence seq) {
      this.pageSequence = seq;
   }

   public PageSequence getPageSequence() {
      return this.pageSequence;
   }

   public Rectangle getViewArea() {
      return this.viewArea;
   }

   public Page getPage() {
      return this.page;
   }

   public void setPage(Page page) {
      this.page = page;
   }

   public int getPageNumber() {
      return this.pageNumber;
   }

   public String getPageNumberString() {
      return this.pageNumberString;
   }

   public void setPageIndex(int index) {
      this.pageIndex = index;
   }

   public int getPageIndex() {
      return this.pageIndex;
   }

   public void setKey(String key) {
      this.pageKey = key;
   }

   public String getKey() {
      if (this.pageKey == null) {
         throw new IllegalStateException("No page key set on the PageViewport: " + this.toString());
      } else {
         return this.pageKey;
      }
   }

   public void setFirstWithID(String id) {
      if (id != null) {
         this.idFirsts.add(id);
      }

   }

   public boolean isFirstWithID(String id) {
      return this.idFirsts.contains(id);
   }

   public void replace(PageViewport oldViewPort) {
      this.idFirsts.addAll(oldViewPort.idFirsts);
      this.unresolvedIDRefs.putAll(oldViewPort.unresolvedIDRefs);
      if (oldViewPort.pendingResolved != null) {
         this.pendingResolved.putAll(oldViewPort.pendingResolved);
      }

   }

   public void addUnresolvedIDRef(String idref, Resolvable res) {
      if (this.unresolvedIDRefs == null) {
         this.unresolvedIDRefs = new HashMap();
      }

      List pageViewports = (List)this.unresolvedIDRefs.get(idref);
      if (pageViewports == null) {
         pageViewports = new ArrayList();
         this.unresolvedIDRefs.put(idref, pageViewports);
      }

      ((List)pageViewports).add(res);
   }

   public boolean isResolved() {
      return this.unresolvedIDRefs == null || this.unresolvedIDRefs.size() == 0;
   }

   public String[] getIDRefs() {
      return this.unresolvedIDRefs == null ? null : (String[])this.unresolvedIDRefs.keySet().toArray(new String[this.unresolvedIDRefs.keySet().size()]);
   }

   public void resolveIDRef(String id, List pages) {
      if (this.page == null) {
         if (this.pendingResolved == null) {
            this.pendingResolved = new HashMap();
         }

         this.pendingResolved.put(id, pages);
      } else if (this.unresolvedIDRefs != null) {
         List todo = (List)this.unresolvedIDRefs.get(id);
         if (todo != null) {
            Iterator var4 = todo.iterator();

            while(var4.hasNext()) {
               Resolvable res = (Resolvable)var4.next();
               res.resolveIDRef(id, pages);
            }
         }
      }

      if (this.unresolvedIDRefs != null && pages != null) {
         this.unresolvedIDRefs.remove(id);
         if (this.unresolvedIDRefs.isEmpty()) {
            this.unresolvedIDRefs = null;
         }
      }

   }

   public void registerMarkers(Map marks, boolean starting, boolean isfirst, boolean islast) {
      if (this.pageMarkers == null) {
         this.pageMarkers = new Markers();
      }

      this.pageMarkers.register(marks, starting, isfirst, islast);
   }

   public Marker resolveMarker(AbstractRetrieveMarker rm) {
      return this.pageMarkers == null ? null : this.pageMarkers.resolve(rm);
   }

   public void dumpMarkers() {
      if (this.pageMarkers != null) {
         this.pageMarkers.dump();
      }

   }

   public void savePage(ObjectOutputStream out) throws IOException {
      this.page.setUnresolvedReferences(this.unresolvedIDRefs);
      out.writeObject(this.page);
      this.page = null;
   }

   public void loadPage(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.page = (Page)in.readObject();
      this.unresolvedIDRefs = this.page.getUnresolvedReferences();
      if (this.unresolvedIDRefs != null && this.pendingResolved != null) {
         Iterator var2 = this.pendingResolved.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry e = (Map.Entry)var2.next();
            this.resolveIDRef((String)e.getKey(), (List)e.getValue());
         }

         this.pendingResolved = null;
      }

   }

   public Object clone() throws CloneNotSupportedException {
      PageViewport pvp = (PageViewport)super.clone();
      pvp.page = (Page)this.page.clone();
      pvp.viewArea = (Rectangle)this.viewArea.clone();
      return pvp;
   }

   public void clear() {
      this.page = null;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("PageViewport: page=");
      sb.append(this.getPageNumberString());
      return sb.toString();
   }

   public String getSimplePageMasterName() {
      return this.simplePageMasterName;
   }

   public boolean isBlank() {
      return this.blank;
   }

   public BodyRegion getBodyRegion() {
      RegionReference regionReference = this.getPage().getRegionViewport(58).getRegionReference();

      assert regionReference instanceof BodyRegion;

      return (BodyRegion)regionReference;
   }

   public Span createSpan(boolean spanAll) {
      return this.getBodyRegion().getMainReference().createSpan(spanAll);
   }

   public Span getCurrentSpan() {
      return this.getBodyRegion().getMainReference().getCurrentSpan();
   }

   public NormalFlow getCurrentFlow() {
      return this.getCurrentSpan().getCurrentFlow();
   }

   public NormalFlow moveToNextFlow() {
      return this.getCurrentSpan().moveToNextFlow();
   }

   public RegionReference getRegionReference(int id) {
      return this.getPage().getRegionViewport(id).getRegionReference();
   }

   public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
      if (this.page != null) {
         this.page.setWritingModeTraits(wmtg);
      }

   }
}
