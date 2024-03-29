package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.AreaTreeObject;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.RetrieveMarker;

public abstract class AbstractLayoutManager extends AbstractBaseLayoutManager implements Constants {
   private static Log log = LogFactory.getLog(AbstractLayoutManager.class);
   protected LayoutManager parentLayoutManager;
   protected List childLMs;
   protected ListIterator fobjIter;
   private Map markers;
   private boolean isFinished;
   protected LayoutManager curChildLM;
   protected ListIterator childLMiter;
   private int lastGeneratedPosition = -1;
   private int smallestPosNumberChecked = Integer.MAX_VALUE;
   private boolean preserveChildrenAtEndOfLayout;

   public AbstractLayoutManager() {
   }

   public AbstractLayoutManager(FObj fo) {
      super(fo);
      this.markers = fo.getMarkers();
      this.fobjIter = fo.getChildNodes();
      this.childLMiter = new LMiter(this);
   }

   public void setParent(LayoutManager lm) {
      this.parentLayoutManager = lm;
   }

   public LayoutManager getParent() {
      return this.parentLayoutManager;
   }

   public void initialize() {
   }

   protected LayoutManager getChildLM() {
      if (this.curChildLM != null && !this.curChildLM.isFinished()) {
         return this.curChildLM;
      } else if (this.childLMiter.hasNext()) {
         this.curChildLM = (LayoutManager)this.childLMiter.next();
         this.curChildLM.initialize();
         return this.curChildLM;
      } else {
         return null;
      }
   }

   protected void setCurrentChildLM(LayoutManager childLM) {
      this.curChildLM = childLM;
      this.childLMiter = new LMiter(this);

      do {
         this.curChildLM = (LayoutManager)this.childLMiter.next();
      } while(this.curChildLM != childLM);

   }

   protected boolean hasNextChildLM() {
      return this.childLMiter.hasNext();
   }

   public boolean isFinished() {
      return this.isFinished;
   }

   public void setFinished(boolean fin) {
      this.isFinished = fin;
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      log.warn("null implementation of getNextKnuthElements() called!");
      this.setFinished(true);
      return null;
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      log.warn("null implementation of getChangeKnuthElement() called!");
      return null;
   }

   public Area getParentArea(Area childArea) {
      return null;
   }

   public void addChildArea(Area childArea) {
   }

   protected List createChildLMs(int size) {
      if (this.fobjIter == null) {
         return null;
      } else {
         List newLMs = new ArrayList(size);

         while(this.fobjIter.hasNext() && newLMs.size() < size) {
            Object theobj = this.fobjIter.next();
            if (theobj instanceof FONode) {
               FONode foNode = (FONode)theobj;
               if (foNode instanceof RetrieveMarker) {
                  foNode = this.getPSLM().resolveRetrieveMarker((RetrieveMarker)foNode);
               }

               if (foNode != null) {
                  this.getPSLM().getLayoutManagerMaker().makeLayoutManagers((FONode)foNode, newLMs);
               }
            }
         }

         return newLMs;
      }
   }

   public PageSequenceLayoutManager getPSLM() {
      return this.parentLayoutManager.getPSLM();
   }

   public Page getCurrentPage() {
      return this.getPSLM().getCurrentPage();
   }

   public PageViewport getCurrentPV() {
      return this.getPSLM().getCurrentPage().getPageViewport();
   }

   public boolean createNextChildLMs(int pos) {
      List newLMs = this.createChildLMs(pos + 1 - this.childLMs.size());
      this.addChildLMs(newLMs);
      return pos < this.childLMs.size();
   }

   public List getChildLMs() {
      if (this.childLMs == null) {
         this.childLMs = new ArrayList(10);
      }

      return this.childLMs;
   }

   public void addChildLM(LayoutManager lm) {
      if (lm != null) {
         lm.setParent(this);
         if (this.childLMs == null) {
            this.childLMs = new ArrayList(10);
         }

         this.childLMs.add(lm);
         if (log.isTraceEnabled()) {
            log.trace(this.getClass().getName() + ": Adding child LM " + lm.getClass().getName());
         }

      }
   }

   public void addChildLMs(List newLMs) {
      if (newLMs != null && newLMs.size() != 0) {
         Iterator var2 = newLMs.iterator();

         while(var2.hasNext()) {
            LayoutManager newLM = (LayoutManager)var2.next();
            this.addChildLM(newLM);
         }

      }
   }

   public Position notifyPos(Position pos) {
      if (pos.getIndex() >= 0) {
         throw new IllegalStateException("Position already got its index");
      } else {
         pos.setIndex(++this.lastGeneratedPosition);
         return pos;
      }
   }

   private void verifyNonNullPosition(Position pos) {
      if (pos == null || pos.getIndex() < 0) {
         throw new IllegalArgumentException("Only non-null Positions with an index can be checked");
      }
   }

   public boolean isFirst(Position pos) {
      this.verifyNonNullPosition(pos);
      if (pos.getIndex() == this.smallestPosNumberChecked) {
         return true;
      } else if (pos.getIndex() < this.smallestPosNumberChecked) {
         this.smallestPosNumberChecked = pos.getIndex();
         return true;
      } else {
         return false;
      }
   }

