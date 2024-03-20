package org.apache.batik.css.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import org.apache.batik.i18n.Localizable;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.ParsedURL;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionFactory;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SimpleSelector;

public class Parser implements ExtendedParser, Localizable {
   public static final String BUNDLE_CLASSNAME = "org.apache.batik.css.parser.resources.Messages";
   protected LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.css.parser.resources.Messages", Parser.class.getClassLoader());
   protected Scanner scanner;
   protected int current;
   protected DocumentHandler documentHandler;
   protected SelectorFactory selectorFactory;
   protected ConditionFactory conditionFactory;
   protected ErrorHandler errorHandler;
   protected String pseudoElement;
   protected String documentURI;

   public Parser() {
      this.documentHandler = DefaultDocumentHandler.INSTANCE;
      this.selectorFactory = DefaultSelectorFactory.INSTANCE;
      this.conditionFactory = DefaultConditionFactory.INSTANCE;
      this.errorHandler = DefaultErrorHandler.INSTANCE;
   }

   public String getParserVersion() {
      return "http://www.w3.org/TR/REC-CSS2";
   }

   public void setLocale(Locale locale) throws CSSException {
      this.localizableSupport.setLocale(locale);
   }

   public Locale getLocale() {
      return this.localizableSupport.getLocale();
   }

   public String formatMessage(String key, Object[] args) throws MissingResourceException {
      return this.localizableSupport.formatMessage(key, args);
   }

   public void setDocumentHandler(DocumentHandler handler) {
      this.documentHandler = handler;
   }

   public void setSelectorFactory(SelectorFactory factory) {
      this.selectorFactory = factory;
   }

   public void setConditionFactory(ConditionFactory factory) {
      this.conditionFactory = factory;
   }

   public void setErrorHandler(ErrorHandler handler) {
      this.errorHandler = handler;
   }

   public void parseStyleSheet(InputSource source) throws CSSException, IOException {
      this.scanner = this.createScanner(source);

      try {
         this.documentHandler.startDocument(source);
         this.current = this.scanner.next();
         switch (this.current) {
            case 18:
               this.documentHandler.comment(this.scanner.getStringValue());
               break;
            case 30:
               if (this.nextIgnoreSpaces() != 19) {
                  this.reportError("charset.string");
               } else {
                  if (this.nextIgnoreSpaces() != 8) {
                     this.reportError("semicolon");
                  }

                  this.next();
               }
         }

         this.skipSpacesAndCDOCDC();

         while(this.current == 28) {
            this.nextIgnoreSpaces();
            this.parseImportRule();
            this.nextIgnoreSpaces();
         }

         while(true) {
            switch (this.current) {
               case 0:
                  return;
               case 29:
                  this.nextIgnoreSpaces();
                  this.parseAtRule();
                  break;
               case 31:
                  this.nextIgnoreSpaces();
                  this.parseFontFaceRule();
                  break;
               case 32:
                  this.nextIgnoreSpaces();
                  this.parseMediaRule();
                  break;
               case 33:
                  this.nextIgnoreSpaces();
                  this.parsePageRule();
                  break;
               default:
                  this.parseRuleSet();
            }

            this.skipSpacesAndCDOCDC();
         }
      } finally {
         this.documentHandler.endDocument(source);
         this.scanner.close();
         this.scanner = null;
      }
   }

   public void parseStyleSheet(String uri) throws CSSException, IOException {
      this.parseStyleSheet(new InputSource(uri));
   }

   public void parseStyleDeclaration(InputSource source) throws CSSException, IOException {
      this.scanner = this.createScanner(source);
      this.parseStyleDeclarationInternal();
   }

   protected void parseStyleDeclarationInternal() throws CSSException, IOException {
      this.nextIgnoreSpaces();

      try {
         this.parseStyleDeclaration(false);
      } catch (CSSParseException var5) {
         this.reportError(var5);
      } finally {
         this.scanner.close();
         this.scanner = null;
      }

   }

   public void parseRule(InputSource source) throws CSSException, IOException {
      this.scanner = this.createScanner(source);
      this.parseRuleInternal();
   }

   protected void parseRuleInternal() throws CSSException, IOException {
      this.nextIgnoreSpaces();
      this.parseRule();
      this.scanner.close();
      this.scanner = null;
   }

   public SelectorList parseSelectors(InputSource source) throws CSSException, IOException {
      this.scanner = this.createScanner(source);
      return this.parseSelectorsInternal();
   }

   protected SelectorList parseSelectorsInternal() throws CSSException, IOException {
      this.nextIgnoreSpaces();
      SelectorList ret = this.parseSelectorList();
      this.scanner.close();
      this.scanner = null;
      return ret;
   }

   public LexicalUnit parsePropertyValue(InputSource source) throws CSSException, IOException {
      this.scanner = this.createScanner(source);
      return this.parsePropertyValueInternal();
   }

