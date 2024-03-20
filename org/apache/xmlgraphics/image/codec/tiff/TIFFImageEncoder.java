package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.Deflater;
import org.apache.xmlgraphics.image.codec.util.ImageEncodeParam;
import org.apache.xmlgraphics.image.codec.util.ImageEncoderImpl;
import org.apache.xmlgraphics.image.codec.util.PropertyUtil;
import org.apache.xmlgraphics.image.codec.util.SeekableOutputStream;

public class TIFFImageEncoder extends ImageEncoderImpl {
   private static final int TIFF_JPEG_TABLES = 347;
   private static final int TIFF_YCBCR_SUBSAMPLING = 530;
   private static final int TIFF_YCBCR_POSITIONING = 531;
   private static final int TIFF_REF_BLACK_WHITE = 532;
   private static final int[] SIZE_OF_TYPE = new int[]{0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};

   public TIFFImageEncoder(OutputStream output, ImageEncodeParam param) {
      super(output, param);
      if (this.param == null) {
         this.param = new TIFFEncodeParam();
      }

   }

   public void encode(RenderedImage im) throws IOException {
      this.writeFileHeader();
      TIFFEncodeParam encodeParam = (TIFFEncodeParam)this.param;
      Iterator iter = encodeParam.getExtraImages();
      if (iter != null) {
         int ifdOffset = 8;
         RenderedImage nextImage = im;
         TIFFEncodeParam nextParam = encodeParam;

         boolean hasNext;
         do {
            hasNext = iter.hasNext();
            ifdOffset = this.encode(nextImage, nextParam, ifdOffset, !hasNext);
            if (hasNext) {
               Object obj = iter.next();
               if (obj instanceof RenderedImage) {
                  nextImage = (RenderedImage)obj;
                  nextParam = encodeParam;
               } else if (obj instanceof Object[]) {
                  Object[] o = (Object[])((Object[])obj);
                  nextImage = (RenderedImage)o[0];
                  nextParam = (TIFFEncodeParam)o[1];
               }
            }
         } while(hasNext);
      } else {
         this.encode(im, encodeParam, 8, true);
      }

   }

   public Object encodeMultiple(Object context, RenderedImage img) throws IOException {
      TIFFEncodeParam encodeParam = (TIFFEncodeParam)this.param;
      if (encodeParam.getExtraImages() != null) {
         throw new IllegalStateException(PropertyUtil.getString("TIFFImageEncoder11"));
      } else {
         Context c = (Context)context;
         if (c == null) {
            c = new Context();
            this.writeFileHeader();
         } else {
            c.ifdOffset = this.encode(c.nextImage, encodeParam, c.ifdOffset, false);
         }

         c.nextImage = img;
         return c;
      }
   }

   public void finishMultiple(Object context) throws IOException {
      if (context == null) {
         throw new NullPointerException();
      } else {
         Context c = (Context)context;
         TIFFEncodeParam encodeParam = (TIFFEncodeParam)this.param;
         c.ifdOffset = this.encode(c.nextImage, encodeParam, c.ifdOffset, true);
      }
   }

