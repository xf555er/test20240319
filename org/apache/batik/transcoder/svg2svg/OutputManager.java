package org.apache.batik.transcoder.svg2svg;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.xml.XMLUtilities;

public class OutputManager {
   protected PrettyPrinter prettyPrinter;
   protected Writer writer;
   protected int level;
   protected StringBuffer margin = new StringBuffer();
   protected int line = 1;
   protected int column;
   protected List xmlSpace = new LinkedList();
   protected boolean canIndent;
   protected List startingLines;
   protected boolean lineAttributes;

   public OutputManager(PrettyPrinter pp, Writer w) {
      this.xmlSpace.add(Boolean.FALSE);
      this.canIndent = true;
      this.startingLines = new LinkedList();
      this.lineAttributes = false;
      this.prettyPrinter = pp;
      this.writer = w;
   }

   public void printCharacter(char c) throws IOException {
      if (c == '\n') {
         this.printNewline();
      } else {
         ++this.column;
         this.writer.write(c);
      }

   }

   public void printNewline() throws IOException {
      String nl = this.prettyPrinter.getNewline();

      for(int i = 0; i < nl.length(); ++i) {
         this.writer.write(nl.charAt(i));
      }

      this.column = 0;
      ++this.line;
   }

   public void printString(String s) throws IOException {
      for(int i = 0; i < s.length(); ++i) {
         this.printCharacter(s.charAt(i));
      }

   }

