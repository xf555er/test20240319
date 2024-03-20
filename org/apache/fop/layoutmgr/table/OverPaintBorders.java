package org.apache.fop.layoutmgr.table;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.BorderProps;
import org.apache.xmlgraphics.java2d.color.ColorUtil;

public class OverPaintBorders {
   protected OverPaintBorders(Block curBlockArea) {
      List newBlocks = new ArrayList();
      List childAreas = new ArrayList(curBlockArea.getChildAreas());
      Collections.sort(childAreas, new SortBlocksByXOffset());
      this.mergeBordersOfType(newBlocks, childAreas, new int[]{Trait.BORDER_BEFORE, Trait.BORDER_AFTER});
      Collections.sort(childAreas, new SortBlocksByYOffset());
      this.mergeBordersOfType(newBlocks, childAreas, new int[]{Trait.BORDER_START, Trait.BORDER_END});
      Iterator var4 = newBlocks.iterator();

      while(var4.hasNext()) {
         Block borderBlock = (Block)var4.next();
         curBlockArea.addBlock(borderBlock);
      }

   }

   private void mergeBordersOfType(List newBlocks, List childAreas, int[] borderTraits) {
      Map mergeMap = new HashMap();
      int[] var5 = borderTraits;
      int var6 = borderTraits.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         int traitType = var5[var7];
         mergeMap.put(traitType, (Object)null);
      }

      Iterator var30 = childAreas.iterator();