   private int encode(RenderedImage im, TIFFEncodeParam encodeParam, int ifdOffset, boolean isLast) throws IOException {
      CompressionValue compression = encodeParam.getCompression();
      if (compression == CompressionValue.JPEG_TTN2) {
         throw new IllegalArgumentException(PropertyUtil.getString("TIFFImageEncoder12"));
      } else {
         boolean isTiled = encodeParam.getWriteTiled();
         int minX = im.getMinX();
         int minY = im.getMinY();
         int width = im.getWidth();
         int height = im.getHeight();
         SampleModel sampleModel = im.getSampleModel();
         ColorModel colorModel = im.getColorModel();
         int[] sampleSize = sampleModel.getSampleSize();
         int dataTypeSize = sampleSize[0];
         int numBands = sampleModel.getNumBands();
         int dataType = sampleModel.getDataType();
         this.validateImage(dataTypeSize, sampleSize, numBands, dataType, colorModel);
         boolean dataTypeIsShort = dataType == 2 || dataType == 1;
         ImageInfo imageInfo = ImageInfo.newInstance(im, dataTypeSize, numBands, colorModel, encodeParam);
         if (imageInfo.getType() == ImageType.UNSUPPORTED) {
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder8"));
         } else {
            int numTiles = imageInfo.getNumTiles();
            long bytesPerTile = imageInfo.getBytesPerTile();
            long bytesPerRow = imageInfo.getBytesPerRow();
            int tileHeight = imageInfo.getTileHeight();
            int tileWidth = imageInfo.getTileWidth();
            long[] tileByteCounts = new long[numTiles];

            for(int i = 0; i < numTiles; ++i) {
               tileByteCounts[i] = bytesPerTile;
            }

            long totalBytesOfData;
            if (!isTiled) {
               totalBytesOfData = (long)(height - tileHeight * (numTiles - 1));
               tileByteCounts[numTiles - 1] = totalBytesOfData * bytesPerRow;
            }

            totalBytesOfData = bytesPerTile * (long)(numTiles - 1) + tileByteCounts[numTiles - 1];
            long[] tileOffsets = new long[numTiles];
            SortedSet fields = new TreeSet();
            fields.add(new TIFFField(256, 4, 1, new long[]{(long)width}));
            fields.add(new TIFFField(257, 4, 1, new long[]{(long)height}));
            char[] shortSampleSize = new char[numBands];

            for(int i = 0; i < numBands; ++i) {
               shortSampleSize[i] = (char)dataTypeSize;
            }

            fields.add(new TIFFField(258, 3, numBands, shortSampleSize));
            fields.add(new TIFFField(259, 3, 1, new char[]{(char)compression.getValue()}));
            fields.add(new TIFFField(262, 3, 1, new char[]{(char)imageInfo.getType().getPhotometricInterpretation()}));
            if (!isTiled) {
               fields.add(new TIFFField(273, 4, numTiles, tileOffsets));
            }

            fields.add(new TIFFField(277, 3, 1, new char[]{(char)numBands}));
            if (!isTiled) {
               fields.add(new TIFFField(278, 4, 1, new long[]{(long)tileHeight}));
               fields.add(new TIFFField(279, 4, numTiles, tileByteCounts));
            }

            if (imageInfo.getColormap() != null) {
               fields.add(new TIFFField(320, 3, imageInfo.getColormapSize(), imageInfo.getColormap()));
            }

            if (isTiled) {
               fields.add(new TIFFField(322, 4, 1, new long[]{(long)tileWidth}));
               fields.add(new TIFFField(323, 4, 1, new long[]{(long)tileHeight}));
               fields.add(new TIFFField(324, 4, numTiles, tileOffsets));
               fields.add(new TIFFField(325, 4, numTiles, tileByteCounts));
            }

            int b;
            char[] sampleFormat;
            if (imageInfo.getNumberOfExtraSamples() > 0) {
               sampleFormat = new char[imageInfo.getNumberOfExtraSamples()];

               for(b = 0; b < imageInfo.getNumberOfExtraSamples(); ++b) {
                  sampleFormat[b] = (char)imageInfo.getExtraSamplesType().getValue();
               }

               fields.add(new TIFFField(338, 3, imageInfo.getNumberOfExtraSamples(), sampleFormat));
            }

            if (dataType != 0) {
               sampleFormat = new char[numBands];
               if (dataType == 4) {
                  sampleFormat[0] = 3;
               } else if (dataType == 1) {
                  sampleFormat[0] = 1;
               } else {
                  sampleFormat[0] = 2;
               }

               for(b = 1; b < numBands; ++b) {
                  sampleFormat[b] = sampleFormat[0];
               }

               fields.add(new TIFFField(339, 3, numBands, sampleFormat));
            }

            if (imageInfo.getType() == ImageType.YCBCR) {
               char subsampleH = 1;
               char subsampleV = 1;
               fields.add(new TIFFField(530, 3, 2, new char[]{subsampleH, subsampleV}));
               fields.add(new TIFFField(531, 3, 1, new char[]{(char)(compression == CompressionValue.JPEG_TTN2 ? 1 : 2)}));
               long[][] refbw = new long[][]{{15L, 1L}, {235L, 1L}, {128L, 1L}, {240L, 1L}, {128L, 1L}, {240L, 1L}};
               fields.add(new TIFFField(532, 5, 6, refbw));
            }

            TIFFField[] extraFields = encodeParam.getExtraFields();
            List extantTags = new ArrayList(fields.size());
            Iterator var74 = fields.iterator();

            while(var74.hasNext()) {
               TIFFField fld = (TIFFField)var74.next();
               extantTags.add(fld.getTag());
            }

            TIFFField[] var76 = extraFields;
            int var78 = extraFields.length;

            for(int var36 = 0; var36 < var78; ++var36) {
               TIFFField fld = var76[var36];
               Integer tagValue = fld.getTag();
               if (!extantTags.contains(tagValue)) {
                  fields.add(fld);
                  extantTags.add(tagValue);
               }
            }

            int dirSize = this.getDirectorySize(fields);
            tileOffsets[0] = (long)(ifdOffset + dirSize);
            OutputStream outCache = null;
            byte[] compressBuf = null;
            File tempFile = null;
            int nextIFDOffset = 0;
            boolean skipByte = false;
            Deflater deflater = null;
            boolean jpegRGBToYCbCr = false;
            int numBytesPadding;
            if (compression == CompressionValue.NONE) {
               numBytesPadding = 0;
               if (dataTypeSize == 16 && tileOffsets[0] % 2L != 0L) {
                  numBytesPadding = 1;
                  int var10002 = tileOffsets[0]++;
               } else if (dataTypeSize == 32 && tileOffsets[0] % 4L != 0L) {
                  numBytesPadding = (int)(4L - tileOffsets[0] % 4L);
                  tileOffsets[0] += (long)numBytesPadding;
               }

               int padding;
               for(padding = 1; padding < numTiles; ++padding) {
                  tileOffsets[padding] = tileOffsets[padding - 1] + tileByteCounts[padding - 1];
               }

               if (!isLast) {
                  nextIFDOffset = (int)(tileOffsets[0] + totalBytesOfData);
                  if ((nextIFDOffset & 1) != 0) {
                     ++nextIFDOffset;
                     skipByte = true;
                  }
               }

               this.writeDirectory(ifdOffset, fields, nextIFDOffset);
               if (numBytesPadding != 0) {
                  for(padding = 0; padding < numBytesPadding; ++padding) {
                     this.output.write(0);
                  }
               }
            } else {
               if (this.output instanceof SeekableOutputStream) {
                  ((SeekableOutputStream)this.output).seek(tileOffsets[0]);
               } else {
                  outCache = this.output;

                  try {
                     tempFile = File.createTempFile("jai-SOS-", ".tmp");
                     tempFile.deleteOnExit();
                     RandomAccessFile raFile = new RandomAccessFile(tempFile, "rw");
                     this.output = new SeekableOutputStream(raFile);
                  } catch (IOException var67) {
                     this.output = new ByteArrayOutputStream((int)totalBytesOfData);
                  }
               }

               int bufSize = false;
               switch (compression) {
                  case PACKBITS:
                     numBytesPadding = (int)(bytesPerTile + (bytesPerRow + 127L) / 128L * (long)tileHeight);
                     break;
                  case DEFLATE:
                     numBytesPadding = (int)bytesPerTile;
                     deflater = new Deflater(encodeParam.getDeflateLevel());
                     break;
                  default:
                     numBytesPadding = 0;
               }

               if (numBytesPadding != 0) {
                  compressBuf = new byte[numBytesPadding];
               }
            }

            int[] pixels = null;
            float[] fpixels = null;
            boolean checkContiguous = dataTypeSize == 1 && sampleModel instanceof MultiPixelPackedSampleModel && dataType == 0 || dataTypeSize == 8 && sampleModel instanceof ComponentSampleModel;
            byte[] bpixels = null;
            if (compression != CompressionValue.JPEG_TTN2) {
               if (dataType == 0) {
                  bpixels = new byte[tileHeight * tileWidth * numBands];
               } else if (dataTypeIsShort) {
                  bpixels = new byte[2 * tileHeight * tileWidth * numBands];
               } else if (dataType == 3 || dataType == 4) {
                  bpixels = new byte[4 * tileHeight * tileWidth * numBands];
               }
            }

            int lastRow = minY + height;
            int lastCol = minX + width;
            int tileNum = 0;

            int totalBytes;
            int rows;
            int size;
            int bytesCopied;
            for(totalBytes = minY; totalBytes < lastRow; totalBytes += tileHeight) {
               rows = isTiled ? tileHeight : Math.min(tileHeight, lastRow - totalBytes);
               size = rows * tileWidth * numBands;

               for(bytesCopied = minX; bytesCopied < lastCol; bytesCopied += tileWidth) {
                  Raster src = im.getData(new Rectangle(bytesCopied, totalBytes, tileWidth, rows));
                  boolean useDataBuffer = false;
                  int numCompressedBytes;
                  int j;
                  int inOffset;
                  if (compression != CompressionValue.JPEG_TTN2) {
                     if (checkContiguous) {
                        if (dataTypeSize == 8) {
                           ComponentSampleModel csm = (ComponentSampleModel)src.getSampleModel();
                           int[] bankIndices = csm.getBankIndices();
                           int[] bandOffsets = csm.getBandOffsets();
                           numCompressedBytes = csm.getPixelStride();
                           j = csm.getScanlineStride();
                           if (numCompressedBytes == numBands && (long)j == bytesPerRow) {
                              useDataBuffer = true;

                              for(inOffset = 0; useDataBuffer && inOffset < numBands; ++inOffset) {
                                 if (bankIndices[inOffset] != 0 || bandOffsets[inOffset] != inOffset) {
                                    useDataBuffer = false;
                                 }
                              }
                           } else {
                              useDataBuffer = false;
                           }
                        } else {
                           MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)src.getSampleModel();
                           if (mpp.getNumBands() == 1 && mpp.getDataBitOffset() == 0 && mpp.getPixelBitStride() == 1) {
                              useDataBuffer = true;
                           }
                        }
                     }

                     if (!useDataBuffer) {
                        if (dataType == 4) {
                           fpixels = src.getPixels(bytesCopied, totalBytes, tileWidth, rows, fpixels);
                        } else {
                           pixels = src.getPixels(bytesCopied, totalBytes, tileWidth, rows, pixels);
                        }
                     }
                  }

                  int pixel = false;
                  int k = 0;
                  int lineStride;
                  int outOffset;
                  int j;
                  int index;
                  int pixel;
                  byte[] btmp;
                  switch (dataTypeSize) {
                     case 1:
                        if (useDataBuffer) {
                           btmp = ((DataBufferByte)src.getDataBuffer()).getData();
                           MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)src.getSampleModel();
                           inOffset = mpp.getScanlineStride();
                           lineStride = mpp.getOffset(bytesCopied - src.getSampleModelTranslateX(), totalBytes - src.getSampleModelTranslateY());
                           if ((long)inOffset == bytesPerRow) {
                              System.arraycopy(btmp, lineStride, bpixels, 0, (int)bytesPerRow * rows);
                           } else {
                              outOffset = 0;

                              for(j = 0; j < rows; ++j) {
                                 System.arraycopy(btmp, lineStride, bpixels, outOffset, (int)bytesPerRow);
                                 lineStride += inOffset;
                                 outOffset = (int)((long)outOffset + bytesPerRow);
                              }
                           }
                        } else {
                           index = 0;

                           for(numCompressedBytes = 0; numCompressedBytes < rows; ++numCompressedBytes) {
                              for(j = 0; j < tileWidth / 8; ++j) {
                                 pixel = pixels[index++] << 7 | pixels[index++] << 6 | pixels[index++] << 5 | pixels[index++] << 4 | pixels[index++] << 3 | pixels[index++] << 2 | pixels[index++] << 1 | pixels[index++];
                                 bpixels[k++] = (byte)pixel;
                              }

                              if (tileWidth % 8 > 0) {
                                 pixel = 0;

                                 for(j = 0; j < tileWidth % 8; ++j) {
                                    pixel |= pixels[index++] << 7 - j;
                                 }

                                 bpixels[k++] = (byte)pixel;
                              }
                           }
                        }

                        if (compression == CompressionValue.NONE) {
                           this.output.write(bpixels, 0, rows * ((tileWidth + 7) / 8));
                        } else if (compression == CompressionValue.PACKBITS) {
                           numCompressedBytes = compressPackBits(bpixels, rows, bytesPerRow, compressBuf);
                           tileByteCounts[tileNum++] = (long)numCompressedBytes;
                           this.output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == CompressionValue.DEFLATE) {
                           numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                           tileByteCounts[tileNum++] = (long)numCompressedBytes;
                           this.output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                     case 4:
                        index = 0;

                        for(numCompressedBytes = 0; numCompressedBytes < rows; ++numCompressedBytes) {
                           for(j = 0; j < tileWidth / 2; ++j) {
                              pixel = pixels[index++] << 4 | pixels[index++];
                              bpixels[k++] = (byte)pixel;
                           }

                           if ((tileWidth & 1) == 1) {
                              pixel = pixels[index++] << 4;
                              bpixels[k++] = (byte)pixel;
                           }
                        }

                        if (compression == CompressionValue.NONE) {
                           this.output.write(bpixels, 0, rows * ((tileWidth + 1) / 2));
                        } else if (compression == CompressionValue.PACKBITS) {
                           numCompressedBytes = compressPackBits(bpixels, rows, bytesPerRow, compressBuf);
                           tileByteCounts[tileNum++] = (long)numCompressedBytes;
                           this.output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == CompressionValue.DEFLATE) {
                           numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                           tileByteCounts[tileNum++] = (long)numCompressedBytes;
                           this.output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                     case 8:
                        if (compression != CompressionValue.JPEG_TTN2) {
                           if (useDataBuffer) {
                              btmp = ((DataBufferByte)src.getDataBuffer()).getData();
                              ComponentSampleModel csm = (ComponentSampleModel)src.getSampleModel();
                              inOffset = csm.getOffset(bytesCopied - src.getSampleModelTranslateX(), totalBytes - src.getSampleModelTranslateY());
                              lineStride = csm.getScanlineStride();
                              if ((long)lineStride == bytesPerRow) {
                                 System.arraycopy(btmp, inOffset, bpixels, 0, (int)bytesPerRow * rows);
                              } else {
                                 outOffset = 0;

                                 for(j = 0; j < rows; ++j) {
                                    System.arraycopy(btmp, inOffset, bpixels, outOffset, (int)bytesPerRow);
                                    inOffset += lineStride;
                                    outOffset = (int)((long)outOffset + bytesPerRow);
                                 }
                              }
                           } else {
                              for(numCompressedBytes = 0; numCompressedBytes < size; ++numCompressedBytes) {
                                 bpixels[numCompressedBytes] = (byte)pixels[numCompressedBytes];
                              }
                           }
                        }

                        if (compression == CompressionValue.NONE) {
                           this.output.write(bpixels, 0, size);
                        } else if (compression == CompressionValue.PACKBITS) {
                           numCompressedBytes = compressPackBits(bpixels, rows, bytesPerRow, compressBuf);
                           tileByteCounts[tileNum++] = (long)numCompressedBytes;
                           this.output.write(compressBuf, 0, numCompressedBytes);
                        } else if (compression == CompressionValue.DEFLATE) {
                           numCompressedBytes = deflate(deflater, bpixels, compressBuf);
                           tileByteCounts[tileNum++] = (long)numCompressedBytes;
                           this.output.write(compressBuf, 0, numCompressedBytes);
                        }
                        break;
                     case 16:
                        numCompressedBytes = 0;

                        for(j = 0; j < size; ++j) {
                           inOffset = pixels[j];
                           bpixels[numCompressedBytes++] = (byte)((inOffset & '\uff00') >> 8);
                           bpixels[numCompressedBytes++] = (byte)(inOffset & 255);
                        }

                        if (compression == CompressionValue.NONE) {
                           this.output.write(bpixels, 0, size * 2);
                        } else if (compression == CompressionValue.PACKBITS) {
                           j = compressPackBits(bpixels, rows, bytesPerRow, compressBuf);
                           tileByteCounts[tileNum++] = (long)j;
                           this.output.write(compressBuf, 0, j);
                        } else if (compression == CompressionValue.DEFLATE) {
                           j = deflate(deflater, bpixels, compressBuf);
                           tileByteCounts[tileNum++] = (long)j;
                           this.output.write(compressBuf, 0, j);
                        }
                        break;
                     case 32:
                        if (dataType == 3) {
                           j = 0;

                           for(inOffset = 0; inOffset < size; ++inOffset) {
                              lineStride = pixels[inOffset];
                              bpixels[j++] = (byte)((lineStride & -16777216) >>> 24);
                              bpixels[j++] = (byte)((lineStride & 16711680) >>> 16);
                              bpixels[j++] = (byte)((lineStride & '\uff00') >>> 8);
                              bpixels[j++] = (byte)(lineStride & 255);
                           }
                        } else {
                           j = 0;

                           for(inOffset = 0; inOffset < size; ++inOffset) {
                              lineStride = Float.floatToIntBits(fpixels[inOffset]);
                              bpixels[j++] = (byte)((lineStride & -16777216) >>> 24);
                              bpixels[j++] = (byte)((lineStride & 16711680) >>> 16);
                              bpixels[j++] = (byte)((lineStride & '\uff00') >>> 8);
                              bpixels[j++] = (byte)(lineStride & 255);
                           }
                        }

                        if (compression == CompressionValue.NONE) {
                           this.output.write(bpixels, 0, size * 4);
                        } else if (compression == CompressionValue.PACKBITS) {
                           j = compressPackBits(bpixels, rows, bytesPerRow, compressBuf);
                           tileByteCounts[tileNum++] = (long)j;
                           this.output.write(compressBuf, 0, j);
                        } else if (compression == CompressionValue.DEFLATE) {
                           j = deflate(deflater, bpixels, compressBuf);
                           tileByteCounts[tileNum++] = (long)j;
                           this.output.write(compressBuf, 0, j);
                        }
                  }
               }
            }

            if (compression == CompressionValue.NONE) {
               if (skipByte) {
                  this.output.write(0);
               }
            } else {
               totalBytes = 0;

               for(rows = 1; rows < numTiles; ++rows) {
                  size = (int)tileByteCounts[rows - 1];
                  totalBytes += size;
                  tileOffsets[rows] = tileOffsets[rows - 1] + (long)size;
               }

               totalBytes += (int)tileByteCounts[numTiles - 1];
               nextIFDOffset = isLast ? 0 : ifdOffset + dirSize + totalBytes;
               if ((nextIFDOffset & 1) != 0) {
                  ++nextIFDOffset;
                  skipByte = true;
               }

               if (outCache == null) {
                  if (skipByte) {
                     this.output.write(0);
                  }

                  SeekableOutputStream sos = (SeekableOutputStream)this.output;
                  long savePos = sos.getFilePointer();
                  sos.seek((long)ifdOffset);
                  this.writeDirectory(ifdOffset, fields, nextIFDOffset);
                  sos.seek(savePos);
               } else if (tempFile != null) {
                  FileInputStream fileStream = new FileInputStream(tempFile);

                  try {
                     this.output.close();
                     this.output = outCache;
                     this.writeDirectory(ifdOffset, fields, nextIFDOffset);
                     byte[] copyBuffer = new byte[8192];

                     int bytesRead;
                     for(bytesCopied = 0; bytesCopied < totalBytes; bytesCopied += bytesRead) {
                        bytesRead = fileStream.read(copyBuffer);
                        if (bytesRead == -1) {
                           break;
                        }

                        this.output.write(copyBuffer, 0, bytesRead);
                     }
                  } finally {
                     fileStream.close();
                  }

                  boolean isDeleted = tempFile.delete();

                  assert isDeleted;

                  if (skipByte) {
                     this.output.write(0);
                  }
               } else {
                  if (!(this.output instanceof ByteArrayOutputStream)) {
                     throw new IllegalStateException(PropertyUtil.getString("TIFFImageEncoder13"));
                  }

                  ByteArrayOutputStream memoryStream = (ByteArrayOutputStream)this.output;
                  this.output = outCache;
                  this.writeDirectory(ifdOffset, fields, nextIFDOffset);
                  memoryStream.writeTo(this.output);
                  if (skipByte) {
                     this.output.write(0);
                  }
               }
            }

            return nextIFDOffset;
         }
      }
   }

