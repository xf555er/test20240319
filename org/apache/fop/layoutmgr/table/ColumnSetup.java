package org.apache.fop.layoutmgr.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.RelativeNumericProperty;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.properties.TableColLength;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;

public class ColumnSetup {
   private static Log log = LogFactory.getLog(ColumnSetup.class);
   private Table table;
   private WritingModeTraitsGetter wmTraits;
   private List columns = new ArrayList();
   private List colWidths = new ArrayList();
   private int maxColIndexReferenced;

   public ColumnSetup(Table table) {
      assert table != null;

      this.table = table;
      this.wmTraits = WritingModeTraits.getWritingModeTraitsGetter(table);
      this.prepareColumns();
      this.initializeColumnWidths();
   }

   private void prepareColumns() {
      List rawCols = this.table.getColumns();
      if (rawCols != null) {
         int colnum = true;
         Iterator var3 = rawCols.iterator();

         label47:
         while(true) {
            TableColumn col;
            do {
               if (!var3.hasNext()) {
                  int pos = 1;

                  for(Iterator var9 = this.columns.iterator(); var9.hasNext(); ++pos) {
                     Object column = var9.next();
                     TableColumn col = (TableColumn)column;

                     assert col != null;
                  }
                  break label47;
               }

               Object rawCol = var3.next();
               col = (TableColumn)rawCol;
            } while(col == null);

            int colnum = col.getColumnNumber();

            for(int i = 0; i < col.getNumberColumnsRepeated(); ++i) {
               while(colnum > this.columns.size()) {
                  this.columns.add((Object)null);
               }

               this.columns.set(colnum - 1, col);
               ++colnum;
            }
         }
      }

   }

   public TableColumn getColumn(int index) {
      int size = this.columns.size();
      if (index <= size) {
         return (TableColumn)this.columns.get(index - 1);
      } else {
         if (index > this.maxColIndexReferenced) {
            this.maxColIndexReferenced = index;
            TableColumn col = this.getColumn(1);
            if (size != 1 || !col.isImplicitColumn()) {
               assert false;

               log.warn(FONode.decorateWithContextInfo("There are fewer table-columns than are needed. Column " + index + " was accessed, but only " + size + " columns have been defined. The last defined column will be reused.", this.table));
               if (!this.table.isAutoLayout()) {
                  log.warn("Please note that according XSL-FO 1.0 (7.26.9) says that the 'column-width' property must be specified for every column, unless the automatic table layout is used.");
               }
            }
         }

         return (TableColumn)this.columns.get(size - 1);
      }
   }

   public String toString() {
      return this.columns.toString();
   }

   public int getColumnCount() {
      return this.maxColIndexReferenced > this.columns.size() ? this.maxColIndexReferenced : this.columns.size();
   }

   public Iterator iterator() {
      return this.columns.iterator();
   }

   private void initializeColumnWidths() {
      int i = this.columns.size();

      while(true) {
         --i;
         if (i < 0) {
            this.colWidths.add(0, (Object)null);
            return;
         }

         if (this.columns.get(i) != null) {
            TableColumn col = (TableColumn)this.columns.get(i);
            Length colWidth = col.getColumnWidth();
            this.colWidths.add(0, colWidth);
         }
      }
   }

   protected double computeTableUnit(TableLayoutManager tlm) {
      return (double)this.computeTableUnit(tlm, tlm.getContentAreaIPD());
   }

   public float computeTableUnit(PercentBaseContext percentBaseContext, int contentAreaIPD) {
      int sumCols = 0;
      float factors = 0.0F;
      float unit = 0.0F;
      Iterator var6 = this.colWidths.iterator();

      while(var6.hasNext()) {
         Object colWidth1 = var6.next();
         Length colWidth = (Length)colWidth1;
         if (colWidth != null) {
            sumCols += colWidth.getValue(percentBaseContext);
            if (colWidth instanceof RelativeNumericProperty) {
               factors = (float)((double)factors + ((RelativeNumericProperty)colWidth).getTableUnits());
            } else if (colWidth instanceof TableColLength) {
               factors = (float)((double)factors + ((TableColLength)colWidth).getTableUnits());
            }
         }
      }

      if (factors > 0.0F) {
         if (sumCols < contentAreaIPD) {
            unit = (float)(contentAreaIPD - sumCols) / factors;
         } else {
            log.warn("No space remaining to distribute over columns.");
         }
      }

      return unit;
   }

   public int getXOffset(int col, int nrColSpan, PercentBaseContext context) {
      return this.wmTraits != null && this.wmTraits.getColumnProgressionDirection() == Direction.RL ? this.getXOffsetRTL(col, nrColSpan, context) : this.getXOffsetLTR(col, context);
   }

   private int getXOffsetRTL(int col, int nrColSpan, PercentBaseContext context) {
      int xoffset = 0;
      int i = col + nrColSpan - 1;
      int nc = this.colWidths.size();

      while(true) {
         ++i;
         if (i >= nc) {
            return xoffset;
         }

         if (this.colWidths.get(i) != null) {
            xoffset += ((Length)this.colWidths.get(i)).getValue(context);
         }
      }
   }

   private int getXOffsetLTR(int col, PercentBaseContext context) {
      int xoffset = 0;
      int i = col;

      while(true) {
         --i;
         if (i < 0) {
            return xoffset;
         }

         int effCol;
         if (i < this.colWidths.size()) {
            effCol = i;
         } else {
            effCol = this.colWidths.size() - 1;
         }

         if (this.colWidths.get(effCol) != null) {
            xoffset += ((Length)this.colWidths.get(effCol)).getValue(context);
         }
      }
   }

   public int getSumOfColumnWidths(PercentBaseContext context) {
      int sum = 0;
      int i = 1;

      for(int c = this.getColumnCount(); i <= c; ++i) {
         int effIndex = i;
         if (i >= this.colWidths.size()) {
            effIndex = this.colWidths.size() - 1;
         }

         if (this.colWidths.get(effIndex) != null) {
            sum += ((Length)this.colWidths.get(effIndex)).getValue(context);
         }
      }

      return sum;
   }
}
