package org.apache.fop.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IDTracker {
   private static final Log LOG = LogFactory.getLog(IDTracker.class);
   private Map idLocations = new HashMap();
   private Map unresolvedIDRefs = new HashMap();
   private Set unfinishedIDs = new HashSet();
   private Set alreadyResolvedIDs = new HashSet();

   public void associateIDWithPageViewport(String id, PageViewport pv) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("associateIDWithPageViewport(" + id + ", " + pv + ")");
      }

      List pvList = (List)this.idLocations.get(id);
      if (pvList == null) {
         List pvList = new ArrayList();
         this.idLocations.put(id, pvList);
         pvList.add(pv);
         pv.setFirstWithID(id);
         if (!this.unfinishedIDs.contains(id)) {
            this.tryIDResolution(id, pvList);
         }
      } else if (!pvList.contains(pv)) {
         pvList.add(pv);
      }

   }

   public void signalPendingID(String id) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("signalPendingID(" + id + ")");
      }

      this.unfinishedIDs.add(id);
   }

   public void signalIDProcessed(String id) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("signalIDProcessed(" + id + ")");
      }

      this.alreadyResolvedIDs.add(id);
      if (this.unfinishedIDs.contains(id)) {
         this.unfinishedIDs.remove(id);
         List idLocs = (List)this.idLocations.get(id);
         Set todo = (Set)this.unresolvedIDRefs.get(id);
         if (todo != null) {
            Iterator var4 = todo.iterator();

            while(var4.hasNext()) {
               Resolvable res = (Resolvable)var4.next();
               res.resolveIDRef(id, idLocs);
            }

            this.unresolvedIDRefs.remove(id);
         }

      }
   }

   public boolean alreadyResolvedID(String id) {
      return this.alreadyResolvedIDs.contains(id);
   }

   private void tryIDResolution(String id, List pvList) {
      Set todo = (Set)this.unresolvedIDRefs.get(id);
      if (todo != null) {
         Iterator var4 = todo.iterator();

         while(var4.hasNext()) {
            Resolvable res = (Resolvable)var4.next();
            if (this.unfinishedIDs.contains(id)) {
               return;
            }

            res.resolveIDRef(id, pvList);
         }

         this.alreadyResolvedIDs.add(id);
         this.unresolvedIDRefs.remove(id);
      }

   }

   public void tryIDResolution(PageViewport pv) {
      String[] ids = pv.getIDRefs();
      if (ids != null) {
         String[] var3 = ids;
         int var4 = ids.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String id = var3[var5];
            List pvList = (List)this.idLocations.get(id);
            if (pvList != null && !pvList.isEmpty()) {
               this.tryIDResolution(id, pvList);
            }
         }
      }

   }

   public List getPageViewportsContainingID(String id) {
      if (this.idLocations != null && !this.idLocations.isEmpty()) {
         List idLocs = (List)this.idLocations.get(id);
         if (idLocs != null) {
            return idLocs;
         }
      }

      return Collections.emptyList();
   }

   public PageViewport getFirstPageViewportContaining(String id) {
      List list = this.getPageViewportsContainingID(id);
      return list != null && !list.isEmpty() ? (PageViewport)list.get(0) : null;
   }

   public PageViewport getLastPageViewportContaining(String id) {
      List list = this.getPageViewportsContainingID(id);
      return list != null && !list.isEmpty() ? (PageViewport)list.get(list.size() - 1) : null;
   }

   public void addUnresolvedIDRef(String idref, Resolvable res) {
      Set todo = (Set)this.unresolvedIDRefs.get(idref);
      if (todo == null) {
         todo = new HashSet();
         this.unresolvedIDRefs.put(idref, todo);
      }

      ((Set)todo).add(res);
   }

   public void replacePageViewPort(PageViewport oldPageViewPort, PageViewport newPageViewPort) {
      Iterator var3 = this.idLocations.values().iterator();

      while(var3.hasNext()) {
         List viewPortList = (List)var3.next();
         int i = 0;

         for(int len = viewPortList.size(); i < len; ++i) {
            PageViewport currPV = (PageViewport)viewPortList.get(i);
            if (currPV == oldPageViewPort) {
               viewPortList.set(i, newPageViewPort);
            }
         }
      }

   }
}
