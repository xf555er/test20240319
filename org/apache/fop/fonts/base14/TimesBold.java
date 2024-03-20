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

public class TimesBold extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Times-Bold";
   private static final String fullName = "Times Bold";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 676;
   private static final int xHeight = 461;
   private static final int ascender = 676;
   private static final int descender = -205;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private static final Map kerning;
   private boolean enableKerning;

   public TimesBold() {
      this(false);
   }

   public TimesBold(boolean enableKerning) {
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
      return "Times-Bold";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Times Bold";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 676;
   }

   public int getCapHeight(int size) {
      return size * 676;
   }

   public int getDescender(int size) {
      return size * -205;
   }

   public int getXHeight(int size) {
      return size * 461;
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
         uri = new URI("base14:" + "Times-Bold".toLowerCase());
      } catch (URISyntaxException var4) {
         throw new RuntimeException(var4);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 722;
      boundingBoxes[65] = new Rectangle(9, 0, 680, 690);
      width[198] = 1000;
      boundingBoxes[198] = new Rectangle(4, 0, 947, 676);
      width[193] = 722;
      boundingBoxes[193] = new Rectangle(9, 0, 680, 923);
      width[194] = 722;
      boundingBoxes[194] = new Rectangle(9, 0, 680, 914);
      width[196] = 722;
      boundingBoxes[196] = new Rectangle(9, 0, 680, 877);
      width[192] = 722;
      boundingBoxes[192] = new Rectangle(9, 0, 680, 923);
      width[197] = 722;
      boundingBoxes[197] = new Rectangle(9, 0, 680, 935);
      width[195] = 722;
      boundingBoxes[195] = new Rectangle(9, 0, 680, 884);
      width[66] = 667;
      boundingBoxes[66] = new Rectangle(16, 0, 603, 676);
      width[67] = 722;
      boundingBoxes[67] = new Rectangle(49, -19, 638, 710);
      width[199] = 722;
      boundingBoxes[199] = new Rectangle(49, -218, 638, 909);
      width[68] = 722;
      boundingBoxes[68] = new Rectangle(14, 0, 676, 676);
      width[69] = 667;
      boundingBoxes[69] = new Rectangle(16, 0, 625, 676);
      width[201] = 667;
      boundingBoxes[201] = new Rectangle(16, 0, 625, 923);
      width[202] = 667;
      boundingBoxes[202] = new Rectangle(16, 0, 625, 914);
      width[203] = 667;
      boundingBoxes[203] = new Rectangle(16, 0, 625, 877);
      width[200] = 667;
      boundingBoxes[200] = new Rectangle(16, 0, 625, 923);
      width[208] = 722;
      boundingBoxes[208] = new Rectangle(6, 0, 684, 676);
      width[128] = 500;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 611;
      boundingBoxes[70] = new Rectangle(16, 0, 567, 676);
      width[71] = 778;
      boundingBoxes[71] = new Rectangle(37, -19, 718, 710);
      width[72] = 778;
      boundingBoxes[72] = new Rectangle(21, 0, 738, 676);
      width[73] = 389;
      boundingBoxes[73] = new Rectangle(20, 0, 350, 676);
      width[205] = 389;
      boundingBoxes[205] = new Rectangle(20, 0, 350, 923);
      width[206] = 389;
      boundingBoxes[206] = new Rectangle(20, 0, 350, 914);
      width[207] = 389;
      boundingBoxes[207] = new Rectangle(20, 0, 350, 877);
      width[204] = 389;
      boundingBoxes[204] = new Rectangle(20, 0, 350, 923);
      width[74] = 500;
      boundingBoxes[74] = new Rectangle(3, -96, 476, 772);
      width[75] = 778;
      boundingBoxes[75] = new Rectangle(30, 0, 739, 676);
      width[76] = 667;
      boundingBoxes[76] = new Rectangle(19, 0, 619, 676);
      width[77] = 944;
      boundingBoxes[77] = new Rectangle(14, 0, 907, 676);
      width[78] = 722;
      boundingBoxes[78] = new Rectangle(16, -18, 685, 694);
      width[209] = 722;
      boundingBoxes[209] = new Rectangle(16, -18, 685, 902);
      width[79] = 778;
      boundingBoxes[79] = new Rectangle(35, -19, 708, 710);
      width[140] = 1000;
      boundingBoxes[140] = new Rectangle(22, -5, 959, 689);
      width[211] = 778;
      boundingBoxes[211] = new Rectangle(35, -19, 708, 942);
      width[212] = 778;
      boundingBoxes[212] = new Rectangle(35, -19, 708, 933);
      width[214] = 778;
      boundingBoxes[214] = new Rectangle(35, -19, 708, 896);
      width[210] = 778;
      boundingBoxes[210] = new Rectangle(35, -19, 708, 942);
      width[216] = 778;
      boundingBoxes[216] = new Rectangle(35, -74, 708, 811);
      width[213] = 778;
      boundingBoxes[213] = new Rectangle(35, -19, 708, 903);
      width[80] = 611;
      boundingBoxes[80] = new Rectangle(16, 0, 584, 676);
      width[81] = 778;
      boundingBoxes[81] = new Rectangle(35, -176, 708, 867);
      width[82] = 722;
      boundingBoxes[82] = new Rectangle(26, 0, 689, 676);
      width[83] = 556;
      boundingBoxes[83] = new Rectangle(35, -19, 478, 711);
      width[138] = 556;
      boundingBoxes[138] = new Rectangle(35, -19, 478, 933);
      width[84] = 667;
      boundingBoxes[84] = new Rectangle(31, 0, 605, 676);
      width[222] = 611;
      boundingBoxes[222] = new Rectangle(16, 0, 584, 676);
      width[85] = 722;
      boundingBoxes[85] = new Rectangle(16, -19, 685, 695);
      width[218] = 722;
      boundingBoxes[218] = new Rectangle(16, -19, 685, 942);
      width[219] = 722;
      boundingBoxes[219] = new Rectangle(16, -19, 685, 933);
      width[220] = 722;
      boundingBoxes[220] = new Rectangle(16, -19, 685, 896);
      width[217] = 722;
      boundingBoxes[217] = new Rectangle(16, -19, 685, 942);
      width[86] = 722;
      boundingBoxes[86] = new Rectangle(16, -18, 685, 694);
      width[87] = 1000;
      boundingBoxes[87] = new Rectangle(19, -15, 962, 691);
      width[88] = 722;
      boundingBoxes[88] = new Rectangle(16, 0, 683, 676);
      width[89] = 722;
      boundingBoxes[89] = new Rectangle(15, 0, 684, 676);
      width[221] = 722;
      boundingBoxes[221] = new Rectangle(15, 0, 684, 923);
      width[159] = 722;
      boundingBoxes[159] = new Rectangle(15, 0, 684, 877);
      width[90] = 667;
      boundingBoxes[90] = new Rectangle(28, 0, 606, 676);
      width[142] = 667;
      boundingBoxes[142] = new Rectangle(28, 0, 606, 914);
      width[97] = 500;
      boundingBoxes[97] = new Rectangle(25, -14, 463, 487);
      width[225] = 500;
      boundingBoxes[225] = new Rectangle(25, -14, 463, 727);
      width[226] = 500;
      boundingBoxes[226] = new Rectangle(25, -14, 463, 718);
      width[180] = 333;
      boundingBoxes[180] = new Rectangle(86, 528, 238, 185);
      width[228] = 500;
      boundingBoxes[228] = new Rectangle(25, -14, 463, 681);
      width[230] = 722;
      boundingBoxes[230] = new Rectangle(33, -14, 660, 487);
      width[224] = 500;
      boundingBoxes[224] = new Rectangle(25, -14, 463, 727);
      width[38] = 833;
      boundingBoxes[38] = new Rectangle(62, -16, 725, 707);
      width[229] = 500;
      boundingBoxes[229] = new Rectangle(25, -14, 463, 754);
      width[94] = 581;
      boundingBoxes[94] = new Rectangle(73, 311, 436, 365);
      width[126] = 520;
      boundingBoxes[126] = new Rectangle(29, 173, 462, 160);
      width[42] = 500;
      boundingBoxes[42] = new Rectangle(56, 255, 391, 436);
      width[64] = 930;
      boundingBoxes[64] = new Rectangle(108, -19, 714, 710);
      width[227] = 500;
      boundingBoxes[227] = new Rectangle(25, -14, 463, 688);
      width[98] = 556;
      boundingBoxes[98] = new Rectangle(17, -14, 504, 690);
      width[92] = 278;
      boundingBoxes[92] = new Rectangle(-25, -19, 328, 710);
      width[124] = 220;
      boundingBoxes[124] = new Rectangle(66, -218, 88, 1000);
      width[123] = 394;
      boundingBoxes[123] = new Rectangle(22, -175, 318, 873);
      width[125] = 394;
      boundingBoxes[125] = new Rectangle(54, -175, 318, 873);
      width[91] = 333;
      boundingBoxes[91] = new Rectangle(67, -149, 234, 827);
      width[93] = 333;
      boundingBoxes[93] = new Rectangle(32, -149, 234, 827);
      width[166] = 220;
      boundingBoxes[166] = new Rectangle(66, -143, 88, 850);
      width[149] = 350;
      boundingBoxes[149] = new Rectangle(35, 198, 280, 280);
      width[99] = 444;
      boundingBoxes[99] = new Rectangle(25, -14, 405, 487);
      width[231] = 444;
      boundingBoxes[231] = new Rectangle(25, -218, 405, 691);
      width[184] = 333;
      boundingBoxes[184] = new Rectangle(68, -218, 226, 218);
      width[162] = 500;
      boundingBoxes[162] = new Rectangle(53, -140, 405, 728);
      width[136] = 333;
      boundingBoxes[136] = new Rectangle(-2, 528, 337, 176);
      width[58] = 333;
      boundingBoxes[58] = new Rectangle(82, -13, 169, 485);
      width[44] = 250;
      boundingBoxes[44] = new Rectangle(39, -180, 184, 335);
      width[169] = 747;
      boundingBoxes[169] = new Rectangle(26, -19, 695, 710);
      width[164] = 500;
      boundingBoxes[164] = new Rectangle(-26, 61, 552, 552);
      width[100] = 556;
      boundingBoxes[100] = new Rectangle(25, -14, 509, 690);
      width[134] = 500;
      boundingBoxes[134] = new Rectangle(47, -134, 406, 825);
      width[135] = 500;
      boundingBoxes[135] = new Rectangle(45, -132, 411, 823);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(57, 402, 286, 286);
      width[168] = 333;
      boundingBoxes[168] = new Rectangle(-2, 537, 337, 130);
      width[247] = 570;
      boundingBoxes[247] = new Rectangle(33, -31, 504, 568);
      width[36] = 500;
      boundingBoxes[36] = new Rectangle(29, -99, 443, 849);
      width[101] = 444;
      boundingBoxes[101] = new Rectangle(25, -14, 401, 487);
      width[233] = 444;
      boundingBoxes[233] = new Rectangle(25, -14, 401, 727);
      width[234] = 444;
      boundingBoxes[234] = new Rectangle(25, -14, 401, 718);
      width[235] = 444;
      boundingBoxes[235] = new Rectangle(25, -14, 401, 681);
      width[232] = 444;
      boundingBoxes[232] = new Rectangle(25, -14, 401, 727);
      width[56] = 500;
      boundingBoxes[56] = new Rectangle(28, -13, 444, 701);
      width[133] = 1000;
      boundingBoxes[133] = new Rectangle(82, -13, 835, 169);
      width[151] = 1000;
      boundingBoxes[151] = new Rectangle(0, 181, 1000, 90);
      width[150] = 500;
      boundingBoxes[150] = new Rectangle(0, 181, 500, 90);
      width[61] = 570;
      boundingBoxes[61] = new Rectangle(33, 107, 504, 292);
      width[240] = 500;
      boundingBoxes[240] = new Rectangle(25, -14, 451, 705);
      width[33] = 333;
      boundingBoxes[33] = new Rectangle(81, -13, 170, 704);
      width[161] = 333;
      boundingBoxes[161] = new Rectangle(82, -203, 170, 704);
      width[102] = 333;
      boundingBoxes[102] = new Rectangle(14, 0, 375, 691);
      width[53] = 500;
      boundingBoxes[53] = new Rectangle(22, -8, 448, 684);
      width[131] = 500;
      boundingBoxes[131] = new Rectangle(0, -155, 498, 861);
      width[52] = 500;
      boundingBoxes[52] = new Rectangle(19, 0, 456, 688);
      width[103] = 500;
      boundingBoxes[103] = new Rectangle(28, -206, 455, 679);
      width[223] = 556;
      boundingBoxes[223] = new Rectangle(19, -12, 498, 703);
      width[96] = 333;
      boundingBoxes[96] = new Rectangle(8, 528, 238, 185);
      width[62] = 570;
      boundingBoxes[62] = new Rectangle(31, -8, 508, 522);
      width[171] = 500;
      boundingBoxes[171] = new Rectangle(23, 36, 450, 379);
      width[187] = 500;
      boundingBoxes[187] = new Rectangle(27, 36, 450, 379);
      width[139] = 333;
      boundingBoxes[139] = new Rectangle(51, 36, 254, 379);
      width[155] = 333;
      boundingBoxes[155] = new Rectangle(28, 36, 254, 379);
      width[104] = 556;
      boundingBoxes[104] = new Rectangle(16, 0, 518, 676);
      width[45] = 333;
      boundingBoxes[45] = new Rectangle(44, 171, 243, 116);
      width[105] = 278;
      boundingBoxes[105] = new Rectangle(16, 0, 239, 691);
      width[237] = 278;
      boundingBoxes[237] = new Rectangle(16, 0, 273, 713);
      width[238] = 278;
      boundingBoxes[238] = new Rectangle(-37, 0, 337, 704);
      width[239] = 278;
      boundingBoxes[239] = new Rectangle(-37, 0, 337, 667);
      width[236] = 278;
      boundingBoxes[236] = new Rectangle(-27, 0, 282, 713);
      width[106] = 333;
      boundingBoxes[106] = new Rectangle(-57, -203, 320, 894);
      width[107] = 556;
      boundingBoxes[107] = new Rectangle(22, 0, 521, 676);
      width[108] = 278;
      boundingBoxes[108] = new Rectangle(16, 0, 239, 676);
      width[60] = 570;
      boundingBoxes[60] = new Rectangle(31, -8, 508, 522);
      width[172] = 570;
      boundingBoxes[172] = new Rectangle(33, 108, 504, 291);
      width[109] = 833;
      boundingBoxes[109] = new Rectangle(16, 0, 798, 473);
      width[175] = 333;
      boundingBoxes[175] = new Rectangle(1, 565, 330, 72);
      width[181] = 556;
      boundingBoxes[181] = new Rectangle(33, -206, 503, 667);
      width[215] = 570;
      boundingBoxes[215] = new Rectangle(48, 16, 474, 474);
      width[110] = 556;
      boundingBoxes[110] = new Rectangle(21, 0, 518, 473);
      width[57] = 500;
      boundingBoxes[57] = new Rectangle(26, -13, 447, 701);
      width[241] = 556;
      boundingBoxes[241] = new Rectangle(21, 0, 518, 674);
      width[35] = 500;
      boundingBoxes[35] = new Rectangle(4, 0, 492, 700);
      width[111] = 500;
      boundingBoxes[111] = new Rectangle(25, -14, 451, 487);
      width[243] = 500;
      boundingBoxes[243] = new Rectangle(25, -14, 451, 727);
      width[244] = 500;
      boundingBoxes[244] = new Rectangle(25, -14, 451, 718);
      width[246] = 500;
      boundingBoxes[246] = new Rectangle(25, -14, 451, 681);
      width[156] = 722;
      boundingBoxes[156] = new Rectangle(22, -14, 674, 487);
      width[242] = 500;
      boundingBoxes[242] = new Rectangle(25, -14, 451, 727);
      width[49] = 500;
      boundingBoxes[49] = new Rectangle(65, 0, 377, 688);
      width[189] = 750;
      boundingBoxes[189] = new Rectangle(-7, -12, 782, 700);
      width[188] = 750;
      boundingBoxes[188] = new Rectangle(28, -12, 715, 700);
      width[185] = 300;
      boundingBoxes[185] = new Rectangle(28, 275, 245, 413);
      width[170] = 300;
      boundingBoxes[170] = new Rectangle(-1, 397, 302, 291);
      width[186] = 330;
      boundingBoxes[186] = new Rectangle(18, 397, 294, 291);
      width[248] = 500;
      boundingBoxes[248] = new Rectangle(25, -92, 451, 641);
      width[245] = 500;
      boundingBoxes[245] = new Rectangle(25, -14, 451, 688);
      width[112] = 556;
      boundingBoxes[112] = new Rectangle(19, -205, 505, 678);
      width[182] = 540;
      boundingBoxes[182] = new Rectangle(0, -186, 519, 862);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(46, -168, 260, 862);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(27, -168, 260, 862);
      width[37] = 1000;
      boundingBoxes[37] = new Rectangle(124, -14, 753, 706);
      width[46] = 250;
      boundingBoxes[46] = new Rectangle(41, -13, 169, 169);
      width[183] = 250;
      boundingBoxes[183] = new Rectangle(41, 248, 169, 169);
      width[137] = 1000;
      boundingBoxes[137] = new Rectangle(7, -29, 988, 735);
      width[43] = 570;
      boundingBoxes[43] = new Rectangle(33, 0, 504, 506);
      width[177] = 570;
      boundingBoxes[177] = new Rectangle(33, 0, 504, 506);
      width[113] = 556;
      boundingBoxes[113] = new Rectangle(34, -205, 502, 678);
      width[63] = 500;
      boundingBoxes[63] = new Rectangle(57, -13, 388, 702);
      width[191] = 500;
      boundingBoxes[191] = new Rectangle(55, -201, 388, 702);
      width[34] = 555;
      boundingBoxes[34] = new Rectangle(83, 404, 389, 287);
      width[132] = 500;
      boundingBoxes[132] = new Rectangle(14, -180, 454, 335);
      width[147] = 500;
      boundingBoxes[147] = new Rectangle(32, 356, 454, 335);
      width[148] = 500;
      boundingBoxes[148] = new Rectangle(14, 356, 454, 335);
      width[145] = 333;
      boundingBoxes[145] = new Rectangle(70, 356, 184, 335);
      width[146] = 333;
      boundingBoxes[146] = new Rectangle(79, 356, 184, 335);
      width[130] = 333;
      boundingBoxes[130] = new Rectangle(79, -180, 184, 335);
      width[39] = 278;
      boundingBoxes[39] = new Rectangle(75, 404, 129, 287);
      width[114] = 444;
      boundingBoxes[114] = new Rectangle(29, 0, 405, 473);
      width[174] = 747;
      boundingBoxes[174] = new Rectangle(26, -19, 695, 710);
      width[115] = 389;
      boundingBoxes[115] = new Rectangle(25, -14, 336, 487);
      width[154] = 389;
      boundingBoxes[154] = new Rectangle(25, -14, 338, 718);
      width[167] = 500;
      boundingBoxes[167] = new Rectangle(57, -132, 386, 823);
      width[59] = 333;
      boundingBoxes[59] = new Rectangle(82, -180, 184, 652);
      width[55] = 500;
      boundingBoxes[55] = new Rectangle(17, 0, 460, 676);
      width[54] = 500;
      boundingBoxes[54] = new Rectangle(28, -13, 447, 701);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(-24, -19, 326, 710);
      width[32] = 250;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 500;
      boundingBoxes[163] = new Rectangle(21, -14, 456, 698);
      width[116] = 333;
      boundingBoxes[116] = new Rectangle(20, -12, 312, 642);
      width[254] = 556;
      boundingBoxes[254] = new Rectangle(19, -205, 505, 881);
      width[51] = 500;
      boundingBoxes[51] = new Rectangle(16, -14, 452, 702);
      width[190] = 750;
      boundingBoxes[190] = new Rectangle(23, -12, 710, 700);
      width[179] = 300;
      boundingBoxes[179] = new Rectangle(3, 268, 294, 420);
      width[152] = 333;
      boundingBoxes[152] = new Rectangle(-16, 547, 365, 127);
      width[153] = 1000;
      boundingBoxes[153] = new Rectangle(24, 271, 953, 405);
      width[50] = 500;
      boundingBoxes[50] = new Rectangle(17, 0, 461, 688);
      width[178] = 300;
      boundingBoxes[178] = new Rectangle(0, 275, 300, 413);
      width[117] = 556;
      boundingBoxes[117] = new Rectangle(16, -14, 521, 475);
      width[250] = 556;
      boundingBoxes[250] = new Rectangle(16, -14, 521, 727);
      width[251] = 556;
      boundingBoxes[251] = new Rectangle(16, -14, 521, 718);
      width[252] = 556;
      boundingBoxes[252] = new Rectangle(16, -14, 521, 681);
      width[249] = 556;
      boundingBoxes[249] = new Rectangle(16, -14, 521, 727);
      width[95] = 500;
      boundingBoxes[95] = new Rectangle(0, -125, 500, 50);
      width[118] = 500;
      boundingBoxes[118] = new Rectangle(21, -14, 464, 475);
      width[119] = 722;
      boundingBoxes[119] = new Rectangle(23, -14, 684, 475);
      width[120] = 500;
      boundingBoxes[120] = new Rectangle(12, 0, 472, 461);
      width[121] = 500;
      boundingBoxes[121] = new Rectangle(16, -205, 464, 666);
      width[253] = 500;
      boundingBoxes[253] = new Rectangle(16, -205, 464, 918);
      width[255] = 500;
      boundingBoxes[255] = new Rectangle(16, -205, 464, 872);
      width[165] = 500;
      boundingBoxes[165] = new Rectangle(-64, 0, 611, 676);
      width[122] = 444;
      boundingBoxes[122] = new Rectangle(21, 0, 399, 461);
      width[158] = 444;
      boundingBoxes[158] = new Rectangle(21, 0, 399, 704);
      width[48] = 500;
      boundingBoxes[48] = new Rectangle(24, -13, 452, 701);
      familyNames = new HashSet();
      familyNames.add("Times");
      kerning = new HashMap();
      Integer first = 79;
      Map pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      Integer second = 65;
      ((Map)pairs).put(second, -40);
      second = 87;
      ((Map)pairs).put(second, -50);
      second = 89;
      ((Map)pairs).put(second, -50);
      second = 84;
      ((Map)pairs).put(second, -40);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 86;
      ((Map)pairs).put(second, -50);
      second = 88;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 107;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -15);
      second = 101;
      ((Map)pairs).put(second, -10);
      first = 112;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, 0);
      first = 80;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -20);
      second = 97;
      ((Map)pairs).put(second, -10);
      second = 65;
      ((Map)pairs).put(second, -74);
      second = 46;
      ((Map)pairs).put(second, -110);
      second = 101;
      ((Map)pairs).put(second, -20);
      second = 44;
      ((Map)pairs).put(second, -92);
      first = 86;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -100);
      second = 79;
      ((Map)pairs).put(second, -45);
      second = 58;
      ((Map)pairs).put(second, -92);
      second = 71;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -129);
      second = 59;
      ((Map)pairs).put(second, -92);
      second = 45;
      ((Map)pairs).put(second, -74);
      second = 105;
      ((Map)pairs).put(second, -37);
      second = 65;
      ((Map)pairs).put(second, -135);
      second = 97;
      ((Map)pairs).put(second, -92);
      second = 117;
      ((Map)pairs).put(second, -92);
      second = 46;
      ((Map)pairs).put(second, -145);
      second = 101;
      ((Map)pairs).put(second, -100);
      first = 118;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -10);
      second = 97;
      ((Map)pairs).put(second, -10);
      second = 46;
      ((Map)pairs).put(second, -70);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -55);
      first = 32;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -55);
      second = 87;
      ((Map)pairs).put(second, -30);
      second = 147;
      ((Map)pairs).put(second, 0);
      second = 89;
      ((Map)pairs).put(second, -55);
      second = 84;
      ((Map)pairs).put(second, -30);
      second = 145;
      ((Map)pairs).put(second, 0);
      second = 86;
      ((Map)pairs).put(second, -45);
      first = 97;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, 0);
      second = 116;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 103;
      ((Map)pairs).put(second, 0);
      second = 98;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, -25);
      first = 70;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -25);
      second = 105;
      ((Map)pairs).put(second, 0);
      second = 114;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, -25);
      second = 65;
      ((Map)pairs).put(second, -90);
      second = 46;
      ((Map)pairs).put(second, -110);
      second = 101;
      ((Map)pairs).put(second, -25);
      second = 44;
      ((Map)pairs).put(second, -92);
      first = 85;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -60);
      second = 46;
      ((Map)pairs).put(second, -50);
      second = 44;
      ((Map)pairs).put(second, -50);
      first = 100;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 100;
      ((Map)pairs).put(second, 0);
      second = 119;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 83;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 46;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 122;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, 0);
      first = 68;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -35);
      second = 87;
      ((Map)pairs).put(second, -40);
      second = 89;
      ((Map)pairs).put(second, -40);
      second = 46;
      ((Map)pairs).put(second, -20);
      second = 86;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 146;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, 0);
      second = 100;
      ((Map)pairs).put(second, -20);
      second = 32;
      ((Map)pairs).put(second, -74);
      second = 146;
      ((Map)pairs).put(second, -63);
      second = 114;
      ((Map)pairs).put(second, -20);
      second = 116;
      ((Map)pairs).put(second, 0);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 115;
      ((Map)pairs).put(second, -37);
      second = 118;
      ((Map)pairs).put(second, -20);
      first = 58;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, 0);
      first = 119;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -10);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -70);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -55);
      first = 75;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -25);
      second = 79;
      ((Map)pairs).put(second, -30);
      second = 117;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -45);
      second = 101;
      ((Map)pairs).put(second, -25);
      first = 82;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -30);
      second = 87;
      ((Map)pairs).put(second, -35);
      second = 85;
      ((Map)pairs).put(second, -30);
      second = 89;
      ((Map)pairs).put(second, -35);
      second = 84;
      ((Map)pairs).put(second, -40);
      second = 86;
      ((Map)pairs).put(second, -55);
      first = 145;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -10);
      second = 145;
      ((Map)pairs).put(second, -63);
      first = 103;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, 0);
      second = 105;
      ((Map)pairs).put(second, 0);
      second = 114;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -15);
      second = 103;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 66;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -30);
      second = 85;
      ((Map)pairs).put(second, -10);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 98;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, -20);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -40);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 98;
      ((Map)pairs).put(second, -10);
      second = 118;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 81;
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
      ((Map)pairs).put(second, 0);
      first = 44;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -45);
      second = 32;
      ((Map)pairs).put(second, 0);
      second = 146;
      ((Map)pairs).put(second, -55);
      first = 102;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, 50);
      second = 111;
      ((Map)pairs).put(second, -25);
      second = 105;
      ((Map)pairs).put(second, -25);
      second = 146;
      ((Map)pairs).put(second, 55);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 102;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -15);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -15);
      first = 84;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -92);
      second = 79;
      ((Map)pairs).put(second, -18);
      second = 119;
      ((Map)pairs).put(second, -74);
      second = 58;
      ((Map)pairs).put(second, -74);
      second = 114;
      ((Map)pairs).put(second, -74);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -74);
      second = 59;
      ((Map)pairs).put(second, -74);
      second = 45;
      ((Map)pairs).put(second, -92);
      second = 105;
      ((Map)pairs).put(second, -18);
      second = 65;
      ((Map)pairs).put(second, -90);
      second = 97;
      ((Map)pairs).put(second, -92);
      second = 117;
      ((Map)pairs).put(second, -92);
      second = 121;
      ((Map)pairs).put(second, -74);
      second = 46;
      ((Map)pairs).put(second, -90);
      second = 101;
      ((Map)pairs).put(second, -92);
      first = 121;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -25);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -70);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -55);
      first = 120;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 101;
      ((Map)pairs).put(second, 0);
      first = 101;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 103;
      ((Map)pairs).put(second, 0);
      second = 98;
      ((Map)pairs).put(second, 0);
      second = 120;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 99;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 107;
      ((Map)pairs).put(second, 0);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 87;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -75);
      second = 79;
      ((Map)pairs).put(second, -10);
      second = 58;
      ((Map)pairs).put(second, -55);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -92);
      second = 59;
      ((Map)pairs).put(second, -55);
      second = 45;
      ((Map)pairs).put(second, -37);
      second = 105;
      ((Map)pairs).put(second, -18);
      second = 65;
      ((Map)pairs).put(second, -120);
      second = 97;
      ((Map)pairs).put(second, -65);
      second = 117;
      ((Map)pairs).put(second, -50);
      second = 121;
      ((Map)pairs).put(second, -60);
      second = 46;
      ((Map)pairs).put(second, -92);
      second = 101;
      ((Map)pairs).put(second, -65);
      first = 104;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, -15);
      first = 71;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 46;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 105;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 118;
      ((Map)pairs).put(second, -10);
      first = 65;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -45);
      second = 146;
      ((Map)pairs).put(second, -74);
      second = 119;
      ((Map)pairs).put(second, -90);
      second = 87;
      ((Map)pairs).put(second, -130);
      second = 67;
      ((Map)pairs).put(second, -55);
      second = 112;
      ((Map)pairs).put(second, -25);
      second = 81;
      ((Map)pairs).put(second, -45);
      second = 71;
      ((Map)pairs).put(second, -55);
      second = 86;
      ((Map)pairs).put(second, -145);
      second = 118;
      ((Map)pairs).put(second, -100);
      second = 148;
      ((Map)pairs).put(second, 0);
      second = 85;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -50);
      second = 89;
      ((Map)pairs).put(second, -100);
      second = 121;
      ((Map)pairs).put(second, -74);
      second = 84;
      ((Map)pairs).put(second, -95);
      first = 147;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -10);
      second = 145;
      ((Map)pairs).put(second, 0);
      first = 78;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 115;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, 0);
      first = 111;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -10);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 103;
      ((Map)pairs).put(second, 0);
      second = 120;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, -10);
      first = 114;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -18);
      second = 100;
      ((Map)pairs).put(second, 0);
      second = 107;
      ((Map)pairs).put(second, 0);
      second = 114;
      ((Map)pairs).put(second, 0);
      second = 99;
      ((Map)pairs).put(second, -18);
      second = 112;
      ((Map)pairs).put(second, -10);
      second = 103;
      ((Map)pairs).put(second, -10);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 113;
      ((Map)pairs).put(second, -18);
      second = 118;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -92);
      second = 45;
      ((Map)pairs).put(second, -37);
      second = 105;
      ((Map)pairs).put(second, 0);
      second = 109;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 117;
      ((Map)pairs).put(second, 0);
      second = 116;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -100);
      second = 110;
      ((Map)pairs).put(second, -15);
      second = 115;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, -18);
      first = 108;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      first = 76;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -20);
      second = 146;
      ((Map)pairs).put(second, -110);
      second = 87;
      ((Map)pairs).put(second, -92);
      second = 89;
      ((Map)pairs).put(second, -92);
      second = 121;
      ((Map)pairs).put(second, -55);
      second = 84;
      ((Map)pairs).put(second, -92);
      second = 86;
      ((Map)pairs).put(second, -92);
      first = 148;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, 0);
      first = 109;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      first = 89;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -111);
      second = 45;
      ((Map)pairs).put(second, -92);
      second = 105;
      ((Map)pairs).put(second, -37);
      second = 79;
      ((Map)pairs).put(second, -35);
      second = 58;
      ((Map)pairs).put(second, -92);
      second = 97;
      ((Map)pairs).put(second, -85);
      second = 65;
      ((Map)pairs).put(second, -110);
      second = 117;
      ((Map)pairs).put(second, -92);
      second = 46;
      ((Map)pairs).put(second, -92);
      second = 101;
      ((Map)pairs).put(second, -111);
      second = 59;
      ((Map)pairs).put(second, -92);
      second = 44;
      ((Map)pairs).put(second, -92);
      first = 74;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -15);
      second = 97;
      ((Map)pairs).put(second, -15);
      second = 65;
      ((Map)pairs).put(second, -30);
      second = 117;
      ((Map)pairs).put(second, -15);
      second = 46;
      ((Map)pairs).put(second, -20);
      second = 101;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 46;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -55);
      second = 146;
      ((Map)pairs).put(second, -55);
      first = 110;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, -40);
   }
}
