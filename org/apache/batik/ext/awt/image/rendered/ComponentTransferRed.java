package org.apache.batik.ext.awt.image.rendered;

import java.awt.RenderingHints;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.WritableRaster;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.TransferFunction;

public class ComponentTransferRed extends AbstractRed {
   LookupOp operation;

   public ComponentTransferRed(CachableRed src, TransferFunction[] funcs, RenderingHints hints) {
      super((CachableRed)src, src.getBounds(), GraphicsUtil.coerceColorModel(src.getColorModel(), false), src.getSampleModel(), (Map)null);
      byte[][] tableData = new byte[][]{funcs[1].getLookupTable(), funcs[2].getLookupTable(), funcs[3].getLookupTable(), funcs[0].getLookupTable()};
      this.operation = new LookupOp(new ByteLookupTable(0, tableData), hints) {
      };
   }

   public WritableRaster copyData(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      wr = src.copyData(wr);
      GraphicsUtil.coerceData(wr, src.getColorModel(), false);
      WritableRaster srcWR = wr.createWritableTranslatedChild(0, 0);
      this.operation.filter(srcWR, srcWR);
      return wr;
   }
}
