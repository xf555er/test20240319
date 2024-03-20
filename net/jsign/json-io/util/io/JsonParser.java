package net.jsign.json-io.util.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonParser {
   private static final Map stringCache = new HashMap();
   private final FastPushbackReader input;
   private final Map objsRead;
   private final StringBuilder strBuf = new StringBuilder(256);
   private final StringBuilder hexBuf = new StringBuilder();
   private final StringBuilder numBuf = new StringBuilder();
   private final boolean useMaps;
   private final Map typeNameMap;

   static {
      stringCache.put("", "");
      stringCache.put("true", "true");
      stringCache.put("True", "True");
      stringCache.put("TRUE", "TRUE");
      stringCache.put("false", "false");
      stringCache.put("False", "False");
      stringCache.put("FALSE", "FALSE");
      stringCache.put("null", "null");
      stringCache.put("yes", "yes");
      stringCache.put("Yes", "Yes");
      stringCache.put("YES", "YES");
      stringCache.put("no", "no");
      stringCache.put("No", "No");
      stringCache.put("NO", "NO");
      stringCache.put("on", "on");
      stringCache.put("On", "On");
      stringCache.put("ON", "ON");
      stringCache.put("off", "off");
      stringCache.put("Off", "Off");
      stringCache.put("OFF", "OFF");
      stringCache.put("@id", "@id");
      stringCache.put("@ref", "@ref");
      stringCache.put("@items", "@items");
      stringCache.put("@type", "@type");
      stringCache.put("@keys", "@keys");
      stringCache.put("0", "0");
      stringCache.put("1", "1");
      stringCache.put("2", "2");
      stringCache.put("3", "3");
      stringCache.put("4", "4");
      stringCache.put("5", "5");
      stringCache.put("6", "6");
      stringCache.put("7", "7");
      stringCache.put("8", "8");
      stringCache.put("9", "9");
   }

   JsonParser(FastPushbackReader reader, Map objectsMap, Map args) {
      this.input = reader;
      this.useMaps = Boolean.TRUE.equals(args.get("USE_MAPS"));
      this.objsRead = objectsMap;
      this.typeNameMap = (Map)args.get("TYPE_NAME_MAP_REVERSE");
   }

   private Object readJsonObject() throws IOException {
      boolean done = false;
      String field = null;
      JsonObject object = new JsonObject();
      int state = 0;
      FastPushbackReader in = this.input;

      while(!done) {
         int c;
         switch (state) {
            case 0:
               c = this.skipWhitespaceRead();
               if (c == 123) {
                  object.line = in.getLine();
                  object.col = in.getCol();
                  c = this.skipWhitespaceRead();
                  if (c == 125) {
                     return "~!o~";
                  }

                  in.unread(c);
                  state = 1;
               } else {
                  this.error("Input is invalid JSON; object does not start with '{', c=" + c);
               }
               break;
            case 1:
               c = this.skipWhitespaceRead();
               if (c == 34) {
                  field = this.readString();
                  c = this.skipWhitespaceRead();
                  if (c != 58) {
                     this.error("Expected ':' between string field and value");
                  }

                  if (field.startsWith("@")) {
                     if (field.equals("@t")) {
                        field = (String)stringCache.get("@type");
                     } else if (field.equals("@i")) {
                        field = (String)stringCache.get("@id");
                     } else if (field.equals("@r")) {
                        field = (String)stringCache.get("@ref");
                     } else if (field.equals("@k")) {
                        field = (String)stringCache.get("@keys");
                     } else if (field.equals("@e")) {
                        field = (String)stringCache.get("@items");
                     }
                  }

                  state = 2;
               } else {
                  this.error("Expected quote");
               }
               break;
            case 2:
               if (field == null) {
                  field = "@items";
               }

               Object value = this.readValue(object);
               if ("@type".equals(field) && this.typeNameMap != null) {
                  String substitute = (String)this.typeNameMap.get(value);
                  if (substitute != null) {
                     value = substitute;
                  }
               }

               object.put(field, value);
               if ("@id".equals(field)) {
                  this.objsRead.put((Long)value, object);
               }

               state = 3;
               break;
            case 3:
               c = this.skipWhitespaceRead();
               if (c == -1) {
                  this.error("EOF reached before closing '}'");
               }

               if (c == 125) {
                  done = true;
               } else if (c == 44) {
                  state = 1;
               } else {
                  this.error("Object not ended with '}'");
               }
         }
      }

      if (this.useMaps && object.isLogicalPrimitive()) {
         return object.getPrimitiveValue();
      } else {
         return object;
      }
   }

   Object readValue(JsonObject object) throws IOException {
      int c = this.skipWhitespaceRead();
      if (c == 34) {
         return this.readString();
      } else if ((c < 48 || c > 57) && c != 45 && c != 78 && c != 73) {
         switch (c) {
            case -1:
               this.error("EOF reached prematurely");
            default:
               return this.error("Unknown JSON value type");
            case 70:
            case 102:
               this.readToken("false");
               return Boolean.FALSE;
            case 78:
            case 110:
               this.readToken("null");
               return null;
            case 84:
            case 116:
               this.readToken("true");
               return Boolean.TRUE;
            case 91:
               return this.readArray(object);
            case 93:
               this.input.unread(93);
               return "~!a~";
            case 123:
               this.input.unread(123);
               return this.readJsonObject();
         }
      } else {
         return this.readNumber(c);
      }
   }

   private Object readArray(JsonObject object) throws IOException {
      List array = new ArrayList();

      while(true) {
         Object o = this.readValue(object);
         if (o != "~!a~") {
            array.add(o);
         }

         int c = this.skipWhitespaceRead();
         if (c == 93) {
            return array.toArray();
         }

         if (c != 44) {
            this.error("Expected ',' or ']' inside array");
         }
      }
   }

   private void readToken(String token) throws IOException {
      int len = token.length();

      for(int i = 1; i < len; ++i) {
         int c = this.input.read();
         if (c == -1) {
            this.error("EOF reached while reading token: " + token);
         }

         int c = Character.toLowerCase((char)c);
         int loTokenChar = token.charAt(i);
         if (loTokenChar != c) {
            this.error("Expected token: " + token);
         }
      }

   }

   private Number readNumber(int c) throws IOException {
      FastPushbackReader in = this.input;
      boolean isFloat = false;
      if (JsonReader.isAllowNanAndInfinity() && (c == 45 || c == 78 || c == 73)) {
         boolean isNeg = c == 45;
         if (isNeg) {
            c = this.input.read();
         }

         if (c == 73) {
            this.readToken("infinity");
            return isNeg ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
         }

         if (78 == c) {
            this.readToken("nan");
            return Double.NaN;
         }

         this.input.unread(c);
         c = 45;
      }

      StringBuilder number = this.numBuf;
      number.setLength(0);
      number.appendCodePoint(c);

      while(true) {
         while(true) {
            c = in.read();
            if ((c < 48 || c > 57) && c != 45 && c != 43) {
               if (c != 46 && c != 101 && c != 69) {
                  if (c != -1) {
                     in.unread(c);
                  }

                  try {
                     if (isFloat) {
                        return Double.parseDouble(number.toString());
                     }

                     return Long.parseLong(number.toString());
                  } catch (Exception var6) {
                     return (Number)this.error("Invalid number: " + number, var6);
                  }
               }

               number.appendCodePoint(c);
               isFloat = true;
            } else {
               number.appendCodePoint(c);
            }
         }
      }
   }

   private String readString() throws IOException {
      StringBuilder str = this.strBuf;
      StringBuilder hex = this.hexBuf;
      str.setLength(0);
      int state = 0;
      FastPushbackReader in = this.input;

      while(true) {
         while(true) {
            int c = in.read();
            if (c == -1) {
               this.error("EOF reached while reading JSON string");
            }

            if (state == 0) {
               if (c == 34) {
                  String s = str.toString();
                  String translate = (String)stringCache.get(s);
                  return translate == null ? s : translate;
               }

               if (c == 92) {
                  state = 1;
               } else {
                  str.appendCodePoint(c);
               }
            } else if (state == 1) {
               switch (c) {
                  case 34:
                     str.appendCodePoint(34);
                     break;
                  case 39:
                     str.appendCodePoint(39);
                     break;
                  case 47:
                     str.appendCodePoint(47);
                     break;
                  case 92:
                     str.appendCodePoint(92);
                     break;
                  case 98:
                     str.appendCodePoint(8);
                     break;
                  case 102:
                     str.appendCodePoint(12);
                     break;
                  case 110:
                     str.appendCodePoint(10);
                     break;
                  case 114:
                     str.appendCodePoint(13);
                     break;
                  case 116:
                     str.appendCodePoint(9);
                     break;
                  case 117:
                     hex.setLength(0);
                     state = 2;
                     break;
                  default:
                     this.error("Invalid character escape sequence specified: " + c);
               }

               if (c != 117) {
                  state = 0;
               }
            } else if (c >= 48 && c <= 57 || c >= 65 && c <= 70 || c >= 97 && c <= 102) {
               hex.appendCodePoint((char)c);
               if (hex.length() == 4) {
                  int value = Integer.parseInt(hex.toString(), 16);
                  str.appendCodePoint(value);
                  state = 0;
               }
            } else {
               this.error("Expected hexadecimal digits");
            }
         }
      }
   }

   private int skipWhitespaceRead() throws IOException {
      FastPushbackReader in = this.input;

      int c;
      do {
         do {
            c = in.read();
         } while(c == 32);
      } while(c == 10 || c == 13 || c == 9);

      return c;
   }

   Object error(String msg) {
      throw new JsonIoException(this.getMessage(msg));
   }

   Object error(String msg, Exception e) {
      throw new JsonIoException(this.getMessage(msg), e);
   }

   String getMessage(String msg) {
      return msg + "\nline: " + this.input.getLine() + ", col: " + this.input.getCol() + "\n" + this.input.getLastSnippet();
   }
}
