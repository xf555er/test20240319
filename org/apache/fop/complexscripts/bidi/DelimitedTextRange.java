package org.apache.fop.complexscripts.bidi;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;
import org.apache.fop.util.CharUtilities;

public class DelimitedTextRange {
   private FONode fn;
   private StringBuffer buffer;
   private List intervals;
   private static final Log log = LogFactory.getLog(BidiResolver.class);

   public DelimitedTextRange(FONode fn) {
      this.fn = fn;
      this.buffer = new StringBuffer();
      this.intervals = new Vector();
   }

   public FONode getNode() {
      return this.fn;
   }

   public void append(CharIterator it, FONode fn) {
      if (it != null) {
         int s = this.buffer.length();

         int e;
         for(e = s; it.hasNext(); ++e) {
            char c = it.nextChar();
            this.buffer.append(c);
         }

         this.intervals.add(new TextInterval(fn, s, e));
      }

   }

   public void append(char c, FONode fn) {
      if (c != 0) {
         int s = this.buffer.length();
         int e = s + 1;
         this.buffer.append(c);
         this.intervals.add(new TextInterval(fn, s, e));
      }

   }

   public boolean isEmpty() {
      return this.buffer.length() == 0;
   }

   public void resolve() {
      WritingModeTraitsGetter tg;
      if ((tg = WritingModeTraits.getWritingModeTraitsGetter(this.getNode())) != null) {
         this.resolve(tg.getInlineProgressionDirection());
      }

   }

   public String toString() {
      StringBuffer sb = new StringBuffer("DR: " + this.fn.getLocalName() + " { <" + CharUtilities.toNCRefs(this.buffer.toString()) + ">");
      sb.append(", intervals <");
      boolean first = true;

      TextInterval ti;
      for(Iterator var3 = this.intervals.iterator(); var3.hasNext(); sb.append(ti.toString())) {
         Object interval = var3.next();
         ti = (TextInterval)interval;
         if (first) {
            first = false;
         } else {
            sb.append(',');
         }
      }

      sb.append("> }");
      return sb.toString();
   }

   private void resolve(Direction paragraphEmbeddingLevel) {
      int[] levels;
      if ((levels = UnicodeBidiAlgorithm.resolveLevels(this.buffer, paragraphEmbeddingLevel)) != null) {
         this.assignLevels(levels);
         this.assignBlockLevel(paragraphEmbeddingLevel);
         this.assignTextLevels();
      }

   }

   private void assignLevels(int[] levels) {
      Vector intervalsNew = new Vector(this.intervals.size());
      Iterator var3 = this.intervals.iterator();

      while(var3.hasNext()) {
         Object interval = var3.next();
         TextInterval ti = (TextInterval)interval;
         intervalsNew.addAll(this.assignLevels(ti, levels));
      }

      if (!intervalsNew.equals(this.intervals)) {
         this.intervals = intervalsNew;
      }

   }

   private List assignLevels(TextInterval ti, int[] levels) {
      Vector tiv = new Vector();
      FONode fn = ti.getNode();
      int fnStart = ti.getStart();
      int i = fnStart;

      int e;
      for(int n = ti.getEnd(); i < n; i = e) {
         e = i;

         int l;
         for(l = levels[i]; e < n && levels[e] == l; ++e) {
         }

         if (ti.getStart() == i && ti.getEnd() == e) {
            ti.setLevel(l);
         } else {
            ti = new TextInterval(fn, fnStart, i, e, l);
         }

         if (log.isDebugEnabled()) {
            log.debug("AL(" + l + "): " + ti);
         }

         tiv.add(ti);
      }

      return tiv;
   }

   private void assignTextLevels() {
      Iterator var1 = this.intervals.iterator();

      while(var1.hasNext()) {
         Object interval = var1.next();
         TextInterval ti = (TextInterval)interval;
         ti.assignTextLevels();
      }

   }

   private void assignBlockLevel(Direction paragraphEmbeddingLevel) {
      int defaultLevel = paragraphEmbeddingLevel == Direction.RL ? 1 : 0;
      Iterator var3 = this.intervals.iterator();

      while(var3.hasNext()) {
         Object interval = var3.next();
         TextInterval ti = (TextInterval)interval;
         this.assignBlockLevel(ti.getNode(), defaultLevel);
      }

   }

   private void assignBlockLevel(FONode node, int defaultLevel) {
      for(FONode fn = node; fn != null; fn = fn.getParent()) {
         if (fn instanceof FObj) {
            FObj fo = (FObj)fn;
            if (fo.isBidiRangeBlockItem()) {
               if (fo.getBidiLevel() < 0) {
                  fo.setBidiLevel(defaultLevel);
               }
               break;
            }
         }
      }

   }
}