   protected LexicalUnit parsePropertyValueInternal() throws CSSException, IOException {
      this.nextIgnoreSpaces();
      LexicalUnit exp = null;

      try {
         exp = this.parseExpression(false);
      } catch (CSSParseException var3) {
         this.reportError(var3);
         throw var3;
      }

      CSSParseException exception = null;
      if (this.current != 0) {
         exception = this.createCSSParseException("eof.expected");
      }

      this.scanner.close();
      this.scanner = null;
      if (exception != null) {
         this.errorHandler.fatalError(exception);
      }

      return exp;
   }

   public boolean parsePriority(InputSource source) throws CSSException, IOException {
      this.scanner = this.createScanner(source);
      return this.parsePriorityInternal();
   }

   protected boolean parsePriorityInternal() throws CSSException, IOException {
      this.nextIgnoreSpaces();
      this.scanner.close();
      this.scanner = null;
      switch (this.current) {
         case 0:
            return false;
         case 28:
            return true;
         default:
            this.reportError("token", new Object[]{this.current});
            return false;
      }
   }

   protected void parseRule() {
      switch (this.scanner.getType()) {
         case 28:
            this.nextIgnoreSpaces();
            this.parseImportRule();
            break;
         case 29:
            this.nextIgnoreSpaces();
            this.parseAtRule();
            break;
         case 30:
         default:
            this.parseRuleSet();
            break;
         case 31:
            this.nextIgnoreSpaces();
            this.parseFontFaceRule();
            break;
         case 32:
            this.nextIgnoreSpaces();
            this.parseMediaRule();
            break;
         case 33:
            this.nextIgnoreSpaces();
            this.parsePageRule();
      }

   }

   protected void parseAtRule() {
      this.scanner.scanAtRule();
      this.documentHandler.ignorableAtRule(this.scanner.getStringValue());
      this.nextIgnoreSpaces();
   }

   protected void parseImportRule() {
      String uri = null;
      switch (this.current) {
         case 19:
         case 51:
            uri = this.scanner.getStringValue();
            this.nextIgnoreSpaces();
            CSSSACMediaList ml;
            if (this.current != 20) {
               ml = new CSSSACMediaList();
               ml.append("all");
            } else {
               ml = this.parseMediaList();
            }

            this.documentHandler.importStyle(uri, ml, (String)null);
            if (this.current != 8) {
               this.reportError("semicolon");
            } else {
               this.next();
            }

            return;
         default:
            this.reportError("string.or.uri");
      }
   }

   protected CSSSACMediaList parseMediaList() {
      CSSSACMediaList result = new CSSSACMediaList();
      result.append(this.scanner.getStringValue());
      this.nextIgnoreSpaces();

      while(this.current == 6) {
         this.nextIgnoreSpaces();
         switch (this.current) {
            case 20:
               result.append(this.scanner.getStringValue());
               this.nextIgnoreSpaces();
               break;
            default:
               this.reportError("identifier");
         }
      }

      return result;
   }

   protected void parseFontFaceRule() {
      try {
         this.documentHandler.startFontFace();
         if (this.current != 1) {
            this.reportError("left.curly.brace");
         } else {
            this.nextIgnoreSpaces();

            try {
               this.parseStyleDeclaration(true);
            } catch (CSSParseException var5) {
               this.reportError(var5);
            }
         }
      } finally {
         this.documentHandler.endFontFace();
      }

   }

   protected void parsePageRule() {
      String page = null;
      String ppage = null;
      if (this.current == 20) {
         page = this.scanner.getStringValue();
         this.nextIgnoreSpaces();
         if (this.current == 16) {
            this.nextIgnoreSpaces();
            if (this.current != 20) {
               this.reportError("identifier");
               return;
            }

            ppage = this.scanner.getStringValue();
            this.nextIgnoreSpaces();
         }
      }

      try {
         this.documentHandler.startPage(page, ppage);
         if (this.current != 1) {
            this.reportError("left.curly.brace");
         } else {
            this.nextIgnoreSpaces();

            try {
               this.parseStyleDeclaration(true);
            } catch (CSSParseException var7) {
               this.reportError(var7);
            }
         }
      } finally {
         this.documentHandler.endPage(page, ppage);
      }

   }

   protected void parseMediaRule() {
      if (this.current != 20) {
         this.reportError("identifier");
      } else {
         CSSSACMediaList ml = this.parseMediaList();

         try {
            this.documentHandler.startMedia(ml);
            if (this.current != 1) {
               this.reportError("left.curly.brace");
            } else {
               this.nextIgnoreSpaces();

               while(true) {
                  switch (this.current) {
                     case 0:
                     case 2:
                        this.nextIgnoreSpaces();
                        return;
                     default:
                        this.parseRuleSet();
                  }
               }
            }
         } finally {
            this.documentHandler.endMedia(ml);
         }

      }
   }

   protected void parseRuleSet() {
      SelectorList sl = null;

      try {
         sl = this.parseSelectorList();
      } catch (CSSParseException var9) {
         this.reportError(var9);
         return;
      }

      try {
         this.documentHandler.startSelector(sl);
         if (this.current != 1) {
            this.reportError("left.curly.brace");
            if (this.current == 2) {
               this.nextIgnoreSpaces();
            }
         } else {
            this.nextIgnoreSpaces();

            try {
               this.parseStyleDeclaration(true);
            } catch (CSSParseException var7) {
               this.reportError(var7);
            }
         }
      } finally {
         this.documentHandler.endSelector(sl);
      }

   }

