package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.afp.Completable;

public class ResourceEnvironmentGroup extends AbstractEnvironmentGroup implements Completable {
   private static final String DEFAULT_NAME = "REG00001";
   private boolean complete;

   public ResourceEnvironmentGroup() {
      this("REG00001");
   }

   public ResourceEnvironmentGroup(String name) {
      super(name);
   }

   protected void writeStart(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-88, (byte)-39);
      os.write(data);
   }

   protected void writeEnd(OutputStream os) throws IOException {
      byte[] data = new byte[17];
      this.copySF(data, (byte)-87, (byte)-39);
      os.write(data);
   }

   protected void writeContent(OutputStream os) throws IOException {
      this.writeObjects(this.mapDataResources, os);
      this.writeObjects(this.mapPageOverlays, os);
   }

   public void setComplete(boolean complete) {
      this.complete = complete;
   }

   public boolean isComplete() {
      return this.complete;
   }
}
