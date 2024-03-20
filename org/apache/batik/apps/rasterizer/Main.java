package org.apache.batik.apps.rasterizer;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.parser.ClockHandler;
import org.apache.batik.parser.ClockParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.util.ApplicationSecurityEnforcer;

public class Main implements SVGConverterController {
   public static final String RASTERIZER_SECURITY_POLICY = "org/apache/batik/apps/rasterizer/resources/rasterizer.policy";
   public static String USAGE = Messages.formatMessage("Main.usage", (Object[])null);
   public static String CL_OPTION_OUTPUT = Messages.get("Main.cl.option.output", "-d");
   public static String CL_OPTION_OUTPUT_DESCRIPTION = Messages.get("Main.cl.option.output.description", "No description");
   public static String CL_OPTION_MIME_TYPE = Messages.get("Main.cl.option.mime.type", "-m");
   public static String CL_OPTION_MIME_TYPE_DESCRIPTION = Messages.get("Main.cl.option.mime.type.description", "No description");
   public static String CL_OPTION_WIDTH = Messages.get("Main.cl.option.width", "-w");
   public static String CL_OPTION_WIDTH_DESCRIPTION = Messages.get("Main.cl.option.width.description", "No description");
   public static String CL_OPTION_HEIGHT = Messages.get("Main.cl.option.height", "-h");
   public static String CL_OPTION_HEIGHT_DESCRIPTION = Messages.get("Main.cl.option.height.description", "No description");
   public static String CL_OPTION_MAX_WIDTH = Messages.get("Main.cl.option.max.width", "-maxw");
   public static String CL_OPTION_MAX_WIDTH_DESCRIPTION = Messages.get("Main.cl.option.max.width.description", "No description");
   public static String CL_OPTION_MAX_HEIGHT = Messages.get("Main.cl.option.max.height", "-maxh");
   public static String CL_OPTION_MAX_HEIGHT_DESCRIPTION = Messages.get("Main.cl.option.max.height.description", "No description");
   public static String CL_OPTION_AOI = Messages.get("Main.cl.option.aoi", "-a");
   public static String CL_OPTION_AOI_DESCRIPTION = Messages.get("Main.cl.option.aoi.description", "No description");
   public static String CL_OPTION_BACKGROUND_COLOR = Messages.get("Main.cl.option.background.color", "-bg");
   public static String CL_OPTION_BACKGROUND_COLOR_DESCRIPTION = Messages.get("Main.cl.option.background.color.description", "No description");
   public static String CL_OPTION_MEDIA_TYPE = Messages.get("Main.cl.option.media.type", "-cssMedia");
   public static String CL_OPTION_MEDIA_TYPE_DESCRIPTION = Messages.get("Main.cl.option.media.type.description", "No description");
   public static String CL_OPTION_DEFAULT_FONT_FAMILY = Messages.get("Main.cl.option.default.font.family", "-font-family");
   public static String CL_OPTION_DEFAULT_FONT_FAMILY_DESCRIPTION = Messages.get("Main.cl.option.default.font.family.description", "No description");
   public static String CL_OPTION_ALTERNATE_STYLESHEET = Messages.get("Main.cl.option.alternate.stylesheet", "-cssAlternate");
   public static String CL_OPTION_ALTERNATE_STYLESHEET_DESCRIPTION = Messages.get("Main.cl.option.alternate.stylesheet.description", "No description");
   public static String CL_OPTION_VALIDATE = Messages.get("Main.cl.option.validate", "-validate");
   public static String CL_OPTION_VALIDATE_DESCRIPTION = Messages.get("Main.cl.option.validate.description", "No description");
   public static String CL_OPTION_ONLOAD = Messages.get("Main.cl.option.onload", "-onload");
   public static String CL_OPTION_ONLOAD_DESCRIPTION = Messages.get("Main.cl.option.onload.description", "No description");
   public static String CL_OPTION_SNAPSHOT_TIME = Messages.get("Main.cl.option.snapshot.time", "-snapshotTime");
   public static String CL_OPTION_SNAPSHOT_TIME_DESCRIPTION = Messages.get("Main.cl.option.snapshot.time.description", "No description");
   public static String CL_OPTION_LANGUAGE = Messages.get("Main.cl.option.language", "-lang");
   public static String CL_OPTION_LANGUAGE_DESCRIPTION = Messages.get("Main.cl.option.language.description", "No description");
   public static String CL_OPTION_USER_STYLESHEET = Messages.get("Main.cl.option.user.stylesheet", "-cssUser");
   public static String CL_OPTION_USER_STYLESHEET_DESCRIPTION = Messages.get("Main.cl.option.user.stylesheet.description", "No description");
   public static String CL_OPTION_DPI = Messages.get("Main.cl.option.dpi", "-dpi");
   public static String CL_OPTION_DPI_DESCRIPTION = Messages.get("Main.cl.option.dpi.description", "No description");
   public static String CL_OPTION_QUALITY = Messages.get("Main.cl.option.quality", "-q");
   public static String CL_OPTION_QUALITY_DESCRIPTION = Messages.get("Main.cl.option.quality.description", "No description");
   public static String CL_OPTION_INDEXED = Messages.get("Main.cl.option.indexed", "-indexed");
   public static String CL_OPTION_INDEXED_DESCRIPTION = Messages.get("Main.cl.option.indexed.description", "No description");
   public static String CL_OPTION_ALLOWED_SCRIPTS = Messages.get("Main.cl.option.allowed.scripts", "-scripts");
   public static String CL_OPTION_ALLOWED_SCRIPTS_DESCRIPTION = Messages.get("Main.cl.option.allowed.scripts.description", "No description");
   public static String CL_OPTION_CONSTRAIN_SCRIPT_ORIGIN = Messages.get("Main.cl.option.constrain.script.origin", "-anyScriptOrigin");
   public static String CL_OPTION_CONSTRAIN_SCRIPT_ORIGIN_DESCRIPTION = Messages.get("Main.cl.option.constrain.script.origin.description", "No description");
   public static String CL_OPTION_BLOCK_EXTERNAL_RESOURCES = Messages.get("Main.cl.option.block.external.resources", "-blockExternalResources");
   public static String CL_OPTION_BLOCK_EXTERNAL_RESOURCES_DESCRIPTION = Messages.get("Main.cl.option.block.external.resources.description", "No description");
   public static String CL_OPTION_SECURITY_OFF = Messages.get("Main.cl.option.security.off", "-scriptSecurityOff");
   public static String CL_OPTION_SECURITY_OFF_DESCRIPTION = Messages.get("Main.cl.option.security.off.description", "No description");
   protected static Map optionMap = new HashMap();
   protected static Map mimeTypeMap = new HashMap();
   protected List args = new ArrayList();
   public static final String ERROR_NOT_ENOUGH_OPTION_VALUES = "Main.error.not.enough.option.values";
   public static final String ERROR_ILLEGAL_ARGUMENT = "Main.error.illegal.argument";
   public static final String ERROR_WHILE_CONVERTING_FILES = "Main.error.while.converting.files";
   public static final String MESSAGE_ABOUT_TO_TRANSCODE = "Main.message.about.to.transcode";
   public static final String MESSAGE_ABOUT_TO_TRANSCODE_SOURCE = "Main.message.about.to.transcode.source";
   public static final String MESSAGE_CONVERSION_FAILED = "Main.message.conversion.failed";
   public static final String MESSAGE_CONVERSION_SUCCESS = "Main.message.conversion.success";