   protected SelectorList parseSelectorList() {
      CSSSelectorList result = new CSSSelectorList();
      result.append(this.parseSelector());

      while(this.current == 6) {
         this.nextIgnoreSpaces();
         result.append(this.parseSelector());
      }

      return result;
   }

   protected Selector parseSelector() {
      this.pseudoElement = null;
      Selector result = this.parseSimpleSelector();

      while(true) {
         switch (this.current) {
            case 4:
               if (this.pseudoElement != null) {
                  throw this.createCSSParseException("pseudo.element.position");
               }

               this.nextIgnoreSpaces();
               result = this.selectorFactory.createDirectAdjacentSelector((short)1, (Selector)result, this.parseSimpleSelector());
               break;
            case 5:
            case 6:
            case 8:
            case 10:
            case 12:
            case 14:
            case 15:
            case 17:
            case 18:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            default:
               if (this.pseudoElement != null) {
                  result = this.selectorFactory.createChildSelector((Selector)result, this.selectorFactory.createPseudoElementSelector((String)null, this.pseudoElement));
               }

               return (Selector)result;
            case 7:
            case 11:
            case 13:
            case 16:
            case 20:
            case 27:
               if (this.pseudoElement != null) {
                  throw this.createCSSParseException("pseudo.element.position");
               }

               result = this.selectorFactory.createDescendantSelector((Selector)result, this.parseSimpleSelector());
               break;
            case 9:
               if (this.pseudoElement != null) {
                  throw this.createCSSParseException("pseudo.element.position");
               }

               this.nextIgnoreSpaces();
               result = this.selectorFactory.createChildSelector((Selector)result, this.parseSimpleSelector());
         }
      }
   }

   protected SimpleSelector parseSimpleSelector() {
      Object result;
      switch (this.current) {
         case 13:
            this.next();
         default:
            result = this.selectorFactory.createElementSelector((String)null, (String)null);
            break;
         case 20:
            result = this.selectorFactory.createElementSelector((String)null, this.scanner.getStringValue());
            this.next();
      }

      Condition cond = null;

      while(true) {
         Object c;
         c = null;
         String val;
         label76:
         switch (this.current) {
            case 7:
               if (this.next() != 20) {
                  throw this.createCSSParseException("identifier");
               }

               c = this.conditionFactory.createClassCondition((String)null, this.scanner.getStringValue());
               this.next();
               break;
            case 11:
               if (this.nextIgnoreSpaces() != 20) {
                  throw this.createCSSParseException("identifier");
               }

               String name = this.scanner.getStringValue();
               int op = this.nextIgnoreSpaces();
               switch (op) {
                  case 3:
                  case 25:
                  case 26:
                     val = null;
                     switch (this.nextIgnoreSpaces()) {
                        case 19:
                        case 20:
                           val = this.scanner.getStringValue();
                           this.nextIgnoreSpaces();
                           if (this.current != 12) {
                              throw this.createCSSParseException("right.bracket");
                           }

                           this.next();
                           switch (op) {
                              case 3:
                                 c = this.conditionFactory.createAttributeCondition(name, (String)null, false, val);
                                 break label76;
                              case 26:
                                 c = this.conditionFactory.createOneOfAttributeCondition(name, (String)null, false, val);
                                 break label76;
                              default:
                                 c = this.conditionFactory.createBeginHyphenAttributeCondition(name, (String)null, false, val);
                                 break label76;
                           }
                        default:
                           throw this.createCSSParseException("identifier.or.string");
                     }
                  case 12:
                     this.next();
                     c = this.conditionFactory.createAttributeCondition(name, (String)null, false, (String)null);
                     break label76;
                  default:
                     throw this.createCSSParseException("right.bracket");
               }
            case 16:
               switch (this.nextIgnoreSpaces()) {
                  case 20:
                     val = this.scanner.getStringValue();
                     if (this.isPseudoElement(val)) {
                        if (this.pseudoElement != null) {
                           throw this.createCSSParseException("duplicate.pseudo.element");
                        }

                        this.pseudoElement = val;
                     } else {
                        c = this.conditionFactory.createPseudoClassCondition((String)null, val);
                     }

                     this.next();
                     break label76;
                  case 52:
                     String func = this.scanner.getStringValue();
                     if (this.nextIgnoreSpaces() != 20) {
                        throw this.createCSSParseException("identifier");
                     }

                     String lang = this.scanner.getStringValue();
                     if (this.nextIgnoreSpaces() != 15) {
                        throw this.createCSSParseException("right.brace");
                     }

                     if (!func.equalsIgnoreCase("lang")) {
                        throw this.createCSSParseException("pseudo.function");
                     }

                     c = this.conditionFactory.createLangCondition(lang);
                     this.next();
                     break label76;
                  default:
                     throw this.createCSSParseException("identifier");
               }
            case 27:
               c = this.conditionFactory.createIdCondition(this.scanner.getStringValue());
               this.next();
               break;
            default:
               this.skipSpaces();
               if (cond != null) {
                  result = this.selectorFactory.createConditionalSelector((SimpleSelector)result, (Condition)cond);
               }

               return (SimpleSelector)result;
         }

         if (c != null) {
            if (cond == null) {
               cond = c;
            } else {
               cond = this.conditionFactory.createAndCondition((Condition)cond, (Condition)c);
            }
         }
      }
   }

