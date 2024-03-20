package org.apache.fop.complexscripts.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.fop.util.CharUtilities;

public final class CharScript {
   public static final int SCRIPT_HEBREW = 125;
   public static final int SCRIPT_MONGOLIAN = 145;
   public static final int SCRIPT_ARABIC = 160;
   public static final int SCRIPT_GREEK = 200;
   public static final int SCRIPT_LATIN = 215;
   public static final int SCRIPT_CYRILLIC = 220;
   public static final int SCRIPT_GEORGIAN = 240;
   public static final int SCRIPT_BOPOMOFO = 285;
   public static final int SCRIPT_HANGUL = 286;
   public static final int SCRIPT_GURMUKHI = 310;
   public static final int SCRIPT_GURMUKHI_2 = 1310;
   public static final int SCRIPT_DEVANAGARI = 315;
   public static final int SCRIPT_DEVANAGARI_2 = 1315;
   public static final int SCRIPT_GUJARATI = 320;
   public static final int SCRIPT_GUJARATI_2 = 1320;
   public static final int SCRIPT_BENGALI = 326;
   public static final int SCRIPT_BENGALI_2 = 1326;
   public static final int SCRIPT_ORIYA = 327;
   public static final int SCRIPT_ORIYA_2 = 1327;
   public static final int SCRIPT_TIBETAN = 330;
   public static final int SCRIPT_TELUGU = 340;
   public static final int SCRIPT_TELUGU_2 = 1340;
   public static final int SCRIPT_KANNADA = 345;
   public static final int SCRIPT_KANNADA_2 = 1345;
   public static final int SCRIPT_TAMIL = 346;
   public static final int SCRIPT_TAMIL_2 = 1346;
   public static final int SCRIPT_MALAYALAM = 347;
   public static final int SCRIPT_MALAYALAM_2 = 1347;
   public static final int SCRIPT_SINHALESE = 348;
   public static final int SCRIPT_BURMESE = 350;
   public static final int SCRIPT_THAI = 352;
   public static final int SCRIPT_KHMER = 355;
   public static final int SCRIPT_LAO = 356;
   public static final int SCRIPT_HIRAGANA = 410;
   public static final int SCRIPT_ETHIOPIC = 430;
   public static final int SCRIPT_HAN = 500;
   public static final int SCRIPT_KATAKANA = 410;
   public static final int SCRIPT_MATH = 995;
   public static final int SCRIPT_SYMBOL = 996;
   public static final int SCRIPT_UNDETERMINED = 998;
   public static final int SCRIPT_UNCODED = 999;
   private static final boolean USE_V2_INDIC = true;
   private static Map scriptTagsMap;
   private static Map scriptCodeMap;

   private CharScript() {
   }

   public static boolean isPunctuation(int c) {
      if (c >= 33 && c <= 47) {
         return true;
      } else if (c >= 58 && c <= 64) {
         return true;
      } else if (c >= 95 && c <= 96) {
         return true;
      } else if (c >= 126 && c <= 126) {
         return true;
      } else if (c >= 161 && c <= 191) {
         return true;
      } else if (c >= 215 && c <= 215) {
         return true;
      } else if (c >= 247 && c <= 247) {
         return true;
      } else {
         return c >= 8192 && c <= 8303;
      }
   }

   public static boolean isDigit(int c) {
      return c >= 48 && c <= 57;
   }

   public static boolean isHebrew(int c) {
      if (c >= 1424 && c <= 1535) {
         return true;
      } else {
         return c >= 64256 && c <= 64335;
      }
   }

   public static boolean isMongolian(int c) {
      return c >= 6144 && c <= 6319;
   }

   public static boolean isArabic(int c) {
      if (c >= 1536 && c <= 1791) {
         return true;
      } else if (c >= 1872 && c <= 1919) {
         return true;
      } else if (c >= 64336 && c <= 65023) {
         return true;
      } else {
         return c >= 65136 && c <= 65279;
      }
   }

   public static boolean isGreek(int c) {
      if (c >= 880 && c <= 1023) {
         return true;
      } else {
         return c >= 7936 && c <= 8191;
      }
   }

