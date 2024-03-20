package org.apache.fop.complexscripts.bidi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.traits.Direction;
import org.apache.fop.util.CharUtilities;

public final class UnicodeBidiAlgorithm implements BidiConstants {
   private static final Log log = LogFactory.getLog(UnicodeBidiAlgorithm.class);

   private UnicodeBidiAlgorithm() {
   }

   public static int[] resolveLevels(CharSequence cs, Direction defaultLevel) {
      int[] chars = new int[cs.length()];
      return !convertToScalar(cs, chars) && defaultLevel != Direction.RL ? null : resolveLevels(chars, defaultLevel == Direction.RL ? 1 : 0, new int[chars.length]);
   }

   public static int[] resolveLevels(int[] chars, int defaultLevel, int[] levels) {
      return resolveLevels(chars, getClasses(chars), defaultLevel, levels, false);
   }

   public static int[] resolveLevels(int[] chars, int[] classes, int defaultLevel, int[] levels, boolean useRuleL1) {
      int[] wca = copySequence(classes);
      int[] ea = new int[levels.length];
      resolveExplicit(wca, defaultLevel, ea);
      resolveRuns(wca, defaultLevel, ea, levelsFromEmbeddings(ea, levels));
      if (useRuleL1) {
         resolveSeparators(classes, wca, defaultLevel, levels);
      }

      dump("RL: CC(" + (chars != null ? chars.length : -1) + ")", chars, classes, defaultLevel, levels);
      return levels;
   }

   private static int[] copySequence(int[] ta) {
      int[] na = new int[ta.length];
      System.arraycopy(ta, 0, na, 0, na.length);
      return na;
   }

   private static void resolveExplicit(int[] wca, int defaultLevel, int[] ea) {
      int[] es = new int[61];
      int ei = 0;
      int ec = defaultLevel;
      int i = 0;

      for(int n = wca.length; i < n; ++i) {
         int bc = wca[i];
         int el;
         switch (bc) {
            case 2:
            case 3:
            case 6:
            case 7:
               int en;
               if (bc != 6 && bc != 7) {
                  en = (ec & -129) + 2 & -2;
               } else {
                  en = (ec & -129) + 1 | 1;
               }

               if (en < 62) {
                  es[ei++] = ec;
                  if (bc != 3 && bc != 7) {
                     ec = en & -129;
                  } else {
                     ec = en | 128;
                  }
               }

               el = ec;
               break;
            case 4:
            case 5:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            default:
               el = ec;
               break;
            case 8:
               el = ec;
               if (ei > 0) {
                  --ei;
                  ec = es[ei];
               }
               break;
            case 16:
               ec = defaultLevel;
               el = defaultLevel;
               ei = 0;
         }

         switch (bc) {
            case 2:
            case 3:
            case 6:
            case 7:
            case 8:
               wca[i] = 15;
               break;
            case 4:
            case 5:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            default:
               if ((el & 128) != 0) {
                  wca[i] = directionOfLevel(el);
               }
            case 15:
         }

         ea[i] = el;
      }

   }

   private static int directionOfLevel(int level) {
      return (level & 1) != 0 ? 4 : 1;
   }

   private static int levelOfEmbedding(int embedding) {
      return embedding & -129;
   }

   private static int[] levelsFromEmbeddings(int[] ea, int[] la) {
      assert ea != null;

      assert la != null;

      assert la.length == ea.length;

      int i = 0;

      for(int n = la.length; i < n; ++i) {
         la[i] = levelOfEmbedding(ea[i]);
      }

      return la;
   }

   private static void resolveRuns(int[] wca, int defaultLevel, int[] ea, int[] la) {
      if (la.length != wca.length) {
         throw new IllegalArgumentException("levels sequence length must match classes sequence length");
      } else if (la.length != ea.length) {
         throw new IllegalArgumentException("levels sequence length must match embeddings sequence length");
      } else {
         int i = 0;
         int n = ea.length;

         int e;
         for(int lPrev = defaultLevel; i < n; i = e) {
            e = i;
            int l = findNextNonRetainedFormattingLevel(wca, ea, i, lPrev);

            while(e < n) {
               if (la[e] != l) {
                  if (!startsWithRetainedFormattingRun(wca, ea, e)) {
                     break;
                  }

                  e += getLevelRunLength(ea, e);
               } else {
                  ++e;
               }
            }

            lPrev = resolveRun(wca, defaultLevel, ea, la, i, e, l, lPrev);
         }

      }
   }

   private static int findNextNonRetainedFormattingLevel(int[] wca, int[] ea, int start, int lPrev) {
      int s = start;

      int e;
      for(e = wca.length; s < e && startsWithRetainedFormattingRun(wca, ea, s); s += getLevelRunLength(ea, s)) {
      }

      return s < e ? levelOfEmbedding(ea[s]) : lPrev;
   }

