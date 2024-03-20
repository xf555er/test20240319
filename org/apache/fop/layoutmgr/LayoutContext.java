package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.fop.layoutmgr.inline.AlignmentContext;
import org.apache.fop.layoutmgr.inline.HyphContext;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.WritingMode;

public final class LayoutContext {
   public static final int NEW_AREA = 1;
   public static final int SUPPRESS_BREAK_BEFORE = 2;
   public static final int FIRST_AREA = 4;
   public static final int LAST_AREA = 8;
   public static final int RESOLVE_LEADING_SPACE = 16;
   private static final int TREAT_AS_ARTIFACT = 32;
   private int flags;
   private MinOptMax stackLimitBP;
   private int currentSpan = 0;
   private int nextSpan = 0;
   private int refIPD;
   private WritingMode writingMode;
   private SpaceSpecifier trailingSpace;
   private SpaceSpecifier leadingSpace;
   private List pendingAfterMarks;
   private List pendingBeforeMarks;
   private HyphContext hyphContext;
   private int bpAlignment;
   private double ipdAdjust;
   private double dSpaceAdjust;
   private AlignmentContext alignmentContext;
   private int spaceBefore;
   private int spaceAfter;
   private int lineStartBorderAndPaddingWidth;
   private int lineEndBorderAndPaddingWidth;
   private int breakBefore;
   private int breakAfter;
   private Keep pendingKeepWithNext;
   private Keep pendingKeepWithPrevious;
   private int disableColumnBalancing;

   public static LayoutContext newInstance() {
      return new LayoutContext(0);
   }

   public static LayoutContext copyOf(LayoutContext copy) {
      return new LayoutContext(copy);
   }

   public static LayoutContext offspringOf(LayoutContext parent) {
      LayoutContext offspring = new LayoutContext(0);
      offspring.setTreatAsArtifact(parent.treatAsArtifact());
      return offspring;
   }

   private LayoutContext(LayoutContext parentLC) {
      this.writingMode = WritingMode.LR_TB;
      this.bpAlignment = 135;
      this.pendingKeepWithNext = Keep.KEEP_AUTO;
      this.pendingKeepWithPrevious = Keep.KEEP_AUTO;
      this.flags = parentLC.flags;
      this.refIPD = parentLC.refIPD;
      this.writingMode = parentLC.writingMode;
      this.setStackLimitBP(parentLC.getStackLimitBP());
      this.leadingSpace = parentLC.leadingSpace;
      this.trailingSpace = parentLC.trailingSpace;
      this.hyphContext = parentLC.hyphContext;
      this.bpAlignment = parentLC.bpAlignment;
      this.dSpaceAdjust = parentLC.dSpaceAdjust;
      this.ipdAdjust = parentLC.ipdAdjust;
      this.alignmentContext = parentLC.alignmentContext;
      this.lineStartBorderAndPaddingWidth = parentLC.lineStartBorderAndPaddingWidth;
      this.lineEndBorderAndPaddingWidth = parentLC.lineEndBorderAndPaddingWidth;
      this.copyPendingMarksFrom(parentLC);
      this.pendingKeepWithNext = parentLC.pendingKeepWithNext;
      this.pendingKeepWithPrevious = parentLC.pendingKeepWithPrevious;
      this.disableColumnBalancing = parentLC.disableColumnBalancing;
   }

   private LayoutContext(int flags) {
      this.writingMode = WritingMode.LR_TB;
      this.bpAlignment = 135;
      this.pendingKeepWithNext = Keep.KEEP_AUTO;
      this.pendingKeepWithPrevious = Keep.KEEP_AUTO;
      this.flags = flags;
      this.refIPD = 0;
      this.stackLimitBP = MinOptMax.ZERO;
      this.leadingSpace = null;
      this.trailingSpace = null;
   }

   public void copyPendingMarksFrom(LayoutContext source) {
      if (source.pendingAfterMarks != null) {
         this.pendingAfterMarks = new ArrayList(source.pendingAfterMarks);
      }

      if (source.pendingBeforeMarks != null) {
         this.pendingBeforeMarks = new ArrayList(source.pendingBeforeMarks);
      }

   }

   public void setFlags(int flags) {
      this.setFlags(flags, true);
   }

