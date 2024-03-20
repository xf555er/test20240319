package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RtfExtraRowSet extends RtfContainer {
   static final int DEFAULT_IDNUM = 0;
   private ITableColumnsInfo parentITableColumnsInfo;
   private final List cells = new LinkedList();
   private int maxRowIndex;

   RtfExtraRowSet(Writer w) throws IOException {
      super((RtfContainer)null, w);
   }

   int addTable(RtfTable tbl, int rowIndex, int xOffset) {
      Iterator var4 = tbl.getChildren().iterator();

      while(var4.hasNext()) {
         Object o = var4.next();
         RtfElement e = (RtfElement)o;
         if (e instanceof RtfTableRow) {
            this.addRow((RtfTableRow)e, rowIndex, xOffset);
            ++rowIndex;
            this.maxRowIndex = Math.max(rowIndex, this.maxRowIndex);
         }
      }

      return rowIndex;
   }

   private void addRow(RtfTableRow row, int rowIndex, int xOffset) {
      Iterator var4 = row.getChildren().iterator();

      while(var4.hasNext()) {
         Object o = var4.next();
         RtfElement e = (RtfElement)o;
         if (e instanceof RtfTableCell) {
            RtfTableCell c = (RtfTableCell)e;
            this.cells.add(new PositionedCell(c, rowIndex, xOffset));
            xOffset += c.getCellWidth();
         }
      }

   }

   RtfTableCell createExtraCell(int rowIndex, int xOffset, int cellWidth, RtfAttributes parentCellAttributes) throws IOException {
      RtfTableCell c = new RtfTableCell((RtfTableRow)null, this.writer, cellWidth, parentCellAttributes, 0);
      this.cells.add(new PositionedCell(c, rowIndex, xOffset));
      return c;
   }

   protected void writeRtfContent() throws IOException {
      Collections.sort(this.cells);
      List rowCells = null;
      int rowIndex = -1;

      PositionedCell pc;
      for(Iterator var3 = this.cells.iterator(); var3.hasNext(); rowCells.add(pc)) {
         Object cell = var3.next();
         pc = (PositionedCell)cell;
         if (pc.rowIndex != rowIndex) {
            if (rowCells != null) {
               this.writeRow(rowCells);
            }

            rowIndex = pc.rowIndex;
            rowCells = new LinkedList();
         }
      }

      if (rowCells != null) {
         this.writeRow(rowCells);
      }

   }

   private void writeRow(List cells) throws IOException {
      if (!allCellsEmpty(cells)) {
         RtfTableRow row = new RtfTableRow((RtfTable)null, this.writer, 0);
         int cellIndex = 0;
         ITableColumnsInfo parentITableColumnsInfo = this.getParentITableColumnsInfo();
         parentITableColumnsInfo.selectFirstColumn();
         float xOffset = 0.0F;
         float xOffsetOfLastPositionedCell = 0.0F;

         for(Iterator var7 = cells.iterator(); var7.hasNext(); ++cellIndex) {
            Object cell = var7.next();
            PositionedCell pc = (PositionedCell)cell;
            if (cellIndex == 0 && pc.xOffset > 0) {
               for(int i = 0; xOffset < (float)pc.xOffset && i < parentITableColumnsInfo.getNumberOfColumns(); ++i) {
                  xOffset += parentITableColumnsInfo.getColumnWidth();
                  row.newTableCellMergedVertically((int)parentITableColumnsInfo.getColumnWidth(), pc.cell.attrib);
                  parentITableColumnsInfo.selectNextColumn();
               }
            }

            row.addChild(pc.cell);
            float var10000 = (float)(pc.xOffset + pc.cell.getCellWidth());
         }

         if (parentITableColumnsInfo.getColumnIndex() < parentITableColumnsInfo.getNumberOfColumns() - 1) {
            parentITableColumnsInfo.selectNextColumn();

            while(parentITableColumnsInfo.getColumnIndex() < parentITableColumnsInfo.getNumberOfColumns()) {
               row.newTableCellMergedVertically((int)parentITableColumnsInfo.getColumnWidth(), this.attrib);
               parentITableColumnsInfo.selectNextColumn();
            }
         }

         row.writeRtf();
      }
   }

   private static boolean allCellsEmpty(List cells) {
      boolean empty = true;
      Iterator var2 = cells.iterator();

      while(var2.hasNext()) {
         Object cell = var2.next();
         PositionedCell pc = (PositionedCell)cell;
         if (pc.cell.containsText()) {
            empty = false;
            break;
         }
      }

      return empty;
   }

   public boolean isEmpty() {
      return false;
   }

   public ITableColumnsInfo getParentITableColumnsInfo() {
      return this.parentITableColumnsInfo;
   }

   public void setParentITableColumnsInfo(ITableColumnsInfo parentITableColumnsInfo) {
      this.parentITableColumnsInfo = parentITableColumnsInfo;
   }

   private static class PositionedCell implements Comparable {
      private final RtfTableCell cell;
      private final int xOffset;
      private final int rowIndex;

      PositionedCell(RtfTableCell c, int index, int offset) {
         this.cell = c;
         this.xOffset = offset;
         this.rowIndex = index;
      }

      public String toString() {
         return "PositionedCell: row " + this.rowIndex + ", offset " + this.xOffset;
      }

      public int compareTo(Object o) {
         int result = 0;
         if (o == null) {
            result = 1;
         } else if (!(o instanceof PositionedCell)) {
            result = 1;
         } else {
            PositionedCell pc = (PositionedCell)o;
            if (this.rowIndex < pc.rowIndex) {
               result = -1;
            } else if (this.rowIndex > pc.rowIndex) {
               result = 1;
            } else if (this.xOffset < pc.xOffset) {
               result = -1;
            } else if (this.xOffset > pc.xOffset) {
               result = 1;
            }
         }

         return result;
      }

      public int hashCode() {
         int hc = super.hashCode();
         hc ^= hc * 11 + this.xOffset;
         hc ^= hc * 19 + this.rowIndex;
         return hc;
      }

      public boolean equals(Object o) {
         if (!(o instanceof PositionedCell)) {
            return false;
         } else {
            PositionedCell pc = (PositionedCell)o;
            return pc.rowIndex == this.rowIndex && pc.xOffset == this.xOffset;
         }
      }
   }
}
