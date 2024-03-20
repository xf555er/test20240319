package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.apache.fop.layoutmgr.inline.KnuthInlineBox;

public final class FootenoteUtil {
   private FootenoteUtil() {
   }

   public static List getFootnotes(List elemenList) {
      return getFootnotes(elemenList, 0, elemenList.size() - 1);
   }

   public static List getFootnotes(List elemenList, int startIndex, int endIndex) {
      ListIterator iter = elemenList.listIterator(startIndex);
      List footnotes = null;

      while(true) {
         while(iter.nextIndex() <= endIndex) {
            ListElement element = (ListElement)iter.next();
            if (element instanceof KnuthInlineBox && ((KnuthInlineBox)element).isAnchor()) {
               footnotes = getFootnoteList(footnotes);
               footnotes.add(((KnuthInlineBox)element).getFootnoteBodyLM());
            } else if (element instanceof KnuthBlockBox && ((KnuthBlockBox)element).hasAnchors()) {
               footnotes = getFootnoteList(footnotes);
               footnotes.addAll(((KnuthBlockBox)element).getFootnoteBodyLMs());
            }
         }

         if (footnotes == null) {
            return Collections.emptyList();
         }

         return footnotes;
      }
   }

   private static List getFootnoteList(List footnotes) {
      return (List)(footnotes == null ? new ArrayList() : footnotes);
   }
}
