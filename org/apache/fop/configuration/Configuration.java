package org.apache.fop.configuration;

public interface Configuration {
   Configuration getChild(String var1);

   Configuration getChild(String var1, boolean var2);

   Configuration[] getChildren(String var1);

   String[] getAttributeNames();

   String getAttribute(String var1) throws ConfigurationException;

   String getAttribute(String var1, String var2);

   boolean getAttributeAsBoolean(String var1, boolean var2);

   float getAttributeAsFloat(String var1) throws ConfigurationException;

   float getAttributeAsFloat(String var1, float var2);

   int getAttributeAsInteger(String var1, int var2);

   String getValue() throws ConfigurationException;

   String getValue(String var1);

   boolean getValueAsBoolean() throws ConfigurationException;

   boolean getValueAsBoolean(boolean var1);

   int getValueAsInteger() throws ConfigurationException;

   int getValueAsInteger(int var1);

   float getValueAsFloat() throws ConfigurationException;

   float getValueAsFloat(float var1);

   String getLocation();
}
