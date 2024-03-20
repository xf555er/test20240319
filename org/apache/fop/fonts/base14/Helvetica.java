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

public class Helvetica extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Helvetica";
   private static final String fullName = "Helvetica";
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

   public Helvetica() {
      this(false);
   }

   public Helvetica(boolean enableKerning) {
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
      return "Helvetica";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Helvetica";
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
         uri = new URI("base14:" + "Helvetica".toLowerCase());
      } catch (URISyntaxException var4) {
         throw new RuntimeException(var4);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 667;
      boundingBoxes[65] = new Rectangle(14, 0, 640, 718);
      width[198] = 1000;
      boundingBoxes[198] = new Rectangle(8, 0, 943, 718);
      width[193] = 667;
      boundingBoxes[193] = new Rectangle(14, 0, 640, 929);
      width[194] = 667;
      boundingBoxes[194] = new Rectangle(14, 0, 640, 929);
      width[196] = 667;
      boundingBoxes[196] = new Rectangle(14, 0, 640, 901);
      width[192] = 667;
      boundingBoxes[192] = new Rectangle(14, 0, 640, 929);
      width[197] = 667;
      boundingBoxes[197] = new Rectangle(14, 0, 640, 931);
      width[195] = 667;
      boundingBoxes[195] = new Rectangle(14, 0, 640, 917);
      width[66] = 667;
      boundingBoxes[66] = new Rectangle(74, 0, 553, 718);
      width[67] = 722;
      boundingBoxes[67] = new Rectangle(44, -19, 637, 756);
      width[199] = 722;
      boundingBoxes[199] = new Rectangle(44, -225, 637, 962);
      width[68] = 722;
      boundingBoxes[68] = new Rectangle(81, 0, 593, 718);
      width[69] = 667;
      boundingBoxes[69] = new Rectangle(86, 0, 530, 718);
      width[201] = 667;
      boundingBoxes[201] = new Rectangle(86, 0, 530, 929);
      width[202] = 667;
      boundingBoxes[202] = new Rectangle(86, 0, 530, 929);
      width[203] = 667;
      boundingBoxes[203] = new Rectangle(86, 0, 530, 901);
      width[200] = 667;
      boundingBoxes[200] = new Rectangle(86, 0, 530, 929);
      width[208] = 722;
      boundingBoxes[208] = new Rectangle(0, 0, 674, 718);
      width[128] = 556;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 611;
      boundingBoxes[70] = new Rectangle(86, 0, 497, 718);
      width[71] = 778;
      boundingBoxes[71] = new Rectangle(48, -19, 656, 756);
      width[72] = 722;
      boundingBoxes[72] = new Rectangle(77, 0, 569, 718);
      width[73] = 278;
      boundingBoxes[73] = new Rectangle(91, 0, 97, 718);
      width[205] = 278;
      boundingBoxes[205] = new Rectangle(91, 0, 201, 929);
      width[206] = 278;
      boundingBoxes[206] = new Rectangle(-6, 0, 291, 929);
      width[207] = 278;
      boundingBoxes[207] = new Rectangle(13, 0, 253, 901);
      width[204] = 278;
      boundingBoxes[204] = new Rectangle(-13, 0, 201, 929);
      width[74] = 500;
      boundingBoxes[74] = new Rectangle(17, -19, 411, 737);
      width[75] = 667;
      boundingBoxes[75] = new Rectangle(76, 0, 587, 718);
      width[76] = 556;
      boundingBoxes[76] = new Rectangle(76, 0, 461, 718);
      width[77] = 833;
      boundingBoxes[77] = new Rectangle(73, 0, 688, 718);
      width[78] = 722;
      boundingBoxes[78] = new Rectangle(76, 0, 570, 718);
      width[209] = 722;
      boundingBoxes[209] = new Rectangle(76, 0, 570, 917);
      width[79] = 778;
      boundingBoxes[79] = new Rectangle(39, -19, 700, 756);
      width[140] = 1000;
      boundingBoxes[140] = new Rectangle(36, -19, 929, 756);
      width[211] = 778;
      boundingBoxes[211] = new Rectangle(39, -19, 700, 948);
      width[212] = 778;
      boundingBoxes[212] = new Rectangle(39, -19, 700, 948);
      width[214] = 778;
      boundingBoxes[214] = new Rectangle(39, -19, 700, 920);
      width[210] = 778;
      boundingBoxes[210] = new Rectangle(39, -19, 700, 948);
      width[216] = 778;
      boundingBoxes[216] = new Rectangle(39, -19, 701, 756);
      width[213] = 778;
      boundingBoxes[213] = new Rectangle(39, -19, 700, 936);
      width[80] = 667;
      boundingBoxes[80] = new Rectangle(86, 0, 536, 718);
      width[81] = 778;
      boundingBoxes[81] = new Rectangle(39, -56, 700, 793);
      width[82] = 722;
      boundingBoxes[82] = new Rectangle(88, 0, 596, 718);
      width[83] = 667;
      boundingBoxes[83] = new Rectangle(49, -19, 571, 756);
      width[138] = 667;
      boundingBoxes[138] = new Rectangle(49, -19, 571, 948);
      width[84] = 611;
      boundingBoxes[84] = new Rectangle(14, 0, 583, 718);
      width[222] = 667;
      boundingBoxes[222] = new Rectangle(86, 0, 536, 718);
      width[85] = 722;
      boundingBoxes[85] = new Rectangle(79, -19, 565, 737);
      width[218] = 722;
      boundingBoxes[218] = new Rectangle(79, -19, 565, 948);
      width[219] = 722;
      boundingBoxes[219] = new Rectangle(79, -19, 565, 948);
      width[220] = 722;
      boundingBoxes[220] = new Rectangle(79, -19, 565, 920);
      width[217] = 722;
      boundingBoxes[217] = new Rectangle(79, -19, 565, 948);
      width[86] = 667;
      boundingBoxes[86] = new Rectangle(20, 0, 627, 718);
      width[87] = 944;
      boundingBoxes[87] = new Rectangle(16, 0, 912, 718);
      width[88] = 667;
      boundingBoxes[88] = new Rectangle(19, 0, 629, 718);
      width[89] = 667;
      boundingBoxes[89] = new Rectangle(14, 0, 639, 718);
      width[221] = 667;
      boundingBoxes[221] = new Rectangle(14, 0, 639, 929);
      width[159] = 667;
      boundingBoxes[159] = new Rectangle(14, 0, 639, 901);
      width[90] = 611;
      boundingBoxes[90] = new Rectangle(23, 0, 565, 718);
      width[142] = 611;
      boundingBoxes[142] = new Rectangle(23, 0, 565, 929);
      width[97] = 556;
      boundingBoxes[97] = new Rectangle(36, -15, 494, 553);
      width[225] = 556;
      boundingBoxes[225] = new Rectangle(36, -15, 494, 749);
      width[226] = 556;
      boundingBoxes[226] = new Rectangle(36, -15, 494, 749);
      width[180] = 333;
      boundingBoxes[180] = new Rectangle(122, 593, 197, 141);
      width[228] = 556;
      boundingBoxes[228] = new Rectangle(36, -15, 494, 721);
      width[230] = 889;
      boundingBoxes[230] = new Rectangle(36, -15, 811, 553);
      width[224] = 556;
      boundingBoxes[224] = new Rectangle(36, -15, 494, 749);
      width[38] = 667;
      boundingBoxes[38] = new Rectangle(44, -15, 601, 733);
      width[229] = 556;
      boundingBoxes[229] = new Rectangle(36, -15, 494, 771);
      width[94] = 469;
      boundingBoxes[94] = new Rectangle(-14, 264, 497, 424);
      width[126] = 584;
      boundingBoxes[126] = new Rectangle(61, 180, 462, 146);
      width[42] = 389;
      boundingBoxes[42] = new Rectangle(39, 431, 310, 287);
      width[64] = 1015;
      boundingBoxes[64] = new Rectangle(147, -19, 721, 756);
      width[227] = 556;
      boundingBoxes[227] = new Rectangle(36, -15, 494, 737);
      width[98] = 556;
      boundingBoxes[98] = new Rectangle(58, -15, 459, 733);
      width[92] = 278;
      boundingBoxes[92] = new Rectangle(-17, -19, 312, 756);
      width[124] = 260;
      boundingBoxes[124] = new Rectangle(94, -225, 73, 1000);
      width[123] = 334;
      boundingBoxes[123] = new Rectangle(42, -196, 250, 918);
      width[125] = 334;
      boundingBoxes[125] = new Rectangle(42, -196, 250, 918);
      width[91] = 278;
      boundingBoxes[91] = new Rectangle(63, -196, 187, 918);
      width[93] = 278;
      boundingBoxes[93] = new Rectangle(28, -196, 187, 918);
      width[166] = 260;
      boundingBoxes[166] = new Rectangle(94, -150, 73, 850);
      width[149] = 350;
      boundingBoxes[149] = new Rectangle(18, 202, 315, 315);
      width[99] = 500;
      boundingBoxes[99] = new Rectangle(30, -15, 447, 553);
      width[231] = 500;
      boundingBoxes[231] = new Rectangle(30, -225, 447, 763);
      width[184] = 333;
      boundingBoxes[184] = new Rectangle(45, -225, 214, 225);
      width[162] = 556;
      boundingBoxes[162] = new Rectangle(51, -115, 462, 738);
      width[136] = 333;
      boundingBoxes[136] = new Rectangle(21, 593, 291, 141);
      width[58] = 278;
      boundingBoxes[58] = new Rectangle(87, 0, 104, 516);
      width[44] = 278;
      boundingBoxes[44] = new Rectangle(87, -147, 104, 253);
      width[169] = 737;
      boundingBoxes[169] = new Rectangle(-14, -19, 766, 756);
      width[164] = 556;
      boundingBoxes[164] = new Rectangle(28, 99, 500, 504);
      width[100] = 556;
      boundingBoxes[100] = new Rectangle(35, -15, 464, 733);
      width[134] = 556;
      boundingBoxes[134] = new Rectangle(43, -159, 471, 877);
      width[135] = 556;
      boundingBoxes[135] = new Rectangle(43, -159, 471, 877);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(54, 411, 292, 292);
      width[168] = 333;
      boundingBoxes[168] = new Rectangle(40, 604, 253, 102);
      width[247] = 584;
      boundingBoxes[247] = new Rectangle(39, -19, 506, 543);
      width[36] = 556;
      boundingBoxes[36] = new Rectangle(32, -115, 488, 890);
      width[101] = 556;
      boundingBoxes[101] = new Rectangle(40, -15, 476, 553);
      width[233] = 556;
      boundingBoxes[233] = new Rectangle(40, -15, 476, 749);
      width[234] = 556;
      boundingBoxes[234] = new Rectangle(40, -15, 476, 749);
      width[235] = 556;
      boundingBoxes[235] = new Rectangle(40, -15, 476, 721);
      width[232] = 556;
      boundingBoxes[232] = new Rectangle(40, -15, 476, 749);
      width[56] = 556;
      boundingBoxes[56] = new Rectangle(38, -19, 479, 722);
      width[133] = 1000;
      boundingBoxes[133] = new Rectangle(115, 0, 770, 106);
      width[151] = 1000;
      boundingBoxes[151] = new Rectangle(0, 240, 1000, 73);
      width[150] = 556;
      boundingBoxes[150] = new Rectangle(0, 240, 556, 73);
      width[61] = 584;
      boundingBoxes[61] = new Rectangle(39, 115, 506, 275);
      width[240] = 556;
      boundingBoxes[240] = new Rectangle(35, -15, 487, 752);
      width[33] = 278;
      boundingBoxes[33] = new Rectangle(90, 0, 97, 718);
      width[161] = 333;
      boundingBoxes[161] = new Rectangle(118, -195, 97, 718);
      width[102] = 278;
      boundingBoxes[102] = new Rectangle(14, 0, 248, 728);
      width[53] = 556;
      boundingBoxes[53] = new Rectangle(32, -19, 482, 707);
      width[131] = 556;
      boundingBoxes[131] = new Rectangle(-11, -207, 512, 944);
      width[52] = 556;
      boundingBoxes[52] = new Rectangle(25, 0, 498, 703);
      width[103] = 556;
      boundingBoxes[103] = new Rectangle(40, -220, 459, 758);
      width[223] = 611;
      boundingBoxes[223] = new Rectangle(67, -15, 504, 743);
      width[96] = 333;
      boundingBoxes[96] = new Rectangle(14, 593, 197, 141);
      width[62] = 584;
      boundingBoxes[62] = new Rectangle(48, 11, 488, 484);
      width[171] = 556;
      boundingBoxes[171] = new Rectangle(97, 108, 362, 338);
      width[187] = 556;
      boundingBoxes[187] = new Rectangle(97, 108, 362, 338);
      width[139] = 333;
      boundingBoxes[139] = new Rectangle(88, 108, 157, 338);
      width[155] = 333;
      boundingBoxes[155] = new Rectangle(88, 108, 157, 338);
      width[104] = 556;
      boundingBoxes[104] = new Rectangle(65, 0, 426, 718);
      width[45] = 333;
      boundingBoxes[45] = new Rectangle(44, 232, 245, 90);
      width[105] = 222;
      boundingBoxes[105] = new Rectangle(67, 0, 88, 718);
      width[237] = 278;
      boundingBoxes[237] = new Rectangle(95, 0, 197, 734);
      width[238] = 278;
      boundingBoxes[238] = new Rectangle(-6, 0, 291, 734);
      width[239] = 278;
      boundingBoxes[239] = new Rectangle(13, 0, 253, 706);
      width[236] = 278;
      boundingBoxes[236] = new Rectangle(-13, 0, 197, 734);
      width[106] = 222;
      boundingBoxes[106] = new Rectangle(-16, -210, 171, 928);
      width[107] = 500;
      boundingBoxes[107] = new Rectangle(67, 0, 434, 718);
      width[108] = 222;
      boundingBoxes[108] = new Rectangle(67, 0, 88, 718);
      width[60] = 584;
      boundingBoxes[60] = new Rectangle(48, 11, 488, 484);
      width[172] = 584;
      boundingBoxes[172] = new Rectangle(39, 108, 506, 282);
      width[109] = 833;
      boundingBoxes[109] = new Rectangle(65, 0, 704, 538);
      width[175] = 333;
      boundingBoxes[175] = new Rectangle(10, 627, 313, 57);
      width[181] = 556;
      boundingBoxes[181] = new Rectangle(68, -207, 421, 730);
      width[215] = 584;
      boundingBoxes[215] = new Rectangle(39, 0, 506, 506);
      width[110] = 556;
      boundingBoxes[110] = new Rectangle(65, 0, 426, 538);
      width[57] = 556;
      boundingBoxes[57] = new Rectangle(42, -19, 472, 722);
      width[241] = 556;
      boundingBoxes[241] = new Rectangle(65, 0, 426, 722);
      width[35] = 556;
      boundingBoxes[35] = new Rectangle(28, 0, 501, 688);
      width[111] = 556;
      boundingBoxes[111] = new Rectangle(35, -14, 486, 552);
      width[243] = 556;
      boundingBoxes[243] = new Rectangle(35, -14, 486, 748);
      width[244] = 556;
      boundingBoxes[244] = new Rectangle(35, -14, 486, 748);
      width[246] = 556;
      boundingBoxes[246] = new Rectangle(35, -14, 486, 720);
      width[156] = 944;
      boundingBoxes[156] = new Rectangle(35, -15, 867, 553);
      width[242] = 556;
      boundingBoxes[242] = new Rectangle(35, -14, 486, 748);
      width[49] = 556;
      boundingBoxes[49] = new Rectangle(101, 0, 258, 703);
      width[189] = 834;
      boundingBoxes[189] = new Rectangle(43, -19, 730, 722);
      width[188] = 834;
      boundingBoxes[188] = new Rectangle(73, -19, 683, 722);
      width[185] = 333;
      boundingBoxes[185] = new Rectangle(43, 281, 179, 422);
      width[170] = 370;
      boundingBoxes[170] = new Rectangle(24, 405, 322, 332);
      width[186] = 365;
      boundingBoxes[186] = new Rectangle(25, 405, 316, 332);
      width[248] = 611;
      boundingBoxes[248] = new Rectangle(28, -22, 509, 567);
      width[245] = 556;
      boundingBoxes[245] = new Rectangle(35, -14, 486, 736);
      width[112] = 556;
      boundingBoxes[112] = new Rectangle(58, -207, 459, 745);
      width[182] = 537;
      boundingBoxes[182] = new Rectangle(18, -173, 479, 891);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(68, -207, 231, 940);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(34, -207, 231, 940);
      width[37] = 889;
      boundingBoxes[37] = new Rectangle(39, -19, 811, 722);
      width[46] = 278;
      boundingBoxes[46] = new Rectangle(87, 0, 104, 106);
      width[183] = 278;
      boundingBoxes[183] = new Rectangle(77, 190, 125, 125);
      width[137] = 1000;
      boundingBoxes[137] = new Rectangle(7, -19, 987, 722);
      width[43] = 584;
      boundingBoxes[43] = new Rectangle(39, 0, 506, 505);
      width[177] = 584;
      boundingBoxes[177] = new Rectangle(39, 0, 506, 506);
      width[113] = 556;
      boundingBoxes[113] = new Rectangle(35, -207, 459, 745);
      width[63] = 556;
      boundingBoxes[63] = new Rectangle(56, 0, 436, 727);
      width[191] = 611;
      boundingBoxes[191] = new Rectangle(91, -201, 436, 726);
      width[34] = 355;
      boundingBoxes[34] = new Rectangle(70, 463, 215, 255);
      width[132] = 333;
      boundingBoxes[132] = new Rectangle(26, -149, 269, 255);
      width[147] = 333;
      boundingBoxes[147] = new Rectangle(38, 470, 269, 255);
      width[148] = 333;
      boundingBoxes[148] = new Rectangle(26, 463, 269, 255);
      width[145] = 222;
      boundingBoxes[145] = new Rectangle(65, 470, 104, 255);
      width[146] = 222;
      boundingBoxes[146] = new Rectangle(53, 463, 104, 255);
      width[130] = 222;
      boundingBoxes[130] = new Rectangle(53, -149, 104, 255);
      width[39] = 191;
      boundingBoxes[39] = new Rectangle(59, 463, 73, 255);
      width[114] = 333;
      boundingBoxes[114] = new Rectangle(77, 0, 255, 538);
      width[174] = 737;
      boundingBoxes[174] = new Rectangle(-14, -19, 766, 756);
      width[115] = 500;
      boundingBoxes[115] = new Rectangle(32, -15, 432, 553);
      width[154] = 500;
      boundingBoxes[154] = new Rectangle(32, -15, 432, 749);
      width[167] = 556;
      boundingBoxes[167] = new Rectangle(43, -191, 469, 928);
      width[59] = 278;
      boundingBoxes[59] = new Rectangle(87, -147, 104, 663);
      width[55] = 556;
      boundingBoxes[55] = new Rectangle(37, 0, 486, 688);
      width[54] = 556;
      boundingBoxes[54] = new Rectangle(38, -19, 480, 722);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(-17, -19, 312, 756);
      width[32] = 278;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 556;
      boundingBoxes[163] = new Rectangle(33, -16, 506, 734);
      width[116] = 278;
      boundingBoxes[116] = new Rectangle(14, -7, 243, 676);
      width[254] = 556;
      boundingBoxes[254] = new Rectangle(58, -207, 459, 925);
      width[51] = 556;
      boundingBoxes[51] = new Rectangle(34, -19, 488, 722);
      width[190] = 834;
      boundingBoxes[190] = new Rectangle(45, -19, 765, 722);
      width[179] = 333;
      boundingBoxes[179] = new Rectangle(5, 270, 320, 433);
      width[152] = 333;
      boundingBoxes[152] = new Rectangle(-4, 606, 341, 116);
      width[153] = 1000;
      boundingBoxes[153] = new Rectangle(46, 306, 857, 412);
      width[50] = 556;
      boundingBoxes[50] = new Rectangle(26, 0, 481, 703);
      width[178] = 333;
      boundingBoxes[178] = new Rectangle(4, 281, 319, 422);
      width[117] = 556;
      boundingBoxes[117] = new Rectangle(68, -15, 421, 538);
      width[250] = 556;
      boundingBoxes[250] = new Rectangle(68, -15, 421, 749);
      width[251] = 556;
      boundingBoxes[251] = new Rectangle(68, -15, 421, 749);
      width[252] = 556;
      boundingBoxes[252] = new Rectangle(68, -15, 421, 721);
      width[249] = 556;
      boundingBoxes[249] = new Rectangle(68, -15, 421, 749);
      width[95] = 556;
      boundingBoxes[95] = new Rectangle(0, -125, 556, 50);
      width[118] = 500;
      boundingBoxes[118] = new Rectangle(8, 0, 484, 523);
      width[119] = 722;
      boundingBoxes[119] = new Rectangle(14, 0, 695, 523);
      width[120] = 500;
      boundingBoxes[120] = new Rectangle(11, 0, 479, 523);
      width[121] = 500;
      boundingBoxes[121] = new Rectangle(11, -214, 478, 737);
      width[253] = 500;
      boundingBoxes[253] = new Rectangle(11, -214, 478, 948);
      width[255] = 500;
      boundingBoxes[255] = new Rectangle(11, -214, 478, 920);
      width[165] = 556;
      boundingBoxes[165] = new Rectangle(3, 0, 550, 688);
      width[122] = 500;
      boundingBoxes[122] = new Rectangle(31, 0, 438, 523);
      width[158] = 500;
      boundingBoxes[158] = new Rectangle(31, 0, 438, 734);
      width[48] = 556;
      boundingBoxes[48] = new Rectangle(37, -19, 482, 722);
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
