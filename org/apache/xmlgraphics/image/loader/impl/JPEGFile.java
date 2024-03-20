package org.apache.xmlgraphics.image.loader.impl;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JPEGFile implements JPEGConstants {
   protected static final Log log = LogFactory.getLog(JPEGFile.class);
   private DataInput in;

   public JPEGFile(ImageInputStream in) {
      this.in = in;
   }

   public JPEGFile(InputStream in) {
      this.in = new DataInputStream(in);
   }

   public DataInput getDataInput() {
      return this.in;
   }

   public int readMarkerSegment() throws IOException {
      int marker;
      do {
         marker = this.in.readByte() & 255;
      } while(marker != 255);

      int segID;
      do {
         segID = this.in.readByte() & 255;
      } while(segID == 255);

      return segID;
   }

   public int readSegmentLength() throws IOException {
      int reclen = this.in.readUnsignedShort();
      return reclen;
   }

   public void skipCurrentMarkerSegment() throws IOException {
      int reclen = this.readSegmentLength();
      this.in.skipBytes(reclen - 2);
   }
}
