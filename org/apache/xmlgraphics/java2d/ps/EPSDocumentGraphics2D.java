package org.apache.xmlgraphics.java2d.ps;

import java.io.IOException;
import java.util.Date;
import org.apache.xmlgraphics.ps.DSCConstants;

public class EPSDocumentGraphics2D extends AbstractPSDocumentGraphics2D {
   public EPSDocumentGraphics2D(boolean textAsShapes) {
      super(textAsShapes);
   }

   protected void writeFileHeader() throws IOException {
      Long pagewidth = (long)this.width;
      Long pageheight = (long)this.height;
      this.gen.writeln("%!PS-Adobe-3.0 EPSF-3.0");
      this.gen.writeDSCComment("Creator", (Object[])(new String[]{"Apache XML Graphics Commons: EPS Generator for Java2D"}));
      this.gen.writeDSCComment("CreationDate", new Object[]{new Date()});
      this.gen.writeDSCComment("Pages", DSCConstants.ATEND);
      this.gen.writeDSCComment("BoundingBox", new Object[]{ZERO, ZERO, pagewidth, pageheight});
      this.gen.writeDSCComment("LanguageLevel", (Object)this.gen.getPSLevel());
      this.gen.writeDSCComment("EndComments");
      this.gen.writeDSCComment("BeginProlog");
      this.writeProcSets();
      if (this.customTextHandler instanceof PSTextHandler) {
         ((PSTextHandler)this.customTextHandler).writeSetup();
      }

      this.gen.writeDSCComment("EndProlog");
   }

   protected void writePageHeader() throws IOException {
      Integer pageNumber = this.pagecount;
      this.gen.writeDSCComment("Page", new Object[]{pageNumber.toString(), pageNumber});
      this.gen.writeDSCComment("PageBoundingBox", new Object[]{ZERO, ZERO, this.width, this.height});
      this.gen.writeDSCComment("BeginPageSetup");
      if (this.customTextHandler instanceof PSTextHandler) {
         ((PSTextHandler)this.customTextHandler).writePageSetup();
      }

   }

   protected void writePageTrailer() throws IOException {
   }
}
