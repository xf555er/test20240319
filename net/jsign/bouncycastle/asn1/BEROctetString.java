package net.jsign.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class BEROctetString extends ASN1OctetString {
   private final int chunkSize;
   private final ASN1OctetString[] octs;

   private static byte[] toBytes(ASN1OctetString[] var0) {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();

      for(int var2 = 0; var2 != var0.length; ++var2) {
         try {
            var1.write(var0[var2].getOctets());
         } catch (IOException var4) {
            throw new IllegalArgumentException("exception converting octets " + var4.toString());
         }
      }

      return var1.toByteArray();
   }

   public BEROctetString(byte[] var1) {
      this((byte[])var1, 1000);
   }

   public BEROctetString(ASN1OctetString[] var1) {
      this((ASN1OctetString[])var1, 1000);
   }

   public BEROctetString(byte[] var1, int var2) {
      this(var1, (ASN1OctetString[])null, var2);
   }

   public BEROctetString(ASN1OctetString[] var1, int var2) {
      this(toBytes(var1), var1, var2);
   }

   private BEROctetString(byte[] var1, ASN1OctetString[] var2, int var3) {
      super(var1);
      this.octs = var2;
      this.chunkSize = var3;
   }

   public Enumeration getObjects() {
      return this.octs == null ? new Enumeration() {
         int pos = 0;

         public boolean hasMoreElements() {
            return this.pos < BEROctetString.this.string.length;
         }

         public Object nextElement() {
            if (this.pos < BEROctetString.this.string.length) {
               int var1 = Math.min(BEROctetString.this.string.length - this.pos, BEROctetString.this.chunkSize);
               byte[] var2 = new byte[var1];
               System.arraycopy(BEROctetString.this.string, this.pos, var2, 0, var1);
               this.pos += var1;
               return new DEROctetString(var2);
            } else {
               throw new NoSuchElementException();
            }
         }
      } : new Enumeration() {
         int counter = 0;

         public boolean hasMoreElements() {
            return this.counter < BEROctetString.this.octs.length;
         }

         public Object nextElement() {
            if (this.counter < BEROctetString.this.octs.length) {
               return BEROctetString.this.octs[this.counter++];
            } else {
               throw new NoSuchElementException();
            }
         }
      };
   }

   boolean isConstructed() {
      return true;
   }

   int encodedLength() throws IOException {
      int var1 = 0;

      for(Enumeration var2 = this.getObjects(); var2.hasMoreElements(); var1 += ((ASN1Encodable)var2.nextElement()).toASN1Primitive().encodedLength()) {
      }

      return 2 + var1 + 2;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncodedIndef(var2, 36, (Enumeration)this.getObjects());
   }

   static BEROctetString fromSequence(ASN1Sequence var0) {
      int var1 = var0.size();
      ASN1OctetString[] var2 = new ASN1OctetString[var1];

      for(int var3 = 0; var3 < var1; ++var3) {
         var2[var3] = ASN1OctetString.getInstance(var0.getObjectAt(var3));
      }

      return new BEROctetString(var2);
   }
}
