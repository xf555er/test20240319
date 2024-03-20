package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.xmlgraphics.util.DateFormatUtil;

public class PDFInfo extends PDFObject {
   private String producer;
   private String title;
   private String author;
   private String subject;
   private String keywords;
   private Date creationDate;
   private Date modDate;
   private Map customProperties;
   private String creator;

   public String getProducer() {
      return this.producer;
   }

   public void setProducer(String producer) {
      this.producer = producer;
   }

   public String getCreator() {
      return this.creator;
   }

   public void setCreator(String creator) {
      this.creator = creator;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String t) {
      this.title = t;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String a) {
      this.author = a;
   }

   public String getSubject() {
      return this.subject;
   }

   public void setSubject(String s) {
      this.subject = s;
   }

   public String getKeywords() {
      return this.keywords;
   }

   public void setKeywords(String k) {
      this.keywords = k;
   }

   public Date getCreationDate() {
      return this.creationDate;
   }

   public void setCreationDate(Date date) {
      this.creationDate = date;
   }

   public Date getModDate() {
      return this.modDate;
   }

   public void setModDate(Date date) {
      this.modDate = date;
   }

   public byte[] toPDF() {
      PDFProfile profile = this.getDocumentSafely().getProfile();
      ByteArrayOutputStream bout = new ByteArrayOutputStream(128);

      try {
         bout.write(encode("<<\n"));
         if (this.title != null && this.title.length() > 0) {
            bout.write(encode("/Title "));
            bout.write(this.encodeText(this.title));
            bout.write(encode("\n"));
         } else {
            profile.verifyTitleAbsent();
         }

         if (this.author != null) {
            bout.write(encode("/Author "));
            bout.write(this.encodeText(this.author));
            bout.write(encode("\n"));
         }

         if (this.subject != null) {
            bout.write(encode("/Subject "));
            bout.write(this.encodeText(this.subject));
            bout.write(encode("\n"));
         }

         if (this.keywords != null) {
            bout.write(encode("/Keywords "));
            bout.write(this.encodeText(this.keywords));
            bout.write(encode("\n"));
         }

         if (this.creator != null) {
            bout.write(encode("/Creator "));
            bout.write(this.encodeText(this.creator));
            bout.write(encode("\n"));
         }

         bout.write(encode("/Producer "));
         bout.write(this.encodeText(this.producer));
         bout.write(encode("\n"));
         if (this.creationDate == null) {
            this.creationDate = new Date();
         }

         bout.write(encode("/CreationDate "));
         bout.write(this.encodeString(formatDateTime(this.creationDate)));
         bout.write(encode("\n"));
         if (profile.isModDateRequired() && this.modDate == null) {
            this.modDate = this.creationDate;
         }

         if (this.modDate != null) {
            bout.write(encode("/ModDate "));
            bout.write(this.encodeString(formatDateTime(this.modDate)));
            bout.write(encode("\n"));
         }

         if (profile.isPDFXActive()) {
            bout.write(encode("/GTS_PDFXVersion "));
            bout.write(this.encodeString(profile.getPDFXMode().getName()));
            bout.write(encode("\n"));
         }

         if (profile.isTrappedEntryRequired()) {
            bout.write(encode("/Trapped /False\n"));
         }

         if (this.customProperties != null) {
            Iterator var3 = this.customProperties.entrySet().iterator();

            while(var3.hasNext()) {
               Map.Entry entry = (Map.Entry)var3.next();
               ((PDFName)entry.getKey()).output(bout);
               bout.write(encode(" "));
               bout.write(this.encodeText((String)entry.getValue()));
               bout.write(encode("\n"));
            }
         }

         bout.write(encode(">>"));
      } catch (IOException var5) {
         log.error("Ignored I/O exception", var5);
      }

      return bout.toByteArray();
   }

   protected static String formatDateTime(Date time, TimeZone tz) {
      return DateFormatUtil.formatPDFDate(time, tz);
   }

   protected static String formatDateTime(Date time) {
      return formatDateTime(time, TimeZone.getDefault());
   }

   public void put(String key, String value) {
      StandardKey standardKey = PDFInfo.StandardKey.get(key);
      if (standardKey != null) {
         throw new IllegalArgumentException(key + " is a reserved keyword");
      } else {
         if (this.customProperties == null) {
            this.customProperties = new LinkedHashMap();
         }

         this.customProperties.put(new PDFName(key), value);
      }
   }

   public static enum StandardKey {
      TITLE("Title"),
      AUTHOR("Author"),
      SUBJECT("Subject"),
      KEYWORDS("Keywords"),
      CREATOR("Creator"),
      PRODUCER("Producer"),
      CREATION_DATE("CreationDate"),
      MOD_DATE("ModDate"),
      TRAPPED("Trapped");

      private final String name;

      private StandardKey(String name) {
         this.name = name;
      }

      public static StandardKey get(String name) {
         StandardKey[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            StandardKey key = var1[var3];
            if (key.name.equals(name)) {
               return key;
            }
         }

         return null;
      }

      public String getName() {
         return this.name;
      }
   }
}