   private void validateImage(int dataTypeSize, int[] sampleSize, int numBands, int dataType, ColorModel colorModel) {
      for(int i = 1; i < sampleSize.length; ++i) {
         if (sampleSize[i] != dataTypeSize) {
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder0"));
         }
      }

      if ((dataTypeSize == 1 || dataTypeSize == 4) && numBands != 1) {
         throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder1"));
      } else {
         switch (dataType) {
            case 0:
               if (dataTypeSize == 4) {
                  throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder2"));
               }
               break;
            case 1:
            case 2:
               if (dataTypeSize != 16) {
                  throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder3"));
               }
               break;
            case 3:
            case 4:
               if (dataTypeSize != 32) {
                  throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder4"));
               }
               break;
            default:
               throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder5"));
         }

         if (colorModel instanceof IndexColorModel && dataType != 0) {
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder6"));
         }
      }
   }

   private int getDirectorySize(SortedSet fields) {
      int numEntries = fields.size();
      int dirSize = 2 + numEntries * 12 + 4;
      Iterator var4 = fields.iterator();

      while(var4.hasNext()) {
         Object field1 = var4.next();
         TIFFField field = (TIFFField)field1;
         int valueSize = field.getCount() * SIZE_OF_TYPE[field.getType()];
         if (valueSize > 4) {
            dirSize += valueSize;
         }
      }

      return dirSize;
   }

