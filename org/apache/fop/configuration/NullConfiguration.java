package org.apache.fop.configuration;

final class NullConfiguration implements Configuration {
   static final NullConfiguration INSTANCE = new NullConfiguration();

   private NullConfiguration() {
   }

   public Configuration getChild(String key) {
      return INSTANCE;
   }

   public Configuration getChild(String key, boolean required) {
      return INSTANCE;
   }

   public Configuration[] getChildren(String key) {
      return new Configuration[0];
   }

   public String[] getAttributeNames() {
      return new String[0];
   }

   public String getAttribute(String key) throws ConfigurationException {
      return "";
   }

   public String getAttribute(String key, String defaultValue) {
      return defaultValue;
   }

   public boolean getAttributeAsBoolean(String key, boolean defaultValue) {
      return defaultValue;
   }

   public float getAttributeAsFloat(String key) throws ConfigurationException {
      return 0.0F;
   }

   public float getAttributeAsFloat(String key, float defaultValue) {
      return defaultValue;
   }

   public int getAttributeAsInteger(String key, int defaultValue) {
      return defaultValue;
   }

   public String getValue() throws ConfigurationException {
      throw new ConfigurationException("missing value");
   }

   public String getValue(String defaultValue) {
      return defaultValue;
   }

   public boolean getValueAsBoolean() throws ConfigurationException {
      return false;
   }

   public boolean getValueAsBoolean(boolean defaultValue) {
      return defaultValue;
   }

   public int getValueAsInteger() throws ConfigurationException {
      return 0;
   }

   public int getValueAsInteger(int defaultValue) {
      return defaultValue;
   }

   public float getValueAsFloat() throws ConfigurationException {
      return 0.0F;
   }

   public float getValueAsFloat(float defaultValue) {
      return defaultValue;
   }

   public String getLocation() {
      return "<no-location>";
   }
}
