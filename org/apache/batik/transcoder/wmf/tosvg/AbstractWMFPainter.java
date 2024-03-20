package org.apache.batik.transcoder.wmf.tosvg;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class AbstractWMFPainter {
   public static final String WMF_FILE_EXTENSION = ".wmf";
   protected WMFFont wmfFont = null;
   protected int currentHorizAlign = 0;
   protected int currentVertAlign = 0;
   public static final int PEN = 1;
   public static final int BRUSH = 2;
   public static final int FONT = 3;
   public static final int NULL_PEN = 4;
   public static final int NULL_BRUSH = 5;
   public static final int PALETTE = 6;
   public static final int OBJ_BITMAP = 7;
   public static final int OBJ_REGION = 8;
   protected WMFRecordStore currentStore;
   protected transient boolean bReadingWMF = true;
   protected transient BufferedInputStream bufStream = null;

   protected BufferedImage getImage(byte[] bit, int width, int height) {
      int _width = (bit[7] & 255) << 24 | (bit[6] & 255) << 16 | (bit[5] & 255) << 8 | bit[4] & 255;
      int _height = (bit[11] & 255) << 24 | (bit[10] & 255) << 16 | (bit[9] & 255) << 8 | bit[8] & 255;
      return width == _width && height == _height ? this.getImage(bit) : null;
   }

   protected Dimension getImageDimension(byte[] bit) {
      int _width = (bit[7] & 255) << 24 | (bit[6] & 255) << 16 | (bit[5] & 255) << 8 | bit[4] & 255;
      int _height = (bit[11] & 255) << 24 | (bit[10] & 255) << 16 | (bit[9] & 255) << 8 | bit[8] & 255;
      return new Dimension(_width, _height);
   }

   protected BufferedImage getImage(byte[] bit) {
      int _width = (bit[7] & 255) << 24 | (bit[6] & 255) << 16 | (bit[5] & 255) << 8 | bit[4] & 255;
      int _height = (bit[11] & 255) << 24 | (bit[10] & 255) << 16 | (bit[9] & 255) << 8 | bit[8] & 255;
      int[] bitI = new int[_width * _height];
      BufferedImage img = new BufferedImage(_width, _height, 1);
      WritableRaster raster = img.getRaster();
      int _headerSize = (bit[3] & 255) << 24 | (bit[2] & 255) << 16 | (bit[1] & 255) << 8 | bit[0] & 255;
      int _planes = (bit[13] & 255) << 8 | bit[12] & 255;
      int _nbit = (bit[15] & 255) << 8 | bit[14] & 255;
      int _size = (bit[23] & 255) << 24 | (bit[22] & 255) << 16 | (bit[21] & 255) << 8 | bit[20] & 255;
      if (_size == 0) {
         _size = ((_width * _nbit + 31 & -32) >> 3) * _height;
      }

      int _clrused = (bit[35] & 255) << 24 | (bit[34] & 255) << 16 | (bit[33] & 255) << 8 | bit[32] & 255;
      int nbColors;
      int offset;
      int pos;
      if (_nbit == 24) {
         nbColors = _size / _height - _width * 3;
         offset = _headerSize;

         for(int j = 0; j < _height; ++j) {
            for(pos = 0; pos < _width; ++pos) {
               bitI[_width * (_height - j - 1) + pos] = -16777216 | (bit[offset + 2] & 255) << 16 | (bit[offset + 1] & 255) << 8 | bit[offset] & 255;
               offset += 3;
            }

            offset += nbColors;
         }
      } else {
         int i;
         int[] palette;
         if (_nbit == 8) {
            int nbColors = false;
            if (_clrused > 0) {
               nbColors = _clrused;
            } else {
               nbColors = 256;
            }

            offset = _headerSize;
            palette = new int[nbColors];

            for(pos = 0; pos < nbColors; ++pos) {
               palette[pos] = -16777216 | (bit[offset + 2] & 255) << 16 | (bit[offset + 1] & 255) << 8 | bit[offset] & 255;
               offset += 4;
            }

            _size = bit.length - offset;
            pos = _size / _height - _width;

            for(int j = 0; j < _height; ++j) {
               for(i = 0; i < _width; ++i) {
                  bitI[_width * (_height - j - 1) + i] = palette[bit[offset] & 255];
                  ++offset;
               }

               offset += pos;
            }
         } else if (_nbit == 1) {
            int nbColors = 2;
            offset = _headerSize;
            palette = new int[nbColors];

            for(pos = 0; pos < nbColors; ++pos) {
               palette[pos] = -16777216 | (bit[offset + 2] & 255) << 16 | (bit[offset + 1] & 255) << 8 | bit[offset] & 255;
               offset += 4;
            }

            pos = 7;
            byte currentByte = bit[offset];
            i = _size / _height - _width / 8;

            for(int j = 0; j < _height; ++j) {
               for(int i = 0; i < _width; ++i) {
                  if ((currentByte & 1 << pos) != 0) {
                     bitI[_width * (_height - j - 1) + i] = palette[1];
                  } else {
                     bitI[_width * (_height - j - 1) + i] = palette[0];
                  }

                  --pos;
                  if (pos == -1) {
                     pos = 7;
                     ++offset;
                     if (offset < bit.length) {
                        currentByte = bit[offset];
                     }
                  }
               }

               offset += i;
               pos = 7;
               if (offset < bit.length) {
                  currentByte = bit[offset];
               }
            }
         }
      }

      raster.setDataElements(0, 0, _width, _height, bitI);
      return img;
   }

   protected AttributedCharacterIterator getCharacterIterator(Graphics2D g2d, String sr, WMFFont wmffont) {
      return this.getAttributedString(g2d, sr, wmffont).getIterator();
   }

   protected AttributedCharacterIterator getCharacterIterator(Graphics2D g2d, String sr, WMFFont wmffont, int align) {
      AttributedString ats = this.getAttributedString(g2d, sr, wmffont);
      return ats.getIterator();
   }

   protected AttributedString getAttributedString(Graphics2D g2d, String sr, WMFFont wmffont) {
      AttributedString ats = new AttributedString(sr);
      Font font = g2d.getFont();
      ats.addAttribute(TextAttribute.SIZE, font.getSize2D());
      ats.addAttribute(TextAttribute.FONT, font);
      if (this.wmfFont.underline != 0) {
         ats.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
      }

      if (this.wmfFont.italic != 0) {
         ats.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
      } else {
         ats.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
      }

      if (this.wmfFont.weight > 400) {
         ats.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
      } else {
         ats.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
      }

      return ats;
   }

   public void setRecordStore(WMFRecordStore currentStore) {
      if (currentStore == null) {
         throw new IllegalArgumentException();
      } else {
         this.currentStore = currentStore;
      }
   }

   public WMFRecordStore getRecordStore() {
      return this.currentStore;
   }

   protected int addObject(WMFRecordStore store, int type, Object obj) {
      return this.currentStore.addObject(type, obj);
   }

   protected int addObjectAt(WMFRecordStore store, int type, Object obj, int idx) {
      return this.currentStore.addObjectAt(type, obj, idx);
   }
}
