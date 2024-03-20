package org.apache.fop.pdf;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.CIDFont;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SimpleSingleByteEncoding;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.OTFSubSetFile;
import org.apache.fop.fonts.truetype.TTFSubSetFile;
import org.apache.fop.fonts.type1.PFBData;
import org.apache.fop.fonts.type1.PFBParser;
import org.apache.fop.fonts.type1.Type1SubsetFile;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.xmp.Metadata;

public class PDFFactory {
   public static final int DEFAULT_PDF_RESOLUTION = 72;
   private PDFDocument document;
   private Log log = LogFactory.getLog(PDFFactory.class);
   private int subsetFontCounter = -1;
   private Map dparts = new HashMap();

   public PDFFactory(PDFDocument document) {
      this.document = document;
   }

   public final PDFDocument getDocument() {
      return this.document;
   }

   public PDFRoot makeRoot(PDFPages pages) {
      PDFRoot pdfRoot = new PDFRoot(this.document, pages);
      pdfRoot.setDocument(this.getDocument());
      this.getDocument().addTrailerObject(pdfRoot);
      return pdfRoot;
   }

   public PDFPages makePages() {
      PDFPages pdfPages = new PDFPages(this.getDocument());
      pdfPages.setDocument(this.getDocument());
      this.getDocument().addTrailerObject(pdfPages);
      return pdfPages;
   }

   public PDFResources makeResources() {
      PDFResources pdfResources = new PDFResources(this.getDocument());
      pdfResources.setDocument(this.getDocument());
      this.getDocument().addTrailerObject(pdfResources);
      return pdfResources;
   }

   protected PDFInfo makeInfo(String prod) {
      PDFInfo pdfInfo = new PDFInfo();
      pdfInfo.setProducer(prod);
      this.getDocument().registerObject(pdfInfo);
      return pdfInfo;
   }

   public PDFMetadata makeMetadata(Metadata meta, boolean readOnly) {
      PDFMetadata pdfMetadata = new PDFMetadata(meta, readOnly);
      this.getDocument().registerObject(pdfMetadata);
      return pdfMetadata;
   }

   public PDFOutputIntent makeOutputIntent() {
      PDFOutputIntent outputIntent = new PDFOutputIntent();
      this.getDocument().registerObject(outputIntent);
      return outputIntent;
   }

   public PDFPage makePage(PDFResources resources, int pageIndex, Rectangle2D mediaBox, Rectangle2D cropBox, Rectangle2D bleedBox, Rectangle2D trimBox) {
      PDFPage page = new PDFPage(resources, pageIndex, mediaBox, cropBox, bleedBox, trimBox);
      this.getDocument().assignObjectNumber(page);
      this.getDocument().getPages().addPage(page);
      return page;
   }

   public PDFPage makePage(PDFResources resources, int pageWidth, int pageHeight, int pageIndex) {
      Rectangle2D mediaBox = new Rectangle2D.Double(0.0, 0.0, (double)pageWidth, (double)pageHeight);
      return this.makePage(resources, pageIndex, mediaBox, mediaBox, mediaBox, mediaBox);
   }

   public PDFPage makePage(PDFResources resources, int pageWidth, int pageHeight) {
      return this.makePage(resources, pageWidth, pageHeight, -1);
   }

   public PDFFunction makeFunction(List domain, List range, float[] cZero, float[] cOne, double interpolationExponentN) {
      PDFFunction function = new PDFFunction(domain, range, cZero, cOne, interpolationExponentN);
      function = this.registerFunction(function);
      return function;
   }

   public PDFFunction registerFunction(PDFFunction function) {
      PDFFunction oldfunc = this.getDocument().findFunction(function);
      if (oldfunc == null) {
         this.getDocument().registerObject(function);
      } else {
         function = oldfunc;
      }

      return function;
   }

   public PDFShading registerShading(PDFResourceContext res, PDFShading shading) {
      PDFShading oldshad = this.getDocument().findShading(shading);
      if (oldshad == null) {
         this.getDocument().registerObject(shading);
      } else {
         shading = oldshad;
      }

      if (res != null) {
         res.addShading(shading);
      }

      return shading;
   }

   public PDFPattern makePattern(PDFResourceContext res, int thePatternType, PDFResources theResources, int thePaintType, int theTilingType, List theBBox, double theXStep, double theYStep, List theMatrix, List theXUID, StringBuffer thePatternDataStream) {
      PDFPattern pattern = new PDFPattern(theResources, 1, thePaintType, theTilingType, theBBox, theXStep, theYStep, theMatrix, theXUID, thePatternDataStream);
      PDFPattern oldpatt = this.getDocument().findPattern(pattern);
      if (oldpatt == null) {
         this.getDocument().registerObject(pattern);
      } else {
         pattern = oldpatt;
      }

      if (res != null) {
         res.addPattern(pattern);
      }

      return pattern;
   }

   public PDFPattern registerPattern(PDFResourceContext res, PDFPattern pattern) {
      PDFPattern oldpatt = this.getDocument().findPattern(pattern);
      if (oldpatt == null) {
         this.getDocument().registerObject(pattern);
      } else {
         pattern = oldpatt;
      }

      if (res != null) {
         res.addPattern(pattern);
      }

      return pattern;
   }

   protected PDFDestination getUniqueDestination(PDFDestination newdest) {
      PDFDestination existing = this.getDocument().findDestination(newdest);
      if (existing != null) {
         return existing;
      } else {
         this.getDocument().addDestination(newdest);
         return newdest;
      }
   }

   public PDFDestination makeDestination(String idRef, Object goToRef) {
      PDFDestination destination = new PDFDestination(idRef, goToRef);
      return this.getUniqueDestination(destination);
   }

