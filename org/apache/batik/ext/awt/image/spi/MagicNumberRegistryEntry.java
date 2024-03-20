package org.apache.batik.ext.awt.image.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

public abstract class MagicNumberRegistryEntry extends AbstractRegistryEntry implements StreamRegistryEntry {
   public static final float PRIORITY = 1000.0F;
   MagicNumber[] magicNumbers;

   public MagicNumberRegistryEntry(String name, float priority, String ext, String mimeType, int offset, byte[] magicNumber) {
      super(name, priority, ext, mimeType);
      this.magicNumbers = new MagicNumber[1];
      this.magicNumbers[0] = new MagicNumber(offset, magicNumber);
   }

   public MagicNumberRegistryEntry(String name, String ext, String mimeType, int offset, byte[] magicNumber) {
      this(name, 1000.0F, ext, mimeType, offset, magicNumber);
   }

   public MagicNumberRegistryEntry(String name, float priority, String ext, String mimeType, MagicNumber[] magicNumbers) {
      super(name, priority, ext, mimeType);
      this.magicNumbers = magicNumbers;
   }

   public MagicNumberRegistryEntry(String name, String ext, String mimeType, MagicNumber[] magicNumbers) {
      this(name, 1000.0F, ext, mimeType, magicNumbers);
   }

   public MagicNumberRegistryEntry(String name, float priority, String[] exts, String[] mimeTypes, int offset, byte[] magicNumber) {
      super(name, priority, exts, mimeTypes);
      this.magicNumbers = new MagicNumber[1];
      this.magicNumbers[0] = new MagicNumber(offset, magicNumber);
   }

   public MagicNumberRegistryEntry(String name, String[] exts, String[] mimeTypes, int offset, byte[] magicNumbers) {
      this(name, 1000.0F, exts, mimeTypes, offset, magicNumbers);
   }

   public MagicNumberRegistryEntry(String name, float priority, String[] exts, String[] mimeTypes, MagicNumber[] magicNumbers) {
      super(name, priority, exts, mimeTypes);
      this.magicNumbers = magicNumbers;
   }

   public MagicNumberRegistryEntry(String name, String[] exts, String[] mimeTypes, MagicNumber[] magicNumbers) {
      this(name, 1000.0F, exts, mimeTypes, magicNumbers);
   }

   public MagicNumberRegistryEntry(String name, String[] exts, String[] mimeTypes, MagicNumber[] magicNumbers, float priority) {
      super(name, priority, exts, mimeTypes);
      this.magicNumbers = magicNumbers;
   }

   public int getReadlimit() {
      int maxbuf = 0;
      MagicNumber[] var2 = this.magicNumbers;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         MagicNumber magicNumber = var2[var4];
         int req = magicNumber.getReadlimit();
         if (req > maxbuf) {
            maxbuf = req;
         }
      }

      return maxbuf;
   }

   public boolean isCompatibleStream(InputStream is) throws StreamCorruptedException {
      MagicNumber[] var2 = this.magicNumbers;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         MagicNumber magicNumber = var2[var4];
         if (magicNumber.isMatch(is)) {
            return true;
         }
      }

      return false;
   }

   public static class MagicNumber {
      int offset;
      byte[] magicNumber;
      byte[] buffer;

      public MagicNumber(int offset, byte[] magicNumber) {
         this.offset = offset;
         this.magicNumber = (byte[])magicNumber.clone();
         this.buffer = new byte[magicNumber.length];
      }

      int getReadlimit() {
         return this.offset + this.magicNumber.length;
      }

      boolean isMatch(InputStream is) throws StreamCorruptedException {
         int idx = 0;
         is.mark(this.getReadlimit());

         boolean var4;
         try {
            int i;
            while(idx < this.offset) {
               i = (int)is.skip((long)(this.offset - idx));
               if (i == -1) {
                  var4 = false;
                  return var4;
               }

               idx += i;
            }

            for(idx = 0; idx < this.buffer.length; idx += i) {
               i = is.read(this.buffer, idx, this.buffer.length - idx);
               if (i == -1) {
                  var4 = false;
                  return var4;
               }
            }

            for(i = 0; i < this.magicNumber.length; ++i) {
               if (this.magicNumber[i] != this.buffer[i]) {
                  var4 = false;
                  return var4;
               }
            }

            return true;
         } catch (IOException var17) {
            var4 = false;
         } finally {
            try {
               is.reset();
            } catch (IOException var16) {
               throw new StreamCorruptedException(var16.getMessage());
            }
         }

         return var4;
      }
   }
}
