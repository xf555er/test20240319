package org.apache.commons.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class IOExceptionList extends IOException {
   private static final long serialVersionUID = 1L;
   private final List causeList;

   public IOExceptionList(List causeList) {
      this(String.format("%,d exceptions: %s", causeList == null ? 0 : causeList.size(), causeList), causeList);
   }

   public IOExceptionList(String message, List causeList) {
      super(message, causeList != null && !causeList.isEmpty() ? (Throwable)causeList.get(0) : null);
      this.causeList = causeList == null ? Collections.emptyList() : causeList;
   }

   public Throwable getCause(int index) {
      return (Throwable)this.causeList.get(index);
   }

   public Throwable getCause(int index, Class clazz) {
      return (Throwable)clazz.cast(this.causeList.get(index));
   }

   public List getCauseList() {
      return this.causeList;
   }

   public List getCauseList(Class clazz) {
      return this.causeList;
   }
}
