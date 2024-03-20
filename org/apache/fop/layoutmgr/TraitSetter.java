package org.apache.fop.layoutmgr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fonts.Font;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.Visibility;

public final class TraitSetter {
   private static final Log LOG = LogFactory.getLog(TraitSetter.class);

   private TraitSetter() {
   }

   public static void setBorderPaddingTraits(Area area, CommonBorderPaddingBackground bpProps, boolean isNotFirst, boolean isNotLast, PercentBaseContext context) {
      int padding = bpProps.getPadding(2, isNotFirst, context);
      if (padding > 0) {
         area.addTrait(Trait.PADDING_START, padding);
      }

      padding = bpProps.getPadding(3, isNotLast, context);
      if (padding > 0) {
         area.addTrait(Trait.PADDING_END, padding);
      }

      padding = bpProps.getPadding(0, false, context);
      if (padding > 0) {
         area.addTrait(Trait.PADDING_BEFORE, padding);
      }

      padding = bpProps.getPadding(1, false, context);
      if (padding > 0) {
         area.addTrait(Trait.PADDING_AFTER, padding);
      }

      addBorderTrait(area, bpProps, isNotFirst, 2, BorderProps.Mode.SEPARATE, Trait.BORDER_START, context);
      addBorderTrait(area, bpProps, isNotLast, 3, BorderProps.Mode.SEPARATE, Trait.BORDER_END, context);
      addBorderTrait(area, bpProps, false, 0, BorderProps.Mode.SEPARATE, Trait.BORDER_BEFORE, context);
      addBorderTrait(area, bpProps, false, 1, BorderProps.Mode.SEPARATE, Trait.BORDER_AFTER, context);
   }

   private static void addBorderTrait(Area area, CommonBorderPaddingBackground bpProps, boolean discard, int side, BorderProps.Mode mode, Integer traitCode, PercentBaseContext context) {
      int width = bpProps.getBorderWidth(side, discard);
      int radiusStart = bpProps.getBorderRadiusStart(side, discard, context);
      int radiusEnd = bpProps.getBorderRadiusEnd(side, discard, context);
      if (width > 0 || radiusStart > 0 || radiusEnd > 0) {
         area.addTrait(traitCode, new BorderProps(bpProps.getBorderStyle(side), width, radiusStart, radiusEnd, bpProps.getBorderColor(side), mode));
      }

   }

   /** @deprecated */
   public static void addBorders(Area area, CommonBorderPaddingBackground borderProps, PercentBaseContext context) {
      BorderProps bps = getBorderProps(borderProps, 0, context);
      if (bps != null) {
         area.addTrait(Trait.BORDER_BEFORE, bps);
      }

      bps = getBorderProps(borderProps, 1, context);
      if (bps != null) {
         area.addTrait(Trait.BORDER_AFTER, bps);
      }

      bps = getBorderProps(borderProps, 2, context);
      if (bps != null) {
         area.addTrait(Trait.BORDER_START, bps);
      }

      bps = getBorderProps(borderProps, 3, context);
      if (bps != null) {
         area.addTrait(Trait.BORDER_END, bps);
      }

      addPadding(area, borderProps, context);
   }

   public static void addBorders(Area area, CommonBorderPaddingBackground borderProps, boolean discardBefore, boolean discardAfter, boolean discardStart, boolean discardEnd, PercentBaseContext context) {
      BorderProps bps = getBorderProps(borderProps, 0, context);
      if (bps != null && !discardBefore) {
         area.addTrait(Trait.BORDER_BEFORE, bps);
      }

      bps = getBorderProps(borderProps, 1, context);
      if (bps != null && !discardAfter) {
         area.addTrait(Trait.BORDER_AFTER, bps);
      }

      bps = getBorderProps(borderProps, 2, context);
      if (bps != null && !discardStart) {
         area.addTrait(Trait.BORDER_START, bps);
      }

      bps = getBorderProps(borderProps, 3, context);
      if (bps != null && !discardEnd) {
         area.addTrait(Trait.BORDER_END, bps);
      }

   }

