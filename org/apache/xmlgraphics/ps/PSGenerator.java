package org.apache.xmlgraphics.ps;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;
import org.apache.xmlgraphics.util.DoubleFormatUtil;

public class PSGenerator implements PSCommandMap {
   public static final int DEFAULT_LANGUAGE_LEVEL = 3;
   /** @deprecated */
   @Deprecated
   public static final Object ATEND;
   public static final char LF = '\n';
   private static final String IDENTITY_H = "Identity-H";
   private Log log = LogFactory.getLog(this.getClass());
   private OutputStream out;
   private int psLevel = 3;
   private boolean acrobatDownsample;
   private boolean commentsEnabled = true;
   private boolean compactMode = true;
   private PSCommandMap commandMap;
   private Stack graphicsStateStack;
   private PSState currentState;
   private StringBuffer doubleBuffer;
   private StringBuffer tempBuffer;
   private boolean identityHEmbedded;
   private PSResource procsetCIDInitResource;
   private PSResource identityHCMapResource;
   private ResourceTracker resTracker;

   public PSGenerator(OutputStream out) {
      this.commandMap = PSProcSets.STD_COMMAND_MAP;
      this.graphicsStateStack = new Stack();
      this.doubleBuffer = new StringBuffer(16);
      this.tempBuffer = new StringBuffer(256);
      this.resTracker = new ResourceTracker();
      this.out = out;
      this.resetGraphicsState();
   }

   public boolean isCompactMode() {
      return this.compactMode;
   }

   public void setCompactMode(boolean value) {
      this.compactMode = value;
   }

   public boolean isCommentsEnabled() {
      return this.commentsEnabled;
   }

   public void setCommentsEnabled(boolean value) {
      this.commentsEnabled = value;
   }

   private void resetGraphicsState() {
      if (!this.graphicsStateStack.isEmpty()) {
         throw new IllegalStateException("Graphics state stack should be empty at this point");
      } else {
         this.currentState = new PSState();
      }
   }

   public OutputStream getOutputStream() {
      return this.out;
   }

   public int getPSLevel() {
      return this.psLevel;
   }

   public void setPSLevel(int level) {
      this.psLevel = level;
   }

   public boolean isAcrobatDownsample() {
      return this.acrobatDownsample;
   }

   public void setAcrobatDownsample(boolean b) {
      this.acrobatDownsample = b;
   }

   public Source resolveURI(String uri) {
      return new StreamSource(uri);
   }

   public final void newLine() throws IOException {
      this.out.write(10);
   }

   public String formatDouble(double value) {
      this.doubleBuffer.setLength(0);
      DoubleFormatUtil.formatDouble(value, 3, 3, this.doubleBuffer);
      return this.doubleBuffer.toString();
   }

   public String formatDouble5(double value) {
      this.doubleBuffer.setLength(0);
      DoubleFormatUtil.formatDouble(value, 5, 5, this.doubleBuffer);
      return this.doubleBuffer.toString();
   }

   public void write(String cmd) throws IOException {
      this.out.write(cmd.getBytes("US-ASCII"));
   }

   public void write(int n) throws IOException {
      this.write(Integer.toString(n));
   }

   public void writeln(String cmd) throws IOException {
      this.write(cmd);
      this.newLine();
   }

   public void commentln(String comment) throws IOException {
      if (this.isCommentsEnabled()) {
         this.writeln(comment);
      }

   }

   public String mapCommand(String command) {
      return this.isCompactMode() ? this.commandMap.mapCommand(command) : command;
   }

   public void writeByteArr(byte[] cmd) throws IOException {
      this.out.write(cmd);
      this.newLine();
   }

   public void flush() throws IOException {
      this.out.flush();
   }

   public static final void escapeChar(char c, StringBuffer target) {
      switch (c) {
         case '\b':
            target.append("\\b");
            break;
         case '\t':
            target.append("\\t");
            break;
         case '\n':
            target.append("\\n");
            break;
         case '\f':
            target.append("\\f");
            break;
         case '\r':
            target.append("\\r");
            break;
         case '(':
            target.append("\\(");
            break;
         case ')':
            target.append("\\)");
            break;
         case '\\':
            target.append("\\\\");
            break;
         default:
            if (c > 255) {
               target.append('?');
            } else if (c >= ' ' && c <= 127) {
               target.append(c);
            } else {
               target.append('\\');
               target.append((char)(48 + (c >> 6)));
               target.append((char)(48 + (c >> 3) % 8));
               target.append((char)(48 + c % 8));
            }
      }

   }

   public static final String convertStringToDSC(String text) {
      return convertStringToDSC(text, false);
   }

   public static final String convertRealToDSC(float value) {
      return Float.toString(value);
   }

   public static final String convertStringToDSC(String text, boolean forceParentheses) {
      if (text != null && text.length() != 0) {
         int initialSize = text.length();
         initialSize += initialSize / 2;
         StringBuffer sb = new StringBuffer(initialSize);
         int i;
         char c;
         if (text.indexOf(32) < 0 && !forceParentheses) {
            for(i = 0; i < text.length(); ++i) {
               c = text.charAt(i);
               escapeChar(c, sb);
            }

            return sb.toString();
         } else {
            sb.append('(');

            for(i = 0; i < text.length(); ++i) {
               c = text.charAt(i);
               escapeChar(c, sb);
            }

            sb.append(')');
            return sb.toString();
         }
      } else {
         return "()";
      }
   }

