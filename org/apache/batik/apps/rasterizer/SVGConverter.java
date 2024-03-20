package org.apache.batik.apps.rasterizer;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.ParsedURL;

public class SVGConverter {
   public static final String ERROR_NO_SOURCES_SPECIFIED = "SVGConverter.error.no.sources.specified";
   public static final String ERROR_CANNOT_COMPUTE_DESTINATION = "SVGConverter.error.cannot.compute.destination";
   public static final String ERROR_CANNOT_USE_DST_FILE = "SVGConverter.error.cannot.use.dst.file";
   public static final String ERROR_CANNOT_ACCESS_TRANSCODER = "SVGConverter.error.cannot.access.transcoder";
   public static final String ERROR_SOURCE_SAME_AS_DESTINATION = "SVGConverter.error.source.same.as.destination";
   public static final String ERROR_CANNOT_READ_SOURCE = "SVGConverter.error.cannot.read.source";
   public static final String ERROR_CANNOT_OPEN_SOURCE = "SVGConverter.error.cannot.open.source";
   public static final String ERROR_OUTPUT_NOT_WRITEABLE = "SVGConverter.error.output.not.writeable";
   public static final String ERROR_CANNOT_OPEN_OUTPUT_FILE = "SVGConverter.error.cannot.open.output.file";
   public static final String ERROR_UNABLE_TO_CREATE_OUTPUT_DIR = "SVGConverter.error.unable.to.create.output.dir";
   public static final String ERROR_WHILE_RASTERIZING_FILE = "SVGConverter.error.while.rasterizing.file";
   protected static final String SVG_EXTENSION = ".svg";
   protected static final float DEFAULT_QUALITY = -1.0F;
   protected static final float MAXIMUM_QUALITY = 0.99F;
   protected static final DestinationType DEFAULT_RESULT_TYPE;
   protected static final float DEFAULT_WIDTH = -1.0F;
   protected static final float DEFAULT_HEIGHT = -1.0F;
   protected DestinationType destinationType;
   protected float height;
   protected float width;
   protected float maxHeight;
   protected float maxWidth;
   protected float quality;
   protected int indexed;
   protected Rectangle2D area;
   protected String language;
   protected String userStylesheet;
   protected float pixelUnitToMillimeter;
   protected boolean validate;
   protected boolean executeOnload;
   protected float snapshotTime;
   protected String allowedScriptTypes;
   protected boolean constrainScriptOrigin;
   protected boolean allowExternalResources;
   protected boolean securityOff;
   protected List sources;
   protected File dst;
   protected Color backgroundColor;
   protected String mediaType;
   protected String defaultFontFamily;
   protected String alternateStylesheet;
   protected List files;
   protected SVGConverterController controller;

   public SVGConverter() {
      this(new DefaultSVGConverterController());
   }

   public SVGConverter(SVGConverterController controller) {
      this.destinationType = DEFAULT_RESULT_TYPE;
      this.height = -1.0F;
      this.width = -1.0F;
      this.maxHeight = -1.0F;
      this.maxWidth = -1.0F;
      this.quality = -1.0F;
      this.indexed = -1;
      this.area = null;
      this.language = null;
      this.userStylesheet = null;
      this.pixelUnitToMillimeter = -1.0F;
      this.validate = false;
      this.executeOnload = false;
      this.snapshotTime = Float.NaN;
      this.allowedScriptTypes = null;
      this.constrainScriptOrigin = true;
      this.allowExternalResources = true;
      this.securityOff = false;
      this.sources = null;
      this.backgroundColor = null;
      this.mediaType = null;
      this.defaultFontFamily = null;
      this.alternateStylesheet = null;
      this.files = new ArrayList();
      if (controller == null) {
         throw new IllegalArgumentException();
      } else {
         this.controller = controller;
      }
   }

   public void setDestinationType(DestinationType destinationType) {
      if (destinationType == null) {
         throw new IllegalArgumentException();
      } else {
         this.destinationType = destinationType;
      }
   }

