package org.apache.batik.util.gui.xmleditor;

import java.awt.Color;
import javax.swing.JEditorPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Element;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class XMLTextEditor extends JEditorPane {
   protected UndoManager undoManager;

   public XMLTextEditor() {
      XMLEditorKit kit = new XMLEditorKit();
      this.setEditorKitForContentType("text/xml", kit);
      this.setContentType("text/xml");
      this.setBackground(Color.white);
      this.undoManager = new UndoManager();
      UndoableEditListener undoableEditHandler = new UndoableEditListener() {
         public void undoableEditHappened(UndoableEditEvent e) {
            XMLTextEditor.this.undoManager.addEdit(e.getEdit());
         }
      };
      this.getDocument().addUndoableEditListener(undoableEditHandler);
   }

   public void setText(String t) {
      super.setText(t);
      this.undoManager.discardAllEdits();
   }

   public void undo() {
      try {
         this.undoManager.undo();
      } catch (CannotUndoException var2) {
      }

   }

   public void redo() {
      try {
         this.undoManager.redo();
      } catch (CannotRedoException var2) {
      }

   }

   public void gotoLine(int line) {
      Element element = this.getDocument().getDefaultRootElement().getElement(line);
      if (element != null) {
         int pos = element.getStartOffset();
         this.setCaretPosition(pos);
      }
   }
}
