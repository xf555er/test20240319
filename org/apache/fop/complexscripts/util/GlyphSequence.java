package org.apache.fop.complexscripts.util;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlyphSequence implements Cloneable {
   private static final int DEFAULT_CHARS_CAPACITY = 8;
   private IntBuffer characters;
   private IntBuffer glyphs;
   private List associations;
   private boolean predications;
   protected GlyphSequence unprocessedGS;

   public GlyphSequence(IntBuffer characters, IntBuffer glyphs, List associations, boolean predications) {
      if (characters == null) {
         characters = IntBuffer.allocate(8);
      }

      if (glyphs == null) {
         glyphs = IntBuffer.allocate(characters.capacity());
      }

      if (associations == null) {
         associations = makeIdentityAssociations(characters.limit(), glyphs.limit());
      }

      this.characters = characters;
      this.glyphs = glyphs;
      this.associations = associations;
      this.predications = predications;
      this.unprocessedGS = this;
   }

   public GlyphSequence(IntBuffer characters, IntBuffer glyphs, List associations) {
      this(characters, glyphs, associations, false);
   }

   public GlyphSequence(GlyphSequence gs) {
      this(gs.characters.duplicate(), copyBuffer(gs.glyphs), copyAssociations(gs.associations), gs.predications);
      this.unprocessedGS = gs.unprocessedGS;
   }

   public GlyphSequence(GlyphSequence gs, int[] bga, int[] iga, int[] lga, CharAssociation[] bal, CharAssociation[] ial, CharAssociation[] lal) {
      this(gs.characters.duplicate(), concatGlyphs(bga, iga, lga), concatAssociations(bal, ial, lal), gs.predications);
   }

   public IntBuffer getCharacters() {
      return this.characters;
   }

   public int[] getCharacterArray(boolean copy) {
      return copy ? toArray(this.characters) : this.characters.array();
   }

   public int getCharacterCount() {
      return this.characters.limit();
   }

   public int getUTF16CharacterCount() {
      int count = 0;
      int[] var2 = this.characters.array();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int ch = var2[var4];
         count += Character.charCount(ch);
      }

      return count;
   }

   public int getGlyph(int index) throws IndexOutOfBoundsException {
      return this.glyphs.get(index);
   }

   public int getUnprocessedGlyph(int index) throws IndexOutOfBoundsException {
      return this.unprocessedGS.getGlyph(index);
   }

   public void setUnprocessedGS(GlyphSequence glyphSequence) {
      this.unprocessedGS = glyphSequence;
   }

   public void setGlyph(int index, int gi) throws IndexOutOfBoundsException {
      if (gi > 65535) {
         gi = 65535;
      }

      this.glyphs.put(index, gi);
   }

   public IntBuffer getGlyphs() {
      return this.glyphs;
   }

   public int[] getGlyphs(int offset, int count) {
      int ng = this.getGlyphCount();
      if (offset < 0) {
         offset = 0;
      } else if (offset > ng) {
         offset = ng;
      }

      if (count < 0) {
         count = ng - offset;
      }

      int[] ga = new int[count];
      int i = offset;
      int n = offset + count;

      for(int k = 0; i < n; ++i) {
         if (k < ga.length) {
            ga[k++] = this.glyphs.get(i);
         }
      }

      return ga;
   }

   public int[] getGlyphArray(boolean copy) {
      return copy ? toArray(this.glyphs) : this.glyphs.array();
   }

   public int getGlyphCount() {
      return this.glyphs.limit();
   }

   public CharAssociation getAssociation(int index) throws IndexOutOfBoundsException {
      return (CharAssociation)this.associations.get(index);
   }

   public List getAssociations() {
      return this.associations;
   }

   public CharAssociation[] getAssociations(int offset, int count) {
      int ng = this.getGlyphCount();
      if (offset < 0) {
         offset = 0;
      } else if (offset > ng) {
         offset = ng;
      }

      if (count < 0) {
         count = ng - offset;
      }

      CharAssociation[] aa = new CharAssociation[count];
      int i = offset;
      int n = offset + count;

      for(int k = 0; i < n; ++i) {
         if (k < aa.length) {
            aa[k++] = (CharAssociation)this.associations.get(i);
         }
      }

      return aa;
   }

   public void setPredications(boolean enable) {
      this.predications = enable;
   }

   public boolean getPredications() {
      return this.predications;
   }

   public void setPredication(int offset, String key, Object value) {
      if (this.predications) {
         CharAssociation[] aa = this.getAssociations(offset, 1);
         CharAssociation ca = aa[0];
         ca.setPredication(key, value);
      }

   }

   public Object getPredication(int offset, String key) {
      if (this.predications) {
         CharAssociation[] aa = this.getAssociations(offset, 1);
         CharAssociation ca = aa[0];
         return ca.getPredication(key);
      } else {
         return null;
      }
   }

   public int compareGlyphs(IntBuffer gb) {
      int ng = this.getGlyphCount();
      int i = 0;

      for(int n = gb.limit(); i < n; ++i) {
         if (i >= ng) {
            return -1;
         }

         int g1 = this.glyphs.get(i);
         int g2 = gb.get(i);
         if (g1 > g2) {
            return 1;
         }

         if (g1 < g2) {
            return -1;
         }
      }

      return 0;
   }

   public Object clone() {
      try {
         GlyphSequence gs = (GlyphSequence)super.clone();
         gs.characters = copyBuffer(this.characters);
         gs.glyphs = copyBuffer(this.glyphs);
         gs.associations = copyAssociations(this.associations);
         return gs;
      } catch (CloneNotSupportedException var2) {
         return null;
      }
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append('{');
      sb.append("chars = [");
      sb.append(this.characters);
      sb.append("], glyphs = [");
      sb.append(this.glyphs);
      sb.append("], associations = [");
      sb.append(this.associations);
      sb.append("]");
      sb.append('}');
      return sb.toString();
   }

   public static boolean sameGlyphs(int[] ga1, int[] ga2) {
      if (ga1 == ga2) {
         return true;
      } else if (ga1 != null && ga2 != null) {
         if (ga1.length != ga2.length) {
            return false;
         } else {
            int i = 0;

            for(int n = ga1.length; i < n; ++i) {
               if (ga1[i] != ga2[i]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static IntBuffer concatGlyphs(int[] bga, int[] iga, int[] lga) {
      int ng = 0;
      if (bga != null) {
         ng += bga.length;
      }

      if (iga != null) {
         ng += iga.length;
      }

      if (lga != null) {
         ng += lga.length;
      }

      IntBuffer gb = IntBuffer.allocate(ng);
      if (bga != null) {
         gb.put(bga);
      }

      if (iga != null) {
         gb.put(iga);
      }

      if (lga != null) {
         gb.put(lga);
      }

      gb.flip();
      return gb;
   }

   public static List concatAssociations(CharAssociation[] baa, CharAssociation[] iaa, CharAssociation[] laa) {
      int na = 0;
      if (baa != null) {
         na += baa.length;
      }

      if (iaa != null) {
         na += iaa.length;
      }

      if (laa != null) {
         na += laa.length;
      }

      if (na > 0) {
         List gl = new ArrayList(na);
         if (baa != null) {
            Collections.addAll(gl, baa);
         }

         if (iaa != null) {
            Collections.addAll(gl, iaa);
         }

         if (laa != null) {
            Collections.addAll(gl, laa);
         }

         return gl;
      } else {
         return null;
      }
   }

   public static GlyphSequence join(GlyphSequence gs, GlyphSequence[] sa) {
      assert sa != null;

      int tg = 0;
      int ta = 0;
      GlyphSequence[] var4 = sa;
      int var5 = sa.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         GlyphSequence s = var4[var6];
         IntBuffer ga = s.getGlyphs();

         assert ga != null;

         int ng = ga.limit();
         List al = s.getAssociations();

         assert al != null;

         int na = al.size();

         assert na == ng;

         tg += ng;
         ta += na;
      }

      IntBuffer uga = IntBuffer.allocate(tg);
      ArrayList ual = new ArrayList(ta);
      GlyphSequence[] var14 = sa;
      int var15 = sa.length;

      for(int var16 = 0; var16 < var15; ++var16) {
         GlyphSequence s = var14[var16];
         uga.put(s.getGlyphs());
         ual.addAll(s.getAssociations());
      }

      return new GlyphSequence(gs.getCharacters(), uga, ual, gs.getPredications());
   }

   public static GlyphSequence reorder(GlyphSequence gs, int source, int count, int target) {
      if (source == target) {
         return gs;
      } else {
         int ng = gs.getGlyphCount();
         int[] ga = gs.getGlyphArray(false);
         int[] nga = new int[ng];
         CharAssociation[] aa = gs.getAssociations(0, ng);
         CharAssociation[] naa = new CharAssociation[ng];
         int t;
         int s;
         int e;
         if (source < target) {
            t = 0;
            s = 0;

            for(e = source; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }

            s = source + count;

            for(e = target; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }

            s = source;

            for(e = source + count; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }

            s = target;

            for(e = ng; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }
         } else {
            t = 0;
            s = 0;

            for(e = target; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }

            s = source;

            for(e = source + count; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }

            s = target;

            for(e = source; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }

            s = source + count;

            for(e = ng; s < e; ++t) {
               nga[t] = ga[s];
               naa[t] = aa[s];
               ++s;
            }
         }

         return new GlyphSequence(gs, (int[])null, nga, (int[])null, (CharAssociation[])null, naa, (CharAssociation[])null);
      }
   }

   private static int[] toArray(IntBuffer ib) {
      if (ib != null) {
         int n = ib.limit();
         int[] ia = new int[n];
         ib.get(ia, 0, n);
         return ia;
      } else {
         return new int[0];
      }
   }

   private static List makeIdentityAssociations(int numChars, int numGlyphs) {
      int nc = numChars;
      List av = new ArrayList(numGlyphs);
      int i = 0;

      for(int n = numGlyphs; i < n; ++i) {
         int k = i > nc ? nc : i;
         av.add(new CharAssociation(i, k == nc ? 0 : 1));
      }

      return av;
   }

   private static IntBuffer copyBuffer(IntBuffer ib) {
      if (ib != null) {
         int[] ia = new int[ib.capacity()];
         int p = ib.position();
         int l = ib.limit();
         System.arraycopy(ib.array(), 0, ia, 0, ia.length);
         return IntBuffer.wrap(ia, p, l - p);
      } else {
         return null;
      }
   }

   private static List copyAssociations(List ca) {
      return (List)(ca != null ? new ArrayList(ca) : ca);
   }
}
