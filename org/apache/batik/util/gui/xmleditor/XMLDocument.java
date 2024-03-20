package org.apache.batik.util.gui.xmleditor;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

public class XMLDocument extends PlainDocument {
   protected XMLScanner lexer;
   protected XMLContext context;
   protected XMLToken cacheToken;

   public XMLDocument() {
      this(new XMLContext());
   }

   public XMLDocument(XMLContext context) {
      this.cacheToken = null;
      this.context = context;
      this.lexer = new XMLScanner();
   }

   public XMLToken getScannerStart(int pos) throws BadLocationException {
      int ctx = 3;
      int offset = 0;
      int tokenOffset = 0;
      int lastCtx;
      int lastOffset;
      if (this.cacheToken != null) {
         if (this.cacheToken.getStartOffset() > pos) {
            this.cacheToken = null;
         } else {
            ctx = this.cacheToken.getContext();
            offset = this.cacheToken.getStartOffset();
            tokenOffset = offset;
            Element element = this.getDefaultRootElement();
            lastCtx = element.getElementIndex(pos);
            lastOffset = element.getElementIndex(offset);
            if (lastCtx - lastOffset < 50) {
               return this.cacheToken;
            }
         }
      }

      String str = this.getText(offset, pos - offset);
      this.lexer.setString(str);
      this.lexer.reset();
      lastCtx = ctx;

      for(lastOffset = offset; offset < pos; ctx = this.lexer.getScanValue()) {
         lastOffset = offset;
         lastCtx = ctx;
         offset = this.lexer.scan(ctx) + tokenOffset;
      }

      this.cacheToken = new XMLToken(lastCtx, lastOffset, offset);
      return this.cacheToken;
   }

   public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
      super.insertString(offset, str, a);
      if (this.cacheToken != null && this.cacheToken.getStartOffset() >= offset) {
         this.cacheToken = null;
      }

   }

   public void remove(int offs, int len) throws BadLocationException {
      super.remove(offs, len);
      if (this.cacheToken != null && this.cacheToken.getStartOffset() >= offs) {
         this.cacheToken = null;
      }

   }

   public int find(String str, int fromIndex, boolean caseSensitive) throws BadLocationException {
      int offset = -1;
      int startOffset = true;
      int len = false;
      int charIndex = false;
      Element rootElement = this.getDefaultRootElement();
      int elementIndex = rootElement.getElementIndex(fromIndex);
      if (elementIndex < 0) {
         return offset;
      } else {
         int charIndex = fromIndex - rootElement.getElement(elementIndex).getStartOffset();

         for(int i = elementIndex; i < rootElement.getElementCount(); ++i) {
            Element element = rootElement.getElement(i);
            int startOffset = element.getStartOffset();
            int len;
            if (element.getEndOffset() > this.getLength()) {
               len = this.getLength() - startOffset;
            } else {
               len = element.getEndOffset() - startOffset;
            }

            String text = this.getText(startOffset, len);
            if (!caseSensitive) {
               text = text.toLowerCase();
               str = str.toLowerCase();
            }

            charIndex = text.indexOf(str, charIndex);
            if (charIndex != -1) {
               offset = startOffset + charIndex;
               break;
            }

            charIndex = 0;
         }

         return offset;
      }
   }
}
