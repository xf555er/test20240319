package org.apache.xmlgraphics.image.loader.impl;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.xmlgraphics.image.codec.png.PNGChunk;
import org.apache.xmlgraphics.image.codec.util.PropertyUtil;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;

class PNGFile implements PNGConstants {
   private ColorModel colorModel;
   private ICC_Profile iccProfile;
   private int sRGBRenderingIntent = -1;
   private int bitDepth;
   private int colorType;
   private boolean isTransparent;
   private int grayTransparentAlpha;
   private int redTransparentAlpha;
   private int greenTransparentAlpha;
   private int blueTransparentAlpha;
   private List streamVec = new ArrayList();
   private int paletteEntries;
   private byte[] redPalette;
   private byte[] greenPalette;
   private byte[] bluePalette;
   private byte[] alphaPalette;
   private boolean hasPalette;
   private boolean hasAlphaPalette;

   public PNGFile(InputStream stream, String uri) throws IOException, ImageException {
      if (!((InputStream)stream).markSupported()) {
         stream = new BufferedInputStream((InputStream)stream);
      }

      DataInputStream distream = new DataInputStream((InputStream)stream);
      long magic = distream.readLong();
      if (magic != -8552249625308161526L) {
         String msg = PropertyUtil.getString("PNGImageDecoder0");
         throw new ImageException(msg);
      } else {
         while(true) {
            String chunkType;
            try {
               chunkType = PNGChunk.getChunkType(distream);
               PNGChunk chunk;
               if (chunkType.equals(PNGChunk.ChunkType.IHDR.name())) {
                  chunk = PNGChunk.readChunk(distream);
                  this.parse_IHDR_chunk(chunk);
               } else if (chunkType.equals(PNGChunk.ChunkType.PLTE.name())) {
                  chunk = PNGChunk.readChunk(distream);
                  this.parse_PLTE_chunk(chunk);
               } else if (chunkType.equals(PNGChunk.ChunkType.IDAT.name())) {
                  chunk = PNGChunk.readChunk(distream);
                  this.streamVec.add(new ByteArrayInputStream(chunk.getData()));
               } else {
                  if (chunkType.equals(PNGChunk.ChunkType.IEND.name())) {
                     PNGChunk.skipChunk(distream);
                     return;
                  }

                  if (chunkType.equals(PNGChunk.ChunkType.tRNS.name())) {
                     chunk = PNGChunk.readChunk(distream);
                     this.parse_tRNS_chunk(chunk);
                  } else if (chunkType.equals(PNGChunk.ChunkType.iCCP.name())) {
                     chunk = PNGChunk.readChunk(distream);
                     this.parse_iCCP_chunk(chunk);
                  } else if (chunkType.equals(PNGChunk.ChunkType.sRGB.name())) {
                     chunk = PNGChunk.readChunk(distream);
                     this.parse_sRGB_chunk(chunk);
                  } else {
                     if (Character.isUpperCase(chunkType.charAt(0))) {
                        throw new ImageException("PNG unknown critical chunk: " + chunkType);
                     }

                     PNGChunk.skipChunk(distream);
                  }
               }
            } catch (Exception var8) {
               chunkType = PropertyUtil.getString("PNGImageDecoder2");
               throw new RuntimeException(chunkType + " " + uri, var8);
            }
         }
      }
   }

   public ImageRawPNG getImageRawPNG(ImageInfo info) throws ImageException {
      InputStream seqStream = new SequenceInputStream(Collections.enumeration(this.streamVec));
      ColorSpace rgbCS = null;
      switch (this.colorType) {
         case 0:
            if (this.hasPalette) {
               throw new ImageException("Corrupt PNG: color palette is not allowed!");
            }

            this.colorModel = new ComponentColorModel(ColorSpace.getInstance(1003), false, false, 1, 0);
            break;
         case 1:
         case 5:
         default:
            throw new ImageException("Unsupported color type: " + this.colorType);
         case 2:
            if (this.iccProfile != null) {
               rgbCS = new ICC_ColorSpace(this.iccProfile);
            } else if (this.sRGBRenderingIntent != -1) {
               rgbCS = ColorSpace.getInstance(1000);
            } else {
               rgbCS = ColorSpace.getInstance(1004);
            }

            this.colorModel = new ComponentColorModel((ColorSpace)rgbCS, false, false, 1, 0);
            break;
         case 3:
            if (this.hasAlphaPalette) {
               this.colorModel = new IndexColorModel(this.bitDepth, this.paletteEntries, this.redPalette, this.greenPalette, this.bluePalette, this.alphaPalette);
            } else {
               this.colorModel = new IndexColorModel(this.bitDepth, this.paletteEntries, this.redPalette, this.greenPalette, this.bluePalette);
            }
            break;
         case 4:
            if (this.hasPalette) {
               throw new ImageException("Corrupt PNG: color palette is not allowed!");
            }

            this.colorModel = new ComponentColorModel(ColorSpace.getInstance(1003), true, false, 3, 0);
            break;
         case 6:
            if (this.iccProfile != null) {
               rgbCS = new ICC_ColorSpace(this.iccProfile);
            } else if (this.sRGBRenderingIntent != -1) {
               rgbCS = ColorSpace.getInstance(1000);
            } else {
               rgbCS = ColorSpace.getInstance(1004);
            }

            this.colorModel = new ComponentColorModel((ColorSpace)rgbCS, true, false, 3, 0);
      }

      ImageRawPNG rawImage = new ImageRawPNG(info, seqStream, this.colorModel, this.bitDepth, this.iccProfile);
      if (this.isTransparent) {
         if (this.colorType == 0) {
            rawImage.setGrayTransparentAlpha(this.grayTransparentAlpha);
         } else if (this.colorType == 2) {
            rawImage.setRGBTransparentAlpha(this.redTransparentAlpha, this.greenTransparentAlpha, this.blueTransparentAlpha);
         } else if (this.colorType == 3) {
            rawImage.setTransparent();
         }
      }

      if (this.sRGBRenderingIntent != -1) {
         rawImage.setRenderingIntent(this.sRGBRenderingIntent);
      }

      return rawImage;
   }