   protected boolean isPseudoElement(String s) {
      switch (s.charAt(0)) {
         case 'A':
         case 'a':
            return s.equalsIgnoreCase("after");
         case 'B':
         case 'b':
            return s.equalsIgnoreCase("before");
         case 'F':
         case 'f':
            return s.equalsIgnoreCase("first-letter") || s.equalsIgnoreCase("first-line");
         default:
            return false;
      }
   }

   protected void parseStyleDeclaration(boolean inSheet) throws CSSException {
      while(true) {
         switch (this.current) {
            case 0:
               if (inSheet) {
                  throw this.createCSSParseException("eof");
               }

               return;
            case 2:
               if (!inSheet) {
                  throw this.createCSSParseException("eof.expected");
               }

               this.nextIgnoreSpaces();
               return;
            case 8:
               this.nextIgnoreSpaces();
               break;
            case 20:
               String name = this.scanner.getStringValue();
               if (this.nextIgnoreSpaces() != 16) {
                  throw this.createCSSParseException("colon");
               }

               this.nextIgnoreSpaces();
               LexicalUnit exp = null;

               try {
                  exp = this.parseExpression(false);
               } catch (CSSParseException var5) {
                  this.reportError(var5);
               }

               if (exp == null) {
                  break;
               }

               boolean important = false;
               if (this.current == 23) {
                  important = true;
                  this.nextIgnoreSpaces();
               }

               this.documentHandler.property(name, exp, important);
               break;
            default:
               throw this.createCSSParseException("identifier");
         }
      }
   }

   protected LexicalUnit parseExpression(boolean param) {
      LexicalUnit result = this.parseTerm((LexicalUnit)null);
      LexicalUnit curr = result;

      while(true) {
         boolean op = false;
         switch (this.current) {
            case 6:
               op = true;
               curr = CSSLexicalUnit.createSimple((short)0, (LexicalUnit)curr);
               this.nextIgnoreSpaces();
               break;
            case 10:
               op = true;
               curr = CSSLexicalUnit.createSimple((short)4, (LexicalUnit)curr);
               this.nextIgnoreSpaces();
         }

         if (param) {
            if (this.current == 15) {
               if (op) {
                  throw this.createCSSParseException("token", new Object[]{this.current});
               }

               return result;
            }

            curr = this.parseTerm((LexicalUnit)curr);
         } else {
            switch (this.current) {
               case 0:
               case 2:
               case 8:
               case 23:
                  if (op) {
                     throw this.createCSSParseException("token", new Object[]{this.current});
                  }

                  return result;
               default:
                  curr = this.parseTerm((LexicalUnit)curr);
            }
         }
      }
   }

   protected LexicalUnit parseTerm(LexicalUnit prev) {
      boolean plus = true;
      boolean sgn = false;
      switch (this.current) {
         case 5:
            plus = false;
         case 4:
            this.next();
            sgn = true;
         default:
            String val;
            switch (this.current) {
               case 24:
                  val = this.scanner.getStringValue();
                  if (!plus) {
                     val = "-" + val;
                  }

                  long lVal = Long.parseLong(val);
                  if (lVal >= -2147483648L && lVal <= 2147483647L) {
                     int iVal = (int)lVal;
                     this.nextIgnoreSpaces();
                     return CSSLexicalUnit.createInteger(iVal, prev);
                  }
               case 54:
                  return CSSLexicalUnit.createFloat((short)14, this.number(plus), prev);
               case 25:
               case 26:
               case 27:
               case 28:
               case 29:
               case 30:
               case 31:
               case 32:
               case 33:
               case 51:
               case 53:
               default:
                  if (sgn) {
                     throw this.createCSSParseException("token", new Object[]{this.current});
                  } else {
                     switch (this.current) {
                        case 19:
                           val = this.scanner.getStringValue();
                           this.nextIgnoreSpaces();
                           return CSSLexicalUnit.createString((short)36, val, prev);
                        case 20:
                           val = this.scanner.getStringValue();
                           this.nextIgnoreSpaces();
                           if (val.equalsIgnoreCase("inherit")) {
                              return CSSLexicalUnit.createSimple((short)12, prev);
                           }

                           return CSSLexicalUnit.createString((short)35, val, prev);
                        case 27:
                           return this.hexcolor(prev);
                        case 51:
                           val = this.scanner.getStringValue();
                           this.nextIgnoreSpaces();
                           return CSSLexicalUnit.createString((short)24, val, prev);
                        default:
                           throw this.createCSSParseException("token", new Object[]{this.current});
                     }
                  }
               case 34:
                  return this.dimension(plus, prev);
               case 35:
                  return CSSLexicalUnit.createFloat((short)16, this.number(plus), prev);
               case 36:
                  return CSSLexicalUnit.createFloat((short)15, this.number(plus), prev);
               case 37:
                  return CSSLexicalUnit.createFloat((short)19, this.number(plus), prev);
               case 38:
                  return CSSLexicalUnit.createFloat((short)20, this.number(plus), prev);
               case 39:
                  return CSSLexicalUnit.createFloat((short)18, this.number(plus), prev);
               case 40:
                  return CSSLexicalUnit.createFloat((short)31, this.number(plus), prev);
               case 41:
                  return CSSLexicalUnit.createFloat((short)33, this.number(plus), prev);
               case 42:
                  return CSSLexicalUnit.createFloat((short)23, this.number(plus), prev);
               case 43:
                  return CSSLexicalUnit.createFloat((short)32, this.number(plus), prev);
               case 44:
                  return CSSLexicalUnit.createFloat((short)22, this.number(plus), prev);
               case 45:
                  return CSSLexicalUnit.createFloat((short)21, this.number(plus), prev);
               case 46:
                  return CSSLexicalUnit.createFloat((short)17, this.number(plus), prev);
               case 47:
                  return CSSLexicalUnit.createFloat((short)28, this.number(plus), prev);
               case 48:
                  return CSSLexicalUnit.createFloat((short)30, this.number(plus), prev);
               case 49:
                  return CSSLexicalUnit.createFloat((short)29, this.number(plus), prev);
               case 50:
                  return CSSLexicalUnit.createFloat((short)34, this.number(plus), prev);
               case 52:
                  return this.parseFunction(plus, prev);
            }
      }
   }