   private void writeFileHeader() throws IOException {
      this.output.write(77);
      this.output.write(77);
      this.output.write(0);
      this.output.write(42);
      this.writeLong(8L);
   }

   private void writeDirectory(int thisIFDOffset, SortedSet fields, int nextIFDOffset) throws IOException {
      int numEntries = fields.size();
      long offsetBeyondIFD = (long)(thisIFDOffset + 12 * numEntries + 4 + 2);
      List tooBig = new ArrayList();
      this.writeUnsignedShort(numEntries);
      Iterator var8 = fields.iterator();

      Object aTooBig;
      while(var8.hasNext()) {
         aTooBig = var8.next();
         TIFFField field = (TIFFField)aTooBig;
         int tag = field.getTag();
         this.writeUnsignedShort(tag);
         int type = field.getType();
         this.writeUnsignedShort(type);
         int count = field.getCount();
         int valueSize = getValueSize(field);
         this.writeLong(type == 2 ? (long)valueSize : (long)count);
         if (valueSize > 4) {
            this.writeLong(offsetBeyondIFD);
            offsetBeyondIFD += (long)valueSize;
            tooBig.add(field);
         } else {
            this.writeValuesAsFourBytes(field);
         }
      }

      this.writeLong((long)nextIFDOffset);
      var8 = tooBig.iterator();

      while(var8.hasNext()) {
         aTooBig = var8.next();
         this.writeValues((TIFFField)aTooBig);
      }

   }

