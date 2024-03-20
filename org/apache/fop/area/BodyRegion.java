package org.apache.fop.area;

import java.util.List;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.traits.WritingModeTraitsGetter;

public class BodyRegion extends RegionReference {
   private static final long serialVersionUID = -1848872997724078080L;
   private BeforeFloat beforeFloat;
   private MainReference mainReference;
   private Footnote footnote;
   private int columnGap;
   private int columnCount;

   public BodyRegion(RegionBody rb, RegionViewport parent) {
      this(rb.getNameId(), rb.getRegionName(), parent, rb.getColumnCount(), rb.getColumnGap());
   }

   public BodyRegion(int regionClass, String regionName, RegionViewport parent, int columnCount, int columnGap) {
      super(regionClass, regionName, parent);
      this.columnCount = columnCount;
      this.columnGap = columnGap;
      this.mainReference = new MainReference(this);
   }

   public int getColumnCount() {
      return this.columnCount;
   }

   public int getColumnGap() {
      return this.columnGap;
   }

   int getContentIPD() {
      RegionViewport rv = this.getRegionViewport();
      return this.getIPD() - rv.getBorderAndPaddingWidthStart() - rv.getBorderAndPaddingWidthEnd();
   }

   public int getColumnIPD() {
      return (this.getContentIPD() - (this.columnCount - 1) * this.columnGap) / this.columnCount;
   }

   public MainReference getMainReference() {
      return this.mainReference;
   }

   public boolean isEmpty() {
      return (this.mainReference == null || this.mainReference.isEmpty()) && (this.footnote == null || this.footnote.isEmpty()) && (this.beforeFloat == null || this.beforeFloat.isEmpty());
   }

   public BeforeFloat getBeforeFloat() {
      if (this.beforeFloat == null) {
         this.beforeFloat = new BeforeFloat();
      }

      return this.beforeFloat;
   }

   public Footnote getFootnote() {
      if (this.footnote == null) {
         this.footnote = new Footnote();
      }

      return this.footnote;
   }

   public int getRemainingBPD() {
      int usedBPD = 0;
      List spans = this.getMainReference().getSpans();
      int previousSpanCount = spans.size() - 1;

      for(int i = 0; i < previousSpanCount; ++i) {
         usedBPD += ((Span)spans.get(i)).getHeight();
      }

      return this.getBPD() - usedBPD;
   }

   public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
      if (this.getMainReference() != null) {
         this.getMainReference().setWritingModeTraits(wmtg);
      }

   }

   public Object clone() throws CloneNotSupportedException {
      BodyRegion br = (BodyRegion)super.clone();
      br.mainReference = new MainReference(br);
      br.mainReference.setSpans(this.mainReference.getSpans());
      return br;
   }
}
