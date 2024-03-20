package org.apache.fop.afp.fonts;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

public abstract class CharactersetEncoder {
   private final CharsetEncoder encoder;

   private CharactersetEncoder(String encoding) {
      this.encoder = Charset.forName(encoding).newEncoder();
      this.encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
   }

   final boolean canEncode(char c) {
      return this.encoder.canEncode(c);
   }

   final EncodedChars encode(CharSequence chars) throws CharacterCodingException {
      ByteBuffer bb;
      synchronized(this.encoder) {
         bb = this.encoder.encode(CharBuffer.wrap(chars));
      }

      if (bb.hasArray()) {
         return this.getEncodedChars(bb.array(), bb.limit());
      } else {
         bb.rewind();
         byte[] bytes = new byte[bb.remaining()];
         bb.get(bytes);
         return this.getEncodedChars(bytes, bytes.length);
      }
   }

   abstract EncodedChars getEncodedChars(byte[] var1, int var2);

   public static EncodedChars encodeSBCS(CharSequence chars, String encoding) throws CharacterCodingException {
      CharactersetEncoder encoder = CharacterSetType.SINGLE_BYTE.getEncoder(encoding);
      return encoder.encode(chars);
   }

   // $FF: synthetic method
   CharactersetEncoder(String x0, Object x1) {
      this(x0);
   }

   public static class EncodedChars {
      private final byte[] bytes;
      private final int offset;
      private final int length;
      private final boolean isDBCS;

      private EncodedChars(byte[] bytes, int offset, int length, boolean isDBCS) {
         if (offset >= 0 && length >= 0 && offset + length <= bytes.length) {
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
            this.isDBCS = isDBCS;
         } else {
            throw new IllegalArgumentException();
         }
      }

      private EncodedChars(byte[] bytes, boolean isDBCS) {
         this(bytes, 0, bytes.length, isDBCS);
      }

      public void writeTo(OutputStream out, int offset, int length) throws IOException {
         if (offset >= 0 && length >= 0 && offset + length <= this.bytes.length) {
            out.write(this.bytes, this.offset + offset, length);
         } else {
            throw new IllegalArgumentException();
         }
      }

      public int getLength() {
         return this.length;
      }

      public boolean isDBCS() {
         return this.isDBCS;
      }

      public byte[] getBytes() {
         byte[] copy = new byte[this.bytes.length];
         System.arraycopy(this.bytes, 0, copy, 0, this.bytes.length);
         return copy;
      }

      // $FF: synthetic method
      EncodedChars(byte[] x0, int x1, int x2, boolean x3, Object x4) {
         this(x0, x1, x2, x3);
      }

      // $FF: synthetic method
      EncodedChars(byte[] x0, boolean x1, Object x2) {
         this(x0, x1);
      }
   }

   static final class DefaultEncoder extends CharactersetEncoder {
      private final boolean isDBCS;

      DefaultEncoder(String encoding, boolean isDBCS) {
         super(encoding, null);
         this.isDBCS = isDBCS;
      }

      EncodedChars getEncodedChars(byte[] byteArray, int length) {
         return new EncodedChars(byteArray, this.isDBCS);
      }
   }

   static final class EbcdicDoubleByteLineDataEncoder extends CharactersetEncoder {
      EbcdicDoubleByteLineDataEncoder(String encoding) {
         super(encoding, null);
      }

      EncodedChars getEncodedChars(byte[] byteArray, int length) {
         return byteArray[0] == 14 && byteArray[length - 1] == 15 ? new EncodedChars(byteArray, 1, length - 2, true) : new EncodedChars(byteArray, true);
      }
   }
}
