package org.apache.batik.css.engine;

import org.apache.batik.css.engine.value.Value;

public class StyleMap {
   public static final short IMPORTANT_MASK = 1;
   public static final short COMPUTED_MASK = 2;
   public static final short NULL_CASCADED_MASK = 4;
   public static final short INHERITED_MASK = 8;
   public static final short LINE_HEIGHT_RELATIVE_MASK = 16;
   public static final short FONT_SIZE_RELATIVE_MASK = 32;
   public static final short COLOR_RELATIVE_MASK = 64;
   public static final short PARENT_RELATIVE_MASK = 128;
   public static final short BLOCK_WIDTH_RELATIVE_MASK = 256;
   public static final short BLOCK_HEIGHT_RELATIVE_MASK = 512;
   public static final short BOX_RELATIVE_MASK = 1024;
   public static final short ORIGIN_MASK = -8192;
   public static final short USER_AGENT_ORIGIN = 0;
   public static final short USER_ORIGIN = 8192;
   public static final short NON_CSS_ORIGIN = 16384;
   public static final short AUTHOR_ORIGIN = 24576;
   public static final short INLINE_AUTHOR_ORIGIN = Short.MIN_VALUE;
   public static final short OVERRIDE_ORIGIN = -24576;
   protected Value[] values;
   protected short[] masks;
   protected boolean fixedCascadedValues;

   public StyleMap(int size) {
      this.values = new Value[size];
      this.masks = new short[size];
   }

   public boolean hasFixedCascadedValues() {
      return this.fixedCascadedValues;
   }

   public void setFixedCascadedStyle(boolean b) {
      this.fixedCascadedValues = b;
   }

   public Value getValue(int i) {
      return this.values[i];
   }

   public short getMask(int i) {
      return this.masks[i];
   }

   public boolean isImportant(int i) {
      return (this.masks[i] & 1) != 0;
   }

   public boolean isComputed(int i) {
      return (this.masks[i] & 2) != 0;
   }

   public boolean isNullCascaded(int i) {
      return (this.masks[i] & 4) != 0;
   }

   public boolean isInherited(int i) {
      return (this.masks[i] & 8) != 0;
   }

   public short getOrigin(int i) {
      return (short)(this.masks[i] & -8192);
   }

   public boolean isColorRelative(int i) {
      return (this.masks[i] & 64) != 0;
   }

   public boolean isParentRelative(int i) {
      return (this.masks[i] & 128) != 0;
   }

   public boolean isLineHeightRelative(int i) {
      return (this.masks[i] & 16) != 0;
   }

   public boolean isFontSizeRelative(int i) {
      return (this.masks[i] & 32) != 0;
   }

   public boolean isBlockWidthRelative(int i) {
      return (this.masks[i] & 256) != 0;
   }

   public boolean isBlockHeightRelative(int i) {
      return (this.masks[i] & 512) != 0;
   }

   public void putValue(int i, Value v) {
      this.values[i] = v;
   }

   public void putMask(int i, short m) {
      this.masks[i] = m;
   }

   public void putImportant(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 1);
      } else {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] & -2);
      }

   }

   public void putOrigin(int i, short val) {
      short[] var10000 = this.masks;
      var10000[i] = (short)(var10000[i] & 8191);
      var10000 = this.masks;
      var10000[i] |= (short)(val & -8192);
   }

   public void putComputed(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 2);
      } else {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] & -3);
      }

   }

   public void putNullCascaded(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 4);
      } else {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] & -5);
      }

   }

   public void putInherited(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 8);
      } else {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] & -9);
      }

   }

   public void putColorRelative(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 64);
      } else {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] & -65);
      }

   }

   public void putParentRelative(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 128);
      } else {
         var10000 = this.masks;
         var10000[i] &= -129;
      }

   }

   public void putLineHeightRelative(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 16);
      } else {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] & -17);
      }

   }

   public void putFontSizeRelative(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 32);
      } else {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] & -33);
      }

   }

   public void putBlockWidthRelative(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 256);
      } else {
         var10000 = this.masks;
         var10000[i] &= -257;
      }

   }

   public void putBlockHeightRelative(int i, boolean b) {
      short[] var10000;
      if (b) {
         var10000 = this.masks;
         var10000[i] = (short)(var10000[i] | 512);
      } else {
         var10000 = this.masks;
         var10000[i] &= -513;
      }

   }

   public String toString(CSSEngine eng) {
      int nSlots = this.values.length;
      StringBuffer sb = new StringBuffer(nSlots * 8);

      for(int i = 0; i < nSlots; ++i) {
         Value v = this.values[i];
         if (v != null) {
            sb.append(eng.getPropertyName(i));
            sb.append(": ");
            sb.append(v);
            if (this.isImportant(i)) {
               sb.append(" !important");
            }

            sb.append(";\n");
         }
      }

      return sb.toString();
   }
}
