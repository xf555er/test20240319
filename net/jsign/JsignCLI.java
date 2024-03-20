package net.jsign;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import net.jsign.commons.cli.CommandLine;
import net.jsign.commons.cli.DefaultParser;
import net.jsign.commons.cli.HelpFormatter;
import net.jsign.commons.cli.Option;
import net.jsign.commons.cli.Options;
import net.jsign.commons.cli.ParseException;

public class JsignCLI {
   private final Options options = new Options();

   public static void main(String... args) {
      try {
         (new JsignCLI()).execute(args);
      } catch (ParseException | SignerException var2) {
         System.err.println("jsign: " + var2.getMessage());
         if (var2.getCause() != null) {
            var2.getCause().printStackTrace(System.err);
         }

         System.err.println("Try `" + getProgramName() + " --help' for more information.");
         System.exit(1);
      }

   }

   JsignCLI() {
      this.options.addOption(Option.builder("s").hasArg().longOpt("keystore").argName("FILE").desc("The keystore file, the SunPKCS11 configuration file or the cloud keystore name").type(File.class).build());
      this.options.addOption(Option.builder().hasArg().longOpt("storepass").argName("PASSWORD").desc("The password to open the keystore").build());
      this.options.addOption(Option.builder().hasArg().longOpt("storetype").argName("TYPE").desc("The type of the keystore:\n- JKS: Java keystore (.jks files)\n- JCEKS: SunJCE keystore (.jks files)\n- PKCS12: Standard PKCS#12 keystore (.p12 or .pfx files)\n- PKCS11: PKCS#11 hardware token\n- YUBIKEY: YubiKey security key\n- AZUREKEYVAULT: Azure Key Vault key management system\n- DIGICERTONE: DigiCert ONE Secure Software Manager\n- GOOGLECLOUD: Google Cloud KMS\n").build());
      this.options.addOption(Option.builder("a").hasArg().longOpt("alias").argName("NAME").desc("The alias of the certificate used for signing in the keystore.").build());
      this.options.addOption(Option.builder().hasArg().longOpt("keypass").argName("PASSWORD").desc("The password of the private key. When using a keystore, this parameter can be omitted if the keystore shares the same password.").build());
      this.options.addOption(Option.builder().hasArg().longOpt("keyfile").argName("FILE").desc("The file containing the private key. PEM and PVK files are supported. ").type(File.class).build());
      this.options.addOption(Option.builder("c").hasArg().longOpt("certfile").argName("FILE").desc("The file containing the PKCS#7 certificate chain\n(.p7b or .spc files).").type(File.class).build());
      this.options.addOption(Option.builder("d").hasArg().longOpt("alg").argName("ALGORITHM").desc("The digest algorithm (SHA-1, SHA-256, SHA-384 or SHA-512)").build());
      this.options.addOption(Option.builder("t").hasArg().longOpt("tsaurl").argName("URL").desc("The URL of the timestamping authority.").build());
      this.options.addOption(Option.builder("m").hasArg().longOpt("tsmode").argName("MODE").desc("The timestamping mode (RFC3161 or Authenticode)").build());
      this.options.addOption(Option.builder("r").hasArg().longOpt("tsretries").argName("NUMBER").desc("The number of retries for timestamping").build());
      this.options.addOption(Option.builder("w").hasArg().longOpt("tsretrywait").argName("SECONDS").desc("The number of seconds to wait between timestamping retries").build());
      this.options.addOption(Option.builder("n").hasArg().longOpt("name").argName("NAME").desc("The name of the application").build());
      this.options.addOption(Option.builder("u").hasArg().longOpt("url").argName("URL").desc("The URL of the application").build());
      this.options.addOption(Option.builder().hasArg().longOpt("proxyUrl").argName("URL").desc("The URL of the HTTP proxy").build());
      this.options.addOption(Option.builder().hasArg().longOpt("proxyUser").argName("NAME").desc("The user for the HTTP proxy. If an user is needed.").build());
      this.options.addOption(Option.builder().hasArg().longOpt("proxyPass").argName("PASSWORD").desc("The password for the HTTP proxy user. If an user is needed.").build());
      this.options.addOption(Option.builder().longOpt("replace").desc("Tells if previous signatures should be replaced.").build());
      this.options.addOption(Option.builder("e").hasArg().longOpt("encoding").argName("ENCODING").desc("The encoding of the script to be signed (UTF-8 by default, or the encoding specified by the byte order mark if there is one).").build());
      this.options.addOption(Option.builder().longOpt("detached").desc("Tells if a detached signature should be generated or reused.").build());
      this.options.addOption(Option.builder("h").longOpt("help").desc("Print the help").build());
   }

   void execute(String... args) throws SignerException, ParseException {
      DefaultParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(this.options, args);
      if (!cmd.hasOption("help") && args.length != 0) {
         SignerHelper helper = new SignerHelper(new StdOutConsole(1), "option");
         this.setOption("keystore", helper, cmd);
         this.setOption("storepass", helper, cmd);
         this.setOption("storetype", helper, cmd);
         this.setOption("alias", helper, cmd);
         this.setOption("keypass", helper, cmd);
         this.setOption("keyfile", helper, cmd);
         this.setOption("certfile", helper, cmd);
         this.setOption("alg", helper, cmd);
         this.setOption("tsaurl", helper, cmd);
         this.setOption("tsmode", helper, cmd);
         this.setOption("tsretries", helper, cmd);
         this.setOption("tsretrywait", helper, cmd);
         this.setOption("name", helper, cmd);
         this.setOption("url", helper, cmd);
         this.setOption("proxyUrl", helper, cmd);
         this.setOption("proxyUser", helper, cmd);
         this.setOption("proxyPass", helper, cmd);
         helper.replace(cmd.hasOption("replace"));
         this.setOption("encoding", helper, cmd);
         helper.detached(cmd.hasOption("detached"));
         if (cmd.getArgList().isEmpty()) {
            throw new SignerException("No file specified");
         } else {
            Iterator var5 = cmd.getArgList().iterator();

            while(var5.hasNext()) {
               String filename = (String)var5.next();
               helper.sign(new File(filename));
            }

         }
      } else {
         this.printHelp();
      }
   }

   private void setOption(String key, SignerHelper helper, CommandLine cmd) {
      String value = cmd.getOptionValue(key);
      helper.param(key, value);
   }

   private void printHelp() {
      String header = "Sign and timestamp Windows executable files, Microsoft Installers (MSI), Cabinet files (CAB) or scripts (PowerShell, VBScript, JScript, WSF).\n\n";
      String footer = "\nExamples:\n\n   Signing with a PKCS#12 keystore and timestamping:\n\n     jsign --keystore keystore.p12 --alias test --storepass pwd \\\n           --tsaurl http://timestamp.comodoca.com/authenticode application.exe\n\n   Signing with a SPC certificate and a PVK key:\n\n     jsign --certfile certificate.spc --keyfile key.pvk --keypass pwd installer.msi\n\nPlease report suggestions and issues on the GitHub project at https://github.com/ebourg/jsign/issues";
      HelpFormatter formatter = new HelpFormatter();
      formatter.setOptionComparator((Comparator)null);
      formatter.setWidth(85);
      formatter.setDescPadding(1);
      formatter.printHelp(getProgramName() + " [OPTIONS] [FILE]...", header, this.options, footer);
   }

   private static String getProgramName() {
      return System.getProperty("basename", "java -jar jsign.jar");
   }
}
