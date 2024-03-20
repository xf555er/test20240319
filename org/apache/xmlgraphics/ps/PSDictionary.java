package org.apache.xmlgraphics.ps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class PSDictionary extends HashMap {
   private static final long serialVersionUID = 815367222496219197L;

   public static PSDictionary valueOf(String str) throws PSDictionaryFormatException {
      return (new Maker()).parseDictionary(str);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof PSDictionary)) {
         return false;
      } else {
         PSDictionary dictionaryObj = (PSDictionary)obj;
         if (dictionaryObj.size() != this.size()) {
            return false;
         } else {
            Iterator var3 = this.entrySet().iterator();

            Map.Entry entry;
            String key;
            do {
               if (!var3.hasNext()) {
                  return true;
               }

               Object e = var3.next();
               entry = (Map.Entry)e;
               key = (String)entry.getKey();
               if (!dictionaryObj.containsKey(key)) {
                  return false;
               }
            } while(dictionaryObj.get(key).equals(entry.getValue()));

            return false;
         }
      }
   }

   public int hashCode() {
      int hashCode = 7;

      Object value;
      for(Iterator var2 = this.values().iterator(); var2.hasNext(); hashCode += value.hashCode()) {
         value = var2.next();
      }

      return hashCode;
   }

   public String toString() {
      if (this.isEmpty()) {
         return "";
      } else {
         StringBuffer sb = new StringBuffer("<<\n");
         Iterator var2 = super.keySet().iterator();

         while(true) {
            while(var2.hasNext()) {
               Object o = var2.next();
               String key = (String)o;
               sb.append("  " + key + " ");
               Object obj = super.get(key);
               if (obj instanceof ArrayList) {
                  List array = (List)obj;
                  StringBuilder str = new StringBuilder("[");
                  Iterator var8 = array.iterator();

                  while(var8.hasNext()) {
                     Object element = var8.next();
                     str.append(element + " ");
                  }

                  String str2 = str.toString().trim();
                  str2 = str2 + "]";
                  sb.append(str2 + "\n");
               } else {
                  sb.append(obj.toString() + "\n");
               }
            }

            sb.append(">>");
            return sb.toString();
         }
      }
   }

   private static class Maker {
      private static final String[][] BRACES = new String[][]{{"<<", ">>"}, {"[", "]"}, {"{", "}"}, {"(", ")"}};
      private static final int OPENING = 0;
      private static final int CLOSING = 1;
      private static final int DICTIONARY = 0;
      private static final int ARRAY = 1;
      private static final int PROCEDURE = 2;
      private static final int STRING = 3;

      private Maker() {
      }

      protected Token nextToken(String str, int fromIndex) {
         Token t = null;

         for(int i = fromIndex; i < str.length(); ++i) {
            boolean isWhitespace = Character.isWhitespace(str.charAt(i));
            if (t == null && !isWhitespace) {
               t = new Token();
               t.startIndex = i;
            } else if (t != null && isWhitespace) {
               t.endIndex = i;
               break;
            }
         }

         if (t != null) {
            if (t.endIndex == -1) {
               t.endIndex = str.length();
            }

            t.value = str.substring(t.startIndex, t.endIndex);
         }

         return t;
      }

      private int indexOfMatchingBrace(String str, String[] braces, int fromIndex) throws PSDictionaryFormatException {
         int len = str.length();
         if (braces.length != 2) {
            throw new PSDictionaryFormatException("Wrong number of braces");
         } else {
            int openCnt = 0;

            for(int closeCnt = 0; fromIndex < len; ++fromIndex) {
               if (str.startsWith(braces[0], fromIndex)) {
                  ++openCnt;
               } else if (str.startsWith(braces[1], fromIndex)) {
                  ++closeCnt;
                  if (openCnt > 0 && openCnt == closeCnt) {
                     return fromIndex;
                  }
               }
            }

            return -1;
         }
      }

      private String stripBraces(String str, String[] braces) throws PSDictionaryFormatException {
         int firstIndex = str.indexOf(braces[0]);
         if (firstIndex == -1) {
            throw new PSDictionaryFormatException("Failed to find opening parameter '" + braces[0] + "");
         } else {
            int lastIndex = this.indexOfMatchingBrace(str, braces, firstIndex);
            if (lastIndex == -1) {
               throw new PSDictionaryFormatException("Failed to find matching closing parameter '" + braces[1] + "'");
            } else {
               int braceLen = braces[0].length();
               str = str.substring(firstIndex + braceLen, lastIndex).trim();
               return str;
            }
         }
      }

      public PSDictionary parseDictionary(String str) throws PSDictionaryFormatException {
         PSDictionary dictionary = new PSDictionary();
         str = this.stripBraces(str.trim(), BRACES[0]);
         int len = str.length();

         Token keyToken;
         Token valueToken;
         for(int currIndex = 0; (keyToken = this.nextToken(str, currIndex)) != null && currIndex <= len; currIndex = valueToken.endIndex + 1) {
            if (keyToken.value == null) {
               throw new PSDictionaryFormatException("Failed to parse object key");
            }

            valueToken = this.nextToken(str, keyToken.endIndex + 1);
            String[] braces = null;
            String[][] var8 = BRACES;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               String[] brace = var8[var10];
               if (valueToken.value.startsWith(brace[0])) {
                  braces = brace;
                  break;
               }
            }

            Object obj = null;
            if (braces != null) {
               valueToken.endIndex = this.indexOfMatchingBrace(str, braces, valueToken.startIndex) + braces[0].length();
               if (valueToken.endIndex < 0) {
                  throw new PSDictionaryFormatException("Closing value brace '" + braces[1] + "' not found for key '" + keyToken.value + "'");
               }

               valueToken.value = str.substring(valueToken.startIndex, valueToken.endIndex);
            }

            if (braces != null && braces != BRACES[2] && braces != BRACES[3]) {
               if (BRACES[1] == braces) {
                  List objList = new ArrayList();
                  String objString = this.stripBraces(valueToken.value, braces);
                  StringTokenizer tokenizer = new StringTokenizer(objString, ",");

                  while(tokenizer.hasMoreTokens()) {
                     objList.add(tokenizer.nextToken());
                  }

                  obj = objList;
               } else if (BRACES[0] == braces) {
                  obj = this.parseDictionary(valueToken.value);
               }
            } else {
               obj = valueToken.value;
            }

            dictionary.put(keyToken.value, obj);
         }

         return dictionary;
      }

      // $FF: synthetic method
      Maker(Object x0) {
         this();
      }

      private static class Token {
         private int startIndex;
         private int endIndex;
         private String value;

         private Token() {
            this.startIndex = -1;
            this.endIndex = -1;
         }

         // $FF: synthetic method
         Token(Object x0) {
            this();
         }
      }
   }
}