   public static void addCollapsingBorders(Area area, CommonBorderPaddingBackground.BorderInfo borderBefore, CommonBorderPaddingBackground.BorderInfo borderAfter, CommonBorderPaddingBackground.BorderInfo borderStart, CommonBorderPaddingBackground.BorderInfo borderEnd, boolean[] outer) {
      BorderProps bps = getCollapsingBorderProps(borderBefore, outer[0]);
      if (bps != null) {
         area.addTrait(Trait.BORDER_BEFORE, bps);
      }

      bps = getCollapsingBorderProps(borderAfter, outer[1]);
      if (bps != null) {
         area.addTrait(Trait.BORDER_AFTER, bps);
      }

      bps = getCollapsingBorderProps(borderStart, outer[2]);
      if (bps != null) {
         area.addTrait(Trait.BORDER_START, bps);
      }

      bps = getCollapsingBorderProps(borderEnd, outer[3]);
      if (bps != null) {
         area.addTrait(Trait.BORDER_END, bps);
      }

   }

   private static void addPadding(Area area, CommonBorderPaddingBackground bordProps, PercentBaseContext context) {
      addPadding(area, bordProps, false, false, false, false, context);
   }

   public static void addPadding(Area area, CommonBorderPaddingBackground bordProps, boolean discardBefore, boolean discardAfter, boolean discardStart, boolean discardEnd, PercentBaseContext context) {
      int padding = bordProps.getPadding(0, discardBefore, context);
      if (padding != 0) {
         area.addTrait(Trait.PADDING_BEFORE, padding);
      }

      padding = bordProps.getPadding(1, discardAfter, context);
      if (padding != 0) {
         area.addTrait(Trait.PADDING_AFTER, padding);
      }

      padding = bordProps.getPadding(2, discardStart, context);
      if (padding != 0) {
         area.addTrait(Trait.PADDING_START, padding);
      }

      padding = bordProps.getPadding(3, discardEnd, context);
      if (padding != 0) {
         area.addTrait(Trait.PADDING_END, padding);
      }

   }

   private static BorderProps getBorderProps(CommonBorderPaddingBackground bordProps, int side, PercentBaseContext context) {
      int width = bordProps.getBorderWidth(side, false);
      int radiusStart = bordProps.getBorderRadiusStart(side, false, context);
      int radiusEnd = bordProps.getBorderRadiusEnd(side, false, context);
      return width == 0 && radiusStart == 0 && radiusEnd == 0 ? null : new BorderProps(bordProps.getBorderStyle(side), width, radiusStart, radiusEnd, bordProps.getBorderColor(side), BorderProps.Mode.SEPARATE);
   }

   private static BorderProps getCollapsingBorderProps(CommonBorderPaddingBackground.BorderInfo borderInfo, boolean outer) {
      assert borderInfo != null;

      int width = borderInfo.getRetainedWidth();
      return width != 0 ? BorderProps.makeRectangular(borderInfo.getStyle(), width, borderInfo.getColor(), outer ? BorderProps.Mode.COLLAPSE_OUTER : BorderProps.Mode.COLLAPSE_INNER) : null;
   }

