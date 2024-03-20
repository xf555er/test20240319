package org.apache.fop.render.pcl.fonts;

public class PCLFontSegment {
   private SegmentID identifier;
   private byte[] data;

   public PCLFontSegment(SegmentID identifier, byte[] data) {
      this.identifier = identifier;
      this.data = data;
   }

   public byte[] getData() {
      return this.data;
   }

   public SegmentID getIdentifier() {
      return this.identifier;
   }

   public int getSize() {
      return this.identifier == PCLFontSegment.SegmentID.NULL ? 0 : this.data.length;
   }

   public static enum SegmentID {
      CC(17219),
      CP(17232),
      GT(18260),
      IF(18758),
      PA(20545),
      XW(22619),
      NULL(65535);

      private int complementID;

      private SegmentID(int complementID) {
         this.complementID = complementID;
      }

      public int getValue() {
         return this.complementID;
      }
   }
}
