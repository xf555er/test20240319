package org.apache.xml.serializer.dom3;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.xml.serializer.DOM3Serializer;
import org.apache.xml.serializer.Encodings;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.utils.SystemIDResolver;
import org.apache.xml.serializer.utils.Utils;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;

public final class LSSerializerImpl implements DOMConfiguration, LSSerializer {
   private static final String DEFAULT_END_OF_LINE;
   private Serializer fXMLSerializer = null;
   protected int fFeatures = 0;
   private DOM3Serializer fDOMSerializer = null;
   private LSSerializerFilter fSerializerFilter = null;
   private Node fVisitedNode = null;
   private String fEndOfLine;
   private DOMErrorHandler fDOMErrorHandler;
   private Properties fDOMConfigProperties;
   private String fEncoding;
   private static final int CANONICAL = 1;
   private static final int CDATA = 2;
   private static final int CHARNORMALIZE = 4;
   private static final int COMMENTS = 8;
   private static final int DTNORMALIZE = 16;
   private static final int ELEM_CONTENT_WHITESPACE = 32;
   private static final int ENTITIES = 64;
   private static final int INFOSET = 128;
   private static final int NAMESPACES = 256;
   private static final int NAMESPACEDECLS = 512;
   private static final int NORMALIZECHARS = 1024;
   private static final int SPLITCDATA = 2048;
   private static final int VALIDATE = 4096;
   private static final int SCHEMAVALIDATE = 8192;
   private static final int WELLFORMED = 16384;
   private static final int DISCARDDEFAULT = 32768;
   private static final int PRETTY_PRINT = 65536;
   private static final int IGNORE_CHAR_DENORMALIZE = 131072;
   private static final int XMLDECL = 262144;
   private String[] fRecognizedParameters;
   // $FF: synthetic field
   static Class class$java$lang$Throwable;

