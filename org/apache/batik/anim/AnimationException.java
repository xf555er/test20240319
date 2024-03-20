package org.apache.batik.anim;

import org.apache.batik.anim.timing.TimedElement;

public class AnimationException extends RuntimeException {
   protected TimedElement e;
   protected String code;
   protected Object[] params;
   protected String message;

   public AnimationException(TimedElement e, String code, Object[] params) {
      this.e = e;
      this.code = code;
      this.params = params;
   }

   public TimedElement getElement() {
      return this.e;
   }

   public String getCode() {
      return this.code;
   }

   public Object[] getParams() {
      return this.params;
   }

   public String getMessage() {
      return TimedElement.formatMessage(this.code, this.params);
   }
}