   private static int getValueSize(TIFFField field) throws UnsupportedEncodingException {
      int type = field.getType();
      int count = field.getCount();
      int valueSize = 0;
      if (type == 2) {
         for(int i = 0; i < count; ++i) {
            byte[] stringBytes = field.getAsString(i).getBytes("UTF-8");
            valueSize += stringBytes.length;
            if (stringBytes[stringBytes.length - 1] != 0) {
               ++valueSize;
            }
         }
      } else {
         valueSize = count * SIZE_OF_TYPE[type];
      }

      return valueSize;
   }

   private void writeValuesAsFourBytes(TIFFField field) throws IOException {
      int dataType = field.getType();
      int count = field.getCount();
      switch (dataType) {
         case 1:
            byte[] bytes = field.getAsBytes();
            if (count > 4) {
               count = 4;
            }

            int i;
            for(i = 0; i < count; ++i) {
               this.output.write(bytes[i]);
            }

            for(i = 0; i < 4 - count; ++i) {
               this.output.write(0);
            }
         case 2:
         default:
            break;
         case 3:
            char[] chars = field.getAsChars();
            if (count > 2) {
               count = 2;
            }

            int i;
            for(i = 0; i < count; ++i) {
               this.writeUnsignedShort(chars[i]);
            }

            for(i = 0; i < 2 - count; ++i) {
               this.writeUnsignedShort(0);
            }

            return;
         case 4:
            long[] longs = field.getAsLongs();

            for(int i = 0; i < count; ++i) {
               this.writeLong(longs[i]);
            }
      }

   }

