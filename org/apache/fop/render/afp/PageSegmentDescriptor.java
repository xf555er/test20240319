package org.apache.fop.render.afp;

class PageSegmentDescriptor {
   private String name;
   private String uri;

   public PageSegmentDescriptor(String name, String uri) {
      this.name = name;
      this.uri = uri;
   }

   public String getName() {
      return this.name;
   }

   public String getURI() {
      return this.uri;
   }
}
