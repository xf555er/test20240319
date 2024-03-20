package org.apache.xmlgraphics.image.loader;

import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import org.apache.xmlgraphics.image.loader.util.ImageInputStreamAdapter;

public class ImageSource implements Source {
   private String systemId;
   private ImageInputStream iin;
   private boolean fastSource;

   public ImageSource(ImageInputStream in, String systemId, boolean fastSource) {
      assert in != null : "InputStream is null";

      this.iin = in;
      this.systemId = systemId;
      this.fastSource = fastSource;
   }

   public InputStream getInputStream() {
      return this.iin == null ? null : new ImageInputStreamAdapter(this.iin);
   }

   public ImageInputStream getImageInputStream() {
      return this.iin;
   }

   public void setImageInputStream(ImageInputStream in) {
      this.iin = in;
   }

   public String getSystemId() {
      return this.systemId;
   }

   public void setSystemId(String systemId) {
      this.systemId = systemId;
   }

   public boolean isFastSource() {
      return this.fastSource;
   }

   public String toString() {
      return (this.isFastSource() ? "FAST " : "") + "ImageSource: " + this.getSystemId() + " " + this.getImageInputStream();
   }
}
