package org.apache.batik.swing.svg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.swing.gvt.Overlay;

public class SVGUpdateOverlay implements Overlay {
   List rects = new LinkedList();
   int size;
   int updateCount;
   int[] counts;

   public SVGUpdateOverlay(int size, int numUpdates) {
      this.size = size;
      this.counts = new int[numUpdates];
   }

   public void addRect(Rectangle r) {
      this.rects.add(r);
      if (this.rects.size() > this.size) {
         this.rects.remove(0);
      }

      ++this.updateCount;
   }

   public void endUpdate() {
      int i;
      for(i = 0; i < this.counts.length - 1; ++i) {
         this.counts[i] = this.counts[i + 1];
      }

      this.counts[i] = this.updateCount;
      this.updateCount = 0;
      int num = this.rects.size();

      for(i = this.counts.length - 1; i >= 0; --i) {
         if (this.counts[i] > num) {
            this.counts[i] = num;
         }

         num -= this.counts[i];
      }

      int[] var10000 = this.counts;
      var10000[0] += num;
   }

   public void paint(Graphics g) {
      Iterator i = this.rects.iterator();
      int count = 0;
      int idx = 0;

      int group;
      for(group = 0; group < this.counts.length - 1 && idx == this.counts[group]; ++group) {
      }

      int cmax = this.counts.length - 1;

      while(i.hasNext()) {
         Rectangle r = (Rectangle)i.next();
         Color c = new Color(1.0F, (float)(cmax - group) / (float)cmax, 0.0F, ((float)count + 1.0F) / (float)this.rects.size());
         g.setColor(c);
         g.drawRect(r.x, r.y, r.width, r.height);
         ++count;
         ++idx;

         while(group < this.counts.length - 1 && idx == this.counts[group]) {
            ++group;
            idx = 0;
         }
      }

   }
}
