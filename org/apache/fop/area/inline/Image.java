package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

public class Image extends Area {
   private static final long serialVersionUID = 4800834714349695386L;
   private String url;

   public Image(String url) {
      this.url = url;
   }

   public String getURL() {
      return this.url;
   }
}
