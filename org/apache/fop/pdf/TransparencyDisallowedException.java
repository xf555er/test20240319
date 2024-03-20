package org.apache.fop.pdf;

public class TransparencyDisallowedException extends PDFConformanceException {
   private static final long serialVersionUID = -1653621832449817596L;
   private final Object profile;
   private final String context;

   public TransparencyDisallowedException(Object profile, String context) {
      super(profile + " does not allow the use of transparency." + (context == null ? "" : " (" + context + ")"));
      this.profile = profile;
      this.context = context;
   }

   public Object getProfile() {
      return this.profile;
   }

   public String getContext() {
      return this.context;
   }
}
