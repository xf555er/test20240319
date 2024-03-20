package org.apache.batik.gvt.renderer;

import org.apache.batik.util.Platform;

public class ConcreteImageRendererFactory implements ImageRendererFactory {
   public Renderer createRenderer() {
      return this.createStaticImageRenderer();
   }

   public ImageRenderer createStaticImageRenderer() {
      return (ImageRenderer)(Platform.isOSX ? new MacRenderer() : new StaticRenderer());
   }

   public ImageRenderer createDynamicImageRenderer() {
      return (ImageRenderer)(Platform.isOSX ? new MacRenderer() : new DynamicRenderer());
   }
}