   public boolean isLast(Position pos) {
      this.verifyNonNullPosition(pos);
      return pos.getIndex() == this.lastGeneratedPosition && this.isFinished();
   }

   public boolean hasLineAreaDescendant() {
      if (this.childLMs != null && !this.childLMs.isEmpty()) {
         Iterator var1 = this.childLMs.iterator();

         LayoutManager childLM;
         do {
            if (!var1.hasNext()) {
               return false;
            }

            childLM = (LayoutManager)var1.next();
         } while(!childLM.hasLineAreaDescendant());

         return true;
      } else {
         return false;
      }
   }

   public int getBaselineOffset() {
      if (this.childLMs != null) {
         Iterator var1 = this.childLMs.iterator();

         while(var1.hasNext()) {
            LayoutManager childLM = (LayoutManager)var1.next();
            if (childLM.hasLineAreaDescendant()) {
               return childLM.getBaselineOffset();
            }
         }
      }

      throw this.newNoLineAreaDescendantException();
   }

   protected IllegalStateException newNoLineAreaDescendantException() {
      return new IllegalStateException("getBaselineOffset called on an object that has no line-area descendant");
   }

   protected void transferForeignAttributes(AreaTreeObject targetArea) {
      Map atts = this.fobj.getForeignAttributes();
      targetArea.setForeignAttributes(atts);
   }

   protected void transferExtensionAttachments(AreaTreeObject targetArea) {
      if (this.fobj.hasExtensionAttachments()) {
         targetArea.setExtensionAttachments(this.fobj.getExtensionAttachments());
      }

   }

   protected void transferExtensions(AreaTreeObject targetArea) {
      this.transferForeignAttributes(targetArea);
      this.transferExtensionAttachments(targetArea);
   }

   protected void registerMarkers(boolean isStarting, boolean isFirst, boolean isLast) {
      if (this.markers != null) {
         this.getCurrentPV().registerMarkers(this.markers, isStarting, isFirst, isLast);
         this.possiblyRegisterMarkersForTables(this.markers, isStarting, isFirst, isLast);
      }

   }

   protected void addId() {
      if (this.fobj != null) {
         this.getPSLM().addIDToPage(this.fobj.getId());
      }

   }

   protected void notifyEndOfLayout() {
      if (this.fobj != null) {
         this.getPSLM().notifyEndOfLayout(this.fobj.getId());
      }

   }

   protected void checkEndOfLayout(Position pos) {
      if (pos != null && pos.getLM() == this && this.isLast(pos)) {
         this.notifyEndOfLayout();
         if (!this.preserveChildrenAtEndOfLayout) {
            this.childLMs = null;
            this.curChildLM = null;
            this.childLMiter = null;
         }

         this.markers = null;

         LayoutManager lm;
         for(lm = this.parentLayoutManager; !(lm instanceof FlowLayoutManager) && !(lm instanceof PageSequenceLayoutManager); lm = lm.getParent()) {
         }

         if (lm instanceof FlowLayoutManager && !this.preserveChildrenAtEndOfLayout) {
            this.fobj.clearChildNodes();
            this.fobjIter = null;
         }
      }

   }

   public void preserveChildrenAtEndOfLayout() {
      this.preserveChildrenAtEndOfLayout = true;
   }

   public String toString() {
      return super.toString() + (this.fobj != null ? "{fobj = " + this.fobj.toString() + "}" : "");
   }

   public void reset() {
      this.isFinished = false;
      this.curChildLM = null;
      this.childLMiter = new LMiter(this);
      Iterator var1 = this.getChildLMs().iterator();

      while(var1.hasNext()) {
         LayoutManager childLM = (LayoutManager)var1.next();
         childLM.reset();
      }

      if (this.fobj != null) {
         this.markers = this.fobj.getMarkers();
      }

      this.lastGeneratedPosition = -1;
   }

   public void recreateChildrenLMs() {
      this.childLMs = new ArrayList();
      this.isFinished = false;
      if (this.fobj != null) {
         this.fobjIter = this.fobj.getChildNodes();
         int position = 0;

         while(this.createNextChildLMs(position++)) {
         }

         this.childLMiter = new LMiter(this);
         LMiter iter = new LMiter(this);

         while(iter.hasNext()) {
            AbstractBaseLayoutManager alm = (AbstractBaseLayoutManager)iter.next();
            alm.initialize();
            alm.recreateChildrenLMs();
            alm.preserveChildrenAtEndOfLayout();
         }

         this.curChildLM = this.getChildLM();
      }
   }

   protected void possiblyRegisterMarkersForTables(Map markers, boolean isStarting, boolean isFirst, boolean isLast) {
      LayoutManager lm = this.parentLayoutManager;
      if (!(lm instanceof FlowLayoutManager) && !(lm instanceof PageSequenceLayoutManager) && lm instanceof AbstractLayoutManager) {
         ((AbstractLayoutManager)lm).possiblyRegisterMarkersForTables(markers, isStarting, isFirst, isLast);
      }
   }

   public boolean handlingFloat() {
      return this.parentLayoutManager != null && this.parentLayoutManager instanceof AbstractLayoutManager ? ((AbstractLayoutManager)this.parentLayoutManager).handlingFloat() : false;
   }
}
