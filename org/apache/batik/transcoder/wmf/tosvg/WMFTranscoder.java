package org.apache.batik.transcoder.wmf.tosvg;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.ToSVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WMFTranscoder extends ToSVGAbstractTranscoder {
   public static final String WMF_EXTENSION = ".wmf";
   public static final String SVG_EXTENSION = ".svg";

   public void transcode(TranscoderInput input, TranscoderOutput output) throws TranscoderException {
      DataInputStream is = this.getCompatibleInput(input);
      WMFRecordStore currentStore = new WMFRecordStore();

      try {
         currentStore.read(is);
      } catch (IOException var20) {
         this.handler.fatalError(new TranscoderException(var20));
         return;
      }

      float conv = 1.0F;
      float wmfwidth;
      float wmfheight;
      if (this.hints.containsKey(KEY_INPUT_WIDTH)) {
         wmfwidth = (float)(Integer)this.hints.get(KEY_INPUT_WIDTH);
         wmfheight = (float)(Integer)this.hints.get(KEY_INPUT_HEIGHT);
      } else {
         wmfwidth = (float)currentStore.getWidthPixels();
         wmfheight = (float)currentStore.getHeightPixels();
      }

      if (this.hints.containsKey(KEY_WIDTH)) {
         float width = (Float)this.hints.get(KEY_WIDTH);
         conv = width / wmfwidth;
         float height = wmfheight * width / wmfwidth;
      }

      int xOffset = 0;
      int yOffset = 0;
      if (this.hints.containsKey(KEY_XOFFSET)) {
         xOffset = (Integer)this.hints.get(KEY_XOFFSET);
      }

      if (this.hints.containsKey(KEY_YOFFSET)) {
         yOffset = (Integer)this.hints.get(KEY_YOFFSET);
      }

      float sizeFactor = currentStore.getUnitsToPixels() * conv;
      int vpX = (int)(currentStore.getVpX() * sizeFactor);
      int vpY = (int)(currentStore.getVpY() * sizeFactor);
      int vpW;
      int vpH;
      if (this.hints.containsKey(KEY_INPUT_WIDTH)) {
         vpW = (int)((float)(Integer)this.hints.get(KEY_INPUT_WIDTH) * conv);
         vpH = (int)((float)(Integer)this.hints.get(KEY_INPUT_HEIGHT) * conv);
      } else {
         vpW = (int)((float)currentStore.getWidthUnits() * sizeFactor);
         vpH = (int)((float)currentStore.getHeightUnits() * sizeFactor);
      }

      WMFPainter painter = new WMFPainter(currentStore, xOffset, yOffset, conv);
      Document doc = this.createDocument(output);
      this.svgGenerator = new SVGGraphics2D(doc);
      this.svgGenerator.getGeneratorContext().setPrecision(4);
      painter.paint(this.svgGenerator);
      this.svgGenerator.setSVGCanvasSize(new Dimension(vpW, vpH));
      Element svgRoot = this.svgGenerator.getRoot();
      svgRoot.setAttributeNS((String)null, "viewBox", String.valueOf(vpX) + ' ' + vpY + ' ' + vpW + ' ' + vpH);
      this.writeSVGToOutput(this.svgGenerator, svgRoot, output);
   }

   private DataInputStream getCompatibleInput(TranscoderInput input) throws TranscoderException {
      if (input == null) {
         this.handler.fatalError(new TranscoderException(String.valueOf(65280)));
      }

      InputStream in = input.getInputStream();
      if (in != null) {
         return new DataInputStream(new BufferedInputStream(in));
      } else {
         String uri = input.getURI();
         if (uri != null) {
            try {
               URL url = new URL(uri);
               in = url.openStream();
               return new DataInputStream(new BufferedInputStream(in));
            } catch (MalformedURLException var5) {
               this.handler.fatalError(new TranscoderException(var5));
            } catch (IOException var6) {
               this.handler.fatalError(new TranscoderException(var6));
            }
         }

         this.handler.fatalError(new TranscoderException(String.valueOf(65281)));
         return null;
      }
   }

   public static void main(String[] args) throws TranscoderException {
      if (args.length < 1) {
         System.out.println("Usage : WMFTranscoder.main <file 1> ... <file n>");
         System.exit(1);
      }

      WMFTranscoder transcoder = new WMFTranscoder();
      int nFiles = args.length;
      String[] var3 = args;
      int var4 = args.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String fileName = var3[var5];
         if (!fileName.toLowerCase().endsWith(".wmf")) {
            System.err.println(fileName + " does not have the " + ".wmf" + " extension. It is ignored");
         } else {
            System.out.print("Processing : " + fileName + "...");
            String outputFileName = fileName.substring(0, fileName.toLowerCase().indexOf(".wmf")) + ".svg";
            File inputFile = new File(fileName);
            File outputFile = new File(outputFileName);

            try {
               TranscoderInput input = new TranscoderInput(inputFile.toURI().toURL().toString());
               TranscoderOutput output = new TranscoderOutput(new FileOutputStream(outputFile));
               transcoder.transcode(input, output);
            } catch (MalformedURLException var12) {
               throw new TranscoderException(var12);
            } catch (IOException var13) {
               throw new TranscoderException(var13);
            }

            System.out.println(".... Done");
         }
      }

      System.exit(0);
   }
}
