package net.jsign.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.jsign.bouncycastle.util.io.Streams;

public class ASN1InputStream extends FilterInputStream {
   private final int limit;
   private final boolean lazyEvaluate;
   private final byte[][] tmpBuffers;

   public ASN1InputStream(InputStream var1) {
      this(var1, StreamUtil.findLimit(var1));
   }

   public ASN1InputStream(byte[] var1) {
      this(new ByteArrayInputStream(var1), var1.length);
   }

   public ASN1InputStream(byte[] var1, boolean var2) {
      this(new ByteArrayInputStream(var1), var1.length, var2);
   }

   public ASN1InputStream(InputStream var1, int var2) {
      this(var1, var2, false);
   }

   public ASN1InputStream(InputStream var1, boolean var2) {
      this(var1, StreamUtil.findLimit(var1), var2);
   }

   public ASN1InputStream(InputStream var1, int var2, boolean var3) {
      super(var1);
      this.limit = var2;
      this.lazyEvaluate = var3;
      this.tmpBuffers = new byte[11][];
   }

   int getLimit() {
      return this.limit;
   }

   protected int readLength() throws IOException {
      return readLength(this, this.limit, false);
   }

   protected ASN1Primitive buildObject(int var1, int var2, int var3) throws IOException {
      boolean var4 = (var1 & 32) != 0;
      DefiniteLengthInputStream var5 = new DefiniteLengthInputStream(this, var3, this.limit);
      if ((var1 & 192) == 192) {
         return new DLPrivate(var4, var2, var5.toByteArray());
      } else if ((var1 & 64) != 0) {
         return new DLApplicationSpecific(var4, var2, var5.toByteArray());
      } else if ((var1 & 128) != 0) {
         return (new ASN1StreamParser(var5)).readTaggedObject(var4, var2);
      } else if (var4) {
         switch (var2) {
            case 4:
               ASN1EncodableVector var6 = this.readVector(var5);
               ASN1OctetString[] var7 = new ASN1OctetString[var6.size()];

               for(int var8 = 0; var8 != var7.length; ++var8) {
                  ASN1Encodable var9 = var6.get(var8);
                  if (!(var9 instanceof ASN1OctetString)) {
                     throw new ASN1Exception("unknown object encountered in constructed OCTET STRING: " + var9.getClass());
                  }

                  var7[var8] = (ASN1OctetString)var9;
               }

               return new BEROctetString(var7);
            case 8:
               return new DLExternal(this.readVector(var5));
            case 16:
               if (this.lazyEvaluate) {
                  return new LazyEncodedSequence(var5.toByteArray());
               }

               return DLFactory.createSequence(this.readVector(var5));
            case 17:
               return DLFactory.createSet(this.readVector(var5));
            default:
               throw new IOException("unknown tag " + var2 + " encountered");
         }
      } else {
         return createPrimitiveDERObject(var2, var5, this.tmpBuffers);
      }
   }

   ASN1EncodableVector readVector(DefiniteLengthInputStream var1) throws IOException {
      if (var1.getRemaining() < 1) {
         return new ASN1EncodableVector(0);
      } else {
         ASN1InputStream var2 = new ASN1InputStream(var1);
         ASN1EncodableVector var3 = new ASN1EncodableVector();

         ASN1Primitive var4;
         while((var4 = var2.readObject()) != null) {
            var3.add(var4);
         }

         return var3;
      }
   }

   public ASN1Primitive readObject() throws IOException {
      int var1 = this.read();
      if (var1 <= 0) {
         if (var1 == 0) {
            throw new IOException("unexpected end-of-contents marker");
         } else {
            return null;
         }
      } else {
         int var2 = readTagNumber(this, var1);
         boolean var3 = (var1 & 32) != 0;
         int var4 = this.readLength();
         if (var4 < 0) {
            if (!var3) {
               throw new IOException("indefinite-length primitive encoding encountered");
            } else {
               IndefiniteLengthInputStream var5 = new IndefiniteLengthInputStream(this, this.limit);
               ASN1StreamParser var6 = new ASN1StreamParser(var5, this.limit);
               if ((var1 & 192) == 192) {
                  return (new BERPrivateParser(var2, var6)).getLoadedObject();
               } else if ((var1 & 64) != 0) {
                  return (new BERApplicationSpecificParser(var2, var6)).getLoadedObject();
               } else if ((var1 & 128) != 0) {
                  return (new BERTaggedObjectParser(true, var2, var6)).getLoadedObject();
               } else {
                  switch (var2) {
                     case 4:
                        return (new BEROctetStringParser(var6)).getLoadedObject();
                     case 8:
                        return (new DERExternalParser(var6)).getLoadedObject();
                     case 16:
                        return (new BERSequenceParser(var6)).getLoadedObject();
                     case 17:
                        return (new BERSetParser(var6)).getLoadedObject();
                     default:
                        throw new IOException("unknown BER object encountered");
                  }
               }
            }
         } else {
            try {
               return this.buildObject(var1, var2, var4);
            } catch (IllegalArgumentException var7) {
               throw new ASN1Exception("corrupted stream detected", var7);
            }
         }
      }
   }

