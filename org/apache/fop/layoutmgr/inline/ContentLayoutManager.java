package org.apache.fop.layoutmgr.inline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.layoutmgr.AbstractBaseLayoutManager;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.PageSequenceLayoutManager;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceSpecifier;

public class ContentLayoutManager extends AbstractBaseLayoutManager implements InlineLevelLayoutManager {
   private static Log log = LogFactory.getLog(ContentLayoutManager.class);
   private Area holder;
   private int stackSize;
   private LayoutManager parentLM;
   private InlineLevelLayoutManager childLM;

   public ContentLayoutManager(Area area, LayoutManager parentLM) {
      this.holder = area;
      this.parentLM = parentLM;
   }

   public ContentLayoutManager(PageSequenceLayoutManager pslm, Title foTitle) {
      this.parentLM = pslm;
      this.holder = new LineArea();
      this.holder.setChangeBarList(this.getChangeBarList());

      try {
         LayoutManager lm = pslm.getLayoutManagerMaker().makeLayoutManager(foTitle);
         this.addChildLM(lm);
         this.fillArea(lm);
      } catch (IllegalStateException var4) {
         log.warn("Title has no content");
         throw var4;
      }
   }

   public void initialize() {
   }

   private void fillArea(LayoutManager curLM) {
      int ipd = 1000000;
      LayoutContext childLC = LayoutContext.newInstance();
      childLC.setFlags(1);
      childLC.setLeadingSpace(new SpaceSpecifier(false));
      childLC.setTrailingSpace(new SpaceSpecifier(false));
      childLC.setRefIPD(ipd);
      int lineHeight = 14000;
      int lead = 12000;
      int follow = 2000;
      int halfLeading = (lineHeight - lead - follow) / 2;
      int lineLead = lead + halfLeading;
      int maxtb = follow + halfLeading;
      this.stackSize = 0;
      List contentList = this.getNextKnuthElements(childLC, 135);
      Iterator var12 = contentList.iterator();

      while(var12.hasNext()) {
         Object aContentList = var12.next();
         KnuthElement element = (KnuthElement)aContentList;
         if (element instanceof KnuthInlineBox) {
            KnuthInlineBox var15 = (KnuthInlineBox)element;
         }
      }

      if (maxtb - lineLead > maxtb) {
         int var10000 = maxtb - lineLead;
      }

      LayoutContext lc = LayoutContext.newInstance();
      lc.setFlags(16, true);
      lc.setLeadingSpace(new SpaceSpecifier(false));
      lc.setTrailingSpace(new SpaceSpecifier(false));
      KnuthPossPosIter contentPosIter = new KnuthPossPosIter(contentList, 0, contentList.size());
      curLM.addAreas(contentPosIter, lc);
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      int savedIPD = ((InlineArea)this.holder).getIPD();
      LayoutContext childContext = LayoutContext.copyOf(context);
      childContext.setIPDAdjust(0.0);
      this.childLM.addAreas(posIter, childContext);
      ((InlineArea)this.holder).setIPD(savedIPD);
   }

   public int getStackingSize() {
      return this.stackSize;
   }

   public Area getParentArea(Area childArea) {
      return this.holder;
   }

   public void addChildArea(Area childArea) {
      this.holder.addChildArea(childArea);
   }

   public void setParent(LayoutManager lm) {
      this.parentLM = lm;
   }

   public LayoutManager getParent() {
      return this.parentLM;
   }

   public boolean isFinished() {
      return false;
   }

   public void setFinished(boolean isFinished) {
   }

   public boolean createNextChildLMs(int pos) {
      return false;
   }

   public List getChildLMs() {
      List childLMs = new ArrayList(1);
      childLMs.add(this.childLM);
      return childLMs;
   }

   public void addChildLM(LayoutManager lm) {
      if (lm != null) {
         lm.setParent(this);
         this.childLM = (InlineLevelLayoutManager)lm;
         log.trace(this.getClass().getName() + ": Adding child LM " + lm.getClass().getName());
      }
   }

   public void addChildLMs(List newLMs) {
      if (newLMs != null && newLMs.size() != 0) {
         Iterator var2 = newLMs.iterator();

         while(var2.hasNext()) {
            Object newLM = var2.next();
            LayoutManager lm = (LayoutManager)newLM;
            this.addChildLM(lm);
         }

      }
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      List contentList = new LinkedList();
      this.childLM.initialize();

      label36:
      while(true) {
         List returnedList;
         do {
            if (this.childLM.isFinished()) {
               this.setFinished(true);
               return contentList;
            }

            returnedList = this.childLM.getNextKnuthElements(context, alignment);
         } while(returnedList == null);

         while(true) {
            while(true) {
               if (returnedList.size() <= 0) {
                  continue label36;
               }

               Object obj = returnedList.remove(0);
               KnuthElement contentElement;
               if (obj instanceof KnuthSequence) {
                  KnuthSequence ks = (KnuthSequence)obj;
                  Iterator var8 = ks.iterator();

                  while(var8.hasNext()) {
                     Object k = var8.next();
                     contentElement = (KnuthElement)k;
                     this.stackSize += contentElement.getWidth();
                     contentList.add(contentElement);
                  }
               } else {
                  contentElement = (KnuthElement)obj;
                  this.stackSize += contentElement.getWidth();
                  contentList.add(contentElement);
               }
            }
         }
      }
   }

   public List addALetterSpaceTo(List oldList) {
      return oldList;
   }

   public List addALetterSpaceTo(List oldList, int depth) {
      return this.addALetterSpaceTo(oldList);
   }

   public String getWordChars(Position pos) {
      return "";
   }

   public void hyphenate(Position pos, HyphContext hc) {
   }

   public boolean applyChanges(List oldList) {
      return false;
   }

   public boolean applyChanges(List oldList, int depth) {
      return this.applyChanges(oldList);
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      return null;
   }

   public List getChangedKnuthElements(List oldList, int alignment, int depth) {
      return this.getChangedKnuthElements(oldList, alignment);
   }

   public PageSequenceLayoutManager getPSLM() {
      return this.parentLM.getPSLM();
   }

   public boolean hasLineAreaDescendant() {
      return true;
   }

   public int getBaselineOffset() {
      return this.childLM.getBaselineOffset();
   }

   public int getContentAreaIPD() {
      return this.holder.getIPD();
   }

   public int getContentAreaBPD() {
      return this.holder.getBPD();
   }

   public boolean getGeneratesReferenceArea() {
      return false;
   }

   public boolean getGeneratesBlockArea() {
      return this.getGeneratesLineArea() || this.holder instanceof Block;
   }

   public boolean getGeneratesLineArea() {
      return this.holder instanceof LineArea;
   }

   public Position notifyPos(Position pos) {
      return pos;
   }
}