   protected LexicalUnit parseFunction(boolean positive, LexicalUnit prev) {
      String name = this.scanner.getStringValue();
      this.nextIgnoreSpaces();
      LexicalUnit params = this.parseExpression(true);
      if (this.current != 15) {
         throw this.createCSSParseException("token", new Object[]{this.current});
      } else {
         this.nextIgnoreSpaces();
         LexicalUnit lu;
         switch (name.charAt(0)) {
            case 'A':
            case 'a':
               if (name.equalsIgnoreCase("attr") && params != null) {
                  switch (params.getLexicalUnitType()) {
                     case 35:
                        lu = params.getNextLexicalUnit();
                        if (lu == null) {
                           return CSSLexicalUnit.createString((short)37, params.getStringValue(), prev);
                        }
                  }
               }
               break;
            case 'C':
            case 'c':
               if (name.equalsIgnoreCase("counter")) {
                  if (params != null) {
                     switch (params.getLexicalUnitType()) {
                        case 35:
                           lu = params.getNextLexicalUnit();
                           if (lu != null) {
                              switch (lu.getLexicalUnitType()) {
                                 case 0:
                                    lu = lu.getNextLexicalUnit();
                                    if (lu != null) {
                                       switch (lu.getLexicalUnitType()) {
                                          case 35:
                                             lu = lu.getNextLexicalUnit();
                                             if (lu == null) {
                                                return CSSLexicalUnit.createPredefinedFunction((short)25, params, prev);
                                             }
                                       }
                                    }
                              }
                           }
                     }
                  }
               } else if (name.equalsIgnoreCase("counters") && params != null) {
                  switch (params.getLexicalUnitType()) {
                     case 35:
                        lu = params.getNextLexicalUnit();
                        if (lu != null) {
                           switch (lu.getLexicalUnitType()) {
                              case 0:
                                 lu = lu.getNextLexicalUnit();
                                 if (lu != null) {
                                    switch (lu.getLexicalUnitType()) {
                                       case 36:
                                          lu = lu.getNextLexicalUnit();
                                          if (lu != null) {
                                             switch (lu.getLexicalUnitType()) {
                                                case 0:
                                                   lu = lu.getNextLexicalUnit();
                                                   if (lu != null) {
                                                      switch (lu.getLexicalUnitType()) {
                                                         case 35:
                                                            lu = lu.getNextLexicalUnit();
                                                            if (lu == null) {
                                                               return CSSLexicalUnit.createPredefinedFunction((short)26, params, prev);
                                                            }
                                                      }
                                                   }
                                             }
                                          }
                                    }
                                 }
                           }
                        }
                  }
               }
               break;
            case 'R':
            case 'r':
               if (name.equalsIgnoreCase("rgb")) {
                  if (params != null) {
                     switch (params.getLexicalUnitType()) {
                        case 13:
                        case 23:
                           lu = params.getNextLexicalUnit();
                           if (lu != null) {
                              switch (lu.getLexicalUnitType()) {
                                 case 0:
                                    lu = lu.getNextLexicalUnit();
                                    if (lu != null) {
                                       switch (lu.getLexicalUnitType()) {
                                          case 13:
                                          case 23:
                                             lu = lu.getNextLexicalUnit();
                                             if (lu != null) {
                                                switch (lu.getLexicalUnitType()) {
                                                   case 0:
                                                      lu = lu.getNextLexicalUnit();
                                                      if (lu != null) {
                                                         switch (lu.getLexicalUnitType()) {
                                                            case 13:
                                                            case 23:
                                                               lu = lu.getNextLexicalUnit();
                                                               if (lu == null) {
                                                                  return CSSLexicalUnit.createPredefinedFunction((short)27, params, prev);
                                                               }
                                                         }
                                                      }
                                                }
                                             }
                                       }
                                    }
                              }
                           }
                     }
                  }
               } else if (name.equalsIgnoreCase("rect") && params != null) {
                  switch (params.getLexicalUnitType()) {
                     case 13:
                        if (params.getIntegerValue() == 0) {
                           lu = params.getNextLexicalUnit();
                           break;
                        }

                        return CSSLexicalUnit.createFunction(name, params, prev);
                     case 14:
                     case 24:
                     case 25:
                     case 26:
                     case 27:
                     case 28:
                     case 29:
                     case 30:
                     case 31:
                     case 32:
                     case 33:
                     case 34:
                     default:
                        return CSSLexicalUnit.createFunction(name, params, prev);
                     case 15:
                     case 16:
                     case 17:
                     case 18:
                     case 19:
                     case 20:
                     case 21:
                     case 22:
                     case 23:
                        lu = params.getNextLexicalUnit();
                        break;
                     case 35:
                        if (!params.getStringValue().equalsIgnoreCase("auto")) {
                           return CSSLexicalUnit.createFunction(name, params, prev);
                        }

                        lu = params.getNextLexicalUnit();
                  }

                  if (lu != null) {
                     switch (lu.getLexicalUnitType()) {
                        case 0:
                           lu = lu.getNextLexicalUnit();
                           if (lu != null) {
                              switch (lu.getLexicalUnitType()) {
                                 case 13:
                                    if (lu.getIntegerValue() == 0) {
                                       lu = lu.getNextLexicalUnit();
                                       break;
                                    }

                                    return CSSLexicalUnit.createFunction(name, params, prev);
                                 case 14:
                                 case 24:
                                 case 25:
                                 case 26:
                                 case 27:
                                 case 28:
                                 case 29:
                                 case 30:
                                 case 31:
                                 case 32:
                                 case 33:
                                 case 34:
                                 default:
                                    return CSSLexicalUnit.createFunction(name, params, prev);
                                 case 15:
                                 case 16:
                                 case 17:
                                 case 18:
                                 case 19:
                                 case 20:
                                 case 21:
                                 case 22:
                                 case 23:
                                    lu = lu.getNextLexicalUnit();
                                    break;
                                 case 35:
                                    if (!lu.getStringValue().equalsIgnoreCase("auto")) {
                                       return CSSLexicalUnit.createFunction(name, params, prev);
                                    }

                                    lu = lu.getNextLexicalUnit();
                              }

                              if (lu != null) {
                                 switch (lu.getLexicalUnitType()) {
                                    case 0:
                                       lu = lu.getNextLexicalUnit();
                                       if (lu != null) {
                                          switch (lu.getLexicalUnitType()) {
                                             case 13:
                                                if (lu.getIntegerValue() == 0) {
                                                   lu = lu.getNextLexicalUnit();
                                                   break;
                                                }

                                                return CSSLexicalUnit.createFunction(name, params, prev);
                                             case 14:
                                             case 24:
                                             case 25:
                                             case 26:
                                             case 27:
                                             case 28:
                                             case 29:
                                             case 30:
                                             case 31:
                                             case 32:
                                             case 33:
                                             case 34:
                                             default:
                                                return CSSLexicalUnit.createFunction(name, params, prev);
                                             case 15:
                                             case 16:
                                             case 17:
                                             case 18:
                                             case 19:
                                             case 20:
                                             case 21:
                                             case 22:
                                             case 23:
                                                lu = lu.getNextLexicalUnit();
                                                break;
                                             case 35:
                                                if (!lu.getStringValue().equalsIgnoreCase("auto")) {
                                                   return CSSLexicalUnit.createFunction(name, params, prev);
                                                }

                                                lu = lu.getNextLexicalUnit();
                                          }

                                          if (lu != null) {
                                             switch (lu.getLexicalUnitType()) {
                                                case 0:
                                                   lu = lu.getNextLexicalUnit();
                                                   if (lu != null) {
                                                      switch (lu.getLexicalUnitType()) {
                                                         case 13:
                                                            if (lu.getIntegerValue() == 0) {
                                                               lu = lu.getNextLexicalUnit();
                                                               break;
                                                            }

                                                            return CSSLexicalUnit.createFunction(name, params, prev);
                                                         case 14:
                                                         case 24:
                                                         case 25:
                                                         case 26:
                                                         case 27:
                                                         case 28:
                                                         case 29:
                                                         case 30:
                                                         case 31:
                                                         case 32:
                                                         case 33:
                                                         case 34:
                                                         default:
                                                            return CSSLexicalUnit.createFunction(name, params, prev);
                                                         case 15:
                                                         case 16:
                                                         case 17:
                                                         case 18:
                                                         case 19:
                                                         case 20:
                                                         case 21:
                                                         case 22:
                                                         case 23:
                                                            lu = lu.getNextLexicalUnit();
                                                            break;
                                                         case 35:
                                                            if (!lu.getStringValue().equalsIgnoreCase("auto")) {
                                                               return CSSLexicalUnit.createFunction(name, params, prev);
                                                            }

                                                            lu = lu.getNextLexicalUnit();
                                                      }

                                                      if (lu == null) {
                                                         return CSSLexicalUnit.createPredefinedFunction((short)38, params, prev);
                                                      }
                                                   }
                                             }
                                          }
                                       }
                                 }
                              }
                           }
                     }
                  }
               }
         }

         return CSSLexicalUnit.createFunction(name, params, prev);
      }
   }

