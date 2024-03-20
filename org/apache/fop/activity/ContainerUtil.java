package org.apache.fop.activity;

import org.apache.fop.configuration.Configurable;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;

public final class ContainerUtil {
   private ContainerUtil() {
   }

   public static void configure(Configurable configurable, Configuration configuration) {
      try {
         configurable.configure(configuration);
      } catch (ConfigurationException var3) {
         var3.printStackTrace();
         throw new IllegalStateException(var3);
      }
   }

   public static void initialize(Initializable initializable) {
      try {
         initializable.initialize();
      } catch (Exception var2) {
         var2.printStackTrace();
         throw new IllegalStateException(var2);
      }
   }
}