   private void writeValues(TIFFField field) throws IOException {
      int dataType = field.getType();
      int count = field.getCount();
      int i;
      int i;
      switch (dataType) {
         case 1:
         case 6:
         case 7:
            byte[] bytes = field.getAsBytes();

            for(int i = 0; i < count; ++i) {
               this.output.write(bytes[i]);
            }

            return;
         case 2:
            for(i = 0; i < count; ++i) {
               byte[] stringBytes = field.getAsString(i).getBytes("UTF-8");
               this.output.write(stringBytes);
               if (stringBytes[stringBytes.length - 1] != 0) {
                  this.output.write(0);
               }
            }

            return;
         case 3:
            char[] chars = field.getAsChars();

            for(int i = 0; i < count; ++i) {
               this.writeUnsignedShort(chars[i]);
            }

            return;
         case 4:
         case 9:
            long[] longs = field.getAsLongs();

            for(int i = 0; i < count; ++i) {
               this.writeLong(longs[i]);
            }

            return;
         case 5:
         case 10:
            long[][] rationals = field.getAsRationals();

            for(i = 0; i < count; ++i) {
               this.writeLong(rationals[i][0]);
               this.writeLong(rationals[i][1]);
            }

            return;
         case 8:
            short[] shorts = field.getAsShorts();

            for(int i = 0; i < count; ++i) {
               this.writeUnsignedShort(shorts[i]);
            }

            return;
         case 11:
            float[] floats = field.getAsFloats();

            for(int i = 0; i < count; ++i) {
               i = Float.floatToIntBits(floats[i]);
               this.writeLong((long)i);
            }

            return;
         case 12:
            double[] doubles = field.getAsDoubles();

            for(i = 0; i < count; ++i) {
               long longBits = Double.doubleToLongBits(doubles[i]);
               this.writeLong(longBits >>> 32);
               this.writeLong(longBits & 4294967295L);
            }

            return;
         default:
            throw new RuntimeException(PropertyUtil.getString("TIFFImageEncoder10"));
      }
   }

