package org.apache.fop.layoutmgr.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.inline.Image;
import org.apache.fop.fo.flow.ExternalGraphic;

public class ExternalGraphicLayoutManager extends AbstractGraphicsLayoutManager {
   public ExternalGraphicLayoutManager(ExternalGraphic node) {
      super(node);
   }

   protected Area getChildArea() {
      Image im = new Image(((ExternalGraphic)this.fobj).getSrc());
      im.setChangeBarList(this.getChangeBarList());
      return im;
   }
}
