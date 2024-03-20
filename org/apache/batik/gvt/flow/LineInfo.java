package org.apache.batik.gvt.flow;

import java.awt.geom.Point2D;
import org.apache.batik.gvt.font.GVTGlyphVector;

public class LineInfo {
   FlowRegions fr;
   double lineHeight = -1.0;
   double ascent = -1.0;
   double descent = -1.0;
   double hLeading = -1.0;
   double baseline;
   int numGlyphs;
   int words = 0;
   int size = 0;
   GlyphGroupInfo[] ggis = null;
   int newSize = 0;
   GlyphGroupInfo[] newGGIS = null;
   int numRanges;
   double[] ranges;
   double[] rangeAdv;
   BlockInfo bi = null;
   boolean paraStart;
   boolean paraEnd;
   protected static final int FULL_WORD = 0;
   protected static final int FULL_ADV = 1;
   static final float MAX_COMPRESS = 0.1F;
   static final float COMRESS_SCALE = 3.0F;

   public LineInfo(FlowRegions fr, BlockInfo bi, boolean paraStart) {
      this.fr = fr;
      this.bi = bi;
      this.lineHeight = (double)bi.getLineHeight();
      this.ascent = (double)bi.getAscent();
      this.descent = (double)bi.getDescent();
      this.hLeading = (this.lineHeight - (this.ascent + this.descent)) / 2.0;
      this.baseline = (double)((float)(fr.getCurrentY() + this.hLeading + this.ascent));
      this.paraStart = paraStart;
      this.paraEnd = false;
      if (this.lineHeight > 0.0) {
         fr.newLineHeight(this.lineHeight);
         this.updateRangeInfo();
      }

   }

   public void setParaEnd(boolean paraEnd) {
      this.paraEnd = paraEnd;
   }

   public boolean addWord(WordInfo wi) {
      double nlh = (double)wi.getLineHeight();
      if (nlh <= this.lineHeight) {
         return this.insertWord(wi);
      } else {
         this.fr.newLineHeight(nlh);
         if (!this.updateRangeInfo()) {
            if (this.lineHeight > 0.0) {
               this.fr.newLineHeight(this.lineHeight);
            }

            return false;
         } else if (!this.insertWord(wi)) {
            if (this.lineHeight > 0.0) {
               this.setLineHeight(this.lineHeight);
            }

            return false;
         } else {
            this.lineHeight = nlh;
            if ((double)wi.getAscent() > this.ascent) {
               this.ascent = (double)wi.getAscent();
            }

            if ((double)wi.getDescent() > this.descent) {
               this.descent = (double)wi.getDescent();
            }

            this.hLeading = (nlh - (this.ascent + this.descent)) / 2.0;
            this.baseline = (double)((float)(this.fr.getCurrentY() + this.hLeading + this.ascent));
            return true;
         }
      }
   }

   public boolean insertWord(WordInfo wi) {
      this.mergeGlyphGroups(wi);
      if (!this.assignGlyphGroupRanges(this.newSize, this.newGGIS)) {
         return false;
      } else {
         this.swapGlyphGroupInfo();
         return true;
      }
   }

   public boolean assignGlyphGroupRanges(int ggSz, GlyphGroupInfo[] ggis) {
      int i = 0;
      int r = 0;

      do {
         if (r >= this.numRanges) {
            return false;
         }

         double range = this.ranges[2 * r + 1] - this.ranges[2 * r];
         float adv = 0.0F;

         float rangeAdvance;
         GlyphGroupInfo ggi;
         for(rangeAdvance = 0.0F; i < ggSz; rangeAdvance += adv) {
            ggi = ggis[i];
            ggi.setRange(r);
            adv = ggi.getAdvance();
            double delta = range - (double)(rangeAdvance + adv);
            if (delta < 0.0) {
               break;
            }

            ++i;
         }

         if (i == ggSz) {
            --i;
            rangeAdvance -= adv;
         }

         ggi = ggis[i];

         float ladv;
         for(ladv = ggi.getLastAdvance(); (double)(rangeAdvance + ladv) > range; ladv = ggi.getLastAdvance()) {
            --i;
            ladv = 0.0F;
            if (i < 0) {
               break;
            }

            ggi = ggis[i];
            if (r != ggi.getRange()) {
               break;
            }

            rangeAdvance -= ggi.getAdvance();
         }

         ++i;
         this.rangeAdv[r] = (double)(rangeAdvance + ladv);
         ++r;
      } while(i != ggSz);

      return true;
   }

