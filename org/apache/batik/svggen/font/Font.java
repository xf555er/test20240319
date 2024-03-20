package org.apache.batik.svggen.font;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.batik.svggen.font.table.CmapTable;
import org.apache.batik.svggen.font.table.GlyfTable;
import org.apache.batik.svggen.font.table.HeadTable;
import org.apache.batik.svggen.font.table.HheaTable;
import org.apache.batik.svggen.font.table.HmtxTable;
import org.apache.batik.svggen.font.table.LocaTable;
import org.apache.batik.svggen.font.table.MaxpTable;
import org.apache.batik.svggen.font.table.NameTable;
import org.apache.batik.svggen.font.table.Os2Table;
import org.apache.batik.svggen.font.table.PostTable;
import org.apache.batik.svggen.font.table.Table;
import org.apache.batik.svggen.font.table.TableDirectory;
import org.apache.batik.svggen.font.table.TableFactory;

public class Font {
   private String path;
   private TableDirectory tableDirectory = null;
   private Table[] tables;
   private Os2Table os2;
   private CmapTable cmap;
   private GlyfTable glyf;
   private HeadTable head;
   private HheaTable hhea;
   private HmtxTable hmtx;
   private LocaTable loca;
   private MaxpTable maxp;
   private NameTable name;
   private PostTable post;

   public Table getTable(int tableType) {
      Table[] var2 = this.tables;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Table table = var2[var4];
         if (table != null && table.getType() == tableType) {
            return table;
         }
      }

      return null;
   }

   public Os2Table getOS2Table() {
      return this.os2;
   }

   public CmapTable getCmapTable() {
      return this.cmap;
   }

   public HeadTable getHeadTable() {
      return this.head;
   }

   public HheaTable getHheaTable() {
      return this.hhea;
   }

   public HmtxTable getHmtxTable() {
      return this.hmtx;
   }

   public LocaTable getLocaTable() {
      return this.loca;
   }

   public MaxpTable getMaxpTable() {
      return this.maxp;
   }

   public NameTable getNameTable() {
      return this.name;
   }

   public PostTable getPostTable() {
      return this.post;
   }

   public int getAscent() {
      return this.hhea.getAscender();
   }

   public int getDescent() {
      return this.hhea.getDescender();
   }

   public int getNumGlyphs() {
      return this.maxp.getNumGlyphs();
   }

   public Glyph getGlyph(int i) {
      return this.glyf.getDescription(i) != null ? new Glyph(this.glyf.getDescription(i), this.hmtx.getLeftSideBearing(i), this.hmtx.getAdvanceWidth(i)) : null;
   }

   public String getPath() {
      return this.path;
   }

   public TableDirectory getTableDirectory() {
      return this.tableDirectory;
   }

   protected void read(String pathName) {
      this.path = pathName;
      File f = new File(pathName);
      if (f.exists()) {
         try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            this.tableDirectory = new TableDirectory(raf);
            this.tables = new Table[this.tableDirectory.getNumTables()];

            for(int i = 0; i < this.tableDirectory.getNumTables(); ++i) {
               this.tables[i] = TableFactory.create(this.tableDirectory.getEntry(i), raf);
            }

            raf.close();
            this.os2 = (Os2Table)this.getTable(1330851634);
            this.cmap = (CmapTable)this.getTable(1668112752);
            this.glyf = (GlyfTable)this.getTable(1735162214);
            this.head = (HeadTable)this.getTable(1751474532);
            this.hhea = (HheaTable)this.getTable(1751672161);
            this.hmtx = (HmtxTable)this.getTable(1752003704);
            this.loca = (LocaTable)this.getTable(1819239265);
            this.maxp = (MaxpTable)this.getTable(1835104368);
            this.name = (NameTable)this.getTable(1851878757);
            this.post = (PostTable)this.getTable(1886352244);
            this.hmtx.init(this.hhea.getNumberOfHMetrics(), this.maxp.getNumGlyphs() - this.hhea.getNumberOfHMetrics());
            this.loca.init(this.maxp.getNumGlyphs(), this.head.getIndexToLocFormat() == 0);
            this.glyf.init(this.maxp.getNumGlyphs(), this.loca);
         } catch (IOException var5) {
            var5.printStackTrace();
         }

      }
   }

   public static Font create() {
      return new Font();
   }

   public static Font create(String pathName) {
      Font f = new Font();
      f.read(pathName);
      return f;
   }
}
