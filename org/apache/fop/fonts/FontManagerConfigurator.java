package org.apache.fop.fonts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.fonts.substitute.FontSubstitutions;
import org.apache.fop.fonts.substitute.FontSubstitutionsConfigurator;
import org.apache.fop.util.LogUtil;
import org.apache.xmlgraphics.io.ResourceResolver;

public class FontManagerConfigurator {
   private static Log log = LogFactory.getLog(FontManagerConfigurator.class);
   private final Configuration cfg;
   private final URI baseURI;
   private final URI fallbackURI;
   private final ResourceResolver resourceResolver;

   public FontManagerConfigurator(Configuration cfg, URI baseURI, URI fallbackURI, ResourceResolver resourceResolver) {
      this.cfg = cfg;
      this.baseURI = baseURI;
      this.fallbackURI = fallbackURI;
      this.resourceResolver = resourceResolver;
   }

   public void configure(FontManager fontManager, boolean strict) throws FOPException {
      if (this.cfg.getChild("font-base", false) != null) {
         try {
            URI fontBase = InternalResourceResolver.getBaseURI(this.cfg.getChild("font-base").getValue((String)null));
            fontManager.setResourceResolver(ResourceResolverFactory.createInternalResourceResolver(this.baseURI.resolve(fontBase), this.resourceResolver));
         } catch (URISyntaxException var9) {
            LogUtil.handleException(log, var9, true);
         }
      } else {
         fontManager.setResourceResolver(ResourceResolverFactory.createInternalResourceResolver(this.fallbackURI, this.resourceResolver));
      }

      if (this.cfg.getChild("use-cache", false) != null) {
         try {
            if (!this.cfg.getChild("use-cache").getValueAsBoolean()) {
               fontManager.disableFontCache();
            } else if (this.cfg.getChild("cache-file", false) != null) {
               fontManager.setCacheFile(URI.create(this.cfg.getChild("cache-file").getValue()));
            }
         } catch (ConfigurationException var8) {
            LogUtil.handleException(log, var8, true);
         }
      }

      if (this.cfg.getChild("base14-kerning", false) != null) {
         try {
            fontManager.setBase14KerningEnabled(this.cfg.getChild("base14-kerning").getValueAsBoolean());
         } catch (ConfigurationException var7) {
            LogUtil.handleException(log, var7, true);
         }
      }

      Configuration fontsCfg = this.cfg.getChild("fonts", false);
      if (fontsCfg != null) {
         Configuration substitutionsCfg = fontsCfg.getChild("substitutions", false);
         if (substitutionsCfg != null) {
            FontSubstitutions substitutions = new FontSubstitutions();
            (new FontSubstitutionsConfigurator(substitutionsCfg)).configure(substitutions);
            fontManager.setFontSubstitutions(substitutions);
         }

         Configuration referencedFontsCfg = fontsCfg.getChild("referenced-fonts", false);
         if (referencedFontsCfg != null) {
            FontTriplet.Matcher matcher = createFontsMatcher(referencedFontsCfg, strict);
            fontManager.setReferencedFontsMatcher(matcher);
         }
      }

   }

   public static FontTriplet.Matcher createFontsMatcher(Configuration cfg, boolean strict) throws FOPException {
      List matcherList = new ArrayList();
      Configuration[] matches = cfg.getChildren("match");
      Configuration[] var4 = matches;
      int var5 = matches.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Configuration matche = var4[var6];

         try {
            matcherList.add(new FontFamilyRegExFontTripletMatcher(matche.getAttribute("font-family")));
         } catch (ConfigurationException var9) {
            LogUtil.handleException(log, var9, strict);
         }
      }

      FontTriplet.Matcher orMatcher = new OrFontTripletMatcher((FontTriplet.Matcher[])matcherList.toArray(new FontTriplet.Matcher[matcherList.size()]));
      return orMatcher;
   }

   public static FontTriplet.Matcher createFontsMatcher(List fontFamilies, boolean strict) throws FOPException {
      List matcherList = new ArrayList();
      Iterator var3 = fontFamilies.iterator();

      while(var3.hasNext()) {
         String fontFamily = (String)var3.next();
         matcherList.add(new FontFamilyRegExFontTripletMatcher(fontFamily));
      }

      FontTriplet.Matcher orMatcher = new OrFontTripletMatcher((FontTriplet.Matcher[])matcherList.toArray(new FontTriplet.Matcher[matcherList.size()]));
      return orMatcher;
   }

   private static class FontFamilyRegExFontTripletMatcher implements FontTriplet.Matcher {
      private final Pattern regex;

      public FontFamilyRegExFontTripletMatcher(String regex) {
         this.regex = Pattern.compile(regex);
      }

      public boolean matches(FontTriplet triplet) {
         return this.regex.matcher(triplet.getName()).matches();
      }
   }

   private static class OrFontTripletMatcher implements FontTriplet.Matcher {
      private final FontTriplet.Matcher[] matchers;

      public OrFontTripletMatcher(FontTriplet.Matcher[] matchers) {
         this.matchers = matchers;
      }

      public boolean matches(FontTriplet triplet) {
         FontTriplet.Matcher[] var2 = this.matchers;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            FontTriplet.Matcher matcher = var2[var4];
            if (matcher.matches(triplet)) {
               return true;
            }
         }

         return false;
      }
   }
}
