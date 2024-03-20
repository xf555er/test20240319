package org.apache.fop.fonts.base14;

import java.awt.Rectangle;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.fop.fonts.Base14Font;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.FontType;

public class HelveticaOblique extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Helvetica-Oblique";
   private static final String fullName = "Helvetica Oblique";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 718;
   private static final int xHeight = 523;
   private static final int ascender = 718;
   private static final int descender = -207;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private static final Map kerning;
   private boolean enableKerning;

   public HelveticaOblique() {
      this(false);
   }

   public HelveticaOblique(boolean enableKerning) {
      this.mapping = CodePointMapping.getMapping("WinAnsiEncoding");
      this.enableKerning = enableKerning;
   }

   public String getEncodingName() {
      return "WinAnsiEncoding";
   }

   public URI getFontURI() {
      return fontFileURI;
   }

   public String getFontName() {
      return "Helvetica-Oblique";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Helvetica Oblique";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 718;
   }

   public int getCapHeight(int size) {
      return size * 718;
   }

   public int getDescender(int size) {
      return size * -207;
   }

   public int getXHeight(int size) {
      return size * 523;
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
      return this.enableKerning;
   }

   public Map getKerningInfo() {
      return kerning;
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
         uri = new URI("base14:" + "Helvetica-Oblique".toLowerCase());
      } catch (URISyntaxException var4) {
         throw new RuntimeException(var4);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 667;
      boundingBoxes[65] = new Rectangle(14, 0, 640, 718);
      width[198] = 1000;
      boundingBoxes[198] = new Rectangle(8, 0, 1089, 718);
      width[193] = 667;
      boundingBoxes[193] = new Rectangle(14, 0, 669, 929);
      width[194] = 667;
      boundingBoxes[194] = new Rectangle(14, 0, 640, 929);
      width[196] = 667;
      boundingBoxes[196] = new Rectangle(14, 0, 640, 901);
      width[192] = 667;
      boundingBoxes[192] = new Rectangle(14, 0, 640, 929);
      width[197] = 667;
      boundingBoxes[197] = new Rectangle(14, 0, 640, 931);
      width[195] = 667;
      boundingBoxes[195] = new Rectangle(14, 0, 685, 917);
      width[66] = 667;
      boundingBoxes[66] = new Rectangle(74, 0, 638, 718);
      width[67] = 722;
      boundingBoxes[67] = new Rectangle(108, -19, 674, 756);
      width[199] = 722;
      boundingBoxes[199] = new Rectangle(108, -225, 674, 962);
      width[68] = 722;
      boundingBoxes[68] = new Rectangle(81, 0, 683, 718);
      width[69] = 667;
      boundingBoxes[69] = new Rectangle(86, 0, 676, 718);
      width[201] = 667;
      boundingBoxes[201] = new Rectangle(86, 0, 676, 929);
      width[202] = 667;
      boundingBoxes[202] = new Rectangle(86, 0, 676, 929);
      width[203] = 667;
      boundingBoxes[203] = new Rectangle(86, 0, 676, 901);
      width[200] = 667;
      boundingBoxes[200] = new Rectangle(86, 0, 676, 929);
      width[208] = 722;
      boundingBoxes[208] = new Rectangle(69, 0, 695, 718);
      width[128] = 556;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 611;
      boundingBoxes[70] = new Rectangle(86, 0, 650, 718);
      width[71] = 778;
      boundingBoxes[71] = new Rectangle(111, -19, 688, 756);
      width[72] = 722;
      boundingBoxes[72] = new Rectangle(77, 0, 722, 718);
      width[73] = 278;
      boundingBoxes[73] = new Rectangle(91, 0, 250, 718);
      width[205] = 278;
      boundingBoxes[205] = new Rectangle(91, 0, 398, 929);
      width[206] = 278;
      boundingBoxes[206] = new Rectangle(91, 0, 361, 929);
      width[207] = 278;
      boundingBoxes[207] = new Rectangle(91, 0, 367, 901);
      width[204] = 278;
      boundingBoxes[204] = new Rectangle(91, 0, 260, 929);
      width[74] = 500;
      boundingBoxes[74] = new Rectangle(47, -19, 534, 737);
      width[75] = 667;
      boundingBoxes[75] = new Rectangle(76, 0, 732, 718);
      width[76] = 556;
      boundingBoxes[76] = new Rectangle(76, 0, 479, 718);
      width[77] = 833;
      boundingBoxes[77] = new Rectangle(73, 0, 841, 718);
      width[78] = 722;
      boundingBoxes[78] = new Rectangle(76, 0, 723, 718);
      width[209] = 722;
      boundingBoxes[209] = new Rectangle(76, 0, 723, 917);
      width[79] = 778;
      boundingBoxes[79] = new Rectangle(105, -19, 721, 756);
      width[140] = 1000;
      boundingBoxes[140] = new Rectangle(98, -19, 1018, 756);
      width[211] = 778;
      boundingBoxes[211] = new Rectangle(105, -19, 721, 948);
      width[212] = 778;
      boundingBoxes[212] = new Rectangle(105, -19, 721, 948);
      width[214] = 778;
      boundingBoxes[214] = new Rectangle(105, -19, 721, 920);
      width[210] = 778;
      boundingBoxes[210] = new Rectangle(105, -19, 721, 948);
      width[216] = 778;
      boundingBoxes[216] = new Rectangle(43, -19, 847, 756);
      width[213] = 778;
      boundingBoxes[213] = new Rectangle(105, -19, 721, 936);
      width[80] = 667;
      boundingBoxes[80] = new Rectangle(86, 0, 651, 718);
      width[81] = 778;
      boundingBoxes[81] = new Rectangle(105, -56, 721, 793);
      width[82] = 722;
      boundingBoxes[82] = new Rectangle(88, 0, 685, 718);
      width[83] = 667;
      boundingBoxes[83] = new Rectangle(90, -19, 623, 756);
      width[138] = 667;
      boundingBoxes[138] = new Rectangle(90, -19, 623, 948);
      width[84] = 611;
      boundingBoxes[84] = new Rectangle(148, 0, 602, 718);
      width[222] = 667;
      boundingBoxes[222] = new Rectangle(86, 0, 626, 718);
      width[85] = 722;
      boundingBoxes[85] = new Rectangle(123, -19, 674, 737);
      width[218] = 722;
      boundingBoxes[218] = new Rectangle(123, -19, 674, 948);
      width[219] = 722;
      boundingBoxes[219] = new Rectangle(123, -19, 674, 948);
      width[220] = 722;
      boundingBoxes[220] = new Rectangle(123, -19, 674, 920);
      width[217] = 722;
      boundingBoxes[217] = new Rectangle(123, -19, 674, 948);
      width[86] = 667;
      boundingBoxes[86] = new Rectangle(173, 0, 627, 718);
      width[87] = 944;
      boundingBoxes[87] = new Rectangle(169, 0, 912, 718);
      width[88] = 667;
      boundingBoxes[88] = new Rectangle(19, 0, 771, 718);
      width[89] = 667;
      boundingBoxes[89] = new Rectangle(167, 0, 639, 718);
      width[221] = 667;
      boundingBoxes[221] = new Rectangle(167, 0, 639, 929);
      width[159] = 667;
      boundingBoxes[159] = new Rectangle(167, 0, 639, 901);
      width[90] = 611;
      boundingBoxes[90] = new Rectangle(23, 0, 718, 718);
      width[142] = 611;
      boundingBoxes[142] = new Rectangle(23, 0, 718, 929);
      width[97] = 556;
      boundingBoxes[97] = new Rectangle(61, -15, 498, 553);
      width[225] = 556;
      boundingBoxes[225] = new Rectangle(61, -15, 526, 749);
      width[226] = 556;
      boundingBoxes[226] = new Rectangle(61, -15, 498, 749);
      width[180] = 333;
      boundingBoxes[180] = new Rectangle(248, 593, 227, 141);
      width[228] = 556;
      boundingBoxes[228] = new Rectangle(61, -15, 498, 721);
      width[230] = 889;
      boundingBoxes[230] = new Rectangle(61, -15, 848, 553);
      width[224] = 556;
      boundingBoxes[224] = new Rectangle(61, -15, 498, 749);
      width[38] = 667;
      boundingBoxes[38] = new Rectangle(77, -15, 570, 733);
      width[229] = 556;
      boundingBoxes[229] = new Rectangle(61, -15, 498, 771);
      width[94] = 469;
      boundingBoxes[94] = new Rectangle(42, 264, 497, 424);
      width[126] = 584;
      boundingBoxes[126] = new Rectangle(111, 180, 469, 146);
      width[42] = 389;
      boundingBoxes[42] = new Rectangle(165, 431, 310, 287);
      width[64] = 1015;
      boundingBoxes[64] = new Rectangle(215, -19, 750, 756);
      width[227] = 556;
      boundingBoxes[227] = new Rectangle(61, -15, 531, 737);
      width[98] = 556;
      boundingBoxes[98] = new Rectangle(58, -15, 526, 733);
      width[92] = 278;
      boundingBoxes[92] = new Rectangle(140, -19, 151, 756);
      width[124] = 260;
      boundingBoxes[124] = new Rectangle(46, -225, 286, 1000);
      width[123] = 334;
      boundingBoxes[123] = new Rectangle(92, -196, 353, 918);
      width[125] = 334;
      boundingBoxes[125] = new Rectangle(0, -196, 354, 918);
      width[91] = 278;
      boundingBoxes[91] = new Rectangle(21, -196, 382, 918);
      width[93] = 278;
      boundingBoxes[93] = new Rectangle(-14, -196, 382, 918);
      width[166] = 260;
      boundingBoxes[166] = new Rectangle(62, -150, 254, 850);
      width[149] = 350;
      boundingBoxes[149] = new Rectangle(91, 202, 322, 315);
      width[99] = 500;
      boundingBoxes[99] = new Rectangle(74, -15, 479, 553);
      width[231] = 500;
      boundingBoxes[231] = new Rectangle(74, -225, 479, 763);
      width[184] = 333;
      boundingBoxes[184] = new Rectangle(2, -225, 230, 225);
      width[162] = 556;
      boundingBoxes[162] = new Rectangle(95, -115, 489, 738);
      width[136] = 333;
      boundingBoxes[136] = new Rectangle(147, 593, 291, 141);
      width[58] = 278;
      boundingBoxes[58] = new Rectangle(87, 0, 214, 516);
      width[44] = 278;
      boundingBoxes[44] = new Rectangle(56, -147, 158, 253);
      width[169] = 737;
      boundingBoxes[169] = new Rectangle(54, -19, 783, 756);
      width[164] = 556;
      boundingBoxes[164] = new Rectangle(60, 99, 586, 504);
      width[100] = 556;
      boundingBoxes[100] = new Rectangle(84, -15, 568, 733);
      width[134] = 556;
      boundingBoxes[134] = new Rectangle(135, -159, 487, 877);
      width[135] = 556;
      boundingBoxes[135] = new Rectangle(52, -159, 571, 877);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(169, 411, 299, 292);
      width[168] = 333;
      boundingBoxes[168] = new Rectangle(168, 604, 275, 102);
      width[247] = 584;
      boundingBoxes[247] = new Rectangle(85, -19, 521, 543);
      width[36] = 556;
      boundingBoxes[36] = new Rectangle(69, -115, 548, 890);
      width[101] = 556;
      boundingBoxes[101] = new Rectangle(84, -15, 494, 553);
      width[233] = 556;
      boundingBoxes[233] = new Rectangle(84, -15, 503, 749);
      width[234] = 556;
      boundingBoxes[234] = new Rectangle(84, -15, 494, 749);
      width[235] = 556;
      boundingBoxes[235] = new Rectangle(84, -15, 494, 721);
      width[232] = 556;
      boundingBoxes[232] = new Rectangle(84, -15, 494, 749);
      width[56] = 556;
      boundingBoxes[56] = new Rectangle(74, -19, 533, 722);
      width[133] = 1000;
      boundingBoxes[133] = new Rectangle(115, 0, 793, 106);
      width[151] = 1000;
      boundingBoxes[151] = new Rectangle(51, 240, 1016, 73);
      width[150] = 556;
      boundingBoxes[150] = new Rectangle(51, 240, 572, 73);
      width[61] = 584;
      boundingBoxes[61] = new Rectangle(63, 115, 565, 275);
      width[240] = 556;
      boundingBoxes[240] = new Rectangle(81, -15, 536, 752);
      width[33] = 278;
      boundingBoxes[33] = new Rectangle(90, 0, 250, 718);
      width[161] = 333;
      boundingBoxes[161] = new Rectangle(77, -195, 249, 718);
      width[102] = 278;
      boundingBoxes[102] = new Rectangle(86, 0, 330, 728);
      width[53] = 556;
      boundingBoxes[53] = new Rectangle(68, -19, 553, 707);
      width[131] = 556;
      boundingBoxes[131] = new Rectangle(-52, -207, 706, 944);
      width[52] = 556;
      boundingBoxes[52] = new Rectangle(61, 0, 515, 703);
      width[103] = 556;
      boundingBoxes[103] = new Rectangle(42, -220, 568, 758);
      width[223] = 611;
      boundingBoxes[223] = new Rectangle(67, -15, 591, 743);
      width[96] = 333;
      boundingBoxes[96] = new Rectangle(170, 593, 167, 141);
      width[62] = 584;
      boundingBoxes[62] = new Rectangle(50, 11, 547, 484);
      width[171] = 556;
      boundingBoxes[171] = new Rectangle(146, 108, 408, 338);
      width[187] = 556;
      boundingBoxes[187] = new Rectangle(120, 108, 408, 338);
      width[139] = 333;
      boundingBoxes[139] = new Rectangle(137, 108, 203, 338);
      width[155] = 333;
      boundingBoxes[155] = new Rectangle(111, 108, 203, 338);
      width[104] = 556;
      boundingBoxes[104] = new Rectangle(65, 0, 508, 718);
      width[45] = 333;
      boundingBoxes[45] = new Rectangle(93, 232, 264, 90);
      width[105] = 222;
      boundingBoxes[105] = new Rectangle(67, 0, 241, 718);
      width[237] = 278;
      boundingBoxes[237] = new Rectangle(95, 0, 353, 734);
      width[238] = 278;
      boundingBoxes[238] = new Rectangle(95, 0, 316, 734);
      width[239] = 278;
      boundingBoxes[239] = new Rectangle(95, 0, 321, 706);
      width[236] = 278;
      boundingBoxes[236] = new Rectangle(95, 0, 215, 734);
      width[106] = 222;
      boundingBoxes[106] = new Rectangle(-60, -210, 368, 928);
      width[107] = 500;
      boundingBoxes[107] = new Rectangle(67, 0, 533, 718);
      width[108] = 222;
      boundingBoxes[108] = new Rectangle(67, 0, 241, 718);
      width[60] = 584;
      boundingBoxes[60] = new Rectangle(94, 11, 547, 484);
      width[172] = 584;
      boundingBoxes[172] = new Rectangle(106, 108, 522, 282);
      width[109] = 833;
      boundingBoxes[109] = new Rectangle(65, 0, 787, 538);
      width[175] = 333;
      boundingBoxes[175] = new Rectangle(143, 627, 325, 57);
      width[181] = 556;
      boundingBoxes[181] = new Rectangle(24, -207, 576, 730);
      width[215] = 584;
      boundingBoxes[215] = new Rectangle(50, 0, 592, 506);
      width[110] = 556;
      boundingBoxes[110] = new Rectangle(65, 0, 508, 538);
      width[57] = 556;
      boundingBoxes[57] = new Rectangle(82, -19, 527, 722);
      width[241] = 556;
      boundingBoxes[241] = new Rectangle(65, 0, 527, 722);
      width[35] = 556;
      boundingBoxes[35] = new Rectangle(73, 0, 558, 688);
      width[111] = 556;
      boundingBoxes[111] = new Rectangle(83, -14, 502, 552);
      width[243] = 556;
      boundingBoxes[243] = new Rectangle(83, -14, 504, 748);
      width[244] = 556;
      boundingBoxes[244] = new Rectangle(83, -14, 502, 748);
      width[246] = 556;
      boundingBoxes[246] = new Rectangle(83, -14, 502, 720);
      width[156] = 944;
      boundingBoxes[156] = new Rectangle(83, -15, 881, 553);
      width[242] = 556;
      boundingBoxes[242] = new Rectangle(83, -14, 502, 748);
      width[49] = 556;
      boundingBoxes[49] = new Rectangle(207, 0, 301, 703);
      width[189] = 834;
      boundingBoxes[189] = new Rectangle(114, -19, 725, 722);
      width[188] = 834;
      boundingBoxes[188] = new Rectangle(150, -19, 652, 722);
      width[185] = 333;
      boundingBoxes[185] = new Rectangle(166, 281, 205, 422);
      width[170] = 370;
      boundingBoxes[170] = new Rectangle(127, 405, 322, 332);
      width[186] = 365;
      boundingBoxes[186] = new Rectangle(141, 405, 327, 332);
      width[248] = 611;
      boundingBoxes[248] = new Rectangle(29, -22, 618, 567);
      width[245] = 556;
      boundingBoxes[245] = new Rectangle(83, -14, 519, 736);
      width[112] = 556;
      boundingBoxes[112] = new Rectangle(14, -207, 570, 745);
      width[182] = 537;
      boundingBoxes[182] = new Rectangle(126, -173, 524, 891);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(108, -207, 346, 940);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(-9, -207, 346, 940);
      width[37] = 889;
      boundingBoxes[37] = new Rectangle(147, -19, 742, 722);
      width[46] = 278;
      boundingBoxes[46] = new Rectangle(87, 0, 127, 106);
      width[183] = 278;
      boundingBoxes[183] = new Rectangle(129, 190, 128, 125);
      width[137] = 1000;
      boundingBoxes[137] = new Rectangle(88, -19, 941, 722);
      width[43] = 584;
      boundingBoxes[43] = new Rectangle(85, 0, 521, 505);
      width[177] = 584;
      boundingBoxes[177] = new Rectangle(39, 0, 579, 506);
      width[113] = 556;
      boundingBoxes[113] = new Rectangle(84, -207, 521, 745);
      width[63] = 556;
      boundingBoxes[63] = new Rectangle(161, 0, 449, 727);
      width[191] = 611;
      boundingBoxes[191] = new Rectangle(85, -201, 449, 726);
      width[34] = 355;
      boundingBoxes[34] = new Rectangle(168, 463, 270, 255);
      width[132] = 333;
      boundingBoxes[132] = new Rectangle(-6, -149, 324, 255);
      width[147] = 333;
      boundingBoxes[147] = new Rectangle(138, 470, 323, 255);
      width[148] = 333;
      boundingBoxes[148] = new Rectangle(124, 463, 324, 255);
      width[145] = 222;
      boundingBoxes[145] = new Rectangle(165, 470, 158, 255);
      width[146] = 222;
      boundingBoxes[146] = new Rectangle(151, 463, 159, 255);
      width[130] = 222;
      boundingBoxes[130] = new Rectangle(21, -149, 159, 255);
      width[39] = 191;
      boundingBoxes[39] = new Rectangle(157, 463, 128, 255);
      width[114] = 333;
      boundingBoxes[114] = new Rectangle(77, 0, 369, 538);
      width[174] = 737;
      boundingBoxes[174] = new Rectangle(54, -19, 783, 756);
      width[115] = 500;
      boundingBoxes[115] = new Rectangle(63, -15, 466, 553);
      width[154] = 500;
      boundingBoxes[154] = new Rectangle(63, -15, 489, 749);
      width[167] = 556;
      boundingBoxes[167] = new Rectangle(76, -191, 508, 928);
      width[59] = 278;
      boundingBoxes[59] = new Rectangle(56, -147, 245, 663);
      width[55] = 556;
      boundingBoxes[55] = new Rectangle(137, 0, 532, 688);
      width[54] = 556;
      boundingBoxes[54] = new Rectangle(91, -19, 524, 722);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(-21, -19, 473, 756);
      width[32] = 278;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 556;
      boundingBoxes[163] = new Rectangle(49, -16, 585, 734);
      width[116] = 278;
      boundingBoxes[116] = new Rectangle(102, -7, 266, 676);
      width[254] = 556;
      boundingBoxes[254] = new Rectangle(14, -207, 570, 925);
      width[51] = 556;
      boundingBoxes[51] = new Rectangle(75, -19, 535, 722);
      width[190] = 834;
      boundingBoxes[190] = new Rectangle(130, -19, 731, 722);
      width[179] = 333;
      boundingBoxes[179] = new Rectangle(90, 270, 346, 433);
      width[152] = 333;
      boundingBoxes[152] = new Rectangle(125, 606, 365, 116);
      width[153] = 1000;
      boundingBoxes[153] = new Rectangle(186, 306, 870, 412);
      width[50] = 556;
      boundingBoxes[50] = new Rectangle(26, 0, 591, 703);
      width[178] = 333;
      boundingBoxes[178] = new Rectangle(64, 281, 385, 422);
      width[117] = 556;
      boundingBoxes[117] = new Rectangle(94, -15, 506, 538);
      width[250] = 556;
      boundingBoxes[250] = new Rectangle(94, -15, 506, 749);
      width[251] = 556;
      boundingBoxes[251] = new Rectangle(94, -15, 506, 749);
      width[252] = 556;
      boundingBoxes[252] = new Rectangle(94, -15, 506, 721);
      width[249] = 556;
      boundingBoxes[249] = new Rectangle(94, -15, 506, 749);
      width[95] = 556;
      boundingBoxes[95] = new Rectangle(-27, -125, 567, 50);
      width[118] = 500;
      boundingBoxes[118] = new Rectangle(119, 0, 484, 523);
      width[119] = 722;
      boundingBoxes[119] = new Rectangle(125, 0, 695, 523);
      width[120] = 500;
      boundingBoxes[120] = new Rectangle(11, 0, 583, 523);
      width[121] = 500;
      boundingBoxes[121] = new Rectangle(15, -214, 585, 737);
      width[253] = 500;
      boundingBoxes[253] = new Rectangle(15, -214, 585, 948);
      width[255] = 500;
      boundingBoxes[255] = new Rectangle(15, -214, 585, 920);
      width[165] = 556;
      boundingBoxes[165] = new Rectangle(81, 0, 618, 688);
      width[122] = 500;
      boundingBoxes[122] = new Rectangle(31, 0, 540, 523);
      width[158] = 500;
      boundingBoxes[158] = new Rectangle(31, 0, 540, 734);
      width[48] = 556;
      boundingBoxes[48] = new Rectangle(93, -19, 515, 722);
      familyNames = new HashSet();
      familyNames.add("Helvetica");
      kerning = new HashMap();
      Integer first = 107;
      Map pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      Integer second = 111;
      ((Map)pairs).put(second, -20);
      second = 101;
      ((Map)pairs).put(second, -20);
      first = 79;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -20);
      second = 87;
      ((Map)pairs).put(second, -30);
      second = 89;
      ((Map)pairs).put(second, -70);
      second = 84;
      ((Map)pairs).put(second, -40);
      second = 46;
      ((Map)pairs).put(second, -40);
      second = 86;
      ((Map)pairs).put(second, -50);
      second = 88;
      ((Map)pairs).put(second, -60);
      second = 44;
      ((Map)pairs).put(second, -40);
      first = 104;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, -30);
      first = 87;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -30);
      second = 45;
      ((Map)pairs).put(second, -40);
      second = 79;
      ((Map)pairs).put(second, -20);
      second = 97;
      ((Map)pairs).put(second, -40);
      second = 65;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -30);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -80);
      second = 101;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -80);
      first = 99;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 107;
      ((Map)pairs).put(second, -20);
      second = 44;
      ((Map)pairs).put(second, -15);
      first = 112;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, -30);
      second = 46;
      ((Map)pairs).put(second, -35);
      second = 44;
      ((Map)pairs).put(second, -35);
      first = 80;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -50);
      second = 97;
      ((Map)pairs).put(second, -40);
      second = 65;
      ((Map)pairs).put(second, -120);
      second = 46;
      ((Map)pairs).put(second, -180);
      second = 101;
      ((Map)pairs).put(second, -50);
      second = 44;
      ((Map)pairs).put(second, -180);
      first = 86;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -80);
      second = 45;
      ((Map)pairs).put(second, -80);
      second = 79;
      ((Map)pairs).put(second, -40);
      second = 58;
      ((Map)pairs).put(second, -40);
      second = 97;
      ((Map)pairs).put(second, -70);
      second = 65;
      ((Map)pairs).put(second, -80);
      second = 117;
      ((Map)pairs).put(second, -70);
      second = 46;
      ((Map)pairs).put(second, -125);
      second = 71;
      ((Map)pairs).put(second, -40);
      second = 101;
      ((Map)pairs).put(second, -80);
      second = 59;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, -125);
      first = 118;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -25);
      second = 97;
      ((Map)pairs).put(second, -25);
      second = 46;
      ((Map)pairs).put(second, -80);
      second = 101;
      ((Map)pairs).put(second, -25);
      second = 44;
      ((Map)pairs).put(second, -80);
      first = 59;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, -50);
      first = 32;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 87;
      ((Map)pairs).put(second, -40);
      second = 147;
      ((Map)pairs).put(second, -30);
      second = 89;
      ((Map)pairs).put(second, -90);
      second = 84;
      ((Map)pairs).put(second, -50);
      second = 145;
      ((Map)pairs).put(second, -60);
      second = 86;
      ((Map)pairs).put(second, -50);
      first = 97;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -20);
      second = 121;
      ((Map)pairs).put(second, -30);
      second = 118;
      ((Map)pairs).put(second, -20);
      first = 65;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -30);
      second = 119;
      ((Map)pairs).put(second, -40);
      second = 87;
      ((Map)pairs).put(second, -50);
      second = 67;
      ((Map)pairs).put(second, -30);
      second = 81;
      ((Map)pairs).put(second, -30);
      second = 71;
      ((Map)pairs).put(second, -30);
      second = 86;
      ((Map)pairs).put(second, -70);
      second = 118;
      ((Map)pairs).put(second, -40);
      second = 85;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -30);
      second = 89;
      ((Map)pairs).put(second, -100);
      second = 84;
      ((Map)pairs).put(second, -120);
      second = 121;
      ((Map)pairs).put(second, -40);
      first = 70;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -30);
      second = 114;
      ((Map)pairs).put(second, -45);
      second = 97;
      ((Map)pairs).put(second, -50);
      second = 65;
      ((Map)pairs).put(second, -80);
      second = 46;
      ((Map)pairs).put(second, -150);
      second = 101;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -150);
      first = 85;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -40);
      second = 46;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, -40);
      first = 115;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -30);
      second = 46;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, -15);
      first = 122;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -15);
      second = 101;
      ((Map)pairs).put(second, -15);
      first = 83;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 46;
      ((Map)pairs).put(second, -20);
      second = 44;
      ((Map)pairs).put(second, -20);
      first = 111;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -30);
      second = 46;
      ((Map)pairs).put(second, -40);
      second = 120;
      ((Map)pairs).put(second, -30);
      second = 118;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, -40);
      first = 68;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -40);
      second = 87;
      ((Map)pairs).put(second, -40);
      second = 89;
      ((Map)pairs).put(second, -90);
      second = 46;
      ((Map)pairs).put(second, -70);
      second = 86;
      ((Map)pairs).put(second, -70);
      second = 44;
      ((Map)pairs).put(second, -70);
      first = 146;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 100;
      ((Map)pairs).put(second, -50);
      second = 32;
      ((Map)pairs).put(second, -70);
      second = 146;
      ((Map)pairs).put(second, -57);
      second = 114;
      ((Map)pairs).put(second, -50);
      second = 115;
      ((Map)pairs).put(second, -50);
      first = 82;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -20);
      second = 87;
      ((Map)pairs).put(second, -30);
      second = 85;
      ((Map)pairs).put(second, -40);
      second = 89;
      ((Map)pairs).put(second, -50);
      second = 84;
      ((Map)pairs).put(second, -30);
      second = 86;
      ((Map)pairs).put(second, -50);
      first = 75;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -40);
      second = 79;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -30);
      second = 121;
      ((Map)pairs).put(second, -50);
      second = 101;
      ((Map)pairs).put(second, -40);
      first = 119;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -10);
      second = 97;
      ((Map)pairs).put(second, -15);
      second = 46;
      ((Map)pairs).put(second, -60);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -60);
      first = 58;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, -50);
      first = 114;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 107;
      ((Map)pairs).put(second, 15);
      second = 58;
      ((Map)pairs).put(second, 30);
      second = 112;
      ((Map)pairs).put(second, 30);
      second = 108;
      ((Map)pairs).put(second, 15);
      second = 118;
      ((Map)pairs).put(second, 30);
      second = 44;
      ((Map)pairs).put(second, -50);
      second = 59;
      ((Map)pairs).put(second, 30);
      second = 105;
      ((Map)pairs).put(second, 15);
      second = 109;
      ((Map)pairs).put(second, 25);
      second = 97;
      ((Map)pairs).put(second, -10);
      second = 117;
      ((Map)pairs).put(second, 15);
      second = 116;
      ((Map)pairs).put(second, 40);
      second = 121;
      ((Map)pairs).put(second, 30);
      second = 46;
      ((Map)pairs).put(second, -50);
      second = 110;
      ((Map)pairs).put(second, 25);
      first = 67;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 46;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -30);
      first = 145;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 145;
      ((Map)pairs).put(second, -57);
      first = 103;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 114;
      ((Map)pairs).put(second, -10);
      first = 66;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 85;
      ((Map)pairs).put(second, -10);
      second = 46;
      ((Map)pairs).put(second, -20);
      second = 44;
      ((Map)pairs).put(second, -20);
      first = 81;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 85;
      ((Map)pairs).put(second, -10);
      first = 76;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -140);
      second = 146;
      ((Map)pairs).put(second, -160);
      second = 87;
      ((Map)pairs).put(second, -70);
      second = 89;
      ((Map)pairs).put(second, -140);
      second = 121;
      ((Map)pairs).put(second, -30);
      second = 84;
      ((Map)pairs).put(second, -110);
      second = 86;
      ((Map)pairs).put(second, -110);
      first = 98;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, -20);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -40);
      second = 108;
      ((Map)pairs).put(second, -20);
      second = 98;
      ((Map)pairs).put(second, -10);
      second = 118;
      ((Map)pairs).put(second, -20);
      second = 44;
      ((Map)pairs).put(second, -40);
      first = 44;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -100);
      second = 146;
      ((Map)pairs).put(second, -100);
      first = 148;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, -40);
      first = 109;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, -10);
      second = 121;
      ((Map)pairs).put(second, -15);
      first = 248;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 107;
      ((Map)pairs).put(second, -55);
      second = 104;
      ((Map)pairs).put(second, -55);
      second = 99;
      ((Map)pairs).put(second, -55);
      second = 112;
      ((Map)pairs).put(second, -55);
      second = 113;
      ((Map)pairs).put(second, -55);
      second = 118;
      ((Map)pairs).put(second, -70);
      second = 105;
      ((Map)pairs).put(second, -55);
      second = 97;
      ((Map)pairs).put(second, -55);
      second = 117;
      ((Map)pairs).put(second, -55);
      second = 116;
      ((Map)pairs).put(second, -55);
      second = 106;
      ((Map)pairs).put(second, -55);
      second = 115;
      ((Map)pairs).put(second, -55);
      second = 122;
      ((Map)pairs).put(second, -55);
      second = 100;
      ((Map)pairs).put(second, -55);
      second = 111;
      ((Map)pairs).put(second, -55);
      second = 119;
      ((Map)pairs).put(second, -70);
      second = 114;
      ((Map)pairs).put(second, -55);
      second = 103;
      ((Map)pairs).put(second, -55);
      second = 108;
      ((Map)pairs).put(second, -55);
      second = 98;
      ((Map)pairs).put(second, -55);
      second = 44;
      ((Map)pairs).put(second, -95);
      second = 109;
      ((Map)pairs).put(second, -55);
      second = 102;
      ((Map)pairs).put(second, -55);
      second = 121;
      ((Map)pairs).put(second, -70);
      second = 46;
      ((Map)pairs).put(second, -95);
      second = 110;
      ((Map)pairs).put(second, -55);
      second = 120;
      ((Map)pairs).put(second, -85);
      second = 101;
      ((Map)pairs).put(second, -55);
      first = 102;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, 60);
      second = 111;
      ((Map)pairs).put(second, -30);
      second = 146;
      ((Map)pairs).put(second, 50);
      second = 97;
      ((Map)pairs).put(second, -30);
      second = 46;
      ((Map)pairs).put(second, -30);
      second = 101;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -30);
      first = 74;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 97;
      ((Map)pairs).put(second, -20);
      second = 65;
      ((Map)pairs).put(second, -20);
      second = 117;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -30);
      first = 89;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -140);
      second = 45;
      ((Map)pairs).put(second, -140);
      second = 105;
      ((Map)pairs).put(second, -20);
      second = 79;
      ((Map)pairs).put(second, -85);
      second = 58;
      ((Map)pairs).put(second, -60);
      second = 97;
      ((Map)pairs).put(second, -140);
      second = 65;
      ((Map)pairs).put(second, -110);
      second = 117;
      ((Map)pairs).put(second, -110);
      second = 46;
      ((Map)pairs).put(second, -140);
      second = 101;
      ((Map)pairs).put(second, -140);
      second = 59;
      ((Map)pairs).put(second, -60);
      second = 44;
      ((Map)pairs).put(second, -140);
      first = 121;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -20);
      second = 97;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -100);
      second = 101;
      ((Map)pairs).put(second, -20);
      second = 44;
      ((Map)pairs).put(second, -100);
      first = 84;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -120);
      second = 79;
      ((Map)pairs).put(second, -40);
      second = 58;
      ((Map)pairs).put(second, -20);
      second = 119;
      ((Map)pairs).put(second, -120);
      second = 114;
      ((Map)pairs).put(second, -120);
      second = 44;
      ((Map)pairs).put(second, -120);
      second = 59;
      ((Map)pairs).put(second, -20);
      second = 45;
      ((Map)pairs).put(second, -140);
      second = 65;
      ((Map)pairs).put(second, -120);
      second = 97;
      ((Map)pairs).put(second, -120);
      second = 117;
      ((Map)pairs).put(second, -120);
      second = 121;
      ((Map)pairs).put(second, -120);
      second = 46;
      ((Map)pairs).put(second, -120);
      second = 101;
      ((Map)pairs).put(second, -120);
      first = 46;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -100);
      second = 32;
      ((Map)pairs).put(second, -60);
      second = 146;
      ((Map)pairs).put(second, -100);
      first = 110;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, -10);
      second = 121;
      ((Map)pairs).put(second, -15);
      second = 118;
      ((Map)pairs).put(second, -20);
      first = 120;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 101;
      ((Map)pairs).put(second, -30);
      first = 101;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -20);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -15);
      second = 120;
      ((Map)pairs).put(second, -30);
      second = 118;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -15);
   }
}
