package org.apache.batik.css.engine;

import org.apache.batik.util.ParsedURL;

public class ImportRule extends MediaRule {
   public static final short TYPE = 2;
   protected ParsedURL uri;

   public short getType() {
      return 2;
   }

   public void setURI(ParsedURL u) {
      this.uri = u;
   }

   public ParsedURL getURI() {
      return this.uri;
   }

   public String toString(CSSEngine eng) {
      StringBuffer sb = new StringBuffer();
      sb.append("@import \"");
      sb.append(this.uri);
      sb.append("\"");
      if (this.mediaList != null) {
         for(int i = 0; i < this.mediaList.getLength(); ++i) {
            sb.append(' ');
            sb.append(this.mediaList.item(i));
         }
      }

      sb.append(";\n");
      return sb.toString();
   }
}
