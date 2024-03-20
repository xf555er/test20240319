package org.apache.batik.bridge.svg12;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.batik.anim.dom.XBLOMContentElement;
import org.apache.batik.parser.AbstractScanner;
import org.apache.batik.parser.ParseException;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathSubsetContentSelector extends AbstractContentSelector {
   protected static final int SELECTOR_INVALID = -1;
   protected static final int SELECTOR_ANY = 0;
   protected static final int SELECTOR_QNAME = 1;
   protected static final int SELECTOR_ID = 2;
   protected int selectorType;
   protected String prefix;
   protected String localName;
   protected int index;
   protected SelectedNodes selectedContent;

   public XPathSubsetContentSelector(ContentManager cm, XBLOMContentElement content, Element bound, String selector) {
      super(cm, content, bound);
      this.parseSelector(selector);
   }

   protected void parseSelector(String selector) {
      this.selectorType = -1;
      Scanner scanner = new Scanner(selector);
      int token = scanner.next();
      if (token == 1) {
         String name1 = scanner.getStringValue();
         token = scanner.next();
         if (token == 0) {
            this.selectorType = 1;
            this.prefix = null;
            this.localName = name1;
            this.index = 0;
            return;
         }

         if (token == 2) {
            token = scanner.next();
            String name2;
            if (token == 1) {
               name2 = scanner.getStringValue();
               token = scanner.next();
               if (token == 0) {
                  this.selectorType = 1;
                  this.prefix = name1;
                  this.localName = name2;
                  this.index = 0;
                  return;
               }

               if (token == 3) {
                  token = scanner.next();
                  if (token == 8) {
                     int number = Integer.parseInt(scanner.getStringValue());
                     token = scanner.next();
                     if (token == 4) {
                        token = scanner.next();
                        if (token == 0) {
                           this.selectorType = 1;
                           this.prefix = name1;
                           this.localName = name2;
                           this.index = number;
                           return;
                        }
                     }
                  }
               }
            } else if (token == 3) {
               token = scanner.next();
               if (token == 8) {
                  int number = Integer.parseInt(scanner.getStringValue());
                  token = scanner.next();
                  if (token == 4) {
                     token = scanner.next();
                     if (token == 0) {
                        this.selectorType = 1;
                        this.prefix = null;
                        this.localName = name1;
                        this.index = number;
                        return;
                     }
                  }
               }
            } else if (token == 5 && name1.equals("id")) {
               token = scanner.next();
               if (token == 7) {
                  name2 = scanner.getStringValue();
                  token = scanner.next();
                  if (token == 6) {
                     token = scanner.next();
                     if (token == 0) {
                        this.selectorType = 2;
                        this.localName = name2;
                        return;
                     }
                  }
               }
            }
         }
      } else if (token == 9) {
         token = scanner.next();
         if (token == 0) {
            this.selectorType = 0;
            return;
         }

         if (token == 3) {
            token = scanner.next();
            if (token == 8) {
               int number = Integer.parseInt(scanner.getStringValue());
               token = scanner.next();
               if (token == 4) {
                  token = scanner.next();
                  if (token == 0) {
                     this.selectorType = 0;
                     this.index = number;
                     return;
                  }
               }
            }
         }
      }

   }

   public NodeList getSelectedContent() {
      if (this.selectedContent == null) {
         this.selectedContent = new SelectedNodes();
      }

      return this.selectedContent;
   }

   boolean update() {
      if (this.selectedContent == null) {
         this.selectedContent = new SelectedNodes();
         return true;
      } else {
         return this.selectedContent.update();
      }
   }

   protected static class Scanner extends AbstractScanner {
      public static final int EOF = 0;
      public static final int NAME = 1;
      public static final int COLON = 2;
      public static final int LEFT_SQUARE_BRACKET = 3;
      public static final int RIGHT_SQUARE_BRACKET = 4;
      public static final int LEFT_PARENTHESIS = 5;
      public static final int RIGHT_PARENTHESIS = 6;
      public static final int STRING = 7;
      public static final int NUMBER = 8;
      public static final int ASTERISK = 9;

      public Scanner(String s) {
         super(s);
      }

      protected int endGap() {
         return this.current == -1 ? 0 : 1;
      }

      protected void nextToken() throws ParseException {
         try {
            switch (this.current) {
               case -1:
                  this.type = 0;
                  return;
               case 0:
               case 1:
               case 2:
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
               case 8:
               case 11:
               case 14:
               case 15:
               case 16:
               case 17:
               case 18:
               case 19:
               case 20:
               case 21:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 27:
               case 28:
               case 29:
               case 30:
               case 31:
               case 33:
               case 35:
               case 36:
               case 37:
               case 38:
               case 43:
               case 44:
               case 45:
               case 46:
               case 47:
               case 59:
               case 60:
               case 61:
               case 62:
               case 63:
               case 64:
               case 65:
               case 66:
               case 67:
               case 68:
               case 69:
               case 70:
               case 71:
               case 72:
               case 73:
               case 74:
               case 75:
               case 76:
               case 77:
               case 78:
               case 79:
               case 80:
               case 81:
               case 82:
               case 83:
               case 84:
               case 85:
               case 86:
               case 87:
               case 88:
               case 89:
               case 90:
               case 92:
               default:
                  if (!XMLUtilities.isXMLNameFirstCharacter((char)this.current)) {
                     this.nextChar();
                     throw new ParseException("identifier.character", this.reader.getLine(), this.reader.getColumn());
                  }

                  do {
                     this.nextChar();
                  } while(this.current != -1 && this.current != 58 && XMLUtilities.isXMLNameCharacter((char)this.current));

                  this.type = 1;
                  return;
               case 9:
               case 10:
               case 12:
               case 13:
               case 32:
                  do {
                     this.nextChar();
                  } while(XMLUtilities.isXMLSpace((char)this.current));

                  this.nextToken();
                  return;
               case 34:
                  this.type = this.string2();
                  return;
               case 39:
                  this.type = this.string1();
                  return;
               case 40:
                  this.nextChar();
                  this.type = 5;
                  return;
               case 41:
                  this.nextChar();
                  this.type = 6;
                  return;
               case 42:
                  this.nextChar();
                  this.type = 9;
                  return;
               case 48:
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               case 56:
               case 57:
                  this.type = this.number();
                  return;
               case 58:
                  this.nextChar();
                  this.type = 2;
                  return;
               case 91:
                  this.nextChar();
                  this.type = 3;
                  return;
               case 93:
                  this.nextChar();
                  this.type = 4;
            }
         } catch (IOException var2) {
            throw new ParseException(var2);
         }
      }

      protected int string1() throws IOException {
         this.start = this.position;

         while(true) {
            switch (this.nextChar()) {
               case -1:
                  throw new ParseException("eof", this.reader.getLine(), this.reader.getColumn());
               case 39:
                  this.nextChar();
                  return 7;
            }
         }
      }

      protected int string2() throws IOException {
         this.start = this.position;

         while(true) {
            switch (this.nextChar()) {
               case -1:
                  throw new ParseException("eof", this.reader.getLine(), this.reader.getColumn());
               case 34:
                  this.nextChar();
                  return 7;
            }
         }
      }

      protected int number() throws IOException {
         while(true) {
            switch (this.nextChar()) {
               case 46:
                  switch (this.nextChar()) {
                     case 48:
                     case 49:
                     case 50:
                     case 51:
                     case 52:
                     case 53:
                     case 54:
                     case 55:
                     case 56:
                     case 57:
                        return this.dotNumber();
                     default:
                        throw new ParseException("character", this.reader.getLine(), this.reader.getColumn());
                  }
               case 47:
               default:
                  return 8;
               case 48:
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               case 56:
               case 57:
            }
         }
      }

      protected int dotNumber() throws IOException {
         while(true) {
            switch (this.nextChar()) {
               case 48:
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               case 56:
               case 57:
                  break;
               default:
                  return 8;
            }
         }
      }
   }

   protected class SelectedNodes implements NodeList {
      protected ArrayList nodes = new ArrayList(10);

      public SelectedNodes() {
         this.update();
      }

      protected boolean update() {
         ArrayList oldNodes = (ArrayList)this.nodes.clone();
         this.nodes.clear();
         int nth = 0;

         for(Node n = XPathSubsetContentSelector.this.boundElement.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1) {
               Element e = (Element)n;
               boolean matched = XPathSubsetContentSelector.this.selectorType == 0;
               switch (XPathSubsetContentSelector.this.selectorType) {
                  case 1:
                     if (XPathSubsetContentSelector.this.prefix == null) {
                        matched = e.getNamespaceURI() == null;
                     } else {
                        String ns = XPathSubsetContentSelector.this.contentElement.lookupNamespaceURI(XPathSubsetContentSelector.this.prefix);
                        if (ns != null) {
                           matched = e.getNamespaceURI().equals(ns);
                        }
                     }

                     matched = matched && XPathSubsetContentSelector.this.localName.equals(e.getLocalName());
                     break;
                  case 2:
                     matched = e.getAttributeNS((String)null, "id").equals(XPathSubsetContentSelector.this.localName);
               }

               if (XPathSubsetContentSelector.this.selectorType == 0 || XPathSubsetContentSelector.this.selectorType == 1) {
                  boolean var10000;
                  label73: {
                     label72: {
                        if (matched) {
                           if (XPathSubsetContentSelector.this.index == 0) {
                              break label72;
                           }

                           ++nth;
                           if (nth == XPathSubsetContentSelector.this.index) {
                              break label72;
                           }
                        }

                        var10000 = false;
                        break label73;
                     }

                     var10000 = true;
                  }

                  matched = var10000;
               }

               if (matched && !XPathSubsetContentSelector.this.isSelected(n)) {
                  this.nodes.add(e);
               }
            }
         }

         int nodesSize = this.nodes.size();
         if (oldNodes.size() != nodesSize) {
            return true;
         } else {
            for(int i = 0; i < nodesSize; ++i) {
               if (oldNodes.get(i) != this.nodes.get(i)) {
                  return true;
               }
            }

            return false;
         }
      }

      public Node item(int index) {
         return index >= 0 && index < this.nodes.size() ? (Node)this.nodes.get(index) : null;
      }

      public int getLength() {
         return this.nodes.size();
      }
   }
}