   public void writeDSCComment(String name) throws IOException {
      this.writeln("%%" + name);
   }

   public void writeDSCComment(String name, Object param) throws IOException {
      this.writeDSCComment(name, new Object[]{param});
   }

   public void writeDSCComment(String name, Object[] params) throws IOException {
      this.tempBuffer.setLength(0);
      this.tempBuffer.append("%%");
      this.tempBuffer.append(name);
      if (params != null && params.length > 0) {
         this.tempBuffer.append(": ");

         for(int i = 0; i < params.length; ++i) {
            if (i > 0) {
               this.tempBuffer.append(" ");
            }

            if (params[i] instanceof String) {
               this.tempBuffer.append(convertStringToDSC((String)params[i]));
            } else if (params[i] == DSCConstants.ATEND) {
               this.tempBuffer.append(DSCConstants.ATEND);
            } else if (params[i] instanceof Double) {
               this.tempBuffer.append(this.formatDouble((Double)params[i]));
            } else if (params[i] instanceof Number) {
               this.tempBuffer.append(params[i].toString());
            } else if (params[i] instanceof Date) {
               DateFormat datef = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
               this.tempBuffer.append(convertStringToDSC(datef.format((Date)params[i])));
            } else {
               if (!(params[i] instanceof PSResource)) {
                  throw new IllegalArgumentException("Unsupported parameter type: " + params[i].getClass().getName());
               }

               this.tempBuffer.append(((PSResource)params[i]).getResourceSpecification());
            }
         }
      }

      this.writeln(this.tempBuffer.toString());
   }

   public void saveGraphicsState() throws IOException {
      this.writeln(this.mapCommand("gsave"));
      PSState state = new PSState(this.currentState, false);
      this.graphicsStateStack.push(this.currentState);
      this.currentState = state;
   }

   public boolean restoreGraphicsState() throws IOException {
      if (this.graphicsStateStack.size() > 0) {
         this.writeln(this.mapCommand("grestore"));
         this.currentState = (PSState)this.graphicsStateStack.pop();
         return true;
      } else {
         return false;
      }
   }

   public PSState getCurrentState() {
      return this.currentState;
   }

   public void showPage() throws IOException {
      this.writeln("showpage");
      this.resetGraphicsState();
   }

   public void concatMatrix(double a, double b, double c, double d, double e, double f) throws IOException {
      AffineTransform at = new AffineTransform(a, b, c, d, e, f);
      this.concatMatrix(at);
   }

   public void concatMatrix(double[] matrix) throws IOException {
      this.concatMatrix(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
   }

   public String formatMatrix(AffineTransform at) {
      double[] matrix = new double[6];
      at.getMatrix(matrix);
      return "[" + this.formatDouble5(matrix[0]) + " " + this.formatDouble5(matrix[1]) + " " + this.formatDouble5(matrix[2]) + " " + this.formatDouble5(matrix[3]) + " " + this.formatDouble5(matrix[4]) + " " + this.formatDouble5(matrix[5]) + "]";
   }

   public void concatMatrix(AffineTransform at) throws IOException {
      this.getCurrentState().concatMatrix(at);
      this.writeln(this.formatMatrix(at) + " " + this.mapCommand("concat"));
   }

   public String formatRectangleToArray(Rectangle2D rect) {
      return "[" + this.formatDouble(rect.getX()) + " " + this.formatDouble(rect.getY()) + " " + this.formatDouble(rect.getWidth()) + " " + this.formatDouble(rect.getHeight()) + "]";
   }

   public void defineRect(double x, double y, double w, double h) throws IOException {
      this.writeln(this.formatDouble(x) + " " + this.formatDouble(y) + " " + this.formatDouble(w) + " " + this.formatDouble(h) + " re");
   }

   public void useLineCap(int linecap) throws IOException {
      if (this.getCurrentState().useLineCap(linecap)) {
         this.writeln(linecap + " " + this.mapCommand("setlinecap"));
      }

   }

   public void useLineJoin(int linejoin) throws IOException {
      if (this.getCurrentState().useLineJoin(linejoin)) {
         this.writeln(linejoin + " " + this.mapCommand("setlinejoin"));
      }

   }

   public void useMiterLimit(float miterlimit) throws IOException {
      if (this.getCurrentState().useMiterLimit(miterlimit)) {
         this.writeln(miterlimit + " " + this.mapCommand("setmiterlimit"));
      }

   }

   public void useLineWidth(double width) throws IOException {
      if (this.getCurrentState().useLineWidth(width)) {
         this.writeln(this.formatDouble(width) + " " + this.mapCommand("setlinewidth"));
      }

   }

   public void useDash(String pattern) throws IOException {
      if (pattern == null) {
         pattern = "[] 0";
      }

      if (this.getCurrentState().useDash(pattern)) {
         this.writeln(pattern + " " + this.mapCommand("setdash"));
      }

   }

   /** @deprecated */
   @Deprecated
   public void useRGBColor(Color col) throws IOException {
      this.useColor(col);
   }

   public void useColor(Color col) throws IOException {
      if (this.getCurrentState().useColor(col)) {
         this.writeln(this.convertColorToPS(col));
      }

   }

   private String convertColorToPS(Color color) {
      StringBuffer codeBuffer = new StringBuffer();
      boolean established = false;
      if (color instanceof ColorWithAlternatives) {
         ColorWithAlternatives colExt = (ColorWithAlternatives)color;
         Color[] alt = colExt.getAlternativeColors();
         Color[] var6 = alt;
         int var7 = alt.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Color col = var6[var8];
            established = this.establishColorFromColor(codeBuffer, col);
            if (established) {
               break;
            }
         }

         if (this.log.isDebugEnabled() && alt.length > 0) {
            this.log.debug("None of the alternative colors are supported. Using fallback: " + color);
         }
      }

      if (!established) {
         established = this.establishColorFromColor(codeBuffer, color);
      }

      if (!established) {
         this.establishFallbackRGB(codeBuffer, color);
      }

      return codeBuffer.toString();
   }