   public void setFlags(int flags, boolean bSet) {
      if (bSet) {
         this.flags |= flags;
      } else {
         this.flags &= ~flags;
      }

   }

   public void unsetFlags(int flags) {
      this.setFlags(flags, false);
   }

   public boolean isStart() {
      return (this.flags & 1) != 0;
   }

   public boolean startsNewArea() {
      return (this.flags & 1) != 0 && this.leadingSpace != null;
   }

   public boolean isFirstArea() {
      return (this.flags & 4) != 0;
   }

   public boolean isLastArea() {
      return (this.flags & 8) != 0;
   }

   public boolean suppressBreakBefore() {
      return (this.flags & 2) != 0;
   }

   public Keep getKeepWithNextPending() {
      return this.pendingKeepWithNext;
   }

   public Keep getKeepWithPreviousPending() {
      return this.pendingKeepWithPrevious;
   }

   public void clearKeepWithNextPending() {
      this.pendingKeepWithNext = Keep.KEEP_AUTO;
   }

   public void clearKeepWithPreviousPending() {
      this.pendingKeepWithPrevious = Keep.KEEP_AUTO;
   }

   public void clearKeepsPending() {
      this.clearKeepWithPreviousPending();
      this.clearKeepWithNextPending();
   }

   public void updateKeepWithNextPending(Keep keep) {
      this.pendingKeepWithNext = this.pendingKeepWithNext.compare(keep);
   }

   public void updateKeepWithPreviousPending(Keep keep) {
      this.pendingKeepWithPrevious = this.pendingKeepWithPrevious.compare(keep);
   }

   public boolean isKeepWithNextPending() {
      return !this.getKeepWithNextPending().isAuto();
   }

   public boolean isKeepWithPreviousPending() {
      return !this.getKeepWithPreviousPending().isAuto();
   }

   public void setLeadingSpace(SpaceSpecifier space) {
      this.leadingSpace = space;
   }

   public SpaceSpecifier getLeadingSpace() {
      return this.leadingSpace;
   }

   public boolean resolveLeadingSpace() {
      return (this.flags & 16) != 0;
   }

   public void setTrailingSpace(SpaceSpecifier space) {
      this.trailingSpace = space;
   }

   public SpaceSpecifier getTrailingSpace() {
      return this.trailingSpace;
   }

   public void addPendingAfterMark(UnresolvedListElementWithLength element) {
      if (this.pendingAfterMarks == null) {
         this.pendingAfterMarks = new ArrayList();
      }

      this.pendingAfterMarks.add(element);
   }

   public List getPendingAfterMarks() {
      return this.pendingAfterMarks != null ? Collections.unmodifiableList(this.pendingAfterMarks) : null;
   }

   public void clearPendingMarks() {
      this.pendingBeforeMarks = null;
      this.pendingAfterMarks = null;
   }

   public void addPendingBeforeMark(UnresolvedListElementWithLength element) {
      if (this.pendingBeforeMarks == null) {
         this.pendingBeforeMarks = new ArrayList();
      }

      this.pendingBeforeMarks.add(element);
   }

   public List getPendingBeforeMarks() {
      return this.pendingBeforeMarks != null ? Collections.unmodifiableList(this.pendingBeforeMarks) : null;
   }

   public void setStackLimitBP(MinOptMax limit) {
      this.stackLimitBP = limit;
   }

   public MinOptMax getStackLimitBP() {
      return this.stackLimitBP;
   }

   public void setRefIPD(int ipd) {
      this.refIPD = ipd;
   }

   public int getRefIPD() {
      return this.refIPD;
   }

   public void setHyphContext(HyphContext hyph) {
      this.hyphContext = hyph;
   }

   public HyphContext getHyphContext() {
      return this.hyphContext;
   }

   public void setBPAlignment(int alignment) {
      this.bpAlignment = alignment;
   }

   public int getBPAlignment() {
      return this.bpAlignment;
   }

   public void setSpaceAdjust(double adjust) {
      this.dSpaceAdjust = adjust;
   }

   public double getSpaceAdjust() {
      return this.dSpaceAdjust;
   }

   public void setIPDAdjust(double ipdA) {
      this.ipdAdjust = ipdA;
   }