   private static int getLevelRunLength(int[] ea, int start) {
      assert start < ea.length;

      int nl = 0;
      int s = start;
      int e = ea.length;

      for(int l0 = levelOfEmbedding(ea[start]); s < e && levelOfEmbedding(ea[s]) == l0; ++s) {
         ++nl;
      }

      return nl;
   }

   private static boolean startsWithRetainedFormattingRun(int[] wca, int[] ea, int start) {
      int nl = getLevelRunLength(ea, start);
      if (nl > 0) {
         int nc = getRetainedFormattingRunLength(wca, start);
         return nc >= nl;
      } else {
         return false;
      }
   }

   private static int getRetainedFormattingRunLength(int[] wca, int start) {
      assert start < wca.length;

      int nc = 0;
      int s = start;

      for(int e = wca.length; s < e && wca[s] == 15; ++s) {
         ++nc;
      }

      return nc;
   }

   private static int resolveRun(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int levelPrev) {
      int sor = directionOfLevel(max(levelPrev, level));
      int le = -1;
      int i;
      if (end == wca.length) {
         le = max(level, defaultLevel);
      } else {
         for(i = end; i < wca.length; ++i) {
            if (wca[i] != 15) {
               le = max(level, la[i]);
               break;
            }
         }

         if (le < 0) {
            le = max(level, defaultLevel);
         }
      }

      i = directionOfLevel(le);
      if (log.isDebugEnabled()) {
         log.debug("BR[" + padLeft(start, 3) + "," + padLeft(end, 3) + "] :" + padLeft(level, 2) + ": SOR(" + getClassName(sor) + "), EOR(" + getClassName(i) + ")");
      }

      resolveWeak(wca, defaultLevel, ea, la, start, end, level, sor, i);
      resolveNeutrals(wca, defaultLevel, ea, la, start, end, level, sor, i);
      resolveImplicit(wca, defaultLevel, ea, la, start, end, level, sor, i);
      return isRetainedFormatting(wca, start, end) ? levelPrev : level;
   }

   private static void resolveWeak(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor) {
      int i = start;
      int n = end;

      int bcPrev;
      int bc;
      for(bcPrev = sor; i < n; ++i) {
         bc = wca[i];
         if (bc == 14) {
            wca[i] = bcPrev;
         } else if (bc != 15) {
            bcPrev = bc;
         }
      }

      i = start;
      n = end;

      for(bcPrev = sor; i < n; ++i) {
         bc = wca[i];
         if (bc == 9) {
            if (bcPrev == 5) {
               wca[i] = 12;
            }
         } else if (isStrong(bc)) {
            bcPrev = bc;
         }
      }

      i = start;

      for(n = end; i < n; ++i) {
         bcPrev = wca[i];
         if (bcPrev == 5) {
            wca[i] = 4;
         }
      }

      i = start;
      n = end;

      int bcNext;
      int j;
      for(bcPrev = sor; i < n; ++i) {
         bc = wca[i];
         if (bc == 10) {
            bcNext = eor;

            for(j = i + 1; j < n; ++j) {
               if ((bc = wca[j]) != 15) {
                  bcNext = bc;
                  break;
               }
            }

            if (bcPrev == 9 && bcNext == 9) {
               wca[i] = 9;
            }
         } else if (bc == 13) {
            bcNext = eor;

            for(j = i + 1; j < n; ++j) {
               if ((bc = wca[j]) != 15) {
                  bcNext = bc;
                  break;
               }
            }

            if (bcPrev == 9 && bcNext == 9) {
               wca[i] = 9;
            } else if (bcPrev == 12 && bcNext == 12) {
               wca[i] = 12;
            }
         }

         if (bc != 15) {
            bcPrev = bc;
         }
      }

      i = start;
      n = end;

      for(bcPrev = sor; i < n; ++i) {
         bc = wca[i];
         if (bc != 11) {
            if (bc != 15) {
               bcPrev = bc;
            }
         } else {
            bcNext = eor;

            for(j = i + 1; j < n; ++j) {
               bc = wca[j];
               if (bc != 15 && bc != 11) {
                  bcNext = bc;
                  break;
               }
            }

            if (bcPrev == 9 || bcNext == 9) {
               wca[i] = 9;
            }
         }
      }

      i = start;

      for(n = end; i < n; ++i) {
         bcPrev = wca[i];
         if (bcPrev == 11 || bcPrev == 10 || bcPrev == 13) {
            wca[i] = 19;
            resolveAdjacentBoundaryNeutrals(wca, start, end, i, 19);
         }
      }

      i = start;
      n = end;

      for(bcPrev = sor; i < n; ++i) {
         bc = wca[i];
         if (bc == 9) {
            if (bcPrev == 1) {
               wca[i] = 1;
            }
         } else if (bc == 1 || bc == 4) {
            bcPrev = bc;
         }
      }

   }

