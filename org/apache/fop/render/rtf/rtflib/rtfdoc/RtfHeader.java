package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class RtfHeader extends RtfContainer {
   private static final String CHARSET = "ansi";
   private final Map userProperties = new HashMap();

   RtfHeader(RtfFile f, Writer w) throws IOException {
      super(f, w);
      new RtfFontTable(this, w);
      new RtfGenerator(this, w);
   }

   protected void writeRtfContent() throws IOException {
      this.writeControlWord("ansi");
      this.writeUserProperties();
      RtfColorTable.getInstance().writeColors(this);
      super.writeRtfContent();
      RtfTemplate.getInstance().writeTemplate(this);
      RtfStyleSheetTable.getInstance().writeStyleSheet(this);
      this.writeFootnoteProperties();
   }

   private void writeUserProperties() throws IOException {
      if (this.userProperties.size() > 0) {
         this.writeGroupMark(true);
         this.writeStarControlWord("userprops");
         Iterator var1 = this.userProperties.entrySet().iterator();

         while(var1.hasNext()) {
            Object o = var1.next();
            Map.Entry entry = (Map.Entry)o;
            this.writeGroupMark(true);
            this.writeControlWord("propname");
            RtfStringConverter.getInstance().writeRtfString(this.writer, entry.getKey().toString());
            this.writeGroupMark(false);
            this.writeControlWord("proptype30");
            this.writeGroupMark(true);
            this.writeControlWord("staticval");
            RtfStringConverter.getInstance().writeRtfString(this.writer, entry.getValue().toString());
            this.writeGroupMark(false);
         }

         this.writeGroupMark(false);
      }

   }

   void write(String toWrite) throws IOException {
      this.writer.write(toWrite);
   }

   void writeRtfString(String toWrite) throws IOException {
      RtfStringConverter.getInstance().writeRtfString(this.writer, toWrite);
   }

   private void writeFootnoteProperties() throws IOException {
      this.newLine();
      this.writeControlWord("fet0");
      this.writeControlWord("ftnbj");
   }
}
