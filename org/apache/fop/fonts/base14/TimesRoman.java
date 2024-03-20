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

public class TimesRoman extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Times-Roman";
   private static final String fullName = "Times Roman";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 662;
   private static final int xHeight = 450;
   private static final int ascender = 683;
   private static final int descender = -217;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private static final Map kerning;
   private boolean enableKerning;

   public TimesRoman() {
      this(false);
   }

   public TimesRoman(boolean enableKerning) {
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
      return "Times-Roman";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Times Roman";
   }

   public Set getFamilyNames() {
      return familyNames;
   }

   public FontType getFontType() {
      return FontType.TYPE1;
   }

   public int getAscender(int size) {
      return size * 683;
   }

   public int getCapHeight(int size) {
      return size * 662;
   }

   public int getDescender(int size) {
      return size * -217;
   }

   public int getXHeight(int size) {
      return size * 450;
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
         uri = new URI("base14:" + "Times-Roman".toLowerCase());
      } catch (URISyntaxException var4) {
         throw new RuntimeException(var4);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 722;
      boundingBoxes[65] = new Rectangle(15, 0, 691, 674);
      width[198] = 889;
      boundingBoxes[198] = new Rectangle(0, 0, 863, 662);
      width[193] = 722;
      boundingBoxes[193] = new Rectangle(15, 0, 691, 890);
      width[194] = 722;
      boundingBoxes[194] = new Rectangle(15, 0, 691, 886);
      width[196] = 722;
      boundingBoxes[196] = new Rectangle(15, 0, 691, 835);
      width[192] = 722;
      boundingBoxes[192] = new Rectangle(15, 0, 691, 890);
      width[197] = 722;
      boundingBoxes[197] = new Rectangle(15, 0, 691, 898);
      width[195] = 722;
      boundingBoxes[195] = new Rectangle(15, 0, 691, 850);
      width[66] = 667;
      boundingBoxes[66] = new Rectangle(17, 0, 576, 662);
      width[67] = 667;
      boundingBoxes[67] = new Rectangle(28, -14, 605, 690);
      width[199] = 667;
      boundingBoxes[199] = new Rectangle(28, -215, 605, 891);
      width[68] = 722;
      boundingBoxes[68] = new Rectangle(16, 0, 669, 662);
      width[69] = 611;
      boundingBoxes[69] = new Rectangle(12, 0, 585, 662);
      width[201] = 611;
      boundingBoxes[201] = new Rectangle(12, 0, 585, 890);
      width[202] = 611;
      boundingBoxes[202] = new Rectangle(12, 0, 585, 886);
      width[203] = 611;
      boundingBoxes[203] = new Rectangle(12, 0, 585, 835);
      width[200] = 611;
      boundingBoxes[200] = new Rectangle(12, 0, 585, 890);
      width[208] = 722;
      boundingBoxes[208] = new Rectangle(16, 0, 669, 662);
      width[128] = 500;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 556;
      boundingBoxes[70] = new Rectangle(12, 0, 534, 662);
      width[71] = 722;
      boundingBoxes[71] = new Rectangle(32, -14, 677, 690);
      width[72] = 722;
      boundingBoxes[72] = new Rectangle(19, 0, 683, 662);
      width[73] = 333;
      boundingBoxes[73] = new Rectangle(18, 0, 297, 662);
      width[205] = 333;
      boundingBoxes[205] = new Rectangle(18, 0, 299, 890);
      width[206] = 333;
      boundingBoxes[206] = new Rectangle(11, 0, 311, 886);
      width[207] = 333;
      boundingBoxes[207] = new Rectangle(18, 0, 297, 835);
      width[204] = 333;
      boundingBoxes[204] = new Rectangle(18, 0, 297, 890);
      width[74] = 389;
      boundingBoxes[74] = new Rectangle(10, -14, 360, 676);
      width[75] = 722;
      boundingBoxes[75] = new Rectangle(34, 0, 689, 662);
      width[76] = 611;
      boundingBoxes[76] = new Rectangle(12, 0, 586, 662);
      width[77] = 889;
      boundingBoxes[77] = new Rectangle(12, 0, 851, 662);
      width[78] = 722;
      boundingBoxes[78] = new Rectangle(12, -11, 695, 673);
      width[209] = 722;
      boundingBoxes[209] = new Rectangle(12, -11, 695, 861);
      width[79] = 722;
      boundingBoxes[79] = new Rectangle(34, -14, 654, 690);
      width[140] = 889;
      boundingBoxes[140] = new Rectangle(30, -6, 855, 674);
      width[211] = 722;
      boundingBoxes[211] = new Rectangle(34, -14, 654, 904);
      width[212] = 722;
      boundingBoxes[212] = new Rectangle(34, -14, 654, 900);
      width[214] = 722;
      boundingBoxes[214] = new Rectangle(34, -14, 654, 849);
      width[210] = 722;
      boundingBoxes[210] = new Rectangle(34, -14, 654, 904);
      width[216] = 722;
      boundingBoxes[216] = new Rectangle(34, -80, 654, 814);
      width[213] = 722;
      boundingBoxes[213] = new Rectangle(34, -14, 654, 864);
      width[80] = 556;
      boundingBoxes[80] = new Rectangle(16, 0, 526, 662);
      width[81] = 722;
      boundingBoxes[81] = new Rectangle(34, -178, 667, 854);
      width[82] = 667;
      boundingBoxes[82] = new Rectangle(17, 0, 642, 662);
      width[83] = 556;
      boundingBoxes[83] = new Rectangle(42, -14, 449, 690);
      width[138] = 556;
      boundingBoxes[138] = new Rectangle(42, -14, 449, 900);
      width[84] = 611;
      boundingBoxes[84] = new Rectangle(17, 0, 576, 662);
      width[222] = 556;
      boundingBoxes[222] = new Rectangle(16, 0, 526, 662);
      width[85] = 722;
      boundingBoxes[85] = new Rectangle(14, -14, 691, 676);
      width[218] = 722;
      boundingBoxes[218] = new Rectangle(14, -14, 691, 904);
      width[219] = 722;
      boundingBoxes[219] = new Rectangle(14, -14, 691, 900);
      width[220] = 722;
      boundingBoxes[220] = new Rectangle(14, -14, 691, 849);
      width[217] = 722;
      boundingBoxes[217] = new Rectangle(14, -14, 691, 904);
      width[86] = 722;
      boundingBoxes[86] = new Rectangle(16, -11, 681, 673);
      width[87] = 944;
      boundingBoxes[87] = new Rectangle(5, -11, 927, 673);
      width[88] = 722;
      boundingBoxes[88] = new Rectangle(10, 0, 694, 662);
      width[89] = 722;
      boundingBoxes[89] = new Rectangle(22, 0, 681, 662);
      width[221] = 722;
      boundingBoxes[221] = new Rectangle(22, 0, 681, 890);
      width[159] = 722;
      boundingBoxes[159] = new Rectangle(22, 0, 681, 835);
      width[90] = 611;
      boundingBoxes[90] = new Rectangle(9, 0, 588, 662);
      width[142] = 611;
      boundingBoxes[142] = new Rectangle(9, 0, 588, 886);
      width[97] = 444;
      boundingBoxes[97] = new Rectangle(37, -10, 405, 470);
      width[225] = 444;
      boundingBoxes[225] = new Rectangle(37, -10, 405, 688);
      width[226] = 444;
      boundingBoxes[226] = new Rectangle(37, -10, 405, 684);
      width[180] = 333;
      boundingBoxes[180] = new Rectangle(93, 507, 224, 171);
      width[228] = 444;
      boundingBoxes[228] = new Rectangle(37, -10, 405, 633);
      width[230] = 667;
      boundingBoxes[230] = new Rectangle(38, -10, 594, 470);
      width[224] = 444;
      boundingBoxes[224] = new Rectangle(37, -10, 405, 688);
      width[38] = 778;
      boundingBoxes[38] = new Rectangle(42, -13, 708, 689);
      width[229] = 444;
      boundingBoxes[229] = new Rectangle(37, -10, 405, 721);
      width[94] = 469;
      boundingBoxes[94] = new Rectangle(24, 297, 422, 365);
      width[126] = 541;
      boundingBoxes[126] = new Rectangle(40, 183, 462, 140);
      width[42] = 500;
      boundingBoxes[42] = new Rectangle(69, 265, 363, 411);
      width[64] = 921;
      boundingBoxes[64] = new Rectangle(116, -14, 693, 690);
      width[227] = 444;
      boundingBoxes[227] = new Rectangle(37, -10, 405, 648);
      width[98] = 500;
      boundingBoxes[98] = new Rectangle(3, -10, 465, 693);
      width[92] = 278;
      boundingBoxes[92] = new Rectangle(-9, -14, 296, 690);
      width[124] = 200;
      boundingBoxes[124] = new Rectangle(67, -218, 66, 1000);
      width[123] = 480;
      boundingBoxes[123] = new Rectangle(100, -181, 250, 861);
      width[125] = 480;
      boundingBoxes[125] = new Rectangle(130, -181, 250, 861);
      width[91] = 333;
      boundingBoxes[91] = new Rectangle(88, -156, 211, 818);
      width[93] = 333;
      boundingBoxes[93] = new Rectangle(34, -156, 211, 818);
      width[166] = 200;
      boundingBoxes[166] = new Rectangle(67, -143, 66, 850);
      width[149] = 350;
      boundingBoxes[149] = new Rectangle(40, 196, 270, 270);
      width[99] = 444;
      boundingBoxes[99] = new Rectangle(25, -10, 387, 470);
      width[231] = 444;
      boundingBoxes[231] = new Rectangle(25, -215, 387, 675);
      width[184] = 333;
      boundingBoxes[184] = new Rectangle(52, -215, 209, 215);
      width[162] = 500;
      boundingBoxes[162] = new Rectangle(53, -138, 395, 717);
      width[136] = 333;
      boundingBoxes[136] = new Rectangle(11, 507, 311, 167);
      width[58] = 278;
      boundingBoxes[58] = new Rectangle(81, -11, 111, 470);
      width[44] = 250;
      boundingBoxes[44] = new Rectangle(56, -141, 139, 243);
      width[169] = 760;
      boundingBoxes[169] = new Rectangle(38, -14, 684, 690);
      width[164] = 500;
      boundingBoxes[164] = new Rectangle(-22, 58, 544, 544);
      width[100] = 500;
      boundingBoxes[100] = new Rectangle(27, -10, 464, 693);
      width[134] = 500;
      boundingBoxes[134] = new Rectangle(59, -149, 383, 825);
      width[135] = 500;
      boundingBoxes[135] = new Rectangle(58, -153, 384, 829);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(57, 390, 286, 286);
      width[168] = 333;
      boundingBoxes[168] = new Rectangle(18, 581, 297, 100);
      width[247] = 564;
      boundingBoxes[247] = new Rectangle(30, -10, 504, 526);
      width[36] = 500;
      boundingBoxes[36] = new Rectangle(44, -87, 413, 814);
      width[101] = 444;
      boundingBoxes[101] = new Rectangle(25, -10, 399, 470);
      width[233] = 444;
      boundingBoxes[233] = new Rectangle(25, -10, 399, 688);
      width[234] = 444;
      boundingBoxes[234] = new Rectangle(25, -10, 399, 684);
      width[235] = 444;
      boundingBoxes[235] = new Rectangle(25, -10, 399, 633);
      width[232] = 444;
      boundingBoxes[232] = new Rectangle(25, -10, 399, 688);
      width[56] = 500;
      boundingBoxes[56] = new Rectangle(56, -14, 389, 690);
      width[133] = 1000;
      boundingBoxes[133] = new Rectangle(111, -11, 777, 111);
      width[151] = 1000;
      boundingBoxes[151] = new Rectangle(0, 201, 1000, 49);
      width[150] = 500;
      boundingBoxes[150] = new Rectangle(0, 201, 500, 49);
      width[61] = 564;
      boundingBoxes[61] = new Rectangle(30, 120, 504, 266);
      width[240] = 500;
      boundingBoxes[240] = new Rectangle(29, -10, 442, 696);
      width[33] = 333;
      boundingBoxes[33] = new Rectangle(130, -9, 108, 685);
      width[161] = 333;
      boundingBoxes[161] = new Rectangle(97, -218, 108, 685);
      width[102] = 333;
      boundingBoxes[102] = new Rectangle(20, 0, 363, 683);
      width[53] = 500;
      boundingBoxes[53] = new Rectangle(32, -14, 406, 702);
      width[131] = 500;
      boundingBoxes[131] = new Rectangle(7, -189, 483, 865);
      width[52] = 500;
      boundingBoxes[52] = new Rectangle(12, 0, 460, 676);
      width[103] = 500;
      boundingBoxes[103] = new Rectangle(28, -218, 442, 678);
      width[223] = 500;
      boundingBoxes[223] = new Rectangle(12, -9, 456, 692);
      width[96] = 333;
      boundingBoxes[96] = new Rectangle(19, 507, 223, 171);
      width[62] = 564;
      boundingBoxes[62] = new Rectangle(28, -8, 508, 522);
      width[171] = 500;
      boundingBoxes[171] = new Rectangle(42, 33, 414, 383);
      width[187] = 500;
      boundingBoxes[187] = new Rectangle(44, 33, 414, 383);
      width[139] = 333;
      boundingBoxes[139] = new Rectangle(63, 33, 222, 383);
      width[155] = 333;
      boundingBoxes[155] = new Rectangle(48, 33, 222, 383);
      width[104] = 500;
      boundingBoxes[104] = new Rectangle(9, 0, 478, 683);
      width[45] = 333;
      boundingBoxes[45] = new Rectangle(39, 194, 246, 63);
      width[105] = 278;
      boundingBoxes[105] = new Rectangle(16, 0, 237, 683);
      width[237] = 278;
      boundingBoxes[237] = new Rectangle(16, 0, 274, 678);
      width[238] = 278;
      boundingBoxes[238] = new Rectangle(-16, 0, 311, 674);
      width[239] = 278;
      boundingBoxes[239] = new Rectangle(-9, 0, 297, 623);
      width[236] = 278;
      boundingBoxes[236] = new Rectangle(-8, 0, 261, 678);
      width[106] = 278;
      boundingBoxes[106] = new Rectangle(-70, -218, 264, 901);
      width[107] = 500;
      boundingBoxes[107] = new Rectangle(7, 0, 498, 683);
      width[108] = 278;
      boundingBoxes[108] = new Rectangle(19, 0, 238, 683);
      width[60] = 564;
      boundingBoxes[60] = new Rectangle(28, -8, 508, 522);
      width[172] = 564;
      boundingBoxes[172] = new Rectangle(30, 108, 504, 278);
      width[109] = 778;
      boundingBoxes[109] = new Rectangle(16, 0, 759, 460);
      width[175] = 333;
      boundingBoxes[175] = new Rectangle(11, 547, 311, 54);
      width[181] = 500;
      boundingBoxes[181] = new Rectangle(36, -218, 476, 668);
      width[215] = 564;
      boundingBoxes[215] = new Rectangle(38, 8, 489, 489);
      width[110] = 500;
      boundingBoxes[110] = new Rectangle(16, 0, 469, 460);
      width[57] = 500;
      boundingBoxes[57] = new Rectangle(30, -22, 429, 698);
      width[241] = 500;
      boundingBoxes[241] = new Rectangle(16, 0, 469, 638);
      width[35] = 500;
      boundingBoxes[35] = new Rectangle(5, 0, 491, 662);
      width[111] = 500;
      boundingBoxes[111] = new Rectangle(29, -10, 441, 470);
      width[243] = 500;
      boundingBoxes[243] = new Rectangle(29, -10, 441, 688);
      width[244] = 500;
      boundingBoxes[244] = new Rectangle(29, -10, 441, 684);
      width[246] = 500;
      boundingBoxes[246] = new Rectangle(29, -10, 441, 633);
      width[156] = 722;
      boundingBoxes[156] = new Rectangle(30, -10, 660, 470);
      width[242] = 500;
      boundingBoxes[242] = new Rectangle(29, -10, 441, 688);
      width[49] = 500;
      boundingBoxes[49] = new Rectangle(111, 0, 283, 676);
      width[189] = 750;
      boundingBoxes[189] = new Rectangle(31, -14, 715, 690);
      width[188] = 750;
      boundingBoxes[188] = new Rectangle(37, -14, 681, 690);
      width[185] = 300;
      boundingBoxes[185] = new Rectangle(57, 270, 191, 406);
      width[170] = 276;
      boundingBoxes[170] = new Rectangle(4, 394, 266, 282);
      width[186] = 310;
      boundingBoxes[186] = new Rectangle(6, 394, 298, 282);
      width[248] = 500;
      boundingBoxes[248] = new Rectangle(29, -112, 441, 663);
      width[245] = 500;
      boundingBoxes[245] = new Rectangle(29, -10, 441, 648);
      width[112] = 500;
      boundingBoxes[112] = new Rectangle(5, -217, 465, 677);
      width[182] = 453;
      boundingBoxes[182] = new Rectangle(-22, -154, 472, 816);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(48, -177, 256, 853);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(29, -177, 256, 853);
      width[37] = 833;
      boundingBoxes[37] = new Rectangle(61, -13, 711, 689);
      width[46] = 250;
      boundingBoxes[46] = new Rectangle(70, -11, 111, 111);
      width[183] = 250;
      boundingBoxes[183] = new Rectangle(70, 199, 111, 111);
      width[137] = 1000;
      boundingBoxes[137] = new Rectangle(7, -19, 987, 725);
      width[43] = 564;
      boundingBoxes[43] = new Rectangle(30, 0, 504, 506);
      width[177] = 564;
      boundingBoxes[177] = new Rectangle(30, 0, 504, 506);
      width[113] = 500;
      boundingBoxes[113] = new Rectangle(24, -217, 464, 677);
      width[63] = 444;
      boundingBoxes[63] = new Rectangle(68, -8, 346, 684);
      width[191] = 444;
      boundingBoxes[191] = new Rectangle(30, -218, 346, 684);
      width[34] = 408;
      boundingBoxes[34] = new Rectangle(77, 431, 254, 245);
      width[132] = 444;
      boundingBoxes[132] = new Rectangle(45, -141, 371, 243);
      width[147] = 444;
      boundingBoxes[147] = new Rectangle(43, 433, 371, 243);
      width[148] = 444;
      boundingBoxes[148] = new Rectangle(30, 433, 371, 243);
      width[145] = 333;
      boundingBoxes[145] = new Rectangle(115, 433, 139, 243);
      width[146] = 333;
      boundingBoxes[146] = new Rectangle(79, 433, 139, 243);
      width[130] = 333;
      boundingBoxes[130] = new Rectangle(79, -141, 139, 243);
      width[39] = 180;
      boundingBoxes[39] = new Rectangle(48, 431, 85, 245);
      width[114] = 333;
      boundingBoxes[114] = new Rectangle(5, 0, 330, 460);
      width[174] = 760;
      boundingBoxes[174] = new Rectangle(38, -14, 684, 690);
      width[115] = 389;
      boundingBoxes[115] = new Rectangle(51, -10, 297, 470);
      width[154] = 389;
      boundingBoxes[154] = new Rectangle(39, -10, 311, 684);
      width[167] = 500;
      boundingBoxes[167] = new Rectangle(70, -148, 356, 824);
      width[59] = 278;
      boundingBoxes[59] = new Rectangle(80, -141, 139, 600);
      width[55] = 500;
      boundingBoxes[55] = new Rectangle(20, -8, 429, 670);
      width[54] = 500;
      boundingBoxes[54] = new Rectangle(34, -14, 434, 698);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(-9, -14, 296, 690);
      width[32] = 250;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 500;
      boundingBoxes[163] = new Rectangle(12, -8, 478, 684);
      width[116] = 278;
      boundingBoxes[116] = new Rectangle(13, -10, 266, 589);
      width[254] = 500;
      boundingBoxes[254] = new Rectangle(5, -217, 465, 900);
      width[51] = 500;
      boundingBoxes[51] = new Rectangle(43, -14, 388, 690);
      width[190] = 750;
      boundingBoxes[190] = new Rectangle(15, -14, 703, 690);
      width[179] = 300;
      boundingBoxes[179] = new Rectangle(15, 262, 276, 414);
      width[152] = 333;
      boundingBoxes[152] = new Rectangle(1, 532, 330, 106);
      width[153] = 980;
      boundingBoxes[153] = new Rectangle(30, 256, 927, 406);
      width[50] = 500;
      boundingBoxes[50] = new Rectangle(30, 0, 445, 676);
      width[178] = 300;
      boundingBoxes[178] = new Rectangle(1, 270, 295, 406);
      width[117] = 500;
      boundingBoxes[117] = new Rectangle(9, -10, 470, 460);
      width[250] = 500;
      boundingBoxes[250] = new Rectangle(9, -10, 470, 688);
      width[251] = 500;
      boundingBoxes[251] = new Rectangle(9, -10, 470, 684);
      width[252] = 500;
      boundingBoxes[252] = new Rectangle(9, -10, 470, 633);
      width[249] = 500;
      boundingBoxes[249] = new Rectangle(9, -10, 470, 688);
      width[95] = 500;
      boundingBoxes[95] = new Rectangle(0, -125, 500, 50);
      width[118] = 500;
      boundingBoxes[118] = new Rectangle(19, -14, 458, 464);
      width[119] = 722;
      boundingBoxes[119] = new Rectangle(21, -14, 673, 464);
      width[120] = 500;
      boundingBoxes[120] = new Rectangle(17, 0, 462, 450);
      width[121] = 500;
      boundingBoxes[121] = new Rectangle(14, -218, 461, 668);
      width[253] = 500;
      boundingBoxes[253] = new Rectangle(14, -218, 461, 896);
      width[255] = 500;
      boundingBoxes[255] = new Rectangle(14, -218, 461, 841);
      width[165] = 500;
      boundingBoxes[165] = new Rectangle(-53, 0, 565, 662);
      width[122] = 444;
      boundingBoxes[122] = new Rectangle(27, 0, 391, 450);
      width[158] = 444;
      boundingBoxes[158] = new Rectangle(27, 0, 391, 674);
      width[48] = 500;
      boundingBoxes[48] = new Rectangle(24, -14, 452, 690);
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
      ((Map)pairs).put(second, -35);
      second = 87;
      ((Map)pairs).put(second, -35);
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
      ((Map)pairs).put(second, -10);
      first = 80;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, -15);
      second = 65;
      ((Map)pairs).put(second, -92);
      second = 46;
      ((Map)pairs).put(second, -111);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -111);
      first = 86;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -129);
      second = 79;
      ((Map)pairs).put(second, -40);
      second = 58;
      ((Map)pairs).put(second, -74);
      second = 71;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, -129);
      second = 59;
      ((Map)pairs).put(second, -74);
      second = 45;
      ((Map)pairs).put(second, -100);
      second = 105;
      ((Map)pairs).put(second, -60);
      second = 65;
      ((Map)pairs).put(second, -135);
      second = 97;
      ((Map)pairs).put(second, -111);
      second = 117;
      ((Map)pairs).put(second, -75);
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
      ((Map)pairs).put(second, -20);
      second = 97;
      ((Map)pairs).put(second, -25);
      second = 46;
      ((Map)pairs).put(second, -65);
      second = 101;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, -65);
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
      ((Map)pairs).put(second, -90);
      second = 84;
      ((Map)pairs).put(second, -18);
      second = 145;
      ((Map)pairs).put(second, 0);
      second = 86;
      ((Map)pairs).put(second, -50);
      first = 97;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -15);
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
      ((Map)pairs).put(second, -20);
      first = 70;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -15);
      second = 105;
      ((Map)pairs).put(second, 0);
      second = 114;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, -15);
      second = 65;
      ((Map)pairs).put(second, -74);
      second = 46;
      ((Map)pairs).put(second, -80);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -80);
      first = 85;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -40);
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
      ((Map)pairs).put(second, -40);
      second = 87;
      ((Map)pairs).put(second, -30);
      second = 89;
      ((Map)pairs).put(second, -55);
      second = 46;
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -50);
      second = 32;
      ((Map)pairs).put(second, -74);
      second = 146;
      ((Map)pairs).put(second, -74);
      second = 114;
      ((Map)pairs).put(second, -50);
      second = 116;
      ((Map)pairs).put(second, -18);
      second = 108;
      ((Map)pairs).put(second, -10);
      second = 115;
      ((Map)pairs).put(second, -55);
      second = 118;
      ((Map)pairs).put(second, -50);
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
      ((Map)pairs).put(second, -10);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -65);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -65);
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
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -25);
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
      ((Map)pairs).put(second, -55);
      second = 85;
      ((Map)pairs).put(second, -40);
      second = 89;
      ((Map)pairs).put(second, -65);
      second = 84;
      ((Map)pairs).put(second, -60);
      second = 86;
      ((Map)pairs).put(second, -80);
      first = 145;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -80);
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
      ((Map)pairs).put(second, -5);
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
      ((Map)pairs).put(second, -35);
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
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -70);
      second = 32;
      ((Map)pairs).put(second, 0);
      second = 146;
      ((Map)pairs).put(second, -70);
      first = 102;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, 0);
      second = 111;
      ((Map)pairs).put(second, 0);
      second = 105;
      ((Map)pairs).put(second, -20);
      second = 146;
      ((Map)pairs).put(second, 55);
      second = 97;
      ((Map)pairs).put(second, -10);
      second = 102;
      ((Map)pairs).put(second, -25);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 84;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -80);
      second = 79;
      ((Map)pairs).put(second, -18);
      second = 119;
      ((Map)pairs).put(second, -80);
      second = 58;
      ((Map)pairs).put(second, -50);
      second = 114;
      ((Map)pairs).put(second, -35);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -74);
      second = 59;
      ((Map)pairs).put(second, -55);
      second = 45;
      ((Map)pairs).put(second, -92);
      second = 105;
      ((Map)pairs).put(second, -35);
      second = 65;
      ((Map)pairs).put(second, -93);
      second = 97;
      ((Map)pairs).put(second, -80);
      second = 117;
      ((Map)pairs).put(second, -45);
      second = 121;
      ((Map)pairs).put(second, -80);
      second = 46;
      ((Map)pairs).put(second, -74);
      second = 101;
      ((Map)pairs).put(second, -70);
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
      ((Map)pairs).put(second, -65);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -65);
      first = 120;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 101;
      ((Map)pairs).put(second, -15);
      first = 101;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 119;
      ((Map)pairs).put(second, -25);
      second = 121;
      ((Map)pairs).put(second, -15);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 103;
      ((Map)pairs).put(second, -15);
      second = 98;
      ((Map)pairs).put(second, 0);
      second = 120;
      ((Map)pairs).put(second, -15);
      second = 118;
      ((Map)pairs).put(second, -25);
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
      ((Map)pairs).put(second, -15);
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
      ((Map)pairs).put(second, -10);
      second = 58;
      ((Map)pairs).put(second, -37);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -92);
      second = 59;
      ((Map)pairs).put(second, -37);
      second = 45;
      ((Map)pairs).put(second, -65);
      second = 105;
      ((Map)pairs).put(second, -40);
      second = 65;
      ((Map)pairs).put(second, -120);
      second = 97;
      ((Map)pairs).put(second, -80);
      second = 117;
      ((Map)pairs).put(second, -50);
      second = 121;
      ((Map)pairs).put(second, -73);
      second = 46;
      ((Map)pairs).put(second, -92);
      second = 101;
      ((Map)pairs).put(second, -80);
      first = 104;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 121;
      ((Map)pairs).put(second, -5);
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
      ((Map)pairs).put(second, -25);
      first = 65;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 79;
      ((Map)pairs).put(second, -55);
      second = 146;
      ((Map)pairs).put(second, -111);
      second = 119;
      ((Map)pairs).put(second, -92);
      second = 87;
      ((Map)pairs).put(second, -90);
      second = 67;
      ((Map)pairs).put(second, -40);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 81;
      ((Map)pairs).put(second, -55);
      second = 71;
      ((Map)pairs).put(second, -40);
      second = 86;
      ((Map)pairs).put(second, -135);
      second = 118;
      ((Map)pairs).put(second, -74);
      second = 148;
      ((Map)pairs).put(second, 0);
      second = 85;
      ((Map)pairs).put(second, -55);
      second = 117;
      ((Map)pairs).put(second, 0);
      second = 89;
      ((Map)pairs).put(second, -105);
      second = 121;
      ((Map)pairs).put(second, -92);
      second = 84;
      ((Map)pairs).put(second, -111);
      first = 147;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -80);
      second = 145;
      ((Map)pairs).put(second, 0);
      first = 78;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -35);
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
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -18);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 113;
      ((Map)pairs).put(second, 0);
      second = 118;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -40);
      second = 45;
      ((Map)pairs).put(second, -20);
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
      ((Map)pairs).put(second, -55);
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
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, -92);
      second = 87;
      ((Map)pairs).put(second, -74);
      second = 89;
      ((Map)pairs).put(second, -100);
      second = 121;
      ((Map)pairs).put(second, -55);
      second = 84;
      ((Map)pairs).put(second, -92);
      second = 86;
      ((Map)pairs).put(second, -100);
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
      ((Map)pairs).put(second, -110);
      second = 45;
      ((Map)pairs).put(second, -111);
      second = 105;
      ((Map)pairs).put(second, -55);
      second = 79;
      ((Map)pairs).put(second, -30);
      second = 58;
      ((Map)pairs).put(second, -92);
      second = 97;
      ((Map)pairs).put(second, -100);
      second = 65;
      ((Map)pairs).put(second, -120);
      second = 117;
      ((Map)pairs).put(second, -111);
      second = 46;
      ((Map)pairs).put(second, -129);
      second = 101;
      ((Map)pairs).put(second, -100);
      second = 59;
      ((Map)pairs).put(second, -92);
      second = 44;
      ((Map)pairs).put(second, -129);
      first = 74;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 65;
      ((Map)pairs).put(second, -60);
      second = 117;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, 0);
      first = 46;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -70);
      second = 146;
      ((Map)pairs).put(second, -70);
      first = 110;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 117;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, -15);
      second = 118;
      ((Map)pairs).put(second, -40);
   }
}
