package org.apache.xmlgraphics.image.loader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSource;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.image.loader.util.SoftMapCache;
import org.apache.xmlgraphics.io.XmlSourceUtil;

public abstract class AbstractImageSessionContext implements ImageSessionContext {
   private static final Log log = LogFactory.getLog(AbstractImageSessionContext.class);
   private static boolean noSourceReuse;
   private final FallbackResolver fallbackResolver;
   private SoftMapCache sessionSources = new SoftMapCache(false);

   public AbstractImageSessionContext() {
      this.fallbackResolver = new UnrestrictedFallbackResolver();
   }

   public AbstractImageSessionContext(FallbackResolver fallbackResolver) {
      this.fallbackResolver = fallbackResolver;
   }

   protected abstract Source resolveURI(String var1);

   public Source newSource(String uri) {
      Source source = this.resolveURI(uri);
      return !(source instanceof StreamSource) && !(source instanceof SAXSource) ? source : this.fallbackResolver.createSource(source, uri);
   }

   protected static ImageInputStream createImageInputStream(InputStream in) throws IOException {
      ImageInputStream iin = ImageIO.createImageInputStream(in);
      return (ImageInputStream)Proxy.newProxyInstance(ImageInputStream.class.getClassLoader(), new Class[]{ImageInputStream.class}, new ObservingImageInputStreamInvocationHandler(iin, in));
   }

   public static File toFile(URL url) {
      if (url != null && url.getProtocol().equals("file")) {
         try {
            String filename = "";
            if (url.getHost() != null && url.getHost().length() > 0) {
               filename = filename + Character.toString(File.separatorChar) + Character.toString(File.separatorChar) + url.getHost();
            }

            filename = filename + url.getFile().replace('/', File.separatorChar);
            filename = URLDecoder.decode(filename, "UTF-8");
            File f = new File(filename);
            return !f.isFile() ? null : f;
         } catch (UnsupportedEncodingException var3) {
            assert false;

            return null;
         }
      } else {
         return null;
      }
   }

   public Source getSource(String uri) {
      return (Source)this.sessionSources.remove(uri);
   }

   public Source needSource(String uri) throws FileNotFoundException {
      Source src = this.getSource(uri);
      if (src == null) {
         if (log.isDebugEnabled()) {
            log.debug("Creating new Source for " + uri);
         }

         src = this.newSource(uri);
         if (src == null) {
            throw new FileNotFoundException("Image not found: " + uri);
         }
      } else if (log.isDebugEnabled()) {
         log.debug("Reusing Source for " + uri);
      }

      return src;
   }

   public void returnSource(String uri, Source src) {
      ImageInputStream in = ImageUtil.getImageInputStream(src);

      try {
         if (in != null && in.getStreamPosition() != 0L) {
            throw new IllegalStateException("ImageInputStream is not reset for: " + uri);
         }
      } catch (IOException var5) {
         XmlSourceUtil.closeQuietly(src);
      }

      if (this.isReusable(src)) {
         log.debug("Returning Source for " + uri);
         this.sessionSources.put(uri, src);
      } else {
         XmlSourceUtil.closeQuietly(src);
      }

   }

   protected boolean isReusable(Source src) {
      if (noSourceReuse) {
         return false;
      } else {
         if (src instanceof ImageSource) {
            ImageSource is = (ImageSource)src;
            if (is.getImageInputStream() != null) {
               return true;
            }
         }

         return src instanceof DOMSource;
      }
   }

   private static ImageSource createImageSource(InputStream in, Source source) {
      try {
         return new ImageSource(createImageInputStream(ImageUtil.autoDecorateInputStream(in)), source.getSystemId(), false);
      } catch (IOException var3) {
         log.error("Unable to create ImageInputStream for InputStream from system identifier '" + source.getSystemId() + "' (" + var3.getMessage() + ")");
         return null;
      }
   }

   static {
      String noSourceReuseString = System.getProperty(AbstractImageSessionContext.class.getName() + ".no-source-reuse");
      noSourceReuse = Boolean.valueOf(noSourceReuseString);
   }