   public static void addBackground(Area area, CommonBorderPaddingBackground backProps, PercentBaseContext context, int ipdShift, int bpdShift, int referenceIPD, int referenceBPD) {
      if (backProps.hasBackground()) {
         Trait.Background back = new Trait.Background();
         back.setColor(backProps.backgroundColor);
         if (backProps.getImageInfo() != null) {
            back.setURL(backProps.backgroundImage);
            back.setImageInfo(backProps.getImageInfo());
            back.setRepeat(backProps.backgroundRepeat);
            SimplePercentBaseContext refContext;
            if (backProps.backgroundPositionHorizontal != null && (back.getRepeat() == 96 || back.getRepeat() == 114)) {
               if (area.getIPD() > 0) {
                  refContext = new SimplePercentBaseContext(context, 9, referenceIPD - back.getImageInfo().getSize().getWidthMpt());
                  back.setHoriz(ipdShift + backProps.backgroundPositionHorizontal.getValue(refContext));
               } else {
                  LOG.warn("Horizontal background image positioning ignored because the IPD was not set on the area. (Yes, it's a bug in FOP)");
               }
            }

            if (backProps.backgroundPositionVertical != null && (back.getRepeat() == 96 || back.getRepeat() == 113)) {
               if (area.getBPD() > 0) {
                  refContext = new SimplePercentBaseContext(context, 10, referenceBPD - back.getImageInfo().getSize().getHeightMpt());
                  back.setVertical(bpdShift + backProps.backgroundPositionVertical.getValue(refContext));
               } else {
                  LOG.warn("Vertical background image positioning ignored because the BPD was not set on the area. (Yes, it's a bug in FOP)");
               }
            }
         }

         area.addTrait(Trait.BACKGROUND, back);
      }
   }

   public static void addBackground(Area area, CommonBorderPaddingBackground backProps, PercentBaseContext context) {
      if (backProps.hasBackground()) {
         Trait.Background back = new Trait.Background();
         back.setColor(backProps.backgroundColor);
         if (backProps.getImageInfo() != null) {
            back.setURL(backProps.backgroundImage);
            back.setImageInfo(backProps.getImageInfo());
            back.setRepeat(backProps.backgroundRepeat);
            int height;
            int imageHeightMpt;
            int lengthBaseValue;
            SimplePercentBaseContext simplePercentBaseContext;
            int vertical;
            if (backProps.backgroundPositionHorizontal != null && (back.getRepeat() == 96 || back.getRepeat() == 114)) {
               if (area.getIPD() > 0) {
                  height = area.getIPD();
                  height += backProps.getPaddingStart(false, context);
                  height += backProps.getPaddingEnd(false, context);
                  imageHeightMpt = back.getImageInfo().getSize().getWidthMpt();
                  lengthBaseValue = height - imageHeightMpt;
                  simplePercentBaseContext = new SimplePercentBaseContext(context, 9, lengthBaseValue);
                  vertical = backProps.backgroundPositionHorizontal.getValue(simplePercentBaseContext);
                  back.setHoriz(vertical);
               } else {
                  LOG.warn("Horizontal background image positioning ignored because the IPD was not set on the area. (Yes, it's a bug in FOP)");
               }
            }

            if (backProps.backgroundPositionVertical != null && (back.getRepeat() == 96 || back.getRepeat() == 113)) {
               if (area.getBPD() > 0) {
                  height = area.getBPD();
                  height += backProps.getPaddingBefore(false, context);
                  height += backProps.getPaddingAfter(false, context);
                  imageHeightMpt = back.getImageInfo().getSize().getHeightMpt();
                  lengthBaseValue = height - imageHeightMpt;
                  simplePercentBaseContext = new SimplePercentBaseContext(context, 10, lengthBaseValue);
                  vertical = backProps.backgroundPositionVertical.getValue(simplePercentBaseContext);
                  back.setVertical(vertical);
               } else {
                  LOG.warn("Vertical background image positioning ignored because the BPD was not set on the area. (Yes, it's a bug in FOP)");
               }
            }

            if (backProps.backgroungImageTargetWidth.getValue() != 0) {
               back.setImageTargetWidth(backProps.backgroungImageTargetWidth.getValue());
            }

            if (backProps.backgroungImageTargetHeight.getValue() != 0) {
               back.setImageTargetHeight(backProps.backgroungImageTargetHeight.getValue());
            }
         }

         area.addTrait(Trait.BACKGROUND, back);
      }
   }