   private static void resolveNeutrals(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor) {
      int i = start;
      int n = end;

      int bcPrev;
      int bc;
      for(bcPrev = sor; i < n; ++i) {
         bc = wca[i];
         if (!isNeutral(bc)) {
            if (bc != 1 && bc != 4) {
               if (bc == 12 || bc == 9) {
                  bcPrev = 4;
               }
            } else {
               bcPrev = bc;
            }
         } else {
            int bcNext = eor;
            int j = i + 1;

            while(j < n) {
               bc = wca[j];
               if (bc != 1 && bc != 4) {
                  if (bc != 12 && bc != 9) {
                     if (!isNeutral(bc) && !isRetainedFormatting(bc)) {
                        break;
                     }

                     ++j;
                     continue;
                  }

                  bcNext = 4;
                  break;
               }

               bcNext = bc;
               break;
            }

            if (bcPrev == bcNext) {
               wca[i] = bcPrev;
               resolveAdjacentBoundaryNeutrals(wca, start, end, i, bcPrev);
            }
         }
      }

      i = start;

      for(n = end; i < n; ++i) {
         bcPrev = wca[i];
         if (isNeutral(bcPrev)) {
            bc = directionOfLevel(levelOfEmbedding(ea[i]));
            wca[i] = bc;
            resolveAdjacentBoundaryNeutrals(wca, start, end, i, bc);
         }
      }

   }

   private static void resolveAdjacentBoundaryNeutrals(int[] wca, int start, int end, int index, int bcNew) {
      if (index >= start && index < end) {
         int i;
         int bc;
         for(i = index - 1; i >= start; --i) {
            bc = wca[i];
            if (bc != 15) {
               break;
            }

            wca[i] = bcNew;
         }

         for(i = index + 1; i < end; ++i) {
            bc = wca[i];
            if (bc != 15) {
               break;
            }

            wca[i] = bcNew;
         }

      } else {
         throw new IllegalArgumentException();
      }
   }

   private static void resolveImplicit(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor) {
      int i = start;

      for(int n = end; i < n; ++i) {
         int bc = wca[i];
         int el = la[i];
         int ed = 0;
         if ((el & 1) == 0) {
            if (bc == 4) {
               ed = 1;
            } else if (bc == 12) {
               ed = 2;
            } else if (bc == 9) {
               ed = 2;
            }
         } else if (bc == 1) {
            ed = 1;
         } else if (bc == 9) {
            ed = 1;
         } else if (bc == 12) {
            ed = 1;
         }

         la[i] = el + ed;
      }

   }

   private static void resolveSeparators(int[] ica, int[] wca, int dl, int[] la) {
      int i = 0;

      int k;
      int ic;
      for(k = ica.length; i < k; ++i) {
         ic = ica[i];
         if (ic == 17 || ic == 16) {
            la[i] = dl;

            for(int k = i - 1; k >= 0; --k) {
               int pc = ica[k];
               if (!isRetainedFormatting(pc)) {
                  if (pc != 18) {
                     break;
                  }

                  la[k] = dl;
               }
            }
         }
      }

      for(i = ica.length; i > 0; --i) {
         k = i - 1;
         ic = ica[k];
         if (!isRetainedFormatting(ic)) {
            if (ic != 18) {
               break;
            }

            la[k] = dl;
         }
      }

      i = 0;

      for(k = ica.length; i < k; ++i) {
         ic = ica[i];
         if (isRetainedFormatting(ic)) {
            if (i == 0) {
               la[i] = dl;
            } else {
               la[i] = la[i - 1];
            }
         }
      }

   }

   private static boolean isStrong(int bc) {
      switch (bc) {
         case 1:
         case 4:
         case 5:
            return true;
         case 2:
         case 3:
         default:
            return false;
      }
   }

   private static boolean isNeutral(int bc) {
      switch (bc) {
         case 16:
         case 17:
         case 18:
         case 19:
            return true;
         default:
            return false;
      }
   }

   private static boolean isRetainedFormatting(int bc) {
      switch (bc) {
         case 2:
         case 3:
         case 6:
         case 7:
         case 8:
         case 15:
            return true;
         case 4:
         case 5:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         default:
            return false;
      }
   }

   private static boolean isRetainedFormatting(int[] ca, int s, int e) {
      for(int i = s; i < e; ++i) {
         if (!isRetainedFormatting(ca[i])) {
            return false;
         }
      }

      return true;
   }

