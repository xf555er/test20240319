package org.apache.xmlgraphics.image.writer;

public class ImageWriterParams {
   public static final int SINGLE_STRIP = -1;
   public static final int ONE_ROW_PER_STRIP = 1;
   private Integer xResolution;
   private Integer yResolution;
   private Float jpegQuality;
   private Boolean jpegForceBaseline;
   private String compressionMethod;
   private ResolutionUnit resolutionUnit;
   private int rowsPerStrip;
   private Endianness endianness;

   public ImageWriterParams() {
      this.resolutionUnit = ResolutionUnit.INCH;
      this.rowsPerStrip = 1;
      this.endianness = Endianness.DEFAULT;
   }

   public boolean hasResolution() {
      return this.getXResolution() != null && this.getYResolution() != null;
   }

   public Integer getResolution() {
      return this.getXResolution();
   }

   public Float getJPEGQuality() {
      return this.jpegQuality;
   }

   public Boolean getJPEGForceBaseline() {
      return this.jpegForceBaseline;
   }

   public String getCompressionMethod() {
      return this.compressionMethod;
   }

   public void setResolution(int resolution) {
      this.setXResolution(resolution);
      this.setYResolution(resolution);
   }

   public void setJPEGQuality(float quality, boolean forceBaseline) {
      this.jpegQuality = quality;
      this.jpegForceBaseline = forceBaseline ? Boolean.TRUE : Boolean.FALSE;
   }

   public void setCompressionMethod(String method) {
      this.compressionMethod = method;
   }

   public boolean isSingleStrip() {
      return this.rowsPerStrip == -1;
   }

   public void setSingleStrip(boolean isSingle) {
      this.rowsPerStrip = isSingle ? -1 : 1;
   }

   public void setRowsPerStrip(int rowsPerStrip) {
      this.rowsPerStrip = rowsPerStrip;
   }

   public int getRowsPerStrip() {
      return this.rowsPerStrip;
   }

   public ResolutionUnit getResolutionUnit() {
      return this.resolutionUnit;
   }

   public void setResolutionUnit(ResolutionUnit resolutionUnit) {
      this.resolutionUnit = resolutionUnit;
   }

   public Integer getXResolution() {
      return this.xResolution;
   }

   public void setXResolution(int resolution) {
      this.xResolution = resolution;
   }

   public Integer getYResolution() {
      return this.yResolution;
   }

   public void setYResolution(int resolution) {
      this.yResolution = resolution;
   }

   public Endianness getEndianness() {
      return this.endianness;
   }

   public void setEndianness(Endianness endianness) {
      this.endianness = endianness;
   }
}