   public static boolean isLatin(int c) {
      if (c >= 65 && c <= 90) {
         return true;
      } else if (c >= 97 && c <= 122) {
         return true;
      } else if (c >= 192 && c <= 214) {
         return true;
      } else if (c >= 216 && c <= 223) {
         return true;
      } else if (c >= 224 && c <= 246) {
         return true;
      } else if (c >= 248 && c <= 255) {
         return true;
      } else if (c >= 256 && c <= 383) {
         return true;
      } else if (c >= 384 && c <= 591) {
         return true;
      } else if (c >= 7680 && c <= 7935) {
         return true;
      } else if (c >= 11360 && c <= 11391) {
         return true;
      } else if (c >= 42784 && c <= 43007) {
         return true;
      } else {
         return c >= 64256 && c <= 64271;
      }
   }

   public static boolean isCyrillic(int c) {
      if (c >= 1024 && c <= 1279) {
         return true;
      } else if (c >= 1280 && c <= 1327) {
         return true;
      } else if (c >= 11744 && c <= 11775) {
         return true;
      } else {
         return c >= 42560 && c <= 42655;
      }
   }

   public static boolean isGeorgian(int c) {
      if (c >= 4256 && c <= 4351) {
         return true;
      } else {
         return c >= 11520 && c <= 11567;
      }
   }

   public static boolean isHangul(int c) {
      if (c >= 4352 && c <= 4607) {
         return true;
      } else if (c >= 12592 && c <= 12687) {
         return true;
      } else if (c >= 43360 && c <= 43391) {
         return true;
      } else if (c >= 44032 && c <= 55203) {
         return true;
      } else {
         return c >= 55216 && c <= 55295;
      }
   }

   public static boolean isGurmukhi(int c) {
      return c >= 2560 && c <= 2687;
   }

   public static boolean isDevanagari(int c) {
      if (c >= 2304 && c <= 2431) {
         return true;
      } else {
         return c >= 43232 && c <= 43263;
      }
   }

   public static boolean isGujarati(int c) {
      return c >= 2688 && c <= 2815;
   }

   public static boolean isBengali(int c) {
      return c >= 2432 && c <= 2559;
   }

   public static boolean isOriya(int c) {
      return c >= 2816 && c <= 2943;
   }

   public static boolean isTibetan(int c) {
      return c >= 3840 && c <= 4095;
   }

   public static boolean isTelugu(int c) {
      return c >= 3072 && c <= 3199;
   }

   public static boolean isKannada(int c) {
      return c >= 3072 && c <= 3199;
   }

   public static boolean isTamil(int c) {
      return c >= 2944 && c <= 3071;
   }

   public static boolean isMalayalam(int c) {
      return c >= 3328 && c <= 3455;
   }

   public static boolean isSinhalese(int c) {
      return c >= 3456 && c <= 3583;
   }

   public static boolean isBurmese(int c) {
      if (c >= 4096 && c <= 4255) {
         return true;
      } else {
         return c >= 43616 && c <= 43647;
      }
   }

   public static boolean isThai(int c) {
      return c >= 3584 && c <= 3711;
   }

   public static boolean isKhmer(int c) {
      if (c >= 6016 && c <= 6143) {
         return true;
      } else {
         return c >= 6624 && c <= 6655;
      }
   }

   public static boolean isLao(int c) {
      return c >= 3712 && c <= 3839;
   }

   public static boolean isEthiopic(int c) {
      if (c >= 4608 && c <= 4991) {
         return true;
      } else if (c >= 4992 && c <= 5023) {
         return true;
      } else if (c >= 11648 && c <= 11743) {
         return true;
      } else {
         return c >= 43776 && c <= 43823;
      }
   }

   public static boolean isHan(int c) {
      if (c >= 13312 && c <= 19903) {
         return true;
      } else if (c >= 19968 && c <= 40959) {
         return true;
      } else if (c >= 63744 && c <= 64255) {
         return true;
      } else if (c >= 131072 && c <= 173791) {
         return true;
      } else if (c >= 173824 && c <= 177983) {
         return true;
      } else {
         return c >= 194560 && c <= 195103;
      }
   }

