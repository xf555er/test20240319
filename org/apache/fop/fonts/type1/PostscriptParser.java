package org.apache.fop.fonts.type1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PostscriptParser {
   protected static final Log LOG = LogFactory.getLog(PostscriptParser.class);
   private static final String DICTIONARY = "dict";
   private static final String FIXED_ARRAY = "array";
   private static final String VARIABLE_ARRAY = "[";
   private static final String SUBROUTINE = "{";
   private HashMap subroutines = new HashMap();

   public List parse(byte[] segment) throws IOException {
      List parsedElements = new ArrayList();
      PSElement foundElement = null;
      String operator = null;
      StringBuilder token = new StringBuilder();
      List tokens = new ArrayList();
      int startPoint = -1;
      boolean specialDelimiter = false;
      boolean lastWasSpecial = false;

      for(int i = 0; i < segment.length; ++i) {
         byte cur = segment[i];
         if (foundElement != null && foundElement.hasMore()) {
            foundElement.parse(cur, i);
         } else {
            char c = (char)cur;
            if (!lastWasSpecial) {
               specialDelimiter = c == '{' || c == '}' || c == '[' || c == ']' || !token.toString().equals("") && c == '/';
               boolean isNotBreak = c != ' ' && c != '\r' && cur != 15 && cur != 12 && cur != 10;
               if (isNotBreak && !specialDelimiter) {
                  token.append(c);
                  continue;
               }
            } else {
               lastWasSpecial = false;
               token.append(c);
               if (token.toString().equals("/")) {
                  continue;
               }
            }

            try {
               boolean setOp = false;
               if ((foundElement == null || !foundElement.hasMore()) && token.length() > 1 && token.charAt(0) == '/' && tokens.size() != 1 || this.hasEndToken(token.toString())) {
                  operator = token.toString();
                  setOp = true;
                  if (tokens.size() > 2 && ((String)tokens.get(tokens.size() - 1)).equals("def")) {
                     PSVariable newVar = new PSVariable((String)tokens.get(0), startPoint);
                     newVar.setValue((String)tokens.get(1));
                     newVar.setEndPoint(i - operator.length());
                     parsedElements.add(newVar);
                  }

                  tokens.clear();
                  startPoint = i - token.length();
               }

               if (operator != null) {
                  if (foundElement instanceof PSSubroutine) {
                     PSSubroutine sub = (PSSubroutine)foundElement;
                     this.subroutines.put(sub.getOperator(), sub);
                     parsedElements.add(sub);
                     if (!setOp) {
                        operator = "";
                     }
                  } else if (foundElement != null) {
                     if (!this.hasMatch(foundElement.getOperator(), parsedElements)) {
                        parsedElements.add(foundElement);
                     } else {
                        LOG.warn("Duplicate " + foundElement.getOperator() + " in font file, Ignoring.");
                     }
                  }

                  foundElement = this.createElement(operator, token.toString(), startPoint);
               }
            } finally {
               tokens.add(token.toString());
               token = new StringBuilder();
               if (specialDelimiter) {
                  specialDelimiter = false;
                  lastWasSpecial = true;
                  --i;
               }

            }
         }
      }

      return parsedElements;
   }

   private boolean hasEndToken(String token) {
      return token.equals("currentdict");
   }

   private boolean hasMatch(String operator, List elements) {
      Iterator var3 = elements.iterator();

      PSElement element;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         element = (PSElement)var3.next();
      } while(!element.getOperator().equals(operator));

      return true;
   }

   public PSElement createElement(String operator, String elementID, int startPoint) {
      if (operator.equals("")) {
         return null;
      } else if (elementID.equals("array")) {
         return new PSFixedArray(operator, startPoint);
      } else if (elementID.equals("[")) {
         return new PSVariableArray(operator, startPoint);
      } else if (elementID.equals("{")) {
         return new PSSubroutine(operator, startPoint);
      } else {
         return !operator.equals("/Private") && elementID.equals("dict") ? new PSDictionary(operator, startPoint) : null;
      }
   }

   public class PSVariable extends PSElement {
      private String value = "";

      public PSVariable(String operator, int startPoint) {
         super(operator, startPoint);
      }

      public void parseToken(String token, int curPos) {
         if (token.equals("def")) {
            this.hasMore = false;
            this.endPoint = curPos;
         }
      }

      public void parseByte(byte cur, int pos) {
      }

      public void setValue(String value) {
         this.value = value;
      }

      public String getValue() {
         return this.value;
      }

      public void setEndPoint(int endPoint) {
         this.endPoint = endPoint;
      }
   }

   public class PSDictionary extends PSElement {
      private HashMap entries = new HashMap();
      private String entry = "";
      private String token = "";
      protected int binaryLength;

      public PSDictionary(String operator, int startPoint) {
         super(operator, startPoint);
      }

      public void parseToken(String token, int curPos) {
         if (token.equals("end")) {
            this.addEntry(this.entry);
            this.hasMore = false;
            this.endPoint = curPos;
         } else {
            if (token.startsWith("/")) {
               if (this.entry.trim().startsWith("/")) {
                  this.tokens.clear();
                  this.addEntry(this.entry);
               }

               this.entry = "";
            }

            if (this.tokens.size() >= 1 || token.startsWith("/")) {
               this.tokens.add(token);
            }

            this.entry = this.entry + token + " ";
            if (this.tokens.size() == 3 && ((String)this.tokens.get(0)).startsWith("/") && !((String)this.tokens.get(2)).equals("def") && this.isInteger((String)this.tokens.get(1))) {
               this.binaryLength = Integer.parseInt((String)this.tokens.get(1));
               this.readBinary = true;
            }

         }
      }

      public HashMap getEntries() {
         return this.entries;
      }

      private void addEntry(String entry) {
         Scanner s = (new Scanner(entry)).useDelimiter(" ");
         String id = s.next();
         this.entries.put(id, entry);
      }

      public void parseByte(byte cur, int pos) {
         if (this.binaryLength > 0) {
            --this.binaryLength;
         } else if (this.readBinary) {
            int start = pos - Integer.parseInt((String)this.tokens.get(1));
            this.binaryEntries.put(this.tokens.get(0), new int[]{start, pos});
            this.readBinary = false;
         } else {
            this.tokens.add(this.token);
            this.parseToken(this.token, pos);
         }

      }
   }

   public class PSSubroutine extends PSElement {
      private int level = 1;
      private String entry = "";

      public PSSubroutine(String operator, int startPoint) {
         super(operator, startPoint);
      }

      public void parseToken(String token, int curPos) {
         if (this.level == 0 && token.length() > 0 && (token.equals("def") || token.equals("ifelse") || token.charAt(0) == '}')) {
            this.hasMore = false;
            this.endPoint = curPos;
         } else {
            if (token.equals("{")) {
               ++this.level;
            } else if (token.equals("}")) {
               --this.level;
            }

            this.entry = this.entry + token + " ";
         }
      }

      public String getSubroutine() {
         return this.entry.trim();
      }

      public void parseByte(byte cur, int pos) {
      }
   }

   public class PSVariableArray extends PSElement {
      private int level;
      private List arrayItems = new ArrayList();
      private String entry = "";

      public PSVariableArray(String operator, int startPoint) {
         super(operator, startPoint);
      }

      public void parseToken(String token, int curPos) {
         this.entry = this.entry + token + " ";
         if (this.level <= 0 && token.length() > 0 && token.charAt(0) == ']') {
            this.hasMore = false;
            this.endPoint = curPos;
         } else {
            if (token.equals("{")) {
               ++this.level;
            } else if (token.equals("}")) {
               --this.level;
               if (!this.entry.equals("") && this.level == 0) {
                  this.arrayItems.add(this.entry);
                  this.entry = "";
               }
            }

         }
      }

      public List getEntries() {
         return this.arrayItems;
      }

      public void parseByte(byte cur, int pos) {
      }
   }

   public class PSFixedArray extends PSElement {
      private String entry = "";
      private String token = "";
      private boolean finished;
      protected int binaryLength;
      private HashMap entries = new HashMap();
      private static final String READ_ONLY = "readonly";

      public PSFixedArray(String operator, int startPoint) {
         super(operator, startPoint);
      }

      public void parseToken(String token, int curPos) {
         if (this.checkForEnd(token) && !token.equals("def")) {
            if (token.equals("dup")) {
               if (this.entry.startsWith("dup")) {
                  this.addEntry(this.entry);
               }

               this.entry = "";
               this.tokens.clear();
            }

            if (!token.equals("readonly")) {
               this.entry = this.entry + token + " ";
            }

            if (!token.trim().equals("")) {
               this.tokens.add(token);
            }

            if (this.tokens.size() == 4 && ((String)this.tokens.get(0)).equals("dup") && this.isInteger((String)this.tokens.get(2))) {
               this.binaryLength = Integer.parseInt((String)this.tokens.get(2));
               this.readBinary = true;
            }

         } else {
            this.hasMore = false;
            this.endPoint = curPos;
         }
      }

      private boolean checkForEnd(String checkToken) {
         boolean subFound = false;
         PSSubroutine sub = (PSSubroutine)PostscriptParser.this.subroutines.get("/" + checkToken);
         if (sub != null && sub.getSubroutine().contains("def")) {
            subFound = true;
         }

         if (!this.finished && (subFound || checkToken.equals("def"))) {
            this.finished = true;
            this.addEntry(this.entry);
            return false;
         } else {
            return !this.finished;
         }
      }

      public HashMap getEntries() {
         return this.entries;
      }

      private void addEntry(String entry) {
         if (!entry.equals("")) {
            if (entry.indexOf(47) != -1 && entry.charAt(entry.indexOf(47) - 1) != ' ') {
               entry = entry.replace("/", " /");
            }

            int entryLen;
            do {
               entryLen = entry.length();
               entry = entry.replace("  ", " ");
            } while(entry.length() != entryLen);

            Scanner s = (new Scanner(entry)).useDelimiter(" ");
            boolean valid = false;
            s.next();
            if (s.hasNext()) {
               int id = s.nextInt();
               this.entries.put(id, entry);
               valid = true;
            }

            if (!valid) {
               this.setFoundUnexpected(true);
            }
         }

      }

      public void parseByte(byte cur, int pos) {
         if (this.binaryLength > 0) {
            this.token = this.token + (char)cur;
            --this.binaryLength;
         } else if (this.readBinary) {
            int bLength = Integer.parseInt((String)this.tokens.get(2));
            int start = pos - bLength;
            int end = start + bLength;
            this.binaryEntries.put(this.tokens.get(1), new int[]{start, end});
            this.token = "";
            this.readBinary = false;
         } else {
            this.tokens.add(this.token);
            this.parseToken(this.token, pos);
            this.token = "";
         }

      }
   }

   public abstract class PSElement {
      protected String operator;
      private List token;
      protected boolean hasMore = true;
      protected LinkedHashMap binaryEntries;
      protected List tokens;
      protected boolean readBinary;
      private int startPoint = -1;
      protected int endPoint = -1;
      private boolean foundUnexpected;

      public PSElement(String operator, int startPoint) {
         this.operator = operator;
         this.startPoint = startPoint;
         this.token = new ArrayList();
         this.binaryEntries = new LinkedHashMap();
         this.tokens = new ArrayList();
      }

      public String getOperator() {
         return this.operator;
      }

      public int getStartPoint() {
         return this.startPoint;
      }

      public int getEndPoint() {
         return this.endPoint;
      }

      public void parse(byte cur, int pos) throws UnsupportedEncodingException {
         if (!this.readBinary) {
            char c = (char)cur;
            boolean specialDelimiter = c == '{' || c == '}' || c == '[' || c == ']' || c == '(' || c == ')';
            boolean isNotValidBreak = c != ' ' && cur != 15 && cur != 12 && c != '\r' && c != '\n';
            if (isNotValidBreak && !specialDelimiter) {
               this.token.add(cur);
            } else {
               this.parseToken(pos);
            }

            if (specialDelimiter) {
               this.token.add(cur);
               this.parseToken(pos);
            }
         } else {
            this.parseByte(cur, pos);
         }

      }

      private void parseToken(int pos) throws UnsupportedEncodingException {
         byte[] bytesToken = new byte[this.token.size()];

         for(int i = 0; i < this.token.size(); ++i) {
            bytesToken[i] = (Byte)this.token.get(i);
         }

         this.parseToken(new String(bytesToken, "ASCII"), pos);
         this.token.clear();
      }

      public abstract void parseByte(byte var1, int var2);

      public abstract void parseToken(String var1, int var2);

      protected boolean isInteger(String intValue) {
         try {
            Integer.parseInt(intValue);
            return true;
         } catch (NumberFormatException var3) {
            return false;
         }
      }

      public LinkedHashMap getBinaryEntries() {
         return this.binaryEntries;
      }

      public int[] getBinaryEntryByIndex(int index) {
         int count = 0;

         for(Iterator var3 = this.binaryEntries.entrySet().iterator(); var3.hasNext(); ++count) {
            Map.Entry entry = (Map.Entry)var3.next();
            if (count == index) {
               return (int[])entry.getValue();
            }
         }

         return new int[0];
      }

      public boolean hasMore() {
         return this.hasMore;
      }

      protected void setFoundUnexpected(boolean foundUnexpected) {
         this.foundUnexpected = foundUnexpected;
      }

      public boolean getFoundUnexpected() {
         return this.foundUnexpected;
      }
   }
}
