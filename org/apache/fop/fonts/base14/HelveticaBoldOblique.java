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

public class HelveticaBoldOblique extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Helvetica-BoldOblique";
   private static final String fullName = "Helvetica Bold Oblique";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 718;
   private static final int xHeight = 532;
   private static final int ascender = 718;
   private static final int descender = -207;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private static final Map kerning;
   private boolean enableKerning;

   public HelveticaBoldOblique() {
      this(false);
   }

   public HelveticaBoldOblique(boolean enableKerning) {
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
      return "Helvetica-BoldOblique";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Helvetica Bold Oblique";
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
      return size * 532;
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
         uri = new URI("base14:" + "Helvetica-BoldOblique".toLowerCase());
      } catch (URISyntaxException var4) {
         throw new RuntimeException(var4);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 722;
      boundingBoxes[65] = new Rectangle(20, 0, 682, 718);
      width[198] = 1000;
      boundingBoxes[198] = new Rectangle(5, 0, 1095, 718);
      width[193] = 722;
      boundingBoxes[193] = new Rectangle(20, 0, 730, 936);
      width[194] = 722;
      boundingBoxes[194] = new Rectangle(20, 0, 686, 936);
      width[196] = 722;
      boundingBoxes[196] = new Rectangle(20, 0, 696, 915);
      width[192] = 722;
      boundingBoxes[192] = new Rectangle(20, 0, 682, 936);
      width[197] = 722;
      boundingBoxes[197] = new Rectangle(20, 0, 682, 962);
      width[195] = 722;
      boundingBoxes[195] = new Rectangle(20, 0, 721, 923);
      width[66] = 722;
      boundingBoxes[66] = new Rectangle(76, 0, 688, 718);
      width[67] = 722;
      boundingBoxes[67] = new Rectangle(107, -19, 682, 756);
      width[199] = 722;
      boundingBoxes[199] = new Rectangle(107, -228, 682, 965);
      width[68] = 722;
      boundingBoxes[68] = new Rectangle(76, 0, 701, 718);
      width[69] = 667;
      boundingBoxes[69] = new Rectangle(76, 0, 681, 718);
      width[201] = 667;
      boundingBoxes[201] = new Rectangle(76, 0, 681, 936);
      width[202] = 667;
      boundingBoxes[202] = new Rectangle(76, 0, 681, 936);
      width[203] = 667;
      boundingBoxes[203] = new Rectangle(76, 0, 681, 915);
      width[200] = 667;
      boundingBoxes[200] = new Rectangle(76, 0, 681, 936);
      width[208] = 722;
      boundingBoxes[208] = new Rectangle(62, 0, 715, 718);
      width[128] = 556;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 611;
      boundingBoxes[70] = new Rectangle(76, 0, 664, 718);
      width[71] = 778;
      boundingBoxes[71] = new Rectangle(108, -19, 709, 756);
      width[72] = 722;
      boundingBoxes[72] = new Rectangle(71, 0, 733, 718);
      width[73] = 278;
      boundingBoxes[73] = new Rectangle(64, 0, 303, 718);
      width[205] = 278;
      boundingBoxes[205] = new Rectangle(64, 0, 464, 936);
      width[206] = 278;
      boundingBoxes[206] = new Rectangle(64, 0, 420, 936);
      width[207] = 278;
      boundingBoxes[207] = new Rectangle(64, 0, 430, 915);
      width[204] = 278;
      boundingBoxes[204] = new Rectangle(64, 0, 303, 936);
      width[74] = 556;
      boundingBoxes[74] = new Rectangle(60, -18, 577, 736);
      width[75] = 722;
      boundingBoxes[75] = new Rectangle(87, 0, 771, 718);
      width[76] = 611;
      boundingBoxes[76] = new Rectangle(76, 0, 535, 718);
      width[77] = 833;
      boundingBoxes[77] = new Rectangle(69, 0, 849, 718);
      width[78] = 722;
      boundingBoxes[78] = new Rectangle(69, 0, 738, 718);
      width[209] = 722;
      boundingBoxes[209] = new Rectangle(69, 0, 738, 923);
      width[79] = 778;
      boundingBoxes[79] = new Rectangle(107, -19, 716, 756);
      width[140] = 1000;
      boundingBoxes[140] = new Rectangle(99, -19, 1015, 756);
      width[211] = 778;
      boundingBoxes[211] = new Rectangle(107, -19, 716, 955);
      width[212] = 778;
      boundingBoxes[212] = new Rectangle(107, -19, 716, 955);
      width[214] = 778;
      boundingBoxes[214] = new Rectangle(107, -19, 716, 934);
      width[210] = 778;
      boundingBoxes[210] = new Rectangle(107, -19, 716, 955);
      width[216] = 778;
      boundingBoxes[216] = new Rectangle(35, -27, 859, 772);
      width[213] = 778;
      boundingBoxes[213] = new Rectangle(107, -19, 716, 942);
      width[80] = 667;
      boundingBoxes[80] = new Rectangle(76, 0, 662, 718);
      width[81] = 778;
      boundingBoxes[81] = new Rectangle(107, -52, 716, 789);
      width[82] = 722;
      boundingBoxes[82] = new Rectangle(76, 0, 702, 718);
      width[83] = 667;
      boundingBoxes[83] = new Rectangle(81, -19, 637, 756);
      width[138] = 667;
      boundingBoxes[138] = new Rectangle(81, -19, 637, 955);
      width[84] = 611;
      boundingBoxes[84] = new Rectangle(140, 0, 611, 718);
      width[222] = 667;
      boundingBoxes[222] = new Rectangle(76, 0, 640, 718);
      width[85] = 722;
      boundingBoxes[85] = new Rectangle(116, -19, 688, 737);
      width[218] = 722;
      boundingBoxes[218] = new Rectangle(116, -19, 688, 955);
      width[219] = 722;
      boundingBoxes[219] = new Rectangle(116, -19, 688, 955);
      width[220] = 722;
      boundingBoxes[220] = new Rectangle(116, -19, 688, 934);
      width[217] = 722;
      boundingBoxes[217] = new Rectangle(116, -19, 688, 955);
      width[86] = 667;
      boundingBoxes[86] = new Rectangle(172, 0, 629, 718);
      width[87] = 944;
      boundingBoxes[87] = new Rectangle(169, 0, 913, 718);
      width[88] = 667;
      boundingBoxes[88] = new Rectangle(14, 0, 777, 718);
      width[89] = 667;
      boundingBoxes[89] = new Rectangle(168, 0, 638, 718);
      width[221] = 667;
      boundingBoxes[221] = new Rectangle(168, 0, 638, 936);
      width[159] = 667;
      boundingBoxes[159] = new Rectangle(168, 0, 638, 915);
      width[90] = 611;
      boundingBoxes[90] = new Rectangle(25, 0, 712, 718);
      width[142] = 611;
      boundingBoxes[142] = new Rectangle(25, 0, 712, 936);
      width[97] = 556;
      boundingBoxes[97] = new Rectangle(55, -14, 528, 560);
      width[225] = 556;
      boundingBoxes[225] = new Rectangle(55, -14, 572, 764);
      width[226] = 556;
      boundingBoxes[226] = new Rectangle(55, -14, 528, 764);
      width[180] = 333;
      boundingBoxes[180] = new Rectangle(236, 604, 279, 146);
      width[228] = 556;
      boundingBoxes[228] = new Rectangle(55, -14, 539, 743);
      width[230] = 889;
      boundingBoxes[230] = new Rectangle(56, -14, 867, 560);
      width[224] = 556;
      boundingBoxes[224] = new Rectangle(55, -14, 528, 764);
      width[38] = 722;
      boundingBoxes[38] = new Rectangle(89, -19, 643, 737);
      width[229] = 556;
      boundingBoxes[229] = new Rectangle(55, -14, 528, 790);
      width[94] = 584;
      boundingBoxes[94] = new Rectangle(131, 323, 460, 375);
      width[126] = 584;
      boundingBoxes[126] = new Rectangle(115, 163, 462, 180);
      width[42] = 389;
      boundingBoxes[42] = new Rectangle(146, 387, 335, 331);
      width[64] = 975;
      boundingBoxes[64] = new Rectangle(186, -19, 768, 756);
      width[227] = 556;
      boundingBoxes[227] = new Rectangle(55, -14, 564, 751);
      width[98] = 611;
      boundingBoxes[98] = new Rectangle(61, -14, 584, 732);
      width[92] = 278;
      boundingBoxes[92] = new Rectangle(124, -19, 183, 756);
      width[124] = 280;
      boundingBoxes[124] = new Rectangle(36, -225, 325, 1000);
      width[123] = 389;
      boundingBoxes[123] = new Rectangle(94, -196, 424, 918);
      width[125] = 389;
      boundingBoxes[125] = new Rectangle(-18, -196, 425, 918);
      width[91] = 333;
      boundingBoxes[91] = new Rectangle(21, -196, 441, 918);
      width[93] = 333;
      boundingBoxes[93] = new Rectangle(-18, -196, 441, 918);
      width[166] = 280;
      boundingBoxes[166] = new Rectangle(52, -150, 293, 850);
      width[149] = 350;
      boundingBoxes[149] = new Rectangle(83, 194, 337, 330);
      width[99] = 556;
      boundingBoxes[99] = new Rectangle(79, -14, 520, 560);
      width[231] = 556;
      boundingBoxes[231] = new Rectangle(79, -228, 520, 774);
      width[184] = 333;
      boundingBoxes[184] = new Rectangle(-37, -228, 257, 228);
      width[162] = 556;
      boundingBoxes[162] = new Rectangle(79, -118, 520, 746);
      width[136] = 333;
      boundingBoxes[136] = new Rectangle(118, 604, 353, 146);
      width[58] = 333;
      boundingBoxes[58] = new Rectangle(92, 0, 259, 512);
      width[44] = 278;
      boundingBoxes[44] = new Rectangle(28, -168, 217, 314);
      width[169] = 737;
      boundingBoxes[169] = new Rectangle(56, -19, 779, 756);
      width[164] = 556;
      boundingBoxes[164] = new Rectangle(27, 76, 653, 560);
      width[100] = 611;
      boundingBoxes[100] = new Rectangle(82, -14, 622, 732);
      width[134] = 556;
      boundingBoxes[134] = new Rectangle(118, -171, 508, 889);
      width[135] = 556;
      boundingBoxes[135] = new Rectangle(46, -171, 582, 889);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(175, 426, 292, 286);
      width[168] = 333;
      boundingBoxes[168] = new Rectangle(137, 614, 345, 115);
      width[247] = 584;
      boundingBoxes[247] = new Rectangle(82, -42, 528, 590);
      width[36] = 556;
      boundingBoxes[36] = new Rectangle(67, -115, 555, 890);
      width[101] = 556;
      boundingBoxes[101] = new Rectangle(70, -14, 523, 560);
      width[233] = 556;
      boundingBoxes[233] = new Rectangle(70, -14, 557, 764);
      width[234] = 556;
      boundingBoxes[234] = new Rectangle(70, -14, 523, 764);
      width[235] = 556;
      boundingBoxes[235] = new Rectangle(70, -14, 524, 743);
      width[232] = 556;
      boundingBoxes[232] = new Rectangle(70, -14, 523, 764);
      width[56] = 556;
      boundingBoxes[56] = new Rectangle(69, -19, 547, 729);
      width[133] = 1000;
      boundingBoxes[133] = new Rectangle(92, 0, 847, 146);
      width[151] = 1000;
      boundingBoxes[151] = new Rectangle(48, 227, 1023, 106);
      width[150] = 556;
      boundingBoxes[150] = new Rectangle(48, 227, 579, 106);
      width[61] = 584;
      boundingBoxes[61] = new Rectangle(58, 87, 575, 332);
      width[240] = 611;
      boundingBoxes[240] = new Rectangle(82, -14, 588, 751);
      width[33] = 333;
      boundingBoxes[33] = new Rectangle(94, 0, 303, 718);
      width[161] = 333;
      boundingBoxes[161] = new Rectangle(50, -186, 303, 718);
      width[102] = 333;
      boundingBoxes[102] = new Rectangle(87, 0, 382, 727);
      width[53] = 556;
      boundingBoxes[53] = new Rectangle(64, -19, 572, 717);
      width[131] = 556;
      boundingBoxes[131] = new Rectangle(-50, -210, 719, 947);
      width[52] = 556;
      boundingBoxes[52] = new Rectangle(60, 0, 538, 710);
      width[103] = 611;
      boundingBoxes[103] = new Rectangle(38, -217, 628, 763);
      width[223] = 611;
      boundingBoxes[223] = new Rectangle(69, -14, 588, 745);
      width[96] = 333;
      boundingBoxes[96] = new Rectangle(136, 604, 217, 146);
      width[62] = 584;
      boundingBoxes[62] = new Rectangle(36, -8, 573, 522);
      width[171] = 556;
      boundingBoxes[171] = new Rectangle(135, 76, 436, 408);
      width[187] = 556;
      boundingBoxes[187] = new Rectangle(104, 76, 436, 408);
      width[139] = 333;
      boundingBoxes[139] = new Rectangle(130, 76, 223, 408);
      width[155] = 333;
      boundingBoxes[155] = new Rectangle(99, 76, 223, 408);
      width[104] = 611;
      boundingBoxes[104] = new Rectangle(65, 0, 564, 718);
      width[45] = 333;
      boundingBoxes[45] = new Rectangle(73, 215, 306, 130);
      width[105] = 278;
      boundingBoxes[105] = new Rectangle(69, 0, 294, 725);
      width[237] = 278;
      boundingBoxes[237] = new Rectangle(69, 0, 419, 750);
      width[238] = 278;
      boundingBoxes[238] = new Rectangle(69, 0, 375, 750);
      width[239] = 278;
      boundingBoxes[239] = new Rectangle(69, 0, 386, 729);
      width[236] = 278;
      boundingBoxes[236] = new Rectangle(69, 0, 257, 750);
      width[106] = 278;
      boundingBoxes[106] = new Rectangle(-42, -214, 405, 939);
      width[107] = 556;
      boundingBoxes[107] = new Rectangle(69, 0, 601, 718);
      width[108] = 278;
      boundingBoxes[108] = new Rectangle(69, 0, 293, 718);
      width[60] = 584;
      boundingBoxes[60] = new Rectangle(82, -8, 573, 522);
      width[172] = 584;
      boundingBoxes[172] = new Rectangle(105, 108, 528, 311);
      width[109] = 889;
      boundingBoxes[109] = new Rectangle(64, 0, 845, 546);
      width[175] = 333;
      boundingBoxes[175] = new Rectangle(122, 604, 361, 74);
      width[181] = 611;
      boundingBoxes[181] = new Rectangle(22, -207, 636, 739);
      width[215] = 584;
      boundingBoxes[215] = new Rectangle(57, 1, 578, 504);
      width[110] = 611;
      boundingBoxes[110] = new Rectangle(65, 0, 564, 546);
      width[57] = 556;
      boundingBoxes[57] = new Rectangle(78, -19, 537, 729);
      width[241] = 611;
      boundingBoxes[241] = new Rectangle(65, 0, 581, 737);
      width[35] = 556;
      boundingBoxes[35] = new Rectangle(60, 0, 584, 698);
      width[111] = 611;
      boundingBoxes[111] = new Rectangle(82, -14, 561, 560);
      width[243] = 611;
      boundingBoxes[243] = new Rectangle(82, -14, 572, 764);
      width[244] = 611;
      boundingBoxes[244] = new Rectangle(82, -14, 561, 764);
      width[246] = 611;
      boundingBoxes[246] = new Rectangle(82, -14, 561, 743);
      width[156] = 944;
      boundingBoxes[156] = new Rectangle(82, -14, 895, 560);
      width[242] = 611;
      boundingBoxes[242] = new Rectangle(82, -14, 561, 764);
      width[49] = 556;
      boundingBoxes[49] = new Rectangle(173, 0, 356, 710);
      width[189] = 834;
      boundingBoxes[189] = new Rectangle(132, -19, 726, 729);
      width[188] = 834;
      boundingBoxes[188] = new Rectangle(132, -19, 674, 729);
      width[185] = 333;
      boundingBoxes[185] = new Rectangle(148, 283, 240, 427);
      width[170] = 370;
      boundingBoxes[170] = new Rectangle(125, 401, 340, 336);
      width[186] = 365;
      boundingBoxes[186] = new Rectangle(123, 401, 362, 336);
      width[248] = 611;
      boundingBoxes[248] = new Rectangle(22, -29, 679, 589);
      width[245] = 611;
      boundingBoxes[245] = new Rectangle(82, -14, 564, 751);
      width[112] = 611;
      boundingBoxes[112] = new Rectangle(18, -207, 627, 753);
      width[182] = 556;
      boundingBoxes[182] = new Rectangle(98, -191, 590, 891);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(76, -208, 394, 942);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(-25, -208, 394, 942);
      width[37] = 889;
      boundingBoxes[37] = new Rectangle(136, -19, 765, 729);
      width[46] = 278;
      boundingBoxes[46] = new Rectangle(64, 0, 181, 146);
      width[183] = 278;
      boundingBoxes[183] = new Rectangle(110, 172, 166, 162);
      width[137] = 1000;
      boundingBoxes[137] = new Rectangle(76, -19, 962, 729);
      width[43] = 584;
      boundingBoxes[43] = new Rectangle(82, 0, 528, 506);
      width[177] = 584;
      boundingBoxes[177] = new Rectangle(40, 0, 585, 506);
      width[113] = 611;
      boundingBoxes[113] = new Rectangle(80, -207, 585, 753);
      width[63] = 611;
      boundingBoxes[63] = new Rectangle(165, 0, 506, 727);
      width[191] = 611;
      boundingBoxes[191] = new Rectangle(53, -195, 506, 727);
      width[34] = 474;
      boundingBoxes[34] = new Rectangle(193, 447, 336, 271);
      width[132] = 500;
      boundingBoxes[132] = new Rectangle(36, -146, 427, 273);
      width[147] = 500;
      boundingBoxes[147] = new Rectangle(160, 454, 428, 273);
      width[148] = 500;
      boundingBoxes[148] = new Rectangle(162, 445, 427, 273);
      width[145] = 278;
      boundingBoxes[145] = new Rectangle(165, 454, 196, 273);
      width[146] = 278;
      boundingBoxes[146] = new Rectangle(167, 445, 195, 273);
      width[130] = 278;
      boundingBoxes[130] = new Rectangle(41, -146, 195, 273);
      width[39] = 238;
      boundingBoxes[39] = new Rectangle(165, 447, 156, 271);
      width[114] = 389;
      boundingBoxes[114] = new Rectangle(64, 0, 425, 546);
      width[174] = 737;
      boundingBoxes[174] = new Rectangle(55, -19, 779, 756);
      width[115] = 556;
      boundingBoxes[115] = new Rectangle(63, -14, 521, 560);
      width[154] = 556;
      boundingBoxes[154] = new Rectangle(63, -14, 551, 764);
      width[167] = 556;
      boundingBoxes[167] = new Rectangle(61, -184, 537, 911);
      width[59] = 333;
      boundingBoxes[59] = new Rectangle(56, -168, 295, 680);
      width[55] = 556;
      boundingBoxes[55] = new Rectangle(125, 0, 551, 698);
      width[54] = 556;
      boundingBoxes[54] = new Rectangle(85, -19, 534, 729);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(-37, -19, 505, 756);
      width[32] = 278;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 556;
      boundingBoxes[163] = new Rectangle(50, -16, 585, 734);
      width[116] = 333;
      boundingBoxes[116] = new Rectangle(100, -6, 322, 682);
      width[254] = 611;
      boundingBoxes[254] = new Rectangle(18, -208, 627, 926);
      width[51] = 556;
      boundingBoxes[51] = new Rectangle(65, -19, 543, 729);
      width[190] = 834;
      boundingBoxes[190] = new Rectangle(99, -19, 740, 729);
      width[179] = 333;
      boundingBoxes[179] = new Rectangle(91, 271, 350, 439);
      width[152] = 333;
      boundingBoxes[152] = new Rectangle(113, 610, 394, 127);
      width[153] = 1000;
      boundingBoxes[153] = new Rectangle(179, 306, 930, 412);
      width[50] = 556;
      boundingBoxes[50] = new Rectangle(26, 0, 593, 710);
      width[178] = 333;
      boundingBoxes[178] = new Rectangle(69, 283, 380, 427);
      width[117] = 611;
      boundingBoxes[117] = new Rectangle(98, -14, 560, 546);
      width[250] = 611;
      boundingBoxes[250] = new Rectangle(98, -14, 560, 764);
      width[251] = 611;
      boundingBoxes[251] = new Rectangle(98, -14, 560, 764);
      width[252] = 611;
      boundingBoxes[252] = new Rectangle(98, -14, 560, 743);
      width[249] = 611;
      boundingBoxes[249] = new Rectangle(98, -14, 560, 764);
      width[95] = 556;
      boundingBoxes[95] = new Rectangle(-27, -125, 567, 50);
      width[118] = 556;
      boundingBoxes[118] = new Rectangle(126, 0, 530, 532);
      width[119] = 778;
      boundingBoxes[119] = new Rectangle(123, 0, 759, 532);
      width[120] = 556;
      boundingBoxes[120] = new Rectangle(15, 0, 633, 532);
      width[121] = 556;
      boundingBoxes[121] = new Rectangle(42, -214, 610, 746);
      width[253] = 556;
      boundingBoxes[253] = new Rectangle(42, -214, 610, 964);
      width[255] = 556;
      boundingBoxes[255] = new Rectangle(42, -214, 610, 943);
      width[165] = 556;
      boundingBoxes[165] = new Rectangle(60, 0, 653, 698);
      width[122] = 500;
      boundingBoxes[122] = new Rectangle(20, 0, 563, 532);
      width[158] = 500;
      boundingBoxes[158] = new Rectangle(20, 0, 566, 750);
      width[48] = 556;
      boundingBoxes[48] = new Rectangle(86, -19, 531, 729);
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
      ((Map)pairs).put(second, -15);
      first = 79;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -50);
      second = 87;
      ((Map)pairs).put(second, -50);
      second = 89;
      ((Map)pairs).put(second, -70);
      second = 84;
      ((Map)pairs).put(second, -40);
      second = 46;
      ((Map)pairs).put(second, -40);
      second = 86;
      ((Map)pairs).put(second, -50);
      second = 88;
      ((Map)pairs).put(second, -50);
      second = 44;
      ((Map)pairs).put(second, -40);
      first = 104;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, -20);
      first = 99;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 107;
      ((Map)pairs).put(second, -20);
      second = 104;
      ((Map)pairs).put(second, -10);
      second = 121;
      ((Map)pairs).put(second, -10);
      second = 108;
      ((Map)pairs).put(second, -20);
      first = 87;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -60);
      second = 45;
      ((Map)pairs).put(second, -40);
      second = 79;
      ((Map)pairs).put(second, -20);
      second = 58;
      ((Map)pairs).put(second, -10);
      second = 97;
      ((Map)pairs).put(second, -40);
      second = 65;
      ((Map)pairs).put(second, -60);
      second = 117;
      ((Map)pairs).put(second, -45);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -80);
      second = 101;
      ((Map)pairs).put(second, -35);
      second = 59;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -80);
      first = 112;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, -15);
      first = 80;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -40);
      second = 97;
      ((Map)pairs).put(second, -30);
      second = 65;
      ((Map)pairs).put(second, -100);
      second = 46;
      ((Map)pairs).put(second, -120);
      second = 101;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -120);
      first = 86;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -90);
      second = 45;
      ((Map)pairs).put(second, -80);
      second = 79;
      ((Map)pairs).put(second, -50);
      second = 58;
      ((Map)pairs).put(second, -40);
      second = 97;
      ((Map)pairs).put(second, -60);
      second = 65;
      ((Map)pairs).put(second, -80);
      second = 117;
      ((Map)pairs).put(second, -60);
      second = 46;
      ((Map)pairs).put(second, -120);
      second = 71;
      ((Map)pairs).put(second, -50);
      second = 101;
      ((Map)pairs).put(second, -50);
      second = 59;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, -120);
      first = 59;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, -40);
      first = 118;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -30);
      second = 97;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -80);
      second = 44;
      ((Map)pairs).put(second, -80);
      first = 32;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 87;
      ((Map)pairs).put(second, -80);
      second = 147;
      ((Map)pairs).put(second, -80);
      second = 89;
      ((Map)pairs).put(second, -120);
      second = 84;
      ((Map)pairs).put(second, -100);
      second = 145;
      ((Map)pairs).put(second, -60);
      second = 86;
      ((Map)pairs).put(second, -80);
      first = 97;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 103;
      ((Map)pairs).put(second, -10);
      second = 118;
      ((Map)pairs).put(second, -15);
      first = 65;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -40);
      second = 119;
      ((Map)pairs).put(second, -30);
      second = 87;
      ((Map)pairs).put(second, -60);
      second = 67;
      ((Map)pairs).put(second, -40);
      second = 81;
      ((Map)pairs).put(second, -40);
      second = 71;
      ((Map)pairs).put(second, -50);
      second = 86;
      ((Map)pairs).put(second, -80);
      second = 118;
      ((Map)pairs).put(second, -40);
      second = 85;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -30);
      second = 89;
      ((Map)pairs).put(second, -110);
      second = 84;
      ((Map)pairs).put(second, -90);
      second = 121;
      ((Map)pairs).put(second, -30);
      first = 70;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 97;
      ((Map)pairs).put(second, -20);
      second = 65;
      ((Map)pairs).put(second, -80);
      second = 46;
      ((Map)pairs).put(second, -100);
      second = 44;
      ((Map)pairs).put(second, -100);
      first = 85;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -50);
      second = 46;
      ((Map)pairs).put(second, -30);
      second = 44;
      ((Map)pairs).put(second, -30);
      first = 115;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -15);
      first = 111;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 120;
      ((Map)pairs).put(second, -30);
      second = 118;
      ((Map)pairs).put(second, -20);
      first = 122;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 101;
      ((Map)pairs).put(second, 10);
      first = 100;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 100;
      ((Map)pairs).put(second, -10);
      second = 119;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -15);
      second = 118;
      ((Map)pairs).put(second, -15);
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
      ((Map)pairs).put(second, -70);
      second = 46;
      ((Map)pairs).put(second, -30);
      second = 86;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, -30);
      first = 146;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 100;
      ((Map)pairs).put(second, -80);
      second = 32;
      ((Map)pairs).put(second, -80);
      second = 146;
      ((Map)pairs).put(second, -46);
      second = 114;
      ((Map)pairs).put(second, -40);
      second = 108;
      ((Map)pairs).put(second, -20);
      second = 115;
      ((Map)pairs).put(second, -60);
      second = 118;
      ((Map)pairs).put(second, -20);
      first = 82;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -20);
      second = 87;
      ((Map)pairs).put(second, -40);
      second = 85;
      ((Map)pairs).put(second, -20);
      second = 89;
      ((Map)pairs).put(second, -50);
      second = 84;
      ((Map)pairs).put(second, -20);
      second = 86;
      ((Map)pairs).put(second, -50);
      first = 75;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -35);
      second = 79;
      ((Map)pairs).put(second, -30);
      second = 117;
      ((Map)pairs).put(second, -30);
      second = 121;
      ((Map)pairs).put(second, -40);
      second = 101;
      ((Map)pairs).put(second, -15);
      first = 58;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, -40);
      first = 119;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -40);
      second = 44;
      ((Map)pairs).put(second, -40);
      first = 114;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -20);
      second = 100;
      ((Map)pairs).put(second, -20);
      second = 45;
      ((Map)pairs).put(second, -20);
      second = 99;
      ((Map)pairs).put(second, -20);
      second = 116;
      ((Map)pairs).put(second, 20);
      second = 121;
      ((Map)pairs).put(second, 10);
      second = 46;
      ((Map)pairs).put(second, -60);
      second = 103;
      ((Map)pairs).put(second, -15);
      second = 113;
      ((Map)pairs).put(second, -20);
      second = 115;
      ((Map)pairs).put(second, -15);
      second = 118;
      ((Map)pairs).put(second, 10);
      second = 44;
      ((Map)pairs).put(second, -60);
      first = 145;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 145;
      ((Map)pairs).put(second, -46);
      first = 108;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -15);
      first = 103;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 103;
      ((Map)pairs).put(second, -10);
      second = 101;
      ((Map)pairs).put(second, 10);
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
      second = 108;
      ((Map)pairs).put(second, -10);
      second = 118;
      ((Map)pairs).put(second, -20);
      first = 76;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -140);
      second = 146;
      ((Map)pairs).put(second, -140);
      second = 87;
      ((Map)pairs).put(second, -80);
      second = 89;
      ((Map)pairs).put(second, -120);
      second = 121;
      ((Map)pairs).put(second, -30);
      second = 84;
      ((Map)pairs).put(second, -90);
      second = 86;
      ((Map)pairs).put(second, -110);
      first = 81;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 85;
      ((Map)pairs).put(second, -10);
      second = 46;
      ((Map)pairs).put(second, 20);
      second = 44;
      ((Map)pairs).put(second, 20);
      first = 44;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -120);
      second = 32;
      ((Map)pairs).put(second, -40);
      second = 146;
      ((Map)pairs).put(second, -120);
      first = 148;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 32;
      ((Map)pairs).put(second, -80);
      first = 109;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, -20);
      second = 121;
      ((Map)pairs).put(second, -30);
      first = 102;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, 30);
      second = 111;
      ((Map)pairs).put(second, -20);
      second = 146;
      ((Map)pairs).put(second, 30);
      second = 46;
      ((Map)pairs).put(second, -10);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -10);
      first = 74;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -20);
      second = 117;
      ((Map)pairs).put(second, -20);
      second = 46;
      ((Map)pairs).put(second, -20);
      second = 44;
      ((Map)pairs).put(second, -20);
      first = 89;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -100);
      second = 79;
      ((Map)pairs).put(second, -70);
      second = 58;
      ((Map)pairs).put(second, -50);
      second = 97;
      ((Map)pairs).put(second, -90);
      second = 65;
      ((Map)pairs).put(second, -110);
      second = 117;
      ((Map)pairs).put(second, -100);
      second = 46;
      ((Map)pairs).put(second, -100);
      second = 101;
      ((Map)pairs).put(second, -80);
      second = 59;
      ((Map)pairs).put(second, -50);
      second = 44;
      ((Map)pairs).put(second, -100);
      first = 84;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -80);
      second = 79;
      ((Map)pairs).put(second, -40);
      second = 58;
      ((Map)pairs).put(second, -40);
      second = 119;
      ((Map)pairs).put(second, -60);
      second = 114;
      ((Map)pairs).put(second, -80);
      second = 44;
      ((Map)pairs).put(second, -80);
      second = 59;
      ((Map)pairs).put(second, -40);
      second = 45;
      ((Map)pairs).put(second, -120);
      second = 65;
      ((Map)pairs).put(second, -90);
      second = 97;
      ((Map)pairs).put(second, -80);
      second = 117;
      ((Map)pairs).put(second, -90);
      second = 121;
      ((Map)pairs).put(second, -60);
      second = 46;
      ((Map)pairs).put(second, -80);
      second = 101;
      ((Map)pairs).put(second, -60);
      first = 121;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -25);
      second = 97;
      ((Map)pairs).put(second, -30);
      second = 46;
      ((Map)pairs).put(second, -80);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -80);
      first = 46;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -120);
      second = 32;
      ((Map)pairs).put(second, -40);
      second = 146;
      ((Map)pairs).put(second, -120);
      first = 110;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, -10);
      second = 121;
      ((Map)pairs).put(second, -20);
      second = 118;
      ((Map)pairs).put(second, -40);
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
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -15);
      second = 46;
      ((Map)pairs).put(second, 20);
      second = 120;
      ((Map)pairs).put(second, -15);
      second = 118;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, 10);
   }
}
