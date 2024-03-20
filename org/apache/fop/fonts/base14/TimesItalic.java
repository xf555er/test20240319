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

public class TimesItalic extends Base14Font {
   private static final URI fontFileURI;
   private static final String fontName = "Times-Italic";
   private static final String fullName = "Times Italic";
   private static final Set familyNames;
   private static final int underlinePosition = -100;
   private static final int underlineThickness = 50;
   private static final String encoding = "WinAnsiEncoding";
   private static final int capHeight = 653;
   private static final int xHeight = 441;
   private static final int ascender = 683;
   private static final int descender = -205;
   private static final int firstChar = 32;
   private static final int lastChar = 255;
   private static final int[] width;
   private static final Rectangle[] boundingBoxes;
   private final CodePointMapping mapping;
   private static final Map kerning;
   private boolean enableKerning;

   public TimesItalic() {
      this(false);
   }

   public TimesItalic(boolean enableKerning) {
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
      return "Times-Italic";
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return "Times Italic";
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
      return size * 653;
   }

   public int getDescender(int size) {
      return size * -205;
   }

   public int getXHeight(int size) {
      return size * 441;
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
         uri = new URI("base14:" + "Times-Italic".toLowerCase());
      } catch (URISyntaxException var4) {
         throw new RuntimeException(var4);
      }

