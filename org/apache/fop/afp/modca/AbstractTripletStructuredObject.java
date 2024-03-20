package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.afp.modca.triplets.AbstractTriplet;
import org.apache.fop.afp.modca.triplets.CommentTriplet;
import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.afp.modca.triplets.ObjectClassificationTriplet;
import org.apache.fop.afp.modca.triplets.Triplet;

public abstract class AbstractTripletStructuredObject extends AbstractStructuredObject {
   protected List triplets = new ArrayList();

   protected int getTripletDataLength() {
      int dataLength = 0;

      Triplet triplet;
      for(Iterator var2 = this.triplets.iterator(); var2.hasNext(); dataLength += triplet.getDataLength()) {
         triplet = (Triplet)var2.next();
      }

      return dataLength;
   }

   public boolean hasTriplets() {
      return this.triplets.size() > 0;
   }

   protected void writeTriplets(OutputStream os) throws IOException {
      if (this.hasTriplets()) {
         this.writeObjects(this.triplets, os);
         this.triplets = null;
      }

   }

   private AbstractTriplet getTriplet(byte tripletId) {
      Iterator var2 = this.triplets.iterator();

      AbstractTriplet trip;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         trip = (AbstractTriplet)var2.next();
      } while(trip.getId() != tripletId);

      return trip;
   }

   public boolean hasTriplet(byte tripletId) {
      return this.getTriplet(tripletId) != null;
   }

   public void addTriplet(AbstractTriplet triplet) {
      this.triplets.add(triplet);
   }

   public void addTriplets(Collection tripletCollection) {
      if (tripletCollection != null) {
         this.triplets.addAll(tripletCollection);
      }

   }

   protected List getTriplets() {
      return this.triplets;
   }

   public void setFullyQualifiedName(byte fqnType, byte fqnFormat, String fqName) {
      this.addTriplet(new FullyQualifiedNameTriplet(fqnType, fqnFormat, fqName, false));
   }

   public void setFullyQualifiedName(byte fqnType, byte fqnFormat, String fqName, boolean utf16be) {
      this.addTriplet(new FullyQualifiedNameTriplet(fqnType, fqnFormat, fqName, utf16be));
   }

   public String getFullyQualifiedName() {
      FullyQualifiedNameTriplet fqNameTriplet = (FullyQualifiedNameTriplet)this.getTriplet((byte)2);
      if (fqNameTriplet != null) {
         return fqNameTriplet.getFullyQualifiedName();
      } else {
         LOG.warn(this + " has no fully qualified name");
         return null;
      }
   }

   public void setObjectClassification(byte objectClass, Registry.ObjectType objectType, boolean dataInContainer, boolean containerHasOEG, boolean dataInOCD) {
      this.addTriplet(new ObjectClassificationTriplet(objectClass, objectType, dataInContainer, containerHasOEG, dataInOCD));
   }

   public void setComment(String commentString) {
      this.addTriplet(new CommentTriplet((byte)101, commentString));
   }
}
