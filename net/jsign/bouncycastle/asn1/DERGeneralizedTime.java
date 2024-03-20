package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import net.jsign.bouncycastle.util.Strings;

public class DERGeneralizedTime extends ASN1GeneralizedTime {
   public DERGeneralizedTime(byte[] var1) {
      super(var1);
   }

   public DERGeneralizedTime(String var1) {
      super(var1);
   }

   private byte[] getDERTime() {
      if (this.time[this.time.length - 1] != 90) {
         return this.time;
      } else {
         byte[] var3;
         if (!this.hasMinutes()) {
            var3 = new byte[this.time.length + 4];
            System.arraycopy(this.time, 0, var3, 0, this.time.length - 1);
            System.arraycopy(Strings.toByteArray("0000Z"), 0, var3, this.time.length - 1, 5);
            return var3;
         } else if (!this.hasSeconds()) {
            var3 = new byte[this.time.length + 2];
            System.arraycopy(this.time, 0, var3, 0, this.time.length - 1);
            System.arraycopy(Strings.toByteArray("00Z"), 0, var3, this.time.length - 1, 3);
            return var3;
         } else if (!this.hasFractionalSeconds()) {
            return this.time;
         } else {
            int var1;
            for(var1 = this.time.length - 2; var1 > 0 && this.time[var1] == 48; --var1) {
            }

            byte[] var2;
            if (this.time[var1] == 46) {
               var2 = new byte[var1 + 1];
               System.arraycopy(this.time, 0, var2, 0, var1);
               var2[var1] = 90;
               return var2;
            } else {
               var2 = new byte[var1 + 2];
               System.arraycopy(this.time, 0, var2, 0, var1 + 1);
               var2[var1 + 1] = 90;
               return var2;
            }
         }
      }
   }

   int encodedLength() {
      int var1 = this.getDERTime().length;
      return 1 + StreamUtil.calculateBodyLength(var1) + var1;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 24, this.getDERTime());
   }

   ASN1Primitive toDERObject() {
      return this;
   }

   ASN1Primitive toDLObject() {
      return this;
   }
}
