package org.apache.fop.fonts.base14;

import java.awt.Rectangle;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.fop.fonts.Base14Font;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.FontType;

public class ZapfDingbats extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "ZapfDingbats";
   private static final String fullName = "ITC Zapf Dingbats";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "ZapfDingbatsEncoding";
   private static final int capHeight = 820;
   private static final int xHeight = 426;
   private static final int ascender = 820;
   private static final int descender = -143;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private boolean enableKerning;

   public ZapfDingbats() {
      this(false);
   }

   public ZapfDingbats(boolean enableKerning) {
      this.mapping = CodePointMapping.getMapping("ZapfDingbatsEncoding");
      this.enableKerning = enableKerning;
   }

   public String getEncodingName() {
      return "ZapfDingbatsEncoding";
   }

   public URI getFontURI() {
      return fontFileURI;
   }

   public String getFontName() {
      return "ZapfDingbats";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "ITC Zapf Dingbats";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 820;
   }

   public int getCapHeight(int size) {
      return size * 820;
   }

   public int getDescender(int size) {
      return size * -143;
   }

   public int getXHeight(int size) {
      return size * 426;
   }

   public int getUnderlinePosition(int size) {
      return size * -100;
   }

   public int getUnderlineThickness(int size) {
      return size * 50;
   }

   public int getFirstChar() {
      return 32;
   }

   public int getLastChar() {
      return 255;
   }

   public int getWidth(int i, int size) {
      return size * width[i];
   }

   public Rectangle getBoundingBox(int glyphIndex, int size) {
      Rectangle bbox = boundingBoxes[glyphIndex];
      return new Rectangle(bbox.x * size, bbox.y * size, bbox.width * size, bbox.height * size);
   }

   public int[] getWidths() {
      int[] arr = new int[this.getLastChar() - this.getFirstChar() + 1];
      System.arraycopy(width, this.getFirstChar(), arr, 0, this.getLastChar() - this.getFirstChar() + 1);
      return arr;
   }

   public boolean hasKerningInfo() {
      return false;
   }

   public Map getKerningInfo() {
      return Collections.EMPTY_MAP;
   }

   public char mapChar(char c) {
      this.notifyMapOperation();
      char d = this.mapping.mapChar(c);
      if (d != 0) {
         return d;
      } else {
         this.warnMissingGlyph(c);
         return '#';
      }
   }

   public boolean hasChar(char c) {
      return this.mapping.mapChar(c) > 0;
   }

   static {
      URI uri = null;

      try {
         uri = new URI("base14:" + "ZapfDingbats".toLowerCase());
      } catch (URISyntaxException var2) {
         throw new RuntimeException(var2);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[32] = 278;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[33] = 974;
      boundingBoxes[33] = new Rectangle(35, 72, 904, 549);
      width[34] = 961;
      boundingBoxes[34] = new Rectangle(35, 81, 892, 530);
      width[35] = 974;
      boundingBoxes[35] = new Rectangle(35, 72, 904, 549);
      width[36] = 980;
      boundingBoxes[36] = new Rectangle(35, 0, 910, 692);
      width[37] = 719;
      boundingBoxes[37] = new Rectangle(34, 139, 651, 427);
      width[38] = 789;
      boundingBoxes[38] = new Rectangle(35, -14, 720, 719);
      width[39] = 790;
      boundingBoxes[39] = new Rectangle(35, -14, 720, 719);
      width[40] = 791;
      boundingBoxes[40] = new Rectangle(35, -13, 726, 718);
      width[41] = 690;
      boundingBoxes[41] = new Rectangle(34, 138, 621, 415);
      width[42] = 960;
      boundingBoxes[42] = new Rectangle(35, 123, 890, 445);
      width[43] = 939;
      boundingBoxes[43] = new Rectangle(35, 134, 869, 425);
      width[44] = 549;
      boundingBoxes[44] = new Rectangle(29, -11, 487, 716);
      width[45] = 855;
      boundingBoxes[45] = new Rectangle(34, 59, 786, 573);
      width[46] = 911;
      boundingBoxes[46] = new Rectangle(35, 50, 841, 592);
      width[47] = 933;
      boundingBoxes[47] = new Rectangle(35, 139, 864, 411);
      width[48] = 911;
      boundingBoxes[48] = new Rectangle(35, 50, 841, 592);
      width[49] = 945;
      boundingBoxes[49] = new Rectangle(35, 139, 874, 414);
      width[50] = 974;
      boundingBoxes[50] = new Rectangle(35, 104, 903, 483);
      width[51] = 755;
      boundingBoxes[51] = new Rectangle(34, -13, 687, 718);
      width[52] = 846;
      boundingBoxes[52] = new Rectangle(36, -14, 775, 719);
      width[53] = 762;
      boundingBoxes[53] = new Rectangle(35, 0, 692, 692);
      width[54] = 761;
      boundingBoxes[54] = new Rectangle(35, 0, 692, 692);
      width[55] = 571;
      boundingBoxes[55] = new Rectangle(-1, -68, 572, 729);
      width[56] = 677;
      boundingBoxes[56] = new Rectangle(36, -13, 606, 718);
      width[57] = 763;
      boundingBoxes[57] = new Rectangle(35, 0, 693, 692);
      width[58] = 760;
      boundingBoxes[58] = new Rectangle(35, 0, 691, 692);
      width[59] = 759;
      boundingBoxes[59] = new Rectangle(35, 0, 690, 692);
      width[60] = 754;
      boundingBoxes[60] = new Rectangle(35, 0, 685, 692);
      width[61] = 494;
      boundingBoxes[61] = new Rectangle(35, 0, 425, 692);
      width[62] = 552;
      boundingBoxes[62] = new Rectangle(35, 0, 482, 692);
      width[63] = 537;
      boundingBoxes[63] = new Rectangle(35, 0, 468, 692);
      width[64] = 577;
      boundingBoxes[64] = new Rectangle(35, 96, 507, 500);
      width[65] = 692;
      boundingBoxes[65] = new Rectangle(35, -14, 622, 719);
      width[66] = 786;
      boundingBoxes[66] = new Rectangle(35, -14, 716, 719);
      width[67] = 788;
      boundingBoxes[67] = new Rectangle(35, -14, 717, 719);
      width[68] = 788;
      boundingBoxes[68] = new Rectangle(35, -14, 718, 719);
      width[69] = 790;
      boundingBoxes[69] = new Rectangle(35, -14, 721, 719);
      width[70] = 793;
      boundingBoxes[70] = new Rectangle(35, -13, 724, 718);
      width[71] = 794;
      boundingBoxes[71] = new Rectangle(35, -13, 724, 718);
      width[72] = 816;
      boundingBoxes[72] = new Rectangle(35, -14, 747, 719);
      width[73] = 823;
      boundingBoxes[73] = new Rectangle(35, -14, 752, 719);
      width[74] = 789;
      boundingBoxes[74] = new Rectangle(35, -14, 719, 719);
      width[75] = 841;
      boundingBoxes[75] = new Rectangle(35, -14, 772, 719);
      width[76] = 823;
      boundingBoxes[76] = new Rectangle(35, -14, 754, 719);
      width[77] = 833;
      boundingBoxes[77] = new Rectangle(35, -14, 763, 719);
      width[78] = 816;
      boundingBoxes[78] = new Rectangle(35, -13, 747, 718);
      width[79] = 831;
      boundingBoxes[79] = new Rectangle(35, -14, 761, 719);
      width[80] = 923;
      boundingBoxes[80] = new Rectangle(35, -14, 853, 719);
      width[81] = 744;
      boundingBoxes[81] = new Rectangle(35, 0, 675, 692);
      width[82] = 723;
      boundingBoxes[82] = new Rectangle(35, 0, 653, 692);
      width[83] = 749;
      boundingBoxes[83] = new Rectangle(35, 0, 679, 692);
      width[84] = 790;
      boundingBoxes[84] = new Rectangle(34, -14, 722, 719);
      width[85] = 792;
      boundingBoxes[85] = new Rectangle(35, -14, 723, 719);
      width[86] = 695;
      boundingBoxes[86] = new Rectangle(35, -14, 626, 720);
      width[87] = 776;
      boundingBoxes[87] = new Rectangle(35, -6, 706, 705);
      width[88] = 768;
      boundingBoxes[88] = new Rectangle(35, -7, 699, 706);
      width[89] = 792;
      boundingBoxes[89] = new Rectangle(35, -14, 722, 719);
      width[90] = 759;
      boundingBoxes[90] = new Rectangle(35, 0, 690, 692);
      width[91] = 707;
      boundingBoxes[91] = new Rectangle(35, -13, 637, 717);
      width[92] = 708;
      boundingBoxes[92] = new Rectangle(35, -14, 637, 719);
      width[93] = 682;
      boundingBoxes[93] = new Rectangle(35, -14, 612, 719);
      width[94] = 701;
      boundingBoxes[94] = new Rectangle(35, -14, 631, 719);
      width[95] = 826;
      boundingBoxes[95] = new Rectangle(35, -14, 756, 719);
      width[96] = 815;
      boundingBoxes[96] = new Rectangle(35, -14, 745, 719);
      width[97] = 789;
      boundingBoxes[97] = new Rectangle(35, -14, 719, 719);
      width[98] = 789;
      boundingBoxes[98] = new Rectangle(35, -14, 719, 719);
      width[99] = 707;
      boundingBoxes[99] = new Rectangle(34, -14, 639, 719);
      width[100] = 687;
      boundingBoxes[100] = new Rectangle(36, 0, 615, 692);
      width[101] = 696;
      boundingBoxes[101] = new Rectangle(35, 0, 626, 691);
      width[102] = 689;
      boundingBoxes[102] = new Rectangle(35, 0, 620, 692);
      width[103] = 786;
      boundingBoxes[103] = new Rectangle(34, -14, 717, 719);
      width[104] = 787;
      boundingBoxes[104] = new Rectangle(35, -14, 717, 719);
      width[105] = 713;
      boundingBoxes[105] = new Rectangle(35, -14, 643, 719);
      width[106] = 791;
      boundingBoxes[106] = new Rectangle(35, -14, 721, 719);
      width[107] = 785;
      boundingBoxes[107] = new Rectangle(36, -14, 715, 719);
      width[108] = 791;
      boundingBoxes[108] = new Rectangle(35, -14, 722, 719);
      width[109] = 873;
      boundingBoxes[109] = new Rectangle(35, -14, 803, 719);
      width[110] = 761;
      boundingBoxes[110] = new Rectangle(35, 0, 691, 692);
      width[111] = 762;
      boundingBoxes[111] = new Rectangle(35, 0, 692, 692);
      width[112] = 762;
      boundingBoxes[112] = new Rectangle(35, 0, 692, 692);
      width[113] = 759;
      boundingBoxes[113] = new Rectangle(35, 0, 690, 692);
      width[114] = 759;
      boundingBoxes[114] = new Rectangle(35, 0, 690, 692);
      width[115] = 892;
      boundingBoxes[115] = new Rectangle(35, 0, 823, 705);
      width[116] = 892;
      boundingBoxes[116] = new Rectangle(35, -14, 823, 706);
      width[117] = 788;
      boundingBoxes[117] = new Rectangle(35, -14, 719, 719);
      width[118] = 784;
      boundingBoxes[118] = new Rectangle(35, -14, 714, 719);
      width[119] = 438;
      boundingBoxes[119] = new Rectangle(35, -14, 368, 719);
      width[120] = 138;
      boundingBoxes[120] = new Rectangle(35, 0, 69, 692);
      width[121] = 277;
      boundingBoxes[121] = new Rectangle(35, 0, 207, 692);
      width[122] = 415;
      boundingBoxes[122] = new Rectangle(35, 0, 345, 692);
      width[123] = 392;
      boundingBoxes[123] = new Rectangle(35, 263, 322, 442);
      width[124] = 392;
      boundingBoxes[124] = new Rectangle(34, 263, 323, 442);
      width[125] = 668;
      boundingBoxes[125] = new Rectangle(35, 263, 598, 442);
      width[126] = 668;
      boundingBoxes[126] = new Rectangle(36, 263, 598, 442);
      width[161] = 732;
      boundingBoxes[161] = new Rectangle(35, -143, 662, 949);
      width[162] = 544;
      boundingBoxes[162] = new Rectangle(56, -14, 432, 720);
      width[163] = 544;
      boundingBoxes[163] = new Rectangle(34, -14, 474, 719);
      width[164] = 910;
      boundingBoxes[164] = new Rectangle(35, 40, 840, 611);
      width[165] = 667;
      boundingBoxes[165] = new Rectangle(35, -14, 598, 719);
      width[166] = 760;
      boundingBoxes[166] = new Rectangle(35, -14, 691, 719);
      width[167] = 760;
      boundingBoxes[167] = new Rectangle(0, 121, 758, 448);
      width[168] = 776;
      boundingBoxes[168] = new Rectangle(35, 0, 706, 705);
      width[169] = 595;
      boundingBoxes[169] = new Rectangle(34, -14, 526, 719);
      width[170] = 694;
      boundingBoxes[170] = new Rectangle(35, -14, 624, 719);
      width[171] = 626;
      boundingBoxes[171] = new Rectangle(34, 0, 557, 705);
      width[172] = 788;
      boundingBoxes[172] = new Rectangle(35, -14, 719, 719);
      width[173] = 788;
      boundingBoxes[173] = new Rectangle(35, -14, 719, 719);
      width[174] = 788;
      boundingBoxes[174] = new Rectangle(35, -14, 719, 719);
      width[175] = 788;
      boundingBoxes[175] = new Rectangle(35, -14, 719, 719);
      width[176] = 788;
      boundingBoxes[176] = new Rectangle(35, -14, 719, 719);
      width[177] = 788;
      boundingBoxes[177] = new Rectangle(35, -14, 719, 719);
      width[178] = 788;
      boundingBoxes[178] = new Rectangle(35, -14, 719, 719);
      width[179] = 788;
      boundingBoxes[179] = new Rectangle(35, -14, 719, 719);
      width[180] = 788;
      boundingBoxes[180] = new Rectangle(35, -14, 719, 719);
      width[181] = 788;
      boundingBoxes[181] = new Rectangle(35, -14, 719, 719);
      width[182] = 788;
      boundingBoxes[182] = new Rectangle(35, -14, 719, 719);
      width[183] = 788;
      boundingBoxes[183] = new Rectangle(35, -14, 719, 719);
      width[184] = 788;
      boundingBoxes[184] = new Rectangle(35, -14, 719, 719);
      width[185] = 788;
      boundingBoxes[185] = new Rectangle(35, -14, 719, 719);
      width[186] = 788;
      boundingBoxes[186] = new Rectangle(35, -14, 719, 719);
      width[187] = 788;
      boundingBoxes[187] = new Rectangle(35, -14, 719, 719);
      width[188] = 788;
      boundingBoxes[188] = new Rectangle(35, -14, 719, 719);
      width[189] = 788;
      boundingBoxes[189] = new Rectangle(35, -14, 719, 719);
      width[190] = 788;
      boundingBoxes[190] = new Rectangle(35, -14, 719, 719);
      width[191] = 788;
      boundingBoxes[191] = new Rectangle(35, -14, 719, 719);
      width[192] = 788;
      boundingBoxes[192] = new Rectangle(35, -14, 719, 719);
      width[193] = 788;
      boundingBoxes[193] = new Rectangle(35, -14, 719, 719);
      width[194] = 788;
      boundingBoxes[194] = new Rectangle(35, -14, 719, 719);
      width[195] = 788;
      boundingBoxes[195] = new Rectangle(35, -14, 719, 719);
      width[196] = 788;
      boundingBoxes[196] = new Rectangle(35, -14, 719, 719);
      width[197] = 788;
      boundingBoxes[197] = new Rectangle(35, -14, 719, 719);
      width[198] = 788;
      boundingBoxes[198] = new Rectangle(35, -14, 719, 719);
      width[199] = 788;
      boundingBoxes[199] = new Rectangle(35, -14, 719, 719);
      width[200] = 788;
      boundingBoxes[200] = new Rectangle(35, -14, 719, 719);
      width[201] = 788;
      boundingBoxes[201] = new Rectangle(35, -14, 719, 719);
      width[202] = 788;
      boundingBoxes[202] = new Rectangle(35, -14, 719, 719);
      width[203] = 788;
      boundingBoxes[203] = new Rectangle(35, -14, 719, 719);
      width[204] = 788;
      boundingBoxes[204] = new Rectangle(35, -14, 719, 719);
      width[205] = 788;
      boundingBoxes[205] = new Rectangle(35, -14, 719, 719);
      width[206] = 788;
      boundingBoxes[206] = new Rectangle(35, -14, 719, 719);
      width[207] = 788;
      boundingBoxes[207] = new Rectangle(35, -14, 719, 719);
      width[208] = 788;
      boundingBoxes[208] = new Rectangle(35, -14, 719, 719);
      width[209] = 788;
      boundingBoxes[209] = new Rectangle(35, -14, 719, 719);
      width[210] = 788;
      boundingBoxes[210] = new Rectangle(35, -14, 719, 719);
      width[211] = 788;
      boundingBoxes[211] = new Rectangle(35, -14, 719, 719);
      width[212] = 894;
      boundingBoxes[212] = new Rectangle(35, 58, 825, 576);
      width[213] = 838;
      boundingBoxes[213] = new Rectangle(35, 152, 768, 388);
      width[214] = 1016;
      boundingBoxes[214] = new Rectangle(34, 152, 947, 388);
      width[215] = 458;
      boundingBoxes[215] = new Rectangle(35, -127, 387, 947);
      width[216] = 748;
      boundingBoxes[216] = new Rectangle(35, 94, 663, 503);
      width[217] = 924;
      boundingBoxes[217] = new Rectangle(35, 140, 855, 412);
      width[218] = 748;
      boundingBoxes[218] = new Rectangle(35, 94, 663, 503);
      width[219] = 918;
      boundingBoxes[219] = new Rectangle(35, 166, 849, 360);
      width[220] = 927;
      boundingBoxes[220] = new Rectangle(35, 32, 857, 628);
      width[221] = 928;
      boundingBoxes[221] = new Rectangle(35, 129, 856, 433);
      width[222] = 928;
      boundingBoxes[222] = new Rectangle(35, 128, 858, 435);
      width[223] = 834;
      boundingBoxes[223] = new Rectangle(35, 155, 764, 382);
      width[224] = 873;
      boundingBoxes[224] = new Rectangle(35, 93, 803, 506);
      width[225] = 828;
      boundingBoxes[225] = new Rectangle(35, 104, 756, 484);
      width[226] = 924;
      boundingBoxes[226] = new Rectangle(35, 98, 854, 496);
      width[227] = 924;
      boundingBoxes[227] = new Rectangle(35, 98, 854, 496);
      width[228] = 917;
      boundingBoxes[228] = new Rectangle(35, 0, 847, 692);
      width[229] = 930;
      boundingBoxes[229] = new Rectangle(35, 84, 861, 524);
      width[230] = 931;
      boundingBoxes[230] = new Rectangle(35, 84, 861, 524);
      width[231] = 463;
      boundingBoxes[231] = new Rectangle(35, -99, 394, 890);
      width[232] = 883;
      boundingBoxes[232] = new Rectangle(35, 71, 813, 552);
      width[233] = 836;
      boundingBoxes[233] = new Rectangle(35, 44, 767, 604);
      width[234] = 836;
      boundingBoxes[234] = new Rectangle(35, 44, 767, 604);
      width[235] = 867;
      boundingBoxes[235] = new Rectangle(35, 101, 797, 490);
      width[236] = 867;
      boundingBoxes[236] = new Rectangle(35, 101, 797, 490);
      width[237] = 696;
      boundingBoxes[237] = new Rectangle(35, 44, 626, 604);
      width[238] = 696;
      boundingBoxes[238] = new Rectangle(35, 44, 626, 604);
      width[239] = 874;
      boundingBoxes[239] = new Rectangle(35, 77, 805, 542);
      width[241] = 874;
      boundingBoxes[241] = new Rectangle(35, 73, 805, 542);
      width[242] = 760;
      boundingBoxes[242] = new Rectangle(35, 0, 690, 692);
      width[243] = 946;
      boundingBoxes[243] = new Rectangle(35, 160, 876, 373);
      width[244] = 771;
      boundingBoxes[244] = new Rectangle(34, 37, 702, 618);
      width[245] = 865;
      boundingBoxes[245] = new Rectangle(35, 207, 795, 274);
      width[246] = 771;
      boundingBoxes[246] = new Rectangle(34, 37, 702, 618);
      width[247] = 888;
      boundingBoxes[247] = new Rectangle(34, -19, 819, 731);
      width[248] = 967;
      boundingBoxes[248] = new Rectangle(35, 124, 897, 444);
      width[249] = 888;
      boundingBoxes[249] = new Rectangle(34, -19, 819, 731);
      width[250] = 831;
      boundingBoxes[250] = new Rectangle(35, 113, 761, 466);
      width[251] = 873;
      boundingBoxes[251] = new Rectangle(36, 118, 802, 460);
      width[252] = 927;
      boundingBoxes[252] = new Rectangle(35, 150, 856, 392);
      width[253] = 970;
      boundingBoxes[253] = new Rectangle(35, 76, 896, 540);
      width[254] = 918;
      boundingBoxes[254] = new Rectangle(34, 99, 850, 494);
      width[137] = 410;
      boundingBoxes[137] = new Rectangle(35, 0, 340, 692);
      width[135] = 509;
      boundingBoxes[135] = new Rectangle(35, 0, 440, 692);
      width[140] = 334;
      boundingBoxes[140] = new Rectangle(35, 0, 264, 692);
      width[134] = 509;
      boundingBoxes[134] = new Rectangle(35, 0, 440, 692);
      width[128] = 390;
      boundingBoxes[128] = new Rectangle(35, -14, 321, 719);
      width[138] = 234;
      boundingBoxes[138] = new Rectangle(35, -14, 164, 719);
      width[132] = 276;
      boundingBoxes[132] = new Rectangle(35, 0, 207, 692);
      width[129] = 390;
      boundingBoxes[129] = new Rectangle(35, -14, 320, 719);
      width[136] = 410;
      boundingBoxes[136] = new Rectangle(35, 0, 340, 692);
      width[131] = 317;
      boundingBoxes[131] = new Rectangle(35, 0, 248, 692);
      width[130] = 317;
      boundingBoxes[130] = new Rectangle(35, 0, 248, 692);
      width[133] = 276;
      boundingBoxes[133] = new Rectangle(35, 0, 207, 692);
      width[141] = 334;
      boundingBoxes[141] = new Rectangle(35, 0, 264, 692);
      width[139] = 234;
      boundingBoxes[139] = new Rectangle(35, -14, 164, 719);
      familyNames = new HashSet();
      familyNames.add("ZapfDingbats");
   }
}
