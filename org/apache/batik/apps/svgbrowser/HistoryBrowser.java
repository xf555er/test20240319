package org.apache.batik.apps.svgbrowser;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.event.EventListenerList;

public class HistoryBrowser {
   public static final int EXECUTING = 1;
   public static final int UNDOING = 2;
   public static final int REDOING = 3;
   public static final int IDLE = 4;
   protected EventListenerList eventListeners = new EventListenerList();
   protected ArrayList history = new ArrayList();
   protected int currentCommandIndex = -1;
   protected int historySize = 1000;
   protected int state = 4;
   protected CommandController commandController;

   public HistoryBrowser(CommandController commandController) {
      this.commandController = commandController;
   }

   public HistoryBrowser(int historySize) {
      this.setHistorySize(historySize);
   }

   protected void setHistorySize(int size) {
      this.historySize = size;
   }

   public void setCommandController(CommandController newCommandController) {
      this.commandController = newCommandController;
   }

   public void addCommand(UndoableCommand command) {
      int n = this.history.size();

      for(int i = n - 1; i > this.currentCommandIndex; --i) {
         this.history.remove(i);
      }

      if (this.commandController != null) {
         this.commandController.execute(command);
      } else {
         this.state = 1;
         command.execute();
         this.state = 4;
      }

      this.history.add(command);
      this.currentCommandIndex = this.history.size() - 1;
      if (this.currentCommandIndex >= this.historySize) {
         this.history.remove(0);
         --this.currentCommandIndex;
      }

      this.fireExecutePerformed(new HistoryBrowserEvent(new CommandNamesInfo(command.getName(), this.getLastUndoableCommandName(), this.getLastRedoableCommandName())));
   }

   public void undo() {
      if (!this.history.isEmpty() && this.currentCommandIndex >= 0) {
         UndoableCommand command = (UndoableCommand)this.history.get(this.currentCommandIndex);
         if (this.commandController != null) {
            this.commandController.undo(command);
         } else {
            this.state = 2;
            command.undo();
            this.state = 4;
         }

         --this.currentCommandIndex;
         this.fireUndoPerformed(new HistoryBrowserEvent(new CommandNamesInfo(command.getName(), this.getLastUndoableCommandName(), this.getLastRedoableCommandName())));
      }
   }

   public void redo() {
      if (!this.history.isEmpty() && this.currentCommandIndex != this.history.size() - 1) {
         UndoableCommand command = (UndoableCommand)this.history.get(++this.currentCommandIndex);
         if (this.commandController != null) {
            this.commandController.redo(command);
         } else {
            this.state = 3;
            command.redo();
            this.state = 4;
         }

         this.fireRedoPerformed(new HistoryBrowserEvent(new CommandNamesInfo(command.getName(), this.getLastUndoableCommandName(), this.getLastRedoableCommandName())));
      }
   }

   public void compoundUndo(int undoNumber) {
      for(int i = 0; i < undoNumber; ++i) {
         this.undo();
      }

   }

   public void compoundRedo(int redoNumber) {
      for(int i = 0; i < redoNumber; ++i) {
         this.redo();
      }

   }

   public String getLastUndoableCommandName() {
      return !this.history.isEmpty() && this.currentCommandIndex >= 0 ? ((UndoableCommand)this.history.get(this.currentCommandIndex)).getName() : "";
   }

   public String getLastRedoableCommandName() {
      return !this.history.isEmpty() && this.currentCommandIndex != this.history.size() - 1 ? ((UndoableCommand)this.history.get(this.currentCommandIndex + 1)).getName() : "";
   }

   public void resetHistory() {
      this.history.clear();
      this.currentCommandIndex = -1;
      this.fireHistoryReset(new HistoryBrowserEvent(new Object()));
   }

   public int getState() {
      return this.commandController != null ? this.commandController.getState() : this.state;
   }

   public void addListener(HistoryBrowserListener listener) {
      this.eventListeners.add(HistoryBrowserListener.class, listener);
   }

