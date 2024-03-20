package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.triplets.AttributeQualifierTriplet;
import org.apache.fop.afp.modca.triplets.AttributeValueTriplet;
import org.apache.fop.afp.modca.triplets.EncodingTriplet;
import org.apache.fop.afp.util.BinaryUtils;

public class TagLogicalElement extends AbstractTripletStructuredObject {
   private State state;

   public TagLogicalElement(State state) {
      this.state = state;
   }

   private void setAttributeValue(String value) {
      if (this.state.encoding != -1) {
         this.addTriplet(new AttributeValueTriplet(value, this.state.encoding));
      } else {
         this.addTriplet(new AttributeValueTriplet(value));
      }

   }

   private void setEncoding(int encoding) {
      if (encoding != -1) {
         this.addTriplet(new EncodingTriplet(encoding));
      }

   }

   public void setAttributeQualifier(int seqNumber, int levNumber) {
      this.addTriplet(new AttributeQualifierTriplet(seqNumber, levNumber));
   }

   public void writeToStream(OutputStream os) throws IOException {
      this.setFullyQualifiedName((byte)11, (byte)0, this.state.key);
      this.setAttributeValue(this.state.value);
      this.setEncoding(this.state.encoding);
      byte[] data = new byte[SF_HEADER_LENGTH];
      this.copySF(data, (byte)-96, (byte)-112);
      int tripletDataLength = this.getTripletDataLength();
      byte[] l = BinaryUtils.convert(data.length + tripletDataLength - 1, 2);
      data[1] = l[0];
      data[2] = l[1];
      os.write(data);
      this.writeTriplets(os);
   }

   public static class State {
      public static final int ENCODING_NONE = -1;
      private String key;
      private String value;
      private int encoding = -1;

      public State(String key, String value) {
         this.key = key;
         this.value = value;
      }

      public State(String key, String value, int encoding) {
         this.key = key;
         this.value = value;
         this.encoding = encoding;
      }
   }
}