   public DestinationType getDestinationType() {
      return this.destinationType;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public float getHeight() {
      return this.height;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public float getWidth() {
      return this.width;
   }

   public void setMaxHeight(float height) {
      this.maxHeight = height;
   }

   public float getMaxHeight() {
      return this.maxHeight;
   }

   public void setMaxWidth(float width) {
      this.maxWidth = width;
   }

   public float getMaxWidth() {
      return this.maxWidth;
   }

   public void setQuality(float quality) throws IllegalArgumentException {
      if (quality >= 1.0F) {
         throw new IllegalArgumentException();
      } else {
         this.quality = quality;
      }
   }

   public float getQuality() {
      return this.quality;
   }

   public void setIndexed(int bits) throws IllegalArgumentException {
      this.indexed = bits;
   }

   public int getIndexed() {
      return this.indexed;
   }

   public void setLanguage(String language) {
      this.language = language;
   }

   public String getLanguage() {
      return this.language;
   }

   public void setUserStylesheet(String userStylesheet) {
      this.userStylesheet = userStylesheet;
   }

   public String getUserStylesheet() {
      return this.userStylesheet;
   }

   public void setPixelUnitToMillimeter(float pixelUnitToMillimeter) {
      this.pixelUnitToMillimeter = pixelUnitToMillimeter;
   }

   public float getPixelUnitToMillimeter() {
      return this.pixelUnitToMillimeter;
   }

   public void setArea(Rectangle2D area) {
      this.area = area;
   }

   public Rectangle2D getArea() {
      return this.area;
   }

   public void setSources(String[] sources) {
      if (sources == null) {
         this.sources = null;
      } else {
         this.sources = new ArrayList();
         String[] var2 = sources;
         int var3 = sources.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String source = var2[var4];
            if (source != null) {
               this.sources.add(source);
            }
         }

         if (this.sources.size() == 0) {
            this.sources = null;
         }
      }

   }

   public List getSources() {
      return this.sources;
   }

   public void setDst(File dst) {
      this.dst = dst;
   }

   public File getDst() {
      return this.dst;
   }

   public void setBackgroundColor(Color backgroundColor) {
      this.backgroundColor = backgroundColor;
   }

   public Color getBackgroundColor() {
      return this.backgroundColor;
   }

   public void setMediaType(String mediaType) {
      this.mediaType = mediaType;
   }

   public String getMediaType() {
      return this.mediaType;
   }

   public void setDefaultFontFamily(String defaultFontFamily) {
      this.defaultFontFamily = defaultFontFamily;
   }

   public String getDefaultFontFamily() {
      return this.defaultFontFamily;
   }

   public void setAlternateStylesheet(String alternateStylesheet) {
      this.alternateStylesheet = alternateStylesheet;
   }

   public String getAlternateStylesheet() {
      return this.alternateStylesheet;
   }

   public void setValidate(boolean validate) {
      this.validate = validate;
   }

   public boolean getValidate() {
      return this.validate;
   }

   public void setExecuteOnload(boolean b) {
      this.executeOnload = b;
   }

   public boolean getExecuteOnload() {
      return this.executeOnload;
   }

   public void setSnapshotTime(float t) {
      this.snapshotTime = t;
   }

   public float getSnapshotTime() {
      return this.snapshotTime;
   }

   public void setAllowedScriptTypes(String allowedScriptTypes) {
      this.allowedScriptTypes = allowedScriptTypes;
   }

   public String getAllowedScriptTypes() {
      return this.allowedScriptTypes;
   }

   public void setConstrainScriptOrigin(boolean constrainScriptOrigin) {
      this.constrainScriptOrigin = constrainScriptOrigin;
   }

   public boolean getConstrainScriptOrigin() {
      return this.constrainScriptOrigin;
   }

   public void setSecurityOff(boolean securityOff) {
      this.securityOff = securityOff;
   }

   public boolean getSecurityOff() {
      return this.securityOff;
   }

   protected boolean isFile(File f) {
      if (f.exists()) {
         return f.isFile();
      } else {
         return f.toString().toLowerCase().endsWith(this.destinationType.getExtension());
      }
   }

   public void execute() throws SVGConverterException {
      List sources = this.computeSources();
      List dstFiles = null;
      if (sources.size() == 1 && this.dst != null && this.isFile(this.dst)) {
         dstFiles = new ArrayList();
         ((List)dstFiles).add(this.dst);
      } else {
         dstFiles = this.computeDstFiles(sources);
      }

      Transcoder transcoder = this.destinationType.getTranscoder();
      if (transcoder == null) {
         throw new SVGConverterException("SVGConverter.error.cannot.access.transcoder", new Object[]{this.destinationType.toString()}, true);
      } else {
         Map hints = this.computeTranscodingHints();
         transcoder.setTranscodingHints(hints);
         if (this.controller.proceedWithComputedTask(transcoder, hints, sources, (List)dstFiles)) {
            for(int i = 0; i < sources.size(); ++i) {
               SVGConverterSource currentFile = (SVGConverterSource)sources.get(i);
               File outputFile = (File)((List)dstFiles).get(i);
               this.createOutputDir(outputFile);
               this.transcode(currentFile, outputFile, transcoder);
            }

         }
      }
   }

   protected List computeDstFiles(List sources) throws SVGConverterException {
      List dstFiles = new ArrayList();
      int n;
      Iterator var4;
      Object source;
      SVGConverterSource src;
      if (this.dst != null) {
         if (this.dst.exists() && this.dst.isFile()) {
            throw new SVGConverterException("SVGConverter.error.cannot.use.dst.file");
         }

         n = sources.size();
         var4 = sources.iterator();

         while(var4.hasNext()) {
            source = var4.next();
            src = (SVGConverterSource)source;
            File outputName = new File(this.dst.getPath(), this.getDestinationFile(src.getName()));
            dstFiles.add(outputName);
         }
      } else {
         n = sources.size();
         var4 = sources.iterator();

         while(var4.hasNext()) {
            source = var4.next();
            src = (SVGConverterSource)source;
            if (!(src instanceof SVGConverterFileSource)) {
               throw new SVGConverterException("SVGConverter.error.cannot.compute.destination", new Object[]{src});
            }

            SVGConverterFileSource fs = (SVGConverterFileSource)src;
            File outputName = new File(fs.getFile().getParent(), this.getDestinationFile(src.getName()));
            dstFiles.add(outputName);
         }
      }

      return dstFiles;
   }

   protected List computeSources() throws SVGConverterException {
      List sources = new ArrayList();
      if (this.sources == null) {
         throw new SVGConverterException("SVGConverter.error.no.sources.specified");
      } else {
         int n = this.sources.size();
         Iterator var3 = this.sources.iterator();

         while(var3.hasNext()) {
            Object source = var3.next();
            String sourceString = (String)source;
            File file = new File(sourceString);
            if (file.exists()) {
               sources.add(new SVGConverterFileSource(file));
            } else {
               String[] fileNRef = this.getFileNRef(sourceString);
               file = new File(fileNRef[0]);
               if (file.exists()) {
                  sources.add(new SVGConverterFileSource(file, fileNRef[1]));
               } else {
                  sources.add(new SVGConverterURLSource(sourceString));
               }
            }
         }

         return sources;
      }
   }

   public String[] getFileNRef(String fileName) {
      int n = fileName.lastIndexOf(35);
      String[] result = new String[]{fileName, ""};
      if (n > -1) {
         result[0] = fileName.substring(0, n);
         if (n + 1 < fileName.length()) {
            result[1] = fileName.substring(n + 1);
         }
      }

      return result;
   }

   protected Map computeTranscodingHints() {
      Map map = new HashMap();
      if (this.area != null) {
         map.put(ImageTranscoder.KEY_AOI, this.area);
      }

      if (this.quality > 0.0F) {
         map.put(JPEGTranscoder.KEY_QUALITY, this.quality);
      }

      if (this.indexed != -1) {
         map.put(PNGTranscoder.KEY_INDEXED, this.indexed);
      }

      if (this.backgroundColor != null) {
         map.put(ImageTranscoder.KEY_BACKGROUND_COLOR, this.backgroundColor);
      }

      if (this.height > 0.0F) {
         map.put(ImageTranscoder.KEY_HEIGHT, this.height);
      }

      if (this.width > 0.0F) {
         map.put(ImageTranscoder.KEY_WIDTH, this.width);
      }

      if (this.maxHeight > 0.0F) {
         map.put(ImageTranscoder.KEY_MAX_HEIGHT, this.maxHeight);
      }

      if (this.maxWidth > 0.0F) {
         map.put(ImageTranscoder.KEY_MAX_WIDTH, this.maxWidth);
      }

      if (this.mediaType != null) {
         map.put(ImageTranscoder.KEY_MEDIA, this.mediaType);
      }

      if (this.defaultFontFamily != null) {
         map.put(ImageTranscoder.KEY_DEFAULT_FONT_FAMILY, this.defaultFontFamily);
      }

      if (this.alternateStylesheet != null) {
         map.put(ImageTranscoder.KEY_ALTERNATE_STYLESHEET, this.alternateStylesheet);
      }

      if (this.userStylesheet != null) {
         String userStylesheetURL;
         try {
            URL userDir = (new File(System.getProperty("user.dir"))).toURI().toURL();
            userStylesheetURL = (new ParsedURL(userDir, this.userStylesheet)).toString();
         } catch (Exception var4) {
            userStylesheetURL = this.userStylesheet;
         }

         map.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, userStylesheetURL);
      }

      if (this.language != null) {
         map.put(ImageTranscoder.KEY_LANGUAGE, this.language);
      }

      if (this.pixelUnitToMillimeter > 0.0F) {
         map.put(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, this.pixelUnitToMillimeter);
      }

      if (this.validate) {
         map.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.TRUE);
      }