   public PDFNames makeNames() {
      PDFNames names = new PDFNames();
      this.getDocument().assignObjectNumber(names);
      this.getDocument().addTrailerObject(names);
      return names;
   }

   public PDFPageLabels makePageLabels() {
      PDFPageLabels pageLabels = new PDFPageLabels();
      this.getDocument().assignObjectNumber(pageLabels);
      this.getDocument().addTrailerObject(pageLabels);
      return pageLabels;
   }

   public PDFDests makeDests(List destinationList) {
      boolean deep = true;
      PDFDests dests = new PDFDests();
      PDFArray kids = new PDFArray(dests);
      Iterator var5 = destinationList.iterator();

      while(var5.hasNext()) {
         Object aDestinationList = var5.next();
         PDFDestination dest = (PDFDestination)aDestinationList;
         PDFNameTreeNode node = new PDFNameTreeNode();
         this.getDocument().registerObject(node);
         node.setLowerLimit(dest.getIDRef());
         node.setUpperLimit(dest.getIDRef());
         node.setNames(new PDFArray(node));
         PDFArray names = node.getNames();
         names.add(dest);
         kids.add(node);
      }

      dests.setLowerLimit(((PDFNameTreeNode)kids.get(0)).getLowerLimit());
      dests.setUpperLimit(((PDFNameTreeNode)kids.get(kids.length() - 1)).getUpperLimit());
      dests.setKids(kids);
      this.getDocument().registerObject(dests);
      return dests;
   }

   public PDFNameTreeNode makeNameTreeNode() {
      PDFNameTreeNode node = new PDFNameTreeNode();
      this.getDocument().registerObject(node);
      return node;
   }

   public PDFLink makeLink(Rectangle2D rect, PDFAction pdfAction) {
      if (rect != null && pdfAction != null) {
         PDFLink link = new PDFLink(rect);
         link.setAction(pdfAction);
         this.getDocument().registerObject(link);
         return link;
      } else {
         return null;
      }
   }

   public PDFLink makeLink(Rectangle2D rect, String page, String dest) {
      PDFLink link = new PDFLink(rect);
      this.getDocument().registerObject(link);
      PDFGoTo gt = new PDFGoTo(page);
      gt.setDestination(dest);
      this.getDocument().registerObject(gt);
      PDFInternalLink internalLink = new PDFInternalLink(gt.referencePDF());
      link.setAction(internalLink);
      return link;
   }

   public PDFLink makeLink(Rectangle2D rect, String dest, boolean isNamedDestination) {
      PDFLink link = new PDFLink(rect);
      this.getDocument().registerObject(link);
      PDFAction pdfAction = new PDFGoTo(dest, isNamedDestination);
      this.getDocument().registerObject(pdfAction);
      link.setAction(pdfAction);
      return link;
   }

   public PDFLink makeLink(Rectangle2D rect, String destination, int linkType, float yoffset) {
      PDFLink link = new PDFLink(rect);
      if (linkType == 0) {
         link.setAction(this.getExternalAction(destination, false));
      } else {
         String goToReference = this.getGoToReference(destination, yoffset);
         PDFInternalLink internalLink = new PDFInternalLink(goToReference);
         link.setAction(internalLink);
      }

      PDFLink oldlink = this.getDocument().findLink(link);
      if (oldlink == null) {
         this.getDocument().registerObject(link);
      } else {
         link = oldlink;
      }

      return link;
   }

   public PDFAction getExternalAction(String target, boolean newWindow) {
      URI uri = this.getTargetUri(target);
      if (uri != null) {
         String scheme = uri.getScheme();
         String filename = uri.getPath();
         if (filename == null) {
            filename = uri.getSchemeSpecificPart();
         }

         if (scheme == null) {
            return new PDFUri(uri.toASCIIString());
         } else if (scheme.equalsIgnoreCase("embedded-file")) {
            return this.getActionForEmbeddedFile(filename, newWindow);
         } else if (scheme.equalsIgnoreCase("file")) {
            if (filename.startsWith("//")) {
               filename = filename.replace("/", "\\");
            } else if (filename.matches("^/[A-z]:/.*")) {
               filename = filename.substring(1);
            }

            if (filename.toLowerCase().endsWith(".pdf")) {
               int page = -1;
               String dest = null;
               String fragment = uri.getFragment();
               if (fragment != null) {
                  String fragmentLo = fragment.toLowerCase();
                  if (fragmentLo.startsWith("page=")) {
                     page = Integer.parseInt(fragmentLo.substring(5));
                  } else if (fragmentLo.startsWith("dest=")) {
                     dest = fragment.substring(5);
                  }
               }

               return this.getGoToPDFAction(filename, dest, page, newWindow);
            } else {
               return (PDFAction)(uri.getQuery() == null && uri.getFragment() == null ? this.getLaunchAction(filename, newWindow) : new PDFUri(uri.toASCIIString()));
            }
         } else {
            return new PDFUri(uri.toASCIIString());
         }
      } else {
         return new PDFUri(target);
      }
   }

   private URI getTargetUri(String target) {
      URI uri;
      try {
         uri = new URI(target);
         String scheme = uri.getScheme();
         String schemeSpecificPart = uri.getSchemeSpecificPart();
         String authority = uri.getAuthority();
         if (scheme == null && schemeSpecificPart.matches("//.*")) {
            uri = this.getFileUri(target);
         } else if (scheme == null && schemeSpecificPart.matches("/.*")) {
            uri = this.getFileUri(target);
         } else if (scheme != null && scheme.matches("[A-z]")) {
            uri = this.getFileUri(target);
         } else if (scheme != null && scheme.equalsIgnoreCase("file") && authority != null) {
            uri = this.getFileUri(target);
         }
      } catch (URISyntaxException var6) {
         uri = this.getFileUri(target);
      }

      return uri;
   }

