package net.jsign.commons.cli;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CommandLine implements Serializable {
   private final List args = new LinkedList();
   private final List options = new ArrayList();

   protected CommandLine() {
   }

   public boolean hasOption(String opt) {
      return this.options.contains(this.resolveOption(opt));
   }

   public String getOptionValue(String opt) {
      String[] values = this.getOptionValues(opt);
      return values == null ? null : values[0];
   }

   public String[] getOptionValues(String opt) {
      List values = new ArrayList();
      Iterator var3 = this.options.iterator();

      while(true) {
         Option option;
         do {
            if (!var3.hasNext()) {
               return values.isEmpty() ? null : (String[])values.toArray(new String[values.size()]);
            }

            option = (Option)var3.next();
         } while(!opt.equals(option.getOpt()) && !opt.equals(option.getLongOpt()));

         values.addAll(option.getValuesList());
      }
   }

   private Option resolveOption(String opt) {
      opt = Util.stripLeadingHyphens(opt);
      Iterator var2 = this.options.iterator();

      Option option;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         option = (Option)var2.next();
         if (opt.equals(option.getOpt())) {
            return option;
         }
      } while(!opt.equals(option.getLongOpt()));

      return option;
   }

   public List getArgList() {
      return this.args;
   }

   protected void addArg(String arg) {
      this.args.add(arg);
   }

   protected void addOption(Option opt) {
      this.options.add(opt);
   }
}
