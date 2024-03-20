package org.apache.fop.fo.flow.table;

import org.apache.fop.layoutmgr.table.CollapsingBorderModel;

public class ConditionalBorder {
   public static final int NORMAL = 0;
   public static final int LEADING_TRAILING = 1;
   public static final int REST = 2;
   BorderSpecification normal;
   BorderSpecification leadingTrailing;
   BorderSpecification rest;
   private CollapsingBorderModel collapsingBorderModel;

   private ConditionalBorder(BorderSpecification normal, BorderSpecification leadingTrailing, BorderSpecification rest, CollapsingBorderModel collapsingBorderModel) {
      assert collapsingBorderModel != null;

      this.normal = normal;
      this.leadingTrailing = leadingTrailing;
      this.rest = rest;
      this.collapsingBorderModel = collapsingBorderModel;
   }

   ConditionalBorder(BorderSpecification borderSpecification, CollapsingBorderModel collapsingBorderModel) {
      this(borderSpecification, borderSpecification, borderSpecification.getBorderInfo().getWidth().isDiscard() ? BorderSpecification.getDefaultBorder() : borderSpecification, collapsingBorderModel);
   }

   void resolve(ConditionalBorder competitor, boolean withNormal, boolean withLeadingTrailing, boolean withRest) {
      BorderSpecification resolvedBorder;
      if (withNormal) {
         resolvedBorder = this.collapsingBorderModel.determineWinner(this.normal, competitor.normal);
         if (resolvedBorder != null) {
            this.normal = resolvedBorder;
            competitor.normal = resolvedBorder;
         }
      }

      if (withLeadingTrailing) {
         resolvedBorder = this.collapsingBorderModel.determineWinner(this.leadingTrailing, competitor.leadingTrailing);
         if (resolvedBorder != null) {
            this.leadingTrailing = resolvedBorder;
            competitor.leadingTrailing = resolvedBorder;
         }
      }

      if (withRest) {
         resolvedBorder = this.collapsingBorderModel.determineWinner(this.rest, competitor.rest);
         if (resolvedBorder != null) {
            this.rest = resolvedBorder;
            competitor.rest = resolvedBorder;
         }
      }

   }

   void integrateCompetingSegment(ConditionalBorder competitor, boolean withNormal, boolean withLeadingTrailing, boolean withRest) {
      BorderSpecification resolvedBorder;
      if (withNormal) {
         resolvedBorder = this.collapsingBorderModel.determineWinner(this.normal, competitor.normal);
         if (resolvedBorder != null) {
            this.normal = resolvedBorder;
         }
      }

      if (withLeadingTrailing) {
         resolvedBorder = this.collapsingBorderModel.determineWinner(this.leadingTrailing, competitor.leadingTrailing);
         if (resolvedBorder != null) {
            this.leadingTrailing = resolvedBorder;
         }
      }

      if (withRest) {
         resolvedBorder = this.collapsingBorderModel.determineWinner(this.rest, competitor.rest);
         if (resolvedBorder != null) {
            this.rest = resolvedBorder;
         }
      }

   }

   void integrateSegment(ConditionalBorder segment, boolean withNormal, boolean withLeadingTrailing, boolean withRest) {
      if (withNormal) {
         this.normal = this.collapsingBorderModel.determineWinner(this.normal, segment.normal);

         assert this.normal != null;
      }

      if (withLeadingTrailing) {
         this.leadingTrailing = this.collapsingBorderModel.determineWinner(this.leadingTrailing, segment.leadingTrailing);

         assert this.leadingTrailing != null;
      }

      if (withRest) {
         this.rest = this.collapsingBorderModel.determineWinner(this.rest, segment.rest);

         assert this.rest != null;
      }

   }

   ConditionalBorder copy() {
      return new ConditionalBorder(this.normal, this.leadingTrailing, this.rest, this.collapsingBorderModel);
   }

   public String toString() {
      return "{normal: " + this.normal + ", leading: " + this.leadingTrailing + ", rest: " + this.rest + "}";
   }

   static ConditionalBorder getDefaultBorder(CollapsingBorderModel collapsingBorderModel) {
      BorderSpecification defaultBorderSpec = BorderSpecification.getDefaultBorder();
      return new ConditionalBorder(defaultBorderSpec, defaultBorderSpec, defaultBorderSpec, collapsingBorderModel);
   }
}
