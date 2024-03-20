package org.apache.fop.afp.fonts;

import java.util.Iterator;
import java.util.List;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;

public class AFPFontCollection implements FontCollection {
   private final AFPEventProducer eventProducer;
   private final List fontInfoList;

   public AFPFontCollection(EventBroadcaster eventBroadcaster, List fontInfoList) {
      this.eventProducer = AFPEventProducer.Provider.get(eventBroadcaster);
      this.fontInfoList = fontInfoList;
   }

   public int setup(int start, FontInfo fontInfo) {
      int num = 1;
      if (this.fontInfoList != null && this.fontInfoList.size() > 0) {
         Iterator var10 = this.fontInfoList.iterator();

         while(var10.hasNext()) {
            AFPFontInfo afpFontInfo = (AFPFontInfo)var10.next();
            AFPFont afpFont = afpFontInfo.getAFPFont();
            List tripletList = afpFontInfo.getFontTriplets();

            for(Iterator var8 = tripletList.iterator(); var8.hasNext(); ++num) {
               FontTriplet triplet = (FontTriplet)var8.next();
               fontInfo.addMetrics("F" + num, afpFont);
               fontInfo.addFontProperties("F" + num, triplet.getName(), triplet.getStyle(), triplet.getWeight());
            }
         }

         this.checkDefaultFontAvailable(fontInfo, "normal", 400);
         this.checkDefaultFontAvailable(fontInfo, "italic", 400);
         this.checkDefaultFontAvailable(fontInfo, "normal", 700);
         this.checkDefaultFontAvailable(fontInfo, "italic", 700);
      } else {
         this.eventProducer.warnDefaultFontSetup(this);
         FontCollection base12FontCollection = new AFPBase12FontCollection(this.eventProducer);
         num = base12FontCollection.setup(num, fontInfo);
      }

      return num;
   }

   private void checkDefaultFontAvailable(FontInfo fontInfo, String style, int weight) {
      if (!fontInfo.hasFont("any", style, weight)) {
         this.eventProducer.warnMissingDefaultFont(this, style, weight);
      }

   }
}
