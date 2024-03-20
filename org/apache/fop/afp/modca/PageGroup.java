package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.Factory;

public class PageGroup extends AbstractResourceEnvironmentGroupContainer {
   public PageGroup(Factory factory, String name) {
      super(factory, name);
   }

   public void createTagLogicalElement(TagLogicalElement.State state) {
      TagLogicalElement tle = this.factory.createTagLogicalElement(state);
      if (!this.getTagLogicalElements().contains(tle)) {
         this.getTagLogicalElements().add(tle);
      }

   }

   public void endPageGroup() {
      this.complete = true;
   }

   protected void writeStart(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-88, (byte)-83);
      os.write(data);
   }

   protected void writeEnd(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-87, (byte)-83);
      os.write(data);
   }

   public String toString() {
      return this.getName();
   }
}