   public double getIPDAdjust() {
      return this.ipdAdjust;
   }

   public void setAlignmentContext(AlignmentContext alignmentContext) {
      this.alignmentContext = alignmentContext;
   }

   public AlignmentContext getAlignmentContext() {
      return this.alignmentContext;
   }

   public void resetAlignmentContext() {
      if (this.alignmentContext != null) {
         this.alignmentContext = this.alignmentContext.getParentAlignmentContext();
      }

   }

   public int getLineStartBorderAndPaddingWidth() {
      return this.lineStartBorderAndPaddingWidth;
   }

   public void setLineStartBorderAndPaddingWidth(int lineStartBorderAndPaddingWidth) {
      this.lineStartBorderAndPaddingWidth = lineStartBorderAndPaddingWidth;
   }

   public int getLineEndBorderAndPaddingWidth() {
      return this.lineEndBorderAndPaddingWidth;
   }

   public void setLineEndBorderAndPaddingWidth(int lineEndBorderAndPaddingWidth) {
      this.lineEndBorderAndPaddingWidth = lineEndBorderAndPaddingWidth;
   }

   public int getNextSpan() {
      return this.nextSpan;
   }

   public int getCurrentSpan() {
      return this.currentSpan == 0 ? 95 : this.currentSpan;
   }

   public void signalSpanChange(int span) {
      switch (span) {
         case 0:
         case 5:
         case 95:
            this.currentSpan = this.nextSpan;
            this.nextSpan = span;
            return;
         default:
            assert false;

            throw new IllegalArgumentException("Illegal value on signalSpanChange() for span: " + span);
      }
   }

   public WritingMode getWritingMode() {
      return this.writingMode;
   }

   public void setWritingMode(WritingMode writingMode) {
      this.writingMode = writingMode;
   }

   public int getSpaceBefore() {
      return this.spaceBefore;
   }

   public void setSpaceBefore(int spaceBefore) {
      this.spaceBefore = spaceBefore;
   }

   public int getSpaceAfter() {
      return this.spaceAfter;
   }

   public void setSpaceAfter(int spaceAfter) {
      this.spaceAfter = spaceAfter;
   }

   public int getBreakBefore() {
      return this.breakBefore;
   }

   public void setBreakBefore(int breakBefore) {
      this.breakBefore = breakBefore;
   }

   public int getBreakAfter() {
      return this.breakAfter;
   }

   public void setBreakAfter(int breakAfter) {
      this.breakAfter = breakAfter;
   }

   public String toString() {
      return "Layout Context:\nStack Limit BPD: \t" + (this.getStackLimitBP() == null ? "null" : this.getStackLimitBP().toString()) + "\nTrailing Space: \t" + (this.getTrailingSpace() == null ? "null" : this.getTrailingSpace().toString()) + "\nLeading Space: \t" + (this.getLeadingSpace() == null ? "null" : this.getLeadingSpace().toString()) + "\nReference IPD: \t" + this.getRefIPD() + "\nSpace Adjust: \t" + this.getSpaceAdjust() + "\nIPD Adjust: \t" + this.getIPDAdjust() + "\nResolve Leading Space: \t" + this.resolveLeadingSpace() + "\nSuppress Break Before: \t" + this.suppressBreakBefore() + "\nIs First Area: \t" + this.isFirstArea() + "\nStarts New Area: \t" + this.startsNewArea() + "\nIs Last Area: \t" + this.isLastArea() + "\nKeeps: \t[keep-with-next=" + this.getKeepWithNextPending() + "][keep-with-previous=" + this.getKeepWithPreviousPending() + "] pending\nBreaks: \tforced [" + (this.breakBefore != 9 ? "break-before" : "") + "][" + (this.breakAfter != 9 ? "break-after" : "") + "]";
   }

   public int getDisableColumnBalancing() {
      return this.disableColumnBalancing;
   }

   public void setDisableColumnBalancing(int disableColumnBalancing) {
      this.disableColumnBalancing = disableColumnBalancing;
   }

   public boolean treatAsArtifact() {
      return (this.flags & 32) != 0;
   }

   public void setTreatAsArtifact(boolean treatAsArtifact) {
      this.setFlags(32, treatAsArtifact);
   }
}
