package org.apache.batik.css.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.StringTokenizer;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.ConditionFactory;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SelectorList;

public class ExtendedParserWrapper implements ExtendedParser {
   public org.w3c.css.sac.Parser parser;

   public static ExtendedParser wrap(org.w3c.css.sac.Parser p) {
      return (ExtendedParser)(p instanceof ExtendedParser ? (ExtendedParser)p : new ExtendedParserWrapper(p));
   }

   public ExtendedParserWrapper(org.w3c.css.sac.Parser parser) {
      this.parser = parser;
   }

   public String getParserVersion() {
      return this.parser.getParserVersion();
   }

   public void setLocale(Locale locale) throws CSSException {
      this.parser.setLocale(locale);
   }

   public void setDocumentHandler(DocumentHandler handler) {
      this.parser.setDocumentHandler(handler);
   }

   public void setSelectorFactory(SelectorFactory selectorFactory) {
      this.parser.setSelectorFactory(selectorFactory);
   }

   public void setConditionFactory(ConditionFactory conditionFactory) {
      this.parser.setConditionFactory(conditionFactory);
   }

   public void setErrorHandler(ErrorHandler handler) {
      this.parser.setErrorHandler(handler);
   }

   public void parseStyleSheet(InputSource source) throws CSSException, IOException {
      this.parser.parseStyleSheet(source);
   }

   public void parseStyleSheet(String uri) throws CSSException, IOException {
      this.parser.parseStyleSheet(uri);
   }

   public void parseStyleDeclaration(InputSource source) throws CSSException, IOException {
      this.parser.parseStyleDeclaration(source);
   }

   public void parseStyleDeclaration(String source) throws CSSException, IOException {
      this.parser.parseStyleDeclaration(new InputSource(new StringReader(source)));
   }

   public void parseRule(InputSource source) throws CSSException, IOException {
      this.parser.parseRule(source);
   }

   public void parseRule(String source) throws CSSException, IOException {
      this.parser.parseRule(new InputSource(new StringReader(source)));
   }

   public SelectorList parseSelectors(InputSource source) throws CSSException, IOException {
      return this.parser.parseSelectors(source);
   }

   public SelectorList parseSelectors(String source) throws CSSException, IOException {
      return this.parser.parseSelectors(new InputSource(new StringReader(source)));
   }

   public LexicalUnit parsePropertyValue(InputSource source) throws CSSException, IOException {
      return this.parser.parsePropertyValue(source);
   }

   public LexicalUnit parsePropertyValue(String source) throws CSSException, IOException {
      return this.parser.parsePropertyValue(new InputSource(new StringReader(source)));
   }

   public boolean parsePriority(InputSource source) throws CSSException, IOException {
      return this.parser.parsePriority(source);
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

   public boolean parsePriority(String source) throws CSSException, IOException {
      return this.parser.parsePriority(new InputSource(new StringReader(source)));
   }
}
