package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class JAuthenticator extends Authenticator {
   public static final String TITLE = "JAuthenticator.title";
   public static final String LABEL_SITE = "JAuthenticator.label.site";
   public static final String LABEL_REQ = "JAuthenticator.label.req";
   public static final String LABEL_USERID = "JAuthenticator.label.userID";
   public static final String LABEL_PASSWORD = "JAuthenticator.label.password";
   public static final String LABEL_CANCEL = "JAuthenticator.label.cancel";
   public static final String LABEL_OK = "JAuthenticator.label.ok";
   protected JDialog window;
   protected JButton cancelButton;
   protected JButton okButton;
   protected JLabel label1;
   protected JLabel label2;
   protected JTextField JUserID;
   protected JPasswordField JPassword;
   final Object lock = new Object();
   private boolean result;
   private volatile boolean wasNotified;
   private String userID;
   private char[] password;
   ActionListener okListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         synchronized(JAuthenticator.this.lock) {
            JAuthenticator.this.window.setVisible(false);
            JAuthenticator.this.userID = JAuthenticator.this.JUserID.getText();
            JAuthenticator.this.password = JAuthenticator.this.JPassword.getPassword();
            JAuthenticator.this.JPassword.setText("");
            JAuthenticator.this.result = true;
            JAuthenticator.this.wasNotified = true;
            JAuthenticator.this.lock.notifyAll();
         }
      }
   };
   ActionListener cancelListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         synchronized(JAuthenticator.this.lock) {
            JAuthenticator.this.window.setVisible(false);
            JAuthenticator.this.userID = null;
            JAuthenticator.this.JUserID.setText("");
            JAuthenticator.this.password = null;
            JAuthenticator.this.JPassword.setText("");
            JAuthenticator.this.result = false;
            JAuthenticator.this.wasNotified = true;
            JAuthenticator.this.lock.notifyAll();
         }
      }
   };

   public JAuthenticator() {
      this.initWindow();
   }

   protected void initWindow() {
      String title = Resources.getString("JAuthenticator.title");
      this.window = new JDialog((Frame)null, title, true);
      Container mainPanel = this.window.getContentPane();
      mainPanel.setLayout(new BorderLayout());
      mainPanel.add(this.buildAuthPanel(), "Center");
      mainPanel.add(this.buildButtonPanel(), "South");
      this.window.pack();
      this.window.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            JAuthenticator.this.cancelListener.actionPerformed(new ActionEvent(e.getWindow(), 1001, "Close"));
         }
      });
   }

   protected JComponent buildAuthPanel() {
      GridBagLayout gridBag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      JPanel proxyPanel = new JPanel(gridBag);
      c.fill = 1;
      c.weightx = 1.0;
      c.gridwidth = 1;
      JLabel labelS = new JLabel(Resources.getString("JAuthenticator.label.site"));
      labelS.setHorizontalAlignment(2);
      gridBag.setConstraints(labelS, c);
      proxyPanel.add(labelS);
      c.gridwidth = 0;
      this.label1 = new JLabel("");
      this.label1.setHorizontalAlignment(2);
      gridBag.setConstraints(this.label1, c);
      proxyPanel.add(this.label1);
      c.gridwidth = 1;
      JLabel labelR = new JLabel(Resources.getString("JAuthenticator.label.req"));
      labelR.setHorizontalAlignment(2);
      gridBag.setConstraints(labelR, c);
      proxyPanel.add(labelR);
      c.gridwidth = 0;
      this.label2 = new JLabel("");
      this.label2.setHorizontalAlignment(2);
      gridBag.setConstraints(this.label2, c);
      proxyPanel.add(this.label2);
      c.gridwidth = 1;
      JLabel labelUserID = new JLabel(Resources.getString("JAuthenticator.label.userID"));
      labelUserID.setHorizontalAlignment(2);
      gridBag.setConstraints(labelUserID, c);
      proxyPanel.add(labelUserID);
      c.gridwidth = 0;
      this.JUserID = new JTextField(20);
      gridBag.setConstraints(this.JUserID, c);
      proxyPanel.add(this.JUserID);
      c.gridwidth = 1;
      JLabel labelPassword = new JLabel(Resources.getString("JAuthenticator.label.password"));
      labelPassword.setHorizontalAlignment(2);
      gridBag.setConstraints(labelPassword, c);
      proxyPanel.add(labelPassword);
      c.gridwidth = 0;
      this.JPassword = new JPasswordField(20);
      this.JPassword.setEchoChar('*');
      this.JPassword.addActionListener(this.okListener);
      gridBag.setConstraints(this.JPassword, c);
      proxyPanel.add(this.JPassword);
      return proxyPanel;
   }

   protected JComponent buildButtonPanel() {
      JPanel buttonPanel = new JPanel();
      this.cancelButton = new JButton(Resources.getString("JAuthenticator.label.cancel"));
      this.cancelButton.addActionListener(this.cancelListener);
      buttonPanel.add(this.cancelButton);
      this.okButton = new JButton(Resources.getString("JAuthenticator.label.ok"));
      this.okButton.addActionListener(this.okListener);
      buttonPanel.add(this.okButton);
      return buttonPanel;
   }

   public PasswordAuthentication getPasswordAuthentication() {
      synchronized(this.lock) {
         EventQueue.invokeLater(new Runnable() {
            public void run() {
               JAuthenticator.this.label1.setText(JAuthenticator.this.getRequestingSite().getHostName());
               JAuthenticator.this.label2.setText(JAuthenticator.this.getRequestingPrompt());
               JAuthenticator.this.window.setVisible(true);
            }
         });
         this.wasNotified = false;

         while(!this.wasNotified) {
            try {
               this.lock.wait();
            } catch (InterruptedException var4) {
            }
         }

         return !this.result ? null : new PasswordAuthentication(this.userID, this.password);
      }
   }
}
