package org.apache.fop.area.inline;

import java.util.Iterator;
import org.apache.fop.area.Area;
import org.apache.fop.area.LinkResolver;

public class BasicLinkArea extends InlineParent {
   private static final long serialVersionUID = 5183753430412208151L;
   private LinkResolver resolver;

   public void setParentArea(Area parentArea) {
      super.setParentArea(parentArea);
      this.setBlockProgressionOffset(this.getBlockProgressionOffset() + this.minChildOffset);
      Iterator var2 = this.inlines.iterator();

      while(var2.hasNext()) {
         InlineArea inline = (InlineArea)var2.next();
         inline.setBlockProgressionOffset(inline.getBlockProgressionOffset() - this.minChildOffset);
      }

      this.setBPD(this.getVirtualBPD());
   }

   public void setResolver(LinkResolver resolver) {
      assert resolver == null || this.resolver == null;

      this.resolver = resolver;
   }

   public LinkResolver getResolver() {
      return this.resolver;
   }
}
