package org.apache.fop.pdf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class PDFEncryptionManager {
   private static final Log LOG = LogFactory.getLog(PDFEncryptionManager.class);

   private PDFEncryptionManager() {
   }

   public static boolean isJCEAvailable() {
      try {
         Class.forName("javax.crypto.Cipher");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   public static boolean checkAvailableAlgorithms() {
      if (!isJCEAvailable()) {
         return false;
      } else {
         Provider[] providers = Security.getProviders("Cipher.RC4");
         if (providers == null) {
            LOG.warn("Cipher provider for RC4 not available.");
            return false;
         } else {
            providers = Security.getProviders("MessageDigest.MD5");
            if (providers == null) {
               LOG.warn("MessageDigest provider for MD5 not available.");
               return false;
            } else {
               return true;
            }
         }
      }
   }

   public static void setupPDFEncryption(PDFEncryptionParams params, PDFDocument pdf) {
      if (pdf == null) {
         throw new NullPointerException("PDF document must not be null");
      } else {
         if (params != null) {
            if (!checkAvailableAlgorithms()) {
               if (isJCEAvailable()) {
                  LOG.warn("PDF encryption has been requested, JCE is available but there's no JCE provider available that provides the necessary algorithms. The PDF won't be encrypted.");
               } else {
                  LOG.warn("PDF encryption has been requested but JCE is unavailable! The PDF won't be encrypted.");
               }
            }

            pdf.setEncryption(params);
         }

      }
   }

   public static PDFEncryption newInstance(PDFEncryptionParams params, PDFDocument pdf) {
      try {
         PDFObjectNumber pdfObjectNumber = new PDFObjectNumber();
         pdfObjectNumber.setDocument(pdf);
         Class clazz = Class.forName("org.apache.fop.pdf.PDFEncryptionJCE");
         Method makeMethod = clazz.getMethod("make", PDFObjectNumber.class, PDFEncryptionParams.class, PDFDocument.class);
         Object obj = makeMethod.invoke((Object)null, pdfObjectNumber, params, pdf);
         return (PDFEncryption)obj;
      } catch (ClassNotFoundException var6) {
         if (checkAvailableAlgorithms()) {
            LOG.warn("JCE and algorithms available, but the implementation class unavailable. Please do a full rebuild.");
         }

         return null;
      } catch (NoSuchMethodException var7) {
         LOG.error(var7);
         return null;
      } catch (IllegalAccessException var8) {
         LOG.error(var8);
         return null;
      } catch (InvocationTargetException var9) {
         LOG.error(var9);
         return null;
      }
   }
}