   protected LexicalUnit hexcolor(LexicalUnit prev) {
      String val = this.scanner.getStringValue();
      int len = val.length();
      LexicalUnit params = null;
      int r;
      int g;
      int b;
      CSSLexicalUnit tmp;
      switch (len) {
         case 3:
            char rc = Character.toLowerCase(val.charAt(0));
            char gc = Character.toLowerCase(val.charAt(1));
            char bc = Character.toLowerCase(val.charAt(2));
            if (ScannerUtilities.isCSSHexadecimalCharacter(rc) && ScannerUtilities.isCSSHexadecimalCharacter(gc) && ScannerUtilities.isCSSHexadecimalCharacter(bc)) {
               int t;
               r = t = rc >= '0' && rc <= '9' ? rc - 48 : rc - 97 + 10;
               t <<= 4;
               r |= t;
               g = t = gc >= '0' && gc <= '9' ? gc - 48 : gc - 97 + 10;
               t <<= 4;
               g |= t;
               b = t = bc >= '0' && bc <= '9' ? bc - 48 : bc - 97 + 10;
               t <<= 4;
               b |= t;
               params = CSSLexicalUnit.createInteger(r, (LexicalUnit)null);
               tmp = CSSLexicalUnit.createSimple((short)0, params);
               tmp = CSSLexicalUnit.createInteger(g, tmp);
               tmp = CSSLexicalUnit.createSimple((short)0, tmp);
               CSSLexicalUnit.createInteger(b, tmp);
               break;
            }

            throw this.createCSSParseException("rgb.color", new Object[]{val});
         case 6:
            char rc1 = Character.toLowerCase(val.charAt(0));
            char rc2 = Character.toLowerCase(val.charAt(1));
            char gc1 = Character.toLowerCase(val.charAt(2));
            char gc2 = Character.toLowerCase(val.charAt(3));
            char bc1 = Character.toLowerCase(val.charAt(4));
            char bc2 = Character.toLowerCase(val.charAt(5));
            if (ScannerUtilities.isCSSHexadecimalCharacter(rc1) && ScannerUtilities.isCSSHexadecimalCharacter(rc2) && ScannerUtilities.isCSSHexadecimalCharacter(gc1) && ScannerUtilities.isCSSHexadecimalCharacter(gc2) && ScannerUtilities.isCSSHexadecimalCharacter(bc1) && ScannerUtilities.isCSSHexadecimalCharacter(bc2)) {
               r = rc1 >= '0' && rc1 <= '9' ? rc1 - 48 : rc1 - 97 + 10;
               r <<= 4;
               r |= rc2 >= '0' && rc2 <= '9' ? rc2 - 48 : rc2 - 97 + 10;
               g = gc1 >= '0' && gc1 <= '9' ? gc1 - 48 : gc1 - 97 + 10;
               g <<= 4;
               g |= gc2 >= '0' && gc2 <= '9' ? gc2 - 48 : gc2 - 97 + 10;
               b = bc1 >= '0' && bc1 <= '9' ? bc1 - 48 : bc1 - 97 + 10;
               b <<= 4;
               b |= bc2 >= '0' && bc2 <= '9' ? bc2 - 48 : bc2 - 97 + 10;
               params = CSSLexicalUnit.createInteger(r, (LexicalUnit)null);
               tmp = CSSLexicalUnit.createSimple((short)0, params);
               tmp = CSSLexicalUnit.createInteger(g, tmp);
               tmp = CSSLexicalUnit.createSimple((short)0, tmp);
               CSSLexicalUnit.createInteger(b, tmp);
               break;
            }

            throw this.createCSSParseException("rgb.color");
         default:
            throw this.createCSSParseException("rgb.color", new Object[]{val});
      }

      this.nextIgnoreSpaces();
      return CSSLexicalUnit.createPredefinedFunction((short)27, params, prev);
   }