   private URI getFileUri(String target) {
      String scheme = null;
      String fragment = null;
      String filename = target;
      String targetLo = target.toLowerCase();
      int index;
      if ((index = targetLo.indexOf(".pdf#page=")) > 0 || (index = targetLo.indexOf(".pdf#dest=")) > 0) {
         filename = target.substring(0, index + 4);
         fragment = target.substring(index + 5);
      }

      if (targetLo.startsWith("file://")) {
         scheme = "file";
         filename = filename.substring("file://".length());
      } else if (targetLo.startsWith("embedded-file:")) {
         scheme = "embedded-file";
         filename = filename.substring("embedded-file:".length());
      } else if (targetLo.startsWith("file:")) {
         scheme = "file";
         filename = filename.substring("file:".length());
      }

      try {
         filename = filename.replace("\\", "/");
         if (filename.matches("[A-z]:.*")) {
            scheme = scheme == null ? "file" : scheme;
            filename = "/" + filename;
         } else if (filename.matches("//.*")) {
            scheme = scheme == null ? "file" : scheme;
            filename = "//" + filename;
         } else if (filename.matches("/.*")) {
            scheme = scheme == null ? "file" : scheme;
         }

         URI uri = new URI(scheme, filename, fragment);
         return uri;
      } catch (URISyntaxException var9) {
         throw new IllegalStateException(var9);
      }
   }

   private PDFAction getActionForEmbeddedFile(String filename, boolean newWindow) {
      PDFNames names = this.getDocument().getRoot().getNames();
      if (names == null) {
         throw new IllegalStateException("No Names dictionary present. Cannot create Launch Action for embedded file: " + filename);
      } else {
         PDFNameTreeNode embeddedFiles = names.getEmbeddedFiles();
         if (embeddedFiles == null) {
            throw new IllegalStateException("No /EmbeddedFiles name tree present. Cannot create Launch Action for embedded file: " + filename);
         } else {
            PDFArray files = embeddedFiles.getNames();
            PDFFileSpec fileSpec = null;

            for(int i = 0; i < files.length(); ++i) {
               ++i;
               PDFReference ref = (PDFReference)files.get(i);
               if (ref.getObject() instanceof PDFFileSpec && ((PDFFileSpec)ref.getObject()).getUnicodeFilename().equals(filename)) {
                  fileSpec = (PDFFileSpec)ref.getObject();
                  break;
               }
            }

            if (fileSpec == null) {
               throw new IllegalStateException("No embedded file with name " + filename + " present.");
            } else {
               StringBuffer scriptBuffer = new StringBuffer();
               scriptBuffer.append("this.exportDataObject({cName:\"");
               scriptBuffer.append(fileSpec.getFilename());
               scriptBuffer.append("\", nLaunch:2});");
               PDFJavaScriptLaunchAction action = new PDFJavaScriptLaunchAction(scriptBuffer.toString());
               return action;
            }
         }
      }
   }

   public String getGoToReference(String pdfPageRef, float yoffset) {
      return this.getPDFGoTo(pdfPageRef, new Point2D.Float(0.0F, yoffset)).referencePDF();
   }

   public PDFGoTo getPDFGoTo(String pdfPageRef, Point2D position) {
      this.getDocument().getProfile().verifyActionAllowed();
      PDFGoTo gt = new PDFGoTo(pdfPageRef, position);
      PDFGoTo oldgt = this.getDocument().findGoTo(gt);
      if (oldgt == null) {
         this.getDocument().assignObjectNumber(gt);
         this.getDocument().addTrailerObject(gt);
      } else {
         gt = oldgt;
      }

      return gt;
   }

   private PDFGoToRemote getGoToPDFAction(String file, String dest, int page, boolean newWindow) {
      this.getDocument().getProfile().verifyActionAllowed();
      PDFFileSpec fileSpec = new PDFFileSpec(file);
      PDFFileSpec oldspec = this.getDocument().findFileSpec(fileSpec);
      if (oldspec == null) {
         this.getDocument().registerObject(fileSpec);
      } else {
         fileSpec = oldspec;
      }

      PDFGoToRemote remote;
      if (dest == null && page == -1) {
         remote = new PDFGoToRemote(fileSpec, newWindow);
      } else if (dest != null) {
         remote = new PDFGoToRemote(fileSpec, dest, newWindow);
      } else {
         remote = new PDFGoToRemote(fileSpec, page, newWindow);
      }

      PDFGoToRemote oldremote = this.getDocument().findGoToRemote(remote);
      if (oldremote == null) {
         this.getDocument().registerObject(remote);
      } else {
         remote = oldremote;
      }

      return remote;
   }

   private PDFLaunch getLaunchAction(String file, boolean newWindow) {
      this.getDocument().getProfile().verifyActionAllowed();
      PDFFileSpec fileSpec = new PDFFileSpec(file);
      PDFFileSpec oldSpec = this.getDocument().findFileSpec(fileSpec);
      if (oldSpec == null) {
         this.getDocument().registerObject(fileSpec);
      } else {
         fileSpec = oldSpec;
      }

      PDFLaunch launch = new PDFLaunch(fileSpec, newWindow);
      PDFLaunch oldLaunch = this.getDocument().findLaunch(launch);
      if (oldLaunch == null) {
         this.getDocument().registerObject(launch);
      } else {
         launch = oldLaunch;
      }

      return launch;
   }