   public void fireExecutePerformed(HistoryBrowserEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == HistoryBrowserListener.class) {
            ((HistoryBrowserListener)listeners[i + 1]).executePerformed(event);
         }
      }

   }

   public void fireUndoPerformed(HistoryBrowserEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == HistoryBrowserListener.class) {
            ((HistoryBrowserListener)listeners[i + 1]).undoPerformed(event);
         }
      }

   }

   public void fireRedoPerformed(HistoryBrowserEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == HistoryBrowserListener.class) {
            ((HistoryBrowserListener)listeners[i + 1]).redoPerformed(event);
         }
      }

   }

   public void fireHistoryReset(HistoryBrowserEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == HistoryBrowserListener.class) {
            ((HistoryBrowserListener)listeners[i + 1]).historyReset(event);
         }
      }

   }

   public void fireDoCompoundEdit(HistoryBrowserEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == HistoryBrowserListener.class) {
            ((HistoryBrowserListener)listeners[i + 1]).doCompoundEdit(event);
         }
      }

   }

   public void fireCompoundEditPerformed(HistoryBrowserEvent event) {
      Object[] listeners = this.eventListeners.getListenerList();
      int length = listeners.length;

      for(int i = 0; i < length; i += 2) {
         if (listeners[i] == HistoryBrowserListener.class) {
            ((HistoryBrowserListener)listeners[i + 1]).compoundEditPerformed(event);
         }
      }

   }

   public static class DocumentCommandController implements CommandController {
      protected DOMViewerController controller;
      protected int state = 4;

      public DocumentCommandController(DOMViewerController controller) {
         this.controller = controller;
      }

      public void execute(final UndoableCommand command) {
         Runnable r = new Runnable() {
            public void run() {
               DocumentCommandController.this.state = 1;
               command.execute();
               DocumentCommandController.this.state = 4;
            }
         };
         this.controller.performUpdate(r);
      }

      public void undo(final UndoableCommand command) {
         Runnable r = new Runnable() {
            public void run() {
               DocumentCommandController.this.state = 2;
               command.undo();
               DocumentCommandController.this.state = 4;
            }
         };
         this.controller.performUpdate(r);
      }

      public void redo(final UndoableCommand command) {
         Runnable r = new Runnable() {
            public void run() {
               DocumentCommandController.this.state = 3;
               command.redo();
               DocumentCommandController.this.state = 4;
            }
         };
         this.controller.performUpdate(r);
      }

      public int getState() {
         return this.state;
      }
   }

   public interface CommandController {
      void execute(UndoableCommand var1);

      void undo(UndoableCommand var1);

      void redo(UndoableCommand var1);

      int getState();
   }

   public static class CommandNamesInfo {
      private String lastUndoableCommandName;
      private String lastRedoableCommandName;
      private String commandName;

      public CommandNamesInfo(String commandName, String lastUndoableCommandName, String lastRedoableCommandName) {
         this.lastUndoableCommandName = lastUndoableCommandName;
         this.lastRedoableCommandName = lastRedoableCommandName;
         this.commandName = commandName;
      }

      public String getLastRedoableCommandName() {
         return this.lastRedoableCommandName;
      }

      public String getLastUndoableCommandName() {
         return this.lastUndoableCommandName;
      }

      public String getCommandName() {
         return this.commandName;
      }
   }

   public static class HistoryBrowserAdapter implements HistoryBrowserListener {
      public void executePerformed(HistoryBrowserEvent event) {
      }

      public void undoPerformed(HistoryBrowserEvent event) {
      }

      public void redoPerformed(HistoryBrowserEvent event) {
      }

      public void historyReset(HistoryBrowserEvent event) {
      }

      public void compoundEditPerformed(HistoryBrowserEvent event) {
      }

      public void doCompoundEdit(HistoryBrowserEvent event) {
      }
   }

   public interface HistoryBrowserListener extends EventListener {
      void executePerformed(HistoryBrowserEvent var1);

      void undoPerformed(HistoryBrowserEvent var1);

      void redoPerformed(HistoryBrowserEvent var1);

      void historyReset(HistoryBrowserEvent var1);

      void doCompoundEdit(HistoryBrowserEvent var1);

      void compoundEditPerformed(HistoryBrowserEvent var1);
   }

   public static class HistoryBrowserEvent extends EventObject {
      public HistoryBrowserEvent(Object source) {
         super(source);
      }
   }
}