   public static void addMargins(Area area, CommonBorderPaddingBackground bpProps, int startIndent, int endIndent, PercentBaseContext context) {
      if (startIndent != 0) {
         area.addTrait(Trait.START_INDENT, startIndent);
      }

      int spaceStart = startIndent - bpProps.getBorderStartWidth(false) - bpProps.getPaddingStart(false, context);
      if (spaceStart != 0) {
         area.addTrait(Trait.SPACE_START, spaceStart);
      }

      if (endIndent != 0) {
         area.addTrait(Trait.END_INDENT, endIndent);
      }

      int spaceEnd = endIndent - bpProps.getBorderEndWidth(false) - bpProps.getPaddingEnd(false, context);
      if (spaceEnd != 0) {
         area.addTrait(Trait.SPACE_END, spaceEnd);
      }

   }

   public static void addMargins(Area area, CommonBorderPaddingBackground bpProps, CommonMarginBlock marginProps, PercentBaseContext context) {
      int startIndent = marginProps.startIndent.getValue(context);
      int endIndent = marginProps.endIndent.getValue(context);
      addMargins(area, bpProps, startIndent, endIndent, context);
   }

   public static int getEffectiveSpace(double adjust, MinOptMax space) {
      if (space == null) {
         return 0;
      } else {
         int spaceOpt = space.getOpt();
         if (adjust > 0.0) {
            spaceOpt += (int)(adjust * (double)space.getStretch());
         } else {
            spaceOpt += (int)(adjust * (double)space.getShrink());
         }

         return spaceOpt;
      }
   }

   public static void addSpaceBeforeAfter(Area area, double adjust, MinOptMax spaceBefore, MinOptMax spaceAfter) {
      addSpaceTrait(area, Trait.SPACE_BEFORE, spaceBefore, adjust);
      addSpaceTrait(area, Trait.SPACE_AFTER, spaceAfter, adjust);
   }

   private static void addSpaceTrait(Area area, Integer spaceTrait, MinOptMax space, double adjust) {
      int effectiveSpace = getEffectiveSpace(adjust, space);
      if (effectiveSpace != 0) {
         area.addTrait(spaceTrait, effectiveSpace);
      }

   }

   public static void addBreaks(Area area, int breakBefore, int breakAfter) {
   }

   public static void addFontTraits(Area area, Font font) {
      area.addTrait(Trait.FONT, font.getFontTriplet());
      area.addTrait(Trait.FONT_SIZE, font.getFontSize());
   }

   public static void addTextDecoration(Area area, CommonTextDecoration deco) {
      if (deco != null) {
         if (deco.hasUnderline()) {
            area.addTrait(Trait.UNDERLINE, Boolean.TRUE);
            area.addTrait(Trait.UNDERLINE_COLOR, deco.getUnderlineColor());
         }

         if (deco.hasOverline()) {
            area.addTrait(Trait.OVERLINE, Boolean.TRUE);
            area.addTrait(Trait.OVERLINE_COLOR, deco.getOverlineColor());
         }

         if (deco.hasLineThrough()) {
            area.addTrait(Trait.LINETHROUGH, Boolean.TRUE);
            area.addTrait(Trait.LINETHROUGH_COLOR, deco.getLineThroughColor());
         }

         if (deco.isBlinking()) {
            area.addTrait(Trait.BLINK, Boolean.TRUE);
         }
      }

   }

   public static void setVisibility(Area area, int visibility) {
      Visibility v;
      switch (visibility) {
         case 26:
            v = Visibility.COLLAPSE;
            break;
         case 57:
            v = Visibility.HIDDEN;
            break;
         default:
            v = Visibility.VISIBLE;
      }

      area.addTrait(Trait.VISIBILITY, v);
   }

   public static void addStructureTreeElement(Area area, StructureTreeElement structureTreeElement) {
      if (structureTreeElement != null) {
         area.addTrait(Trait.STRUCTURE_TREE_ELEMENT, structureTreeElement);
      }

   }

   public static void setProducerID(Area area, String id) {
      if (id != null && id.length() > 0) {
         area.addTrait(Trait.PROD_ID, id);
      }

   }

   public static void setLayer(Area area, String layer) {
      if (layer != null && layer.length() > 0) {
         area.addTrait(Trait.LAYER, layer);
      }

   }
}
