package org.apache.batik.css.engine;

import org.w3c.css.sac.SACMediaList;

public class MediaRule extends StyleSheet implements Rule {
   public static final short TYPE = 1;
   protected SACMediaList mediaList;

   public short getType() {
      return 1;
   }

   public void setMediaList(SACMediaList ml) {
      this.mediaList = ml;
   }

   public SACMediaList getMediaList() {
      return this.mediaList;
   }

   public String toString(CSSEngine eng) {
      StringBuffer sb = new StringBuffer();
      sb.append("@media");
      int i;
      if (this.mediaList != null) {
         for(i = 0; i < this.mediaList.getLength(); ++i) {
            sb.append(' ');
            sb.append(this.mediaList.item(i));
         }
      }

      sb.append(" {\n");

      for(i = 0; i < this.size; ++i) {
         sb.append(this.rules[i].toString(eng));
      }

      sb.append("}\n");
      return sb.toString();
   }
}