      if (this.executeOnload) {
         map.put(ImageTranscoder.KEY_EXECUTE_ONLOAD, Boolean.TRUE);
      }

      if (!Float.isNaN(this.snapshotTime)) {
         map.put(ImageTranscoder.KEY_SNAPSHOT_TIME, this.snapshotTime);
      }

      if (this.allowedScriptTypes != null) {
         map.put(ImageTranscoder.KEY_ALLOWED_SCRIPT_TYPES, this.allowedScriptTypes);
      }

      if (!this.constrainScriptOrigin) {
         map.put(ImageTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, Boolean.FALSE);
      }

      if (!this.allowExternalResources) {
         map.put(ImageTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, Boolean.FALSE);
      }

      return map;
   }

   protected void transcode(SVGConverterSource inputFile, File outputFile, Transcoder transcoder) throws SVGConverterException {
      TranscoderInput input = null;
      TranscoderOutput output = null;
      OutputStream outputStream = null;
      if (this.controller.proceedWithSourceTranscoding(inputFile, outputFile)) {
         try {
            if (inputFile.isSameAs(outputFile.getPath())) {
               throw new SVGConverterException("SVGConverter.error.source.same.as.destination", true);
            }

            if (!inputFile.isReadable()) {
               throw new SVGConverterException("SVGConverter.error.cannot.read.source", new Object[]{inputFile.getName()});
            }

            try {
               InputStream in = inputFile.openStream();
               in.close();
            } catch (IOException var14) {
               throw new SVGConverterException("SVGConverter.error.cannot.open.source", new Object[]{inputFile.getName(), var14.toString()});
            }

            input = new TranscoderInput(inputFile.getURI());
            if (!this.isWriteable(outputFile)) {
               throw new SVGConverterException("SVGConverter.error.output.not.writeable", new Object[]{outputFile.getName()});
            }

            try {
               outputStream = new FileOutputStream(outputFile);
            } catch (FileNotFoundException var13) {
               throw new SVGConverterException("SVGConverter.error.cannot.open.output.file", new Object[]{outputFile.getName()});
            }

            output = new TranscoderOutput(outputStream);
         } catch (SVGConverterException var15) {
            boolean proceed = this.controller.proceedOnSourceTranscodingFailure(inputFile, outputFile, var15.getErrorCode());
            if (proceed) {
               var15.printStackTrace();
               return;
            }

            throw var15;
         }

         boolean success = false;

         try {
            transcoder.transcode(input, output);
            success = true;
         } catch (Exception var12) {
            var12.printStackTrace();

            try {
               outputStream.flush();
               outputStream.close();
            } catch (IOException var11) {
            }

            boolean proceed = this.controller.proceedOnSourceTranscodingFailure(inputFile, outputFile, "SVGConverter.error.while.rasterizing.file");
            if (!proceed) {
               throw new SVGConverterException("SVGConverter.error.while.rasterizing.file", new Object[]{outputFile.getName(), var12.getMessage()});
            }
         }

         try {
            outputStream.flush();
            outputStream.close();
         } catch (IOException var10) {
            return;
         }

         if (success) {
            this.controller.onSourceTranscodingSuccess(inputFile, outputFile);
         }

      }
   }

   protected String getDestinationFile(String file) {
      String newSuffix = this.destinationType.getExtension();
      int suffixStart = file.lastIndexOf(46);
      String dest = null;
      if (suffixStart != -1) {
         dest = file.substring(0, suffixStart) + newSuffix;
      } else {
         dest = file + newSuffix;
      }

      return dest;
   }

   protected void createOutputDir(File output) throws SVGConverterException {
      boolean success = true;
      String parentDir = output.getParent();
      if (parentDir != null) {
         File outputDir = new File(output.getParent());
         if (!outputDir.exists()) {
            success = outputDir.mkdirs();
         } else if (!outputDir.isDirectory()) {
            success = outputDir.mkdirs();
         }
      }

      if (!success) {
         throw new SVGConverterException("SVGConverter.error.unable.to.create.output.dir");
      }
   }

   protected boolean isWriteable(File file) {
      if (file.exists()) {
         if (!file.canWrite()) {
            return false;
         }
      } else {
         try {
            file.createNewFile();
         } catch (IOException var3) {
            return false;
         }
      }

      return true;
   }

   static {
      DEFAULT_RESULT_TYPE = DestinationType.PNG;
   }

   public static class SVGFileFilter implements FileFilter {
      public static final String SVG_EXTENSION = ".svg";

      public boolean accept(File file) {
         return file != null && file.getName().toLowerCase().endsWith(".svg");
      }
   }
}
