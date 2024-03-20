package org.apache.fop.render.intermediate;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.FontInfo;
import org.apache.xmlgraphics.util.DoubleFormatUtil;

public final class IFUtil {
   private IFUtil() {
   }

   private static String format(double value) {
      if (value == -0.0) {
         value = 0.0;
      }

      StringBuffer buf = new StringBuffer();
      DoubleFormatUtil.formatDouble(value, 6, 6, buf);
      return buf.toString();
   }

   public static StringBuffer toString(AffineTransform transform, StringBuffer sb) {
      if (transform.isIdentity()) {
         return sb;
      } else {
         double[] matrix = new double[6];
         transform.getMatrix(matrix);
         if (matrix[0] == 1.0 && matrix[3] == 1.0 && matrix[1] == 0.0 && matrix[2] == 0.0) {
            sb.append("translate(");
            sb.append(format(matrix[4]));
            if (matrix[5] != 0.0) {
               sb.append(',').append(format(matrix[5]));
            }
         } else {
            sb.append("matrix(");

            for(int i = 0; i < 6; ++i) {
               if (i > 0) {
                  sb.append(',');
               }

               sb.append(format(matrix[i]));
            }
         }

         sb.append(')');
         return sb;
      }
   }

   public static StringBuffer toString(AffineTransform[] transforms, StringBuffer sb) {
      int i = 0;

      for(int c = transforms.length; i < c; ++i) {
         if (i > 0) {
            sb.append(' ');
         }

         toString(transforms[i], sb);
      }

      return sb;
   }

   public static String toString(AffineTransform[] transforms) {
      return toString(transforms, new StringBuffer()).toString();
   }

   public static String toString(AffineTransform transform) {
      return toString(transform, new StringBuffer()).toString();
   }

   public static String toString(int[] coordinates) {
      if (coordinates == null) {
         return "";
      } else {
         StringBuffer sb = new StringBuffer();
         int i = 0;

         for(int c = coordinates.length; i < c; ++i) {
            if (i > 0) {
               sb.append(' ');
            }

            sb.append(Integer.toString(coordinates[i]));
         }

         return sb.toString();
      }
   }

   public static String toString(Rectangle rect) {
      if (rect == null) {
         return "";
      } else {
         StringBuffer sb = new StringBuffer();
         sb.append(rect.x).append(' ').append(rect.y).append(' ');
         sb.append(rect.width).append(' ').append(rect.height);
         return sb.toString();
      }
   }

   public static void setupFonts(IFDocumentHandler documentHandler, FontInfo fontInfo) throws FOPException {
      if (fontInfo == null) {
         fontInfo = new FontInfo();
      }

      if (documentHandler instanceof IFSerializer) {
         IFSerializer serializer = (IFSerializer)documentHandler;
         if (serializer.getMimickedDocumentHandler() != null) {
            documentHandler = serializer.getMimickedDocumentHandler();
         }
      }

      IFDocumentHandlerConfigurator configurator = documentHandler.getConfigurator();
      if (configurator != null) {
         configurator.setupFontInfo(documentHandler.getMimeType(), fontInfo);
         documentHandler.setFontInfo(fontInfo);
      } else {
         documentHandler.setDefaultFontInfo(fontInfo);
      }

   }

   public static void setupFonts(IFDocumentHandler documentHandler) throws FOPException {
      setupFonts(documentHandler, (FontInfo)null);
   }

   public static String getEffectiveMIMEType(IFDocumentHandler documentHandler) {
      if (documentHandler instanceof IFSerializer) {
         IFDocumentHandler mimic = ((IFSerializer)documentHandler).getMimickedDocumentHandler();
         if (mimic != null) {
            return mimic.getMimeType();
         }
      }

      return documentHandler.getMimeType();
   }

   public static int[] convertDPToDX(int[][] dp, int count) {
      int[] dx;
      if (dp != null) {
         dx = new int[count];
         int i = 0;

         for(int n = count; i < n; ++i) {
            if (dp[i] != null) {
               dx[i] = dp[i][0];
            }
         }
      } else {
         dx = null;
      }

      return dx;
   }

   public static int[] convertDPToDX(int[][] dp) {
      return convertDPToDX(dp, dp != null ? dp.length : 0);
   }

   public static int[][] convertDXToDP(int[] dx, int count) {
      int[][] dp;
      if (dx != null) {
         dp = new int[count][4];
         int i = 0;

         for(int n = count; i < n; ++i) {
            int[] pa = dp[i];
            int d = dx[i];
            pa[0] = d;
            pa[2] = d;
         }
      } else {
         dp = (int[][])null;
      }

      return dp;
   }

   public static int[][] convertDXToDP(int[] dx) {
      return convertDXToDP(dx, dx != null ? dx.length : 0);
   }

   public static boolean isPAIdentity(int[] pa) {
      if (pa == null) {
         return true;
      } else {
         for(int k = 0; k < 4; ++k) {
            if (pa[k] != 0) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isDPIdentity(int[][] dp) {
      if (dp == null) {
         return true;
      } else {
         int[][] var1 = dp;
         int var2 = dp.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            int[] aDp = var1[var3];
            if (!isPAIdentity(aDp)) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isDPOnlyDX(int[][] dp) {
      if (dp == null) {
         return false;
      } else {
         int[][] var1 = dp;
         int var2 = dp.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            int[] pa = var1[var3];
            if (pa != null && pa[0] != pa[2]) {
               return false;
            }
         }

         return true;
      }
   }

   public static void adjustPA(int[] paDst, int[] paSrc) {
      if (paDst != null && paSrc != null) {
         assert paDst.length == 4;

         assert paSrc.length == 4;

         for(int i = 0; i < 4; ++i) {
            paDst[i] += paSrc[i];
         }
      }

   }

   public static int[][] copyDP(int[][] dp, int offset, int count) {
      if (dp != null && offset <= dp.length && offset + count <= dp.length) {
         int[][] dpNew = new int[count][];
         int i = 0;

         for(int n = count; i < n; ++i) {
            int[] paSrc = dp[i + offset];
            if (paSrc != null) {
               int[] paDst = new int[4];
               System.arraycopy(paSrc, 0, paDst, 0, 4);
               dpNew[i] = paDst;
            }
         }

         return dpNew;
      } else {
         throw new IllegalArgumentException();
      }
   }
}
