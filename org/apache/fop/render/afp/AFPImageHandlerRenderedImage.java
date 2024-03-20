package org.apache.fop.render.afp;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPImageObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.util.bitmap.BitmapImageUtil;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;
import org.apache.xmlgraphics.util.UnitConv;

public class AFPImageHandlerRenderedImage extends AFPImageHandler implements ImageHandler {
   private static Log log = LogFactory.getLog(AFPImageHandlerRenderedImage.class);
   private static final ImageFlavor[] FLAVORS;

   private void setDefaultResourceLevel(AFPImageObjectInfo imageObjectInfo, AFPResourceManager resourceManager) {
      AFPResourceInfo resourceInfo = imageObjectInfo.getResourceInfo();
      if (!resourceInfo.levelChanged()) {
         resourceInfo.setLevel(resourceManager.getResourceLevelDefaults().getDefaultResourceLevel((byte)6));
      }

   }

   protected AFPDataObjectInfo createDataObjectInfo() {
      return new AFPImageObjectInfo();
   }

   public int getPriority() {
      return 300;
   }

   public Class getSupportedImageClass() {
      return ImageRendered.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      AFPRenderingContext afpContext = (AFPRenderingContext)context;
      AFPDataObjectInfo info = this.createDataObjectInfo();

      assert info instanceof AFPImageObjectInfo;

      AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)info;
      AFPPaintingState paintingState = afpContext.getPaintingState();
      imageObjectInfo.setResourceInfo(createResourceInformation(image.getInfo().getOriginalURI(), afpContext.getForeignAttributes()));
      this.setDefaultResourceLevel(imageObjectInfo, afpContext.getResourceManager());
      imageObjectInfo.setObjectAreaInfo(createObjectAreaInfo(paintingState, pos));
      Dimension targetSize = pos.getSize();
      ImageRendered imageRend = (ImageRendered)image;
      RenderedImageEncoder encoder = new RenderedImageEncoder(imageRend, targetSize);
      encoder.prepareEncoding(imageObjectInfo, paintingState);
      boolean included = afpContext.getResourceManager().tryIncludeObject(imageObjectInfo);
      if (!included) {
         long start = System.currentTimeMillis();
         encoder.encodeImage(imageObjectInfo, paintingState);
         if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Image encoding took " + duration + "ms.");
         }