   public boolean setLineHeight(double lh) {
      this.fr.newLineHeight(lh);
      if (this.updateRangeInfo()) {
         this.lineHeight = lh;
         return true;
      } else {
         if (this.lineHeight > 0.0) {
            this.fr.newLineHeight(this.lineHeight);
         }

         return false;
      }
   }

   public double getCurrentY() {
      return this.fr.getCurrentY();
   }

   public boolean gotoY(double y) {
      if (this.fr.gotoY(y)) {
         return true;
      } else {
         if (this.lineHeight > 0.0) {
            this.updateRangeInfo();
         }

         this.baseline = (double)((float)(this.fr.getCurrentY() + this.hLeading + this.ascent));
         return false;
      }
   }

   protected boolean updateRangeInfo() {
      this.fr.resetRange();
      int nr = this.fr.getNumRangeOnLine();
      if (nr == 0) {
         return false;
      } else {
         this.numRanges = nr;
         int r;
         if (this.ranges == null) {
            this.rangeAdv = new double[this.numRanges];
            this.ranges = new double[2 * this.numRanges];
         } else if (this.numRanges > this.rangeAdv.length) {
            r = 2 * this.rangeAdv.length;
            if (r < this.numRanges) {
               r = this.numRanges;
            }

            this.rangeAdv = new double[r];
            this.ranges = new double[2 * r];
         }

         for(r = 0; r < this.numRanges; ++r) {
            double[] rangeBounds = this.fr.nextRange();
            double r0 = rangeBounds[0];
            double delta;
            if (r == 0) {
               delta = (double)this.bi.getLeftMargin();
               if (this.paraStart) {
                  double indent = (double)this.bi.getIndent();
                  if (delta < -indent) {
                     delta = 0.0;
                  } else {
                     delta += indent;
                  }
               }

               r0 += delta;
            }

            delta = rangeBounds[1];
            if (r == this.numRanges - 1) {
               delta -= (double)this.bi.getRightMargin();
            }

            this.ranges[2 * r] = r0;
            this.ranges[2 * r + 1] = delta;
         }

         return true;
      }
   }

   protected void swapGlyphGroupInfo() {
      GlyphGroupInfo[] tmp = this.ggis;
      this.ggis = this.newGGIS;
      this.newGGIS = tmp;
      this.size = this.newSize;
      this.newSize = 0;
   }

   protected void mergeGlyphGroups(WordInfo wi) {
      int numGG = wi.getNumGlyphGroups();
      this.newSize = 0;
      int s;
      if (this.ggis == null) {
         this.newSize = numGG;
         this.newGGIS = new GlyphGroupInfo[numGG];

         for(s = 0; s < numGG; ++s) {
            this.newGGIS[s] = wi.getGlyphGroup(s);
         }
      } else {
         s = 0;
         int i = 0;
         GlyphGroupInfo nggi = wi.getGlyphGroup(i);
         int nStart = nggi.getStart();
         GlyphGroupInfo oggi = this.ggis[this.size - 1];
         int oStart = oggi.getStart();
         this.newGGIS = assureSize(this.newGGIS, this.size + numGG);
         if (nStart < oStart) {
            oggi = this.ggis[s];
            oStart = oggi.getStart();

            while(s < this.size && i < numGG) {
               if (nStart < oStart) {
                  this.newGGIS[this.newSize++] = nggi;
                  ++i;
                  if (i < numGG) {
                     nggi = wi.getGlyphGroup(i);
                     nStart = nggi.getStart();
                  }
               } else {
                  this.newGGIS[this.newSize++] = oggi;
                  ++s;
                  if (s < this.size) {
                     oggi = this.ggis[s];
                     oStart = oggi.getStart();
                  }
               }
            }
         }

         while(s < this.size) {
            this.newGGIS[this.newSize++] = this.ggis[s++];
         }

         while(i < numGG) {
            this.newGGIS[this.newSize++] = wi.getGlyphGroup(i++);
         }
      }

   }

