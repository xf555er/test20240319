package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.AbstractStructuredObject;

public class ImageContent extends AbstractStructuredObject {
   public static final byte COMPID_G3_MH = -128;
   public static final byte COMPID_G3_MR = -127;
   public static final byte COMPID_G3_MMR = -126;
   public static final byte COMPID_JPEG = -125;
   private ImageSizeParameter imageSizeParameter;
   private IDEStructureParameter ideStructureParameter;
   private byte encoding = 3;
   private byte ideSize = 1;
   private byte compression = -64;
   private byte[] data;
   private TileTOC tileTOC;
   private Tile tile;
   private static final int MAX_DATA_LEN = 65535;

   public void setImageSizeParameter(ImageSizeParameter imageSizeParameter) {
      this.imageSizeParameter = imageSizeParameter;
   }

   public void setIDEStructureParameter(IDEStructureParameter parameter) {
      this.ideStructureParameter = parameter;
   }

   public IDEStructureParameter getIDEStructureParameter() {
      return this.ideStructureParameter;
   }

   public IDEStructureParameter needIDEStructureParameter() {
      if (this.ideStructureParameter == null) {
         this.setIDEStructureParameter(new IDEStructureParameter());
      }

      return this.getIDEStructureParameter();
   }

   public void setImageEncoding(byte enc) {
      this.encoding = enc;
   }

   public void setImageCompression(byte comp) {
      this.compression = comp;
   }

   public void setImageIDESize(byte s) {
      this.ideSize = s;
   }

   /** @deprecated */
   @Deprecated
   public void setImageIDEColorModel(byte color) {
      this.needIDEStructureParameter().setColorModel(color);
   }

   /** @deprecated */
   @Deprecated
   public void setSubtractive(boolean subtractive) {
      this.needIDEStructureParameter().setSubtractive(subtractive);
   }

   public void setImageData(byte[] imageData) {
      if (this.tile != null) {
         this.tile.setImageData(imageData);
      } else {
         this.data = imageData;
      }

   }

   protected void writeContent(OutputStream os) throws IOException {
      if (this.imageSizeParameter != null) {
         this.imageSizeParameter.writeToStream(os);
      }

      if (this.tileTOC != null) {
         this.tileTOC.writeToStream(os);
      }

      if (this.tile == null) {
         os.write(this.getImageEncodingParameter());
         os.write(this.getImageIDESizeParameter());
      } else {
         this.tile.setImageEncodingParameter(this.encoding);
         this.tile.setImageIDESizeParameter(this.ideSize);
      }

      if (this.tile == null) {
         if (this.getIDEStructureParameter() != null) {
            this.getIDEStructureParameter().writeToStream(os);
         }
      } else if (this.getIDEStructureParameter() != null) {
         this.tile.setIDEStructureParameter(this.getIDEStructureParameter());
      }

      boolean useFS10 = this.ideSize == 1;
      if (!useFS10 && this.tileTOC == null) {
         os.write(this.getExternalAlgorithmParameter());
      }

      if (this.tile != null) {
         this.tile.writeToStream(os);
      }

      if (this.data != null) {
         byte[] dataHeader = new byte[]{-2, -110, 0, 0};
         int lengthOffset = true;
         writeChunksToStream(this.data, dataHeader, 2, 65535, os);
      }

   }

   protected void writeStart(OutputStream os) throws IOException {
      byte[] startData = new byte[]{-111, 1, -1};
      os.write(startData);
   }

   protected void writeEnd(OutputStream os) throws IOException {
      byte[] endData = new byte[]{-109, 0};
      os.write(endData);
   }

   private byte[] getImageEncodingParameter() {
      byte[] encodingData = new byte[]{-107, 2, this.encoding, (byte)(this.encoding == -125 ? 254 : 1)};
      return encodingData;
   }

   private byte[] getExternalAlgorithmParameter() {
      if (this.encoding == -125 && this.compression != 0) {
         byte[] extAlgData = new byte[]{-97, 0, 16, 0, -125, 0, 0, 0, this.compression, 0, 0, 0};
         extAlgData[1] = (byte)(extAlgData.length - 2);
         return extAlgData;
      } else {
         return new byte[0];
      }
   }

   private byte[] getImageIDESizeParameter() {
      if (this.ideSize != 1) {
         byte[] ideSizeData = new byte[]{-106, 1, this.ideSize};
         return ideSizeData;
      } else {
         return new byte[0];
      }
   }

   public void setTileTOC(TileTOC toc) {
      this.tileTOC = toc;
   }

   public void addTile(Tile tile) {
      this.tile = tile;
   }
}
