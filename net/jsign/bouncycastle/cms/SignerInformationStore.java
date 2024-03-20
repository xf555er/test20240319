package net.jsign.bouncycastle.cms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.jsign.bouncycastle.util.Iterable;

public class SignerInformationStore implements Iterable {
   private List all = new ArrayList();
   private Map table = new HashMap();

   public SignerInformationStore(SignerInformation var1) {
      this.all = new ArrayList(1);
      this.all.add(var1);
      SignerId var2 = var1.getSID();
      this.table.put(var2, this.all);
   }

   public SignerInformationStore(Collection var1) {
      SignerInformation var3;
      ArrayList var5;
      for(Iterator var2 = var1.iterator(); var2.hasNext(); var5.add(var3)) {
         var3 = (SignerInformation)var2.next();
         SignerId var4 = var3.getSID();
         var5 = (ArrayList)this.table.get(var4);
         if (var5 == null) {
            var5 = new ArrayList(1);
            this.table.put(var4, var5);
         }
      }

      this.all = new ArrayList(var1);
   }

   public Collection getSigners() {
      return new ArrayList(this.all);
   }

   public Iterator iterator() {
      return this.getSigners().iterator();
   }
}
