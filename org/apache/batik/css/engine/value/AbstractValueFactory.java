package org.apache.batik.css.engine.value;

import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMException;

public abstract class AbstractValueFactory {
   public abstract String getPropertyName();

   protected static String resolveURI(ParsedURL base, String value) {
      return (new ParsedURL(base, value)).toString();
   }

   protected DOMException createInvalidIdentifierDOMException(String ident) {
      Object[] p = new Object[]{this.getPropertyName(), ident};
      String s = Messages.formatMessage("invalid.identifier", p);
      return new DOMException((short)12, s);
   }

   protected DOMException createInvalidLexicalUnitDOMException(short type) {
      Object[] p = new Object[]{this.getPropertyName(), Integer.valueOf(type)};
      String s = Messages.formatMessage("invalid.lexical.unit", p);
      return new DOMException((short)9, s);
   }

   protected DOMException createInvalidFloatTypeDOMException(short t) {
      Object[] p = new Object[]{this.getPropertyName(), Integer.valueOf(t)};
      String s = Messages.formatMessage("invalid.float.type", p);
      return new DOMException((short)15, s);
   }

   protected DOMException createInvalidFloatValueDOMException(float f) {
      Object[] p = new Object[]{this.getPropertyName(), f};
      String s = Messages.formatMessage("invalid.float.value", p);
      return new DOMException((short)15, s);
   }

   protected DOMException createInvalidStringTypeDOMException(short t) {
      Object[] p = new Object[]{this.getPropertyName(), Integer.valueOf(t)};
      String s = Messages.formatMessage("invalid.string.type", p);
      return new DOMException((short)15, s);
   }

   protected DOMException createMalformedLexicalUnitDOMException() {
      Object[] p = new Object[]{this.getPropertyName()};
      String s = Messages.formatMessage("malformed.lexical.unit", p);
      return new DOMException((short)15, s);
   }

   protected DOMException createDOMException() {
      Object[] p = new Object[]{this.getPropertyName()};
      String s = Messages.formatMessage("invalid.access", p);
      return new DOMException((short)9, s);
   }
}
