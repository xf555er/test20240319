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

public class TimesBoldItalic extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Times-BoldItalic";
   private static final String fullName = "Times Bold Italic";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 669;
   private static final int xHeight = 462;
   private static final int ascender = 699;
   private static final int descender = -205;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private static final Map kerning;
   private boolean enableKerning;

   public TimesBoldItalic() {
      this(false);
   }

   public TimesBoldItalic(boolean enableKerning) {
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
      return "Times-BoldItalic";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Times Bold Italic";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 699;
   }

   public int getCapHeight(int size) {
      return size * 669;
   }

   public int getDescender(int size) {
      return size * -205;
   }

   public int getXHeight(int size) {
      return size * 462;
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
         uri = new URI("base14:" + "Times-BoldItalic".toLowerCase());
      } catch (URISyntaxException var4) {
         throw new RuntimeException(var4);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 667;
      boundingBoxes[65] = new Rectangle(-67, 0, 660, 683);
      width[198] = 944;
      boundingBoxes[198] = new Rectangle(-64, 0, 982, 669);
      width[193] = 667;
      boundingBoxes[193] = new Rectangle(-67, 0, 660, 904);
      width[194] = 667;
      boundingBoxes[194] = new Rectangle(-67, 0, 660, 897);
      width[196] = 667;
      boundingBoxes[196] = new Rectangle(-67, 0, 660, 862);
      width[192] = 667;
      boundingBoxes[192] = new Rectangle(-67, 0, 660, 904);
      width[197] = 667;
      boundingBoxes[197] = new Rectangle(-67, 0, 660, 921);
      width[195] = 667;
      boundingBoxes[195] = new Rectangle(-67, 0, 660, 862);
      width[66] = 667;
      boundingBoxes[66] = new Rectangle(-24, 0, 648, 669);
      width[67] = 667;
      boundingBoxes[67] = new Rectangle(32, -18, 645, 703);
      width[199] = 667;
      boundingBoxes[199] = new Rectangle(32, -218, 645, 903);
      width[68] = 722;
      boundingBoxes[68] = new Rectangle(-46, 0, 731, 669);
      width[69] = 667;
      boundingBoxes[69] = new Rectangle(-27, 0, 680, 669);
      width[201] = 667;
      boundingBoxes[201] = new Rectangle(-27, 0, 680, 904);
      width[202] = 667;
      boundingBoxes[202] = new Rectangle(-27, 0, 680, 897);
      width[203] = 667;
      boundingBoxes[203] = new Rectangle(-27, 0, 680, 862);
      width[200] = 667;
      boundingBoxes[200] = new Rectangle(-27, 0, 680, 904);
      width[208] = 722;
      boundingBoxes[208] = new Rectangle(-31, 0, 731, 669);
      width[128] = 500;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 667;
      boundingBoxes[70] = new Rectangle(-13, 0, 673, 669);
      width[71] = 722;
      boundingBoxes[71] = new Rectangle(21, -18, 685, 703);
      width[72] = 778;
      boundingBoxes[72] = new Rectangle(-24, 0, 823, 669);
      width[73] = 389;
      boundingBoxes[73] = new Rectangle(-32, 0, 438, 669);
      width[205] = 389;
      boundingBoxes[205] = new Rectangle(-32, 0, 464, 904);
      width[206] = 389;
      boundingBoxes[206] = new Rectangle(-32, 0, 482, 897);
      width[207] = 389;
      boundingBoxes[207] = new Rectangle(-32, 0, 482, 862);
      width[204] = 389;
      boundingBoxes[204] = new Rectangle(-32, 0, 438, 904);
      width[74] = 500;
      boundingBoxes[74] = new Rectangle(-46, -99, 570, 768);
      width[75] = 667;
      boundingBoxes[75] = new Rectangle(-21, 0, 723, 669);
      width[76] = 611;
      boundingBoxes[76] = new Rectangle(-22, 0, 612, 669);
      width[77] = 889;
      boundingBoxes[77] = new Rectangle(-29, -12, 946, 681);
      width[78] = 722;
      boundingBoxes[78] = new Rectangle(-27, -15, 775, 684);
      width[209] = 722;
      boundingBoxes[209] = new Rectangle(-27, -15, 775, 877);
      width[79] = 722;
      boundingBoxes[79] = new Rectangle(27, -18, 664, 703);
      width[140] = 944;
      boundingBoxes[140] = new Rectangle(23, -8, 923, 685);
      width[211] = 722;
      boundingBoxes[211] = new Rectangle(27, -18, 664, 922);
      width[212] = 722;
      boundingBoxes[212] = new Rectangle(27, -18, 664, 915);
      width[214] = 722;
      boundingBoxes[214] = new Rectangle(27, -18, 664, 880);
      width[210] = 722;
      boundingBoxes[210] = new Rectangle(27, -18, 664, 922);
      width[216] = 722;
      boundingBoxes[216] = new Rectangle(27, -125, 664, 889);
      width[213] = 722;
      boundingBoxes[213] = new Rectangle(27, -18, 664, 880);
      width[80] = 611;
      boundingBoxes[80] = new Rectangle(-27, 0, 640, 669);
      width[81] = 722;
      boundingBoxes[81] = new Rectangle(27, -208, 664, 893);
      width[82] = 667;
      boundingBoxes[82] = new Rectangle(-29, 0, 652, 669);
      width[83] = 556;
      boundingBoxes[83] = new Rectangle(2, -18, 524, 703);
      width[138] = 556;
      boundingBoxes[138] = new Rectangle(2, -18, 551, 915);
      width[84] = 611;
      boundingBoxes[84] = new Rectangle(50, 0, 600, 669);
      width[222] = 611;
      boundingBoxes[222] = new Rectangle(-27, 0, 600, 669);
      width[85] = 722;
      boundingBoxes[85] = new Rectangle(67, -18, 677, 687);
      width[218] = 722;
      boundingBoxes[218] = new Rectangle(67, -18, 677, 922);
      width[219] = 722;
      boundingBoxes[219] = new Rectangle(67, -18, 677, 915);
      width[220] = 722;
      boundingBoxes[220] = new Rectangle(67, -18, 677, 880);
      width[217] = 722;
      boundingBoxes[217] = new Rectangle(67, -18, 677, 922);
      width[86] = 667;
      boundingBoxes[86] = new Rectangle(65, -18, 650, 687);
      width[87] = 889;
      boundingBoxes[87] = new Rectangle(65, -18, 875, 687);
      width[88] = 667;
      boundingBoxes[88] = new Rectangle(-24, 0, 718, 669);
      width[89] = 611;
      boundingBoxes[89] = new Rectangle(73, 0, 586, 669);
      width[221] = 611;
      boundingBoxes[221] = new Rectangle(73, 0, 586, 904);
      width[159] = 611;
      boundingBoxes[159] = new Rectangle(73, 0, 586, 862);
      width[90] = 611;
      boundingBoxes[90] = new Rectangle(-11, 0, 601, 669);
      width[142] = 611;
      boundingBoxes[142] = new Rectangle(-11, 0, 601, 897);
      width[97] = 500;
      boundingBoxes[97] = new Rectangle(-21, -14, 476, 476);
      width[225] = 500;
      boundingBoxes[225] = new Rectangle(-21, -14, 484, 711);
      width[226] = 500;
      boundingBoxes[226] = new Rectangle(-21, -14, 476, 704);
      width[180] = 333;
      boundingBoxes[180] = new Rectangle(139, 516, 240, 181);
      width[228] = 500;
      boundingBoxes[228] = new Rectangle(-21, -14, 497, 669);
      width[230] = 722;
      boundingBoxes[230] = new Rectangle(-5, -13, 678, 475);
      width[224] = 500;
      boundingBoxes[224] = new Rectangle(-21, -14, 476, 711);
      width[38] = 778;
      boundingBoxes[38] = new Rectangle(5, -19, 694, 701);
      width[229] = 500;
      boundingBoxes[229] = new Rectangle(-21, -14, 476, 743);
      width[94] = 570;
      boundingBoxes[94] = new Rectangle(67, 304, 436, 365);
      width[126] = 570;
      boundingBoxes[126] = new Rectangle(54, 173, 462, 160);
      width[42] = 500;
      boundingBoxes[42] = new Rectangle(65, 249, 391, 436);
      width[64] = 832;
      boundingBoxes[64] = new Rectangle(63, -18, 707, 703);
      width[227] = 500;
      boundingBoxes[227] = new Rectangle(-21, -14, 512, 669);
      width[98] = 500;
      boundingBoxes[98] = new Rectangle(-14, -13, 458, 712);
      width[92] = 278;
      boundingBoxes[92] = new Rectangle(-1, -18, 280, 703);
      width[124] = 220;
      boundingBoxes[124] = new Rectangle(66, -218, 88, 1000);
      width[123] = 348;
      boundingBoxes[123] = new Rectangle(5, -187, 431, 873);
      width[125] = 348;
      boundingBoxes[125] = new Rectangle(-129, -187, 431, 873);
      width[91] = 333;
      boundingBoxes[91] = new Rectangle(-37, -159, 399, 833);
      width[93] = 333;
      boundingBoxes[93] = new Rectangle(-56, -157, 399, 831);
      width[166] = 220;
      boundingBoxes[166] = new Rectangle(66, -143, 88, 850);
      width[149] = 350;
      boundingBoxes[149] = new Rectangle(0, 175, 350, 350);
      width[99] = 444;
      boundingBoxes[99] = new Rectangle(-5, -13, 397, 475);
      width[231] = 444;
      boundingBoxes[231] = new Rectangle(-5, -218, 397, 680);
      width[184] = 333;
      boundingBoxes[184] = new Rectangle(-80, -218, 236, 223);
      width[162] = 500;
      boundingBoxes[162] = new Rectangle(42, -143, 397, 719);
      width[136] = 333;
      boundingBoxes[136] = new Rectangle(40, 516, 327, 174);
      width[58] = 333;
      boundingBoxes[58] = new Rectangle(23, -13, 241, 472);
      width[44] = 250;
      boundingBoxes[44] = new Rectangle(-60, -182, 204, 316);
      width[169] = 747;
      boundingBoxes[169] = new Rectangle(30, -18, 688, 703);
      width[164] = 500;
      boundingBoxes[164] = new Rectangle(-26, 34, 552, 552);
      width[100] = 500;
      boundingBoxes[100] = new Rectangle(-21, -13, 538, 712);
      width[134] = 500;
      boundingBoxes[134] = new Rectangle(91, -145, 403, 830);
      width[135] = 500;
      boundingBoxes[135] = new Rectangle(10, -139, 483, 824);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(83, 397, 286, 286);
      width[168] = 333;
      boundingBoxes[168] = new Rectangle(55, 550, 347, 134);
      width[247] = 570;
      boundingBoxes[247] = new Rectangle(33, -29, 504, 564);
      width[36] = 500;
      boundingBoxes[36] = new Rectangle(-20, -100, 517, 833);
      width[101] = 444;
      boundingBoxes[101] = new Rectangle(5, -13, 393, 475);
      width[233] = 444;
      boundingBoxes[233] = new Rectangle(5, -13, 430, 710);
      width[234] = 444;
      boundingBoxes[234] = new Rectangle(5, -13, 418, 703);
      width[235] = 444;
      boundingBoxes[235] = new Rectangle(5, -13, 443, 668);
      width[232] = 444;
      boundingBoxes[232] = new Rectangle(5, -13, 393, 710);
      width[56] = 500;
      boundingBoxes[56] = new Rectangle(3, -13, 473, 696);
      width[133] = 1000;
      boundingBoxes[133] = new Rectangle(40, -13, 812, 148);
      width[151] = 1000;
      boundingBoxes[151] = new Rectangle(-40, 178, 1017, 91);
      width[150] = 500;
      boundingBoxes[150] = new Rectangle(-40, 178, 517, 91);
      width[61] = 570;
      boundingBoxes[61] = new Rectangle(33, 107, 504, 292);
      width[240] = 500;
      boundingBoxes[240] = new Rectangle(-3, -13, 457, 712);
      width[33] = 389;
      boundingBoxes[33] = new Rectangle(67, -13, 303, 697);
      width[161] = 389;
      boundingBoxes[161] = new Rectangle(19, -205, 303, 697);
      width[102] = 333;
      boundingBoxes[102] = new Rectangle(-169, -205, 615, 903);
      width[53] = 500;
      boundingBoxes[53] = new Rectangle(-11, -13, 498, 682);
      width[131] = 500;
      boundingBoxes[131] = new Rectangle(-87, -156, 624, 863);
      width[52] = 500;
      boundingBoxes[52] = new Rectangle(-15, 0, 518, 683);
      width[103] = 500;
      boundingBoxes[103] = new Rectangle(-52, -203, 530, 665);
      width[223] = 500;
      boundingBoxes[223] = new Rectangle(-200, -200, 673, 905);
      width[96] = 333;
      boundingBoxes[96] = new Rectangle(85, 516, 212, 181);
      width[62] = 570;
      boundingBoxes[62] = new Rectangle(31, -8, 508, 522);
      width[171] = 500;
      boundingBoxes[171] = new Rectangle(12, 32, 456, 383);
      width[187] = 500;
      boundingBoxes[187] = new Rectangle(12, 32, 456, 383);
      width[139] = 333;
      boundingBoxes[139] = new Rectangle(32, 32, 271, 383);
      width[155] = 333;
      boundingBoxes[155] = new Rectangle(10, 32, 271, 383);
      width[104] = 556;
      boundingBoxes[104] = new Rectangle(-13, -9, 511, 708);
      width[45] = 333;
      boundingBoxes[45] = new Rectangle(2, 166, 269, 116);
      width[105] = 278;
      boundingBoxes[105] = new Rectangle(2, -9, 261, 693);
      width[237] = 278;
      boundingBoxes[237] = new Rectangle(2, -9, 350, 706);
      width[238] = 278;
      boundingBoxes[238] = new Rectangle(-3, -9, 327, 699);
      width[239] = 278;
      boundingBoxes[239] = new Rectangle(2, -9, 362, 664);
      width[236] = 278;
      boundingBoxes[236] = new Rectangle(2, -9, 257, 706);
      width[106] = 278;
      boundingBoxes[106] = new Rectangle(-189, -207, 468, 891);
      width[107] = 500;
      boundingBoxes[107] = new Rectangle(-23, -8, 506, 707);
      width[108] = 278;
      boundingBoxes[108] = new Rectangle(2, -9, 288, 708);
      width[60] = 570;
      boundingBoxes[60] = new Rectangle(31, -8, 508, 522);
      width[172] = 606;
      boundingBoxes[172] = new Rectangle(51, 108, 504, 291);
      width[109] = 778;
      boundingBoxes[109] = new Rectangle(-14, -9, 736, 471);
      width[175] = 333;
      boundingBoxes[175] = new Rectangle(51, 553, 342, 70);
      width[181] = 576;
      boundingBoxes[181] = new Rectangle(-60, -207, 576, 656);
      width[215] = 570;
      boundingBoxes[215] = new Rectangle(48, 16, 474, 474);
      width[110] = 556;
      boundingBoxes[110] = new Rectangle(-6, -9, 499, 471);
      width[57] = 500;
      boundingBoxes[57] = new Rectangle(-12, -10, 487, 693);
      width[241] = 556;
      boundingBoxes[241] = new Rectangle(-6, -9, 510, 664);
      width[35] = 500;
      boundingBoxes[35] = new Rectangle(-33, 0, 566, 700);
      width[111] = 500;
      boundingBoxes[111] = new Rectangle(-3, -13, 444, 475);
      width[243] = 500;
      boundingBoxes[243] = new Rectangle(-3, -13, 466, 710);
      width[244] = 500;
      boundingBoxes[244] = new Rectangle(-3, -13, 454, 703);
      width[246] = 500;
      boundingBoxes[246] = new Rectangle(-3, -13, 474, 668);
      width[156] = 722;
      boundingBoxes[156] = new Rectangle(6, -13, 668, 475);
      width[242] = 500;
      boundingBoxes[242] = new Rectangle(-3, -13, 444, 710);
      width[49] = 500;
      boundingBoxes[49] = new Rectangle(5, 0, 414, 683);
      width[189] = 750;
      boundingBoxes[189] = new Rectangle(-9, -14, 732, 697);
      width[188] = 750;
      boundingBoxes[188] = new Rectangle(7, -14, 714, 697);
      width[185] = 300;
      boundingBoxes[185] = new Rectangle(30, 274, 271, 409);
      width[170] = 266;
      boundingBoxes[170] = new Rectangle(16, 399, 314, 286);
      width[186] = 300;
      boundingBoxes[186] = new Rectangle(56, 400, 291, 285);
      width[248] = 500;
      boundingBoxes[248] = new Rectangle(-3, -119, 444, 679);
      width[245] = 500;
      boundingBoxes[245] = new Rectangle(-3, -13, 494, 668);
      width[112] = 500;
      boundingBoxes[112] = new Rectangle(-120, -205, 566, 667);
      width[182] = 500;
      boundingBoxes[182] = new Rectangle(-57, -193, 619, 862);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(28, -179, 316, 864);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(-44, -179, 315, 864);
      width[37] = 833;
      boundingBoxes[37] = new Rectangle(39, -10, 754, 702);
      width[46] = 250;
      boundingBoxes[46] = new Rectangle(-9, -13, 148, 148);
      width[183] = 250;
      boundingBoxes[183] = new Rectangle(51, 257, 148, 148);
      width[137] = 1000;
      boundingBoxes[137] = new Rectangle(7, -29, 989, 735);
      width[43] = 570;
      boundingBoxes[43] = new Rectangle(33, 0, 504, 506);
      width[177] = 570;
      boundingBoxes[177] = new Rectangle(33, 0, 504, 506);
      width[113] = 500;
      boundingBoxes[113] = new Rectangle(1, -205, 470, 667);
      width[63] = 500;
      boundingBoxes[63] = new Rectangle(79, -13, 391, 697);
      width[191] = 500;
      boundingBoxes[191] = new Rectangle(30, -205, 391, 697);
      width[34] = 555;
      boundingBoxes[34] = new Rectangle(136, 398, 400, 287);
      width[132] = 500;
      boundingBoxes[132] = new Rectangle(-57, -182, 460, 316);
      width[147] = 500;
      boundingBoxes[147] = new Rectangle(53, 369, 460, 316);
      width[148] = 500;
      boundingBoxes[148] = new Rectangle(53, 369, 460, 316);
      width[145] = 333;
      boundingBoxes[145] = new Rectangle(128, 369, 204, 316);
      width[146] = 333;
      boundingBoxes[146] = new Rectangle(98, 369, 204, 316);
      width[130] = 333;
      boundingBoxes[130] = new Rectangle(-5, -182, 204, 316);
      width[39] = 278;
      boundingBoxes[39] = new Rectangle(128, 398, 140, 287);
      width[114] = 389;
      boundingBoxes[114] = new Rectangle(-21, 0, 410, 462);
      width[174] = 747;
      boundingBoxes[174] = new Rectangle(30, -18, 688, 703);
      width[115] = 389;
      boundingBoxes[115] = new Rectangle(-19, -13, 352, 475);
      width[154] = 389;
      boundingBoxes[154] = new Rectangle(-19, -13, 443, 703);
      width[167] = 500;
      boundingBoxes[167] = new Rectangle(36, -143, 423, 828);
      width[59] = 333;
      boundingBoxes[59] = new Rectangle(-25, -183, 289, 642);
      width[55] = 500;
      boundingBoxes[55] = new Rectangle(52, 0, 473, 669);
      width[54] = 500;
      boundingBoxes[54] = new Rectangle(23, -15, 486, 694);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(-64, -18, 406, 703);
      width[32] = 250;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 500;
      boundingBoxes[163] = new Rectangle(-32, -12, 542, 695);
      width[116] = 278;
      boundingBoxes[116] = new Rectangle(-11, -9, 292, 603);
      width[254] = 500;
      boundingBoxes[254] = new Rectangle(-120, -205, 566, 904);
      width[51] = 500;
      boundingBoxes[51] = new Rectangle(-15, -13, 465, 696);
      width[190] = 750;
      boundingBoxes[190] = new Rectangle(7, -14, 719, 697);
      width[179] = 300;
      boundingBoxes[179] = new Rectangle(17, 265, 304, 418);
      width[152] = 333;
      boundingBoxes[152] = new Rectangle(48, 536, 359, 119);
      width[153] = 1000;
      boundingBoxes[153] = new Rectangle(32, 263, 936, 406);
      width[50] = 500;
      boundingBoxes[50] = new Rectangle(-27, 0, 473, 683);
      width[178] = 300;
      boundingBoxes[178] = new Rectangle(2, 274, 311, 409);
      width[117] = 556;
      boundingBoxes[117] = new Rectangle(15, -9, 477, 471);
      width[250] = 556;
      boundingBoxes[250] = new Rectangle(15, -9, 477, 706);
      width[251] = 556;
      boundingBoxes[251] = new Rectangle(15, -9, 477, 699);
      width[252] = 556;
      boundingBoxes[252] = new Rectangle(15, -9, 484, 664);
      width[249] = 556;
      boundingBoxes[249] = new Rectangle(15, -9, 477, 706);
      width[95] = 500;
      boundingBoxes[95] = new Rectangle(0, -125, 500, 50);
      width[118] = 444;
      boundingBoxes[118] = new Rectangle(16, -13, 385, 475);
      width[119] = 667;
      boundingBoxes[119] = new Rectangle(16, -13, 598, 475);
      width[120] = 500;
      boundingBoxes[120] = new Rectangle(-46, -13, 515, 475);
      width[121] = 444;
      boundingBoxes[121] = new Rectangle(-94, -205, 486, 667);
      width[253] = 444;
      boundingBoxes[253] = new Rectangle(-94, -205, 529, 902);
      width[255] = 444;
      boundingBoxes[255] = new Rectangle(-94, -205, 537, 860);
      width[165] = 500;
      boundingBoxes[165] = new Rectangle(33, 0, 595, 669);
      width[122] = 389;
      boundingBoxes[122] = new Rectangle(-43, -78, 411, 527);
      width[158] = 389;
      boundingBoxes[158] = new Rectangle(-43, -78, 467, 768);
      width[48] = 500;
      boundingBoxes[48] = new Rectangle(17, -14, 460, 697);
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
      ((Map)pairs).put(second, -10);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, -30);
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
      ((Map)pairs).put(second, -55);
      second = 97;
      ((Map)pairs).put(second, -40);
      second = 65;
      ((Map)pairs).put(second, -85);
      second = 46;
      ((Map)pairs).put(second, -129);
      second = 101;
      ((Map)pairs).put(second, -50);
      second = 44;
      ((Map)pairs).put(second, -129);
      first = 86;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -111);
      second = 79;
      ((Map)pairs).put(second, -30);
      second = 58;
      ((Map)pairs).put(second, -74);
      second = 71;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -129);
      second = 59;
      ((Map)pairs).put(second, -74);
      second = 45;
      ((Map)pairs).put(second, -70);
      second = 105;
      ((Map)pairs).put(second, -55);
      second = 65;
      ((Map)pairs).put(second, -85);
      second = 97;
      ((Map)pairs).put(second, -111);
      second = 117;
      ((Map)pairs).put(second, -55);
      second = 46;
      ((Map)pairs).put(second, -129);
      second = 101;
      ((Map)pairs).put(second, -111);
      first = 118;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -15);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -37);
      second = 101;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, -37);
      first = 32;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -37);
      second = 87;
      ((Map)pairs).put(second, -70);
      second = 147;
      ((Map)pairs).put(second, 0);
      second = 89;
      ((Map)pairs).put(second, -70);
      second = 84;
      ((Map)pairs).put(second, 0);
      second = 145;
      ((Map)pairs).put(second, 0);
      second = 86;
      ((Map)pairs).put(second, -70);
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
      ((Map)pairs).put(second, 0);
      first = 70;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -70);
      second = 105;
      ((Map)pairs).put(second, -40);
      second = 114;
      ((Map)pairs).put(second, -50);
      second = 97;
      ((Map)pairs).put(second, -95);
      second = 65;
      ((Map)pairs).put(second, -100);
      second = 46;
      ((Map)pairs).put(second, -129);
      second = 101;
      ((Map)pairs).put(second, -100);
      second = 44;
      ((Map)pairs).put(second, -129);
      first = 85;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -45);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 100;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 100;
      ((Map)pairs).put(second, 0);
      second = 119;
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -25);
      second = 87;
      ((Map)pairs).put(second, -40);
      second = 89;
      ((Map)pairs).put(second, -50);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 86;
      ((Map)pairs).put(second, -50);
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
      ((Map)pairs).put(second, -15);
      second = 32;
      ((Map)pairs).put(second, -74);
      second = 146;
      ((Map)pairs).put(second, -74);
      second = 114;
      ((Map)pairs).put(second, -15);
      second = 116;
      ((Map)pairs).put(second, -37);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 115;
      ((Map)pairs).put(second, -74);
      second = 118;
      ((Map)pairs).put(second, -15);
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
      ((Map)pairs).put(second, -15);
      second = 97;
      ((Map)pairs).put(second, -10);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -37);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -37);
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
      ((Map)pairs).put(second, -20);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 101;
      ((Map)pairs).put(second, -25);
      first = 82;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -40);
      second = 87;
      ((Map)pairs).put(second, -18);
      second = 85;
      ((Map)pairs).put(second, -40);
      second = 89;
      ((Map)pairs).put(second, -18);
      second = 84;
      ((Map)pairs).put(second, -30);
      second = 86;
      ((Map)pairs).put(second, -18);
      first = 145;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, 0);
      second = 145;
      ((Map)pairs).put(second, -74);
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
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -25);
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
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 44;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -95);
      second = 32;
      ((Map)pairs).put(second, 0);
      second = 146;
      ((Map)pairs).put(second, -95);
      first = 102;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, 0);
      second = 111;
      ((Map)pairs).put(second, -10);
      second = 105;
      ((Map)pairs).put(second, 0);
      second = 146;
      ((Map)pairs).put(second, 55);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 102;
      ((Map)pairs).put(second, -18);
      second = 46;
      ((Map)pairs).put(second, -10);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -10);
      first = 84;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -95);
      second = 79;
      ((Map)pairs).put(second, -18);
      second = 119;
      ((Map)pairs).put(second, -37);
      second = 58;
      ((Map)pairs).put(second, -74);
      second = 114;
      ((Map)pairs).put(second, -37);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -92);
      second = 59;
      ((Map)pairs).put(second, -74);
      second = 45;
      ((Map)pairs).put(second, -92);
      second = 105;
      ((Map)pairs).put(second, -37);
      second = 65;
      ((Map)pairs).put(second, -55);
      second = 97;
      ((Map)pairs).put(second, -92);
      second = 117;
      ((Map)pairs).put(second, -37);
      second = 121;
      ((Map)pairs).put(second, -37);
      second = 46;
      ((Map)pairs).put(second, -92);
      second = 101;
      ((Map)pairs).put(second, -92);
      first = 121;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -37);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -37);
      first = 120;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 101;
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, -10);
      second = 120;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 99;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 107;
      ((Map)pairs).put(second, -10);
      second = 104;
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, -80);
      second = 79;
      ((Map)pairs).put(second, -15);
      second = 58;
      ((Map)pairs).put(second, -55);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -74);
      second = 59;
      ((Map)pairs).put(second, -55);
      second = 45;
      ((Map)pairs).put(second, -50);
      second = 105;
      ((Map)pairs).put(second, -37);
      second = 65;
      ((Map)pairs).put(second, -74);
      second = 97;
      ((Map)pairs).put(second, -85);
      second = 117;
      ((Map)pairs).put(second, -55);
      second = 121;
      ((Map)pairs).put(second, -55);
      second = 46;
      ((Map)pairs).put(second, -74);
      second = 101;
      ((Map)pairs).put(second, -90);
      first = 104;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, 0);
      first = 65;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -50);
      second = 146;
      ((Map)pairs).put(second, -74);
      second = 119;
      ((Map)pairs).put(second, -74);
      second = 87;
      ((Map)pairs).put(second, -100);
      second = 67;
      ((Map)pairs).put(second, -65);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 81;
      ((Map)pairs).put(second, -55);
      second = 71;
      ((Map)pairs).put(second, -60);
      second = 86;
      ((Map)pairs).put(second, -95);
      second = 118;
      ((Map)pairs).put(second, -74);
      second = 148;
      ((Map)pairs).put(second, 0);
      second = 85;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -30);
      second = 89;
      ((Map)pairs).put(second, -70);
      second = 121;
      ((Map)pairs).put(second, -74);
      second = 84;
      ((Map)pairs).put(second, -55);
      first = 147;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, 0);
      second = 145;
      ((Map)pairs).put(second, 0);
      first = 78;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -30);
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
      ((Map)pairs).put(second, -25);
      second = 121;
      ((Map)pairs).put(second, -10);
      second = 103;
      ((Map)pairs).put(second, 0);
      second = 120;
      ((Map)pairs).put(second, -10);
      second = 118;
      ((Map)pairs).put(second, -15);
      first = 114;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, 0);
      second = 100;
      ((Map)pairs).put(second, 0);
      second = 107;
      ((Map)pairs).put(second, 0);
      second = 114;
      ((Map)pairs).put(second, 0);
      second = 99;
      ((Map)pairs).put(second, 0);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 103;
      ((Map)pairs).put(second, 0);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 113;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -65);
      second = 45;
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -65);
      second = 110;
      ((Map)pairs).put(second, 0);
      second = 115;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, 0);
      second = 146;
      ((Map)pairs).put(second, -55);
      second = 87;
      ((Map)pairs).put(second, -37);
      second = 89;
      ((Map)pairs).put(second, -37);
      second = 121;
      ((Map)pairs).put(second, -37);
      second = 84;
      ((Map)pairs).put(second, -18);
      second = 86;
      ((Map)pairs).put(second, -37);
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
      ((Map)pairs).put(second, -55);
      second = 79;
      ((Map)pairs).put(second, -25);
      second = 58;
      ((Map)pairs).put(second, -92);
      second = 97;
      ((Map)pairs).put(second, -92);
      second = 65;
      ((Map)pairs).put(second, -74);
      second = 117;
      ((Map)pairs).put(second, -92);
      second = 46;
      ((Map)pairs).put(second, -74);
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
      ((Map)pairs).put(second, -40);
      second = 97;
      ((Map)pairs).put(second, -40);
      second = 65;
      ((Map)pairs).put(second, -25);
      second = 117;
      ((Map)pairs).put(second, -40);
      second = 46;
      ((Map)pairs).put(second, -10);
      second = 101;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, -10);
      first = 46;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -95);
      second = 146;
      ((Map)pairs).put(second, -95);
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