   public PDFOutline makeOutline(PDFOutline parent, String label, PDFReference actionRef, boolean showSubItems) {
      PDFOutline pdfOutline = new PDFOutline(label, actionRef, showSubItems);
      if (parent != null) {
         parent.addOutline(pdfOutline);
      }

      this.getDocument().registerObject(pdfOutline);
      return pdfOutline;
   }

   public PDFOutline makeOutline(PDFOutline parent, String label, PDFAction pdfAction, boolean showSubItems) {
      return pdfAction == null ? null : this.makeOutline(parent, label, new PDFReference(pdfAction.getAction()), showSubItems);
   }

   public PDFOutline makeOutline(PDFOutline parent, String label, String destination, float yoffset, boolean showSubItems) {
      String goToRef = this.getGoToReference(destination, yoffset);
      return this.makeOutline(parent, label, new PDFReference(goToRef), showSubItems);
   }

   public PDFEncoding makeEncoding(String encodingName) {
      PDFEncoding encoding = new PDFEncoding(encodingName);
      this.getDocument().registerObject(encoding);
      return encoding;
   }

   public PDFFont makeFont(String fontname, String basefont, String encoding, FontMetrics metrics, FontDescriptor descriptor) {
      PDFFont preRegisteredfont = this.getDocument().findFont(fontname);
      if (preRegisteredfont != null) {
         return preRegisteredfont;
      } else {
         boolean forceToUnicode = true;
         if (descriptor == null) {
            PDFFont font = new PDFFont(fontname, FontType.TYPE1, basefont, encoding);
            this.getDocument().registerObject(font);
            if (forceToUnicode && !PDFEncoding.isPredefinedEncoding(encoding)) {
               CodePointMapping mapping;
               if (encoding != null) {
                  mapping = CodePointMapping.getMapping(encoding);
               } else {
                  Typeface tf = (Typeface)metrics;
                  mapping = CodePointMapping.getMapping(tf.getEncodingName());
               }

               this.generateToUnicodeCmap(font, mapping);
            }

            return font;
         } else {
            FontType fonttype = metrics.getFontType();
            String fontPrefix = descriptor.isSubsetEmbedded() ? this.createSubsetFontPrefix() : "";
            String subsetFontName = fontPrefix + basefont;
            PDFFontDescriptor pdfdesc = this.makeFontDescriptor(descriptor, fontPrefix);
            PDFFont font = null;
            font = PDFFont.createFont(fontname, fonttype, subsetFontName, (Object)null);
            if (descriptor instanceof RefPDFFont) {
               font.setObjectNumber(((RefPDFFont)descriptor).getRef().getObjectNumber());
               font.setDocument(this.getDocument());
               this.getDocument().addObject(font);
            } else {
               this.getDocument().registerObject(font);
            }

            if (fonttype != FontType.TYPE0 && fonttype != FontType.CIDTYPE0) {
               if (fonttype == FontType.TYPE1C && (metrics instanceof LazyFont || metrics instanceof MultiByteFont)) {
                  this.handleType1CFont(pdfdesc, font, metrics, fontname, basefont, descriptor);
               } else {
                  assert font instanceof PDFFontNonBase14;

                  PDFFontNonBase14 nonBase14 = (PDFFontNonBase14)font;
                  nonBase14.setDescriptor(pdfdesc);
                  SingleByteFont singleByteFont;
                  if (metrics instanceof LazyFont) {
                     singleByteFont = (SingleByteFont)((LazyFont)metrics).getRealFont();
                  } else {
                     singleByteFont = (SingleByteFont)metrics;
                  }

                  int firstChar = false;
                  int lastChar = false;
                  boolean defaultChars = false;
                  int firstChar;
                  int lastChar;
                  if (singleByteFont.getEmbeddingMode() != EmbeddingMode.SUBSET) {
                     defaultChars = true;
                  } else {
                     Map usedGlyphs = singleByteFont.getUsedGlyphs();
                     if (fonttype == FontType.TYPE1 && usedGlyphs.size() > 0) {
                        SortedSet keys = new TreeSet(usedGlyphs.keySet());
                        keys.remove(0);
                        if (keys.size() > 0) {
                           firstChar = (Integer)keys.first();
                           lastChar = (Integer)keys.last();
                           int[] newWidths = new int[lastChar - firstChar + 1];

                           for(int i = firstChar; i < lastChar + 1; ++i) {
                              if (usedGlyphs.get(i) != null) {
                                 if (i - singleByteFont.getFirstChar() >= metrics.getWidths().length) {
                                    defaultChars = true;
                                    break;
                                 }

                                 newWidths[i - firstChar] = metrics.getWidths()[i - singleByteFont.getFirstChar()];
                              } else {
                                 newWidths[i - firstChar] = 0;
                              }
                           }

                           nonBase14.setWidthMetrics(firstChar, lastChar, new PDFArray((PDFObject)null, newWidths));
                        }
                     } else {
                        defaultChars = true;
                     }
                  }

                  if (defaultChars) {
                     firstChar = singleByteFont.getFirstChar();
                     lastChar = singleByteFont.getLastChar();
                     nonBase14.setWidthMetrics(firstChar, lastChar, new PDFArray((PDFObject)null, metrics.getWidths()));
                  }

                  SingleByteEncoding mapping = singleByteFont.getEncoding();
                  if (singleByteFont.isSymbolicFont()) {
                     if (forceToUnicode) {
                        this.generateToUnicodeCmap(nonBase14, mapping);
                     }
                  } else if (PDFEncoding.isPredefinedEncoding(mapping.getName())) {
                     font.setEncoding(mapping.getName());
                  } else if (!mapping.getName().equals("FOPPDFEncoding")) {
                     Object pdfEncoding = this.createPDFEncoding(mapping, singleByteFont.getFontName());
                     if (pdfEncoding instanceof PDFEncoding) {
                        font.setEncoding((PDFEncoding)pdfEncoding);
                     } else {
                        font.setEncoding((String)pdfEncoding);
                     }

                     if (forceToUnicode) {
                        this.generateToUnicodeCmap(nonBase14, mapping);
                     }
                  } else if (fonttype == FontType.TRUETYPE) {
                     font.setEncoding(encoding);
                  } else {
                     String[] charNameMap = mapping.getCharNameMap();
                     char[] intmap = mapping.getUnicodeCharMap();
                     PDFArray differences = new PDFArray();
                     int len = intmap.length;
                     if (charNameMap.length < len) {
                        len = charNameMap.length;
                     }

                     int last = 0;

                     for(int i = 0; i < len; ++i) {
                        if (intmap[i] - 1 != last) {
                           differences.add((double)intmap[i]);
                        }

                        last = intmap[i];
                        differences.add(new PDFName(charNameMap[i]));
                     }

                     PDFEncoding pdfEncoding = new PDFEncoding(singleByteFont.getEncodingName());
                     this.getDocument().registerObject(pdfEncoding);
                     pdfEncoding.setDifferences(differences);
                     font.setEncoding(pdfEncoding);
                     if (mapping.getUnicodeCharMap() != null) {
                        this.generateToUnicodeCmap(nonBase14, mapping);
                     }
                  }

                  if (singleByteFont.hasAdditionalEncodings()) {
                     int i = 0;

                     for(int c = singleByteFont.getAdditionalEncodingCount(); i < c; ++i) {
                        SimpleSingleByteEncoding addEncoding = singleByteFont.getAdditionalEncoding(i);
                        String name = fontname + "_" + (i + 1);
                        Object pdfenc = this.createPDFEncoding(addEncoding, singleByteFont.getFontName());
                        PDFFontNonBase14 addFont = (PDFFontNonBase14)PDFFont.createFont(name, fonttype, basefont, pdfenc);
                        addFont.setDescriptor(pdfdesc);
                        addFont.setWidthMetrics(addEncoding.getFirstChar(), addEncoding.getLastChar(), new PDFArray((PDFObject)null, singleByteFont.getAdditionalWidths(i)));
                        this.getDocument().registerObject(addFont);
                        this.getDocument().getResources().addFont(addFont);
                        if (forceToUnicode) {
                           this.generateToUnicodeCmap(addFont, addEncoding);
                        }
                     }
                  }
               }
            } else {
               font.setEncoding(encoding);
               CIDFont cidMetrics;
               if (metrics instanceof LazyFont) {
                  cidMetrics = (CIDFont)((LazyFont)metrics).getRealFont();
               } else {
                  cidMetrics = (CIDFont)metrics;
               }

               PDFCIDSystemInfo sysInfo = new PDFCIDSystemInfo(cidMetrics.getRegistry(), cidMetrics.getOrdering(), cidMetrics.getSupplement());
               sysInfo.setDocument(this.document);

               assert pdfdesc instanceof PDFCIDFontDescriptor;

               PDFCIDFont cidFont = new PDFCIDFont(subsetFontName, cidMetrics.getCIDType(), cidMetrics.getDefaultWidth(), this.getFontWidths(cidMetrics), sysInfo, (PDFCIDFontDescriptor)pdfdesc);
               this.getDocument().registerObject(cidFont);
               Object cmap;
               if (cidMetrics instanceof MultiByteFont && ((MultiByteFont)cidMetrics).getCmapStream() != null) {
                  cmap = new PDFCMap("fop-ucs-H", (PDFCIDSystemInfo)null);

                  try {
                     ((PDFCMap)cmap).setData(IOUtils.toByteArray(((MultiByteFont)cidMetrics).getCmapStream()));
                  } catch (IOException var25) {
                     throw new RuntimeException(var25);
                  }
               } else {
                  cmap = new PDFToUnicodeCMap(cidMetrics.getCIDSet().getChars(), "fop-ucs-H", new PDFCIDSystemInfo("Adobe", "Identity", 0), false);
               }

               this.getDocument().registerObject((PDFObject)cmap);

               assert font instanceof PDFFontType0;

               ((PDFFontType0)font).setCMAP((PDFCMap)cmap);
               ((PDFFontType0)font).setDescendantFonts(cidFont);
            }

            return font;
         }
      }
   }

