package org.apache.batik.extension.svg;

import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.bridge.StrokingTextPainter;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextPainter;

public class FlowExtTextPainter extends StrokingTextPainter {
   protected static TextPainter singleton = new FlowExtTextPainter();

   public static TextPainter getInstance() {
      return singleton;
   }

   public List getTextRuns(TextNode node, AttributedCharacterIterator aci) {
      List textRuns = node.getTextRuns();
      if (textRuns != null) {
         return textRuns;
      } else {
         AttributedCharacterIterator[] chunkACIs = this.getTextChunkACIs(aci);
         textRuns = this.computeTextRuns(node, aci, chunkACIs);
         aci.first();
         List rgns = (List)aci.getAttribute(FLOW_REGIONS);
         if (rgns != null) {
            Iterator i = textRuns.iterator();
            List chunkLayouts = new ArrayList();
            StrokingTextPainter.TextRun tr = (StrokingTextPainter.TextRun)i.next();
            List layouts = new ArrayList();
            chunkLayouts.add(layouts);
            layouts.add(tr.getLayout());

            for(; i.hasNext(); layouts.add(tr.getLayout())) {
               tr = (StrokingTextPainter.TextRun)i.next();
               if (tr.isFirstRunInChunk()) {
                  layouts = new ArrayList();
                  chunkLayouts.add(layouts);
               }
            }

            FlowExtGlyphLayout.textWrapTextChunk(chunkACIs, chunkLayouts, rgns);
         }

         node.setTextRuns(textRuns);
         return textRuns;
      }
   }
}
