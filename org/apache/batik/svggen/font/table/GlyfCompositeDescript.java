package org.apache.batik.svggen.font.table;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GlyfCompositeDescript extends GlyfDescript {
   private List components = new ArrayList();
   protected boolean beingResolved = false;
   protected boolean resolved = false;

   public GlyfCompositeDescript(GlyfTable parentTable, ByteArrayInputStream bais) {
      super(parentTable, (short)-1, bais);

      GlyfCompositeComp comp;
      do {
         comp = new GlyfCompositeComp(bais);
         this.components.add(comp);
      } while((comp.getFlags() & 32) != 0);

      if ((comp.getFlags() & 256) != 0) {
         this.readInstructions(bais, bais.read() << 8 | bais.read());
      }

   }

   public void resolve() {
      if (!this.resolved) {
         if (this.beingResolved) {
            System.err.println("Circular reference in GlyfCompositeDesc");
         } else {
            this.beingResolved = true;
            int firstIndex = 0;
            int firstContour = 0;
            Iterator var3 = this.components.iterator();

            while(var3.hasNext()) {
               Object component = var3.next();
               GlyfCompositeComp comp = (GlyfCompositeComp)component;
               comp.setFirstIndex(firstIndex);
               comp.setFirstContour(firstContour);
               GlyfDescript desc = this.parentTable.getDescription(comp.getGlyphIndex());
               if (desc != null) {
                  desc.resolve();
                  firstIndex += desc.getPointCount();
                  firstContour += desc.getContourCount();
               }
            }

            this.resolved = true;
            this.beingResolved = false;
         }
      }
   }

   public int getEndPtOfContours(int i) {
      GlyfCompositeComp c = this.getCompositeCompEndPt(i);
      if (c != null) {
         GlyphDescription gd = this.parentTable.getDescription(c.getGlyphIndex());
         return gd.getEndPtOfContours(i - c.getFirstContour()) + c.getFirstIndex();
      } else {
         return 0;
      }
   }

   public byte getFlags(int i) {
      GlyfCompositeComp c = this.getCompositeComp(i);
      if (c != null) {
         GlyphDescription gd = this.parentTable.getDescription(c.getGlyphIndex());
         return gd.getFlags(i - c.getFirstIndex());
      } else {
         return 0;
      }
   }

   public short getXCoordinate(int i) {
      GlyfCompositeComp c = this.getCompositeComp(i);
      if (c != null) {
         GlyphDescription gd = this.parentTable.getDescription(c.getGlyphIndex());
         int n = i - c.getFirstIndex();
         int x = gd.getXCoordinate(n);
         int y = gd.getYCoordinate(n);
         short x1 = (short)c.scaleX(x, y);
         x1 = (short)(x1 + c.getXTranslate());
         return x1;
      } else {
         return 0;
      }
   }

   public short getYCoordinate(int i) {
      GlyfCompositeComp c = this.getCompositeComp(i);
      if (c != null) {
         GlyphDescription gd = this.parentTable.getDescription(c.getGlyphIndex());
         int n = i - c.getFirstIndex();
         int x = gd.getXCoordinate(n);
         int y = gd.getYCoordinate(n);
         short y1 = (short)c.scaleY(x, y);
         y1 = (short)(y1 + c.getYTranslate());
         return y1;
      } else {
         return 0;
      }
   }

   public boolean isComposite() {
      return true;
   }

   public int getPointCount() {
      if (!this.resolved) {
         System.err.println("getPointCount called on unresolved GlyfCompositeDescript");
      }

      GlyfCompositeComp c = (GlyfCompositeComp)this.components.get(this.components.size() - 1);
      return c.getFirstIndex() + this.parentTable.getDescription(c.getGlyphIndex()).getPointCount();
   }

   public int getContourCount() {
      if (!this.resolved) {
         System.err.println("getContourCount called on unresolved GlyfCompositeDescript");
      }

      GlyfCompositeComp c = (GlyfCompositeComp)this.components.get(this.components.size() - 1);
      return c.getFirstContour() + this.parentTable.getDescription(c.getGlyphIndex()).getContourCount();
   }

   public int getComponentIndex(int i) {
      return ((GlyfCompositeComp)this.components.get(i)).getFirstIndex();
   }

   public int getComponentCount() {
      return this.components.size();
   }

   protected GlyfCompositeComp getCompositeComp(int i) {
      Iterator var3 = this.components.iterator();

      GlyfCompositeComp c;
      GlyfDescript gd;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         Object component = var3.next();
         c = (GlyfCompositeComp)component;
         gd = this.parentTable.getDescription(c.getGlyphIndex());
      } while(c.getFirstIndex() > i || i >= c.getFirstIndex() + gd.getPointCount());

      return c;
   }

   protected GlyfCompositeComp getCompositeCompEndPt(int i) {
      Iterator var3 = this.components.iterator();

      GlyfCompositeComp c;
      GlyfDescript gd;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         Object component = var3.next();
         c = (GlyfCompositeComp)component;
         gd = this.parentTable.getDescription(c.getGlyphIndex());
      } while(c.getFirstContour() > i || i >= c.getFirstContour() + gd.getContourCount());

      return c;
   }
}
