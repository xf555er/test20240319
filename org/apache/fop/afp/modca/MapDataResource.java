package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.afp.modca.triplets.Triplet;
import org.apache.fop.afp.util.BinaryUtils;

public class MapDataResource extends AbstractTripletStructuredObject {
   private List tripletsList = new ArrayList();

   public void finishElement() {
      this.tripletsList.add(this.triplets);
      this.triplets = new ArrayList();
   }

   protected int getTripletDataLength() {
      int dataLength = 0;

      List l;
      for(Iterator var2 = this.tripletsList.iterator(); var2.hasNext(); dataLength += this.getTripletDataLength(l) + 2) {
         l = (List)var2.next();
      }

      return dataLength;
   }

   private int getTripletDataLength(List l) {
      int dataLength = 0;

      Triplet triplet;
      for(Iterator var3 = l.iterator(); var3.hasNext(); dataLength += triplet.getDataLength()) {
         triplet = (Triplet)var3.next();
      }

      return dataLength;
   }

   public void writeToStream(OutputStream os) throws IOException {
      super.writeStart(os);
      byte[] data = new byte[9];
      this.copySF(data, (byte)-85, (byte)-61);
      int tripletDataLen = this.getTripletDataLength();
      byte[] len = BinaryUtils.convert(8 + tripletDataLen, 2);
      data[1] = len[0];
      data[2] = len[1];
      os.write(data);
      Iterator var5 = this.tripletsList.iterator();

      while(var5.hasNext()) {
         List l = (List)var5.next();
         len = BinaryUtils.convert(2 + this.getTripletDataLength(l), 2);
         os.write(len);
         this.writeObjects(l, os);
      }

   }
}