   public void layout() {
      if (this.size != 0) {
         this.assignGlyphGroupRanges(this.size, this.ggis);
         GVTGlyphVector gv = this.ggis[0].getGlyphVector();
         int justType = false;
         double ggAdv = 0.0;
         double gAdv = 0.0;
         int[] rangeGG = new int[this.numRanges];
         int[] rangeG = new int[this.numRanges];
         GlyphGroupInfo[] rangeLastGGI = new GlyphGroupInfo[this.numRanges];
         GlyphGroupInfo ggi = this.ggis[0];
         int r = ggi.getRange();
         int var10002 = rangeGG[r]++;
         rangeG[r] += ggi.getGlyphCount();

         int currRange;
         for(currRange = 1; currRange < this.size; ++currRange) {
            ggi = this.ggis[currRange];
            r = ggi.getRange();
            if (rangeLastGGI[r] == null || !rangeLastGGI[r].getHideLast()) {
               var10002 = rangeGG[r]++;
            }

            rangeLastGGI[r] = ggi;
            rangeG[r] += ggi.getGlyphCount();
            GlyphGroupInfo pggi = this.ggis[currRange - 1];
            int pr = pggi.getRange();
            if (r != pr) {
               rangeG[pr] += pggi.getLastGlyphCount() - pggi.getGlyphCount();
            }
         }

         rangeG[r] += ggi.getLastGlyphCount() - ggi.getGlyphCount();
         currRange = -1;
         double locX = 0.0;
         double range = 0.0;
         double rAdv = 0.0;
         int r = true;
         ggi = null;

         for(int i = 0; i < this.size; ++i) {
            GlyphGroupInfo pggi = ggi;
            int prevRange = currRange;
            ggi = this.ggis[i];
            currRange = ggi.getRange();
            int start;
            if (currRange != prevRange) {
               locX = this.ranges[2 * currRange];
               range = this.ranges[2 * currRange + 1] - locX;
               rAdv = this.rangeAdv[currRange];
               start = this.bi.getTextAlignment();
               if (this.paraEnd && start == 3) {
                  start = 0;
               }

               switch (start) {
                  case 0:
                     break;
                  case 1:
                     locX += (range - rAdv) / 2.0;
                     break;
                  case 2:
                     locX += range - rAdv;
                     break;
                  case 3:
                  default:
                     double delta = range - rAdv;
                     int numSp;
                     if (!justType) {
                        numSp = rangeGG[currRange] - 1;
                        if (numSp >= 1) {
                           ggAdv = delta / (double)numSp;
                        }
                     } else {
                        numSp = rangeG[currRange] - 1;
                        if (numSp >= 1) {
                           gAdv = delta / (double)numSp;
                        }
                     }
               }
            } else if (pggi != null && pggi.getHideLast()) {
               gv.setGlyphVisible(pggi.getEnd(), false);
            }

            start = ggi.getStart();
            int end = ggi.getEnd();
            boolean[] hide = ggi.getHide();
            Point2D p2d = gv.getGlyphPosition(start);
            double deltaX = p2d.getX();
            double advAdj = 0.0;

            for(int g = start; g <= end; ++g) {
               Point2D np2d = gv.getGlyphPosition(g + 1);
               if (hide[g - start]) {
                  gv.setGlyphVisible(g, false);
                  advAdj += np2d.getX() - p2d.getX();
               } else {
                  gv.setGlyphVisible(g, true);
               }

               p2d.setLocation(p2d.getX() - deltaX - advAdj + locX, p2d.getY() + this.baseline);
               gv.setGlyphPosition(g, p2d);
               p2d = np2d;
               advAdj -= gAdv;
            }

            if (ggi.getHideLast()) {
               locX += (double)ggi.getAdvance() - advAdj;
            } else {
               locX += (double)ggi.getAdvance() - advAdj + ggAdv;
            }
         }

      }
   }

   public static GlyphGroupInfo[] assureSize(GlyphGroupInfo[] ggis, int sz) {
      if (ggis == null) {
         if (sz < 10) {
            sz = 10;
         }

         return new GlyphGroupInfo[sz];
      } else if (sz <= ggis.length) {
         return ggis;
      } else {
         int nsz = ggis.length * 2;
         if (nsz < sz) {
            nsz = sz;
         }

         return new GlyphGroupInfo[nsz];
      }
   }
}