   public static boolean isBopomofo(int c) {
      return c >= 12544 && c <= 12591;
   }

   public static boolean isHiragana(int c) {
      return c >= 12352 && c <= 12447;
   }

   public static boolean isKatakana(int c) {
      if (c >= 12448 && c <= 12543) {
         return true;
      } else {
         return c >= 12784 && c <= 12799;
      }
   }

   public static int scriptOf(int c) {
      if (CharUtilities.isAnySpace(c)) {
         return 998;
      } else if (isPunctuation(c)) {
         return 998;
      } else if (isDigit(c)) {
         return 998;
      } else if (isLatin(c)) {
         return 215;
      } else if (isCyrillic(c)) {
         return 220;
      } else if (isGreek(c)) {
         return 200;
      } else if (isHan(c)) {
         return 500;
      } else if (isBopomofo(c)) {
         return 285;
      } else if (isKatakana(c)) {
         return 410;
      } else if (isHiragana(c)) {
         return 410;
      } else if (isHangul(c)) {
         return 286;
      } else if (isArabic(c)) {
         return 160;
      } else if (isHebrew(c)) {
         return 125;
      } else if (isMongolian(c)) {
         return 145;
      } else if (isGeorgian(c)) {
         return 240;
      } else if (isGurmukhi(c)) {
         return useV2IndicRules(310);
      } else if (isDevanagari(c)) {
         return useV2IndicRules(315);
      } else if (isGujarati(c)) {
         return useV2IndicRules(320);
      } else if (isBengali(c)) {
         return useV2IndicRules(326);
      } else if (isOriya(c)) {
         return useV2IndicRules(327);
      } else if (isTibetan(c)) {
         return 330;
      } else if (isTelugu(c)) {
         return useV2IndicRules(340);
      } else if (isKannada(c)) {
         return useV2IndicRules(345);
      } else if (isTamil(c)) {
         return useV2IndicRules(346);
      } else if (isMalayalam(c)) {
         return useV2IndicRules(347);
      } else if (isSinhalese(c)) {
         return 348;
      } else if (isBurmese(c)) {
         return 350;
      } else if (isThai(c)) {
         return 352;
      } else if (isKhmer(c)) {
         return 355;
      } else if (isLao(c)) {
         return 356;
      } else {
         return isEthiopic(c) ? 430 : 998;
      }
   }

   public static int useV2IndicRules(int sc) {
      return sc < 1000 ? sc + 1000 : sc;
   }

   public static int[] scriptsOf(CharSequence cs) {
      Set s = new HashSet();
      int i = 0;

      int ns;
      for(ns = cs.length(); i < ns; ++i) {
         s.add(scriptOf(cs.charAt(i)));
      }

      int[] sa = new int[s.size()];
      ns = 0;

      Object value;
      for(Iterator var4 = s.iterator(); var4.hasNext(); sa[ns++] = (Integer)value) {
         value = var4.next();
      }

      Arrays.sort(sa);
      return sa;
   }

   public static int dominantScript(CharSequence cs) {
      Map m = new HashMap();
      int sMax = 0;

      int cMax;
      Integer k;
      for(cMax = cs.length(); sMax < cMax; ++sMax) {
         int c = cs.charAt(sMax);
         int s = scriptOf(c);
         Integer k = s;
         k = (Integer)m.get(k);
         if (k != null) {
            m.put(k, k + 1);
         } else {
            m.put(k, 0);
         }
      }

      sMax = -1;
      cMax = -1;
      Iterator var11 = m.entrySet().iterator();

      while(var11.hasNext()) {
         Object o = var11.next();
         Map.Entry e = (Map.Entry)o;
         k = (Integer)e.getKey();
         int s = k;
         switch (s) {
            case 998:
            case 999:
               break;
            default:
               Integer v = (Integer)e.getValue();

               assert v != null;

               int c = v;
               if (c > cMax) {
                  cMax = c;
                  sMax = s;
               }
         }
      }

      if (sMax < 0) {
         sMax = 998;
      }

      return sMax;
   }