      fontFileURI = uri;
      width = new int[256];
      boundingBoxes = new Rectangle[256];
      width[65] = 611;
      boundingBoxes[65] = new Rectangle(-51, 0, 615, 668);
      width[198] = 889;
      boundingBoxes[198] = new Rectangle(-27, 0, 938, 653);
      width[193] = 611;
      boundingBoxes[193] = new Rectangle(-51, 0, 615, 876);
      width[194] = 611;
      boundingBoxes[194] = new Rectangle(-51, 0, 615, 873);
      width[196] = 611;
      boundingBoxes[196] = new Rectangle(-51, 0, 615, 818);
      width[192] = 611;
      boundingBoxes[192] = new Rectangle(-51, 0, 615, 876);
      width[197] = 611;
      boundingBoxes[197] = new Rectangle(-51, 0, 615, 883);
      width[195] = 611;
      boundingBoxes[195] = new Rectangle(-51, 0, 617, 836);
      width[66] = 611;
      boundingBoxes[66] = new Rectangle(-8, 0, 596, 653);
      width[67] = 667;
      boundingBoxes[67] = new Rectangle(66, -18, 623, 684);
      width[199] = 667;
      boundingBoxes[199] = new Rectangle(66, -217, 623, 883);
      width[68] = 722;
      boundingBoxes[68] = new Rectangle(-8, 0, 708, 653);
      width[69] = 611;
      boundingBoxes[69] = new Rectangle(-1, 0, 635, 653);
      width[201] = 611;
      boundingBoxes[201] = new Rectangle(-1, 0, 635, 876);
      width[202] = 611;
      boundingBoxes[202] = new Rectangle(-1, 0, 635, 873);
      width[203] = 611;
      boundingBoxes[203] = new Rectangle(-1, 0, 635, 818);
      width[200] = 611;
      boundingBoxes[200] = new Rectangle(-1, 0, 635, 876);
      width[208] = 722;
      boundingBoxes[208] = new Rectangle(-8, 0, 708, 653);
      width[128] = 500;
      boundingBoxes[128] = new Rectangle(0, 0, 0, 0);
      width[70] = 611;
      boundingBoxes[70] = new Rectangle(8, 0, 637, 653);
      width[71] = 722;
      boundingBoxes[71] = new Rectangle(52, -18, 670, 684);
      width[72] = 722;
      boundingBoxes[72] = new Rectangle(-8, 0, 775, 653);
      width[73] = 333;
      boundingBoxes[73] = new Rectangle(-8, 0, 392, 653);
      width[205] = 333;
      boundingBoxes[205] = new Rectangle(-8, 0, 441, 876);
      width[206] = 333;
      boundingBoxes[206] = new Rectangle(-8, 0, 433, 873);
      width[207] = 333;
      boundingBoxes[207] = new Rectangle(-8, 0, 443, 818);
      width[204] = 333;
      boundingBoxes[204] = new Rectangle(-8, 0, 392, 876);
      width[74] = 444;
      boundingBoxes[74] = new Rectangle(-6, -18, 497, 671);
      width[75] = 667;
      boundingBoxes[75] = new Rectangle(7, 0, 715, 653);
      width[76] = 556;
      boundingBoxes[76] = new Rectangle(-8, 0, 567, 653);
      width[77] = 833;
      boundingBoxes[77] = new Rectangle(-18, 0, 891, 653);
      width[78] = 667;
      boundingBoxes[78] = new Rectangle(-20, -15, 747, 668);
      width[209] = 667;
      boundingBoxes[209] = new Rectangle(-20, -15, 747, 851);
      width[79] = 722;
      boundingBoxes[79] = new Rectangle(60, -18, 639, 684);
      width[140] = 944;
      boundingBoxes[140] = new Rectangle(49, -8, 915, 674);
      width[211] = 722;
      boundingBoxes[211] = new Rectangle(60, -18, 639, 894);
      width[212] = 722;
      boundingBoxes[212] = new Rectangle(60, -18, 639, 891);
      width[214] = 722;
      boundingBoxes[214] = new Rectangle(60, -18, 639, 836);
      width[210] = 722;
      boundingBoxes[210] = new Rectangle(60, -18, 639, 894);
      width[216] = 722;
      boundingBoxes[216] = new Rectangle(60, -105, 639, 827);
      width[213] = 722;
      boundingBoxes[213] = new Rectangle(60, -18, 639, 854);
      width[80] = 611;
      boundingBoxes[80] = new Rectangle(0, 0, 605, 653);
      width[81] = 722;
      boundingBoxes[81] = new Rectangle(59, -182, 640, 848);
      width[82] = 611;
      boundingBoxes[82] = new Rectangle(-13, 0, 601, 653);
      width[83] = 500;
      boundingBoxes[83] = new Rectangle(17, -18, 491, 685);
      width[138] = 500;
      boundingBoxes[138] = new Rectangle(17, -18, 503, 891);
      width[84] = 556;
      boundingBoxes[84] = new Rectangle(59, 0, 574, 653);
      width[222] = 611;
      boundingBoxes[222] = new Rectangle(0, 0, 569, 653);
      width[85] = 722;
      boundingBoxes[85] = new Rectangle(102, -18, 663, 671);
      width[218] = 722;
      boundingBoxes[218] = new Rectangle(102, -18, 663, 894);
      width[219] = 722;
      boundingBoxes[219] = new Rectangle(102, -18, 663, 891);
      width[220] = 722;
      boundingBoxes[220] = new Rectangle(102, -18, 663, 836);
      width[217] = 722;
      boundingBoxes[217] = new Rectangle(102, -18, 663, 894);
      width[86] = 611;
      boundingBoxes[86] = new Rectangle(76, -18, 612, 671);
      width[87] = 833;
      boundingBoxes[87] = new Rectangle(71, -18, 835, 671);
      width[88] = 611;
      boundingBoxes[88] = new Rectangle(-29, 0, 684, 653);
      width[89] = 556;
      boundingBoxes[89] = new Rectangle(78, 0, 555, 653);
      width[221] = 556;
      boundingBoxes[221] = new Rectangle(78, 0, 555, 876);
      width[159] = 556;
      boundingBoxes[159] = new Rectangle(78, 0, 555, 818);
      width[90] = 556;
      boundingBoxes[90] = new Rectangle(-6, 0, 612, 653);
      width[142] = 556;
      boundingBoxes[142] = new Rectangle(-6, 0, 612, 873);
      width[97] = 500;
      boundingBoxes[97] = new Rectangle(17, -11, 459, 452);
      width[225] = 500;
      boundingBoxes[225] = new Rectangle(17, -11, 470, 675);
      width[226] = 500;
      boundingBoxes[226] = new Rectangle(17, -11, 459, 672);
      width[180] = 333;
      boundingBoxes[180] = new Rectangle(180, 494, 223, 170);
      width[228] = 500;
      boundingBoxes[228] = new Rectangle(17, -11, 472, 617);
      width[230] = 667;
      boundingBoxes[230] = new Rectangle(23, -11, 617, 452);
      width[224] = 500;
      boundingBoxes[224] = new Rectangle(17, -11, 459, 675);
      width[38] = 778;
      boundingBoxes[38] = new Rectangle(76, -18, 647, 684);
      width[229] = 500;
      boundingBoxes[229] = new Rectangle(17, -11, 459, 702);
      width[94] = 422;
      boundingBoxes[94] = new Rectangle(0, 301, 422, 365);
      width[126] = 541;
      boundingBoxes[126] = new Rectangle(40, 183, 462, 140);
      width[42] = 500;
      boundingBoxes[42] = new Rectangle(128, 255, 364, 411);
      width[64] = 920;
      boundingBoxes[64] = new Rectangle(118, -18, 688, 684);
      width[227] = 500;
      boundingBoxes[227] = new Rectangle(17, -11, 494, 635);
      width[98] = 500;
      boundingBoxes[98] = new Rectangle(23, -11, 450, 694);
      width[92] = 278;
      boundingBoxes[92] = new Rectangle(-41, -18, 360, 684);
      width[124] = 275;
      boundingBoxes[124] = new Rectangle(105, -217, 66, 1000);
      width[123] = 400;
      boundingBoxes[123] = new Rectangle(51, -177, 356, 864);
      width[125] = 400;
      boundingBoxes[125] = new Rectangle(-7, -177, 356, 864);
      width[91] = 389;
      boundingBoxes[91] = new Rectangle(21, -153, 370, 816);
      width[93] = 389;
      boundingBoxes[93] = new Rectangle(12, -153, 370, 816);
      width[166] = 275;
      boundingBoxes[166] = new Rectangle(105, -142, 66, 850);
      width[149] = 350;
      boundingBoxes[149] = new Rectangle(40, 191, 270, 270);
      width[99] = 444;
      boundingBoxes[99] = new Rectangle(30, -11, 395, 452);
      width[231] = 444;
      boundingBoxes[231] = new Rectangle(30, -217, 395, 658);
      width[184] = 333;
      boundingBoxes[184] = new Rectangle(-30, -217, 212, 217);
      width[162] = 500;
      boundingBoxes[162] = new Rectangle(77, -143, 395, 703);
      width[136] = 333;
      boundingBoxes[136] = new Rectangle(91, 492, 294, 169);
      width[58] = 333;
      boundingBoxes[58] = new Rectangle(50, -11, 211, 452);
      width[44] = 250;
      boundingBoxes[44] = new Rectangle(-4, -129, 139, 230);
      width[169] = 760;
      boundingBoxes[169] = new Rectangle(41, -18, 678, 684);
      width[164] = 500;
      boundingBoxes[164] = new Rectangle(-22, 53, 544, 544);
      width[100] = 500;
      boundingBoxes[100] = new Rectangle(15, -13, 512, 696);
      width[134] = 500;
      boundingBoxes[134] = new Rectangle(101, -159, 387, 825);
      width[135] = 500;
      boundingBoxes[135] = new Rectangle(22, -143, 469, 809);
      width[176] = 400;
      boundingBoxes[176] = new Rectangle(101, 390, 286, 286);
      width[168] = 333;
      boundingBoxes[168] = new Rectangle(107, 548, 298, 98);
      width[247] = 675;
      boundingBoxes[247] = new Rectangle(86, -11, 504, 528);
      width[36] = 500;
      boundingBoxes[36] = new Rectangle(31, -89, 466, 820);
      width[101] = 444;
      boundingBoxes[101] = new Rectangle(31, -11, 381, 452);
      width[233] = 444;
      boundingBoxes[233] = new Rectangle(31, -11, 428, 675);
      width[234] = 444;
      boundingBoxes[234] = new Rectangle(31, -11, 410, 672);
      width[235] = 444;
      boundingBoxes[235] = new Rectangle(31, -11, 420, 617);
      width[232] = 444;
      boundingBoxes[232] = new Rectangle(31, -11, 381, 675);
      width[56] = 500;
      boundingBoxes[56] = new Rectangle(30, -7, 463, 683);
      width[133] = 889;
      boundingBoxes[133] = new Rectangle(57, -11, 705, 111);
      width[151] = 889;
      boundingBoxes[151] = new Rectangle(-6, 197, 900, 46);
      width[150] = 500;
      boundingBoxes[150] = new Rectangle(-6, 197, 511, 46);
      width[61] = 675;
      boundingBoxes[61] = new Rectangle(86, 120, 504, 266);
      width[240] = 500;
      boundingBoxes[240] = new Rectangle(27, -11, 455, 694);
      width[33] = 333;
      boundingBoxes[33] = new Rectangle(39, -11, 263, 678);
      width[161] = 389;
      boundingBoxes[161] = new Rectangle(59, -205, 263, 678);
      width[102] = 278;
      boundingBoxes[102] = new Rectangle(-147, -207, 571, 885);
      width[53] = 500;
      boundingBoxes[53] = new Rectangle(15, -7, 476, 673);
      width[131] = 500;
      boundingBoxes[131] = new Rectangle(25, -182, 482, 864);
      width[52] = 500;
      boundingBoxes[52] = new Rectangle(1, 0, 478, 676);
      width[103] = 500;
      boundingBoxes[103] = new Rectangle(8, -206, 464, 647);
      width[223] = 500;
      boundingBoxes[223] = new Rectangle(-168, -207, 661, 886);
      width[96] = 333;
      boundingBoxes[96] = new Rectangle(121, 492, 190, 172);
      width[62] = 675;
      boundingBoxes[62] = new Rectangle(84, -8, 508, 522);
      width[171] = 500;
      boundingBoxes[171] = new Rectangle(53, 37, 392, 366);
      width[187] = 500;
      boundingBoxes[187] = new Rectangle(55, 37, 392, 366);
      width[139] = 333;
      boundingBoxes[139] = new Rectangle(51, 37, 230, 366);
      width[155] = 333;
      boundingBoxes[155] = new Rectangle(52, 37, 230, 366);
      width[104] = 500;
      boundingBoxes[104] = new Rectangle(19, -9, 459, 692);
      width[45] = 333;
      boundingBoxes[45] = new Rectangle(49, 192, 233, 63);
      width[105] = 278;
      boundingBoxes[105] = new Rectangle(49, -11, 215, 665);
      width[237] = 278;
      boundingBoxes[237] = new Rectangle(49, -11, 306, 675);
      width[238] = 278;
      boundingBoxes[238] = new Rectangle(33, -11, 294, 672);
      width[239] = 278;
      boundingBoxes[239] = new Rectangle(49, -11, 303, 617);
      width[236] = 278;
      boundingBoxes[236] = new Rectangle(49, -11, 235, 675);
      width[106] = 278;
      boundingBoxes[106] = new Rectangle(-124, -207, 400, 861);
      width[107] = 444;
      boundingBoxes[107] = new Rectangle(14, -11, 447, 694);
      width[108] = 278;
      boundingBoxes[108] = new Rectangle(41, -11, 238, 694);
      width[60] = 675;
      boundingBoxes[60] = new Rectangle(84, -8, 508, 522);
      width[172] = 675;
      boundingBoxes[172] = new Rectangle(86, 108, 504, 278);
      width[109] = 722;
      boundingBoxes[109] = new Rectangle(12, -9, 692, 450);
      width[175] = 333;
      boundingBoxes[175] = new Rectangle(99, 532, 312, 51);
      width[181] = 500;
      boundingBoxes[181] = new Rectangle(-30, -209, 527, 637);
      width[215] = 675;
      boundingBoxes[215] = new Rectangle(93, 8, 489, 489);
      width[110] = 500;
      boundingBoxes[110] = new Rectangle(14, -9, 460, 450);
      width[57] = 500;
      boundingBoxes[57] = new Rectangle(23, -17, 469, 693);
      width[241] = 500;
      boundingBoxes[241] = new Rectangle(14, -9, 462, 633);
      width[35] = 500;
      boundingBoxes[35] = new Rectangle(2, 0, 538, 676);
      width[111] = 500;
      boundingBoxes[111] = new Rectangle(27, -11, 441, 452);
      width[243] = 500;
      boundingBoxes[243] = new Rectangle(27, -11, 460, 675);
      width[244] = 500;
      boundingBoxes[244] = new Rectangle(27, -11, 441, 672);
      width[246] = 500;
      boundingBoxes[246] = new Rectangle(27, -11, 462, 617);
      width[156] = 667;
      boundingBoxes[156] = new Rectangle(20, -12, 626, 453);
      width[242] = 500;
      boundingBoxes[242] = new Rectangle(27, -11, 441, 675);
      width[49] = 500;
      boundingBoxes[49] = new Rectangle(49, 0, 360, 676);
      width[189] = 750;
      boundingBoxes[189] = new Rectangle(34, -10, 715, 686);
      width[188] = 750;
      boundingBoxes[188] = new Rectangle(33, -10, 703, 686);
      width[185] = 300;
      boundingBoxes[185] = new Rectangle(43, 271, 241, 405);
      width[170] = 276;
      boundingBoxes[170] = new Rectangle(42, 406, 310, 270);
      width[186] = 310;
      boundingBoxes[186] = new Rectangle(67, 406, 295, 270);
      width[248] = 500;
      boundingBoxes[248] = new Rectangle(28, -135, 441, 689);
      width[245] = 500;
      boundingBoxes[245] = new Rectangle(27, -11, 469, 635);
      width[112] = 500;
      boundingBoxes[112] = new Rectangle(-75, -205, 544, 646);
      width[182] = 523;
      boundingBoxes[182] = new Rectangle(55, -123, 561, 776);
      width[40] = 333;
      boundingBoxes[40] = new Rectangle(42, -181, 273, 850);
      width[41] = 333;
      boundingBoxes[41] = new Rectangle(16, -180, 273, 849);
      width[37] = 833;
      boundingBoxes[37] = new Rectangle(79, -13, 711, 689);
      width[46] = 250;
      boundingBoxes[46] = new Rectangle(27, -11, 111, 111);
      width[183] = 250;
      boundingBoxes[183] = new Rectangle(70, 199, 111, 111);
      width[137] = 1000;
      boundingBoxes[137] = new Rectangle(25, -19, 985, 725);
      width[43] = 675;
      boundingBoxes[43] = new Rectangle(86, 0, 504, 506);
      width[177] = 675;
      boundingBoxes[177] = new Rectangle(86, 0, 504, 506);
      width[113] = 500;
      boundingBoxes[113] = new Rectangle(25, -209, 458, 650);
      width[63] = 500;
      boundingBoxes[63] = new Rectangle(132, -12, 340, 676);
      width[191] = 500;
      boundingBoxes[191] = new Rectangle(28, -205, 340, 676);
      width[34] = 420;
      boundingBoxes[34] = new Rectangle(144, 421, 288, 245);
      width[132] = 556;
      boundingBoxes[132] = new Rectangle(57, -129, 348, 230);
      width[147] = 556;
      boundingBoxes[147] = new Rectangle(166, 436, 348, 230);
      width[148] = 556;
      boundingBoxes[148] = new Rectangle(151, 436, 348, 230);
      width[145] = 333;
      boundingBoxes[145] = new Rectangle(171, 436, 139, 230);
      width[146] = 333;
      boundingBoxes[146] = new Rectangle(151, 436, 139, 230);
      width[130] = 333;
      boundingBoxes[130] = new Rectangle(44, -129, 139, 230);
      width[39] = 214;
      boundingBoxes[39] = new Rectangle(132, 421, 109, 245);
      width[114] = 389;
      boundingBoxes[114] = new Rectangle(45, 0, 367, 441);
      width[174] = 760;
      boundingBoxes[174] = new Rectangle(41, -18, 678, 684);
      width[115] = 389;
      boundingBoxes[115] = new Rectangle(16, -13, 350, 455);
      width[154] = 389;
      boundingBoxes[154] = new Rectangle(16, -13, 438, 674);
      width[167] = 500;
      boundingBoxes[167] = new Rectangle(53, -162, 408, 828);
      width[59] = 333;
      boundingBoxes[59] = new Rectangle(27, -129, 234, 570);
      width[55] = 500;
      boundingBoxes[55] = new Rectangle(75, -8, 462, 674);
      width[54] = 500;
      boundingBoxes[54] = new Rectangle(30, -7, 491, 693);
      width[47] = 278;
      boundingBoxes[47] = new Rectangle(-65, -18, 451, 684);
      width[32] = 250;
      boundingBoxes[32] = new Rectangle(0, 0, 0, 0);
      width[163] = 500;
      boundingBoxes[163] = new Rectangle(10, -6, 507, 676);
      width[116] = 278;
      boundingBoxes[116] = new Rectangle(37, -11, 259, 557);
      width[254] = 500;
      boundingBoxes[254] = new Rectangle(-75, -205, 544, 888);
      width[51] = 500;
      boundingBoxes[51] = new Rectangle(15, -7, 450, 683);
      width[190] = 750;
      boundingBoxes[190] = new Rectangle(23, -10, 713, 686);
      width[179] = 300;
      boundingBoxes[179] = new Rectangle(43, 268, 296, 408);
      width[152] = 333;
      boundingBoxes[152] = new Rectangle(100, 517, 327, 107);
      width[153] = 980;
      boundingBoxes[153] = new Rectangle(30, 247, 927, 406);
      width[50] = 500;
      boundingBoxes[50] = new Rectangle(12, 0, 440, 676);
      width[178] = 300;
      boundingBoxes[178] = new Rectangle(33, 271, 291, 405);
      width[117] = 500;
      boundingBoxes[117] = new Rectangle(42, -11, 433, 452);
      width[250] = 500;
      boundingBoxes[250] = new Rectangle(42, -11, 435, 675);
      width[251] = 500;
      boundingBoxes[251] = new Rectangle(42, -11, 433, 672);
      width[252] = 500;
      boundingBoxes[252] = new Rectangle(42, -11, 437, 617);
      width[249] = 500;
      boundingBoxes[249] = new Rectangle(42, -11, 433, 675);
      width[95] = 500;
      boundingBoxes[95] = new Rectangle(0, -125, 500, 50);
      width[118] = 444;
      boundingBoxes[118] = new Rectangle(21, -18, 405, 459);
      width[119] = 667;
      boundingBoxes[119] = new Rectangle(16, -18, 632, 459);
      width[120] = 444;
      boundingBoxes[120] = new Rectangle(-27, -11, 474, 452);
      width[121] = 444;
      boundingBoxes[121] = new Rectangle(-24, -206, 450, 647);
      width[253] = 444;
      boundingBoxes[253] = new Rectangle(-24, -206, 483, 870);
      width[255] = 444;
      boundingBoxes[255] = new Rectangle(-24, -206, 465, 812);
      width[165] = 500;
      boundingBoxes[165] = new Rectangle(27, 0, 576, 653);
      width[122] = 389;
      boundingBoxes[122] = new Rectangle(-2, -81, 382, 509);
      width[158] = 389;
      boundingBoxes[158] = new Rectangle(-2, -81, 436, 742);
      width[48] = 500;
      boundingBoxes[48] = new Rectangle(32, -7, 465, 683);
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
      ((Map)pairs).put(second, -55);
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
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, -80);
      second = 97;
      ((Map)pairs).put(second, -80);
      second = 65;
      ((Map)pairs).put(second, -90);
      second = 46;
      ((Map)pairs).put(second, -135);
      second = 101;
      ((Map)pairs).put(second, -80);
      second = 44;
      ((Map)pairs).put(second, -135);
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
      ((Map)pairs).put(second, -65);
      second = 71;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -129);
      second = 59;
      ((Map)pairs).put(second, -74);
      second = 45;
      ((Map)pairs).put(second, -55);
      second = 105;
      ((Map)pairs).put(second, -74);
      second = 65;
      ((Map)pairs).put(second, -60);
      second = 97;
      ((Map)pairs).put(second, -111);
      second = 117;
      ((Map)pairs).put(second, -74);
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
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -74);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -74);
      first = 32;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -18);
      second = 87;
      ((Map)pairs).put(second, -40);
      second = 147;
      ((Map)pairs).put(second, 0);
      second = 89;
      ((Map)pairs).put(second, -75);
      second = 84;
      ((Map)pairs).put(second, -18);
      second = 145;
      ((Map)pairs).put(second, 0);
      second = 86;
      ((Map)pairs).put(second, -35);
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
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, -105);
      second = 105;
      ((Map)pairs).put(second, -45);
      second = 114;
      ((Map)pairs).put(second, -55);
      second = 97;
      ((Map)pairs).put(second, -75);
      second = 65;
      ((Map)pairs).put(second, -115);
      second = 46;
      ((Map)pairs).put(second, -135);
      second = 101;
      ((Map)pairs).put(second, -75);
      second = 44;
      ((Map)pairs).put(second, -135);
      first = 85;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 65;
      ((Map)pairs).put(second, -40);
      second = 46;
      ((Map)pairs).put(second, -25);
      second = 44;
      ((Map)pairs).put(second, -25);
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
      ((Map)pairs).put(second, -35);
      second = 87;
      ((Map)pairs).put(second, -40);
      second = 89;
      ((Map)pairs).put(second, -40);
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
      ((Map)pairs).put(second, -25);
      second = 32;
      ((Map)pairs).put(second, -111);
      second = 146;
      ((Map)pairs).put(second, -111);
      second = 114;
      ((Map)pairs).put(second, -25);
      second = 116;
      ((Map)pairs).put(second, -30);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 115;
      ((Map)pairs).put(second, -40);
      second = 118;
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -74);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -74);
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
      ((Map)pairs).put(second, -40);
      second = 121;
      ((Map)pairs).put(second, -40);
      second = 101;
      ((Map)pairs).put(second, -35);
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
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -111);
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
      ((Map)pairs).put(second, -10);
      second = 101;
      ((Map)pairs).put(second, -10);
      second = 44;
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -140);
      second = 32;
      ((Map)pairs).put(second, 0);
      second = 146;
      ((Map)pairs).put(second, -140);
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
      ((Map)pairs).put(second, 92);
      second = 97;
      ((Map)pairs).put(second, 0);
      second = 102;
      ((Map)pairs).put(second, -18);
      second = 46;
      ((Map)pairs).put(second, -15);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 101;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, -55);
      second = 114;
      ((Map)pairs).put(second, -55);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -74);
      second = 59;
      ((Map)pairs).put(second, -65);
      second = 45;
      ((Map)pairs).put(second, -74);
      second = 105;
      ((Map)pairs).put(second, -55);
      second = 65;
      ((Map)pairs).put(second, -50);
      second = 97;
      ((Map)pairs).put(second, -92);
      second = 117;
      ((Map)pairs).put(second, -55);
      second = 121;
      ((Map)pairs).put(second, -74);
      second = 46;
      ((Map)pairs).put(second, -74);
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
      ((Map)pairs).put(second, -55);
      second = 101;
      ((Map)pairs).put(second, 0);
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
      ((Map)pairs).put(second, -15);
      second = 121;
      ((Map)pairs).put(second, -30);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -15);
      second = 103;
      ((Map)pairs).put(second, -40);
      second = 98;
      ((Map)pairs).put(second, 0);
      second = 120;
      ((Map)pairs).put(second, -20);
      second = 118;
      ((Map)pairs).put(second, -15);
      second = 44;
      ((Map)pairs).put(second, -10);
      first = 99;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 107;
      ((Map)pairs).put(second, -20);
      second = 104;
      ((Map)pairs).put(second, -15);
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
      ((Map)pairs).put(second, -92);
      second = 79;
      ((Map)pairs).put(second, -25);
      second = 58;
      ((Map)pairs).put(second, -65);
      second = 104;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -92);
      second = 59;
      ((Map)pairs).put(second, -65);
      second = 45;
      ((Map)pairs).put(second, -37);
      second = 105;
      ((Map)pairs).put(second, -55);
      second = 65;
      ((Map)pairs).put(second, -60);
      second = 97;
      ((Map)pairs).put(second, -92);
      second = 117;
      ((Map)pairs).put(second, -55);
      second = 121;
      ((Map)pairs).put(second, -70);
      second = 46;
      ((Map)pairs).put(second, -92);
      second = 101;
      ((Map)pairs).put(second, -92);
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
      ((Map)pairs).put(second, -40);
      second = 146;
      ((Map)pairs).put(second, -37);
      second = 119;
      ((Map)pairs).put(second, -55);
      second = 87;
      ((Map)pairs).put(second, -95);
      second = 67;
      ((Map)pairs).put(second, -30);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 81;
      ((Map)pairs).put(second, -40);
      second = 71;
      ((Map)pairs).put(second, -35);
      second = 86;
      ((Map)pairs).put(second, -105);
      second = 118;
      ((Map)pairs).put(second, -55);
      second = 148;
      ((Map)pairs).put(second, 0);
      second = 85;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -20);
      second = 89;
      ((Map)pairs).put(second, -55);
      second = 121;
      ((Map)pairs).put(second, -55);
      second = 84;
      ((Map)pairs).put(second, -37);
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
      ((Map)pairs).put(second, -27);
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
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 103;
      ((Map)pairs).put(second, -10);
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
      ((Map)pairs).put(second, -45);
      second = 100;
      ((Map)pairs).put(second, -37);
      second = 107;
      ((Map)pairs).put(second, 0);
      second = 114;
      ((Map)pairs).put(second, 0);
      second = 99;
      ((Map)pairs).put(second, -37);
      second = 112;
      ((Map)pairs).put(second, 0);
      second = 103;
      ((Map)pairs).put(second, -37);
      second = 108;
      ((Map)pairs).put(second, 0);
      second = 113;
      ((Map)pairs).put(second, -37);
      second = 118;
      ((Map)pairs).put(second, 0);
      second = 44;
      ((Map)pairs).put(second, -111);
      second = 45;
      ((Map)pairs).put(second, -20);
      second = 105;
      ((Map)pairs).put(second, 0);
      second = 109;
      ((Map)pairs).put(second, 0);
      second = 97;
      ((Map)pairs).put(second, -15);
      second = 117;
      ((Map)pairs).put(second, 0);
      second = 116;
      ((Map)pairs).put(second, 0);
      second = 121;
      ((Map)pairs).put(second, 0);
      second = 46;
      ((Map)pairs).put(second, -111);
      second = 110;
      ((Map)pairs).put(second, 0);
      second = 115;
      ((Map)pairs).put(second, -10);
      second = 101;
      ((Map)pairs).put(second, -37);
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
      ((Map)pairs).put(second, -37);
      second = 87;
      ((Map)pairs).put(second, -55);
      second = 89;
      ((Map)pairs).put(second, -20);
      second = 121;
      ((Map)pairs).put(second, -30);
      second = 84;
      ((Map)pairs).put(second, -20);
      second = 86;
      ((Map)pairs).put(second, -55);
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
      ((Map)pairs).put(second, -92);
      second = 45;
      ((Map)pairs).put(second, -74);
      second = 105;
      ((Map)pairs).put(second, -74);
      second = 79;
      ((Map)pairs).put(second, -15);
      second = 58;
      ((Map)pairs).put(second, -65);
      second = 97;
      ((Map)pairs).put(second, -92);
      second = 65;
      ((Map)pairs).put(second, -50);
      second = 117;
      ((Map)pairs).put(second, -92);
      second = 46;
      ((Map)pairs).put(second, -92);
      second = 101;
      ((Map)pairs).put(second, -92);
      second = 59;
      ((Map)pairs).put(second, -65);
      second = 44;
      ((Map)pairs).put(second, -92);
      first = 74;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 111;
      ((Map)pairs).put(second, -25);
      second = 97;
      ((Map)pairs).put(second, -35);
      second = 65;
      ((Map)pairs).put(second, -40);
      second = 117;
      ((Map)pairs).put(second, -35);
      second = 46;
      ((Map)pairs).put(second, -25);
      second = 101;
      ((Map)pairs).put(second, -25);
      second = 44;
      ((Map)pairs).put(second, -25);
      first = 46;
      pairs = (Map)kerning.get(first);
      if (pairs == null) {
         pairs = new HashMap();
         kerning.put(first, pairs);
      }

      second = 148;
      ((Map)pairs).put(second, -140);
      second = 146;
      ((Map)pairs).put(second, -140);
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
