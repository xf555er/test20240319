package org.apache.batik.bridge;

public class UserAgentViewport implements Viewport {
   private UserAgent userAgent;

   public UserAgentViewport(UserAgent userAgent) {
      this.userAgent = userAgent;
   }

   public float getWidth() {
      return (float)this.userAgent.getViewportSize().getWidth();
   }

   public float getHeight() {
      return (float)this.userAgent.getViewportSize().getHeight();
   }
}