   public static final class RestrictedFallbackResolver implements FallbackResolver {
      public Source createSource(Source source, String uri) {
         if (source == null) {
            if (AbstractImageSessionContext.log.isDebugEnabled()) {
               AbstractImageSessionContext.log.debug("URI could not be resolved: " + uri);
            }

            return null;
         } else if (ImageUtil.hasInputStream(source)) {
            return AbstractImageSessionContext.createImageSource(XmlSourceUtil.getInputStream(source), source);
         } else {
            throw new UnsupportedOperationException("There are no contingency mechanisms for I/O.");
         }
      }
   }

   public static final class UnrestrictedFallbackResolver implements FallbackResolver {
      public Source createSource(Source source, String uri) {
         if (source == null) {
            if (AbstractImageSessionContext.log.isDebugEnabled()) {
               AbstractImageSessionContext.log.debug("URI could not be resolved: " + uri);
            }

            return null;
         } else {
            ImageSource imageSource = null;
            String resolvedURI = source.getSystemId();

            URL url;
            try {
               url = new URL(resolvedURI);
            } catch (MalformedURLException var13) {
               url = null;
            }

            File f = AbstractImageSessionContext.toFile(url);
            if (f != null) {
               boolean directFileAccess = true;

               assert source instanceof StreamSource || source instanceof SAXSource;

               InputStream in = XmlSourceUtil.getInputStream(source);
               if (in == null) {
                  try {
                     in = new FileInputStream(f);
                  } catch (FileNotFoundException var12) {
                     AbstractImageSessionContext.log.error("Error while opening file. Could not load image from system identifier '" + source.getSystemId() + "' (" + var12.getMessage() + ")");
                     return null;
                  }
               }

               InputStream in = ImageUtil.decorateMarkSupported((InputStream)in);

               try {
                  if (ImageUtil.isGZIPCompressed(in)) {
                     directFileAccess = false;
                  }
               } catch (IOException var14) {
                  AbstractImageSessionContext.log.error("Error while checking the InputStream for GZIP compression. Could not load image from system identifier '" + source.getSystemId() + "' (" + var14.getMessage() + ")");
                  return null;
               }

               if (directFileAccess) {
                  IOUtils.closeQuietly(in);

                  try {
                     ImageInputStream newInputStream = ImageIO.createImageInputStream(f);
                     if (newInputStream == null) {
                        AbstractImageSessionContext.log.error("Unable to create ImageInputStream for local file " + f + " from system identifier '" + source.getSystemId() + "'");
                        return null;
                     }

                     imageSource = new ImageSource(newInputStream, resolvedURI, true);
                  } catch (IOException var11) {
                     AbstractImageSessionContext.log.error("Unable to create ImageInputStream for local file from system identifier '" + source.getSystemId() + "' (" + var11.getMessage() + ")");
                  }
               }
            }

            if (imageSource == null) {
               if (XmlSourceUtil.hasReader(source) && !ImageUtil.hasInputStream(source)) {
                  return source;
               } else {
                  InputStream in = XmlSourceUtil.getInputStream(source);
                  if (in == null && url != null) {
                     try {
                        in = url.openStream();
                     } catch (Exception var10) {
                        AbstractImageSessionContext.log.error("Unable to obtain stream from system identifier '" + source.getSystemId() + "'");
                     }
                  }

                  if (in == null) {
                     AbstractImageSessionContext.log.error("The Source that was returned from URI resolution didn't contain an InputStream for URI: " + uri);
                     return null;
                  } else {
                     return AbstractImageSessionContext.createImageSource(in, source);
                  }
               }
            } else {
               return imageSource;
            }
         }
      }
   }

   public interface FallbackResolver {
      Source createSource(Source var1, String var2);
   }

   private static class ObservingImageInputStreamInvocationHandler implements InvocationHandler {
      private ImageInputStream iin;
      private InputStream in;

      public ObservingImageInputStreamInvocationHandler(ImageInputStream iin, InputStream underlyingStream) {
         this.iin = iin;
         this.in = underlyingStream;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         try {
            if ("close".equals(method.getName())) {
               Object var4;
               try {
                  var4 = method.invoke(this.iin, args);
               } finally {
                  IOUtils.closeQuietly(this.in);
                  this.in = null;
               }

               return var4;
            } else {
               return method.invoke(this.iin, args);
            }
         } catch (InvocationTargetException var9) {
            throw var9.getCause();
         }
      }
   }
}
