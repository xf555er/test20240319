package org.apache.fop.area;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.traits.WritingModeTraitsGetter;

public class MainReference extends Area {
   private static final long serialVersionUID = 7635126485620012448L;
   private BodyRegion parent;
   private List spanAreas = new ArrayList();
   private boolean isEmpty = true;
   private transient WritingModeTraitsGetter wmtg;

   public MainReference(BodyRegion parent) {
      this.parent = parent;
      this.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
   }

   public Span createSpan(boolean spanAll) {
      if (this.spanAreas.size() > 0 && this.getCurrentSpan().isEmpty()) {
         this.spanAreas.remove(this.spanAreas.size() - 1);
      }

      Span newSpan = new Span(spanAll ? 1 : this.getColumnCount(), this.getColumnGap(), this.parent.getContentIPD());
      this.spanAreas.add(newSpan);
      if (this.wmtg != null) {
         newSpan.setWritingModeTraits(this.wmtg);
      }

      return this.getCurrentSpan();
   }

   public List getSpans() {
      return this.spanAreas;
   }

   public void setSpans(List spans) {
      this.spanAreas = new ArrayList(spans);
   }

   public Span getCurrentSpan() {
      return (Span)this.spanAreas.get(this.spanAreas.size() - 1);
   }

   public boolean isEmpty() {
      if (this.isEmpty && this.spanAreas != null) {
         Iterator var1 = this.spanAreas.iterator();

         while(var1.hasNext()) {
            Span spanArea = (Span)var1.next();
            if (!spanArea.isEmpty()) {
               this.isEmpty = false;
               break;
            }
         }
      }

      return this.isEmpty;
   }

   public int getColumnCount() {
      return this.parent.getColumnCount();
   }

   public int getColumnGap() {
      return this.parent.getColumnGap();
   }

   public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
      this.wmtg = wmtg;
      Iterator var2 = this.getSpans().iterator();

      while(var2.hasNext()) {
         Span s = (Span)var2.next();
         s.setWritingModeTraits(wmtg);
      }

   }
}