   protected Scanner createScanner(InputSource source) {
      this.documentURI = source.getURI();
      if (this.documentURI == null) {
         this.documentURI = "";
      }

      Reader r = source.getCharacterStream();
      if (r != null) {
         return new Scanner(r);
      } else {
         InputStream is = source.getByteStream();
         if (is != null) {
            return new Scanner(is, source.getEncoding());
         } else {
            String uri = source.getURI();
            if (uri == null) {
               throw new CSSException(this.formatMessage("empty.source", (Object[])null));
            } else {
               try {
                  ParsedURL purl = new ParsedURL(uri);
                  is = purl.openStreamRaw("text/css");
                  return new Scanner(is, source.getEncoding());
               } catch (IOException var6) {
                  throw new CSSException(var6);
               }
            }
         }
      }
   }

   protected int skipSpaces() {
      int lex;
      for(lex = this.scanner.getType(); lex == 17; lex = this.next()) {
      }

      return lex;
   }

   protected int skipSpacesAndCDOCDC() {
      while(true) {
         switch (this.current) {
            case 17:
            case 18:
            case 21:
            case 22:
               this.scanner.clearBuffer();
               this.next();
               break;
            case 19:
            case 20:
            default:
               return this.current;
         }
      }
   }

   protected float number(boolean positive) {
      try {
         float sgn = positive ? 1.0F : -1.0F;
         String val = this.scanner.getStringValue();
         this.nextIgnoreSpaces();
         return sgn * Float.parseFloat(val);
      } catch (NumberFormatException var4) {
         throw this.createCSSParseException("number.format");
      }
   }