   private boolean establishColorFromColor(StringBuffer codeBuffer, Color color) {
      float[] comps = color.getColorComponents((float[])null);
      if (color.getColorSpace().getType() == 9) {
         this.writeSetColor(codeBuffer, comps, "setcmykcolor");
         return true;
      } else {
         return false;
      }
   }

   private void writeSetColor(StringBuffer codeBuffer, float[] comps, String command) {
      int i = 0;

      for(int c = comps.length; i < c; ++i) {
         if (i > 0) {
            codeBuffer.append(" ");
         }

         codeBuffer.append(this.formatDouble((double)comps[i]));
      }

      codeBuffer.append(" ").append(this.mapCommand(command));
   }

   private void establishFallbackRGB(StringBuffer codeBuffer, Color color) {
      float[] comps;
      if (color.getColorSpace().isCS_sRGB()) {
         comps = color.getColorComponents((float[])null);
      } else {
         if (this.log.isDebugEnabled()) {
            this.log.debug("Converting color to sRGB as a fallback: " + color);
         }

         ColorSpace sRGB = ColorSpace.getInstance(1000);
         comps = color.getColorComponents(sRGB, (float[])null);
      }

      assert comps.length == 3;

      boolean gray = ColorUtil.isGray(color);
      if (gray) {
         comps = new float[]{comps[0]};
      }

      this.writeSetColor(codeBuffer, comps, gray ? "setgray" : "setrgbcolor");
   }

   public void useFont(String name, float size) throws IOException {
      if (this.getCurrentState().useFont(name, size)) {
         this.writeln(name + " " + this.formatDouble((double)size) + " F");
      }

   }

   public ResourceTracker getResourceTracker() {
      return this.resTracker;
   }

   public void setResourceTracker(ResourceTracker resTracker) {
      this.resTracker = resTracker;
   }

   /** @deprecated */
   @Deprecated
   public void notifyStartNewPage() {
      this.getResourceTracker().notifyStartNewPage();
   }

   /** @deprecated */
   @Deprecated
   public void notifyResourceUsage(PSResource res, boolean needed) {
      this.getResourceTracker().notifyResourceUsageOnPage(res);
   }

   /** @deprecated */
   @Deprecated
   public void writeResources(boolean pageLevel) throws IOException {
      this.getResourceTracker().writeResources(pageLevel, this);
   }

   /** @deprecated */
   @Deprecated
   public boolean isResourceSupplied(PSResource res) {
      return this.getResourceTracker().isResourceSupplied(res);
   }

   public boolean embedIdentityH() throws IOException {
      if (this.identityHEmbedded) {
         return false;
      } else {
         this.resTracker.registerNeededResource(this.getProcsetCIDInitResource());
         this.writeDSCComment("BeginDocument", (Object)"Identity-H");
         InputStream cmap = PSGenerator.class.getResourceAsStream("Identity-H");

         try {
            IOUtils.copyLarge(cmap, this.out);
         } finally {
            IOUtils.closeQuietly(cmap);
         }

         this.writeDSCComment("EndDocument");
         this.resTracker.registerSuppliedResource(this.getIdentityHCMapResource());
         this.identityHEmbedded = true;
         return true;
      }
   }

   public PSResource getIdentityHCMapResource() {
      if (this.identityHCMapResource == null) {
         this.identityHCMapResource = new PSResource("cmap", "Identity-H");
      }

      return this.identityHCMapResource;
   }

   public PSResource getProcsetCIDInitResource() {
      if (this.procsetCIDInitResource == null) {
         this.procsetCIDInitResource = new PSResource("procset", "CIDInit");
      }

      return this.procsetCIDInitResource;
   }

   public void includeProcsetCIDInitResource() throws IOException {
      this.writeDSCComment("IncludeResource", (Object)this.getProcsetCIDInitResource());
   }

   static {
      ATEND = DSCConstants.ATEND;
   }
}
