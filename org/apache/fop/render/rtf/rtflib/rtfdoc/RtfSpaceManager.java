package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.util.Iterator;
import java.util.LinkedList;

public class RtfSpaceManager {
   private LinkedList blockAttributes = new LinkedList();
   private LinkedList inlineAttributes = new LinkedList();
   private int accumulatedSpace;

   public void stopUpdatingSpaceBefore() {
      Iterator var1 = this.blockAttributes.iterator();

      while(var1.hasNext()) {
         Object blockAttribute = var1.next();
         RtfSpaceSplitter splitter = (RtfSpaceSplitter)blockAttribute;
         if (splitter.isBeforeCadidateSet()) {
            splitter.stopUpdatingSpaceBefore();
         }
      }

   }

   public void setCandidate(RtfAttributes attrs) {
      Iterator var2 = this.blockAttributes.iterator();

      while(var2.hasNext()) {
         Object blockAttribute = var2.next();
         RtfSpaceSplitter splitter = (RtfSpaceSplitter)blockAttribute;
         splitter.setSpaceBeforeCandidate(attrs);
         splitter.setSpaceAfterCandidate(attrs);
      }

   }

   public RtfSpaceSplitter pushRtfSpaceSplitter(RtfAttributes attrs) {
      RtfSpaceSplitter splitter = new RtfSpaceSplitter(attrs, this.accumulatedSpace);
      this.accumulatedSpace = 0;
      this.blockAttributes.addLast(splitter);
      return splitter;
   }

   public void popRtfSpaceSplitter() {
      if (!this.blockAttributes.isEmpty()) {
         RtfSpaceSplitter splitter = (RtfSpaceSplitter)this.blockAttributes.removeLast();
         this.accumulatedSpace += splitter.flush();
      }

   }

   public void pushInlineAttributes(RtfAttributes attrs) {
      this.inlineAttributes.addLast(attrs);
   }

   public void popInlineAttributes() {
      if (!this.inlineAttributes.isEmpty()) {
         this.inlineAttributes.removeLast();
      }

   }

   public RtfAttributes getLastInlineAttribute() {
      return (RtfAttributes)this.inlineAttributes.getLast();
   }
}
