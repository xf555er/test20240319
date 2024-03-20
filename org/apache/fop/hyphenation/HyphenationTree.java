package org.apache.fop.hyphenation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;

public class HyphenationTree extends TernaryTree implements PatternConsumer {
   private static final long serialVersionUID = -7842107987915665573L;
   protected ByteVector vspace = new ByteVector();
   protected HashMap stoplist = new HashMap(23);
   protected TernaryTree classmap = new TernaryTree();
   private transient TernaryTree ivalues;

   public HyphenationTree() {
      this.vspace.alloc(1);
   }

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
   }

   protected int packValues(String values) {
      int n = values.length();
      int m = (n & 1) == 1 ? (n >> 1) + 2 : (n >> 1) + 1;
      int offset = this.vspace.alloc(m);
      byte[] va = this.vspace.getArray();

      for(int i = 0; i < n; ++i) {
         int j = i >> 1;
         byte v = (byte)(values.charAt(i) - 48 + 1 & 15);
         if ((i & 1) == 1) {
            va[j + offset] |= v;
         } else {
            va[j + offset] = (byte)(v << 4);
         }
      }

      va[m - 1 + offset] = 0;
      return offset;
   }

   protected String unpackValues(int k) {
      StringBuffer buf = new StringBuffer();

      for(byte v = this.vspace.get(k++); v != 0; v = this.vspace.get(k++)) {
         char c = (char)((v >>> 4) - 1 + 48);
         buf.append(c);
         c = (char)(v & 15);
         if (c == 0) {
            break;
         }

         c = (char)(c - 1 + 48);
         buf.append(c);
      }

      return buf.toString();
   }

   public void loadPatterns(String filename) throws HyphenationException {
      File f = new File(filename);

      try {
         InputSource src = new InputSource(f.toURI().toURL().toExternalForm());
         this.loadPatterns(src);
      } catch (MalformedURLException var4) {
         throw new HyphenationException("Error converting the File '" + f + "' to a URL: " + var4.getMessage());
      }
   }

   public void loadPatterns(InputSource source) throws HyphenationException {
      PatternParser pp = new PatternParser(this);
      this.ivalues = new TernaryTree();
      pp.parse(source);
      this.trimToSize();
      this.vspace.trimToSize();
      this.classmap.trimToSize();
      this.ivalues = null;
   }

   public String findPattern(String pat) {
      int k = super.find(pat);
      return k >= 0 ? this.unpackValues(k) : "";
   }

   protected int hstrcmp(char[] s, int si, char[] t, int ti) {
      while(s[si] == t[ti]) {
         if (s[si] == 0) {
            return 0;
         }

         ++si;
         ++ti;
      }

      if (t[ti] == 0) {
         return 0;
      } else {
         return s[si] - t[ti];
      }
   }

   protected byte[] getValues(int k) {
      StringBuffer buf = new StringBuffer();

      for(byte v = this.vspace.get(k++); v != 0; v = this.vspace.get(k++)) {
         char c = (char)((v >>> 4) - 1);
         buf.append(c);
         c = (char)(v & 15);
         if (c == 0) {
            break;
         }

         --c;
         buf.append(c);
      }

      byte[] res = new byte[buf.length()];

      for(int i = 0; i < res.length; ++i) {
         res[i] = (byte)buf.charAt(i);
      }

      return res;
   }

   protected void searchPatterns(char[] word, int index, byte[] il) {
      int i = index;
      char sp = word[index];
      char p = this.root;

      label79:
      while(p > 0 && p < this.sc.length) {
         byte[] values;
         int j;
         int var12;
         if (this.sc[p] == '\uffff') {
            if (this.hstrcmp(word, i, this.kv.getArray(), this.lo[p]) == 0) {
               values = this.getValues(this.eq[p]);
               j = index;
               byte[] var15 = values;
               int var16 = values.length;

               for(var12 = 0; var12 < var16; ++var12) {
                  byte value = var15[var12];
                  if (j < il.length && value > il[j]) {
                     il[j] = value;
                  }

                  ++j;
               }
            }

            return;
         }

         j = sp - this.sc[p];
         if (j == 0) {
            if (sp == 0) {
               break;
            }

            ++i;
            sp = word[i];
            p = this.eq[p];

            for(char q = p; q > 0 && q < this.sc.length && this.sc[q] != '\uffff'; q = this.lo[q]) {
               if (this.sc[q] == 0) {
                  values = this.getValues(this.eq[q]);
                  int j = index;
                  byte[] var11 = values;
                  var12 = values.length;
                  int var13 = 0;

                  while(true) {
                     if (var13 >= var12) {
                        continue label79;
                     }

                     byte value = var11[var13];
                     if (j < il.length && value > il[j]) {
                        il[j] = value;
                     }

                     ++j;
                     ++var13;
                  }
               }
            }
         } else {
            p = j < 0 ? this.lo[p] : this.hi[p];
         }
      }

   }

   public Hyphenation hyphenate(String word, int remainCharCount, int pushCharCount) {
      char[] w = word.toCharArray();
      if (this.isMultiPartWord(w, w.length)) {
         List words = this.splitOnNonCharacters(w);
         return new Hyphenation(new String(w), this.getHyphPointsForWords(words, remainCharCount, pushCharCount));
      } else {
         return this.hyphenate(w, 0, w.length, remainCharCount, pushCharCount);
      }
   }

   private boolean isMultiPartWord(char[] w, int len) {
      int wordParts = 0;

      for(int i = 0; i < len; ++i) {
         char[] c = new char[]{w[i], '\u0000'};
         int nc = this.classmap.find(c, 0);
         if (nc > 0) {
            if (wordParts > 1) {
               return true;
            }

            wordParts = 1;
         } else if (wordParts == 1) {
            ++wordParts;
         }
      }

      return false;
   }

   private List splitOnNonCharacters(char[] word) {
      List breakPoints = this.getNonLetterBreaks(word);
      if (breakPoints.size() == 0) {
         return Collections.emptyList();
      } else {
         List words = new ArrayList();

         for(int ibreak = 0; ibreak < breakPoints.size(); ++ibreak) {
            char[] newWord = this.getWordFromCharArray(word, ibreak == 0 ? 0 : (Integer)breakPoints.get(ibreak - 1), (Integer)breakPoints.get(ibreak));
            words.add(newWord);
         }

         if (word.length - (Integer)breakPoints.get(breakPoints.size() - 1) - 1 > 1) {
            char[] newWord = this.getWordFromCharArray(word, (Integer)breakPoints.get(breakPoints.size() - 1), word.length);
            words.add(newWord);
         }

         return words;
      }
   }

   private List getNonLetterBreaks(char[] word) {
      char[] c = new char[2];
      List breakPoints = new ArrayList();
      boolean foundLetter = false;

      for(int i = 0; i < word.length; ++i) {
         c[0] = word[i];
         if (this.classmap.find(c, 0) < 0) {
            if (foundLetter) {
               breakPoints.add(i);
            }
         } else {
            foundLetter = true;
         }
      }

      return breakPoints;
   }

   private char[] getWordFromCharArray(char[] word, int startIndex, int endIndex) {
      char[] newWord = new char[endIndex - (startIndex == 0 ? startIndex : startIndex + 1)];
      int iChar = 0;

      for(int i = startIndex == 0 ? 0 : startIndex + 1; i < endIndex; ++i) {
         newWord[iChar++] = word[i];
      }

      return newWord;
   }

   private int[] getHyphPointsForWords(List nonLetterWords, int remainCharCount, int pushCharCount) {
      int[] breaks = new int[0];

      for(int iNonLetterWord = 0; iNonLetterWord < nonLetterWords.size(); ++iNonLetterWord) {
         char[] nonLetterWord = (char[])nonLetterWords.get(iNonLetterWord);
         Hyphenation curHyph = this.hyphenate(nonLetterWord, 0, nonLetterWord.length, iNonLetterWord == 0 ? remainCharCount : 1, iNonLetterWord == nonLetterWords.size() - 1 ? pushCharCount : 1);
         if (curHyph != null) {
            int[] combined = new int[breaks.length + curHyph.getHyphenationPoints().length];
            int[] hyphPoints = curHyph.getHyphenationPoints();
            int foreWordsSize = this.calcForeWordsSize(nonLetterWords, iNonLetterWord);

            for(int i = 0; i < hyphPoints.length; ++i) {
               hyphPoints[i] += foreWordsSize;
            }

            System.arraycopy(breaks, 0, combined, 0, breaks.length);
            System.arraycopy(hyphPoints, 0, combined, breaks.length, hyphPoints.length);
            breaks = combined;
         }
      }

      return breaks;
   }

   private int calcForeWordsSize(List nonLetterWords, int iNonLetterWord) {
      int result = 0;

      for(int i = 0; i < iNonLetterWord; ++i) {
         result += ((char[])nonLetterWords.get(i)).length + 1;
      }

      return result;
   }

   public Hyphenation hyphenate(char[] w, int offset, int len, int remainCharCount, int pushCharCount) {
      char[] word = new char[len + 3];
      char[] c = new char[2];
      int iIgnoreAtBeginning = 0;
      int iLength = len;
      boolean bEndOfLetters = false;

      int i;
      for(i = 1; i <= len; ++i) {
         c[0] = w[offset + i - 1];
         int nc = this.classmap.find(c, 0);
         if (nc < 0) {
            if (i == 1 + iIgnoreAtBeginning) {
               ++iIgnoreAtBeginning;
            } else {
               bEndOfLetters = true;
            }

            --iLength;
         } else {
            if (bEndOfLetters) {
               return null;
            }

            word[i - iIgnoreAtBeginning] = (char)nc;
         }
      }

      len = iLength;
      if (iLength < remainCharCount + pushCharCount) {
         return null;
      } else {
         int[] result = new int[iLength + 1];
         int k = 0;
         String sw = new String(word, 1, iLength);
         if (this.stoplist.containsKey(sw)) {
            ArrayList hw = (ArrayList)this.stoplist.get(sw);
            int j = 0;

            for(i = 0; i < hw.size(); ++i) {
               Object o = hw.get(i);
               if (o instanceof String) {
                  j += ((String)o).length();
                  if (j >= remainCharCount && j < len - pushCharCount) {
                     result[k++] = j + iIgnoreAtBeginning;
                  }
               }
            }
         } else {
            word[0] = '.';
            word[iLength + 1] = '.';
            word[iLength + 2] = 0;
            byte[] il = new byte[iLength + 3];

            for(i = 0; i < len + 1; ++i) {
               this.searchPatterns(word, i, il);
            }

            for(i = 0; i < len; ++i) {
               if ((il[i + 1] & 1) == 1 && i >= remainCharCount && i <= len - pushCharCount) {
                  result[k++] = i + iIgnoreAtBeginning;
               }
            }
         }

         if (k > 0) {
            int[] res = new int[k];
            System.arraycopy(result, 0, res, 0, k);
            return new Hyphenation(new String(w, offset, len), res);
         } else {
            return null;
         }
      }
   }

   public void addClass(String chargroup) {
      if (chargroup.length() > 0) {
         char equivChar = chargroup.charAt(0);
         char[] key = new char[]{'\u0000', '\u0000'};

         for(int i = 0; i < chargroup.length(); ++i) {
            key[0] = chargroup.charAt(i);
            this.classmap.insert(key, 0, equivChar);
         }
      }

   }

   public void addException(String word, ArrayList hyphenatedword) {
      this.stoplist.put(word, hyphenatedword);
   }

   public void addPattern(String pattern, String ivalue) {
      int k = this.ivalues.find(ivalue);
      if (k <= 0) {
         k = this.packValues(ivalue);
         this.ivalues.insert(ivalue, (char)k);
      }

      this.insert(pattern, (char)k);
   }

   public void printStats() {
      System.out.println("Value space size = " + Integer.toString(this.vspace.length()));
      super.printStats();
   }

   public static void main(String[] argv) throws Exception {
      HyphenationTree ht = null;
      int minCharCount = 2;
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      while(true) {
         System.out.print("l:\tload patterns from XML\nL:\tload patterns from serialized object\ns:\tset minimum character count\nw:\twrite hyphenation tree to object file\nh:\thyphenate\nf:\tfind pattern\nb:\tbenchmark\nq:\tquit\n\nCommand:");
         String token = in.readLine();
         if (token == null) {
            break;
         }

         token = token.trim();
         if (token.equals("f")) {
            System.out.print("Pattern: ");
            token = in.readLine();
            if (token == null) {
               break;
            }

            token = token.trim();
            System.out.println("Values: " + ht.findPattern(token));
         } else if (token.equals("s")) {
            System.out.print("Minimun value: ");
            token = in.readLine();
            if (token == null) {
               break;
            }

            token = token.trim();
            minCharCount = Integer.parseInt(token);
         } else if (token.equals("l")) {
            ht = new HyphenationTree();
            System.out.print("XML file name: ");
            token = in.readLine();
            if (token == null) {
               break;
            }

            token = token.trim();
            ht.loadPatterns(token);
         } else if (token.equals("L")) {
            ObjectInputStream ois = null;
            System.out.print("Object file name: ");
            token = in.readLine();
            if (token == null) {
               break;
            }

            token = token.trim();

            try {
               ois = new ObjectInputStream(new FileInputStream(token));
               ht = (HyphenationTree)ois.readObject();
            } catch (Exception var58) {
               var58.printStackTrace();
            } finally {
               if (ois != null) {
                  try {
                     ois.close();
                  } catch (IOException var54) {
                  }
               }

            }
         } else if (token.equals("w")) {
            System.out.print("Object file name: ");
            token = in.readLine();
            if (token == null) {
               break;
            }

            token = token.trim();
            ObjectOutputStream oos = null;

            try {
               oos = new ObjectOutputStream(new FileOutputStream(token));
               oos.writeObject(ht);
            } catch (Exception var57) {
               var57.printStackTrace();
            } finally {
               if (oos != null) {
                  try {
                     oos.flush();
                  } catch (IOException var56) {
                  }

                  try {
                     oos.close();
                  } catch (IOException var55) {
                  }
               }

            }
         } else if (token.equals("h")) {
            System.out.print("Word: ");
            token = in.readLine();
            if (token == null) {
               break;
            }

            token = token.trim();
            System.out.print("Hyphenation points: ");
            System.out.println(ht.hyphenate(token, minCharCount, minCharCount));
         } else if (!token.equals("b")) {
            if (token.equals("q")) {
               break;
            }
         } else {
            if (ht == null) {
               System.out.println("No patterns have been loaded.");
               break;
            }

            System.out.print("Word list filename: ");
            token = in.readLine();
            if (token == null) {
               break;
            }

            token = token.trim();
            long starttime = 0L;
            int counter = 0;
            BufferedReader reader = null;

            try {
               reader = new BufferedReader(new FileReader(token));

               String line;
               for(starttime = System.currentTimeMillis(); (line = reader.readLine()) != null; ++counter) {
                  Hyphenation hyp = ht.hyphenate(line, minCharCount, minCharCount);
                  if (hyp != null) {
                     String var11 = hyp.toString();
                  }
               }
            } catch (Exception var61) {
               System.out.println("Exception " + var61);
               var61.printStackTrace();
            } finally {
               IOUtils.closeQuietly((Reader)reader);
            }

            long endtime = System.currentTimeMillis();
            long result = endtime - starttime;
            System.out.println(counter + " words in " + result + " Milliseconds hyphenated");
         }
      }

   }
}
