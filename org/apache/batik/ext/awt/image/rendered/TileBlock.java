package org.apache.batik.ext.awt.image.rendered;

import java.util.ArrayList;
import java.util.List;

public class TileBlock {
   int occX;
   int occY;
   int occW;
   int occH;
   int xOff;
   int yOff;
   int w;
   int h;
   int benefit;
   boolean[] occupied;

   TileBlock(int occX, int occY, int occW, int occH, boolean[] occupied, int xOff, int yOff, int w, int h) {
      this.occX = occX;
      this.occY = occY;
      this.occW = occW;
      this.occH = occH;
      this.xOff = xOff;
      this.yOff = yOff;
      this.w = w;
      this.h = h;
      this.occupied = occupied;

      for(int y = 0; y < h; ++y) {
         for(int x = 0; x < w; ++x) {
            if (!occupied[x + xOff + occW * (y + yOff)]) {
               ++this.benefit;
            }
         }
      }

   }

   public String toString() {
      String ret = "";

      for(int y = 0; y < this.occH; ++y) {
         for(int x = 0; x < this.occW + 1; ++x) {
            if (x != this.xOff && x != this.xOff + this.w) {
               if (y == this.yOff && x > this.xOff && x < this.xOff + this.w) {
                  ret = ret + "-";
               } else if (y == this.yOff + this.h - 1 && x > this.xOff && x < this.xOff + this.w) {
                  ret = ret + "_";
               } else {
                  ret = ret + " ";
               }
            } else if (y != this.yOff && y != this.yOff + this.h - 1) {
               if (y > this.yOff && y < this.yOff + this.h - 1) {
                  ret = ret + "|";
               } else {
                  ret = ret + " ";
               }
            } else {
               ret = ret + "+";
            }

            if (x != this.occW) {
               if (this.occupied[x + y * this.occW]) {
                  ret = ret + "*";
               } else {
                  ret = ret + ".";
               }
            }
         }

         ret = ret + "\n";
      }

      return ret;
   }

   int getXLoc() {
      return this.occX + this.xOff;
   }

   int getYLoc() {
      return this.occY + this.yOff;
   }

   int getWidth() {
      return this.w;
   }

   int getHeight() {
      return this.h;
   }

   int getBenefit() {
      return this.benefit;
   }

   int getWork() {
      return this.w * this.h + 1;
   }

   static int getWork(TileBlock[] blocks) {
      int ret = 0;
      TileBlock[] var2 = blocks;
      int var3 = blocks.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         TileBlock block = var2[var4];
         ret += block.getWork();
      }

      return ret;
   }

   TileBlock[] getBestSplit() {
      if (this.simplify()) {
         return null;
      } else {
         return this.benefit == this.w * this.h ? new TileBlock[]{this} : this.splitOneGo();
      }
   }

   public TileBlock[] splitOneGo() {
      boolean[] filled = (boolean[])this.occupied.clone();
      List items = new ArrayList();

      for(int y = this.yOff; y < this.yOff + this.h; ++y) {
         for(int x = this.xOff; x < this.xOff + this.w; ++x) {
            if (!filled[x + y * this.occW]) {
               int cw = this.xOff + this.w - x;

               int ch;
               for(ch = x; ch < x + cw; ++ch) {
                  if (filled[ch + y * this.occW]) {
                     cw = ch - x;
                  } else {
                     filled[ch + y * this.occW] = true;
                  }
               }

               ch = 1;

               for(int cy = y + 1; cy < this.yOff + this.h; ++cy) {
                  int cx;
                  for(cx = x; cx < x + cw && !filled[cx + cy * this.occW]; ++cx) {
                  }

                  if (cx != x + cw) {
                     break;
                  }

                  for(cx = x; cx < x + cw; ++cx) {
                     filled[cx + cy * this.occW] = true;
                  }

                  ++ch;
               }

               items.add(new TileBlock(this.occX, this.occY, this.occW, this.occH, this.occupied, x, y, cw, ch));
               x += cw - 1;
            }
         }
      }

      TileBlock[] ret = new TileBlock[items.size()];
      items.toArray(ret);
      return ret;
   }

   public boolean simplify() {
      boolean[] workOccupied = this.occupied;

      int x;
      int y;
      for(x = 0; x < this.h; ++x) {
         for(y = 0; y < this.w && workOccupied[y + this.xOff + this.occW * (x + this.yOff)]; ++y) {
         }

         if (y != this.w) {
            break;
         }

         ++this.yOff;
         --x;
         --this.h;
      }

      if (this.h == 0) {
         return true;
      } else {
         for(x = this.h - 1; x >= 0; --x) {
            for(y = 0; y < this.w && workOccupied[y + this.xOff + this.occW * (x + this.yOff)]; ++y) {
            }

            if (y != this.w) {
               break;
            }

            --this.h;
         }

         for(x = 0; x < this.w; ++x) {
            for(y = 0; y < this.h && workOccupied[x + this.xOff + this.occW * (y + this.yOff)]; ++y) {
            }

            if (y != this.h) {
               break;
            }

            ++this.xOff;
            --x;
            --this.w;
         }

         for(x = this.w - 1; x >= 0; --x) {
            for(y = 0; y < this.h && workOccupied[x + this.xOff + this.occW * (y + this.yOff)]; ++y) {
            }

            if (y != this.h) {
               break;
            }

            --this.w;
         }

         return false;
      }
   }
}
