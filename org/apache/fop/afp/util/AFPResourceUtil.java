package org.apache.fop.afp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;

public final class AFPResourceUtil {
   private static final byte TYPE_CODE_BEGIN = -88;
   private static final byte TYPE_CODE_END = -87;
   private static final byte END_FIELD_ANY_NAME = -1;
   private static final Log LOG = LogFactory.getLog(AFPResourceUtil.class);

   private AFPResourceUtil() {
   }

   public static byte[] getNext(byte[] identifier, InputStream inputStream) throws IOException {
      MODCAParser parser = new MODCAParser(inputStream);

      UnparsedStructuredField field;
      do {
         field = parser.readNextStructuredField();
         if (field == null) {
            return null;
         }
      } while(field.getSfClassCode() != identifier[0] || field.getSfTypeCode() != identifier[1] || field.getSfCategoryCode() != identifier[2]);

      return field.getCompleteFieldAsBytes();
   }

   private static String getResourceName(UnparsedStructuredField field) throws UnsupportedEncodingException {
      byte[] nameBytes = new byte[8];
      byte[] fieldData = field.getData();
      if (fieldData.length < 8) {
         throw new IllegalArgumentException("Field data does not contain a resource name");
      } else {
         System.arraycopy(fieldData, 0, nameBytes, 0, 8);
         return new String(nameBytes, "Cp1146");
      }
   }

   public static void copyResourceFile(InputStream in, OutputStream out) throws IOException {
      MODCAParser parser = new MODCAParser(in);

      while(true) {
         UnparsedStructuredField field = parser.readNextStructuredField();
         if (field == null) {
            return;
         }

         out.write(90);
         field.writeTo(out);
      }
   }

   public static void copyNamedResource(String name, InputStream in, final OutputStream out) throws IOException {
      final MODCAParser parser = new MODCAParser(in);
      Collection resourceNames = new HashSet();

      while(true) {
         UnparsedStructuredField field = parser.readNextStructuredField();
         if (field == null) {
            throw new IOException("Requested resource '" + name + "' not found. Encountered resource names: " + resourceNames);
         }

         if (field.getSfTypeCode() == -88) {
            String resourceName = getResourceName(field);
            resourceNames.add(resourceName);
            if (resourceName.equals(name)) {
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Start of requested structured field found:\n" + field);
               }

               final UnparsedStructuredField fieldBegin = field;
               boolean wrapInResource;
               if (field.getSfCategoryCode() == 95) {
                  wrapInResource = true;
               } else {
                  if (field.getSfCategoryCode() != -50) {
                     throw new IOException("Cannot handle resource: " + field);
                  }

                  wrapInResource = false;
               }

               if (wrapInResource) {
                  ResourceObject resourceObject = new ResourceObject(name) {
                     protected void writeContent(OutputStream os) throws IOException {
                        AFPResourceUtil.copyNamedStructuredFields(this.name, fieldBegin, parser, out);
                     }
                  };
                  resourceObject.setType((byte)-5);
                  resourceObject.writeToStream(out);
               } else {
                  copyNamedStructuredFields(name, fieldBegin, parser, out);
               }

               return;
            }
         }
      }
   }

   private static void copyNamedStructuredFields(String name, UnparsedStructuredField fieldBegin, MODCAParser parser, OutputStream out) throws IOException {
      for(UnparsedStructuredField field = fieldBegin; field != null; field = parser.readNextStructuredField()) {
         out.write(90);
         field.writeTo(out);
         if (isEndOfStructuredField(field, fieldBegin, name)) {
            return;
         }
      }

      throw new IOException("Ending structured field not found for resource " + name);
   }

   private static boolean isEndOfStructuredField(UnparsedStructuredField field, UnparsedStructuredField fieldBegin, String name) throws UnsupportedEncodingException {
      return fieldMatchesEndTagType(field) && fieldMatchesBeginCategoryCode(field, fieldBegin) && fieldHasValidName(field, name);
   }

   private static boolean fieldMatchesEndTagType(UnparsedStructuredField field) {
      return field.getSfTypeCode() == -87;
   }

   private static boolean fieldMatchesBeginCategoryCode(UnparsedStructuredField field, UnparsedStructuredField fieldBegin) {
      return fieldBegin.getSfCategoryCode() == field.getSfCategoryCode();
   }

   private static boolean fieldHasValidName(UnparsedStructuredField field, String name) throws UnsupportedEncodingException {
      if (field.getData().length > 0) {
         return field.getData()[0] == field.getData()[1] && field.getData()[0] == -1 ? true : name.equals(getResourceName(field));
      } else {
         return true;
      }
   }
}
