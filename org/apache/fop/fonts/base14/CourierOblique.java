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

public class CourierOblique extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Courier-Oblique";
   private static final String fullName = "Courier Oblique";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 562;
   private static final int xHeight = 426;
   private static final int ascender = 629;
   private static final int descender = -157;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private boolean enableKerning;

   public CourierOblique() {
      this(false);
   }

   public CourierOblique(boolean enableKerning) {
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
      return "Courier-Oblique";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Courier Oblique";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 629;
   }

   public int getCapHeight(int size) {
      return size * 562;
   }

   public int getDescender(int size) {
      return size * -157;
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
         uri = new URI("base14:" + "Courier-Oblique".toLowerCase());
      } catch (URISyntaxException var2) {
         throw new RuntimeException(var2);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 600;
      boundingBoxes[65] = new Rectangle(3, 0, 604, 562);
      width[198] = 600;
      boundingBoxes[198] = new Rectangle(3, 0, 652, 562);
      width[193] = 600;
      boundingBoxes[193] = new Rectangle(3, 0, 657, 805);
      width[194] = 600;
      boundingBoxes[194] = new Rectangle(3, 0, 604, 787);
      width[196] = 600;
      boundingBoxes[196] = new Rectangle(3, 0, 604, 753);
      width[192] = 600;
      boundingBoxes[192] = new Rectangle(3, 0, 604, 805);
      width[197] = 600;
      boundingBoxes[197] = new Rectangle(3, 0, 604, 750);
      width[195] = 600;
      boundingBoxes[195] = new Rectangle(3, 0, 652, 729);
      width[66] = 600;
      boundingBoxes[66] = new Rectangle(43, 0, 573, 562);
      width[67] = 600;
      boundingBoxes[67] = new Rectangle(93, -18, 562, 598);
      width[199] = 600;
      boundingBoxes[199] = new Rectangle(93, -151, 565, 731);
      width[68] = 600;
      boundingBoxes[68] = new Rectangle(43, 0, 602, 562);
      width[69] = 600;
      boundingBoxes[69] = new Rectangle(53, 0, 607, 562);
      width[201] = 600;
      boundingBoxes[201] = new Rectangle(53, 0, 617, 805);
      width[202] = 600;
      boundingBoxes[202] = new Rectangle(53, 0, 607, 787);
      width[203] = 600;
      boundingBoxes[203] = new Rectangle(53, 0, 607, 753);
      width[200] = 600;
      boundingBoxes[200] = new Rectangle(53, 0, 607, 805);
      width[208] = 600;
      boundingBoxes[208] = new Rectangle(43, 0, 602, 562);
      width[128] = 600;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 600;
      boundingBoxes[70] = new Rectangle(53, 0, 607, 562);
      width[71] = 600;
      boundingBoxes[71] = new Rectangle(83, -18, 562, 598);
      width[72] = 600;
      boundingBoxes[72] = new Rectangle(32, 0, 655, 562);
      width[73] = 600;
      boundingBoxes[73] = new Rectangle(96, 0, 527, 562);
      width[205] = 600;
      boundingBoxes[205] = new Rectangle(96, 0, 544, 805);
      width[206] = 600;
      boundingBoxes[206] = new Rectangle(96, 0, 527, 787);
      width[207] = 600;
      boundingBoxes[207] = new Rectangle(96, 0, 527, 753);
      width[204] = 600;
      boundingBoxes[204] = new Rectangle(96, 0, 527, 805);
      width[74] = 600;
      boundingBoxes[74] = new Rectangle(52, -18, 633, 580);
      width[75] = 600;
      boundingBoxes[75] = new Rectangle(38, 0, 633, 562);
      width[76] = 600;
      boundingBoxes[76] = new Rectangle(47, 0, 560, 562);
      width[77] = 600;
      boundingBoxes[77] = new Rectangle(4, 0, 711, 562);
      width[78] = 600;
      boundingBoxes[78] = new Rectangle(7, -13, 705, 575);
      width[209] = 600;
      boundingBoxes[209] = new Rectangle(7, -13, 705, 742);
      width[79] = 600;
      boundingBoxes[79] = new Rectangle(94, -18, 531, 598);
      width[140] = 600;
      boundingBoxes[140] = new Rectangle(59, 0, 613, 562);
      width[211] = 600;
      boundingBoxes[211] = new Rectangle(94, -18, 546, 823);
      width[212] = 600;
      boundingBoxes[212] = new Rectangle(94, -18, 531, 805);
      width[214] = 600;
      boundingBoxes[214] = new Rectangle(94, -18, 531, 771);
      width[210] = 600;
      boundingBoxes[210] = new Rectangle(94, -18, 531, 823);
      width[216] = 600;
      boundingBoxes[216] = new Rectangle(94, -80, 531, 709);
      width[213] = 600;
      boundingBoxes[213] = new Rectangle(94, -18, 561, 747);
      width[80] = 600;
      boundingBoxes[80] = new Rectangle(79, 0, 565, 562);
      width[81] = 600;
      boundingBoxes[81] = new Rectangle(95, -138, 530, 718);
      width[82] = 600;
      boundingBoxes[82] = new Rectangle(38, 0, 560, 562);
      width[83] = 600;
      boundingBoxes[83] = new Rectangle(76, -20, 574, 600);
      width[138] = 600;
      boundingBoxes[138] = new Rectangle(76, -20, 596, 822);
      width[84] = 600;
      boundingBoxes[84] = new Rectangle(108, 0, 557, 562);
      width[222] = 600;
      boundingBoxes[222] = new Rectangle(79, 0, 527, 562);
      width[85] = 600;
      boundingBoxes[85] = new Rectangle(125, -18, 577, 580);
      width[218] = 600;
      boundingBoxes[218] = new Rectangle(125, -18, 577, 823);
      width[219] = 600;
      boundingBoxes[219] = new Rectangle(125, -18, 577, 805);
      width[220] = 600;
      boundingBoxes[220] = new Rectangle(125, -18, 577, 771);
      width[217] = 600;
      boundingBoxes[217] = new Rectangle(125, -18, 577, 823);
      width[86] = 600;
      boundingBoxes[86] = new Rectangle(105, -13, 618, 575);
      width[87] = 600;
      boundingBoxes[87] = new Rectangle(106, -13, 616, 575);
      width[88] = 600;
      boundingBoxes[88] = new Rectangle(23, 0, 652, 562);
      width[89] = 600;
      boundingBoxes[89] = new Rectangle(133, 0, 562, 562);
      width[221] = 600;
      boundingBoxes[221] = new Rectangle(133, 0, 562, 805);
      width[159] = 600;
      boundingBoxes[159] = new Rectangle(133, 0, 562, 753);
      width[90] = 600;
      boundingBoxes[90] = new Rectangle(86, 0, 524, 562);
      width[142] = 600;
      boundingBoxes[142] = new Rectangle(86, 0, 556, 802);
      width[97] = 600;
      boundingBoxes[97] = new Rectangle(76, -15, 493, 456);
      width[225] = 600;
      boundingBoxes[225] = new Rectangle(76, -15, 536, 687);
      width[226] = 600;
      boundingBoxes[226] = new Rectangle(76, -15, 505, 669);
      width[180] = 600;
      boundingBoxes[180] = new Rectangle(348, 497, 264, 175);
      width[228] = 600;
      boundingBoxes[228] = new Rectangle(76, -15, 499, 635);
      width[230] = 600;
      boundingBoxes[230] = new Rectangle(41, -15, 585, 456);
      width[224] = 600;
      boundingBoxes[224] = new Rectangle(76, -15, 493, 687);
      width[38] = 600;
      boundingBoxes[38] = new Rectangle(87, -15, 493, 558);
      width[229] = 600;
      boundingBoxes[229] = new Rectangle(76, -15, 493, 642);
      width[94] = 600;
      boundingBoxes[94] = new Rectangle(175, 354, 412, 268);
      width[126] = 600;
      boundingBoxes[126] = new Rectangle(116, 197, 484, 123);
      width[42] = 600;
      boundingBoxes[42] = new Rectangle(212, 257, 368, 350);
      width[64] = 600;
      boundingBoxes[64] = new Rectangle(127, -15, 455, 637);
      width[227] = 600;
      boundingBoxes[227] = new Rectangle(76, -15, 553, 621);
      width[98] = 600;
      boundingBoxes[98] = new Rectangle(29, -15, 596, 644);
      width[92] = 600;
      boundingBoxes[92] = new Rectangle(249, -80, 219, 709);
      width[124] = 600;
      boundingBoxes[124] = new Rectangle(222, -250, 263, 1000);
      width[123] = 600;
      boundingBoxes[123] = new Rectangle(233, -108, 336, 730);
      width[125] = 600;
      boundingBoxes[125] = new Rectangle(140, -108, 337, 730);
      width[91] = 600;
      boundingBoxes[91] = new Rectangle(246, -108, 328, 730);
      width[93] = 600;
      boundingBoxes[93] = new Rectangle(135, -108, 328, 730);
      width[166] = 600;
      boundingBoxes[166] = new Rectangle(238, -175, 231, 850);
      width[149] = 600;
      boundingBoxes[149] = new Rectangle(224, 130, 261, 253);
      width[99] = 600;
      boundingBoxes[99] = new Rectangle(106, -15, 502, 456);
      width[231] = 600;
      boundingBoxes[231] = new Rectangle(106, -151, 508, 592);
      width[184] = 600;
      boundingBoxes[184] = new Rectangle(197, -151, 147, 161);
      width[162] = 600;
      boundingBoxes[162] = new Rectangle(151, -49, 437, 663);
      width[136] = 600;
      boundingBoxes[136] = new Rectangle(229, 477, 352, 177);
      width[58] = 600;
      boundingBoxes[58] = new Rectangle(238, -15, 203, 400);
      width[44] = 600;
      boundingBoxes[44] = new Rectangle(157, -112, 213, 234);
      width[169] = 600;
      boundingBoxes[169] = new Rectangle(53, -18, 614, 598);
      width[164] = 600;
      boundingBoxes[164] = new Rectangle(94, 58, 534, 448);
      width[100] = 600;
      boundingBoxes[100] = new Rectangle(85, -15, 555, 644);
      width[134] = 600;
      boundingBoxes[134] = new Rectangle(217, -78, 329, 658);
      width[135] = 600;
      boundingBoxes[135] = new Rectangle(163, -78, 383, 658);
      width[176] = 600;
      boundingBoxes[176] = new Rectangle(214, 269, 362, 353);
      width[168] = 600;
      boundingBoxes[168] = new Rectangle(272, 537, 307, 103);
      width[247] = 600;
      boundingBoxes[247] = new Rectangle(136, 48, 437, 419);
      width[36] = 600;
      boundingBoxes[36] = new Rectangle(108, -126, 488, 788);
      width[101] = 600;
      boundingBoxes[101] = new Rectangle(106, -15, 492, 456);
      width[233] = 600;
      boundingBoxes[233] = new Rectangle(106, -15, 506, 687);
      width[234] = 600;
      boundingBoxes[234] = new Rectangle(106, -15, 492, 669);
      width[235] = 600;
      boundingBoxes[235] = new Rectangle(106, -15, 492, 635);
      width[232] = 600;
      boundingBoxes[232] = new Rectangle(106, -15, 492, 687);
      width[56] = 600;
      boundingBoxes[56] = new Rectangle(132, -15, 456, 637);
      width[133] = 600;
      boundingBoxes[133] = new Rectangle(46, -15, 529, 126);
      width[151] = 600;
      boundingBoxes[151] = new Rectangle(49, 231, 612, 54);
      width[150] = 600;
      boundingBoxes[150] = new Rectangle(124, 231, 462, 54);
      width[61] = 600;
      boundingBoxes[61] = new Rectangle(109, 138, 491, 238);
      width[240] = 600;
      boundingBoxes[240] = new Rectangle(102, -15, 537, 644);
      width[33] = 600;
      boundingBoxes[33] = new Rectangle(243, -15, 221, 587);
      width[161] = 600;
      boundingBoxes[161] = new Rectangle(225, -157, 220, 587);
      width[102] = 600;
      boundingBoxes[102] = new Rectangle(114, 0, 548, 629);
      width[53] = 600;
      boundingBoxes[53] = new Rectangle(99, -15, 490, 622);
      width[131] = 600;
      boundingBoxes[131] = new Rectangle(-26, -143, 697, 765);
      width[52] = 600;
      boundingBoxes[52] = new Rectangle(108, 0, 433, 622);
      width[103] = 600;
      boundingBoxes[103] = new Rectangle(61, -157, 596, 598);
      width[223] = 600;
      boundingBoxes[223] = new Rectangle(48, -15, 569, 644);
      width[96] = 600;
      boundingBoxes[96] = new Rectangle(294, 497, 190, 175);
      width[62] = 600;
      boundingBoxes[62] = new Rectangle(85, 42, 514, 430);
      width[171] = 600;
      boundingBoxes[171] = new Rectangle(92, 70, 560, 376);
      width[187] = 600;
      boundingBoxes[187] = new Rectangle(58, 70, 560, 376);
      width[139] = 600;
      boundingBoxes[139] = new Rectangle(204, 70, 336, 376);
      width[155] = 600;
      boundingBoxes[155] = new Rectangle(170, 70, 336, 376);
      width[104] = 600;
      boundingBoxes[104] = new Rectangle(33, 0, 559, 629);
      width[45] = 600;
      boundingBoxes[45] = new Rectangle(152, 231, 406, 54);
      width[105] = 600;
      boundingBoxes[105] = new Rectangle(95, 0, 420, 657);
      width[237] = 600;
      boundingBoxes[237] = new Rectangle(95, 0, 517, 672);
      width[238] = 600;
      boundingBoxes[238] = new Rectangle(95, 0, 456, 654);
      width[239] = 600;
      boundingBoxes[239] = new Rectangle(95, 0, 450, 620);
      width[236] = 600;
      boundingBoxes[236] = new Rectangle(95, 0, 420, 672);
      width[106] = 600;
      boundingBoxes[106] = new Rectangle(52, -157, 498, 814);
      width[107] = 600;
      boundingBoxes[107] = new Rectangle(58, 0, 575, 629);
      width[108] = 600;
      boundingBoxes[108] = new Rectangle(95, 0, 420, 629);
      width[60] = 600;
      boundingBoxes[60] = new Rectangle(96, 42, 514, 430);
      width[172] = 600;
      boundingBoxes[172] = new Rectangle(155, 108, 436, 261);
      width[109] = 600;
      boundingBoxes[109] = new Rectangle(-5, 0, 620, 441);
      width[175] = 600;
      boundingBoxes[175] = new Rectangle(232, 525, 368, 40);
      width[181] = 600;
      boundingBoxes[181] = new Rectangle(72, -157, 500, 583);
      width[215] = 600;
      boundingBoxes[215] = new Rectangle(103, 43, 504, 427);
      width[110] = 600;
      boundingBoxes[110] = new Rectangle(26, 0, 559, 441);
      width[57] = 600;
      boundingBoxes[57] = new Rectangle(93, -15, 481, 637);
      width[241] = 600;
      boundingBoxes[241] = new Rectangle(26, 0, 603, 606);
      width[35] = 600;
      boundingBoxes[35] = new Rectangle(133, -32, 463, 671);
      width[111] = 600;
      boundingBoxes[111] = new Rectangle(102, -15, 486, 456);
      width[243] = 600;
      boundingBoxes[243] = new Rectangle(102, -15, 510, 687);
      width[244] = 600;
      boundingBoxes[244] = new Rectangle(102, -15, 486, 669);
      width[246] = 600;
      boundingBoxes[246] = new Rectangle(102, -15, 486, 635);
      width[156] = 600;
      boundingBoxes[156] = new Rectangle(54, -15, 561, 456);
      width[242] = 600;
      boundingBoxes[242] = new Rectangle(102, -15, 486, 687);
      width[49] = 600;
      boundingBoxes[49] = new Rectangle(98, 0, 417, 622);
      width[189] = 600;
      boundingBoxes[189] = new Rectangle(65, -57, 604, 722);
      width[188] = 600;
      boundingBoxes[188] = new Rectangle(65, -57, 609, 722);
      width[185] = 600;
      boundingBoxes[185] = new Rectangle(231, 249, 260, 373);
      width[170] = 600;
      boundingBoxes[170] = new Rectangle(209, 249, 303, 331);
      width[186] = 600;
      boundingBoxes[186] = new Rectangle(210, 249, 325, 331);
      width[248] = 600;
      boundingBoxes[248] = new Rectangle(102, -80, 486, 586);
      width[245] = 600;
      boundingBoxes[245] = new Rectangle(102, -15, 527, 621);
      width[112] = 600;
      boundingBoxes[112] = new Rectangle(-24, -157, 629, 598);
      width[182] = 600;
      boundingBoxes[182] = new Rectangle(100, -78, 530, 640);
      width[40] = 600;
      boundingBoxes[40] = new Rectangle(313, -108, 259, 730);
      width[41] = 600;
      boundingBoxes[41] = new Rectangle(137, -108, 259, 730);
      width[37] = 600;
      boundingBoxes[37] = new Rectangle(134, -15, 465, 637);
      width[46] = 600;
      boundingBoxes[46] = new Rectangle(238, -15, 144, 124);
      width[183] = 600;
      boundingBoxes[183] = new Rectangle(275, 189, 159, 138);
      width[137] = 600;
      boundingBoxes[137] = new Rectangle(59, -15, 568, 637);
      width[43] = 600;
      boundingBoxes[43] = new Rectangle(129, 44, 451, 426);
      width[177] = 600;
      boundingBoxes[177] = new Rectangle(96, 44, 498, 514);
      width[113] = 600;
      boundingBoxes[113] = new Rectangle(85, -157, 597, 598);
      width[63] = 600;
      boundingBoxes[63] = new Rectangle(222, -15, 361, 587);
      width[191] = 600;
      boundingBoxes[191] = new Rectangle(105, -157, 361, 587);
      width[34] = 600;
      boundingBoxes[34] = new Rectangle(273, 328, 259, 234);
      width[132] = 600;
      boundingBoxes[132] = new Rectangle(115, -134, 363, 234);
      width[147] = 600;
      boundingBoxes[147] = new Rectangle(262, 328, 279, 234);
      width[148] = 600;
      boundingBoxes[148] = new Rectangle(213, 328, 363, 234);
      width[145] = 600;
      boundingBoxes[145] = new Rectangle(343, 328, 114, 234);
      width[146] = 600;
      boundingBoxes[146] = new Rectangle(283, 328, 212, 234);
      width[130] = 600;
      boundingBoxes[130] = new Rectangle(185, -134, 212, 234);
      width[39] = 600;
      boundingBoxes[39] = new Rectangle(345, 328, 115, 234);
      width[114] = 600;
      boundingBoxes[114] = new Rectangle(60, 0, 576, 441);
      width[174] = 600;
      boundingBoxes[174] = new Rectangle(53, -18, 614, 598);
      width[115] = 600;
      boundingBoxes[115] = new Rectangle(78, -15, 506, 456);
      width[154] = 600;
      boundingBoxes[154] = new Rectangle(78, -15, 536, 684);
      width[167] = 600;
      boundingBoxes[167] = new Rectangle(104, -78, 486, 658);
      width[59] = 600;
      boundingBoxes[59] = new Rectangle(157, -112, 284, 497);
      width[55] = 600;
      boundingBoxes[55] = new Rectangle(182, 0, 430, 607);
      width[54] = 600;
      boundingBoxes[54] = new Rectangle(155, -15, 474, 637);
      width[47] = 600;
      boundingBoxes[47] = new Rectangle(112, -80, 492, 709);
      width[32] = 600;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 600;
      boundingBoxes[163] = new Rectangle(124, -21, 497, 632);
      width[116] = 600;
      boundingBoxes[116] = new Rectangle(167, -15, 394, 576);
      width[254] = 600;
      boundingBoxes[254] = new Rectangle(-24, -157, 629, 786);
      width[51] = 600;
      boundingBoxes[51] = new Rectangle(82, -15, 456, 637);
      width[190] = 600;
      boundingBoxes[190] = new Rectangle(73, -56, 586, 722);
      width[179] = 600;
      boundingBoxes[179] = new Rectangle(213, 240, 288, 382);
      width[152] = 600;
      boundingBoxes[152] = new Rectangle(212, 489, 417, 117);
      width[153] = 600;
      boundingBoxes[153] = new Rectangle(75, 263, 667, 299);
      width[50] = 600;
      boundingBoxes[50] = new Rectangle(70, 0, 498, 622);
      width[178] = 600;
      boundingBoxes[178] = new Rectangle(230, 249, 305, 373);
      width[117] = 600;
      boundingBoxes[117] = new Rectangle(101, -15, 471, 441);
      width[250] = 600;
      boundingBoxes[250] = new Rectangle(101, -15, 501, 687);
      width[251] = 600;
      boundingBoxes[251] = new Rectangle(101, -15, 471, 669);
      width[252] = 600;
      boundingBoxes[252] = new Rectangle(101, -15, 474, 635);
      width[249] = 600;
      boundingBoxes[249] = new Rectangle(101, -15, 471, 687);
      width[95] = 600;
      boundingBoxes[95] = new Rectangle(-27, -125, 611, 50);
      width[118] = 600;
      boundingBoxes[118] = new Rectangle(90, -10, 591, 436);
      width[119] = 600;
      boundingBoxes[119] = new Rectangle(76, -10, 619, 436);
      width[120] = 600;
      boundingBoxes[120] = new Rectangle(20, 0, 635, 426);
      width[121] = 600;
      boundingBoxes[121] = new Rectangle(-4, -157, 687, 583);
      width[253] = 600;
      boundingBoxes[253] = new Rectangle(-4, -157, 687, 829);
      width[255] = 600;
      boundingBoxes[255] = new Rectangle(-4, -157, 687, 777);
      width[165] = 600;
      boundingBoxes[165] = new Rectangle(120, 0, 573, 562);
      width[122] = 600;
      boundingBoxes[122] = new Rectangle(99, 0, 494, 426);
      width[158] = 600;
      boundingBoxes[158] = new Rectangle(99, 0, 525, 669);
      width[48] = 600;
      boundingBoxes[48] = new Rectangle(154, -15, 421, 637);
      familyNames = new HashSet();
      familyNames.add("Courier");
   }
}