   private void handleType1CFont(PDFFontDescriptor pdfdesc, PDFFont font, FontMetrics metrics, String fontname, String basefont, FontDescriptor descriptor) {
      PDFFontNonBase14 nonBase14 = (PDFFontNonBase14)font;
      nonBase14.setDescriptor(pdfdesc);
      MultiByteFont singleByteFont;
      if (metrics instanceof LazyFont) {
         singleByteFont = (MultiByteFont)((LazyFont)metrics).getRealFont();
      } else {
         singleByteFont = (MultiByteFont)metrics;
      }

      Map usedGlyphs = singleByteFont.getUsedGlyphs();
      SortedSet keys = new TreeSet(usedGlyphs.keySet());
      keys.remove(0);
      int count = keys.size();
      Iterator usedGlyphNames = singleByteFont.getUsedGlyphNames().values().iterator();
      count = this.setupFontMetrics(nonBase14, pdfdesc, usedGlyphNames, 0, count, metrics);
      List additionalEncodings = this.addAdditionalEncodings(metrics, descriptor, fontname, basefont);

      for(int j = 0; j < additionalEncodings.size(); ++j) {
         PDFFontNonBase14 additional = (PDFFontNonBase14)additionalEncodings.get(j);
         int start = 256 * (j + 1);
         count = this.setupFontMetrics(additional, pdfdesc, usedGlyphNames, start, count, metrics);
      }

   }

