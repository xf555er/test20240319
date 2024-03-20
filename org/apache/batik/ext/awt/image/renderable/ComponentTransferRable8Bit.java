package org.apache.batik.ext.awt.image.renderable;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.image.ComponentTransferFunction;
import org.apache.batik.ext.awt.image.DiscreteTransfer;
import org.apache.batik.ext.awt.image.GammaTransfer;
import org.apache.batik.ext.awt.image.IdentityTransfer;
import org.apache.batik.ext.awt.image.LinearTransfer;
import org.apache.batik.ext.awt.image.TableTransfer;
import org.apache.batik.ext.awt.image.TransferFunction;
import org.apache.batik.ext.awt.image.rendered.ComponentTransferRed;

public class ComponentTransferRable8Bit extends AbstractColorInterpolationRable implements ComponentTransferRable {
   public static final int ALPHA = 0;
   public static final int RED = 1;
   public static final int GREEN = 2;
   public static final int BLUE = 3;
   private ComponentTransferFunction[] functions = new ComponentTransferFunction[4];
   private TransferFunction[] txfFunc = new TransferFunction[4];

   public ComponentTransferRable8Bit(Filter src, ComponentTransferFunction alphaFunction, ComponentTransferFunction redFunction, ComponentTransferFunction greenFunction, ComponentTransferFunction blueFunction) {
      super((Filter)src, (Map)null);
      this.setAlphaFunction(alphaFunction);
      this.setRedFunction(redFunction);
      this.setGreenFunction(greenFunction);
      this.setBlueFunction(blueFunction);
   }

   public void setSource(Filter src) {
      this.init(src, (Map)null);
   }

   public Filter getSource() {
      return (Filter)this.getSources().get(0);
   }

   public ComponentTransferFunction getAlphaFunction() {
      return this.functions[0];
   }

   public void setAlphaFunction(ComponentTransferFunction alphaFunction) {
      this.touch();
      this.functions[0] = alphaFunction;
      this.txfFunc[0] = null;
   }

   public ComponentTransferFunction getRedFunction() {
      return this.functions[1];
   }

   public void setRedFunction(ComponentTransferFunction redFunction) {
      this.touch();
      this.functions[1] = redFunction;
      this.txfFunc[1] = null;
   }

   public ComponentTransferFunction getGreenFunction() {
      return this.functions[2];
   }

   public void setGreenFunction(ComponentTransferFunction greenFunction) {
      this.touch();
      this.functions[2] = greenFunction;
      this.txfFunc[2] = null;
   }

   public ComponentTransferFunction getBlueFunction() {
      return this.functions[3];
   }

   public void setBlueFunction(ComponentTransferFunction blueFunction) {
      this.touch();
      this.functions[3] = blueFunction;
      this.txfFunc[3] = null;
   }

   public RenderedImage createRendering(RenderContext rc) {
      RenderedImage srcRI = this.getSource().createRendering(rc);
      return srcRI == null ? null : new ComponentTransferRed(this.convertSourceCS(srcRI), this.getTransferFunctions(), rc.getRenderingHints());
   }

   private TransferFunction[] getTransferFunctions() {
      TransferFunction[] txfFunc = new TransferFunction[4];
      System.arraycopy(this.txfFunc, 0, txfFunc, 0, 4);
      ComponentTransferFunction[] functions = new ComponentTransferFunction[4];
      System.arraycopy(this.functions, 0, functions, 0, 4);

      for(int i = 0; i < 4; ++i) {
         if (txfFunc[i] == null) {
            txfFunc[i] = getTransferFunction(functions[i]);
            synchronized(this.functions) {
               if (this.functions[i] == functions[i]) {
                  this.txfFunc[i] = txfFunc[i];
               }
            }
         }
      }

      return txfFunc;
   }

   private static TransferFunction getTransferFunction(ComponentTransferFunction function) {
      TransferFunction txfFunc = null;
      if (function == null) {
         txfFunc = new IdentityTransfer();
      } else {
         switch (function.getType()) {
            case 0:
               txfFunc = new IdentityTransfer();
               break;
            case 1:
               txfFunc = new TableTransfer(tableFloatToInt(function.getTableValues()));
               break;
            case 2:
               txfFunc = new DiscreteTransfer(tableFloatToInt(function.getTableValues()));
               break;
            case 3:
               txfFunc = new LinearTransfer(function.getSlope(), function.getIntercept());
               break;
            case 4:
               txfFunc = new GammaTransfer(function.getAmplitude(), function.getExponent(), function.getOffset());
               break;
            default:
               throw new RuntimeException();
         }
      }

      return (TransferFunction)txfFunc;
   }

   private static int[] tableFloatToInt(float[] tableValues) {
      int[] values = new int[tableValues.length];

      for(int i = 0; i < tableValues.length; ++i) {
         values[i] = (int)(tableValues[i] * 255.0F);
      }

      return values;
   }
}
