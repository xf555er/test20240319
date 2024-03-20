package org.apache.xmlgraphics.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.image.loader.ImageSource;
import org.apache.xmlgraphics.image.loader.util.ImageInputStreamAdapter;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.xml.sax.InputSource;

public final class XmlSourceUtil {
   private XmlSourceUtil() {
   }

   public static InputStream getInputStream(Source src) {
      try {
         if (src instanceof StreamSource) {
            return ((StreamSource)src).getInputStream();
         }

         if (src instanceof DOMSource) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            StreamResult xmlSource = new StreamResult(outStream);
            TransformerFactory.newInstance().newTransformer().transform(src, xmlSource);
            return new ByteArrayInputStream(outStream.toByteArray());
         }

         if (src instanceof SAXSource) {
            return ((SAXSource)src).getInputSource().getByteStream();
         }

         if (src instanceof ImageSource) {
            return new ImageInputStreamAdapter(((ImageSource)src).getImageInputStream());
         }
      } catch (Exception var3) {
      }

      return null;
   }

   public static InputStream needInputStream(Source src) {
      InputStream in = getInputStream(src);
      if (in != null) {
         return in;
      } else {
         throw new IllegalArgumentException("Source must be a StreamSource with an InputStream or an ImageSource");
      }
   }

   public static boolean hasReader(Source src) {
      if (src instanceof StreamSource) {
         Reader reader = ((StreamSource)src).getReader();
         return reader != null;
      } else {
         if (src instanceof SAXSource) {
            InputSource is = ((SAXSource)src).getInputSource();
            if (is != null) {
               return is.getCharacterStream() != null;
            }
         }

         return false;
      }
   }

   public static void removeStreams(Source src) {
      if (src instanceof ImageSource) {
         ImageSource isrc = (ImageSource)src;
         isrc.setImageInputStream((ImageInputStream)null);
      } else if (src instanceof StreamSource) {
         StreamSource ssrc = (StreamSource)src;
         ssrc.setInputStream((InputStream)null);
         ssrc.setReader((Reader)null);
      } else if (src instanceof SAXSource) {
         InputSource is = ((SAXSource)src).getInputSource();
         if (is != null) {
            is.setByteStream((InputStream)null);
            is.setCharacterStream((Reader)null);
         }
      }

   }

   public static void closeQuietly(Source src) {
      if (src instanceof StreamSource) {
         StreamSource streamSource = (StreamSource)src;
         IOUtils.closeQuietly(streamSource.getReader());
      } else if (src instanceof ImageSource) {
         if (ImageUtil.getImageInputStream(src) != null) {
            try {
               ImageUtil.getImageInputStream(src).close();
            } catch (IOException var2) {
            }
         }
      } else if (src instanceof SAXSource) {
         InputSource is = ((SAXSource)src).getInputSource();
         if (is != null) {
            IOUtils.closeQuietly(is.getByteStream());
            IOUtils.closeQuietly(is.getCharacterStream());
         }
      }

      removeStreams(src);
   }

   public static boolean hasInputStream(Source src) {
      return getInputStream(src) != null;
   }
}