   private void parse_IHDR_chunk(PNGChunk chunk) {
      this.bitDepth = chunk.getInt1(8);
      this.colorType = chunk.getInt1(9);
      int compressionMethod = chunk.getInt1(10);
      if (compressionMethod != 0) {
         throw new RuntimeException("Unsupported PNG compression method: " + compressionMethod);
      } else {
         int filterMethod = chunk.getInt1(11);
         if (filterMethod != 0) {
            throw new RuntimeException("Unsupported PNG filter method: " + filterMethod);
         } else {
            int interlaceMethod = chunk.getInt1(12);
            if (interlaceMethod != 0) {
               throw new RuntimeException("Unsupported PNG interlace method: " + interlaceMethod);
            }
         }
      }
   }

   private void parse_PLTE_chunk(PNGChunk chunk) {
      this.paletteEntries = chunk.getLength() / 3;
      this.redPalette = new byte[this.paletteEntries];
      this.greenPalette = new byte[this.paletteEntries];
      this.bluePalette = new byte[this.paletteEntries];
      this.hasPalette = true;
      int pltIndex = 0;

      for(int i = 0; i < this.paletteEntries; ++i) {
         this.redPalette[i] = chunk.getByte(pltIndex++);
         this.greenPalette[i] = chunk.getByte(pltIndex++);
         this.bluePalette[i] = chunk.getByte(pltIndex++);
      }

   }

   private void parse_tRNS_chunk(PNGChunk chunk) {
      if (this.colorType == 3) {
         int entries = chunk.getLength();
         if (entries > this.paletteEntries) {
            String msg = PropertyUtil.getString("PNGImageDecoder14");
            throw new RuntimeException(msg);
         }

         this.alphaPalette = new byte[this.paletteEntries];

         int i;
         for(i = 0; i < entries; ++i) {
            this.alphaPalette[i] = chunk.getByte(i);
         }

         for(i = entries; i < this.paletteEntries; ++i) {
            this.alphaPalette[i] = -1;
         }

         this.hasAlphaPalette = true;
      } else if (this.colorType == 0) {
         this.grayTransparentAlpha = chunk.getInt2(0);
      } else if (this.colorType == 2) {
         this.redTransparentAlpha = chunk.getInt2(0);
         this.greenTransparentAlpha = chunk.getInt2(2);
         this.blueTransparentAlpha = chunk.getInt2(4);
      } else if (this.colorType == 4 || this.colorType == 6) {
         String msg = PropertyUtil.getString("PNGImageDecoder15");
         throw new RuntimeException(msg);
      }

      this.isTransparent = true;
   }

   private void parse_iCCP_chunk(PNGChunk chunk) {
      int length = chunk.getLength();
      int textIndex = 0;

      while(chunk.getByte(textIndex++) != 0) {
      }

      ++textIndex;
      byte[] profile = new byte[length - textIndex];
      System.arraycopy(chunk.getData(), textIndex, profile, 0, length - textIndex);
      ByteArrayInputStream bais = new ByteArrayInputStream(profile);
      InflaterInputStream iis = new InflaterInputStream(bais, new Inflater());

      try {
         this.iccProfile = ICC_Profile.getInstance(iis);
      } catch (IOException var8) {
      }

   }

   private void parse_sRGB_chunk(PNGChunk chunk) {
      this.sRGBRenderingIntent = chunk.getByte(0);
   }
}