   private void writeUnsignedShort(int s) throws IOException {
      this.output.write((s & '\uff00') >>> 8);
      this.output.write(s & 255);
   }

   private void writeLong(long l) throws IOException {
      this.output.write((int)((l & -16777216L) >>> 24));
      this.output.write((int)((l & 16711680L) >>> 16));
      this.output.write((int)((l & 65280L) >>> 8));
      this.output.write((int)(l & 255L));
   }

   private static int compressPackBits(byte[] data, int numRows, long bytesPerRow, byte[] compData) {
      int inOffset = 0;
      int outOffset = 0;

      for(int i = 0; i < numRows; ++i) {
         outOffset = packBits(data, inOffset, (int)bytesPerRow, compData, outOffset);
         inOffset = (int)((long)inOffset + bytesPerRow);
      }

      return outOffset;
   }

   private static int packBits(byte[] input, int inOffset, int inCount, byte[] output, int outOffset) {
      int inMax = inOffset + inCount - 1;
      int inMaxMinus1 = inMax - 1;

      while(true) {
         while(true) {
            int run;
            do {
               if (inOffset > inMax) {
                  return outOffset;
               }

               run = 1;

               byte replicate;
               for(replicate = input[inOffset]; run < 127 && inOffset < inMax && input[inOffset] == input[inOffset + 1]; ++inOffset) {
                  ++run;
               }

               if (run > 1) {
                  ++inOffset;
                  output[outOffset++] = (byte)(-(run - 1));
                  output[outOffset++] = replicate;
               }

               for(run = 0; run < 128 && (inOffset < inMax && input[inOffset] != input[inOffset + 1] || inOffset < inMaxMinus1 && input[inOffset] != input[inOffset + 2]); output[outOffset] = input[inOffset++]) {
                  ++run;
                  ++outOffset;
               }

               if (run > 0) {
                  output[outOffset] = (byte)(run - 1);
                  ++outOffset;
               }
            } while(inOffset != inMax);

            if (run > 0 && run < 128) {
               ++output[outOffset];
               output[outOffset++] = input[inOffset++];
            } else {
               output[outOffset++] = 0;
               output[outOffset++] = input[inOffset++];
            }
         }
      }
   }

   private static int deflate(Deflater deflater, byte[] inflated, byte[] deflated) {
      deflater.setInput(inflated);
      deflater.finish();
      int numCompressedBytes = deflater.deflate(deflated);
      deflater.reset();
      return numCompressedBytes;
   }

   private static class Context {
      private RenderedImage nextImage;
      private int ifdOffset;

      private Context() {
         this.ifdOffset = 8;
      }

      // $FF: synthetic method
      Context(Object x0) {
         this();
      }
   }
}
