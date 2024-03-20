package org.apache.batik.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.io.NormalizingReader;
import org.apache.batik.util.io.StreamNormalizingReader;
import org.apache.batik.util.io.StringNormalizingReader;

public abstract class AbstractParser implements Parser {
   public static final String BUNDLE_CLASSNAME = "org.apache.batik.parser.resources.Messages";
   protected ErrorHandler errorHandler = new DefaultErrorHandler();
   protected LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.parser.resources.Messages", AbstractParser.class.getClassLoader());
   protected NormalizingReader reader;
   protected int current;

   public int getCurrent() {
      return this.current;
   }

   public void setLocale(Locale l) {
      this.localizableSupport.setLocale(l);
   }

   public Locale getLocale() {
      return this.localizableSupport.getLocale();
   }

   public String formatMessage(String key, Object[] args) throws MissingResourceException {
      return this.localizableSupport.formatMessage(key, args);
   }

   public void setErrorHandler(ErrorHandler handler) {
      this.errorHandler = handler;
   }

   public void parse(Reader r) throws ParseException {
      try {
         this.reader = new StreamNormalizingReader(r);
         this.doParse();
      } catch (IOException var3) {
         this.errorHandler.error(new ParseException(this.createErrorMessage("io.exception", (Object[])null), var3));
      }

   }

   public void parse(InputStream is, String enc) throws ParseException {
      try {
         this.reader = new StreamNormalizingReader(is, enc);
         this.doParse();
      } catch (IOException var4) {
         this.errorHandler.error(new ParseException(this.createErrorMessage("io.exception", (Object[])null), var4));
      }

   }

   public void parse(String s) throws ParseException {
      try {
         this.reader = new StringNormalizingReader(s);
         this.doParse();
      } catch (IOException var3) {
         this.errorHandler.error(new ParseException(this.createErrorMessage("io.exception", (Object[])null), var3));
      }

   }

   protected abstract void doParse() throws ParseException, IOException;

   protected void reportError(String key, Object[] args) throws ParseException {
      this.errorHandler.error(new ParseException(this.createErrorMessage(key, args), this.reader.getLine(), this.reader.getColumn()));
   }

   protected void reportCharacterExpectedError(char expectedChar, int currentChar) {
      this.reportError("character.expected", new Object[]{expectedChar, currentChar});
   }

   protected void reportUnexpectedCharacterError(int currentChar) {
      this.reportError("character.unexpected", new Object[]{currentChar});
   }

   protected String createErrorMessage(String key, Object[] args) {
      try {
         return this.formatMessage(key, args);
      } catch (MissingResourceException var4) {
         return key;
      }
   }

   protected String getBundleClassName() {
      return "org.apache.batik.parser.resources.Messages";
   }

   protected void skipSpaces() throws IOException {
      while(true) {
         switch (this.current) {
            case 9:
            case 10:
            case 13:
            case 32:
               this.current = this.reader.read();
               break;
            default:
               return;
         }
      }
   }

   protected void skipCommaSpaces() throws IOException {
      while(true) {
         switch (this.current) {
            case 9:
            case 10:
            case 13:
            case 32:
               this.current = this.reader.read();
               break;
            default:
               if (this.current == 44) {
                  while(true) {
                     switch (this.current = this.reader.read()) {
                        case 9:
                        case 10:
                        case 13:
                        case 32:
                           break;
                        default:
                           return;
                     }
                  }
               }

               return;
         }
      }
   }
}