   static int readTagNumber(InputStream var0, int var1) throws IOException {
      int var2 = var1 & 31;
      if (var2 == 31) {
         var2 = 0;
         int var3 = var0.read();
         if ((var3 & 127) == 0) {
            throw new IOException("corrupted stream - invalid high tag number found");
         }

         while(var3 >= 0 && (var3 & 128) != 0) {
            var2 |= var3 & 127;
            var2 <<= 7;
            var3 = var0.read();
         }

         if (var3 < 0) {
            throw new EOFException("EOF found inside tag value.");
         }

         var2 |= var3 & 127;
      }

      return var2;
   }

   static int readLength(InputStream var0, int var1, boolean var2) throws IOException {
      int var3 = var0.read();
      if (var3 < 0) {
         throw new EOFException("EOF found when length expected");
      } else if (var3 == 128) {
         return -1;
      } else {
         if (var3 > 127) {
            int var4 = var3 & 127;
            if (var4 > 4) {
               throw new IOException("DER length more than 4 bytes: " + var4);
            }

            var3 = 0;

            for(int var5 = 0; var5 < var4; ++var5) {
               int var6 = var0.read();
               if (var6 < 0) {
                  throw new EOFException("EOF found reading length");
               }

               var3 = (var3 << 8) + var6;
            }

            if (var3 < 0) {
               throw new IOException("corrupted stream - negative length found");
            }

            if (var3 >= var1 && !var2) {
               throw new IOException("corrupted stream - out of bounds length found: " + var3 + " >= " + var1);
            }
         }

         return var3;
      }
   }

   private static byte[] getBuffer(DefiniteLengthInputStream var0, byte[][] var1) throws IOException {
      int var2 = var0.getRemaining();
      if (var2 >= var1.length) {
         return var0.toByteArray();
      } else {
         byte[] var3 = var1[var2];
         if (var3 == null) {
            var3 = var1[var2] = new byte[var2];
         }

         var0.readAllIntoByteArray(var3);
         return var3;
      }
   }

   private static char[] getBMPCharBuffer(DefiniteLengthInputStream var0) throws IOException {
      int var1 = var0.getRemaining();
      if (0 != (var1 & 1)) {
         throw new IOException("malformed BMPString encoding encountered");
      } else {
         char[] var2 = new char[var1 / 2];
         int var3 = 0;

         byte[] var4;
         for(var4 = new byte[8]; var1 >= 8; var1 -= 8) {
            if (Streams.readFully(var0, var4, 0, 8) != 8) {
               throw new EOFException("EOF encountered in middle of BMPString");
            }

            var2[var3] = (char)(var4[0] << 8 | var4[1] & 255);
            var2[var3 + 1] = (char)(var4[2] << 8 | var4[3] & 255);
            var2[var3 + 2] = (char)(var4[4] << 8 | var4[5] & 255);
            var2[var3 + 3] = (char)(var4[6] << 8 | var4[7] & 255);
            var3 += 4;
         }

         if (var1 > 0) {
            if (Streams.readFully(var0, var4, 0, var1) != var1) {
               throw new EOFException("EOF encountered in middle of BMPString");
            }

            int var5 = 0;

            do {
               int var6 = var4[var5++] << 8;
               int var7 = var4[var5++] & 255;
               var2[var3++] = (char)(var6 | var7);
            } while(var5 < var1);
         }

         if (0 == var0.getRemaining() && var2.length == var3) {
            return var2;
         } else {
            throw new IllegalStateException();
         }
      }
   }

   static ASN1Primitive createPrimitiveDERObject(int var0, DefiniteLengthInputStream var1, byte[][] var2) throws IOException {
      switch (var0) {
         case 1:
            return ASN1Boolean.fromOctetString(getBuffer(var1, var2));
         case 2:
            return new ASN1Integer(var1.toByteArray(), false);
         case 3:
            return ASN1BitString.fromInputStream(var1.getRemaining(), var1);
         case 4:
            return new DEROctetString(var1.toByteArray());
         case 5:
            return DERNull.INSTANCE;
         case 6:
            return ASN1ObjectIdentifier.fromOctetString(getBuffer(var1, var2));
         case 7:
         case 8:
         case 9:
         case 11:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 29:
         default:
            throw new IOException("unknown tag " + var0 + " encountered");
         case 10:
            return ASN1Enumerated.fromOctetString(getBuffer(var1, var2));
         case 12:
            return new DERUTF8String(var1.toByteArray());
         case 18:
            return new DERNumericString(var1.toByteArray());
         case 19:
            return new DERPrintableString(var1.toByteArray());
         case 20:
            return new DERT61String(var1.toByteArray());
         case 21:
            return new DERVideotexString(var1.toByteArray());
         case 22:
            return new DERIA5String(var1.toByteArray());
         case 23:
            return new ASN1UTCTime(var1.toByteArray());
         case 24:
            return new ASN1GeneralizedTime(var1.toByteArray());
         case 25:
            return new DERGraphicString(var1.toByteArray());
         case 26:
            return new DERVisibleString(var1.toByteArray());
         case 27:
            return new DERGeneralString(var1.toByteArray());
         case 28:
            return new DERUniversalString(var1.toByteArray());
         case 30:
            return new DERBMPString(getBMPCharBuffer(var1));
      }
   }
}
