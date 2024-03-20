package org.apache.batik.util.gui.xmleditor;

import java.awt.Graphics;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;

public class XMLView extends PlainView {
   protected XMLContext context = null;
   protected XMLScanner lexer = new XMLScanner();
   protected int tabSize = 4;

   public XMLView(XMLContext context, Element elem) {
      super(elem);
      this.context = context;
   }

   public int getTabSize() {
      return this.tabSize;
   }

   protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
      XMLDocument doc = (XMLDocument)this.getDocument();
      XMLToken token = doc.getScannerStart(p0);
      String str = doc.getText(token.getStartOffset(), p1 - token.getStartOffset() + 1);
      this.lexer.setString(str);
      this.lexer.reset();
      int pos = token.getStartOffset();
      int ctx = token.getContext();

      int lastCtx;
      for(lastCtx = ctx; pos < p0; ctx = this.lexer.getScanValue()) {
         pos = this.lexer.scan(ctx) + token.getStartOffset();
         lastCtx = ctx;
      }

      int mark;
      Segment text;
      for(mark = p0; pos < p1; ctx = this.lexer.getScanValue()) {
         if (lastCtx != ctx) {
            g.setColor(this.context.getSyntaxForeground(lastCtx));
            g.setFont(this.context.getSyntaxFont(lastCtx));
            text = this.getLineBuffer();
            doc.getText(mark, pos - mark, text);
            x = Utilities.drawTabbedText(text, x, y, g, this, mark);
            mark = pos;
         }

         pos = this.lexer.scan(ctx) + token.getStartOffset();
         lastCtx = ctx;
      }

      g.setColor(this.context.getSyntaxForeground(lastCtx));
      g.setFont(this.context.getSyntaxFont(lastCtx));
      text = this.getLineBuffer();
      doc.getText(mark, p1 - mark, text);
      x = Utilities.drawTabbedText(text, x, y, g, this, mark);
      return x;
   }
}