   private int setupFontMetrics(PDFFontNonBase14 font, PDFFontDescriptor pdfdesc, Iterator usedGlyphNames, int start, int count, FontMetrics metrics) {
      font.setDescriptor(pdfdesc);
      PDFArray differences = new PDFArray();
      int firstChar = 0;
      differences.add((double)firstChar);
      int lastChar = Math.min(count, 255);
      int[] newWidths = new int[lastChar + 1];

      for(int i = 0; i < newWidths.length; ++i) {
         newWidths[i] = metrics.getWidth(start + i, 1);
         differences.add(new PDFName((String)usedGlyphNames.next()));
         --count;
      }

      font.setWidthMetrics(firstChar, lastChar, new PDFArray((PDFObject)null, newWidths));
      PDFEncoding pdfEncoding = new PDFEncoding("WinAnsiEncoding");
      this.getDocument().registerTrailerObject(pdfEncoding);
      pdfEncoding.setDifferences(differences);
      font.setEncoding(pdfEncoding);
      return count;
   }

   private List addAdditionalEncodings(FontMetrics metrics, FontDescriptor descriptor, String fontname, String basefont) {
      List additionalEncodings = new ArrayList();
      FontType fonttype = metrics.getFontType();
      if (descriptor != null && fonttype != FontType.TYPE0) {
         CustomFont singleByteFont;
         if (metrics instanceof LazyFont) {
            singleByteFont = (CustomFont)((LazyFont)metrics).getRealFont();
         } else {
            singleByteFont = (CustomFont)metrics;
         }

         if (singleByteFont.hasAdditionalEncodings()) {
            int i = additionalEncodings.size();

            for(int c = singleByteFont.getAdditionalEncodingCount(); i < c; ++i) {
               SimpleSingleByteEncoding addEncoding = singleByteFont.getAdditionalEncoding(i);
               String name = fontname + "_" + (i + 1);
               Object pdfenc = this.createPDFEncoding(addEncoding, singleByteFont.getFontName());
               PDFFontNonBase14 addFont = (PDFFontNonBase14)PDFFont.createFont(name, fonttype, basefont, pdfenc);
               this.getDocument().registerObject(addFont);
               this.getDocument().getResources().addFont(addFont);
               additionalEncodings.add(addFont);
            }
         }
      }

      return additionalEncodings;
   }

   private void generateToUnicodeCmap(PDFFont font, SingleByteEncoding encoding) {
      PDFCMap cmap = new PDFToUnicodeCMap(encoding.getUnicodeCharMap(), "fop-ucs-H", new PDFCIDSystemInfo("Adobe", "Identity", 0), true);
      this.getDocument().registerObject(cmap);
      font.setToUnicode(cmap);
   }

   public Object createPDFEncoding(SingleByteEncoding encoding, String fontName) {
      return PDFEncoding.createPDFEncoding(encoding, fontName);
   }

   private PDFWArray getFontWidths(CIDFont cidFont) {
      PDFWArray warray = new PDFWArray();
      if (cidFont instanceof MultiByteFont && ((MultiByteFont)cidFont).getWidthsMap() != null) {
         Map map = ((MultiByteFont)cidFont).getWidthsMap();
         Iterator var4 = map.entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry cid = (Map.Entry)var4.next();
            warray.addEntry((Integer)cid.getKey(), new int[]{(Integer)cid.getValue()});
         }
      } else {
         int[] widths = cidFont.getCIDSet().getWidths();
         warray.addEntry(0, widths);
      }

