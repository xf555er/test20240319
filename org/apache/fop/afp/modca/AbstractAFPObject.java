package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.Streamable;
import org.apache.fop.afp.util.BinaryUtils;

public abstract class AbstractAFPObject implements Streamable {
   protected static final Log LOG = LogFactory.getLog(AbstractAFPObject.class);
   protected static final byte SF_CLASS = -45;
   private static final byte[] SF_HEADER = new byte[]{90, 0, 16, -45, 0, 0, 0, 0, 0};
   protected static final int SF_HEADER_LENGTH;

   protected void copySF(byte[] data, byte type, byte category) {
      copySF(data, (byte)-45, type, category);
   }

   protected static void copySF(byte[] data, byte clazz, byte type, byte category) {
      System.arraycopy(SF_HEADER, 0, data, 0, SF_HEADER_LENGTH);
      data[3] = clazz;
      data[4] = type;
      data[5] = category;
   }

   protected void writeObjects(Collection objects, OutputStream os) throws IOException {
      if (objects != null) {
         Iterator it = objects.iterator();

         while(it.hasNext()) {
            Streamable s = (Streamable)it.next();
            s.writeToStream(os);
            it.remove();
         }
      }

   }

   protected static void writeChunksToStream(byte[] data, byte[] dataHeader, int lengthOffset, int maxChunkLength, OutputStream os) throws IOException {
      int dataLength = data.length;
      int numFullChunks = dataLength / maxChunkLength;
      int lastChunkLength = dataLength % maxChunkLength;
      int headerLen = dataHeader.length - lengthOffset;
      if (headerLen == 2) {
         headerLen = 0;
      }

      int off = 0;
      byte[] len;
      if (numFullChunks > 0) {
         len = BinaryUtils.convert(headerLen + maxChunkLength, 2);
         dataHeader[lengthOffset] = len[0];
         dataHeader[lengthOffset + 1] = len[1];

         for(int i = 0; i < numFullChunks; off += maxChunkLength) {
            os.write(dataHeader);
            os.write(data, off, maxChunkLength);
            ++i;
         }
      }

      if (lastChunkLength > 0) {
         len = BinaryUtils.convert(headerLen + lastChunkLength, 2);
         dataHeader[lengthOffset] = len[0];
         dataHeader[lengthOffset + 1] = len[1];
         os.write(dataHeader);
         os.write(data, off, lastChunkLength);
      }

   }

   protected static String truncate(String str, int maxLength) {
      if (str.length() > maxLength) {
         str = str.substring(0, maxLength);
         LOG.warn("truncated character string '" + str + "', longer than " + maxLength + " chars");
      }

      return str;
   }

   static {
      SF_HEADER_LENGTH = SF_HEADER.length;
   }

   public interface Category {
      byte PAGE_SEGMENT = 95;
      byte OBJECT_AREA = 107;
      byte COLOR_ATTRIBUTE_TABLE = 119;
      byte IM_IMAGE = 123;
      byte MEDIUM = -120;
      byte CODED_FONT = -118;
      byte PROCESS_ELEMENT = -112;
      byte OBJECT_CONTAINER = -110;
      byte PRESENTATION_TEXT = -101;
      byte INDEX = -89;
      byte DOCUMENT = -88;
      byte PAGE_GROUP = -83;
      byte PAGE = -81;
      byte GRAPHICS = -69;
      byte DATA_RESOURCE = -61;
      byte DOCUMENT_ENVIRONMENT_GROUP = -60;
      byte RESOURCE_GROUP = -58;
      byte OBJECT_ENVIRONMENT_GROUP = -57;
      byte ACTIVE_ENVIRONMENT_GROUP = -55;
      byte MEDIUM_MAP = -52;
      byte FORM_MAP = -51;
      byte NAME_RESOURCE = -50;
      byte PAGE_OVERLAY = -40;
      byte RESOURCE_ENVIROMENT_GROUP = -39;
      byte OVERLAY = -33;
      byte DATA_SUPRESSION = -22;
      byte BARCODE = -21;
      byte NO_OPERATION = -18;
      byte IMAGE = -5;
   }

   public interface Type {
      byte ATTRIBUTE = -96;
      byte COPY_COUNT = -94;
      byte DESCRIPTOR = -90;
      byte CONTROL = -89;
      byte BEGIN = -88;
      byte END = -87;
      byte MAP = -85;
      byte POSITION = -84;
      byte PROCESS = -83;
      byte INCLUDE = -81;
      byte TABLE = -80;
      byte MIGRATION = -79;
      byte VARIABLE = -78;
      byte LINK = -76;
      byte DATA = -18;
   }
}