         afpContext.getResourceManager().createObject(imageObjectInfo);
      }

   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      return (image == null || image instanceof ImageRendered) && targetContext instanceof AFPRenderingContext;
   }

   static {
      FLAVORS = new ImageFlavor[]{ImageFlavor.BUFFERED_IMAGE, ImageFlavor.RENDERED_IMAGE};
   }

   private static final class RenderedImageEncoder {
      private ImageRendered imageRendered;
      private Dimension targetSize;
      private boolean useFS10;
      private int maxPixelSize;
      private boolean usePageSegments;
      private boolean resample;
      private Dimension resampledDim;
      private ImageSize intrinsicSize;
      private ImageSize effIntrinsicSize;

      private RenderedImageEncoder(ImageRendered imageRendered, Dimension targetSize) {
         this.imageRendered = imageRendered;
         this.targetSize = targetSize;
      }

      private void prepareEncoding(AFPImageObjectInfo imageObjectInfo, AFPPaintingState paintingState) {
         this.maxPixelSize = paintingState.getBitsPerPixel();
         if (paintingState.isColorImages()) {
            if (paintingState.isCMYKImagesSupported()) {
               this.maxPixelSize *= 4;
            } else {
               this.maxPixelSize *= 3;
            }
         }

         RenderedImage renderedImage = this.imageRendered.getRenderedImage();
         this.useFS10 = this.maxPixelSize == 1 || BitmapImageUtil.isMonochromeImage(renderedImage);
         ImageInfo imageInfo = this.imageRendered.getInfo();
         this.intrinsicSize = imageInfo.getSize();
         this.effIntrinsicSize = this.intrinsicSize;
         this.effIntrinsicSize.setSizeInPixels(renderedImage.getWidth(), renderedImage.getHeight());
         AFPResourceInfo resourceInfo = imageObjectInfo.getResourceInfo();
         this.usePageSegments = this.useFS10 && !resourceInfo.getLevel().isInline();
         int resolution;
         if (this.usePageSegments) {
            resolution = paintingState.getResolution();
            this.resampledDim = new Dimension((int)Math.ceil(UnitConv.mpt2px(this.targetSize.getWidth(), resolution)), (int)Math.ceil(UnitConv.mpt2px(this.targetSize.getHeight(), resolution)));
            resourceInfo.setImageDimension(this.resampledDim);
            this.resample = this.resampledDim.width < renderedImage.getWidth() && this.resampledDim.height < renderedImage.getHeight();
            if (this.resample) {
               this.effIntrinsicSize = new ImageSize(this.resampledDim.width, this.resampledDim.height, (double)resolution);
            }
         }

         imageObjectInfo.setDataHeightRes((int)Math.round(this.effIntrinsicSize.getDpiHorizontal() * 10.0));
         imageObjectInfo.setDataWidthRes((int)Math.round(this.effIntrinsicSize.getDpiVertical() * 10.0));
         imageObjectInfo.setDataHeight(this.effIntrinsicSize.getHeightPx());
         imageObjectInfo.setDataWidth(this.effIntrinsicSize.getWidthPx());
         resolution = paintingState.getResolution();
         AFPObjectAreaInfo objectAreaInfo = imageObjectInfo.getObjectAreaInfo();
         objectAreaInfo.setWidthRes(resolution);
         objectAreaInfo.setHeightRes(resolution);
      }

      private AFPDataObjectInfo encodeImage(AFPImageObjectInfo imageObjectInfo, AFPPaintingState paintingState) throws IOException {
         RenderedImage renderedImage = this.imageRendered.getRenderedImage();
         FunctionSet functionSet = this.useFS10 ? AFPImageHandlerRenderedImage.RenderedImageEncoder.FunctionSet.FS10 : AFPImageHandlerRenderedImage.RenderedImageEncoder.FunctionSet.FS11;
         if (this.usePageSegments) {
            assert this.resampledDim != null;

            imageObjectInfo.setCreatePageSegment(true);
            float ditheringQuality = paintingState.getDitheringQuality();
            if (this.resample) {
               if (AFPImageHandlerRenderedImage.log.isDebugEnabled()) {
                  AFPImageHandlerRenderedImage.log.debug("Resample from " + this.intrinsicSize.getDimensionPx() + " to " + this.resampledDim);
               }

               renderedImage = BitmapImageUtil.convertToMonochrome(renderedImage, this.resampledDim, ditheringQuality);
            } else if (ditheringQuality >= 0.5F) {
               renderedImage = BitmapImageUtil.convertToMonochrome(renderedImage, this.intrinsicSize.getDimensionPx(), ditheringQuality);
            }
         }

         ColorModel cm = renderedImage.getColorModel();
         if (AFPImageHandlerRenderedImage.log.isTraceEnabled()) {
            AFPImageHandlerRenderedImage.log.trace("ColorModel: " + cm);
         }

         int pixelSize = cm.getPixelSize();
         if (cm.hasAlpha()) {
            pixelSize -= 8;
         }

         byte[] imageData = null;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         boolean allowDirectEncoding = true;
         if (allowDirectEncoding && pixelSize <= this.maxPixelSize) {
            ImageEncodingHelper helper = new ImageEncodingHelper(renderedImage, pixelSize == 32);
            ColorModel encodedColorModel = helper.getEncodedColorModel();
            boolean directEncode = true;
            if (helper.getEncodedColorModel().getPixelSize() > this.maxPixelSize) {
               directEncode = false;
            }

            if (BitmapImageUtil.getColorIndexSize(renderedImage) > 2) {
               directEncode = false;
            }

            if (this.useFS10 && BitmapImageUtil.isMonochromeImage(renderedImage) && BitmapImageUtil.isZeroBlack(renderedImage)) {
               directEncode = false;
               if (this.encodeInvertedBilevel(helper, imageObjectInfo, baos)) {
                  imageData = baos.toByteArray();
               }
            }

            if (directEncode) {
               AFPImageHandlerRenderedImage.log.debug("Encoding image directly...");
               imageObjectInfo.setBitsPerPixel(encodedColorModel.getPixelSize());
               if (pixelSize == 32) {
                  functionSet = AFPImageHandlerRenderedImage.RenderedImageEncoder.FunctionSet.FS45;
               }

               if (!paintingState.canEmbedJpeg() && paintingState.getBitmapEncodingQuality() < 1.0F) {
                  try {
                     if (AFPImageHandlerRenderedImage.log.isDebugEnabled()) {
                        AFPImageHandlerRenderedImage.log.debug("Encoding using baseline DCT (JPEG, q=" + paintingState.getBitmapEncodingQuality() + ")...");
                     }

                     this.encodeToBaselineDCT(renderedImage, paintingState.getBitmapEncodingQuality(), paintingState.getResolution(), baos);
                     imageObjectInfo.setCompression(-125);
                  } catch (IOException var14) {
                     helper.encode(baos);
                  }
               } else {
                  helper.encode(baos);
               }

               imageData = baos.toByteArray();
            }
         }

         if (imageData == null) {
            AFPImageHandlerRenderedImage.log.debug("Encoding image via RGB...");
            imageData = this.encodeViaRGB(renderedImage, imageObjectInfo, paintingState, baos);
         }

         if (paintingState.getFS45()) {
            functionSet = AFPImageHandlerRenderedImage.RenderedImageEncoder.FunctionSet.FS45;
         }

         imageObjectInfo.setCreatePageSegment((functionSet.equals(AFPImageHandlerRenderedImage.RenderedImageEncoder.FunctionSet.FS11) || functionSet.equals(AFPImageHandlerRenderedImage.RenderedImageEncoder.FunctionSet.FS45)) && paintingState.getWrapPSeg());
         imageObjectInfo.setMimeType(functionSet.getMimeType());
         imageObjectInfo.setData(imageData);
         return imageObjectInfo;
      }

      private byte[] encodeViaRGB(RenderedImage renderedImage, AFPImageObjectInfo imageObjectInfo, AFPPaintingState paintingState, ByteArrayOutputStream baos) throws IOException {
         ImageEncodingHelper.encodeRenderedImageAsRGB(renderedImage, baos);
         byte[] imageData = baos.toByteArray();
         imageObjectInfo.setBitsPerPixel(24);
         boolean colorImages = paintingState.isColorImages();
         imageObjectInfo.setColor(colorImages);
         if (!colorImages) {
            AFPImageHandlerRenderedImage.log.debug("Converting RGB image to grayscale...");
            baos.reset();
            int bitsPerPixel = paintingState.getBitsPerPixel();
            imageObjectInfo.setBitsPerPixel(bitsPerPixel);
            ImageEncodingHelper.encodeRGBAsGrayScale(imageData, renderedImage.getWidth(), renderedImage.getHeight(), bitsPerPixel, baos);
            imageData = baos.toByteArray();
            if (bitsPerPixel == 1) {
               imageObjectInfo.setSubtractive(true);
            }
         }

         return imageData;
      }

      private boolean encodeInvertedBilevel(ImageEncodingHelper helper, AFPImageObjectInfo imageObjectInfo, OutputStream out) throws IOException {
         RenderedImage renderedImage = helper.getImage();
         if (!BitmapImageUtil.isMonochromeImage(renderedImage)) {
            throw new IllegalStateException("This method only supports binary images!");
         } else {
            int tiles = renderedImage.getNumXTiles() * renderedImage.getNumYTiles();
            if (tiles > 1) {
               return false;
            } else {
               SampleModel sampleModel = renderedImage.getSampleModel();
               SampleModel expectedSampleModel = new MultiPixelPackedSampleModel(0, renderedImage.getWidth(), renderedImage.getHeight(), 1);
               if (!expectedSampleModel.equals(sampleModel)) {
                  return false;
               } else {
                  imageObjectInfo.setBitsPerPixel(1);
                  Raster raster = renderedImage.getTile(0, 0);
                  DataBuffer buffer = raster.getDataBuffer();
                  if (!(buffer instanceof DataBufferByte)) {
                     return false;
                  } else {
                     DataBufferByte byteBuffer = (DataBufferByte)buffer;
                     AFPImageHandlerRenderedImage.log.debug("Encoding image as inverted bi-level...");
                     byte[] rawData = byteBuffer.getData();
                     int remaining = rawData.length;
                     int pos = 0;

                     int size;
                     for(byte[] data = new byte[4096]; remaining > 0; remaining -= size) {
                        size = Math.min(remaining, data.length);

                        for(int i = 0; i < size; ++i) {
                           data[i] = (byte)(~rawData[pos]);
                           ++pos;
                        }

                        out.write(data, 0, size);
                     }

                     return true;
                  }
               }
            }
         }
      }

      private void encodeToBaselineDCT(RenderedImage image, float quality, int resolution, OutputStream out) throws IOException {
         ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/jpeg");
         ImageWriterParams params = new ImageWriterParams();
         params.setJPEGQuality(quality, true);
         params.setResolution(resolution);
         writer.writeImage(image, out, params);
      }

      // $FF: synthetic method
      RenderedImageEncoder(ImageRendered x0, Dimension x1, Object x2) {
         this(x0, x1);
      }

      private static enum FunctionSet {
         FS10("image/x-afp+fs10"),
         FS11("image/x-afp+fs11"),
         FS45("image/x-afp+fs45");

         private String mimeType;

         private FunctionSet(String mimeType) {
            this.mimeType = mimeType;
         }

         private String getMimeType() {
            return this.mimeType;
         }
      }
   }
}