   public LSSerializerImpl() {
      this.fEndOfLine = DEFAULT_END_OF_LINE;
      this.fDOMErrorHandler = null;
      this.fDOMConfigProperties = null;
      this.fRecognizedParameters = new String[]{"canonical-form", "cdata-sections", "check-character-normalization", "comments", "datatype-normalization", "element-content-whitespace", "entities", "infoset", "namespaces", "namespace-declarations", "split-cdata-sections", "validate", "validate-if-schema", "well-formed", "discard-default-content", "format-pretty-print", "ignore-unknown-character-denormalizations", "xml-declaration", "error-handler"};
      this.fFeatures |= 2;
      this.fFeatures |= 8;
      this.fFeatures |= 32;
      this.fFeatures |= 64;
      this.fFeatures |= 256;
      this.fFeatures |= 512;
      this.fFeatures |= 2048;
      this.fFeatures |= 16384;
      this.fFeatures |= 32768;
      this.fFeatures |= 262144;
      this.fDOMConfigProperties = new Properties();
      this.initializeSerializerProps();
      Properties configProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      this.fXMLSerializer = SerializerFactory.getSerializer(configProps);
      this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);
   }

   public void initializeSerializerProps() {
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}canonical-form", "default:no");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}check-character-normalization", "default:no");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", "default:no");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", "default:yes");
      if ((this.fFeatures & 128) != 0) {
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", "default:yes");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", "default:yes");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", "default:yes");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", "default:yes");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", "default:yes");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", "default:no");
         this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", "default:no");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", "default:no");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", "default:no");
         this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", "default:no");
      }

      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate", "default:no");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", "default:no");
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", "default:yes");
      this.fDOMConfigProperties.setProperty("indent", "default:yes");
      this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xalan}indent-amount", Integer.toString(3));
      this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", "default:yes");
      this.fDOMConfigProperties.setProperty("omit-xml-declaration", "no");
   }

   public boolean canSetParameter(String name, Object value) {
      if (value instanceof Boolean) {
         if (name.equalsIgnoreCase("cdata-sections") || name.equalsIgnoreCase("comments") || name.equalsIgnoreCase("entities") || name.equalsIgnoreCase("infoset") || name.equalsIgnoreCase("element-content-whitespace") || name.equalsIgnoreCase("namespaces") || name.equalsIgnoreCase("namespace-declarations") || name.equalsIgnoreCase("split-cdata-sections") || name.equalsIgnoreCase("well-formed") || name.equalsIgnoreCase("discard-default-content") || name.equalsIgnoreCase("format-pretty-print") || name.equalsIgnoreCase("xml-declaration")) {
            return true;
         }

         if (name.equalsIgnoreCase("canonical-form") || name.equalsIgnoreCase("check-character-normalization") || name.equalsIgnoreCase("datatype-normalization") || name.equalsIgnoreCase("validate-if-schema") || name.equalsIgnoreCase("validate")) {
            return !(Boolean)value;
         }

         if (name.equalsIgnoreCase("ignore-unknown-character-denormalizations")) {
            return (Boolean)value;
         }
      } else if (name.equalsIgnoreCase("error-handler") && value == null || value instanceof DOMErrorHandler) {
         return true;
      }

      return false;
   }

   public Object getParameter(String name) throws DOMException {
      if (name.equalsIgnoreCase("comments")) {
         return (this.fFeatures & 8) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("cdata-sections")) {
         return (this.fFeatures & 2) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("entities")) {
         return (this.fFeatures & 64) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("namespaces")) {
         return (this.fFeatures & 256) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("namespace-declarations")) {
         return (this.fFeatures & 512) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("split-cdata-sections")) {
         return (this.fFeatures & 2048) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("well-formed")) {
         return (this.fFeatures & 16384) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("discard-default-content")) {
         return (this.fFeatures & '耀') != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("format-pretty-print")) {
         return (this.fFeatures & 65536) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("xml-declaration")) {
         return (this.fFeatures & 262144) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("element-content-whitespace")) {
         return (this.fFeatures & 32) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("format-pretty-print")) {
         return (this.fFeatures & 65536) != 0 ? Boolean.TRUE : Boolean.FALSE;
      } else if (name.equalsIgnoreCase("ignore-unknown-character-denormalizations")) {
         return Boolean.TRUE;
      } else if (!name.equalsIgnoreCase("canonical-form") && !name.equalsIgnoreCase("check-character-normalization") && !name.equalsIgnoreCase("datatype-normalization") && !name.equalsIgnoreCase("validate") && !name.equalsIgnoreCase("validate-if-schema")) {
         if (name.equalsIgnoreCase("infoset")) {
            return (this.fFeatures & 64) == 0 && (this.fFeatures & 2) == 0 && (this.fFeatures & 32) != 0 && (this.fFeatures & 256) != 0 && (this.fFeatures & 512) != 0 && (this.fFeatures & 16384) != 0 && (this.fFeatures & 8) != 0 ? Boolean.TRUE : Boolean.FALSE;
         } else if (name.equalsIgnoreCase("error-handler")) {
            return this.fDOMErrorHandler;
         } else if (!name.equalsIgnoreCase("schema-location") && !name.equalsIgnoreCase("schema-type")) {
            String msg = Utils.messages.createMessage("FEATURE_NOT_FOUND", new Object[]{name});
            throw new DOMException((short)8, msg);
         } else {
            return null;
         }
      } else {
         return Boolean.FALSE;
      }
   }

   public DOMStringList getParameterNames() {
      return new DOMStringListImpl(this.fRecognizedParameters);
   }

   public void setParameter(String name, Object value) throws DOMException {
      if (value instanceof Boolean) {
         boolean state = (Boolean)value;
         if (name.equalsIgnoreCase("comments")) {
            this.fFeatures = state ? this.fFeatures | 8 : this.fFeatures & -9;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("cdata-sections")) {
            this.fFeatures = state ? this.fFeatures | 2 : this.fFeatures & -3;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("entities")) {
            this.fFeatures = state ? this.fFeatures | 64 : this.fFeatures & -65;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", "explicit:yes");
               this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", "explicit:no");
               this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("namespaces")) {
            this.fFeatures = state ? this.fFeatures | 256 : this.fFeatures & -257;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("namespace-declarations")) {
            this.fFeatures = state ? this.fFeatures | 512 : this.fFeatures & -513;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("split-cdata-sections")) {
            this.fFeatures = state ? this.fFeatures | 2048 : this.fFeatures & -2049;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("well-formed")) {
            this.fFeatures = state ? this.fFeatures | 16384 : this.fFeatures & -16385;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("discard-default-content")) {
            this.fFeatures = state ? this.fFeatures | '耀' : this.fFeatures & -32769;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("format-pretty-print")) {
            this.fFeatures = state ? this.fFeatures | 65536 : this.fFeatures & -65537;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print", "explicit:no");
            }
         } else if (name.equalsIgnoreCase("xml-declaration")) {
            this.fFeatures = state ? this.fFeatures | 262144 : this.fFeatures & -262145;
            if (state) {
               this.fDOMConfigProperties.setProperty("omit-xml-declaration", "no");
            } else {
               this.fDOMConfigProperties.setProperty("omit-xml-declaration", "yes");
            }
         } else if (name.equalsIgnoreCase("element-content-whitespace")) {
            this.fFeatures = state ? this.fFeatures | 32 : this.fFeatures & -33;
            if (state) {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", "explicit:yes");
            } else {
               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", "explicit:no");
            }
         } else {
            String msg;
            if (name.equalsIgnoreCase("ignore-unknown-character-denormalizations")) {
               if (!state) {
                  msg = Utils.messages.createMessage("FEATURE_NOT_SUPPORTED", new Object[]{name});
                  throw new DOMException((short)9, msg);
               }

               this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}ignore-unknown-character-denormalizations", "explicit:yes");
            } else if (!name.equalsIgnoreCase("canonical-form") && !name.equalsIgnoreCase("validate-if-schema") && !name.equalsIgnoreCase("validate") && !name.equalsIgnoreCase("check-character-normalization") && !name.equalsIgnoreCase("datatype-normalization")) {
               if (!name.equalsIgnoreCase("infoset")) {
                  if (!name.equalsIgnoreCase("error-handler") && !name.equalsIgnoreCase("schema-location") && !name.equalsIgnoreCase("schema-type")) {
                     msg = Utils.messages.createMessage("FEATURE_NOT_FOUND", new Object[]{name});
                     throw new DOMException((short)8, msg);
                  }

                  msg = Utils.messages.createMessage("TYPE_MISMATCH_ERR", new Object[]{name});
                  throw new DOMException((short)17, msg);
               }

               if (state) {
                  this.fFeatures &= -65;
                  this.fFeatures &= -3;
                  this.fFeatures &= -8193;
                  this.fFeatures &= -17;
                  this.fFeatures |= 256;
                  this.fFeatures |= 512;
                  this.fFeatures |= 16384;
                  this.fFeatures |= 32;
                  this.fFeatures |= 8;
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", "explicit:yes");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", "explicit:yes");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}comments", "explicit:yes");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", "explicit:yes");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", "explicit:yes");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}entities", "explicit:no");
                  this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}entities", "explicit:no");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", "explicit:no");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", "explicit:no");
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", "explicit:no");
               }
            } else {
               if (state) {
                  msg = Utils.messages.createMessage("FEATURE_NOT_SUPPORTED", new Object[]{name});
                  throw new DOMException((short)9, msg);
               }

               if (name.equalsIgnoreCase("canonical-form")) {
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}canonical-form", "explicit:no");
               } else if (name.equalsIgnoreCase("validate-if-schema")) {
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate-if-schema", "explicit:no");
               } else if (name.equalsIgnoreCase("validate")) {
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}validate", "explicit:no");
               } else if (name.equalsIgnoreCase("validate-if-schema")) {
                  this.fDOMConfigProperties.setProperty("check-character-normalizationcheck-character-normalization", "explicit:no");
               } else if (name.equalsIgnoreCase("datatype-normalization")) {
                  this.fDOMConfigProperties.setProperty("{http://www.w3.org/TR/DOM-Level-3-LS}datatype-normalization", "explicit:no");
               }
            }
         }
      } else {
         String msg;
         if (name.equalsIgnoreCase("error-handler")) {
            if (value != null && !(value instanceof DOMErrorHandler)) {
               msg = Utils.messages.createMessage("TYPE_MISMATCH_ERR", new Object[]{name});
               throw new DOMException((short)17, msg);
            }

            this.fDOMErrorHandler = (DOMErrorHandler)value;
         } else {
            if (!name.equalsIgnoreCase("schema-location") && !name.equalsIgnoreCase("schema-type")) {
               if (!name.equalsIgnoreCase("comments") && !name.equalsIgnoreCase("cdata-sections") && !name.equalsIgnoreCase("entities") && !name.equalsIgnoreCase("namespaces") && !name.equalsIgnoreCase("namespace-declarations") && !name.equalsIgnoreCase("split-cdata-sections") && !name.equalsIgnoreCase("well-formed") && !name.equalsIgnoreCase("discard-default-content") && !name.equalsIgnoreCase("format-pretty-print") && !name.equalsIgnoreCase("xml-declaration") && !name.equalsIgnoreCase("element-content-whitespace") && !name.equalsIgnoreCase("ignore-unknown-character-denormalizations") && !name.equalsIgnoreCase("canonical-form") && !name.equalsIgnoreCase("validate-if-schema") && !name.equalsIgnoreCase("validate") && !name.equalsIgnoreCase("check-character-normalization") && !name.equalsIgnoreCase("datatype-normalization") && !name.equalsIgnoreCase("infoset")) {
                  msg = Utils.messages.createMessage("FEATURE_NOT_FOUND", new Object[]{name});
                  throw new DOMException((short)8, msg);
               }

               msg = Utils.messages.createMessage("TYPE_MISMATCH_ERR", new Object[]{name});
               throw new DOMException((short)17, msg);
            }

            if (value != null) {
               if (!(value instanceof String)) {
                  msg = Utils.messages.createMessage("TYPE_MISMATCH_ERR", new Object[]{name});
                  throw new DOMException((short)17, msg);
               }

               msg = Utils.messages.createMessage("FEATURE_NOT_SUPPORTED", new Object[]{name});
               throw new DOMException((short)9, msg);
            }
         }
      }

   }

   public DOMConfiguration getDomConfig() {
      return this;
   }

   public LSSerializerFilter getFilter() {
      return this.fSerializerFilter;
   }

   public String getNewLine() {
      return this.fEndOfLine;
   }

   public void setFilter(LSSerializerFilter filter) {
      this.fSerializerFilter = filter;
   }

   public void setNewLine(String newLine) {
      this.fEndOfLine = newLine != null ? newLine : DEFAULT_END_OF_LINE;
   }

   public boolean write(Node nodeArg, LSOutput destination) throws LSException {
      if (destination == null) {
         String msg = Utils.messages.createMessage("no-output-specified", (Object[])null);
         if (this.fDOMErrorHandler != null) {
            this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "no-output-specified"));
         }

         throw new LSException((short)82, msg);
      } else if (nodeArg == null) {
         return false;
      } else {
         Serializer serializer = this.fXMLSerializer;
         serializer.reset();
         String msg;
         if (nodeArg != this.fVisitedNode) {
            String xmlVersion = this.getXMLVersion(nodeArg);
            this.fEncoding = destination.getEncoding();
            if (this.fEncoding == null) {
               this.fEncoding = this.getInputEncoding(nodeArg);
               this.fEncoding = this.fEncoding != null ? this.fEncoding : (this.getXMLEncoding(nodeArg) == null ? "UTF-8" : this.getXMLEncoding(nodeArg));
            }

            if (!Encodings.isRecognizedEncoding(this.fEncoding)) {
               msg = Utils.messages.createMessage("unsupported-encoding", (Object[])null);
               if (this.fDOMErrorHandler != null) {
                  this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "unsupported-encoding"));
               }

               throw new LSException((short)82, msg);
            }

            serializer.getOutputFormat().setProperty("version", xmlVersion);
            this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}xml-version", xmlVersion);
            this.fDOMConfigProperties.setProperty("encoding", this.fEncoding);
            if ((nodeArg.getNodeType() != 9 || nodeArg.getNodeType() != 1 || nodeArg.getNodeType() != 6) && (this.fFeatures & 262144) != 0) {
               this.fDOMConfigProperties.setProperty("omit-xml-declaration", "default:no");
            }

            this.fVisitedNode = nodeArg;
         }

         this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);

         try {
            Writer writer = destination.getCharacterStream();
            if (writer == null) {
               OutputStream outputStream = destination.getByteStream();
               if (outputStream == null) {
                  String uri = destination.getSystemId();
                  String msg;
                  if (uri == null) {
                     msg = Utils.messages.createMessage("no-output-specified", (Object[])null);
                     if (this.fDOMErrorHandler != null) {
                        this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "no-output-specified"));
                     }

                     throw new LSException((short)82, msg);
                  }

                  msg = SystemIDResolver.getAbsoluteURI(uri);
                  URL url = new URL(msg);
                  OutputStream urlOutStream = null;
                  String protocol = url.getProtocol();
                  String host = url.getHost();
                  if (protocol.equalsIgnoreCase("file") && (host == null || host.length() == 0 || host.equals("localhost"))) {
                     urlOutStream = new FileOutputStream(getPathWithoutEscapes(url.getPath()));
                  } else {
                     URLConnection urlCon = url.openConnection();
                     urlCon.setDoInput(false);
                     urlCon.setDoOutput(true);
                     urlCon.setUseCaches(false);
                     urlCon.setAllowUserInteraction(false);
                     if (urlCon instanceof HttpURLConnection) {
                        HttpURLConnection httpCon = (HttpURLConnection)urlCon;
                        httpCon.setRequestMethod("PUT");
                     }

                     urlOutStream = urlCon.getOutputStream();
                  }

                  serializer.setOutputStream((OutputStream)urlOutStream);
               } else {
                  serializer.setOutputStream(outputStream);
               }
            } else {
               serializer.setWriter(writer);
            }

            if (this.fDOMSerializer == null) {
               this.fDOMSerializer = (DOM3Serializer)serializer.asDOM3Serializer();
            }

            if (this.fDOMErrorHandler != null) {
               this.fDOMSerializer.setErrorHandler(this.fDOMErrorHandler);
            }

            if (this.fSerializerFilter != null) {
               this.fDOMSerializer.setNodeFilter(this.fSerializerFilter);
            }

            this.fDOMSerializer.setNewLine(this.fEndOfLine.toCharArray());
            this.fDOMSerializer.serializeDOM3(nodeArg);
            return true;
         } catch (UnsupportedEncodingException var14) {
            msg = Utils.messages.createMessage("unsupported-encoding", (Object[])null);
            if (this.fDOMErrorHandler != null) {
               this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "unsupported-encoding", var14));
            }

            throw (LSException)createLSException((short)82, var14).fillInStackTrace();
         } catch (LSException var15) {
            throw var15;
         } catch (RuntimeException var16) {
            throw (LSException)createLSException((short)82, var16).fillInStackTrace();
         } catch (Exception var17) {
            if (this.fDOMErrorHandler != null) {
               this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, var17.getMessage(), (String)null, var17));
            }

            throw (LSException)createLSException((short)82, var17).fillInStackTrace();
         }
      }
   }

   public String writeToString(Node nodeArg) throws DOMException, LSException {
      if (nodeArg == null) {
         return null;
      } else {
         Serializer serializer = this.fXMLSerializer;
         serializer.reset();
         if (nodeArg != this.fVisitedNode) {
            String xmlVersion = this.getXMLVersion(nodeArg);
            serializer.getOutputFormat().setProperty("version", xmlVersion);
            this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}xml-version", xmlVersion);
            this.fDOMConfigProperties.setProperty("encoding", "UTF-16");
            if ((nodeArg.getNodeType() != 9 || nodeArg.getNodeType() != 1 || nodeArg.getNodeType() != 6) && (this.fFeatures & 262144) != 0) {
               this.fDOMConfigProperties.setProperty("omit-xml-declaration", "default:no");
            }

            this.fVisitedNode = nodeArg;
         }

         this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);
         StringWriter output = new StringWriter();

         try {
            serializer.setWriter(output);
            if (this.fDOMSerializer == null) {
               this.fDOMSerializer = (DOM3Serializer)serializer.asDOM3Serializer();
            }

            if (this.fDOMErrorHandler != null) {
               this.fDOMSerializer.setErrorHandler(this.fDOMErrorHandler);
            }

            if (this.fSerializerFilter != null) {
               this.fDOMSerializer.setNodeFilter(this.fSerializerFilter);
            }

            this.fDOMSerializer.setNewLine(this.fEndOfLine.toCharArray());
            this.fDOMSerializer.serializeDOM3(nodeArg);
         } catch (LSException var5) {
            throw var5;
         } catch (RuntimeException var6) {
            throw (LSException)createLSException((short)82, var6).fillInStackTrace();
         } catch (Exception var7) {
            if (this.fDOMErrorHandler != null) {
               this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, var7.getMessage(), (String)null, var7));
            }

            throw (LSException)createLSException((short)82, var7).fillInStackTrace();
         }

         return output.toString();
      }
   }

   public boolean writeToURI(Node nodeArg, String uri) throws LSException {
      if (nodeArg == null) {
         return false;
      } else {
         Serializer serializer = this.fXMLSerializer;
         serializer.reset();
         String msg;
         if (nodeArg != this.fVisitedNode) {
            msg = this.getXMLVersion(nodeArg);
            this.fEncoding = this.getInputEncoding(nodeArg);
            if (this.fEncoding == null) {
               this.fEncoding = this.fEncoding != null ? this.fEncoding : (this.getXMLEncoding(nodeArg) == null ? "UTF-8" : this.getXMLEncoding(nodeArg));
            }

            serializer.getOutputFormat().setProperty("version", msg);
            this.fDOMConfigProperties.setProperty("{http://xml.apache.org/xerces-2j}xml-version", msg);
            this.fDOMConfigProperties.setProperty("encoding", this.fEncoding);
            if ((nodeArg.getNodeType() != 9 || nodeArg.getNodeType() != 1 || nodeArg.getNodeType() != 6) && (this.fFeatures & 262144) != 0) {
               this.fDOMConfigProperties.setProperty("omit-xml-declaration", "default:no");
            }

            this.fVisitedNode = nodeArg;
         }

         this.fXMLSerializer.setOutputFormat(this.fDOMConfigProperties);

         try {
            if (uri == null) {
               msg = Utils.messages.createMessage("no-output-specified", (Object[])null);
               if (this.fDOMErrorHandler != null) {
                  this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, msg, "no-output-specified"));
               }

               throw new LSException((short)82, msg);
            } else {
               msg = SystemIDResolver.getAbsoluteURI(uri);
               URL url = new URL(msg);
               OutputStream urlOutStream = null;
               String protocol = url.getProtocol();
               String host = url.getHost();
               if (!protocol.equalsIgnoreCase("file") || host != null && host.length() != 0 && !host.equals("localhost")) {
                  URLConnection urlCon = url.openConnection();
                  urlCon.setDoInput(false);
                  urlCon.setDoOutput(true);
                  urlCon.setUseCaches(false);
                  urlCon.setAllowUserInteraction(false);
                  if (urlCon instanceof HttpURLConnection) {
                     HttpURLConnection httpCon = (HttpURLConnection)urlCon;
                     httpCon.setRequestMethod("PUT");
                  }

                  urlOutStream = urlCon.getOutputStream();
               } else {
                  urlOutStream = new FileOutputStream(getPathWithoutEscapes(url.getPath()));
               }

               serializer.setOutputStream((OutputStream)urlOutStream);
               if (this.fDOMSerializer == null) {
                  this.fDOMSerializer = (DOM3Serializer)serializer.asDOM3Serializer();
               }

               if (this.fDOMErrorHandler != null) {
                  this.fDOMSerializer.setErrorHandler(this.fDOMErrorHandler);
               }

               if (this.fSerializerFilter != null) {
                  this.fDOMSerializer.setNodeFilter(this.fSerializerFilter);
               }

               this.fDOMSerializer.setNewLine(this.fEndOfLine.toCharArray());
               this.fDOMSerializer.serializeDOM3(nodeArg);
               return true;
            }
         } catch (LSException var11) {
            throw var11;
         } catch (RuntimeException var12) {
            throw (LSException)createLSException((short)82, var12).fillInStackTrace();
         } catch (Exception var13) {
            if (this.fDOMErrorHandler != null) {
               this.fDOMErrorHandler.handleError(new DOMErrorImpl((short)3, var13.getMessage(), (String)null, var13));
            }

            throw (LSException)createLSException((short)82, var13).fillInStackTrace();
         }
      }
   }

   protected String getXMLVersion(Node nodeArg) {
      Document doc = null;
      if (nodeArg != null) {
         if (nodeArg.getNodeType() == 9) {
            doc = (Document)nodeArg;
         } else {
            doc = nodeArg.getOwnerDocument();
         }

         if (doc != null && doc.getImplementation().hasFeature("Core", "3.0")) {
            return doc.getXmlVersion();
         }
      }

      return "1.0";
   }

   protected String getXMLEncoding(Node nodeArg) {
      Document doc = null;
      if (nodeArg != null) {
         if (nodeArg.getNodeType() == 9) {
            doc = (Document)nodeArg;
         } else {
            doc = nodeArg.getOwnerDocument();
         }

         if (doc != null && doc.getImplementation().hasFeature("Core", "3.0")) {
            return doc.getXmlEncoding();
         }
      }

      return "UTF-8";
   }

   protected String getInputEncoding(Node nodeArg) {
      Document doc = null;
      if (nodeArg != null) {
         if (nodeArg.getNodeType() == 9) {
            doc = (Document)nodeArg;
         } else {
            doc = nodeArg.getOwnerDocument();
         }

         if (doc != null && doc.getImplementation().hasFeature("Core", "3.0")) {
            return doc.getInputEncoding();
         }
      }

      return null;
   }

   public DOMErrorHandler getErrorHandler() {
      return this.fDOMErrorHandler;
   }

   private static String getPathWithoutEscapes(String origPath) {
      if (origPath != null && origPath.length() != 0 && origPath.indexOf(37) != -1) {
         StringTokenizer tokenizer = new StringTokenizer(origPath, "%");
         StringBuffer result = new StringBuffer(origPath.length());
         int size = tokenizer.countTokens();
         result.append(tokenizer.nextToken());

         for(int i = 1; i < size; ++i) {
            String token = tokenizer.nextToken();
            if (token.length() >= 2 && isHexDigit(token.charAt(0)) && isHexDigit(token.charAt(1))) {
               result.append((char)Integer.valueOf(token.substring(0, 2), 16));
               token = token.substring(2);
            }

            result.append(token);
         }

         return result.toString();
      } else {
         return origPath;
      }
   }

   private static boolean isHexDigit(char c) {
      return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
   }

   private static LSException createLSException(short code, Throwable cause) {
      LSException lse = new LSException(code, cause != null ? cause.getMessage() : null);
      if (cause != null && LSSerializerImpl.ThrowableMethods.fgThrowableMethodsAvailable) {
         try {
            LSSerializerImpl.ThrowableMethods.fgThrowableInitCauseMethod.invoke(lse, cause);
         } catch (Exception var4) {
         }
      }

      return lse;
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }

   static {
      String lineSeparator = (String)AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            try {
               return System.getProperty("line.separator");
            } catch (SecurityException var2) {
               return null;
            }
         }
      });
      DEFAULT_END_OF_LINE = lineSeparator == null || !lineSeparator.equals("\r\n") && !lineSeparator.equals("\r") ? "\n" : lineSeparator;
   }

   static class ThrowableMethods {
      private static Method fgThrowableInitCauseMethod = null;
      private static boolean fgThrowableMethodsAvailable = false;

      private ThrowableMethods() {
      }

      static {
         try {
            fgThrowableInitCauseMethod = (LSSerializerImpl.class$java$lang$Throwable == null ? (LSSerializerImpl.class$java$lang$Throwable = LSSerializerImpl.class$("java.lang.Throwable")) : LSSerializerImpl.class$java$lang$Throwable).getMethod("initCause", LSSerializerImpl.class$java$lang$Throwable == null ? (LSSerializerImpl.class$java$lang$Throwable = LSSerializerImpl.class$("java.lang.Throwable")) : LSSerializerImpl.class$java$lang$Throwable);
            fgThrowableMethodsAvailable = true;
         } catch (Exception var1) {
            fgThrowableInitCauseMethod = null;
            fgThrowableMethodsAvailable = false;
         }

      }
   }
}