   private static int max(int x, int y) {
      return x > y ? x : y;
   }

   private static int[] getClasses(int[] chars) {
      int[] classes = new int[chars.length];
      int i = 0;

      for(int n = chars.length; i < n; ++i) {
         int ch = chars[i];
         int bc;
         if (ch >= 0) {
            bc = BidiClass.getBidiClass(chars[i]);
         } else {
            bc = 20;
         }

         classes[i] = bc;
      }

      return classes;
   }

   private static boolean convertToScalar(CharSequence cs, int[] chars) throws IllegalArgumentException {
      boolean triggered = false;
      if (chars.length != cs.length()) {
         throw new IllegalArgumentException("characters array length must match input sequence length");
      } else {
         int i = 0;
         int n = chars.length;

         while(i < n) {
            int chIn = cs.charAt(i);
            int chOut;
            if (chIn < '\ud800') {
               chOut = chIn;
            } else if (chIn < '\udc00') {
               if (i + 1 >= n) {
                  throw new IllegalArgumentException("truncated surrogate pair");
               }

               int chLo = cs.charAt(i + 1);
               if (chLo < '\udc00' || chLo > '\udfff') {
                  throw new IllegalArgumentException("isolated high surrogate");
               }

               chOut = convertToScalar(chIn, chLo);
            } else {
               if (chIn < '\ue000') {
                  throw new IllegalArgumentException("isolated low surrogate");
               }

               chOut = chIn;
            }

            if (!triggered && triggersBidi(chOut)) {
               triggered = true;
            }

            if ((chOut & 16711680) == 0) {
               chars[i++] = chOut;
            } else {
               chars[i++] = chOut;
               chars[i++] = -1;
            }
         }

         return triggered;
      }
   }

   private static int convertToScalar(int chHi, int chLo) {
      if (chHi >= 55296 && chHi <= 56319) {
         if (chLo >= 56320 && chLo <= 57343) {
            return ((chHi & 1023) << 10 | chLo & 1023) + 65536;
         } else {
            throw new IllegalArgumentException("bad low surrogate");
         }
      } else {
         throw new IllegalArgumentException("bad high surrogate");
      }
   }

   private static boolean triggersBidi(int var0) {
      // $FF: Couldn't be decompiled
   }

   private static void dump(String header, int[] chars, int[] classes, int defaultLevel, int[] levels) {
      log.debug(header);
      log.debug("BD: default level(" + defaultLevel + ")");
      StringBuffer sb = new StringBuffer();
      int i;
      int n;
      int ch;
      if (chars != null) {
         i = 0;

         for(n = chars.length; i < n; ++i) {
            ch = chars[i];
            sb.setLength(0);
            if (ch > 32 && ch < 127) {
               sb.append((char)ch);
            } else {
               sb.append(CharUtilities.charToNCRef(ch));
            }

            for(int k = sb.length(); k < 12; ++k) {
               sb.append(' ');
            }

            sb.append(": " + padRight(getClassName(classes[i]), 4) + " " + levels[i]);
            log.debug(sb);
         }
      } else {
         i = 0;

         for(n = classes.length; i < n; ++i) {
            sb.setLength(0);

            for(ch = sb.length(); ch < 12; ++ch) {
               sb.append(' ');
            }

            sb.append(": " + padRight(getClassName(classes[i]), 4) + " " + levels[i]);
            log.debug(sb);
         }
      }

   }

   private static String getClassName(int bc) {
      switch (bc) {
         case 1:
            return "L";
         case 2:
            return "LRE";
         case 3:
            return "LRO";
         case 4:
            return "R";
         case 5:
            return "AL";
         case 6:
            return "RLE";
         case 7:
            return "RLO";
         case 8:
            return "PDF";
         case 9:
            return "EN";
         case 10:
            return "ES";
         case 11:
            return "ET";
         case 12:
            return "AN";
         case 13:
            return "CS";
         case 14:
            return "NSM";
         case 15:
            return "BN";
         case 16:
            return "B";
         case 17:
            return "S";
         case 18:
            return "WS";
         case 19:
            return "ON";
         case 20:
            return "SUR";
         default:
            return "?";
      }
   }

   private static String padLeft(int n, int width) {
      return padLeft(Integer.toString(n), width);
   }

   private static String padLeft(String s, int width) {
      StringBuffer sb = new StringBuffer();

      for(int i = s.length(); i < width; ++i) {
         sb.append(' ');
      }

      sb.append(s);
      return sb.toString();
   }

   private static String padRight(String s, int width) {
      StringBuffer sb = new StringBuffer(s);

      for(int i = sb.length(); i < width; ++i) {
         sb.append(' ');
      }

      return sb.toString();
   }
}
