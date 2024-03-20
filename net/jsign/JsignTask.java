package net.jsign;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class JsignTask extends Task {
   private File file;
   private String name;
   private String url;
   private String algorithm;
   private File keystore;
   private String storepass;
   private String storetype;
   private String alias;
   private File certfile;
   private File keyfile;
   private String keypass;
   private String tsaurl;
   private String tsmode;
   private int tsretries = -1;
   private int tsretrywait = -1;
   private boolean replace;
   private String encoding = "UTF-8";
   private boolean detached;

   public void setFile(File file) {
      this.file = file;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public void setAlg(String alg) {
      this.algorithm = alg;
   }

   public void setTsmode(String tsmode) {
      this.tsmode = tsmode;
   }

   public void setKeystore(File keystore) {
      this.keystore = keystore;
   }

   public void setStorepass(String storepass) {
      this.storepass = storepass;
   }

   public void setStoretype(String storetype) {
      this.storetype = storetype;
   }

   public void setAlias(String alias) {
      this.alias = alias;
   }

   public void setCertfile(File certfile) {
      this.certfile = certfile;
   }

   public void setKeyfile(File keyfile) {
      this.keyfile = keyfile;
   }

   public void setKeypass(String keypass) {
      this.keypass = keypass;
   }

   public void setTsaurl(String tsaurl) {
      this.tsaurl = tsaurl;
   }

   public void setTsretries(int tsretries) {
      this.tsretries = tsretries;
   }

   public void setTsretrywait(int tsretrywait) {
      this.tsretrywait = tsretrywait;
   }

   public void setReplace(boolean replace) {
      this.replace = replace;
   }

   public void setEncoding(String encoding) {
      this.encoding = encoding;
   }

   public void setDetached(boolean detached) {
      this.detached = detached;
   }

   public void execute() throws BuildException {
      try {
         SignerHelper helper = new SignerHelper(new AntConsole(this), "attribute");
         helper.name(this.name);
         helper.url(this.url);
         helper.alg(this.algorithm);
         helper.keystore(this.keystore);
         helper.storepass(this.storepass);
         helper.storetype(this.storetype);
         helper.alias(this.alias);
         helper.certfile(this.certfile);
         helper.keyfile(this.keyfile);
         helper.keypass(this.keypass);
         helper.tsaurl(this.tsaurl);
         helper.tsmode(this.tsmode);
         helper.tsretries(this.tsretries);
         helper.tsretrywait(this.tsretrywait);
         helper.replace(this.replace);
         helper.encoding(this.encoding);
         helper.detached(this.detached);
         helper.sign(this.file);
      } catch (Exception var2) {
         throw new BuildException(var2.getMessage(), var2, this.getLocation());
      }
   }
}
