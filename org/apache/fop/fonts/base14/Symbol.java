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

public class Symbol extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Symbol";
   private static final String fullName = "Symbol";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "SymbolEncoding";
   private static final int capHeight = 1010;
   private static final int xHeight = 520;
   private static final int ascender = 1010;
   private static final int descender = -293;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private boolean enableKerning;

   public Symbol() {
      this(false);
   }

   public Symbol(boolean enableKerning) {
      this.mapping = CodePointMapping.getMapping("SymbolEncoding");
      this.enableKerning = enableKerning;
   }

   public String getEncodingName() {
      return "SymbolEncoding";
   }

   public URI getFontURI() {
      return fontFileURI;
   }

   public String getFontName() {
      return "Symbol";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Symbol";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 1010;
   }

   public int getCapHeight(int size) {
      return size * 1010;
   }

   public int getDescender(int size) {
      return size * -293;
   }

   public int getXHeight(int size) {
      return size * 520;
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
         uri = new URI("base14:" + "Symbol".toLowerCase());
      } catch (URISyntaxException var2) {
         throw new RuntimeException(var2);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[32] = 250;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[33] = 333;
      boundingBoxes[33] = new Rectangle(128, -17, 112, 689);
      width[34] = 713;
      boundingBoxes[34] = new Rectangle(31, 0, 650, 705);
      width[35] = 500;
      boundingBoxes[35] = new Rectangle(20, -16, 461, 689);
      width[36] = 549;
      boundingBoxes[36] = new Rectangle(25, 0, 453, 707);
      width[37] = 833;
      boundingBoxes[37] = new Rectangle(63, -36, 708, 691);
      width[38] = 778;
      boundingBoxes[38] = new Rectangle(41, -18, 709, 679);
      width[39] = 439;
      boundingBoxes[39] = new Rectangle(48, -17, 366, 517);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(53, -191, 247, 864);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(30, -191, 247, 864);
      width[42] = 500;
      boundingBoxes[42] = new Rectangle(65, 134, 362, 417);
      width[43] = 549;
      boundingBoxes[43] = new Rectangle(10, 0, 529, 533);
      width[44] = 250;
      boundingBoxes[44] = new Rectangle(56, -152, 138, 256);
      width[45] = 549;
      boundingBoxes[45] = new Rectangle(11, 233, 524, 55);
      width[46] = 250;
      boundingBoxes[46] = new Rectangle(69, -17, 112, 112);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(0, -18, 254, 664);
      width[48] = 500;
      boundingBoxes[48] = new Rectangle(24, -14, 452, 699);
      width[49] = 500;
      boundingBoxes[49] = new Rectangle(117, 0, 273, 673);
      width[50] = 500;
      boundingBoxes[50] = new Rectangle(25, 0, 450, 685);
      width[51] = 500;
      boundingBoxes[51] = new Rectangle(43, -14, 392, 699);
      width[52] = 500;
      boundingBoxes[52] = new Rectangle(15, 0, 454, 685);
      width[53] = 500;
      boundingBoxes[53] = new Rectangle(32, -14, 413, 704);
      width[54] = 500;
      boundingBoxes[54] = new Rectangle(34, -14, 434, 699);
      width[55] = 500;
      boundingBoxes[55] = new Rectangle(24, -16, 424, 689);
      width[56] = 500;
      boundingBoxes[56] = new Rectangle(56, -14, 389, 699);
      width[57] = 500;
      boundingBoxes[57] = new Rectangle(30, -18, 429, 703);
      width[58] = 278;
      boundingBoxes[58] = new Rectangle(81, -17, 112, 477);
      width[59] = 278;
      boundingBoxes[59] = new Rectangle(83, -152, 138, 612);
      width[60] = 549;
      boundingBoxes[60] = new Rectangle(26, 0, 497, 522);
      width[61] = 549;
      boundingBoxes[61] = new Rectangle(11, 141, 526, 249);
      width[62] = 549;
      boundingBoxes[62] = new Rectangle(26, 0, 497, 522);
      width[63] = 444;
      boundingBoxes[63] = new Rectangle(70, -17, 342, 703);
      width[64] = 549;
      boundingBoxes[64] = new Rectangle(11, 0, 526, 475);
      width[65] = 722;
      boundingBoxes[65] = new Rectangle(4, 0, 680, 673);
      width[66] = 667;
      boundingBoxes[66] = new Rectangle(29, 0, 563, 673);
      width[67] = 722;
      boundingBoxes[67] = new Rectangle(-9, 0, 713, 673);
      width[68] = 612;
      boundingBoxes[68] = new Rectangle(6, 0, 602, 688);
      width[69] = 611;
      boundingBoxes[69] = new Rectangle(32, 0, 585, 673);
      width[70] = 763;
      boundingBoxes[70] = new Rectangle(26, 0, 715, 673);
      width[71] = 603;
      boundingBoxes[71] = new Rectangle(24, 0, 585, 673);
      width[72] = 722;
      boundingBoxes[72] = new Rectangle(39, 0, 690, 673);
      width[73] = 333;
      boundingBoxes[73] = new Rectangle(32, 0, 284, 673);
      width[74] = 631;
      boundingBoxes[74] = new Rectangle(18, -18, 605, 707);
      width[75] = 722;
      boundingBoxes[75] = new Rectangle(35, 0, 687, 673);
      width[76] = 686;
      boundingBoxes[76] = new Rectangle(6, 0, 674, 688);
      width[77] = 889;
      boundingBoxes[77] = new Rectangle(28, 0, 859, 673);
      width[78] = 722;
      boundingBoxes[78] = new Rectangle(29, -8, 691, 681);
      width[79] = 722;
      boundingBoxes[79] = new Rectangle(41, -17, 674, 702);
      width[80] = 768;
      boundingBoxes[80] = new Rectangle(25, 0, 720, 673);
      width[81] = 741;
      boundingBoxes[81] = new Rectangle(41, -17, 674, 702);
      width[82] = 556;
      boundingBoxes[82] = new Rectangle(28, 0, 535, 673);
      width[83] = 592;
      boundingBoxes[83] = new Rectangle(5, 0, 584, 673);
      width[84] = 611;
      boundingBoxes[84] = new Rectangle(33, 0, 574, 673);
      width[85] = 690;
      boundingBoxes[85] = new Rectangle(-8, 0, 702, 673);
      width[86] = 439;
      boundingBoxes[86] = new Rectangle(40, -233, 396, 733);
      width[87] = 768;
      boundingBoxes[87] = new Rectangle(34, 0, 702, 688);
      width[88] = 645;
      boundingBoxes[88] = new Rectangle(40, 0, 559, 673);
      width[89] = 795;
      boundingBoxes[89] = new Rectangle(15, 0, 766, 684);
      width[90] = 611;
      boundingBoxes[90] = new Rectangle(44, 0, 592, 673);
      width[91] = 333;
      boundingBoxes[91] = new Rectangle(86, -155, 213, 829);
      width[92] = 863;
      boundingBoxes[92] = new Rectangle(163, 0, 538, 487);
      width[93] = 333;
      boundingBoxes[93] = new Rectangle(33, -155, 213, 829);
      width[94] = 658;
      boundingBoxes[94] = new Rectangle(15, 0, 637, 674);
      width[95] = 500;
      boundingBoxes[95] = new Rectangle(-2, -125, 504, 50);
      width[96] = 500;
      boundingBoxes[96] = new Rectangle(480, 881, 610, 36);
      width[97] = 631;
      boundingBoxes[97] = new Rectangle(41, -18, 581, 518);
      width[98] = 549;
      boundingBoxes[98] = new Rectangle(61, -223, 454, 964);
      width[99] = 549;
      boundingBoxes[99] = new Rectangle(12, -231, 510, 730);
      width[100] = 494;
      boundingBoxes[100] = new Rectangle(40, -19, 441, 759);
      width[101] = 439;
      boundingBoxes[101] = new Rectangle(22, -19, 405, 521);
      width[102] = 521;
      boundingBoxes[102] = new Rectangle(28, -224, 464, 897);
      width[103] = 411;
      boundingBoxes[103] = new Rectangle(5, -225, 479, 724);
      width[104] = 603;
      boundingBoxes[104] = new Rectangle(0, -202, 527, 716);
      width[105] = 329;
      boundingBoxes[105] = new Rectangle(0, -17, 301, 520);
      width[106] = 603;
      boundingBoxes[106] = new Rectangle(36, -224, 551, 723);
      width[107] = 549;
      boundingBoxes[107] = new Rectangle(33, 0, 525, 501);
      width[108] = 549;
      boundingBoxes[108] = new Rectangle(24, -17, 524, 756);
      width[109] = 576;
      boundingBoxes[109] = new Rectangle(33, -223, 534, 723);
      width[110] = 521;
      boundingBoxes[110] = new Rectangle(-9, -16, 484, 523);
      width[111] = 549;
      boundingBoxes[111] = new Rectangle(35, -19, 466, 518);
      width[112] = 549;
      boundingBoxes[112] = new Rectangle(10, -19, 520, 506);
      width[113] = 521;
      boundingBoxes[113] = new Rectangle(43, -17, 442, 707);
      width[114] = 549;
      boundingBoxes[114] = new Rectangle(50, -230, 440, 729);
      width[115] = 603;
      boundingBoxes[115] = new Rectangle(30, -21, 558, 521);
      width[116] = 439;
      boundingBoxes[116] = new Rectangle(10, -19, 408, 519);
      width[117] = 576;
      boundingBoxes[117] = new Rectangle(7, -18, 528, 525);
      width[118] = 713;
      boundingBoxes[118] = new Rectangle(12, -18, 659, 601);
      width[119] = 686;
      boundingBoxes[119] = new Rectangle(42, -17, 642, 517);
      width[120] = 493;
      boundingBoxes[120] = new Rectangle(27, -224, 442, 990);
      width[121] = 686;
      boundingBoxes[121] = new Rectangle(12, -228, 689, 728);
      width[122] = 494;
      boundingBoxes[122] = new Rectangle(60, -225, 407, 981);
      width[123] = 480;
      boundingBoxes[123] = new Rectangle(58, -183, 339, 856);
      width[124] = 200;
      boundingBoxes[124] = new Rectangle(65, -293, 70, 1000);
      width[125] = 480;
      boundingBoxes[125] = new Rectangle(79, -183, 339, 856);
      width[126] = 549;
      boundingBoxes[126] = new Rectangle(17, 203, 512, 104);
      width[160] = 750;
      boundingBoxes[160] = new Rectangle(20, -12, 694, 697);
      width[161] = 620;
      boundingBoxes[161] = new Rectangle(-2, 0, 612, 685);
      width[162] = 247;
      boundingBoxes[162] = new Rectangle(27, 459, 201, 276);
      width[163] = 549;
      boundingBoxes[163] = new Rectangle(29, 0, 497, 639);
      width[164] = 167;
      boundingBoxes[164] = new Rectangle(-180, -12, 520, 689);
      width[165] = 713;
      boundingBoxes[165] = new Rectangle(26, 124, 662, 280);
      width[166] = 500;
      boundingBoxes[166] = new Rectangle(2, -193, 492, 879);
      width[167] = 753;
      boundingBoxes[167] = new Rectangle(86, -26, 574, 559);
      width[168] = 753;
      boundingBoxes[168] = new Rectangle(142, -36, 458, 586);
      width[169] = 753;
      boundingBoxes[169] = new Rectangle(117, -33, 514, 565);
      width[170] = 753;
      boundingBoxes[170] = new Rectangle(113, -36, 516, 584);
      width[171] = 1042;
      boundingBoxes[171] = new Rectangle(24, -15, 1000, 526);
      width[172] = 987;
      boundingBoxes[172] = new Rectangle(32, -15, 910, 526);
      width[173] = 603;
      boundingBoxes[173] = new Rectangle(45, 0, 526, 910);
      width[174] = 987;
      boundingBoxes[174] = new Rectangle(49, -15, 910, 526);
      width[175] = 603;
      boundingBoxes[175] = new Rectangle(45, -22, 526, 910);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(50, 385, 300, 300);
      width[177] = 549;
      boundingBoxes[177] = new Rectangle(10, 0, 529, 645);
      width[178] = 411;
      boundingBoxes[178] = new Rectangle(20, 459, 393, 278);
      width[179] = 549;
      boundingBoxes[179] = new Rectangle(29, 0, 497, 639);
      width[180] = 549;
      boundingBoxes[180] = new Rectangle(17, 8, 516, 516);
      width[181] = 713;
      boundingBoxes[181] = new Rectangle(27, 123, 612, 281);
      width[182] = 494;
      boundingBoxes[182] = new Rectangle(26, -20, 436, 766);
      width[183] = 460;
      boundingBoxes[183] = new Rectangle(50, 113, 360, 360);
      width[184] = 549;
      boundingBoxes[184] = new Rectangle(10, 71, 526, 385);
      width[185] = 549;
      boundingBoxes[185] = new Rectangle(15, -25, 525, 574);
      width[186] = 549;
      boundingBoxes[186] = new Rectangle(14, 82, 524, 361);
      width[187] = 549;
      boundingBoxes[187] = new Rectangle(14, 135, 513, 259);
      width[188] = 1000;
      boundingBoxes[188] = new Rectangle(111, -17, 778, 112);
      width[189] = 603;
      boundingBoxes[189] = new Rectangle(280, -120, 56, 1130);
      width[190] = 1000;
      boundingBoxes[190] = new Rectangle(-60, 220, 1110, 56);
      width[191] = 658;
      boundingBoxes[191] = new Rectangle(15, -16, 587, 645);
      width[192] = 823;
      boundingBoxes[192] = new Rectangle(175, -18, 486, 676);
      width[193] = 686;
      boundingBoxes[193] = new Rectangle(10, -53, 568, 793);
      width[194] = 795;
      boundingBoxes[194] = new Rectangle(26, -15, 733, 749);
      width[195] = 987;
      boundingBoxes[195] = new Rectangle(159, -211, 711, 784);
      width[196] = 768;
      boundingBoxes[196] = new Rectangle(43, -17, 690, 690);
      width[197] = 768;
      boundingBoxes[197] = new Rectangle(43, -15, 690, 690);
      width[198] = 823;
      boundingBoxes[198] = new Rectangle(39, -24, 742, 743);
      width[199] = 768;
      boundingBoxes[199] = new Rectangle(40, 0, 692, 509);
      width[200] = 768;
      boundingBoxes[200] = new Rectangle(40, -17, 692, 509);
      width[201] = 713;
      boundingBoxes[201] = new Rectangle(20, 0, 653, 470);
      width[202] = 713;
      boundingBoxes[202] = new Rectangle(20, -125, 653, 595);
      width[203] = 713;
      boundingBoxes[203] = new Rectangle(36, -70, 654, 610);
      width[204] = 713;
      boundingBoxes[204] = new Rectangle(37, 0, 653, 470);
      width[205] = 713;
      boundingBoxes[205] = new Rectangle(37, -125, 653, 595);
      width[206] = 713;
      boundingBoxes[206] = new Rectangle(45, 0, 460, 468);
      width[207] = 713;
      boundingBoxes[207] = new Rectangle(45, -58, 460, 613);
      width[208] = 768;
      boundingBoxes[208] = new Rectangle(26, 0, 712, 673);
      width[209] = 713;
      boundingBoxes[209] = new Rectangle(36, -19, 645, 737);
      width[210] = 790;
      boundingBoxes[210] = new Rectangle(50, -17, 690, 690);
      width[211] = 790;
      boundingBoxes[211] = new Rectangle(51, -15, 690, 690);
      width[212] = 890;
      boundingBoxes[212] = new Rectangle(18, 293, 837, 380);
      width[213] = 823;
      boundingBoxes[213] = new Rectangle(25, -101, 778, 852);
      width[214] = 549;
      boundingBoxes[214] = new Rectangle(10, -38, 505, 955);
      width[215] = 250;
      boundingBoxes[215] = new Rectangle(69, 210, 100, 100);
      width[216] = 713;
      boundingBoxes[216] = new Rectangle(15, 0, 665, 288);
      width[217] = 603;
      boundingBoxes[217] = new Rectangle(23, 0, 560, 454);
      width[218] = 603;
      boundingBoxes[218] = new Rectangle(30, 0, 548, 477);
      width[219] = 1042;
      boundingBoxes[219] = new Rectangle(27, -20, 996, 530);
      width[220] = 987;
      boundingBoxes[220] = new Rectangle(30, -15, 909, 528);
      width[221] = 603;
      boundingBoxes[221] = new Rectangle(39, 2, 528, 909);
      width[222] = 987;
      boundingBoxes[222] = new Rectangle(45, -20, 909, 528);
      width[223] = 603;
      boundingBoxes[223] = new Rectangle(44, -19, 528, 909);
      width[224] = 494;
      boundingBoxes[224] = new Rectangle(18, 0, 448, 745);
      width[225] = 329;
      boundingBoxes[225] = new Rectangle(25, -198, 281, 944);
      width[226] = 790;
      boundingBoxes[226] = new Rectangle(50, -20, 690, 690);
      width[227] = 790;
      boundingBoxes[227] = new Rectangle(49, -15, 690, 690);
      width[228] = 786;
      boundingBoxes[228] = new Rectangle(5, 293, 720, 380);
      width[229] = 713;
      boundingBoxes[229] = new Rectangle(14, -108, 681, 860);
      width[230] = 384;
      boundingBoxes[230] = new Rectangle(24, -293, 412, 1219);
      width[231] = 384;
      boundingBoxes[231] = new Rectangle(24, -85, 84, 1010);
      width[232] = 384;
      boundingBoxes[232] = new Rectangle(24, -293, 412, 1219);
      width[233] = 384;
      boundingBoxes[233] = new Rectangle(0, -80, 349, 1006);
      width[234] = 384;
      boundingBoxes[234] = new Rectangle(0, -79, 77, 1004);
      width[235] = 384;
      boundingBoxes[235] = new Rectangle(0, -80, 349, 1006);
      width[236] = 494;
      boundingBoxes[236] = new Rectangle(209, -85, 236, 1010);
      width[237] = 494;
      boundingBoxes[237] = new Rectangle(20, -85, 264, 1020);
      width[238] = 494;
      boundingBoxes[238] = new Rectangle(209, -75, 236, 1010);
      width[239] = 494;
      boundingBoxes[239] = new Rectangle(209, -85, 75, 1020);
      width[241] = 329;
      boundingBoxes[241] = new Rectangle(21, -198, 281, 944);
      width[242] = 274;
      boundingBoxes[242] = new Rectangle(2, -107, 289, 1023);
      width[243] = 686;
      boundingBoxes[243] = new Rectangle(308, -88, 367, 1008);
      width[244] = 686;
      boundingBoxes[244] = new Rectangle(308, -88, 70, 1063);
      width[245] = 686;
      boundingBoxes[245] = new Rectangle(11, -87, 367, 1008);
      width[246] = 384;
      boundingBoxes[246] = new Rectangle(54, -293, 412, 1219);
      width[247] = 384;
      boundingBoxes[247] = new Rectangle(382, -85, 84, 1010);
      width[248] = 384;
      boundingBoxes[248] = new Rectangle(54, -293, 412, 1219);
      width[249] = 384;
      boundingBoxes[249] = new Rectangle(22, -80, 349, 1006);
      width[250] = 384;
      boundingBoxes[250] = new Rectangle(294, -79, 77, 1004);
      width[251] = 384;
      boundingBoxes[251] = new Rectangle(22, -80, 349, 1006);
      width[252] = 494;
      boundingBoxes[252] = new Rectangle(48, -85, 236, 1010);
      width[253] = 494;
      boundingBoxes[253] = new Rectangle(209, -85, 264, 1020);
      width[254] = 494;
      boundingBoxes[254] = new Rectangle(48, -75, 236, 1010);
      familyNames = new HashSet();
      familyNames.add("Symbol");
   }
}
