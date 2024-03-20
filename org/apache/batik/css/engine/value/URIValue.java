package org.apache.batik.css.engine.value;

public class URIValue extends StringValue {
   String cssText;

   public URIValue(String cssText, String uri) {
      super((short)20, uri);
      this.cssText = cssText;
   }

   public String getCssText() {
      return "url(" + this.cssText + ')';
   }
}
