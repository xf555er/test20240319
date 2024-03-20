package org.apache.xmlgraphics.java2d;

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

public class GenericGraphicsDevice extends GraphicsDevice {
   private final GraphicsConfiguration gc;

   public GenericGraphicsDevice(GraphicsConfiguration gc) {
      this.gc = gc;
   }

   public GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate gct) {
      return this.gc;
   }

   public GraphicsConfiguration[] getConfigurations() {
      return new GraphicsConfiguration[]{this.gc};
   }

   public GraphicsConfiguration getDefaultConfiguration() {
      return this.gc;
   }

   public String getIDstring() {
      return this.toString();
   }

   public int getType() {
      return 1;
   }
}
