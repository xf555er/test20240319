package net.jsign.commons.cli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Options implements Serializable {
   private final Map shortOpts = new LinkedHashMap();
   private final Map longOpts = new LinkedHashMap();
   private final List requiredOpts = new ArrayList();
   private final Map optionGroups = new LinkedHashMap();

   Collection getOptionGroups() {
      return new HashSet(this.optionGroups.values());
   }

   public Options addOption(Option opt) {
      String key = opt.getKey();
      if (opt.hasLongOpt()) {
         this.longOpts.put(opt.getLongOpt(), opt);
      }

      if (opt.isRequired()) {
         if (this.requiredOpts.contains(key)) {
            this.requiredOpts.remove(this.requiredOpts.indexOf(key));
         }

         this.requiredOpts.add(key);
      }

      this.shortOpts.put(key, opt);
      return this;
   }

   public Collection getOptions() {
      return Collections.unmodifiableCollection(this.helpOptions());
   }

   List helpOptions() {
      return new ArrayList(this.shortOpts.values());
   }

   public List getRequiredOptions() {
      return Collections.unmodifiableList(this.requiredOpts);
   }

   public Option getOption(String opt) {
      opt = Util.stripLeadingHyphens(opt);
      return this.shortOpts.containsKey(opt) ? (Option)this.shortOpts.get(opt) : (Option)this.longOpts.get(opt);
   }

   public List getMatchingOptions(String opt) {
      opt = Util.stripLeadingHyphens(opt);
      List matchingOpts = new ArrayList();
      if (this.longOpts.keySet().contains(opt)) {
         return Collections.singletonList(opt);
      } else {
         Iterator var3 = this.longOpts.keySet().iterator();

         while(var3.hasNext()) {
            String longOpt = (String)var3.next();
            if (longOpt.startsWith(opt)) {
               matchingOpts.add(longOpt);
            }
         }

         return matchingOpts;
      }
   }

   public boolean hasOption(String opt) {
      opt = Util.stripLeadingHyphens(opt);
      return this.shortOpts.containsKey(opt) || this.longOpts.containsKey(opt);
   }

   public boolean hasLongOption(String opt) {
      opt = Util.stripLeadingHyphens(opt);
      return this.longOpts.containsKey(opt);
   }

   public boolean hasShortOption(String opt) {
      opt = Util.stripLeadingHyphens(opt);
      return this.shortOpts.containsKey(opt);
   }

   public OptionGroup getOptionGroup(Option opt) {
      return (OptionGroup)this.optionGroups.get(opt.getKey());
   }

   public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("[ Options: [ short ");
      buf.append(this.shortOpts.toString());
      buf.append(" ] [ long ");
      buf.append(this.longOpts);
      buf.append(" ]");
      return buf.toString();
   }
}
