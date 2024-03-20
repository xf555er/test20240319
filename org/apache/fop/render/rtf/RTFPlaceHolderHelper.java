package org.apache.fop.render.rtf;

import java.io.IOException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.render.rtf.rtflib.exceptions.RtfException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.tools.BuilderContext;

public class RTFPlaceHolderHelper {
   private BuilderContext builderContext;

   public RTFPlaceHolderHelper(BuilderContext builderContext) {
      this.builderContext = builderContext;
   }

   public void createRTFPlaceholder(Class containerClass) throws RtfException {
      if (containerClass == RtfTableRow.class) {
         this.createRtfTableRow();
      }

   }

   private void createRtfTableRow() throws RtfException {
      try {
         RtfContainer element = this.builderContext.getContainer(RtfTable.class, true, (Object)null);
         if (element != null && element instanceof RtfTable) {
            RtfTable table = (RtfTable)element;
            RtfAttributes attribs = new RtfAttributes();
            RtfTableRow newRow = table.newTableRow(attribs);
            this.builderContext.pushContainer(newRow);
            this.builderContext.getTableContext().selectFirstColumn();
         }

      } catch (FOPException var5) {
         throw new RtfException(var5.getMessage());
      } catch (IOException var6) {
         throw new RtfException(var6.getMessage());
      }
   }
}
