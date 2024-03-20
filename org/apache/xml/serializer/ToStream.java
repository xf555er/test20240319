package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class ToStream extends SerializerBase {
   private static final String COMMENT_BEGIN = "<!--";
   private static final String COMMENT_END = "-->";
   protected BoolStack m_disableOutputEscapingStates = new BoolStack();
   EncodingInfo m_encodingInfo = new EncodingInfo((String)null, (String)null, '\u0000');
   protected BoolStack m_preserves = new BoolStack();
   protected boolean m_ispreserve = false;
   protected boolean m_isprevtext = false;
   private static final char[] s_systemLineSep = SecuritySupport.getSystemProperty("line.separator").toCharArray();
   protected char[] m_lineSep;
   protected boolean m_lineSepUse;
   protected int m_lineSepLen;
   protected CharInfo m_charInfo;
   boolean m_shouldFlush;
   protected boolean m_spaceBeforeClose;
   boolean m_startNewLine;
   protected boolean m_inDoctype;
   boolean m_isUTF8;
   protected boolean m_cdataStartCalled;
   private boolean m_expandDTDEntities;
   protected boolean m_escaping;
   OutputStream m_outputStream;
   private boolean m_writer_set_by_user;

   public ToStream() {
      this.m_lineSep = s_systemLineSep;
      this.m_lineSepUse = true;
      this.m_lineSepLen = this.m_lineSep.length;
      this.m_shouldFlush = true;
      this.m_spaceBeforeClose = false;
      this.m_inDoctype = false;
      this.m_isUTF8 = false;
      this.m_cdataStartCalled = false;
      this.m_expandDTDEntities = true;
      this.m_escaping = true;
   }

   protected void closeCDATA() throws SAXException {
      try {
         this.m_writer.write("]]>");
         this.m_cdataTagOpen = false;
      } catch (IOException var2) {
         throw new SAXException(var2);
      }
   }

   public void serialize(Node node) throws IOException {
      try {
         TreeWalker walker = new TreeWalker(this);
         walker.traverse(node);
      } catch (SAXException var3) {
         throw new WrappedRuntimeException(var3);
      }
   }

   protected final void flushWriter() throws SAXException {
      Writer writer = this.m_writer;
      if (null != writer) {
         try {
            if (writer instanceof WriterToUTF8Buffered) {
               if (this.m_shouldFlush) {
                  ((WriterToUTF8Buffered)writer).flush();
               } else {
                  ((WriterToUTF8Buffered)writer).flushBuffer();
               }
            }

            if (writer instanceof WriterToASCI) {
               if (this.m_shouldFlush) {
                  writer.flush();
               }
            } else {
               writer.flush();
            }
         } catch (IOException var3) {
            throw new SAXException(var3);
         }
      }

   }

   public OutputStream getOutputStream() {
      return this.m_outputStream;
   }

   public void elementDecl(String name, String model) throws SAXException {
      if (!this.m_inExternalDTD) {
         try {
            Writer writer = this.m_writer;
            this.DTDprolog();
            writer.write("<!ELEMENT ");
            writer.write(name);
            writer.write(32);
            writer.write(model);
            writer.write(62);
            writer.write(this.m_lineSep, 0, this.m_lineSepLen);
         } catch (IOException var4) {
            throw new SAXException(var4);
         }
      }
   }

   public void internalEntityDecl(String name, String value) throws SAXException {
      if (!this.m_inExternalDTD) {
         try {
            this.DTDprolog();
            this.outputEntityDecl(name, value);
         } catch (IOException var4) {
            throw new SAXException(var4);
         }
      }
   }

   void outputEntityDecl(String name, String value) throws IOException {
      Writer writer = this.m_writer;
      writer.write("<!ENTITY ");
      writer.write(name);
      writer.write(" \"");
      writer.write(value);
      writer.write("\">");
      writer.write(this.m_lineSep, 0, this.m_lineSepLen);
   }

   protected final void outputLineSep() throws IOException {
      this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
   }

   void setProp(String name, String val, boolean defaultVal) {
      if (val != null) {
         char first = getFirstCharLocName(name);
         boolean b;
         switch (first) {
            case 'c':
               if ("cdata-section-elements".equals(name)) {
                  this.addCdataSectionElements(val);
               }
               break;
            case 'd':
               if ("doctype-system".equals(name)) {
                  this.m_doctypeSystem = val;
               } else if ("doctype-public".equals(name)) {
                  this.m_doctypePublic = val;
                  if (val.startsWith("-//W3C//DTD XHTML")) {
                     this.m_spaceBeforeClose = true;
                  }
               }
               break;
            case 'e':
               String newEncoding = val;
               if ("encoding".equals(name)) {
                  String possible_encoding = Encodings.getMimeEncoding(val);
                  if (possible_encoding != null) {
                     super.setProp("mime-name", possible_encoding, defaultVal);
                  }

                  String oldExplicitEncoding = this.getOutputPropertyNonDefault("encoding");
                  String oldDefaultEncoding = this.getOutputPropertyDefault("encoding");
                  if (defaultVal && (oldDefaultEncoding == null || !oldDefaultEncoding.equalsIgnoreCase(val)) || !defaultVal && (oldExplicitEncoding == null || !oldExplicitEncoding.equalsIgnoreCase(val))) {
                     EncodingInfo encodingInfo = Encodings.getEncodingInfo(val);
                     if (val != null && encodingInfo.name == null) {
                        String msg = Utils.messages.createMessage("ER_ENCODING_NOT_SUPPORTED", new Object[]{val});
                        String msg2 = "Warning: encoding \"" + val + "\" not supported, using " + "UTF-8";

                        try {
                           Transformer tran = super.getTransformer();
                           if (tran != null) {
                              ErrorListener errHandler = tran.getErrorListener();
                              if (null != errHandler && this.m_sourceLocator != null) {
                                 errHandler.warning(new TransformerException(msg, this.m_sourceLocator));
                                 errHandler.warning(new TransformerException(msg2, this.m_sourceLocator));
                              } else {
                                 System.out.println(msg);
                                 System.out.println(msg2);
                              }
                           } else {
                              System.out.println(msg);
                              System.out.println(msg2);
                           }
                        } catch (Exception var14) {
                        }

                        newEncoding = "UTF-8";
                        val = "UTF-8";
                        encodingInfo = Encodings.getEncodingInfo(newEncoding);
                     }

                     if (!defaultVal || oldExplicitEncoding == null) {
                        this.m_encodingInfo = encodingInfo;
                        if (newEncoding != null) {
                           this.m_isUTF8 = newEncoding.equals("UTF-8");
                        }

                        OutputStream os = this.getOutputStream();
                        if (os != null) {
                           Writer w = this.getWriter();
                           String oldEncoding = this.getOutputProperty("encoding");
                           if ((w == null || !this.m_writer_set_by_user) && !newEncoding.equalsIgnoreCase(oldEncoding)) {
                              super.setProp(name, val, defaultVal);
                              this.setOutputStreamInternal(os, false);
                           }
                        }
                     }
                  }
               }
            case 'f':
            case 'g':
            case 'h':
            case 'j':
            case 'k':
            case 'n':
            case 'p':
            case 'q':
            case 'r':
            case 't':
            case 'u':
            default:
               break;
            case 'i':
               if ("{http://xml.apache.org/xalan}indent-amount".equals(name)) {
                  this.setIndentAmount(Integer.parseInt(val));
               } else if ("indent".equals(name)) {
                  b = "yes".equals(val);
                  this.m_doIndent = b;
               }
               break;
            case 'l':
               if ("{http://xml.apache.org/xalan}line-separator".equals(name)) {
                  this.m_lineSep = val.toCharArray();
                  this.m_lineSepLen = this.m_lineSep.length;
               }
               break;
            case 'm':
               if ("media-type".equals(name)) {
                  this.m_mediatype = val;
               }
               break;
            case 'o':
               if ("omit-xml-declaration".equals(name)) {
                  b = "yes".equals(val);
                  this.m_shouldNotWriteXMLHeader = b;
               }
               break;
            case 's':
               if ("standalone".equals(name)) {
                  if (defaultVal) {
                     this.setStandaloneInternal(val);
                  } else {
                     this.m_standaloneWasSpecified = true;
                     this.setStandaloneInternal(val);
                  }
               }
               break;
            case 'v':
               if ("version".equals(name)) {
                  this.m_version = val;
               }
         }

         super.setProp(name, val, defaultVal);
      }

   }

   public void setOutputFormat(Properties format) {
      boolean shouldFlush = this.m_shouldFlush;
      String key;
      if (format != null) {
         Enumeration propNames = format.propertyNames();

         while(propNames.hasMoreElements()) {
            key = (String)propNames.nextElement();
            String value = format.getProperty(key);
            String explicitValue = (String)format.get(key);
            if (explicitValue == null && value != null) {
               this.setOutputPropertyDefault(key, value);
            }

            if (explicitValue != null) {
               this.setOutputProperty(key, explicitValue);
            }
         }
      }

      String entitiesFileName = (String)format.get("{http://xml.apache.org/xalan}entities");
      if (null != entitiesFileName) {
         key = (String)format.get("method");
         this.m_charInfo = CharInfo.getCharInfo(entitiesFileName, key);
      }

      this.m_shouldFlush = shouldFlush;
   }

   public Properties getOutputFormat() {
      Properties def = new Properties();
      Set s = this.getOutputPropDefaultKeys();
      Iterator i = s.iterator();

      String key;
      while(i.hasNext()) {
         String key = (String)i.next();
         key = this.getOutputPropertyDefault(key);
         def.put(key, key);
      }

      Properties props = new Properties(def);
      Set s = this.getOutputPropKeys();
      Iterator i = s.iterator();

      while(i.hasNext()) {
         key = (String)i.next();
         String val = this.getOutputPropertyNonDefault(key);
         if (val != null) {
            props.put(key, val);
         }
      }

      return props;
   }

   public void setWriter(Writer writer) {
      this.setWriterInternal(writer, true);
   }

   private void setWriterInternal(Writer writer, boolean setByUser) {
      this.m_writer_set_by_user = setByUser;
      this.m_writer = writer;
      if (this.m_tracer != null) {
         boolean noTracerYet = true;

         for(Writer w2 = this.m_writer; w2 instanceof WriterChain; w2 = ((WriterChain)w2).getWriter()) {
            if (w2 instanceof SerializerTraceWriter) {
               noTracerYet = false;
               break;
            }
         }

         if (noTracerYet) {
            this.m_writer = new SerializerTraceWriter(this.m_writer, this.m_tracer);
         }
      }

   }

   public boolean setLineSepUse(boolean use_sytem_line_break) {
      boolean oldValue = this.m_lineSepUse;
      this.m_lineSepUse = use_sytem_line_break;
      return oldValue;
   }

   public void setOutputStream(OutputStream output) {
      this.setOutputStreamInternal(output, true);
   }

   private void setOutputStreamInternal(OutputStream output, boolean setByUser) {
      this.m_outputStream = output;
      String encoding = this.getOutputProperty("encoding");
      if ("UTF-8".equalsIgnoreCase(encoding)) {
         this.setWriterInternal(new WriterToUTF8Buffered(output), false);
      } else if (!"WINDOWS-1250".equals(encoding) && !"US-ASCII".equals(encoding) && !"ASCII".equals(encoding)) {
         if (encoding != null) {
            Writer osw = null;

            try {
               osw = Encodings.getWriter(output, encoding);
            } catch (UnsupportedEncodingException var7) {
               osw = null;
            }

            if (osw == null) {
               System.out.println("Warning: encoding \"" + encoding + "\" not supported" + ", using " + "UTF-8");
               encoding = "UTF-8";
               this.setEncoding(encoding);

               try {
                  osw = Encodings.getWriter(output, encoding);
               } catch (UnsupportedEncodingException var6) {
                  var6.printStackTrace();
               }
            }

            this.setWriterInternal(osw, false);
         } else {
            Writer osw = new OutputStreamWriter(output);
            this.setWriterInternal(osw, false);
         }
      } else {
         this.setWriterInternal(new WriterToASCI(output), false);
      }

   }

   public boolean setEscaping(boolean escape) {
      boolean temp = this.m_escaping;
      this.m_escaping = escape;
      return temp;
   }

   protected void indent(int depth) throws IOException {
      if (this.m_startNewLine) {
         this.outputLineSep();
      }

      if (this.m_indentAmount > 0) {
         this.printSpace(depth * this.m_indentAmount);
      }

   }

   protected void indent() throws IOException {
      this.indent(this.m_elemContext.m_currentElemDepth);
   }

   private void printSpace(int n) throws IOException {
      Writer writer = this.m_writer;

      for(int i = 0; i < n; ++i) {
         writer.write(32);
      }

   }

   public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
      if (!this.m_inExternalDTD) {
         try {
            Writer writer = this.m_writer;
            this.DTDprolog();
            writer.write("<!ATTLIST ");
            writer.write(eName);
            writer.write(32);
            writer.write(aName);
            writer.write(32);
            writer.write(type);
            if (valueDefault != null) {
               writer.write(32);
               writer.write(valueDefault);
            }

            writer.write(62);
            writer.write(this.m_lineSep, 0, this.m_lineSepLen);
         } catch (IOException var7) {
            throw new SAXException(var7);
         }
      }
   }

   public Writer getWriter() {
      return this.m_writer;
   }

   public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
      try {
         this.DTDprolog();
         this.m_writer.write("<!ENTITY ");
         this.m_writer.write(name);
         if (publicId != null) {
            this.m_writer.write(" PUBLIC \"");
            this.m_writer.write(publicId);
         } else {
            this.m_writer.write(" SYSTEM \"");
            this.m_writer.write(systemId);
         }

         this.m_writer.write("\" >");
         this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
      } catch (IOException var5) {
         var5.printStackTrace();
      }

   }

   protected boolean escapingNotNeeded(char ch) {
      boolean ret;
      if (ch < 127) {
         if (ch < ' ' && '\n' != ch && '\r' != ch && '\t' != ch) {
            ret = false;
         } else {
            ret = true;
         }
      } else {
         ret = this.m_encodingInfo.isInEncoding(ch);
      }

      return ret;
   }

   protected int writeUTF16Surrogate(char c, char[] ch, int i, int end) throws IOException {
      int codePoint = 0;
      if (i + 1 >= end) {
         throw new IOException(Utils.messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(c)}));
      } else {
         char low = ch[i + 1];
         if (!Encodings.isLowUTF16Surrogate(low)) {
            throw new IOException(Utils.messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(c) + " " + Integer.toHexString(low)}));
         } else {
            Writer writer = this.m_writer;
            if (this.m_encodingInfo.isInEncoding(c, low)) {
               writer.write(ch, i, 2);
            } else {
               String encoding = this.getEncoding();
               if (encoding != null) {
                  codePoint = Encodings.toCodePoint(c, low);
                  writer.write(38);
                  writer.write(35);
                  writer.write(Integer.toString(codePoint));
                  writer.write(59);
               } else {
                  writer.write(ch, i, 2);
               }
            }

            return codePoint;
         }
      }
   }

   int accumDefaultEntity(Writer writer, char ch, int i, char[] chars, int len, boolean fromTextNode, boolean escLF) throws IOException {
      if (!escLF && '\n' == ch) {
         writer.write(this.m_lineSep, 0, this.m_lineSepLen);
      } else {
         if ((!fromTextNode || !this.m_charInfo.shouldMapTextChar(ch)) && (fromTextNode || !this.m_charInfo.shouldMapAttrChar(ch))) {
            return i;
         }

         String outputStringForChar = this.m_charInfo.getOutputStringForChar(ch);
         if (null == outputStringForChar) {
            return i;
         }

         writer.write(outputStringForChar);
      }

      return i + 1;
   }

   void writeNormalizedChars(char[] ch, int start, int length, boolean isCData, boolean useSystemLineSeparator) throws IOException, SAXException {
      Writer writer = this.m_writer;
      int end = start + length;

      for(int i = start; i < end; ++i) {
         char c = ch[i];
         if ('\n' == c && useSystemLineSeparator) {
            writer.write(this.m_lineSep, 0, this.m_lineSepLen);
         } else {
            String intStr;
            if (isCData && !this.escapingNotNeeded(c)) {
               if (this.m_cdataTagOpen) {
                  this.closeCDATA();
               }

               if (Encodings.isHighUTF16Surrogate(c)) {
                  this.writeUTF16Surrogate(c, ch, i, end);
                  ++i;
               } else {
                  writer.write("&#");
                  intStr = Integer.toString(c);
                  writer.write(intStr);
                  writer.write(59);
               }
            } else if (isCData && i < end - 2 && ']' == c && ']' == ch[i + 1] && '>' == ch[i + 2]) {
               writer.write("]]]]><![CDATA[>");
               i += 2;
            } else if (this.escapingNotNeeded(c)) {
               if (isCData && !this.m_cdataTagOpen) {
                  writer.write("<![CDATA[");
                  this.m_cdataTagOpen = true;
               }

               writer.write(c);
            } else if (Encodings.isHighUTF16Surrogate(c)) {
               if (this.m_cdataTagOpen) {
                  this.closeCDATA();
               }

               this.writeUTF16Surrogate(c, ch, i, end);
               ++i;
            } else {
               if (this.m_cdataTagOpen) {
                  this.closeCDATA();
               }

               writer.write("&#");
               intStr = Integer.toString(c);
               writer.write(intStr);
               writer.write(59);
            }
         }
      }

   }

   public void endNonEscaping() throws SAXException {
      this.m_disableOutputEscapingStates.pop();
   }

   public void startNonEscaping() throws SAXException {
      this.m_disableOutputEscapingStates.push(true);
   }

   protected void cdata(char[] ch, int start, int length) throws SAXException {
      try {
         if (this.m_elemContext.m_startTagOpen) {
            this.closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
         }

         this.m_ispreserve = true;
         if (this.shouldIndent()) {
            this.indent();
         }

         boolean writeCDataBrackets = length >= 1 && this.escapingNotNeeded(ch[start]);
         if (writeCDataBrackets && !this.m_cdataTagOpen) {
            this.m_writer.write("<![CDATA[");
            this.m_cdataTagOpen = true;
         }

         if (this.isEscapingDisabled()) {
            this.charactersRaw(ch, start, length);
         } else {
            this.writeNormalizedChars(ch, start, length, true, this.m_lineSepUse);
         }

         if (writeCDataBrackets && ch[start + length - 1] == ']') {
            this.closeCDATA();
         }

         if (this.m_tracer != null) {
            super.fireCDATAEvent(ch, start, length);
         }

      } catch (IOException var6) {
         throw new SAXException(Utils.messages.createMessage("ER_OIERROR", (Object[])null), var6);
      }
   }

   private boolean isEscapingDisabled() {
      return this.m_disableOutputEscapingStates.peekOrFalse();
   }

   protected void charactersRaw(char[] ch, int start, int length) throws SAXException {
      if (!this.m_inEntityRef) {
         try {
            if (this.m_elemContext.m_startTagOpen) {
               this.closeStartTag();
               this.m_elemContext.m_startTagOpen = false;
            }

            this.m_ispreserve = true;
            this.m_writer.write(ch, start, length);
         } catch (IOException var5) {
            throw new SAXException(var5);
         }
      }
   }

   public void characters(char[] chars, int start, int length) throws SAXException {
      if (length != 0 && (!this.m_inEntityRef || this.m_expandDTDEntities)) {
         this.m_docIsEmpty = false;
         if (this.m_elemContext.m_startTagOpen) {
            this.closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
         } else if (this.m_needToCallStartDocument) {
            this.startDocumentInternal();
         }

         if (!this.m_cdataStartCalled && !this.m_elemContext.m_isCdataSection) {
            if (this.m_cdataTagOpen) {
               this.closeCDATA();
            }

            if (!this.m_disableOutputEscapingStates.peekOrFalse() && this.m_escaping) {
               if (this.m_elemContext.m_startTagOpen) {
                  this.closeStartTag();
                  this.m_elemContext.m_startTagOpen = false;
               }

               try {
                  int end = start + length;
                  int lastDirtyCharProcessed = start - 1;
                  Writer writer = this.m_writer;
                  boolean isAllWhitespace = true;
                  int i = start;

                  int ch;
                  String outputStringForChar;
                  while(i < end && isAllWhitespace) {
                     ch = chars[i];
                     if (this.m_charInfo.shouldMapTextChar(ch)) {
                        this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        outputStringForChar = this.m_charInfo.getOutputStringForChar((char)ch);
                        writer.write(outputStringForChar);
                        isAllWhitespace = false;
                        lastDirtyCharProcessed = i++;
                     } else {
                        switch (ch) {
                           case 9:
                              ++i;
                              break;
                           case 10:
                              lastDirtyCharProcessed = this.processLineFeed(chars, i, lastDirtyCharProcessed, writer);
                              ++i;
                              break;
                           case 13:
                              this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                              writer.write("&#13;");
                              lastDirtyCharProcessed = i++;
                              break;
                           case 32:
                              ++i;
                              break;
                           default:
                              isAllWhitespace = false;
                        }
                     }
                  }

                  if (i < end || !isAllWhitespace) {
                     this.m_ispreserve = true;
                  }

                  for(; i < end; ++i) {
                     ch = chars[i];
                     if (this.m_charInfo.shouldMapTextChar(ch)) {
                        this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                        outputStringForChar = this.m_charInfo.getOutputStringForChar((char)ch);
                        writer.write(outputStringForChar);
                        lastDirtyCharProcessed = i;
                     } else if (ch <= 31) {
                        switch (ch) {
                           case 9:
                              break;
                           case 10:
                              lastDirtyCharProcessed = this.processLineFeed(chars, i, lastDirtyCharProcessed, writer);
                              break;
                           case 11:
                           case 12:
                           default:
                              this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                              writer.write("&#");
                              writer.write(Integer.toString(ch));
                              writer.write(59);
                              lastDirtyCharProcessed = i;
                              break;
                           case 13:
                              this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                              writer.write("&#13;");
                              lastDirtyCharProcessed = i;
                        }
                     } else if (ch >= 127) {
                        if (ch <= 159) {
                           this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                           writer.write("&#");
                           writer.write(Integer.toString(ch));
                           writer.write(59);
                           lastDirtyCharProcessed = i;
                        } else if (ch == 8232) {
                           this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                           writer.write("&#8232;");
                           lastDirtyCharProcessed = i;
                        } else if (!this.m_encodingInfo.isInEncoding((char)ch)) {
                           this.writeOutCleanChars(chars, i, lastDirtyCharProcessed);
                           writer.write("&#");
                           writer.write(Integer.toString(ch));
                           writer.write(59);
                           lastDirtyCharProcessed = i;
                        }
                     }
                  }

                  int startClean = lastDirtyCharProcessed + 1;
                  if (i > startClean) {
                     ch = i - startClean;
                     this.m_writer.write(chars, startClean, ch);
                  }

                  this.m_isprevtext = true;
               } catch (IOException var12) {
                  throw new SAXException(var12);
               }

               if (this.m_tracer != null) {
                  super.fireCharEvent(chars, start, length);
               }

            } else {
               this.charactersRaw(chars, start, length);
               if (this.m_tracer != null) {
                  super.fireCharEvent(chars, start, length);
               }

            }
         } else {
            this.cdata(chars, start, length);
         }
      }
   }

   private int processLineFeed(char[] chars, int i, int lastProcessed, Writer writer) throws IOException {
      if (this.m_lineSepUse && (this.m_lineSepLen != 1 || this.m_lineSep[0] != '\n')) {
         this.writeOutCleanChars(chars, i, lastProcessed);
         writer.write(this.m_lineSep, 0, this.m_lineSepLen);
         lastProcessed = i;
      }

      return lastProcessed;
   }

   private void writeOutCleanChars(char[] chars, int i, int lastProcessed) throws IOException {
      int startClean = lastProcessed + 1;
      if (startClean < i) {
         int lengthClean = i - startClean;
         this.m_writer.write(chars, startClean, lengthClean);
      }

   }

   private static boolean isCharacterInC0orC1Range(char ch) {
      if (ch != '\t' && ch != '\n' && ch != '\r') {
         return ch >= 127 && ch <= 159 || ch >= 1 && ch <= 31;
      } else {
         return false;
      }
   }

   private static boolean isNELorLSEPCharacter(char ch) {
      return ch == 133 || ch == 8232;
   }

   private int processDirty(char[] chars, int end, int i, char ch, int lastDirty, boolean fromTextNode) throws IOException {
      int startClean = lastDirty + 1;
      if (i > startClean) {
         int lengthClean = i - startClean;
         this.m_writer.write(chars, startClean, lengthClean);
      }

      if ('\n' == ch && fromTextNode) {
         this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
      } else {
         startClean = this.accumDefaultEscape(this.m_writer, ch, i, chars, end, fromTextNode, false);
         i = startClean - 1;
      }

      return i;
   }

   public void characters(String s) throws SAXException {
      if (!this.m_inEntityRef || this.m_expandDTDEntities) {
         int length = s.length();
         if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[length * 2 + 1];
         }

         s.getChars(0, length, this.m_charsBuff, 0);
         this.characters(this.m_charsBuff, 0, length);
      }
   }

   private int accumDefaultEscape(Writer writer, char ch, int i, char[] chars, int len, boolean fromTextNode, boolean escLF) throws IOException {
      int pos = this.accumDefaultEntity(writer, ch, i, chars, len, fromTextNode, escLF);
      if (i == pos) {
         if (Encodings.isHighUTF16Surrogate(ch)) {
            int codePoint = false;
            if (i + 1 >= len) {
               throw new IOException(Utils.messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(ch)}));
            }

            ++i;
            char next = chars[i];
            if (!Encodings.isLowUTF16Surrogate(next)) {
               throw new IOException(Utils.messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(ch) + " " + Integer.toHexString(next)}));
            }

            int codePoint = Encodings.toCodePoint(ch, next);
            writer.write("&#");
            writer.write(Integer.toString(codePoint));
            writer.write(59);
            pos += 2;
         } else {
            if (!isCharacterInC0orC1Range(ch) && !isNELorLSEPCharacter(ch)) {
               if ((!this.escapingNotNeeded(ch) || fromTextNode && this.m_charInfo.shouldMapTextChar(ch) || !fromTextNode && this.m_charInfo.shouldMapAttrChar(ch)) && this.m_elemContext.m_currentElemDepth > 0) {
                  writer.write("&#");
                  writer.write(Integer.toString(ch));
                  writer.write(59);
               } else {
                  writer.write(ch);
               }
            } else {
               writer.write("&#");
               writer.write(Integer.toString(ch));
               writer.write(59);
            }

            ++pos;
         }
      }

      return pos;
   }

   public void startElement(String namespaceURI, String localName, String name, Attributes atts) throws SAXException {
      if (!this.m_inEntityRef) {
         if (this.m_needToCallStartDocument) {
            this.startDocumentInternal();
            this.m_needToCallStartDocument = false;
            this.m_docIsEmpty = false;
         } else if (this.m_cdataTagOpen) {
            this.closeCDATA();
         }

         try {
            if (this.m_needToOutputDocTypeDecl) {
               if (null != this.getDoctypeSystem()) {
                  this.outputDocTypeDecl(name, true);
               }

               this.m_needToOutputDocTypeDecl = false;
            }

            if (this.m_elemContext.m_startTagOpen) {
               this.closeStartTag();
               this.m_elemContext.m_startTagOpen = false;
            }

            if (namespaceURI != null) {
               this.ensurePrefixIsDeclared(namespaceURI, name);
            }

            this.m_ispreserve = false;
            if (this.shouldIndent() && this.m_startNewLine) {
               this.indent();
            }

            this.m_startNewLine = true;
            Writer writer = this.m_writer;
            writer.write(60);
            writer.write(name);
         } catch (IOException var6) {
            throw new SAXException(var6);
         }

         if (atts != null) {
            this.addAttributes(atts);
         }

         this.m_elemContext = this.m_elemContext.push(namespaceURI, localName, name);
         this.m_isprevtext = false;
         if (this.m_tracer != null) {
            this.firePseudoAttributes();
         }

      }
   }

   public void startElement(String elementNamespaceURI, String elementLocalName, String elementName) throws SAXException {
      this.startElement(elementNamespaceURI, elementLocalName, elementName, (Attributes)null);
   }

   public void startElement(String elementName) throws SAXException {
      this.startElement((String)null, (String)null, elementName, (Attributes)null);
   }

   void outputDocTypeDecl(String name, boolean closeDecl) throws SAXException {
      if (this.m_cdataTagOpen) {
         this.closeCDATA();
      }

      try {
         Writer writer = this.m_writer;
         writer.write("<!DOCTYPE ");
         writer.write(name);
         String doctypePublic = this.getDoctypePublic();
         if (null != doctypePublic) {
            writer.write(" PUBLIC \"");
            writer.write(doctypePublic);
            writer.write(34);
         }

         String doctypeSystem = this.getDoctypeSystem();
         if (null != doctypeSystem) {
            if (null == doctypePublic) {
               writer.write(" SYSTEM \"");
            } else {
               writer.write(" \"");
            }

            writer.write(doctypeSystem);
            if (closeDecl) {
               writer.write("\">");
               writer.write(this.m_lineSep, 0, this.m_lineSepLen);
               closeDecl = false;
            } else {
               writer.write(34);
            }
         }

      } catch (IOException var6) {
         throw new SAXException(var6);
      }
   }

   public void processAttributes(Writer writer, int nAttrs) throws IOException, SAXException {
      String encoding = this.getEncoding();

      for(int i = 0; i < nAttrs; ++i) {
         String name = this.m_attributes.getQName(i);
         String value = this.m_attributes.getValue(i);
         writer.write(32);
         writer.write(name);
         writer.write("=\"");
         this.writeAttrString(writer, value, encoding);
         writer.write(34);
      }

   }

   public void writeAttrString(Writer writer, String string, String encoding) throws IOException {
      int len = string.length();
      if (len > this.m_attrBuff.length) {
         this.m_attrBuff = new char[len * 2 + 1];
      }

      string.getChars(0, len, this.m_attrBuff, 0);
      char[] stringChars = this.m_attrBuff;

      for(int i = 0; i < len; ++i) {
         char ch = stringChars[i];
         if (this.m_charInfo.shouldMapAttrChar(ch)) {
            this.accumDefaultEscape(writer, ch, i, stringChars, len, false, true);
         } else if (0 <= ch && ch <= 31) {
            switch (ch) {
               case '\t':
                  writer.write("&#9;");
                  break;
               case '\n':
                  writer.write("&#10;");
                  break;
               case '\u000b':
               case '\f':
               default:
                  writer.write("&#");
                  writer.write(Integer.toString(ch));
                  writer.write(59);
                  break;
               case '\r':
                  writer.write("&#13;");
            }
         } else if (ch < 127) {
            writer.write(ch);
         } else if (ch <= 159) {
            writer.write("&#");
            writer.write(Integer.toString(ch));
            writer.write(59);
         } else if (ch == 8232) {
            writer.write("&#8232;");
         } else if (this.m_encodingInfo.isInEncoding(ch)) {
            writer.write(ch);
         } else {
            writer.write("&#");
            writer.write(Integer.toString(ch));
            writer.write(59);
         }
      }

   }

   public void endElement(String namespaceURI, String localName, String name) throws SAXException {
      if (!this.m_inEntityRef) {
         this.m_prefixMap.popNamespaces(this.m_elemContext.m_currentElemDepth, (ContentHandler)null);

         try {
            Writer writer = this.m_writer;
            if (this.m_elemContext.m_startTagOpen) {
               if (this.m_tracer != null) {
                  super.fireStartElem(this.m_elemContext.m_elementName);
               }

               int nAttrs = this.m_attributes.getLength();
               if (nAttrs > 0) {
                  this.processAttributes(this.m_writer, nAttrs);
                  this.m_attributes.clear();
               }

               if (this.m_spaceBeforeClose) {
                  writer.write(" />");
               } else {
                  writer.write("/>");
               }
            } else {
               if (this.m_cdataTagOpen) {
                  this.closeCDATA();
               }

               if (this.shouldIndent()) {
                  this.indent(this.m_elemContext.m_currentElemDepth - 1);
               }

               writer.write(60);
               writer.write(47);
               writer.write(name);
               writer.write(62);
            }
         } catch (IOException var6) {
            throw new SAXException(var6);
         }

         if (!this.m_elemContext.m_startTagOpen && this.m_doIndent) {
            this.m_ispreserve = this.m_preserves.isEmpty() ? false : this.m_preserves.pop();
         }

         this.m_isprevtext = false;
         if (this.m_tracer != null) {
            super.fireEndElem(name);
         }

         this.m_elemContext = this.m_elemContext.m_prev;
      }
   }

   public void endElement(String name) throws SAXException {
      this.endElement((String)null, (String)null, name);
   }

   public void startPrefixMapping(String prefix, String uri) throws SAXException {
      this.startPrefixMapping(prefix, uri, true);
   }

   public boolean startPrefixMapping(String prefix, String uri, boolean shouldFlush) throws SAXException {
      int pushDepth;
      if (shouldFlush) {
         this.flushPending();
         pushDepth = this.m_elemContext.m_currentElemDepth + 1;
      } else {
         pushDepth = this.m_elemContext.m_currentElemDepth;
      }

      boolean pushed = this.m_prefixMap.pushNamespace(prefix, uri, pushDepth);
      if (pushed) {
         String name;
         if ("".equals(prefix)) {
            name = "xmlns";
            this.addAttributeAlways("http://www.w3.org/2000/xmlns/", name, name, "CDATA", uri, false);
         } else if (!"".equals(uri)) {
            name = "xmlns:" + prefix;
            this.addAttributeAlways("http://www.w3.org/2000/xmlns/", prefix, name, "CDATA", uri, false);
         }
      }

      return pushed;
   }

   public void comment(char[] ch, int start, int length) throws SAXException {
      if (!this.m_inEntityRef) {
         if (this.m_elemContext.m_startTagOpen) {
            this.closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
         } else if (this.m_needToCallStartDocument) {
            this.startDocumentInternal();
            this.m_needToCallStartDocument = false;
         }

         try {
            int limit = start + length;
            boolean wasDash = false;
            if (this.m_cdataTagOpen) {
               this.closeCDATA();
            }

            if (this.shouldIndent()) {
               this.indent();
            }

            Writer writer = this.m_writer;
            writer.write("<!--");

            int remainingChars;
            for(remainingChars = start; remainingChars < limit; ++remainingChars) {
               if (wasDash && ch[remainingChars] == '-') {
                  writer.write(ch, start, remainingChars - start);
                  writer.write(" -");
                  start = remainingChars + 1;
               }

               wasDash = ch[remainingChars] == '-';
            }

            if (length > 0) {
               remainingChars = limit - start;
               if (remainingChars > 0) {
                  writer.write(ch, start, remainingChars);
               }

               if (ch[limit - 1] == '-') {
                  writer.write(32);
               }
            }

            writer.write("-->");
         } catch (IOException var9) {
            throw new SAXException(var9);
         }

         this.m_startNewLine = true;
         if (this.m_tracer != null) {
            super.fireCommentEvent(ch, start, length);
         }

      }
   }

   public void endCDATA() throws SAXException {
      if (this.m_cdataTagOpen) {
         this.closeCDATA();
      }

      this.m_cdataStartCalled = false;
   }

   public void endDTD() throws SAXException {
      try {
         if (this.m_needToOutputDocTypeDecl) {
            this.outputDocTypeDecl(this.m_elemContext.m_elementName, false);
            this.m_needToOutputDocTypeDecl = false;
         }

         Writer writer = this.m_writer;
         if (!this.m_inDoctype) {
            writer.write("]>");
         } else {
            writer.write(62);
         }

         writer.write(this.m_lineSep, 0, this.m_lineSepLen);
      } catch (IOException var2) {
         throw new SAXException(var2);
      }
   }

   public void endPrefixMapping(String prefix) throws SAXException {
   }

   public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
      if (0 != length) {
         this.characters(ch, start, length);
      }
   }

   public void skippedEntity(String name) throws SAXException {
   }

   public void startCDATA() throws SAXException {
      this.m_cdataStartCalled = true;
   }

   public void startEntity(String name) throws SAXException {
      if (name.equals("[dtd]")) {
         this.m_inExternalDTD = true;
      }

      if (!this.m_expandDTDEntities && !this.m_inExternalDTD) {
         this.startNonEscaping();
         this.characters("&" + name + ';');
         this.endNonEscaping();
      }

      this.m_inEntityRef = true;
   }

   protected void closeStartTag() throws SAXException {
      if (this.m_elemContext.m_startTagOpen) {
         try {
            if (this.m_tracer != null) {
               super.fireStartElem(this.m_elemContext.m_elementName);
            }

            int nAttrs = this.m_attributes.getLength();
            if (nAttrs > 0) {
               this.processAttributes(this.m_writer, nAttrs);
               this.m_attributes.clear();
            }

            this.m_writer.write(62);
         } catch (IOException var2) {
            throw new SAXException(var2);
         }

         if (this.m_CdataElems != null) {
            this.m_elemContext.m_isCdataSection = this.isCdataSection();
         }

         if (this.m_doIndent) {
            this.m_isprevtext = false;
            this.m_preserves.push(this.m_ispreserve);
         }
      }

   }

   public void startDTD(String name, String publicId, String systemId) throws SAXException {
      this.setDoctypeSystem(systemId);
      this.setDoctypePublic(publicId);
      this.m_elemContext.m_elementName = name;
      this.m_inDoctype = true;
   }

   public int getIndentAmount() {
      return this.m_indentAmount;
   }

   public void setIndentAmount(int m_indentAmount) {
      this.m_indentAmount = m_indentAmount;
   }

   protected boolean shouldIndent() {
      return this.m_doIndent && !this.m_ispreserve && !this.m_isprevtext && this.m_elemContext.m_currentElemDepth > 0;
   }

   private void setCdataSectionElements(String key, Properties props) {
      String s = props.getProperty(key);
      if (null != s) {
         Vector v = new Vector();
         int l = s.length();
         boolean inCurly = false;
         StringBuffer buf = new StringBuffer();

         for(int i = 0; i < l; ++i) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
               if (!inCurly) {
                  if (buf.length() > 0) {
                     this.addCdataSectionElement(buf.toString(), v);
                     buf.setLength(0);
                  }
                  continue;
               }
            } else if ('{' == c) {
               inCurly = true;
            } else if ('}' == c) {
               inCurly = false;
            }

            buf.append(c);
         }

         if (buf.length() > 0) {
            this.addCdataSectionElement(buf.toString(), v);
            buf.setLength(0);
         }

         this.setCdataSectionElements(v);
      }

   }

   private void addCdataSectionElement(String URI_and_localName, Vector v) {
      StringTokenizer tokenizer = new StringTokenizer(URI_and_localName, "{}", false);
      String s1 = tokenizer.nextToken();
      String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
      if (null == s2) {
         v.addElement((Object)null);
         v.addElement(s1);
      } else {
         v.addElement(s1);
         v.addElement(s2);
      }

   }

   public void setCdataSectionElements(Vector URI_and_localNames) {
      if (URI_and_localNames != null) {
         int len = URI_and_localNames.size() - 1;
         if (len > 0) {
            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < len; i += 2) {
               if (i != 0) {
                  sb.append(' ');
               }

               String uri = (String)URI_and_localNames.elementAt(i);
               String localName = (String)URI_and_localNames.elementAt(i + 1);
               if (uri != null) {
                  sb.append('{');
                  sb.append(uri);
                  sb.append('}');
               }

               sb.append(localName);
            }

            this.m_StringOfCDATASections = sb.toString();
         }
      }

      this.initCdataElems(this.m_StringOfCDATASections);
   }

   protected String ensureAttributesNamespaceIsDeclared(String ns, String localName, String rawName) throws SAXException {
      if (ns != null && ns.length() > 0) {
         int index = false;
         int index;
         String prefixFromRawName = (index = rawName.indexOf(":")) < 0 ? "" : rawName.substring(0, index);
         String prefix;
         if (index > 0) {
            prefix = this.m_prefixMap.lookupNamespace(prefixFromRawName);
            if (prefix != null && prefix.equals(ns)) {
               return null;
            } else {
               this.startPrefixMapping(prefixFromRawName, ns, false);
               this.addAttribute("http://www.w3.org/2000/xmlns/", prefixFromRawName, "xmlns:" + prefixFromRawName, "CDATA", ns, false);
               return prefixFromRawName;
            }
         } else {
            prefix = this.m_prefixMap.lookupPrefix(ns);
            if (prefix == null) {
               prefix = this.m_prefixMap.generateNextPrefix();
               this.startPrefixMapping(prefix, ns, false);
               this.addAttribute("http://www.w3.org/2000/xmlns/", prefix, "xmlns:" + prefix, "CDATA", ns, false);
            }

            return prefix;
         }
      } else {
         return null;
      }
   }

   void ensurePrefixIsDeclared(String ns, String rawName) throws SAXException {
      if (ns != null && ns.length() > 0) {
         int index;
         boolean no_prefix = (index = rawName.indexOf(":")) < 0;
         String prefix = no_prefix ? "" : rawName.substring(0, index);
         if (null != prefix) {
            String foundURI = this.m_prefixMap.lookupNamespace(prefix);
            if (null == foundURI || !foundURI.equals(ns)) {
               this.startPrefixMapping(prefix, ns);
               this.addAttributeAlways("http://www.w3.org/2000/xmlns/", no_prefix ? "xmlns" : prefix, no_prefix ? "xmlns" : "xmlns:" + prefix, "CDATA", ns, false);
            }
         }
      }

   }

   public void flushPending() throws SAXException {
      if (this.m_needToCallStartDocument) {
         this.startDocumentInternal();
         this.m_needToCallStartDocument = false;
      }

      if (this.m_elemContext.m_startTagOpen) {
         this.closeStartTag();
         this.m_elemContext.m_startTagOpen = false;
      }

      if (this.m_cdataTagOpen) {
         this.closeCDATA();
         this.m_cdataTagOpen = false;
      }

      if (this.m_writer != null) {
         try {
            this.m_writer.flush();
         } catch (IOException var2) {
         }
      }

   }

   public void setContentHandler(ContentHandler ch) {
   }

   public boolean addAttributeAlways(String uri, String localName, String rawName, String type, String value, boolean xslAttribute) {
      int index;
      if (uri != null && localName != null && uri.length() != 0) {
         index = this.m_attributes.getIndex(uri, localName);
      } else {
         index = this.m_attributes.getIndex(rawName);
      }

      boolean was_added;
      if (index >= 0) {
         String old_value = null;
         if (this.m_tracer != null) {
            old_value = this.m_attributes.getValue(index);
            if (value.equals(old_value)) {
               old_value = null;
            }
         }

         this.m_attributes.setValue(index, value);
         was_added = false;
         if (old_value != null) {
            this.firePseudoAttributes();
         }
      } else {
         if (xslAttribute) {
            int colonIndex = rawName.indexOf(58);
            if (colonIndex > 0) {
               String prefix = rawName.substring(0, colonIndex);
               NamespaceMappings.MappingRecord existing_mapping = this.m_prefixMap.getMappingFromPrefix(prefix);
               if (existing_mapping != null && existing_mapping.m_declarationDepth == this.m_elemContext.m_currentElemDepth && !existing_mapping.m_uri.equals(uri)) {
                  prefix = this.m_prefixMap.lookupPrefix(uri);
                  if (prefix == null) {
                     prefix = this.m_prefixMap.generateNextPrefix();
                  }

                  rawName = prefix + ':' + localName;
               }
            }

            try {
               this.ensureAttributesNamespaceIsDeclared(uri, localName, rawName);
            } catch (SAXException var12) {
               var12.printStackTrace();
            }
         }

         this.m_attributes.addAttribute(uri, localName, rawName, type, value);
         was_added = true;
         if (this.m_tracer != null) {
            this.firePseudoAttributes();
         }
      }

      return was_added;
   }

   protected void firePseudoAttributes() {
      if (this.m_tracer != null) {
         try {
            this.m_writer.flush();
            StringBuffer sb = new StringBuffer();
            int nAttrs = this.m_attributes.getLength();
            if (nAttrs > 0) {
               Writer writer = new WritertoStringBuffer(sb);
               this.processAttributes(writer, nAttrs);
            }

            sb.append('>');
            char[] ch = sb.toString().toCharArray();
            this.m_tracer.fireGenerateEvent(11, ch, 0, ch.length);
         } catch (IOException var4) {
         } catch (SAXException var5) {
         }
      }

   }

   public void setTransformer(Transformer transformer) {
      super.setTransformer(transformer);
      if (this.m_tracer != null && !(this.m_writer instanceof SerializerTraceWriter)) {
         this.setWriterInternal(new SerializerTraceWriter(this.m_writer, this.m_tracer), false);
      }

   }

   public boolean reset() {
      boolean wasReset = false;
      if (super.reset()) {
         this.resetToStream();
         wasReset = true;
      }

      return wasReset;
   }

   private void resetToStream() {
      this.m_cdataStartCalled = false;
      this.m_disableOutputEscapingStates.clear();
      this.m_escaping = true;
      this.m_expandDTDEntities = true;
      this.m_inDoctype = false;
      this.m_ispreserve = false;
      this.m_isprevtext = false;
      this.m_isUTF8 = false;
      this.m_lineSep = s_systemLineSep;
      this.m_lineSepLen = s_systemLineSep.length;
      this.m_lineSepUse = true;
      this.m_preserves.clear();
      this.m_shouldFlush = true;
      this.m_spaceBeforeClose = false;
      this.m_startNewLine = false;
      this.m_writer_set_by_user = false;
   }

   public void setEncoding(String encoding) {
      this.setOutputProperty("encoding", encoding);
   }

   public void notationDecl(String name, String pubID, String sysID) throws SAXException {
      try {
         this.DTDprolog();
         this.m_writer.write("<!NOTATION ");
         this.m_writer.write(name);
         if (pubID != null) {
            this.m_writer.write(" PUBLIC \"");
            this.m_writer.write(pubID);
         } else {
            this.m_writer.write(" SYSTEM \"");
            this.m_writer.write(sysID);
         }

         this.m_writer.write("\" >");
         this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
      } catch (IOException var5) {
         var5.printStackTrace();
      }

   }

   public void unparsedEntityDecl(String name, String pubID, String sysID, String notationName) throws SAXException {
      try {
         this.DTDprolog();
         this.m_writer.write("<!ENTITY ");
         this.m_writer.write(name);
         if (pubID != null) {
            this.m_writer.write(" PUBLIC \"");
            this.m_writer.write(pubID);
         } else {
            this.m_writer.write(" SYSTEM \"");
            this.m_writer.write(sysID);
         }

         this.m_writer.write("\" NDATA ");
         this.m_writer.write(notationName);
         this.m_writer.write(" >");
         this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   private void DTDprolog() throws SAXException, IOException {
      Writer writer = this.m_writer;
      if (this.m_needToOutputDocTypeDecl) {
         this.outputDocTypeDecl(this.m_elemContext.m_elementName, false);
         this.m_needToOutputDocTypeDecl = false;
      }

      if (this.m_inDoctype) {
         writer.write(" [");
         writer.write(this.m_lineSep, 0, this.m_lineSepLen);
         this.m_inDoctype = false;
      }

   }

   public void setDTDEntityExpansion(boolean expand) {
      this.m_expandDTDEntities = expand;
   }

   public void setNewLine(char[] eolChars) {
      this.m_lineSep = eolChars;
      this.m_lineSepLen = eolChars.length;
   }

   public void addCdataSectionElements(String URI_and_localNames) {
      if (URI_and_localNames != null) {
         this.initCdataElems(URI_and_localNames);
      }

      if (this.m_StringOfCDATASections == null) {
         this.m_StringOfCDATASections = URI_and_localNames;
      } else {
         this.m_StringOfCDATASections = this.m_StringOfCDATASections + " " + URI_and_localNames;
      }

   }

   static final class BoolStack {
      private boolean[] m_values;
      private int m_allocatedSize;
      private int m_index;

      public BoolStack() {
         this(32);
      }

      public BoolStack(int size) {
         this.m_allocatedSize = size;
         this.m_values = new boolean[size];
         this.m_index = -1;
      }

      public final int size() {
         return this.m_index + 1;
      }

      public final void clear() {
         this.m_index = -1;
      }

      public final boolean push(boolean val) {
         if (this.m_index == this.m_allocatedSize - 1) {
            this.grow();
         }

         return this.m_values[++this.m_index] = val;
      }

      public final boolean pop() {
         return this.m_values[this.m_index--];
      }

      public final boolean popAndTop() {
         --this.m_index;
         return this.m_index >= 0 ? this.m_values[this.m_index] : false;
      }

      public final void setTop(boolean b) {
         this.m_values[this.m_index] = b;
      }

      public final boolean peek() {
         return this.m_values[this.m_index];
      }

      public final boolean peekOrFalse() {
         return this.m_index > -1 ? this.m_values[this.m_index] : false;
      }

      public final boolean peekOrTrue() {
         return this.m_index > -1 ? this.m_values[this.m_index] : true;
      }

      public boolean isEmpty() {
         return this.m_index == -1;
      }

      private void grow() {
         this.m_allocatedSize *= 2;
         boolean[] newVector = new boolean[this.m_allocatedSize];
         System.arraycopy(this.m_values, 0, newVector, 0, this.m_index + 1);
         this.m_values = newVector;
      }
   }

   private static class WritertoStringBuffer extends Writer {
      private final StringBuffer m_stringbuf;

      WritertoStringBuffer(StringBuffer sb) {
         this.m_stringbuf = sb;
      }

      public void write(char[] arg0, int arg1, int arg2) throws IOException {
         this.m_stringbuf.append(arg0, arg1, arg2);
      }

      public void flush() throws IOException {
      }

      public void close() throws IOException {
      }

      public void write(int i) {
         this.m_stringbuf.append((char)i);
      }

      public void write(String s) {
         this.m_stringbuf.append(s);
      }
   }
}
