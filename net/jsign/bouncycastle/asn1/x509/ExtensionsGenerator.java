package net.jsign.bouncycastle.asn1.x509;

import java.util.Hashtable;
import java.util.Vector;

public class ExtensionsGenerator {
   private Hashtable extensions = new Hashtable();
   private Vector extOrdering = new Vector();

   public boolean isEmpty() {
      return this.extOrdering.isEmpty();
   }

   public Extensions generate() {
      Extension[] var1 = new Extension[this.extOrdering.size()];

      for(int var2 = 0; var2 != this.extOrdering.size(); ++var2) {
         var1[var2] = (Extension)this.extensions.get(this.extOrdering.elementAt(var2));
      }

      return new Extensions(var1);
   }
}
