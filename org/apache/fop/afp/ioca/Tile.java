package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.modca.AbstractStructuredObject;
import org.apache.fop.afp.util.BinaryUtils;

public class Tile extends AbstractStructuredObject {
   private static final int MAX_DATA_LEN = 8191;
   private TilePosition tilePosition;
   private TileSize tileSize;
   private BandImage bandImage;
   private byte[] data;
   private IDEStructureParameter ideStructureParameter;
   private byte encoding = 3;
   private byte ideSize = 1;

   public void writeContent(OutputStream os) throws IOException {
      this.tilePosition.writeToStream(os);
      this.tileSize.writeToStream(os);
      os.write(this.getImageEncodingParameter());
      os.write(this.getImageIDESizeParameter());
      if (this.bandImage != null) {
         this.bandImage.writeToStream(os);
      }

      if (this.ideStructureParameter != null) {
         this.ideStructureParameter.writeToStream(os);
      }

      if (this.data != null) {
         byte[] c = new byte[this.data.length / 4];
         byte[] m = new byte[this.data.length / 4];
         byte[] y = new byte[this.data.length / 4];
         byte[] k = new byte[this.data.length / 4];

         for(int j = 0; j < this.data.length / 4; ++j) {
            c[j] = this.data[4 * j];
            m[j] = this.data[4 * j + 1];
            y[j] = this.data[4 * j + 2];
            k[j] = this.data[4 * j + 3];
         }

         byte[] dataHeader = new byte[]{-2, -100, 0, 0, 0, 0, 0};
         int lengthOffset = true;
         dataHeader[4] = 1;
         writeChunksToStream(c, dataHeader, 2, 8191, os);
         dataHeader[4] = 2;
         writeChunksToStream(m, dataHeader, 2, 8191, os);
         dataHeader[4] = 3;
         writeChunksToStream(y, dataHeader, 2, 8191, os);
         dataHeader[4] = 4;
         writeChunksToStream(k, dataHeader, 2, 8191, os);
      }

   }

   protected void writeStart(OutputStream os) throws IOException {
      byte[] startData = new byte[]{-116, 0};
      os.write(startData);
   }

   protected void writeEnd(OutputStream os) throws IOException {
      byte[] endData = new byte[]{-115, 0};
      os.write(endData);
   }

   public void setPosition(TilePosition tilePosition) {
      this.tilePosition = tilePosition;
   }

   public void setSize(TileSize tileSize) {
      this.tileSize = tileSize;
   }

   public void setImageData(byte[] imageData) {
      this.data = (byte[])imageData.clone();
   }

   protected static void writeChunksToStream(byte[] data, byte[] dataHeader, int lengthOffset, int maxChunkLength, OutputStream os) throws IOException {
      int dataLength = data.length;
      maxChunkLength -= 3;
      int numFullChunks = dataLength / maxChunkLength;
      int lastChunkLength = dataLength % maxChunkLength;
      byte[] len = new byte[]{31, -1};
      int off = 0;
      if (numFullChunks > 0) {
         dataHeader[lengthOffset] = len[0];
         dataHeader[lengthOffset + 1] = len[1];

         for(int i = 0; i < numFullChunks; off += maxChunkLength) {
            os.write(dataHeader);
            os.write(data, off, maxChunkLength);
            ++i;
         }
      }

      if (lastChunkLength > 0) {
         len = BinaryUtils.convert(3 + lastChunkLength, 2);
         dataHeader[lengthOffset] = len[0];
         dataHeader[lengthOffset + 1] = len[1];
         os.write(dataHeader);
         os.write(data, off, lastChunkLength);
      }

   }

   public void setImageEncodingParameter(byte encoding) {
      this.encoding = encoding;
   }

   public void setImageIDESizeParameter(byte ideSize) {
      this.ideSize = ideSize;
   }

   public void setIDEStructureParameter(IDEStructureParameter ideStructureParameter) {
      this.ideStructureParameter = ideStructureParameter;
   }

   private byte[] getImageEncodingParameter() {
      byte[] encodingData = new byte[]{-107, 2, this.encoding, (byte)(this.encoding == -125 ? 254 : 1)};
      return encodingData;
   }

   private byte[] getImageIDESizeParameter() {
      if (this.ideSize != 1) {
         byte[] ideSizeData = new byte[]{-106, 1, this.ideSize};
         return ideSizeData;
      } else {
         return new byte[0];
      }
   }

   public void setBandImage(BandImage bandImage) {
      this.bandImage = bandImage;
   }
}
