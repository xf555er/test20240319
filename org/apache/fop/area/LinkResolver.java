package org.apache.fop.area;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LinkResolver implements Resolvable, Serializable {
   private static final long serialVersionUID = -7102134165192960718L;
   private boolean resolved;
   private String idRef;
   private Area area;
   private transient List dependents;

   public LinkResolver() {
      this((String)null, (Area)null);
   }

   public LinkResolver(String id, Area a) {
      this.idRef = id;
      this.area = a;
   }

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
   }

   public boolean isResolved() {
      return this.resolved;
   }

   public String[] getIDRefs() {
      return new String[]{this.idRef};
   }

   public void resolveIDRef(String id, List pages) {
      this.resolveIDRef(id, (PageViewport)pages.get(0));
   }

   public void resolveIDRef(String id, PageViewport pv) {
      if (this.idRef.equals(id) && pv != null) {
         this.resolved = true;
         if (this.area != null) {
            Trait.InternalLink iLink = new Trait.InternalLink(pv.getKey(), this.idRef);
            this.area.addTrait(Trait.INTERNAL_LINK, iLink);
            this.area = null;
         }

         this.resolveDependents(id, pv);
      }

   }

   public void addDependent(Resolvable dependent) {
      if (this.dependents == null) {
         this.dependents = new ArrayList();
      }

      this.dependents.add(dependent);
   }

   private void resolveDependents(String id, PageViewport pv) {
      if (this.dependents != null) {
         List pages = new ArrayList();
         pages.add(pv);
         Iterator var4 = this.dependents.iterator();

         while(var4.hasNext()) {
            Resolvable r = (Resolvable)var4.next();
            r.resolveIDRef(id, pages);
         }
      }

   }
}
