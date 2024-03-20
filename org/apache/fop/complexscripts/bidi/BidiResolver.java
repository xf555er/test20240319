package org.apache.fop.complexscripts.bidi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.pagination.PageSequence;

public final class BidiResolver {
   private static final Log log = LogFactory.getLog(BidiResolver.class);

   private BidiResolver() {
   }

   public static void resolveInlineDirectionality(PageSequence ps) {
      if (log.isDebugEnabled()) {
         log.debug("BD: RESOLVE: " + ps);
      }

      List ranges = ps.collectDelimitedTextRanges(new Stack());
      if (log.isDebugEnabled()) {
         dumpRanges("BD: RESOLVE: RANGES:", ranges);
      }

      List ranges = pruneEmptyRanges(ranges);
      if (log.isDebugEnabled()) {
         dumpRanges("BD: RESOLVE: PRUNED RANGES:", ranges);
      }

      resolveInlineDirectionality(ranges);
   }

   public static void reorder(LineArea la) {
      List runs = collectRuns(la.getInlineAreas(), new Vector());
      if (log.isDebugEnabled()) {
         dumpRuns("BD: REORDER: INPUT:", runs);
      }

      runs = splitRuns(runs);
      if (log.isDebugEnabled()) {
         dumpRuns("BD: REORDER: SPLIT INLINES:", runs);
      }

      int[] mm = computeMinMaxLevel(runs, (int[])null);
      if (log.isDebugEnabled()) {
         log.debug("BD: REORDER: { min = " + mm[0] + ", max = " + mm[1] + "}");
      }

      int mn = mm[0];
      int mx = mm[1];
      if (mx > 0) {
         int l1 = mx;

         for(int l2 = (mn & 1) == 0 ? mn + 1 : mn; l1 >= l2; --l1) {
            runs = reorderRuns(runs, l1);
         }
      }

      if (log.isDebugEnabled()) {
         dumpRuns("BD: REORDER: REORDERED RUNS:", runs);
      }

      boolean mirror = true;
      reverseWords(runs, mirror);
      if (log.isDebugEnabled()) {
         dumpRuns("BD: REORDER: REORDERED WORDS:", runs);
      }

      replaceInlines(la, replicateSplitWords(runs));
   }

   private static void resolveInlineDirectionality(List ranges) {
      Iterator var1 = ranges.iterator();

      while(var1.hasNext()) {
         Object range = var1.next();
         DelimitedTextRange r = (DelimitedTextRange)range;
         r.resolve();
         if (log.isDebugEnabled()) {
            log.debug(r);
         }
      }

   }

   private static List collectRuns(List inlines, List runs) {
      InlineArea ia;
      for(Iterator var2 = inlines.iterator(); var2.hasNext(); runs = ia.collectInlineRuns(runs)) {
         Object inline = var2.next();
         ia = (InlineArea)inline;
      }

      return runs;
   }

   private static List splitRuns(List runs) {
      List runsNew = new Vector();
      Iterator var2 = ((List)runs).iterator();

      while(var2.hasNext()) {
         Object run = var2.next();
         InlineRun ir = (InlineRun)run;
         if (ir.isHomogenous()) {
            runsNew.add(ir);
         } else {
            runsNew.addAll(ir.split());
         }
      }

      if (!runsNew.equals(runs)) {
         runs = runsNew;
      }

      return (List)runs;
   }

   private static int[] computeMinMaxLevel(List runs, int[] mm) {
      if (mm == null) {
         mm = new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE};
      }

      Iterator var2 = runs.iterator();

      while(var2.hasNext()) {
         Object run = var2.next();
         InlineRun ir = (InlineRun)run;
         ir.updateMinMax(mm);
      }

      return mm;
   }

   private static List reorderRuns(List runs, int level) {
      assert level >= 0;

      List runsNew = new Vector();
      int i = 0;

      for(int n = ((List)runs).size(); i < n; ++i) {
         InlineRun iri = (InlineRun)((List)runs).get(i);
         if (iri.getMinLevel() < level) {
            runsNew.add(iri);
         } else {
            int e;
            for(e = i; e < n; ++e) {
               InlineRun ire = (InlineRun)((List)runs).get(e);
               if (ire.getMinLevel() < level) {
                  break;
               }
            }

            if (i < e) {
               runsNew.addAll(reverseRuns((List)runs, i, e));
            }

            i = e - 1;
         }
      }

      if (!runsNew.equals(runs)) {
         runs = runsNew;
      }

      return (List)runs;
   }

   private static List reverseRuns(List runs, int s, int e) {
      int n = e - s;
      Vector runsNew = new Vector(n);
      if (n > 0) {
         for(int i = 0; i < n; ++i) {
            int k = n - i - 1;
            InlineRun ir = (InlineRun)runs.get(s + k);
            ir.reverse();
            runsNew.add(ir);
         }
      }

      return runsNew;
   }

   private static void reverseWords(List runs, boolean mirror) {
      Iterator var2 = runs.iterator();

      while(var2.hasNext()) {
         Object run = var2.next();
         InlineRun ir = (InlineRun)run;
         ir.maybeReverseWord(mirror);
      }

   }

   private static List replicateSplitWords(List runs) {
      return runs;
   }

   private static void replaceInlines(LineArea la, List runs) {
      List inlines = new ArrayList();
      Iterator var3 = runs.iterator();

      while(var3.hasNext()) {
         Object run = var3.next();
         InlineRun ir = (InlineRun)run;
         inlines.add(ir.getInline());
      }

      la.setInlineAreas(unflattenInlines(inlines));
   }

   private static List unflattenInlines(List inlines) {
      return (new UnflattenProcessor(inlines)).unflatten();
   }

   private static void dumpRuns(String header, List runs) {
      log.debug(header);
      Iterator var2 = runs.iterator();

      while(var2.hasNext()) {
         Object run = var2.next();
         InlineRun ir = (InlineRun)run;
         log.debug(ir);
      }

   }

   private static void dumpRanges(String header, List ranges) {
      log.debug(header);
      Iterator var2 = ranges.iterator();

      while(var2.hasNext()) {
         Object range = var2.next();
         DelimitedTextRange r = (DelimitedTextRange)range;
         log.debug(r);
      }

   }

   private static List pruneEmptyRanges(List ranges) {
      Vector rv = new Vector();
      Iterator var2 = ranges.iterator();

      while(var2.hasNext()) {
         Object range = var2.next();
         DelimitedTextRange r = (DelimitedTextRange)range;
         if (!r.isEmpty()) {
            rv.add(r);
         }
      }

      return rv;
   }
}
