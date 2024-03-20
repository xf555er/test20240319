package org.apache.xml.serializer.utils;

public final class Utils {
   public static final Messages messages;
   // $FF: synthetic field
   static Class class$org$apache$xml$serializer$utils$SerializerMessages;

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }

   static {
      messages = new Messages((class$org$apache$xml$serializer$utils$SerializerMessages == null ? (class$org$apache$xml$serializer$utils$SerializerMessages = class$("org.apache.xml.serializer.utils.SerializerMessages")) : class$org$apache$xml$serializer$utils$SerializerMessages).getName());
   }
}