      return warray;
   }

   private String createSubsetFontPrefix() {
      ++this.subsetFontCounter;
      DecimalFormat counterFormat = new DecimalFormat("00000");
      String counterString = counterFormat.format((long)this.subsetFontCounter);
      StringBuffer sb = new StringBuffer("E");
      char[] var4 = counterString.toCharArray();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         char c = var4[var6];
         sb.append((char)(c + 17));
      }

      sb.append("+");
      return sb.toString();
   }

   private PDFFontDescriptor makeFontDescriptor(FontDescriptor desc, String fontPrefix) {
      PDFFontDescriptor descriptor = null;
      if (desc.getFontType() != FontType.TYPE0 && desc.getFontType() != FontType.CIDTYPE0) {
         descriptor = new PDFFontDescriptor(fontPrefix + desc.getEmbedFontName(), desc.getAscender(), desc.getDescender(), desc.getCapHeight(), desc.getFlags(), new PDFRectangle(desc.getFontBBox()), desc.getItalicAngle(), desc.getStemV());
      } else {
         descriptor = new PDFCIDFontDescriptor(fontPrefix + desc.getEmbedFontName(), desc.getFontBBox(), desc.getCapHeight(), desc.getFlags(), desc.getItalicAngle(), desc.getStemV(), (String)null);
      }

      this.getDocument().registerObject((PDFObject)descriptor);
      if (desc.isEmbeddable()) {
         AbstractPDFStream stream = this.makeFontFile(desc, fontPrefix);
         if (stream != null) {
            ((PDFFontDescriptor)descriptor).setFontFile(desc.getFontType(), stream);
            this.getDocument().registerObject(stream);
         }

         CustomFont font = this.getCustomFont(desc);
         if (font instanceof CIDFont) {
            CIDFont cidFont = (CIDFont)font;
            this.buildCIDSet((PDFFontDescriptor)descriptor, cidFont);
         }
      }

      return (PDFFontDescriptor)descriptor;
   }

   private void buildCIDSet(PDFFontDescriptor descriptor, CIDFont cidFont) {
      BitSet cidSet = cidFont.getCIDSet().getGlyphIndices();
      PDFStream pdfStream = this.makeStream((String)null, true);
      ByteArrayOutputStream baout = new ByteArrayOutputStream(cidSet.length() / 8 + 1);
      int value = 0;
      int i = 0;

      for(int c = cidSet.length(); i < c; ++i) {
         int shift = i % 8;
         boolean b = cidSet.get(i);
         if (b) {
            value |= 1 << 7 - shift;
         }

         if (shift == 7) {
            baout.write(value);
            value = 0;
         }
      }

      baout.write(value);

      try {
         pdfStream.setData(baout.toByteArray());
         descriptor.setCIDSet(pdfStream);
      } catch (IOException var14) {
         this.log.error("Failed to write CIDSet [" + cidFont + "] " + cidFont.getEmbedFontName(), var14);
      } finally {
         IOUtils.closeQuietly((OutputStream)baout);
      }

   }

   public AbstractPDFStream makeFontFile(FontDescriptor desc, String fontPrefix) {
      if (desc.getFontType() == FontType.OTHER) {
         throw new IllegalArgumentException("Trying to embed unsupported font type: " + desc.getFontType());
      } else {
         CustomFont font = this.getCustomFont(desc);
         InputStream in = null;

         Object embeddedFont;
         try {
            MultiByteFont mbfont;
            try {
               in = font.getInputStream();
               if (in != null) {
                  embeddedFont = null;
                  byte[] subsetData;
                  if (desc.getFontType() == FontType.TYPE0) {
                     mbfont = (MultiByteFont)font;
                     FontFileReader reader = new FontFileReader(in);
                     String header = OFFontLoader.readHeader(reader);
                     boolean isCFF = mbfont.isOTFFile();
                     if (font.getEmbeddingMode() == EmbeddingMode.FULL) {
                        subsetData = reader.getAllBytes();
                        if (isCFF) {
                           this.document.setPDFVersion(Version.V1_6);
                        }
                     } else {
                        subsetData = this.getFontSubsetBytes(reader, mbfont, header, fontPrefix, desc, isCFF);
                     }

                     embeddedFont = this.getFontStream(font, subsetData, isCFF);
                  } else if (desc.getFontType() == FontType.TYPE1) {
                     if (font.getEmbeddingMode() != EmbeddingMode.SUBSET) {
                        embeddedFont = this.fullyEmbedType1Font(in);
                     } else {
                        assert font instanceof SingleByteFont;

                        SingleByteFont sbfont = (SingleByteFont)font;
                        Type1SubsetFile pfbFile = new Type1SubsetFile();
                        subsetData = pfbFile.createSubset(in, sbfont);
                        InputStream subsetStream = new ByteArrayInputStream(subsetData);
                        PFBParser parser = new PFBParser();
                        PFBData pfb = parser.parsePFB(subsetStream);
                        embeddedFont = new PDFT1Stream();
                        ((PDFT1Stream)embeddedFont).setData(pfb);
                     }
                  } else {
                     byte[] file;
                     PDFCFFStream embeddedFont2;
                     PDFCFFStream var25;
                     if (desc.getFontType() == FontType.TYPE1C) {
                        if (font.getEmbeddingMode() != EmbeddingMode.SUBSET) {
                           file = IOUtils.toByteArray(in);
                           embeddedFont2 = new PDFCFFStream("Type1C");
                           embeddedFont2.setData(file);
                           var25 = embeddedFont2;
                           return var25;
                        }

                        FontFileReader reader = new FontFileReader(in);
                        String header = OFFontLoader.readHeader(reader);
                        subsetData = this.getFontSubsetBytes(reader, (MultiByteFont)font, header, fontPrefix, desc, true);
                        embeddedFont = this.getFontStream(font, subsetData, true);
                     } else {
                        if (desc.getFontType() == FontType.CIDTYPE0) {
                           file = IOUtils.toByteArray(in);
                           embeddedFont2 = new PDFCFFStream("CIDFontType0C");
                           embeddedFont2.setData(file);
                           var25 = embeddedFont2;
                           return var25;
                        }

                        file = IOUtils.toByteArray(in);
                        embeddedFont = new PDFTTFStream(file.length);
                        ((PDFTTFStream)embeddedFont).setData(file, file.length);
                     }
                  }

                  Object var22 = embeddedFont;
                  return (AbstractPDFStream)var22;
               }

               embeddedFont = null;
            } catch (IOException var15) {
               this.log.error("Failed to embed font [" + desc + "] " + desc.getEmbedFontName(), var15);
               mbfont = null;
               return mbfont;
            }
         } finally {
            if (in != null) {
               IOUtils.closeQuietly(in);
            }

         }

         return (AbstractPDFStream)embeddedFont;
      }
   }

   private AbstractPDFStream fullyEmbedType1Font(InputStream in) throws IOException {
      PFBParser parser = new PFBParser();
      PFBData pfb = parser.parsePFB(in);
      AbstractPDFStream embeddedFont = new PDFT1Stream();
      ((PDFT1Stream)embeddedFont).setData(pfb);
      return embeddedFont;
   }

   private byte[] getFontSubsetBytes(FontFileReader reader, MultiByteFont mbfont, String header, String fontPrefix, FontDescriptor desc, boolean isCFF) throws IOException {
      if (isCFF) {
         OTFSubSetFile otfFile = new OTFSubSetFile();
         otfFile.readFont(reader, fontPrefix + desc.getEmbedFontName(), mbfont);
         return otfFile.getFontSubset();
      } else {
         TTFSubSetFile otfFile = new TTFSubSetFile();
         otfFile.readFont(reader, mbfont.getTTCName(), header, mbfont.getUsedGlyphs());
         return otfFile.getFontSubset();
      }
   }

   private AbstractPDFStream getFontStream(CustomFont font, byte[] fontBytes, boolean isCFF) throws IOException {
      Object embeddedFont;
      if (isCFF) {
         embeddedFont = new PDFCFFStreamType0C(font);
         ((PDFCFFStreamType0C)embeddedFont).setData(fontBytes, fontBytes.length);
      } else {
         embeddedFont = new PDFTTFStream(fontBytes.length);
         ((PDFTTFStream)embeddedFont).setData(fontBytes, fontBytes.length);
      }

      return (AbstractPDFStream)embeddedFont;
   }

   private CustomFont getCustomFont(FontDescriptor desc) {
      Typeface tempFont;
      if (desc instanceof LazyFont) {
         tempFont = ((LazyFont)desc).getRealFont();
      } else {
         tempFont = (Typeface)desc;
      }

      if (!(tempFont instanceof CustomFont)) {
         throw new IllegalArgumentException("FontDescriptor must be instance of CustomFont, but is a " + desc.getClass().getName());
      } else {
         return (CustomFont)tempFont;
      }
   }

   public PDFStream makeStream(String type, boolean add) {
      PDFStream obj = new PDFStream();
      obj.setDocument(this.getDocument());
      obj.getFilterList().addDefaultFilters(this.getDocument().getFilterMap(), type);
      if (add) {
         this.getDocument().registerObject(obj);
      }

      return obj;
   }

   public PDFICCStream makePDFICCStream() {
      PDFICCStream iccStream = new PDFICCStream();
      this.getDocument().registerObject(iccStream);
      return iccStream;
   }

   public PDFICCBasedColorSpace makeICCBasedColorSpace(PDFResourceContext res, String explicitName, PDFICCStream iccStream) {
      PDFICCBasedColorSpace cs = new PDFICCBasedColorSpace(explicitName, iccStream);
      this.getDocument().registerObject(cs);
      if (res != null) {
         res.getPDFResources().addColorSpace(cs);
      } else {
         this.getDocument().getResources().addColorSpace(cs);
      }

      return cs;
   }

   public PDFSeparationColorSpace makeSeparationColorSpace(PDFResourceContext res, NamedColorSpace ncs) {
      String colorName = ncs.getColorName();
      Double zero = 0.0;
      Double one = 1.0;
      List domain = Arrays.asList(zero, one);
      List range = Arrays.asList(zero, one, zero, one, zero, one);
      float[] cZero = new float[]{1.0F, 1.0F, 1.0F};
      float[] cOne = ncs.getRGBColor().getColorComponents((float[])null);
      PDFFunction tintFunction = this.makeFunction(domain, range, cZero, cOne, 1.0);
      PDFSeparationColorSpace cs = new PDFSeparationColorSpace(colorName, tintFunction);
      this.getDocument().registerObject(cs);
      if (res != null) {
         res.getPDFResources().addColorSpace(cs);
      } else {
         this.getDocument().getResources().addColorSpace(cs);
      }

      return cs;
   }

   public PDFArray makeArray(int[] values) {
      PDFArray array = new PDFArray((PDFObject)null, values);
      this.getDocument().registerObject(array);
      return array;
   }

   public PDFGState makeGState(Map settings, PDFGState current) {
      PDFGState wanted = new PDFGState();
      wanted.addValues(PDFGState.DEFAULT);
      wanted.addValues(settings);
      PDFGState existing = this.getDocument().findGState(wanted, current);
      if (existing != null) {
         return existing;
      } else {
         PDFGState gstate = new PDFGState();
         gstate.addValues(settings);
         this.getDocument().registerObject(gstate);
         return gstate;
      }
   }

   public PDFAnnotList makeAnnotList() {
      PDFAnnotList obj = new PDFAnnotList();
      this.getDocument().assignObjectNumber(obj);
      return obj;
   }

   public PDFLayer makeLayer(String id) {
      PDFLayer layer = new PDFLayer(id);
      this.getDocument().registerObject(layer);
      return layer;
   }

   public PDFSetOCGStateAction makeSetOCGStateAction(String id) {
      PDFSetOCGStateAction action = new PDFSetOCGStateAction(id);
      this.getDocument().registerObject(action);
      return action;
   }

   public PDFTransitionAction makeTransitionAction(String id) {
      PDFTransitionAction action = new PDFTransitionAction(id);
      this.getDocument().registerObject(action);
      return action;
   }

   public PDFNavigator makeNavigator(String id) {
      PDFNavigator navigator = new PDFNavigator(id);
      this.getDocument().registerObject(navigator);
      return navigator;
   }

   public void makeDPart(PDFPage page, String pageMasterName) {
      PDFDPartRoot root = this.getDocument().getRoot().getDPartRoot();
      PDFDPart dPart;
      if (this.dparts.containsKey(pageMasterName)) {
         dPart = (PDFDPart)this.dparts.get(pageMasterName);
      } else {
         dPart = new PDFDPart(root.dpart);
         root.add(dPart);
         this.getDocument().registerTrailerObject(dPart);
         this.dparts.put(pageMasterName, dPart);
      }

      dPart.addPage(page);
      page.put("DPart", dPart);
   }

   public PDFDPartRoot makeDPartRoot() {
      PDFDPartRoot pdfdPartRoot = new PDFDPartRoot(this.getDocument());
      this.getDocument().registerTrailerObject(pdfdPartRoot);
      return pdfdPartRoot;
   }
}
