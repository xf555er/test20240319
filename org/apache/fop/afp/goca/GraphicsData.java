package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.StructuredData;
import org.apache.fop.afp.util.BinaryUtils;
import org.apache.fop.afp.util.StringUtils;

public final class GraphicsData extends AbstractGraphicsDrawingOrderContainer {
   public static final int MAX_DATA_LEN = 8208;
   private GraphicsChainedSegment currentSegment;
   private boolean segmentedData;

   public int getDataLength() {
      return 8 + super.getDataLength();
   }

   public void setSegmentedData(boolean segmented) {
      this.segmentedData = segmented;
   }

   public String createSegmentName() {
      return StringUtils.lpad(String.valueOf((super.objects != null ? super.objects.size() : 0) + 1), '0', 4);
   }

   public GraphicsChainedSegment newSegment() {
      return this.newSegment(false, false);
   }

   public GraphicsChainedSegment newSegment(boolean appended, boolean prologPresent) {
      String segmentName = this.createSegmentName();
      if (this.currentSegment == null) {
         this.currentSegment = new GraphicsChainedSegment(segmentName);
      } else {
         this.currentSegment.setComplete(true);
         this.currentSegment = new GraphicsChainedSegment(segmentName, this.currentSegment.getNameBytes(), appended, prologPresent);
      }

      super.addObject(this.currentSegment);
      return this.currentSegment;
   }

   public void addObject(StructuredData object) {
      if (this.currentSegment == null || this.currentSegment.getDataLength() + object.getDataLength() >= 8192) {
         this.newSegment(true, false);
      }

      this.currentSegment.addObject(object);
   }

   public StructuredData removeCurrentSegment() {
      this.currentSegment = null;
      return super.removeLast();
   }

   public void writeToStream(OutputStream os) throws IOException {
      byte[] data = new byte[9];
      copySF(data, (byte)-45, (byte)-18, (byte)-69);
      int dataLength = this.getDataLength();
      byte[] len = BinaryUtils.convert(dataLength, 2);
      data[1] = len[0];
      data[2] = len[1];
      if (this.segmentedData) {
         data[6] = (byte)(data[6] | 32);
      }

      os.write(data);
      this.writeObjects(this.objects, os);
   }

   public String toString() {
      return "GraphicsData(len: " + this.getDataLength() + ")";
   }

   public void addSegment(GraphicsChainedSegment segment) {
      this.currentSegment = segment;
      super.addObject(this.currentSegment);
   }
}
