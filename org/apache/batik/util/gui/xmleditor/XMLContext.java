package org.apache.batik.util.gui.xmleditor;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.StyleContext;

public class XMLContext extends StyleContext {
   public static final String XML_DECLARATION_STYLE = "xml_declaration";
   public static final String DOCTYPE_STYLE = "doctype";
   public static final String COMMENT_STYLE = "comment";
   public static final String ELEMENT_STYLE = "element";
   public static final String CHARACTER_DATA_STYLE = "character_data";
   public static final String ATTRIBUTE_NAME_STYLE = "attribute_name";
   public static final String ATTRIBUTE_VALUE_STYLE = "attribute_value";
   public static final String CDATA_STYLE = "cdata";
   protected Map syntaxForegroundMap = null;
   protected Map syntaxFontMap = null;

   public XMLContext() {
      this.syntaxFontMap = new HashMap();
      this.syntaxForegroundMap = new HashMap();
      Font defaultFont = new Font("Monospaced", 0, 12);
      String syntaxName = "default";
      Color fontForeground = Color.black;
      this.syntaxFontMap.put(syntaxName, defaultFont);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "xml_declaration";
      Font font = defaultFont.deriveFont(1);
      fontForeground = new Color(0, 0, 124);
      this.syntaxFontMap.put(syntaxName, font);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "doctype";
      font = defaultFont.deriveFont(1);
      fontForeground = new Color(0, 0, 124);
      this.syntaxFontMap.put(syntaxName, font);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "comment";
      fontForeground = new Color(128, 128, 128);
      this.syntaxFontMap.put(syntaxName, defaultFont);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "element";
      fontForeground = new Color(0, 0, 255);
      this.syntaxFontMap.put(syntaxName, defaultFont);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "character_data";
      fontForeground = Color.black;
      this.syntaxFontMap.put(syntaxName, defaultFont);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "attribute_name";
      fontForeground = new Color(0, 124, 0);
      this.syntaxFontMap.put(syntaxName, defaultFont);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "attribute_value";
      fontForeground = new Color(153, 0, 107);
      this.syntaxFontMap.put(syntaxName, defaultFont);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
      syntaxName = "cdata";
      fontForeground = new Color(124, 98, 0);
      this.syntaxFontMap.put(syntaxName, defaultFont);
      this.syntaxForegroundMap.put(syntaxName, fontForeground);
   }

   public XMLContext(Map syntaxFontMap, Map syntaxForegroundMap) {
      this.setSyntaxFont(syntaxFontMap);
      this.setSyntaxForeground(syntaxForegroundMap);
   }

   public void setSyntaxForeground(Map syntaxForegroundMap) {
      if (syntaxForegroundMap == null) {
         throw new IllegalArgumentException("syntaxForegroundMap can not be null");
      } else {
         this.syntaxForegroundMap = syntaxForegroundMap;
      }
   }

   public void setSyntaxFont(Map syntaxFontMap) {
      if (syntaxFontMap == null) {
         throw new IllegalArgumentException("syntaxFontMap can not be null");
      } else {
         this.syntaxFontMap = syntaxFontMap;
      }
   }

   public Color getSyntaxForeground(int ctx) {
      String name = this.getSyntaxName(ctx);
      return this.getSyntaxForeground(name);
   }

   public Color getSyntaxForeground(String name) {
      return (Color)this.syntaxForegroundMap.get(name);
   }

   public Font getSyntaxFont(int ctx) {
      String name = this.getSyntaxName(ctx);
      return this.getSyntaxFont(name);
   }

   public Font getSyntaxFont(String name) {
      return (Font)this.syntaxFontMap.get(name);
   }

   public String getSyntaxName(int ctx) {
      String name = "character_data";
      switch (ctx) {
         case 1:
            name = "comment";
            break;
         case 2:
            name = "element";
            break;
         case 3:
         case 8:
         case 9:
         default:
            name = "default";
            break;
         case 4:
            name = "attribute_name";
            break;
         case 5:
            name = "attribute_value";
            break;
         case 6:
            name = "xml_declaration";
            break;
         case 7:
            name = "doctype";
            break;
         case 10:
            name = "cdata";
      }

      return name;
   }
}
