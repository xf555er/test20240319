package org.apache.batik.apps.rasterizer;

public class SVGConverterException extends Exception {
   protected String errorCode;
   protected Object[] errorInfo;
   protected boolean isFatal;

   public SVGConverterException(String errorCode) {
      this(errorCode, (Object[])null, false);
   }

   public SVGConverterException(String errorCode, Object[] errorInfo) {
      this(errorCode, errorInfo, false);
   }

   public SVGConverterException(String errorCode, Object[] errorInfo, boolean isFatal) {
      this.errorCode = errorCode;
      this.errorInfo = errorInfo;
      this.isFatal = isFatal;
   }

   public SVGConverterException(String errorCode, boolean isFatal) {
      this(errorCode, (Object[])null, isFatal);
   }

   public boolean isFatal() {
      return this.isFatal;
   }

   public String getMessage() {
      return Messages.formatMessage(this.errorCode, this.errorInfo);
   }

   public String getErrorCode() {
      return this.errorCode;
   }
}
