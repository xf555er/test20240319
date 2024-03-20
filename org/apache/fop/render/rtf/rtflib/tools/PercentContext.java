package org.apache.fop.render.rtf.rtflib.tools;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.pagination.PageSequence;

public class PercentContext implements PercentBaseContext {
   private static Log log = LogFactory.getLog(PercentContext.class);
   private Map lengthMap = new HashMap();
   private Map tableUnitMap = new HashMap();
   private boolean baseWidthSet;

   public int getBaseLength(int lengthBase, FObj fobj) {
      if (fobj == null) {
         return 0;
      } else {
         if (fobj instanceof TableColumn && fobj.getParent() instanceof FObj) {
            fobj = (FObj)fobj.getParent();
         }

         switch (lengthBase) {
            case 3:
            case 4:
            case 5:
               Object width = this.lengthMap.get(fobj);
               if (width != null) {
                  return Integer.parseInt(width.toString());
               } else {
                  if (fobj.getParent() != null) {
                     width = this.lengthMap.get(fobj.getParent());
                     if (width != null) {
                        return Integer.parseInt(width.toString());
                     }
                  }

                  return 0;
               }
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            default:
               log.error(new Exception("Unsupported base type for LengthBase:" + lengthBase));
               return 0;
            case 11:
               Object unit = this.tableUnitMap.get(fobj);
               if (unit != null) {
                  return (Integer)unit;
               } else {
                  if (fobj.getParent() != null) {
                     unit = this.tableUnitMap.get(fobj.getParent());
                     if (unit != null) {
                        return (Integer)unit;
                     }
                  }

                  return 0;
               }
         }
      }
   }

   public void setDimension(FObj fobj, int width) {
      if (fobj instanceof PageSequence) {
         this.baseWidthSet = true;
      }

      this.lengthMap.put(fobj, width);
   }

   public void setTableUnit(Table table, int tableUnit) {
      this.tableUnitMap.put(table, tableUnit);
   }

   private Integer findParent(FONode fobj) {
      if (fobj.getRoot() != fobj) {
         return this.lengthMap.containsKey(fobj) ? Integer.valueOf(this.lengthMap.get(fobj).toString()) : this.findParent(fobj.getParent());
      } else {
         log.error("Base Value for element " + fobj.getName() + " not found");
         return -1;
      }
   }

   public void setDimension(FObj fobj) {
      if (this.baseWidthSet) {
         Integer width = this.findParent(fobj.getParent());
         if (width != -1) {
            this.lengthMap.put(fobj, width);
         }
      }

   }
}