      while(var30.hasNext()) {
         Object child = var30.next();
         Block childBlock = (Block)child;
         BorderProps startBps = (BorderProps)childBlock.getTrait(Trait.BORDER_START);
         BorderProps endBps = (BorderProps)childBlock.getTrait(Trait.BORDER_END);
         BorderProps beforeBps = (BorderProps)childBlock.getTrait(Trait.BORDER_BEFORE);
         BorderProps afterBps = (BorderProps)childBlock.getTrait(Trait.BORDER_AFTER);
         int[] var12 = borderTraits;
         int var13 = borderTraits.length;

         for(int var14 = 0; var14 < var13; ++var14) {
            int traitType = var12[var14];
            BorderProps borderProps = (BorderProps)childBlock.getTrait(traitType);
            if (borderProps != null) {
               Map currTraitMap = (Map)mergeMap.get(traitType);
               Point endPoint = this.getEndMiddlePoint(childBlock, traitType, startBps, endBps, beforeBps, afterBps);
               BorderProps bpsCurr = (BorderProps)childBlock.getTrait(traitType);
               Block prevBlock = null;
               if (currTraitMap == null) {
                  currTraitMap = new HashMap();
                  mergeMap.put(traitType, currTraitMap);
               } else {
                  label179: {
                     Point startPoint = this.getStartMiddlePoint(childBlock, traitType, startBps, endBps, beforeBps, afterBps);
                     Iterator var23 = ((Map)currTraitMap).entrySet().iterator();

                     Map.Entry entry;
                     Point prevEndPoint;
                     boolean isVertical;
                     boolean isHorizontal;
                     do {
                        if (!var23.hasNext()) {
                           break label179;
                        }

                        entry = (Map.Entry)var23.next();
                        prevEndPoint = (Point)entry.getKey();
                        isVertical = traitType == Trait.BORDER_START || traitType == Trait.BORDER_END;
                        isHorizontal = traitType == Trait.BORDER_BEFORE || traitType == Trait.BORDER_AFTER;
                     } while((!isHorizontal || prevEndPoint.y != startPoint.y || prevEndPoint.x < startPoint.x) && (!isVertical || prevEndPoint.x != startPoint.x || prevEndPoint.y < startPoint.y));

                     Block prevBlockCurr = (Block)entry.getValue();
                     ((Map)currTraitMap).remove(prevEndPoint);
                     BorderProps bpsPrev = (BorderProps)prevBlockCurr.getTrait(traitType);
                     if (this.canMergeBorders(bpsPrev, bpsCurr)) {
                        prevBlock = prevBlockCurr;
                     }
                  }
               }

               Block borderBlock;
               if (prevBlock != null && newBlocks.contains(prevBlock)) {
                  borderBlock = prevBlock;
               } else {
                  borderBlock = new Block();
                  borderBlock.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
                  borderBlock.setPositioning(2);
                  borderBlock.setBidiLevel(childBlock.getBidiLevel());
                  newBlocks.add(borderBlock);
                  BorderProps prevBeforeBps = (BorderProps)childBlock.getTrait(Trait.BORDER_BEFORE);
                  int prevBefore = prevBeforeBps != null ? prevBeforeBps.width : 0;
                  Integer prevPaddingStart = (Integer)childBlock.getTrait(Trait.PADDING_START);
                  Integer prevPaddingEnd = (Integer)childBlock.getTrait(Trait.PADDING_END);
                  Integer prevPaddingBefore = (Integer)childBlock.getTrait(Trait.PADDING_BEFORE);
                  Integer prevPaddingAfter = (Integer)childBlock.getTrait(Trait.PADDING_AFTER);
                  if (traitType == Trait.BORDER_START) {
                     borderBlock.setYOffset(childBlock.getYOffset() + prevBefore);
                     borderBlock.setXOffset(childBlock.getXOffset() - (prevPaddingStart != null ? prevPaddingStart : 0));
                  } else if (traitType == Trait.BORDER_END) {
                     borderBlock.setYOffset(childBlock.getYOffset() + prevBefore);
                     borderBlock.setXOffset(childBlock.getXOffset() - (prevPaddingStart != null ? prevPaddingStart : 0));
                     borderBlock.setIPD(childBlock.getIPD() + (prevPaddingStart != null ? prevPaddingStart : 0) + (prevPaddingEnd != null ? prevPaddingEnd : 0));
                  } else if (traitType == Trait.BORDER_BEFORE) {
                     borderBlock.setYOffset(childBlock.getYOffset());
                     borderBlock.setXOffset(childBlock.getXOffset() - (prevPaddingStart != null ? prevPaddingStart : 0));
                  } else if (traitType == Trait.BORDER_AFTER) {
                     borderBlock.setYOffset(childBlock.getYOffset() + prevBefore);
                     borderBlock.setXOffset(childBlock.getXOffset() - (prevPaddingStart != null ? prevPaddingStart : 0));
                     borderBlock.setBPD(childBlock.getBPD() + (prevPaddingBefore != null ? prevPaddingBefore : 0) + (prevPaddingAfter != null ? prevPaddingAfter : 0));
                  }
               }

               Integer paddingEnd = (Integer)childBlock.getTrait(Trait.PADDING_END);
               Integer paddingAfter = (Integer)childBlock.getTrait(Trait.PADDING_AFTER);
               int newEndPoint;
               if (traitType != Trait.BORDER_BEFORE && traitType != Trait.BORDER_AFTER) {
                  if (traitType == Trait.BORDER_START || traitType == Trait.BORDER_END) {
                     newEndPoint = childBlock.getYOffset() + childBlock.getBPD() + childBlock.getBorderAndPaddingWidthBefore() + (paddingAfter != null ? paddingAfter : 0);
                     borderBlock.setBPD(newEndPoint - borderBlock.getYOffset());
                  }
               } else {
                  newEndPoint = childBlock.getXOffset() + childBlock.getIPD() + (paddingEnd != null ? paddingEnd : 0);
                  borderBlock.setIPD(newEndPoint - borderBlock.getXOffset());
               }

               BorderProps newBps = new BorderProps(bpsCurr.style, bpsCurr.width, 0, 0, bpsCurr.color, bpsCurr.getMode());
               borderBlock.addTrait(traitType, newBps);
               ((Map)currTraitMap).put(endPoint, borderBlock);
            }
         }
      }

   }

   private boolean canMergeBorders(BorderProps bpsPrev, BorderProps bpsCurr) {
      return bpsPrev.style == bpsCurr.style && ColorUtil.isSameColor(bpsPrev.color, bpsCurr.color) && bpsPrev.width == bpsCurr.width && bpsPrev.getMode() == bpsPrev.getMode() && bpsPrev.getRadiusEnd() == 0 && bpsCurr.getRadiusStart() == 0;
   }

   private Point getEndMiddlePoint(Block block, int borderTrait, BorderProps startBps, BorderProps endBps, BorderProps beforeBps, BorderProps afterBps) {
      int x;
      int y;
      Integer paddingEnd;
      if (borderTrait == Trait.BORDER_START) {
         paddingEnd = (Integer)block.getTrait(Trait.PADDING_START);
         x = block.getXOffset() - (paddingEnd != null ? paddingEnd : 0) - BorderProps.getClippedWidth(startBps);
         y = block.getYOffset() + block.getBPD() + block.getBorderAndPaddingWidthBefore() + block.getBorderAndPaddingWidthAfter();
      } else if (borderTrait == Trait.BORDER_END) {
         paddingEnd = (Integer)block.getTrait(Trait.PADDING_END);
         x = block.getXOffset() + block.getIPD() + (paddingEnd != null ? paddingEnd : 0) + BorderProps.getClippedWidth(endBps);
         y = block.getYOffset() + block.getBPD() + block.getBorderAndPaddingWidthBefore() + block.getBorderAndPaddingWidthAfter();
      } else if (borderTrait == Trait.BORDER_AFTER) {
         paddingEnd = (Integer)block.getTrait(Trait.PADDING_END);
         x = block.getXOffset() + block.getIPD() + (paddingEnd != null ? paddingEnd : 0) + BorderProps.getClippedWidth(endBps);
         Integer paddingAfter = (Integer)block.getTrait(Trait.PADDING_AFTER);
         y = block.getYOffset() + block.getBPD() + block.getBorderAndPaddingWidthBefore() + (paddingAfter != null ? paddingAfter : 0) + BorderProps.getClippedWidth(afterBps);
      } else {
         if (borderTrait != Trait.BORDER_BEFORE) {
            throw new IllegalArgumentException("Invalid trait: " + borderTrait);
         }

         paddingEnd = (Integer)block.getTrait(Trait.PADDING_END);
         x = block.getXOffset() + block.getIPD() + (paddingEnd != null ? paddingEnd : 0) + BorderProps.getClippedWidth(endBps);
         y = block.getYOffset() + BorderProps.getClippedWidth(beforeBps);
      }

      return new Point(x, y);
   }

   private Point getStartMiddlePoint(Block block, int borderTrait, BorderProps startBps, BorderProps endBps, BorderProps beforeBps, BorderProps afterBps) {
      int x;
      int y;
      Integer paddingAfter;
      if (borderTrait == Trait.BORDER_START) {
         paddingAfter = (Integer)block.getTrait(Trait.PADDING_START);
         x = block.getXOffset() - (paddingAfter != null ? paddingAfter : 0) - BorderProps.getClippedWidth(startBps);
         y = block.getYOffset();
      } else if (borderTrait == Trait.BORDER_BEFORE) {
         x = block.getXOffset() - block.getBorderAndPaddingWidthStart();
         y = block.getYOffset() + BorderProps.getClippedWidth(beforeBps);
      } else if (borderTrait == Trait.BORDER_END) {
         paddingAfter = (Integer)block.getTrait(Trait.PADDING_END);
         x = block.getXOffset() + block.getIPD() + (paddingAfter != null ? paddingAfter : 0) + BorderProps.getClippedWidth(endBps);
         y = block.getYOffset();
      } else {
         if (borderTrait != Trait.BORDER_AFTER) {
            throw new IllegalArgumentException("Invalid trait: " + borderTrait);
         }

         x = block.getXOffset() - block.getBorderAndPaddingWidthStart();
         paddingAfter = (Integer)block.getTrait(Trait.PADDING_AFTER);
         y = block.getYOffset() + block.getBorderAndPaddingWidthBefore() + block.getBPD() + (paddingAfter != null ? paddingAfter : 0) + BorderProps.getClippedWidth(afterBps);
      }

      return new Point(x, y);
   }

   static class SortBlocksByYOffset implements Comparator, Serializable {
      private static final long serialVersionUID = -1166133555737149237L;

      public int compare(Object o1, Object o2) {
         Block b1 = (Block)o1;
         Block b2 = (Block)o2;
         Integer paddingStart1 = (Integer)b1.getTrait(Trait.PADDING_START);
         Integer paddingStart2 = (Integer)b2.getTrait(Trait.PADDING_START);
         int x1 = b1.getXOffset() - (paddingStart1 != null ? paddingStart1 : 0);
         int x2 = b2.getXOffset() - (paddingStart2 != null ? paddingStart2 : 0);
         if (b1.getYOffset() > b2.getYOffset()) {
            return 1;
         } else {
            return b1.getYOffset() < b2.getYOffset() ? -1 : Integer.compare(x1, x2);
         }
      }
   }

   static class SortBlocksByXOffset implements Comparator, Serializable {
      private static final long serialVersionUID = 5368454957520223766L;

      public int compare(Object o1, Object o2) {
         Block b1 = (Block)o1;
         Block b2 = (Block)o2;
         Integer paddingStart1 = (Integer)b1.getTrait(Trait.PADDING_START);
         Integer paddingStart2 = (Integer)b2.getTrait(Trait.PADDING_START);
         int x1 = b1.getXOffset() - (paddingStart1 != null ? paddingStart1 : 0);
         int x2 = b2.getXOffset() - (paddingStart2 != null ? paddingStart2 : 0);
         if (x1 > x2) {
            return 1;
         } else {
            return x1 < x2 ? -1 : Integer.compare(b1.getYOffset(), b2.getYOffset());
         }
      }
   }
}
