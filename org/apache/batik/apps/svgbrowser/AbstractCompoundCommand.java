package org.apache.batik.apps.svgbrowser;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class AbstractCompoundCommand extends AbstractUndoableCommand {
   protected ArrayList atomCommands = new ArrayList();

   public void addCommand(UndoableCommand command) {
      if (command.shouldExecute()) {
         this.atomCommands.add(command);
      }

   }

   public void execute() {
      int n = this.atomCommands.size();
      Iterator var2 = this.atomCommands.iterator();

      while(var2.hasNext()) {
         Object atomCommand = var2.next();
         UndoableCommand cmd = (UndoableCommand)atomCommand;
         cmd.execute();
      }

   }

   public void undo() {
      int size = this.atomCommands.size();

      for(int i = size - 1; i >= 0; --i) {
         UndoableCommand command = (UndoableCommand)this.atomCommands.get(i);
         command.undo();
      }

   }

   public void redo() {
      int n = this.atomCommands.size();
      Iterator var2 = this.atomCommands.iterator();

      while(var2.hasNext()) {
         Object atomCommand = var2.next();
         UndoableCommand cmd = (UndoableCommand)atomCommand;
         cmd.redo();
      }

   }

   public boolean shouldExecute() {
      boolean shouldExecute = true;
      if (this.atomCommands.size() == 0) {
         shouldExecute = false;
      }

      int n = this.atomCommands.size();

      for(int i = 0; i < n && shouldExecute; ++i) {
         UndoableCommand command = (UndoableCommand)this.atomCommands.get(i);
         shouldExecute = command.shouldExecute() && shouldExecute;
      }

      return shouldExecute;
   }

   public int getCommandNumber() {
      return this.atomCommands.size();
   }
}
