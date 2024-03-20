package org.apache.batik.apps.rasterizer;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.batik.transcoder.Transcoder;

public class DefaultSVGConverterController implements SVGConverterController {
   public boolean proceedWithComputedTask(Transcoder transcoder, Map hints, List sources, List dest) {
      return true;
   }

   public boolean proceedWithSourceTranscoding(SVGConverterSource source, File dest) {
      return true;
   }

   public boolean proceedOnSourceTranscodingFailure(SVGConverterSource source, File dest, String errorCode) {
      return true;
   }

   public void onSourceTranscodingSuccess(SVGConverterSource source, File dest) {
   }
}