   protected LexicalUnit dimension(boolean positive, LexicalUnit prev) {
      try {
         float sgn = positive ? 1.0F : -1.0F;
         String val = this.scanner.getStringValue();
         int i = 0;

         label25:
         while(i < val.length()) {
            switch (val.charAt(i)) {
               case '.':
               case '0':
               case '1':
               case '2':
               case '3':
               case '4':
               case '5':
               case '6':
               case '7':
               case '8':
               case '9':
                  ++i;
                  break;
               case '/':
               default:
                  break label25;
            }
         }

         this.nextIgnoreSpaces();
         return CSSLexicalUnit.createDimension(sgn * Float.parseFloat(val.substring(0, i)), val.substring(i), prev);
      } catch (NumberFormatException var6) {
         throw this.createCSSParseException("number.format");
      }
   }

   protected int next() {
      try {
         while(true) {
            this.scanner.clearBuffer();
            this.current = this.scanner.next();
            if (this.current != 18) {
               return this.current;
            }

            this.documentHandler.comment(this.scanner.getStringValue());
         }
      } catch (ParseException var2) {
         this.reportError(var2.getMessage());
         return this.current;
      }
   }

   protected int nextIgnoreSpaces() {
      try {
         while(true) {
            this.scanner.clearBuffer();
            this.current = this.scanner.next();
            switch (this.current) {
               case 17:
                  break;
               case 18:
                  this.documentHandler.comment(this.scanner.getStringValue());
                  break;
               default:
                  return this.current;
            }
         }
      } catch (ParseException var2) {
         this.errorHandler.error(this.createCSSParseException(var2.getMessage()));
         return this.current;
      }
   }

   protected void reportError(String key) {
      this.reportError(key, (Object[])null);
   }

   protected void reportError(String key, Object[] params) {
      this.reportError(this.createCSSParseException(key, params));
   }

   protected void reportError(CSSParseException e) {
      this.errorHandler.error(e);
      int cbraces = 1;

      while(true) {
         switch (this.current) {
            case 0:
               return;
            case 2:
            case 8:
               --cbraces;
               if (cbraces == 0) {
                  this.nextIgnoreSpaces();
                  return;
               }
            case 1:
               ++cbraces;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            default:
               this.nextIgnoreSpaces();
         }
      }
   }

   protected CSSParseException createCSSParseException(String key) {
      return this.createCSSParseException(key, (Object[])null);
   }

   protected CSSParseException createCSSParseException(String key, Object[] params) {
      return new CSSParseException(this.formatMessage(key, params), this.documentURI, this.scanner.getLine(), this.scanner.getColumn());
   }

   public void parseStyleDeclaration(String source) throws CSSException, IOException {
      this.scanner = new Scanner(source);
      this.parseStyleDeclarationInternal();
   }

   public void parseRule(String source) throws CSSException, IOException {
      this.scanner = new Scanner(source);
      this.parseRuleInternal();
   }

   public SelectorList parseSelectors(String source) throws CSSException, IOException {
      this.scanner = new Scanner(source);
      return this.parseSelectorsInternal();
   }

   public LexicalUnit parsePropertyValue(String source) throws CSSException, IOException {
      this.scanner = new Scanner(source);
      return this.parsePropertyValueInternal();
   }

   public boolean parsePriority(String source) throws CSSException, IOException {
      this.scanner = new Scanner(source);
      return this.parsePriorityInternal();
   }

   public SACMediaList parseMedia(String mediaText) throws CSSException, IOException {
      CSSSACMediaList result = new CSSSACMediaList();
      if (!"all".equalsIgnoreCase(mediaText)) {
         StringTokenizer st = new StringTokenizer(mediaText, " ,");

         while(st.hasMoreTokens()) {
            result.append(st.nextToken());
         }
      }

      return result;
   }
}