   public void printCharacters(char[] ca) throws IOException {
      char[] var2 = ca;
      int var3 = ca.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         char aCa = var2[var4];
         this.printCharacter(aCa);
      }

   }

   public void printSpaces(char[] text, boolean opt) throws IOException {
      if (this.prettyPrinter.getFormat()) {
         if (!opt) {
            this.printCharacter(' ');
         }
      } else {
         this.printCharacters(text);
      }

   }

   public void printTopSpaces(char[] text) throws IOException {
      if (this.prettyPrinter.getFormat()) {
         int nl = this.newlines(text);

         for(int i = 0; i < nl; ++i) {
            this.printNewline();
         }
      } else {
         this.printCharacters(text);
      }

   }

   public void printComment(char[] text) throws IOException {
      if (this.prettyPrinter.getFormat()) {
         if (this.canIndent) {
            this.printNewline();
            this.printString(this.margin.toString());
         }

         this.printString("<!--");
         if (this.column + text.length + 3 < this.prettyPrinter.getDocumentWidth()) {
            this.printCharacters(text);
         } else {
            this.formatText(text, this.margin.toString(), false);
            this.printCharacter(' ');
         }

         if (this.column + 3 > this.prettyPrinter.getDocumentWidth()) {
            this.printNewline();
            this.printString(this.margin.toString());
         }

         this.printString("-->");
      } else {
         this.printString("<!--");
         this.printCharacters(text);
         this.printString("-->");
      }

   }

   public void printXMLDecl(char[] space1, char[] space2, char[] space3, char[] version, char versionDelim, char[] space4, char[] space5, char[] space6, char[] encoding, char encodingDelim, char[] space7, char[] space8, char[] space9, char[] standalone, char standaloneDelim, char[] space10) throws IOException {
      this.printString("<?xml");
      this.printSpaces(space1, false);
      this.printString("version");
      if (space2 != null) {
         this.printSpaces(space2, true);
      }

      this.printCharacter('=');
      if (space3 != null) {
         this.printSpaces(space3, true);
      }

      this.printCharacter(versionDelim);
      this.printCharacters(version);
      this.printCharacter(versionDelim);
      if (space4 != null) {
         this.printSpaces(space4, false);
         if (encoding != null) {
            this.printString("encoding");
            if (space5 != null) {
               this.printSpaces(space5, true);
            }

            this.printCharacter('=');
            if (space6 != null) {
               this.printSpaces(space6, true);
            }

            this.printCharacter(encodingDelim);
            this.printCharacters(encoding);
            this.printCharacter(encodingDelim);
            if (space7 != null) {
               this.printSpaces(space7, standalone == null);
            }
         }

         if (standalone != null) {
            this.printString("standalone");
            if (space8 != null) {
               this.printSpaces(space8, true);
            }

            this.printCharacter('=');
            if (space9 != null) {
               this.printSpaces(space9, true);
            }

            this.printCharacter(standaloneDelim);
            this.printCharacters(standalone);
            this.printCharacter(standaloneDelim);
            if (space10 != null) {
               this.printSpaces(space10, true);
            }
         }
      }

      this.printString("?>");
   }

   public void printPI(char[] target, char[] space, char[] data) throws IOException {
      if (this.prettyPrinter.getFormat() && this.canIndent) {
         this.printNewline();
         this.printString(this.margin.toString());
      }

      this.printString("<?");
      this.printCharacters(target);
      this.printSpaces(space, false);
      this.printCharacters(data);
      this.printString("?>");
   }

   public void printDoctypeStart(char[] space1, char[] root, char[] space2, String externalId, char[] space3, char[] string1, char string1Delim, char[] space4, char[] string2, char string2Delim, char[] space5) throws IOException {
      if (this.prettyPrinter.getFormat()) {
         this.printString("<!DOCTYPE");
         this.printCharacter(' ');
         this.printCharacters(root);
         if (space2 != null) {
            this.printCharacter(' ');
            this.printString(externalId);
            this.printCharacter(' ');
            this.printCharacter(string1Delim);
            this.printCharacters(string1);
            this.printCharacter(string1Delim);
            if (space4 != null && string2 != null) {
               if (this.column + string2.length + 3 > this.prettyPrinter.getDocumentWidth()) {
                  this.printNewline();

                  for(int i = 0; i < this.prettyPrinter.getTabulationWidth(); ++i) {
                     this.printCharacter(' ');
                  }
               } else {
                  this.printCharacter(' ');
               }

               this.printCharacter(string2Delim);
               this.printCharacters(string2);
               this.printCharacter(string2Delim);
               this.printCharacter(' ');
            }
         }
      } else {
         this.printString("<!DOCTYPE");
         this.printSpaces(space1, false);
         this.printCharacters(root);
         if (space2 != null) {
            this.printSpaces(space2, false);
            this.printString(externalId);
            this.printSpaces(space3, false);
            this.printCharacter(string1Delim);
            this.printCharacters(string1);
            this.printCharacter(string1Delim);
            if (space4 != null) {
               this.printSpaces(space4, string2 == null);
               if (string2 != null) {
                  this.printCharacter(string2Delim);
                  this.printCharacters(string2);
                  this.printCharacter(string2Delim);
                  if (space5 != null) {
                     this.printSpaces(space5, true);
                  }
               }
            }
         }
      }

   }

   public void printDoctypeEnd(char[] space) throws IOException {
      if (space != null) {
         this.printSpaces(space, true);
      }

      this.printCharacter('>');
   }

   public void printParameterEntityReference(char[] name) throws IOException {
      this.printCharacter('%');
      this.printCharacters(name);
      this.printCharacter(';');
   }

   public void printEntityReference(char[] name, boolean first) throws IOException {
      if (this.prettyPrinter.getFormat() && this.xmlSpace.get(0) != Boolean.TRUE && first) {
         this.printNewline();
         this.printString(this.margin.toString());
      }

      this.printCharacter('&');
      this.printCharacters(name);
      this.printCharacter(';');
   }

   public void printCharacterEntityReference(char[] code, boolean first, boolean preceedingSpace) throws IOException {
      if (this.prettyPrinter.getFormat() && this.xmlSpace.get(0) != Boolean.TRUE) {
         if (first) {
            this.printNewline();
            this.printString(this.margin.toString());
         } else if (preceedingSpace) {
            int endCol = this.column + code.length + 3;
            if (endCol > this.prettyPrinter.getDocumentWidth()) {
               this.printNewline();
               this.printString(this.margin.toString());
            } else {
               this.printCharacter(' ');
            }
         }
      }

      this.printString("&#");
      this.printCharacters(code);
      this.printCharacter(';');
   }

   public void printElementStart(char[] name, List attributes, char[] space) throws IOException {
      this.xmlSpace.add(0, this.xmlSpace.get(0));
      this.startingLines.add(0, this.line);
      if (this.prettyPrinter.getFormat() && this.canIndent) {
         this.printNewline();
         this.printString(this.margin.toString());
      }

      this.printCharacter('<');
      this.printCharacters(name);
      Iterator it;
      if (this.prettyPrinter.getFormat()) {
         it = attributes.iterator();
         AttributeInfo ai;
         if (it.hasNext()) {
            ai = (AttributeInfo)it.next();
            if (ai.isAttribute("xml:space")) {
               this.xmlSpace.set(0, ai.value.equals("preserve") ? Boolean.TRUE : Boolean.FALSE);
            }

            this.printCharacter(' ');
            this.printCharacters(ai.name);
            this.printCharacter('=');
            this.printCharacter(ai.delimiter);
            this.printString(ai.value);
            this.printCharacter(ai.delimiter);
         }

         while(it.hasNext()) {
            ai = (AttributeInfo)it.next();
            if (ai.isAttribute("xml:space")) {
               this.xmlSpace.set(0, ai.value.equals("preserve") ? Boolean.TRUE : Boolean.FALSE);
            }

            int len = ai.name.length + ai.value.length() + 4;
            if (!this.lineAttributes && len + this.column <= this.prettyPrinter.getDocumentWidth()) {
               this.printCharacter(' ');
            } else {
               this.printNewline();
               this.printString(this.margin.toString());

               for(int i = 0; i < name.length + 2; ++i) {
                  this.printCharacter(' ');
               }
            }

            this.printCharacters(ai.name);
            this.printCharacter('=');
            this.printCharacter(ai.delimiter);
            this.printString(ai.value);
            this.printCharacter(ai.delimiter);
         }
      } else {
         it = attributes.iterator();

         while(it.hasNext()) {
            Object attribute = it.next();
            AttributeInfo ai = (AttributeInfo)attribute;
            if (ai.isAttribute("xml:space")) {
               this.xmlSpace.set(0, ai.value.equals("preserve") ? Boolean.TRUE : Boolean.FALSE);
            }

            this.printSpaces(ai.space, false);
            this.printCharacters(ai.name);
            if (ai.space1 != null) {
               this.printSpaces(ai.space1, true);
            }

            this.printCharacter('=');
            if (ai.space2 != null) {
               this.printSpaces(ai.space2, true);
            }

            this.printCharacter(ai.delimiter);
            this.printString(ai.value);
            this.printCharacter(ai.delimiter);
         }
      }

      if (space != null) {
         this.printSpaces(space, true);
      }

      ++this.level;

      for(int i = 0; i < this.prettyPrinter.getTabulationWidth(); ++i) {
         this.margin.append(' ');
      }

      this.canIndent = true;
   }

   public void printElementEnd(char[] name, char[] space) throws IOException {
      for(int i = 0; i < this.prettyPrinter.getTabulationWidth(); ++i) {
         this.margin.deleteCharAt(0);
      }

      --this.level;
      if (name != null) {
         if (this.prettyPrinter.getFormat() && this.xmlSpace.get(0) != Boolean.TRUE && (this.line != (Integer)this.startingLines.get(0) || this.column + name.length + 3 >= this.prettyPrinter.getDocumentWidth())) {
            this.printNewline();
            this.printString(this.margin.toString());
         }

         this.printString("</");
         this.printCharacters(name);
         if (space != null) {
            this.printSpaces(space, true);
         }

         this.printCharacter('>');
      } else {
         this.printString("/>");
      }

      this.startingLines.remove(0);
      this.xmlSpace.remove(0);
   }

   public boolean printCharacterData(char[] data, boolean first, boolean preceedingSpace) throws IOException {
      if (!this.prettyPrinter.getFormat()) {
         this.printCharacters(data);
         return false;
      } else {
         this.canIndent = true;
         if (!this.isWhiteSpace(data)) {
            if (this.xmlSpace.get(0) == Boolean.TRUE) {
               this.printCharacters(data);
               this.canIndent = false;
               return false;
            } else {
               if (first) {
                  this.printNewline();
                  this.printString(this.margin.toString());
               }

               return this.formatText(data, this.margin.toString(), preceedingSpace);
            }
         } else {
            int nl = this.newlines(data);

            for(int i = 0; i < nl - 1; ++i) {
               this.printNewline();
            }

            return true;
         }
      }
   }

   public void printCDATASection(char[] data) throws IOException {
      this.printString("<![CDATA[");
      this.printCharacters(data);
      this.printString("]]>");
   }

   public void printNotation(char[] space1, char[] name, char[] space2, String externalId, char[] space3, char[] string1, char string1Delim, char[] space4, char[] string2, char string2Delim, char[] space5) throws IOException {
      this.writer.write("<!NOTATION");
      this.printSpaces(space1, false);
      this.writer.write(name);
      this.printSpaces(space2, false);
      this.writer.write(externalId);
      this.printSpaces(space3, false);
      this.writer.write(string1Delim);
      this.writer.write(string1);
      this.writer.write(string1Delim);
      if (space4 != null) {
         this.printSpaces(space4, false);
         if (string2 != null) {
            this.writer.write(string2Delim);
            this.writer.write(string2);
            this.writer.write(string2Delim);
         }
      }

      if (space5 != null) {
         this.printSpaces(space5, true);
      }

      this.writer.write(62);
   }

   public void printAttlistStart(char[] space, char[] name) throws IOException {
      this.writer.write("<!ATTLIST");
      this.printSpaces(space, false);
      this.writer.write(name);
   }

   public void printAttlistEnd(char[] space) throws IOException {
      if (space != null) {
         this.printSpaces(space, false);
      }

      this.writer.write(62);
   }

   public void printAttName(char[] space1, char[] name, char[] space2) throws IOException {
      this.printSpaces(space1, false);
      this.writer.write(name);
      this.printSpaces(space2, false);
   }

   public void printEnumeration(List names) throws IOException {
      this.writer.write(40);
      Iterator it = names.iterator();
      NameInfo ni = (NameInfo)it.next();
      if (ni.space1 != null) {
         this.printSpaces(ni.space1, true);
      }

      this.writer.write(ni.name);
      if (ni.space2 != null) {
         this.printSpaces(ni.space2, true);
      }

      while(it.hasNext()) {
         this.writer.write(124);
         ni = (NameInfo)it.next();
         if (ni.space1 != null) {
            this.printSpaces(ni.space1, true);
         }

         this.writer.write(ni.name);
         if (ni.space2 != null) {
            this.printSpaces(ni.space2, true);
         }
      }

      this.writer.write(41);
   }

   protected int newlines(char[] text) {
      int result = 0;
      char[] var3 = text;
      int var4 = text.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         char aText = var3[var5];
         if (aText == '\n') {
            ++result;
         }
      }

      return result;
   }

   protected boolean isWhiteSpace(char[] text) {
      char[] var2 = text;
      int var3 = text.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         char aText = var2[var4];
         if (!XMLUtilities.isXMLSpace(aText)) {
            return false;
         }
      }

      return true;
   }

   protected boolean formatText(char[] text, String margin, boolean preceedingSpace) throws IOException {
      int i = 0;
      boolean startsWithSpace = preceedingSpace;

      while(i < text.length) {
         while(i < text.length) {
            if (!XMLUtilities.isXMLSpace(text[i])) {
               StringBuffer sb = new StringBuffer();

               while(i < text.length && !XMLUtilities.isXMLSpace(text[i])) {
                  sb.append(text[i++]);
               }

               if (sb.length() == 0) {
                  return startsWithSpace;
               }

               if (startsWithSpace) {
                  int endCol = this.column + sb.length();
                  if (endCol < this.prettyPrinter.getDocumentWidth() - 1 || margin.length() + sb.length() >= this.prettyPrinter.getDocumentWidth() - 1 && margin.length() >= this.column) {
                     if (this.column > margin.length()) {
                        this.printCharacter(' ');
                     }
                  } else {
                     this.printNewline();
                     this.printString(margin);
                  }
               }

               this.printString(sb.toString());
               startsWithSpace = false;
            } else {
               startsWithSpace = true;
               ++i;
            }
         }

         return startsWithSpace;
      }

      return startsWithSpace;
   }

   public static class AttributeInfo {
      public char[] space;
      public char[] name;
      public char[] space1;
      public char[] space2;
      public String value;
      public char delimiter;
      public boolean entityReferences;

      public AttributeInfo(char[] sp, char[] n, char[] sp1, char[] sp2, String val, char delim, boolean entity) {
         this.space = sp;
         this.name = n;
         this.space1 = sp1;
         this.space2 = sp2;
         this.value = val;
         this.delimiter = delim;
         this.entityReferences = entity;
      }

      public boolean isAttribute(String s) {
         if (this.name.length == s.length()) {
            for(int i = 0; i < this.name.length; ++i) {
               if (this.name[i] != s.charAt(i)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public static class NameInfo {
      public char[] space1;
      public char[] name;
      public char[] space2;

      public NameInfo(char[] sp1, char[] nm, char[] sp2) {
         this.space1 = sp1;
         this.name = nm;
         this.space2 = sp2;
      }
   }
}