   public Main(String[] args) {
      String[] var2 = args;
      int var3 = args.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String arg = var2[var4];
         this.args.add(arg);
      }

   }

   protected void error(String errorCode, Object[] errorArgs) {
      System.err.println(Messages.formatMessage(errorCode, errorArgs));
   }

   public void execute() {
      SVGConverter c = new SVGConverter(this);
      List sources = new ArrayList();
      int nArgs = this.args.size();

      for(int i = 0; i < nArgs; ++i) {
         String v = (String)this.args.get(i);
         OptionHandler optionHandler = (OptionHandler)optionMap.get(v);
         if (optionHandler == null) {
            sources.add(v);
         } else {
            int nOptionArgs = optionHandler.getOptionValuesLength();
            if (i + nOptionArgs >= nArgs) {
               this.error("Main.error.not.enough.option.values", new Object[]{v, optionHandler.getOptionDescription()});
               return;
            }

            String[] optionValues = new String[nOptionArgs];

            for(int j = 0; j < nOptionArgs; ++j) {
               optionValues[j] = (String)this.args.get(1 + i + j);
            }

            i += nOptionArgs;

            try {
               optionHandler.handleOption(optionValues, c);
            } catch (IllegalArgumentException var16) {
               var16.printStackTrace();
               this.error("Main.error.illegal.argument", new Object[]{v, optionHandler.getOptionDescription(), this.toString(optionValues)});
               return;
            }
         }
      }

      ApplicationSecurityEnforcer securityEnforcer = new ApplicationSecurityEnforcer(this.getClass(), "org/apache/batik/apps/rasterizer/resources/rasterizer.policy");
      securityEnforcer.enforceSecurity(!c.getSecurityOff());
      String[] expandedSources = this.expandSources(sources);
      c.setSources(expandedSources);
      this.validateConverterConfig(c);
      if (expandedSources != null && expandedSources.length >= 1) {
         try {
            c.execute();
         } catch (SVGConverterException var14) {
            this.error("Main.error.while.converting.files", new Object[]{var14.getMessage()});
         } finally {
            System.out.flush();
            securityEnforcer.enforceSecurity(false);
         }

      } else {
         System.out.println(USAGE);
         System.out.flush();
         securityEnforcer.enforceSecurity(false);
      }
   }

   protected String toString(String[] v) {
      StringBuffer sb = new StringBuffer();
      int n = v != null ? v.length : 0;

      for(int i = 0; i < n; ++i) {
         sb.append(v[i]);
         sb.append(' ');
      }

      return sb.toString();
   }

   public void validateConverterConfig(SVGConverter c) {
   }

   protected String[] expandSources(List sources) {
      List expandedSources = new ArrayList();
      Iterator var3 = sources.iterator();

      while(true) {
         while(var3.hasNext()) {
            Object source = var3.next();
            String v = (String)source;
            File f = new File(v);
            if (f.exists() && f.isDirectory()) {
               File[] fl = f.listFiles(new SVGConverter.SVGFileFilter());
               File[] var8 = fl;
               int var9 = fl.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  File aFl = var8[var10];
                  expandedSources.add(aFl.getPath());
               }
            } else {
               expandedSources.add(v);
            }
         }

         String[] s = new String[expandedSources.size()];
         expandedSources.toArray(s);
         return s;
      }
   }

   public static void main(String[] args) {
      (new Main(args)).execute();
      System.exit(0);
   }

   public boolean proceedWithComputedTask(Transcoder transcoder, Map hints, List sources, List dest) {
      System.out.println(Messages.formatMessage("Main.message.about.to.transcode", new Object[]{"" + sources.size()}));
      return true;
   }

   public boolean proceedWithSourceTranscoding(SVGConverterSource source, File dest) {
      System.out.print(Messages.formatMessage("Main.message.about.to.transcode.source", new Object[]{source.toString(), dest.toString()}));
      return true;
   }

   public boolean proceedOnSourceTranscodingFailure(SVGConverterSource source, File dest, String errorCode) {
      System.out.println(Messages.formatMessage("Main.message.conversion.failed", new Object[]{errorCode}));
      return true;
   }

   public void onSourceTranscodingSuccess(SVGConverterSource source, File dest) {
      System.out.println(Messages.formatMessage("Main.message.conversion.success", (Object[])null));
   }

   static {
      mimeTypeMap.put("image/jpg", DestinationType.JPEG);
      mimeTypeMap.put("image/jpeg", DestinationType.JPEG);
      mimeTypeMap.put("image/jpe", DestinationType.JPEG);
      mimeTypeMap.put("image/png", DestinationType.PNG);
      mimeTypeMap.put("application/pdf", DestinationType.PDF);
      mimeTypeMap.put("image/tiff", DestinationType.TIFF);
      optionMap.put(CL_OPTION_OUTPUT, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            c.setDst(new File(optionValue));
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_OUTPUT_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_MIME_TYPE, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            DestinationType dstType = (DestinationType)Main.mimeTypeMap.get(optionValue);
            if (dstType == null) {
               throw new IllegalArgumentException();
            } else {
               c.setDestinationType(dstType);
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_MIME_TYPE_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_WIDTH, new FloatOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            if (optionValue <= 0.0F) {
               throw new IllegalArgumentException();
            } else {
               c.setWidth(optionValue);
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_WIDTH_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_HEIGHT, new FloatOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            if (optionValue <= 0.0F) {
               throw new IllegalArgumentException();
            } else {
               c.setHeight(optionValue);
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_HEIGHT_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_MAX_WIDTH, new FloatOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            if (optionValue <= 0.0F) {
               throw new IllegalArgumentException();
            } else {
               c.setMaxWidth(optionValue);
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_MAX_WIDTH_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_MAX_HEIGHT, new FloatOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            if (optionValue <= 0.0F) {
               throw new IllegalArgumentException();
            } else {
               c.setMaxHeight(optionValue);
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_MAX_HEIGHT_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_AOI, new RectangleOptionHandler() {
         public void handleOption(Rectangle2D optionValue, SVGConverter c) {
            c.setArea(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_AOI_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_BACKGROUND_COLOR, new ColorOptionHandler() {
         public void handleOption(Color optionValue, SVGConverter c) {
            c.setBackgroundColor(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_BACKGROUND_COLOR_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_MEDIA_TYPE, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            c.setMediaType(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_MEDIA_TYPE_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_DEFAULT_FONT_FAMILY, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            c.setDefaultFontFamily(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_DEFAULT_FONT_FAMILY_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_ALTERNATE_STYLESHEET, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            c.setAlternateStylesheet(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_ALTERNATE_STYLESHEET_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_USER_STYLESHEET, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            c.setUserStylesheet(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_USER_STYLESHEET_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_LANGUAGE, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            c.setLanguage(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_LANGUAGE_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_DPI, new FloatOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            if (optionValue <= 0.0F) {
               throw new IllegalArgumentException();
            } else {
               c.setPixelUnitToMillimeter(2.54F / optionValue * 10.0F);
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_DPI_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_QUALITY, new FloatOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            if (!(optionValue <= 0.0F) && !(optionValue >= 1.0F)) {
               c.setQuality(optionValue);
            } else {
               throw new IllegalArgumentException();
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_QUALITY_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_INDEXED, new FloatOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            if (optionValue != 1.0F && optionValue != 2.0F && optionValue != 4.0F && optionValue != 8.0F) {
               throw new IllegalArgumentException();
            } else {
               c.setIndexed((int)optionValue);
            }
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_INDEXED_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_VALIDATE, new NoValueOptionHandler() {
         public void handleOption(SVGConverter c) {
            c.setValidate(true);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_VALIDATE_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_ONLOAD, new NoValueOptionHandler() {
         public void handleOption(SVGConverter c) {
            c.setExecuteOnload(true);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_ONLOAD_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_SNAPSHOT_TIME, new TimeOptionHandler() {
         public void handleOption(float optionValue, SVGConverter c) {
            c.setExecuteOnload(true);
            c.setSnapshotTime(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_SNAPSHOT_TIME_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_ALLOWED_SCRIPTS, new SingleValueOptionHandler() {
         public void handleOption(String optionValue, SVGConverter c) {
            c.setAllowedScriptTypes(optionValue);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_ALLOWED_SCRIPTS_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_CONSTRAIN_SCRIPT_ORIGIN, new NoValueOptionHandler() {
         public void handleOption(SVGConverter c) {
            c.setConstrainScriptOrigin(false);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_CONSTRAIN_SCRIPT_ORIGIN_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_SECURITY_OFF, new NoValueOptionHandler() {
         public void handleOption(SVGConverter c) {
            c.setSecurityOff(true);
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_SECURITY_OFF_DESCRIPTION;
         }
      });
      optionMap.put(CL_OPTION_BLOCK_EXTERNAL_RESOURCES, new NoValueOptionHandler() {
         public void handleOption(SVGConverter c) {
            c.allowExternalResources = false;
         }

         public String getOptionDescription() {
            return Main.CL_OPTION_BLOCK_EXTERNAL_RESOURCES_DESCRIPTION;
         }
      });
   }

   public abstract static class ColorOptionHandler extends SingleValueOptionHandler {
      public void handleOption(String optionValue, SVGConverter c) {
         Color color = this.parseARGB(optionValue);
         if (color == null) {
            throw new IllegalArgumentException();
         } else {
            this.handleOption(color, c);
         }
      }

      public abstract void handleOption(Color var1, SVGConverter var2);

      public Color parseARGB(String argbVal) {
         Color c = null;
         if (argbVal != null) {
            StringTokenizer st = new StringTokenizer(argbVal, ".");
            if (st.countTokens() == 4) {
               String aStr = st.nextToken();
               String rStr = st.nextToken();
               String gStr = st.nextToken();
               String bStr = st.nextToken();
               int a = -1;
               int r = -1;
               int g = -1;
               int b = -1;

               try {
                  a = Integer.parseInt(aStr);
                  r = Integer.parseInt(rStr);
                  g = Integer.parseInt(gStr);
                  b = Integer.parseInt(bStr);
               } catch (NumberFormatException var13) {
               }

               if (a >= 0 && a <= 255 && r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                  c = new Color(r, g, b, a);
               }
            }
         }

         return c;
      }
   }

   public abstract static class RectangleOptionHandler extends SingleValueOptionHandler {
      public void handleOption(String optionValue, SVGConverter c) {
         Rectangle2D r = this.parseRect(optionValue);
         if (r == null) {
            throw new IllegalArgumentException();
         } else {
            this.handleOption((Rectangle2D)r, c);
         }
      }

      public abstract void handleOption(Rectangle2D var1, SVGConverter var2);

      public Rectangle2D.Float parseRect(String rectValue) {
         Rectangle2D.Float rect = null;
         if (rectValue != null) {
            if (!rectValue.toLowerCase().endsWith("f")) {
               rectValue = rectValue + "f";
            }

            StringTokenizer st = new StringTokenizer(rectValue, ",");
            if (st.countTokens() == 4) {
               String xStr = st.nextToken();
               String yStr = st.nextToken();
               String wStr = st.nextToken();
               String hStr = st.nextToken();
               float x = Float.NaN;
               float y = Float.NaN;
               float w = Float.NaN;
               float h = Float.NaN;

               try {
                  x = Float.parseFloat(xStr);
                  y = Float.parseFloat(yStr);
                  w = Float.parseFloat(wStr);
                  h = Float.parseFloat(hStr);
               } catch (NumberFormatException var13) {
               }

               if (!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(w) && w > 0.0F && !Float.isNaN(h) && h > 0.0F) {
                  rect = new Rectangle2D.Float(x, y, w, h);
               }
            }
         }

         return rect;
      }
   }

   public abstract static class TimeOptionHandler extends FloatOptionHandler {
      public void handleOption(String optionValue, final SVGConverter c) {
         try {
            ClockParser p = new ClockParser(false);
            p.setClockHandler(new ClockHandler() {
               public void clockValue(float v) {
                  TimeOptionHandler.this.handleOption(v, c);
               }
            });
            p.parse(optionValue);
         } catch (ParseException var4) {
            throw new IllegalArgumentException();
         }
      }

      public abstract void handleOption(float var1, SVGConverter var2);
   }

   public abstract static class FloatOptionHandler extends SingleValueOptionHandler {
      public void handleOption(String optionValue, SVGConverter c) {
         try {
            this.handleOption(Float.parseFloat(optionValue), c);
         } catch (NumberFormatException var4) {
            throw new IllegalArgumentException();
         }
      }

      public abstract void handleOption(float var1, SVGConverter var2);
   }

   public abstract static class SingleValueOptionHandler extends AbstractOptionHandler {
      public void safeHandleOption(String[] optionValues, SVGConverter c) {
         this.handleOption(optionValues[0], c);
      }

      public int getOptionValuesLength() {
         return 1;
      }

      public abstract void handleOption(String var1, SVGConverter var2);
   }

   public abstract static class NoValueOptionHandler extends AbstractOptionHandler {
      public void safeHandleOption(String[] optionValues, SVGConverter c) {
         this.handleOption(c);
      }

      public int getOptionValuesLength() {
         return 0;
      }

      public abstract void handleOption(SVGConverter var1);
   }

   public abstract static class AbstractOptionHandler implements OptionHandler {
      public void handleOption(String[] optionValues, SVGConverter c) {
         int nOptions = optionValues != null ? optionValues.length : 0;
         if (nOptions != this.getOptionValuesLength()) {
            throw new IllegalArgumentException();
         } else {
            this.safeHandleOption(optionValues, c);
         }
      }

      public abstract void safeHandleOption(String[] var1, SVGConverter var2);
   }

   public interface OptionHandler {
      void handleOption(String[] var1, SVGConverter var2);

      int getOptionValuesLength();

      String getOptionDescription();
   }
}