   public static boolean isIndicScript(String script) {
      return isIndicScript(scriptCodeFromTag(script));
   }

   public static boolean isIndicScript(int script) {
      switch (script) {
         case 310:
         case 315:
         case 320:
         case 326:
         case 327:
         case 340:
         case 345:
         case 346:
         case 347:
         case 350:
         case 355:
         case 1310:
         case 1315:
         case 1320:
         case 1326:
         case 1327:
         case 1340:
         case 1345:
         case 1346:
         case 1347:
            return true;
         default:
            return false;
      }
   }

   public static String scriptTagFromCode(int code) {
      Map m = getScriptTagsMap();
      if (m != null) {
         String tag;
         return (tag = (String)m.get(code)) != null ? tag : "";
      } else {
         return "";
      }
   }

   public static int scriptCodeFromTag(String tag) {
      Map m = getScriptCodeMap();
      if (m != null) {
         Integer c;
         return (c = (Integer)m.get(tag)) != null ? c : 998;
      } else {
         return 998;
      }
   }

   private static void putScriptTag(Map tm, Map cm, int code, String tag) {
      assert tag != null;

      assert tag.length() != 0;

      assert code >= 0;

      assert code < 2000;

      tm.put(code, tag);
      cm.put(tag, code);
   }

   private static void makeScriptMaps() {
      HashMap tm = new HashMap();
      HashMap cm = new HashMap();
      putScriptTag(tm, cm, 125, "hebr");
      putScriptTag(tm, cm, 145, "mong");
      putScriptTag(tm, cm, 160, "arab");
      putScriptTag(tm, cm, 200, "grek");
      putScriptTag(tm, cm, 215, "latn");
      putScriptTag(tm, cm, 220, "cyrl");
      putScriptTag(tm, cm, 240, "geor");
      putScriptTag(tm, cm, 285, "bopo");
      putScriptTag(tm, cm, 286, "hang");
      putScriptTag(tm, cm, 310, "guru");
      putScriptTag(tm, cm, 1310, "gur2");
      putScriptTag(tm, cm, 315, "deva");
      putScriptTag(tm, cm, 1315, "dev2");
      putScriptTag(tm, cm, 320, "gujr");
      putScriptTag(tm, cm, 1320, "gjr2");
      putScriptTag(tm, cm, 326, "beng");
      putScriptTag(tm, cm, 1326, "bng2");
      putScriptTag(tm, cm, 327, "orya");
      putScriptTag(tm, cm, 1327, "ory2");
      putScriptTag(tm, cm, 330, "tibt");
      putScriptTag(tm, cm, 340, "telu");
      putScriptTag(tm, cm, 1340, "tel2");
      putScriptTag(tm, cm, 345, "knda");
      putScriptTag(tm, cm, 1345, "knd2");
      putScriptTag(tm, cm, 346, "taml");
      putScriptTag(tm, cm, 1346, "tml2");
      putScriptTag(tm, cm, 347, "mlym");
      putScriptTag(tm, cm, 1347, "mlm2");
      putScriptTag(tm, cm, 348, "sinh");
      putScriptTag(tm, cm, 350, "mymr");
      putScriptTag(tm, cm, 352, "thai");
      putScriptTag(tm, cm, 355, "khmr");
      putScriptTag(tm, cm, 356, "laoo");
      putScriptTag(tm, cm, 410, "hira");
      putScriptTag(tm, cm, 430, "ethi");
      putScriptTag(tm, cm, 500, "hani");
      putScriptTag(tm, cm, 410, "kana");
      putScriptTag(tm, cm, 995, "zmth");
      putScriptTag(tm, cm, 996, "zsym");
      putScriptTag(tm, cm, 998, "zyyy");
      putScriptTag(tm, cm, 999, "zzzz");
      scriptTagsMap = tm;
      scriptCodeMap = cm;
   }

   private static Map getScriptTagsMap() {
      if (scriptTagsMap == null) {
         makeScriptMaps();
      }

      return scriptTagsMap;
   }

   private static Map getScriptCodeMap() {
      if (scriptCodeMap == null) {
         makeScriptMaps();
      }

      return scriptCodeMap;
   }
}
