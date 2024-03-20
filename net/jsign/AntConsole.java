package net.jsign;

import org.apache.tools.ant.Task;

class AntConsole implements Console {
   private final Task task;

   public AntConsole(Task task) {
      this.task = task;
   }

   public void debug(String message) {
      this.task.log(message, 4);
   }

   public void info(String message) {
      this.task.log(message, 2);
   }
}
