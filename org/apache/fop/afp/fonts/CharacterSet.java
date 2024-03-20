package org.apache.fop.afp.fonts;

import java.awt.Rectangle;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.afp.util.StringUtils;

public class CharacterSet {
   protected static final Log LOG = LogFactory.getLog(CharacterSet.class.getName());
   public static final String DEFAULT_CODEPAGE = "T1V10500";
   public static final String DEFAULT_ENCODING = "Cp500";
   private static final int MAX_NAME_LEN = 8;
   public static final int SUPPORTED_ORIENTATION = 0;
   protected final String codePage;
   protected final String encoding;
   private final CharactersetEncoder encoder;
   protected final String name;
   private final AFPResourceAccessor accessor;
   private CharacterSetOrientation characterSetOrientation;
   private int nominalVerticalSize;

   CharacterSet(String codePage, String encoding, CharacterSetType charsetType, String name, AFPResourceAccessor accessor, AFPEventProducer eventProducer) {
      if (name.length() > 8) {
         String msg = "Character set name '" + name + "' must be a maximum of " + 8 + " characters";
         eventProducer.characterSetNameInvalid(this, msg);
         throw new IllegalArgumentException(msg);
      } else {
         this.name = this.padName(name);
         if (codePage == null) {
            this.codePage = null;
         } else {
            this.codePage = this.padName(codePage);
         }

         this.encoding = encoding;
         this.encoder = charsetType.getEncoder(encoding);
         this.accessor = accessor;
      }
   }

   private String padName(String name) {
      return name.length() < 8 ? StringUtils.rpad(name, ' ', 8) : name;
   }

   public void addCharacterSetOrientation(CharacterSetOrientation cso) {
      if (cso.getOrientation() == 0) {
         this.characterSetOrientation = cso;
      }

   }

   public void setNominalVerticalSize(int nominalVerticalSize) {
      this.nominalVerticalSize = nominalVerticalSize;
   }

   public int getNominalVerticalSize() {
      return this.nominalVerticalSize;
   }

   public int getAscender() {
      return this.getCharacterSetOrientation().getAscender();
   }

   public int getUnderscoreWidth() {
      return this.getCharacterSetOrientation().getUnderscoreWidth();
   }

   public int getUnderscorePosition() {
      return this.getCharacterSetOrientation().getUnderscorePosition();
   }

   public int getCapHeight() {
      return this.getCharacterSetOrientation().getCapHeight();
   }

   public int getDescender() {
      return this.getCharacterSetOrientation().getDescender();
   }

   public AFPResourceAccessor getResourceAccessor() {
      return this.accessor;
   }

   public int getXHeight() {
      return this.getCharacterSetOrientation().getXHeight();
   }

   public int getWidth(char character, int size) {
      return this.getCharacterSetOrientation().getWidth(character, size);
   }

   public Rectangle getCharacterBox(char character, int size) {
      return this.getCharacterSetOrientation().getCharacterBox(character, size);
   }

   public String getName() {
      return this.name;
   }

   public byte[] getNameBytes() {
      byte[] nameBytes = null;

      byte[] nameBytes;
      try {
         nameBytes = this.name.getBytes("Cp1146");
      } catch (UnsupportedEncodingException var3) {
         nameBytes = this.name.getBytes();
         LOG.warn("UnsupportedEncodingException translating the name " + this.name);
      }

      return nameBytes;
   }

   public String getCodePage() {
      return this.codePage;
   }

   public String getEncoding() {
      return this.encoding;
   }

   private CharacterSetOrientation getCharacterSetOrientation() {
      return this.characterSetOrientation;
   }

   public boolean hasChar(char c) {
      return this.encoder != null ? this.encoder.canEncode(c) : true;
   }

   public CharactersetEncoder.EncodedChars encodeChars(CharSequence chars) throws CharacterCodingException {
      return this.encoder.encode(chars);
   }

   public char mapChar(char c) {
      return c;
   }

   public int getSpaceIncrement() {
      return this.getCharacterSetOrientation().getSpaceIncrement();
   }

   public int getEmSpaceIncrement() {
      return this.getCharacterSetOrientation().getEmSpaceIncrement();
   }

   public int getNominalCharIncrement() {
      return this.getCharacterSetOrientation().getNominalCharIncrement();
   }
}
