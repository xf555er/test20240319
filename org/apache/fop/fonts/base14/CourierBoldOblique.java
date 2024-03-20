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

public class CourierBoldOblique extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Courier-BoldOblique";
   private static final String fullName = "Courier Bold Oblique";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 562;
   private static final int xHeight = 439;
   private static final int ascender = 626;
   private static final int descender = -142;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private boolean enableKerning;

   public CourierBoldOblique() {
      this(false);
   }

   public CourierBoldOblique(boolean enableKerning) {
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
      return "Courier-BoldOblique";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Courier Bold Oblique";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 626;
   }

   public int getCapHeight(int size) {
      return size * 562;
   }

   public int getDescender(int size) {
      return size * -142;
   }

   public int getXHeight(int size) {
      return size * 439;
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
         uri = new URI("base14:" + "Courier-BoldOblique".toLowerCase());
      } catch (URISyntaxException var2) {
         throw new RuntimeException(var2);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 600;
      boundingBoxes[65] = new Rectangle(-9, 0, 641, 562);
      width[198] = 600;
      boundingBoxes[198] = new Rectangle(-29, 0, 737, 562);
      width[193] = 600;
      boundingBoxes[193] = new Rectangle(-9, 0, 664, 784);
      width[194] = 600;
      boundingBoxes[194] = new Rectangle(-9, 0, 641, 780);
      width[196] = 600;
      boundingBoxes[196] = new Rectangle(-9, 0, 641, 761);
      width[192] = 600;
      boundingBoxes[192] = new Rectangle(-9, 0, 641, 784);
      width[197] = 600;
      boundingBoxes[197] = new Rectangle(-9, 0, 641, 801);
      width[195] = 600;
      boundingBoxes[195] = new Rectangle(-9, 0, 678, 759);
      width[66] = 600;
      boundingBoxes[66] = new Rectangle(30, 0, 600, 562);
      width[67] = 600;
      boundingBoxes[67] = new Rectangle(74, -18, 601, 598);
      width[199] = 600;
      boundingBoxes[199] = new Rectangle(74, -206, 601, 786);
      width[68] = 600;
      boundingBoxes[68] = new Rectangle(30, 0, 634, 562);
      width[69] = 600;
      boundingBoxes[69] = new Rectangle(25, 0, 645, 562);
      width[201] = 600;
      boundingBoxes[201] = new Rectangle(25, 0, 645, 784);
      width[202] = 600;
      boundingBoxes[202] = new Rectangle(25, 0, 645, 780);
      width[203] = 600;
      boundingBoxes[203] = new Rectangle(25, 0, 645, 761);
      width[200] = 600;
      boundingBoxes[200] = new Rectangle(25, 0, 645, 784);
      width[208] = 600;
      boundingBoxes[208] = new Rectangle(30, 0, 634, 562);
      width[128] = 600;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 600;
      boundingBoxes[70] = new Rectangle(39, 0, 645, 562);
      width[71] = 600;
      boundingBoxes[71] = new Rectangle(74, -18, 601, 598);
      width[72] = 600;
      boundingBoxes[72] = new Rectangle(20, 0, 680, 562);
      width[73] = 600;
      boundingBoxes[73] = new Rectangle(77, 0, 566, 562);
      width[205] = 600;
      boundingBoxes[205] = new Rectangle(77, 0, 566, 784);
      width[206] = 600;
      boundingBoxes[206] = new Rectangle(77, 0, 566, 780);
      width[207] = 600;
      boundingBoxes[207] = new Rectangle(77, 0, 566, 761);
      width[204] = 600;
      boundingBoxes[204] = new Rectangle(77, 0, 566, 784);
      width[74] = 600;
      boundingBoxes[74] = new Rectangle(58, -18, 663, 580);
      width[75] = 600;
      boundingBoxes[75] = new Rectangle(21, 0, 671, 562);
      width[76] = 600;
      boundingBoxes[76] = new Rectangle(39, 0, 597, 562);
      width[77] = 600;
      boundingBoxes[77] = new Rectangle(-2, 0, 724, 562);
      width[78] = 600;
      boundingBoxes[78] = new Rectangle(8, -12, 722, 574);
      width[209] = 600;
      boundingBoxes[209] = new Rectangle(8, -12, 722, 771);
      width[79] = 600;
      boundingBoxes[79] = new Rectangle(74, -18, 571, 598);
      width[140] = 600;
      boundingBoxes[140] = new Rectangle(26, 0, 675, 562);
      width[211] = 600;
      boundingBoxes[211] = new Rectangle(74, -18, 571, 802);
      width[212] = 600;
      boundingBoxes[212] = new Rectangle(74, -18, 571, 798);
      width[214] = 600;
      boundingBoxes[214] = new Rectangle(74, -18, 571, 779);
      width[210] = 600;
      boundingBoxes[210] = new Rectangle(74, -18, 571, 802);
      width[216] = 600;
      boundingBoxes[216] = new Rectangle(48, -22, 625, 606);
      width[213] = 600;
      boundingBoxes[213] = new Rectangle(74, -18, 595, 777);
      width[80] = 600;
      boundingBoxes[80] = new Rectangle(48, 0, 595, 562);
      width[81] = 600;
      boundingBoxes[81] = new Rectangle(83, -138, 553, 718);
      width[82] = 600;
      boundingBoxes[82] = new Rectangle(24, 0, 593, 562);
      width[83] = 600;
      boundingBoxes[83] = new Rectangle(54, -22, 619, 604);
      width[138] = 600;
      boundingBoxes[138] = new Rectangle(54, -22, 635, 812);
      width[84] = 600;
      boundingBoxes[84] = new Rectangle(86, 0, 593, 562);
      width[222] = 600;
      boundingBoxes[222] = new Rectangle(48, 0, 572, 562);
      width[85] = 600;
      boundingBoxes[85] = new Rectangle(101, -18, 615, 580);
      width[218] = 600;
      boundingBoxes[218] = new Rectangle(101, -18, 615, 802);
      width[219] = 600;
      boundingBoxes[219] = new Rectangle(101, -18, 615, 798);
      width[220] = 600;
      boundingBoxes[220] = new Rectangle(101, -18, 615, 779);
      width[217] = 600;
      boundingBoxes[217] = new Rectangle(101, -18, 615, 802);
      width[86] = 600;
      boundingBoxes[86] = new Rectangle(84, 0, 649, 562);
      width[87] = 600;
      boundingBoxes[87] = new Rectangle(79, 0, 659, 562);
      width[88] = 600;
      boundingBoxes[88] = new Rectangle(12, 0, 678, 562);
      width[89] = 600;
      boundingBoxes[89] = new Rectangle(109, 0, 600, 562);
      width[221] = 600;
      boundingBoxes[221] = new Rectangle(109, 0, 600, 784);
      width[159] = 600;
      boundingBoxes[159] = new Rectangle(109, 0, 600, 761);
      width[90] = 600;
      boundingBoxes[90] = new Rectangle(62, 0, 575, 562);
      width[142] = 600;
      boundingBoxes[142] = new Rectangle(62, 0, 597, 790);
      width[97] = 600;
      boundingBoxes[97] = new Rectangle(61, -15, 532, 469);
      width[225] = 600;
      boundingBoxes[225] = new Rectangle(61, -15, 548, 676);
      width[226] = 600;
      boundingBoxes[226] = new Rectangle(61, -15, 546, 672);
      width[180] = 600;
      boundingBoxes[180] = new Rectangle(312, 508, 297, 153);
      width[228] = 600;
      boundingBoxes[228] = new Rectangle(61, -15, 534, 653);
      width[230] = 600;
      boundingBoxes[230] = new Rectangle(21, -15, 631, 469);
      width[224] = 600;
      boundingBoxes[224] = new Rectangle(61, -15, 532, 676);
      width[38] = 600;
      boundingBoxes[38] = new Rectangle(61, -15, 534, 558);
      width[229] = 600;
      boundingBoxes[229] = new Rectangle(61, -15, 532, 693);
      width[94] = 600;
      boundingBoxes[94] = new Rectangle(171, 250, 385, 366);
      width[126] = 600;
      boundingBoxes[126] = new Rectangle(120, 153, 470, 203);
      width[42] = 600;
      boundingBoxes[42] = new Rectangle(179, 219, 419, 382);
      width[64] = 600;
      boundingBoxes[64] = new Rectangle(65, -15, 577, 631);
      width[227] = 600;
      boundingBoxes[227] = new Rectangle(61, -15, 582, 651);
      width[98] = 600;
      boundingBoxes[98] = new Rectangle(13, -15, 623, 641);
      width[92] = 600;
      boundingBoxes[92] = new Rectangle(222, -77, 274, 703);
      width[124] = 600;
      boundingBoxes[124] = new Rectangle(201, -250, 304, 1000);
      width[123] = 600;
      boundingBoxes[123] = new Rectangle(203, -102, 392, 718);
      width[125] = 600;
      boundingBoxes[125] = new Rectangle(114, -102, 392, 718);
      width[91] = 600;
      boundingBoxes[91] = new Rectangle(223, -102, 383, 718);
      width[93] = 600;
      boundingBoxes[93] = new Rectangle(103, -102, 383, 718);
      width[166] = 600;
      boundingBoxes[166] = new Rectangle(217, -175, 272, 850);
      width[149] = 600;
      boundingBoxes[149] = new Rectangle(196, 132, 327, 298);
      width[99] = 600;
      boundingBoxes[99] = new Rectangle(81, -15, 550, 474);
      width[231] = 600;
      boundingBoxes[231] = new Rectangle(81, -206, 550, 665);
      width[184] = 600;
      boundingBoxes[184] = new Rectangle(168, -206, 200, 206);
      width[162] = 600;
      boundingBoxes[162] = new Rectangle(121, -49, 484, 663);
      width[136] = 600;
      boundingBoxes[136] = new Rectangle(212, 483, 395, 174);
      width[58] = 600;
      boundingBoxes[58] = new Rectangle(205, -15, 275, 440);
      width[44] = 600;
      boundingBoxes[44] = new Rectangle(99, -111, 331, 285);
      width[169] = 600;
      boundingBoxes[169] = new Rectangle(53, -18, 614, 598);
      width[164] = 600;
      boundingBoxes[164] = new Rectangle(77, 49, 567, 468);
      width[100] = 600;
      boundingBoxes[100] = new Rectangle(60, -15, 585, 641);
      width[134] = 600;
      boundingBoxes[134] = new Rectangle(175, -70, 411, 650);
      width[135] = 600;
      boundingBoxes[135] = new Rectangle(121, -70, 466, 650);
      width[176] = 600;
      boundingBoxes[176] = new Rectangle(173, 243, 397, 373);
      width[168] = 600;
      boundingBoxes[168] = new Rectangle(246, 498, 349, 140);
      width[247] = 600;
      boundingBoxes[247] = new Rectangle(114, 16, 482, 484);
      width[36] = 600;
      boundingBoxes[36] = new Rectangle(87, -126, 543, 792);
      width[101] = 600;
      boundingBoxes[101] = new Rectangle(81, -15, 524, 469);
      width[233] = 600;
      boundingBoxes[233] = new Rectangle(81, -15, 528, 676);
      width[234] = 600;
      boundingBoxes[234] = new Rectangle(81, -15, 526, 672);
      width[235] = 600;
      boundingBoxes[235] = new Rectangle(81, -15, 524, 653);
      width[232] = 600;
      boundingBoxes[232] = new Rectangle(81, -15, 524, 676);
      width[56] = 600;
      boundingBoxes[56] = new Rectangle(115, -15, 489, 631);
      width[133] = 600;
      boundingBoxes[133] = new Rectangle(35, -15, 552, 131);
      width[151] = 600;
      boundingBoxes[151] = new Rectangle(33, 203, 644, 110);
      width[150] = 600;
      boundingBoxes[150] = new Rectangle(108, 203, 494, 110);
      width[61] = 600;
      boundingBoxes[61] = new Rectangle(96, 118, 518, 280);
      width[240] = 600;
      boundingBoxes[240] = new Rectangle(93, -27, 568, 653);
      width[33] = 600;
      boundingBoxes[33] = new Rectangle(215, -15, 280, 587);
      width[161] = 600;
      boundingBoxes[161] = new Rectangle(196, -146, 281, 595);
      width[102] = 600;
      boundingBoxes[102] = new Rectangle(83, 0, 594, 626);
      width[53] = 600;
      boundingBoxes[53] = new Rectangle(77, -15, 544, 616);
      width[131] = 600;
      boundingBoxes[131] = new Rectangle(-57, -131, 759, 747);
      width[52] = 600;
      boundingBoxes[52] = new Rectangle(81, 0, 478, 616);
      width[103] = 600;
      boundingBoxes[103] = new Rectangle(40, -146, 634, 600);
      width[223] = 600;
      boundingBoxes[223] = new Rectangle(22, -15, 607, 641);
      width[96] = 600;
      boundingBoxes[96] = new Rectangle(272, 508, 231, 153);
      width[62] = 600;
      boundingBoxes[62] = new Rectangle(97, 15, 492, 486);
      width[171] = 600;
      boundingBoxes[171] = new Rectangle(62, 70, 577, 376);
      width[187] = 600;
      boundingBoxes[187] = new Rectangle(71, 70, 576, 376);
      width[139] = 600;
      boundingBoxes[139] = new Rectangle(195, 70, 350, 376);
      width[155] = 600;
      boundingBoxes[155] = new Rectangle(165, 70, 349, 376);
      width[104] = 600;
      boundingBoxes[104] = new Rectangle(18, 0, 597, 626);
      width[45] = 600;
      boundingBoxes[45] = new Rectangle(143, 203, 424, 110);
      width[105] = 600;
      boundingBoxes[105] = new Rectangle(77, 0, 469, 658);
      width[237] = 600;
      boundingBoxes[237] = new Rectangle(77, 0, 532, 661);
      width[238] = 600;
      boundingBoxes[238] = new Rectangle(77, 0, 500, 657);
      width[239] = 600;
      boundingBoxes[239] = new Rectangle(77, 0, 484, 618);
      width[236] = 600;
      boundingBoxes[236] = new Rectangle(77, 0, 469, 661);
      width[106] = 600;
      boundingBoxes[106] = new Rectangle(36, -146, 544, 804);
      width[107] = 600;
      boundingBoxes[107] = new Rectangle(33, 0, 610, 626);
      width[108] = 600;
      boundingBoxes[108] = new Rectangle(77, 0, 469, 626);
      width[60] = 600;
      boundingBoxes[60] = new Rectangle(120, 15, 493, 486);
      width[172] = 600;
      boundingBoxes[172] = new Rectangle(135, 103, 482, 310);
      width[109] = 600;
      boundingBoxes[109] = new Rectangle(-22, 0, 671, 454);
      width[175] = 600;
      boundingBoxes[175] = new Rectangle(195, 505, 442, 80);
      width[181] = 600;
      boundingBoxes[181] = new Rectangle(49, -142, 543, 581);
      width[215] = 600;
      boundingBoxes[215] = new Rectangle(104, 39, 502, 439);
      width[110] = 600;
      boundingBoxes[110] = new Rectangle(18, 0, 597, 454);
      width[57] = 600;
      boundingBoxes[57] = new Rectangle(75, -15, 517, 631);
      width[241] = 600;
      boundingBoxes[241] = new Rectangle(18, 0, 625, 636);
      width[35] = 600;
      boundingBoxes[35] = new Rectangle(88, -45, 553, 696);
      width[111] = 600;
      boundingBoxes[111] = new Rectangle(71, -15, 551, 469);
      width[243] = 600;
      boundingBoxes[243] = new Rectangle(71, -15, 578, 676);
      width[244] = 600;
      boundingBoxes[244] = new Rectangle(71, -15, 551, 672);
      width[246] = 600;
      boundingBoxes[246] = new Rectangle(71, -15, 551, 653);
      width[156] = 600;
      boundingBoxes[156] = new Rectangle(18, -15, 644, 469);
      width[242] = 600;
      boundingBoxes[242] = new Rectangle(71, -15, 551, 676);
      width[49] = 600;
      boundingBoxes[49] = new Rectangle(93, 0, 469, 616);
      width[189] = 600;
      boundingBoxes[189] = new Rectangle(22, -60, 694, 721);
      width[188] = 600;
      boundingBoxes[188] = new Rectangle(13, -60, 694, 721);
      width[185] = 600;
      boundingBoxes[185] = new Rectangle(212, 230, 302, 386);
      width[170] = 600;
      boundingBoxes[170] = new Rectangle(188, 196, 338, 384);
      width[186] = 600;
      boundingBoxes[186] = new Rectangle(188, 196, 355, 384);
      width[248] = 600;
      boundingBoxes[248] = new Rectangle(54, -24, 584, 487);
      width[245] = 600;
      boundingBoxes[245] = new Rectangle(71, -15, 572, 651);
      width[112] = 600;
      boundingBoxes[112] = new Rectangle(-32, -142, 654, 596);
      width[182] = 600;
      boundingBoxes[182] = new Rectangle(61, -70, 639, 650);
      width[40] = 600;
      boundingBoxes[40] = new Rectangle(265, -102, 327, 718);
      width[41] = 600;
      boundingBoxes[41] = new Rectangle(117, -102, 327, 718);
      width[37] = 600;
      boundingBoxes[37] = new Rectangle(101, -15, 524, 631);
      width[46] = 600;
      boundingBoxes[46] = new Rectangle(206, -15, 221, 186);
      width[183] = 600;
      boundingBoxes[183] = new Rectangle(248, 165, 213, 186);
      width[137] = 600;
      boundingBoxes[137] = new Rectangle(-45, -15, 788, 631);
      width[43] = 600;
      boundingBoxes[43] = new Rectangle(114, 39, 482, 439);
      width[177] = 600;
      boundingBoxes[177] = new Rectangle(76, 24, 538, 491);
      width[113] = 600;
      boundingBoxes[113] = new Rectangle(60, -142, 625, 596);
      width[63] = 600;
      boundingBoxes[63] = new Rectangle(183, -14, 409, 594);
      width[191] = 600;
      boundingBoxes[191] = new Rectangle(100, -146, 409, 595);
      width[34] = 600;
      boundingBoxes[34] = new Rectangle(211, 277, 374, 285);
      width[132] = 600;
      boundingBoxes[132] = new Rectangle(34, -142, 526, 285);
      width[147] = 600;
      boundingBoxes[147] = new Rectangle(190, 277, 404, 285);
      width[148] = 600;
      boundingBoxes[148] = new Rectangle(119, 277, 526, 285);
      width[145] = 600;
      boundingBoxes[145] = new Rectangle(297, 277, 190, 285);
      width[146] = 600;
      boundingBoxes[146] = new Rectangle(229, 277, 314, 285);
      width[130] = 600;
      boundingBoxes[130] = new Rectangle(144, -142, 314, 285);
      width[39] = 600;
      boundingBoxes[39] = new Rectangle(303, 277, 190, 285);
      width[114] = 600;
      boundingBoxes[114] = new Rectangle(47, 0, 608, 454);
      width[174] = 600;
      boundingBoxes[174] = new Rectangle(53, -18, 614, 598);
      width[115] = 600;
      boundingBoxes[115] = new Rectangle(66, -17, 542, 476);
      width[154] = 600;
      boundingBoxes[154] = new Rectangle(66, -17, 567, 684);
      width[167] = 600;
      boundingBoxes[167] = new Rectangle(74, -70, 546, 650);
      width[59] = 600;
      boundingBoxes[59] = new Rectangle(99, -111, 382, 536);
      width[55] = 600;
      boundingBoxes[55] = new Rectangle(147, 0, 475, 601);
      width[54] = 600;
      boundingBoxes[54] = new Rectangle(135, -15, 517, 631);
      width[47] = 600;
      boundingBoxes[47] = new Rectangle(90, -77, 536, 703);
      width[32] = 600;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 600;
      boundingBoxes[163] = new Rectangle(106, -28, 544, 639);
      width[116] = 600;
      boundingBoxes[116] = new Rectangle(118, -15, 449, 577);
      width[254] = 600;
      boundingBoxes[254] = new Rectangle(-32, -142, 654, 768);
      width[51] = 600;
      boundingBoxes[51] = new Rectangle(71, -15, 500, 631);
      width[190] = 600;
      boundingBoxes[190] = new Rectangle(8, -60, 691, 721);
      width[179] = 600;
      boundingBoxes[179] = new Rectangle(193, 222, 333, 394);
      width[152] = 600;
      boundingBoxes[152] = new Rectangle(199, 493, 444, 143);
      width[153] = 600;
      boundingBoxes[153] = new Rectangle(86, 230, 783, 332);
      width[50] = 600;
      boundingBoxes[50] = new Rectangle(61, 0, 533, 616);
      width[178] = 600;
      boundingBoxes[178] = new Rectangle(191, 230, 351, 386);
      width[117] = 600;
      boundingBoxes[117] = new Rectangle(70, -15, 522, 454);
      width[250] = 600;
      boundingBoxes[250] = new Rectangle(70, -15, 529, 676);
      width[251] = 600;
      boundingBoxes[251] = new Rectangle(70, -15, 527, 672);
      width[252] = 600;
      boundingBoxes[252] = new Rectangle(70, -15, 525, 653);
      width[249] = 600;
      boundingBoxes[249] = new Rectangle(70, -15, 522, 676);
      width[95] = 600;
      boundingBoxes[95] = new Rectangle(-27, -125, 612, 50);
      width[118] = 600;
      boundingBoxes[118] = new Rectangle(70, 0, 625, 439);
      width[119] = 600;
      boundingBoxes[119] = new Rectangle(53, 0, 659, 439);
      width[120] = 600;
      boundingBoxes[120] = new Rectangle(6, 0, 665, 439);
      width[121] = 600;
      boundingBoxes[121] = new Rectangle(-21, -142, 716, 581);
      width[253] = 600;
      boundingBoxes[253] = new Rectangle(-21, -142, 716, 803);
      width[255] = 600;
      boundingBoxes[255] = new Rectangle(-21, -142, 716, 780);
      width[165] = 600;
      boundingBoxes[165] = new Rectangle(98, 0, 612, 562);
      width[122] = 600;
      boundingBoxes[122] = new Rectangle(81, 0, 533, 439);
      width[158] = 600;
      boundingBoxes[158] = new Rectangle(81, 0, 562, 667);
      width[48] = 600;
      boundingBoxes[48] = new Rectangle(135, -15, 458, 631);
      familyNames = new HashSet();
      familyNames.add("Courier");
   }
}
