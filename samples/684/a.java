import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

class WSL_MediaWikiConfig extends JPanel implements ActionListener, FocusListener {
    /**
     * Called when the user clicks the button or presses
     * Enter in a text field.
     */
    public void actionPerformed(ActionEvent e) {
	try {
	    button.setEnabled(false);
	    installationDirButton.setEnabled(false);
	    if ("clear".equals(e.getActionCommand())) {
		wikiName.setText("");
		email.setText("");
		host.setText("");
		dbName.setText("");
		dbUser.setText("");
		installationDir.setText("");
		passwordField.setText("");
		confirmPasswordField.setText("");
	    } else {
		if (checkFields()) {
		    //create method to display things in the display
		    addressDisplay.setText("&lt;html&gt;&lt;p align=center&gt;Everything is OK&lt;/html&gt;&lt;/p&gt;");
		    downloadMW();
		    installMW();
		    existingMW.setLocalSettings();
		    destroy();
		    //            		dataSet = true;
		}
	    }
	} finally {
	    button.setEnabled(true);
	    installationDirButton.setEnabled(true);
	}

    }

    private JButton button, installationDirButton;
    private JButton button, installationDirButton;
    private JTextField wikiName, email, host, dbName, dbUser, installationDir;
    private JTextField wikiName, email, host, dbName, dbUser, installationDir;
    private JTextField wikiName, email, host, dbName, dbUser, installationDir;
    private JTextField wikiName, email, host, dbName, dbUser, installationDir;
    private JTextField wikiName, email, host, dbName, dbUser, installationDir;
    private JTextField wikiName, email, host, dbName, dbUser, installationDir;
    private JPasswordField passwordField, confirmPasswordField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel addressDisplay;
    private static WSL_ExistMWConfig existingMW;
    private final String mediaWikiFileName = "mediawiki-1.16.1.tar.gz";
    private WSL_Util wsl_util;
    private final String localSettings = curDir + "/plugins/WSL/resources/LocalSettings.php";
    private static JFrame frame;
    private final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"
	    + "[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final String curDir = System.getProperty("user.dir");

    /**
     * This methods checks if all the fields are correct
     * @return boolean
     */
    private boolean checkFields() {
	boolean everythingOK = true;
	StringBuffer warning = new StringBuffer();
	warning.append("&lt;html&gt;&lt;p align=center&gt;");
	if (!validateEmail(email.getText())) {
	    everythingOK = false;
	    warning.append("Invalid email: " + email.getText() + " &lt;BR&gt; ");
	}
	if (!Arrays.equals(passwordField.getPassword(), confirmPasswordField.getPassword())) {
	    everythingOK = false;
	    warning.append("Passwords do not match &lt;BR&gt; ");
	}
	if (wikiName.getText().equals("")) {
	    everythingOK = false;
	    warning.append("Wiki name is empty &lt;BR&gt; ");
	}
	if (email.getText().equals("")) {
	    everythingOK = false;
	    warning.append("Email is empty &lt;BR&gt; ");
	}
	if (host.getText().equals("")) {
	    everythingOK = false;
	    warning.append("Host is empty &lt;BR&gt; ");
	}
	if (dbName.getText().equals("")) {
	    everythingOK = false;
	    warning.append("DB name is empty &lt;BR&gt; ");
	}
	if (dbUser.getText().equals("")) {
	    everythingOK = false;
	    warning.append("DB user is empty &lt;BR&gt; ");
	}
	if (installationDir.getText().equals("")) {
	    everythingOK = false;
	    warning.append("Installation dir is empty &lt;BR&gt; ");
	}
	if (passwordField.getPassword().equals("")) {
	    everythingOK = false;
	    warning.append("Password is empty &lt;BR&gt; ");
	}
	if (confirmPasswordField.getPassword().equals("")) {
	    everythingOK = false;
	    warning.append("Confirmation password is empty &lt;BR&gt; ");
	}
	warning.append("&lt;/p&gt;&lt;/html&gt;");
	addressDisplay.setText(warning.toString());
	return everythingOK;
    }

    /**
     * This methods downloads mediaWiki 1.16.1 from
     * a Onekin repository
     */
    private void downloadMW() {
	BufferedInputStream in;
	try {
	    in = new BufferedInputStream(
		    new java.net.URL("http://www.onekin.org/wsl/downloads/" + mediaWikiFileName).openStream());
	    FileOutputStream fos = new FileOutputStream(mediaWikiFileName);
	    BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
	    byte[] data = new byte[1024];
	    int x = 0;
	    while ((x = in.read(data, 0, 1024)) &gt;= 0) {
		bout.write(data, 0, x);
	    }
	    bout.close();
	    in.close();
	    fos.close();
	    uncompressMW(mediaWikiFileName, new File(installationDir.getText()));
	    deleteFile(mediaWikiFileName);

	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void installMW() {
	try {
	    String userLocalSettings = installationDir.getText() + File.separator + wikiName.getText()
		    + "/LocalSettings.php";
	    File file = new File(userLocalSettings);
	    wsl_util.copyFile(new File(localSettings), file);
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    String line = "", newText = "";
	    while ((line = reader.readLine()) != null) {// Substitute variables with user inputs           	 
		if (line.contains("$wgScriptPath       =")) {
		    line = "$wgScriptPath = \"/" + wikiName.getText() + "\";";
		} else if (line.contains("$wgSitename         =")) {
		    line = "$wgSitename = \"" + wikiName.getText() + "\";";
		} else if (line.contains("$wgEmergencyContact =")) {
		    line = "$wgEmergencyContact = \"" + email.getText() + "\";";
		} else if (line.contains("$wgPasswordSender =")) {
		    line = "$wgPasswordSender = \"" + email.getText() + "\";";
		} else if (line.contains("$wgDBserver         =")) {
		    line = "$wgDBserver         = \"" + host.getText() + "\";";
		} else if (line.contains("$wgDBname           =")) {
		    line = "$wgDBname           = \"" + dbName.getText() + "\";";
		} else if (line.contains("$wgDBuser           =")) {
		    line = "$wgDBuser           = \"" + dbUser.getText() + "\";";
		} else if (line.contains("$wgDBpassword       =")) {
		    line = "$wgDBpassword       = \"" + new String(passwordField.getPassword()) + "\";";
		} //Remove default logo and skin
		else if (line.contains("$wgLogo             = \"$wgStylePath/common/images/wiki.png\";")) {
		    line = "#$wgLogo             = \"$wgStylePath/common/images/wiki.png\";";
		} else if (line.contains("$wgDefaultSkin = 'monobook';")) {
		    line = "#$wgDefaultSkin = 'monobook';";
		}
		newText = newText + line + "\n";
	    }
	    reader.close();

	    FileWriter writer = new FileWriter(userLocalSettings);
	    writer.write(newText);
	    writer.close();
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	}
	createDB();
    }

    public void destroy() {
	if (frame != null) {
	    frame.dispose();
	}
    }

    /**
     * 
     * @param text
     * @return Boolean
     * Email validator, extracted from 
     * www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression
     */
    private Boolean validateEmail(String email) {
	Pattern pattern;
	Matcher matcher;
	pattern = Pattern.compile(EMAIL_PATTERN);
	//Validate email with regular expression
	matcher = pattern.matcher(email);
	return matcher.matches();
    }

    /**
     * This methods uncompresses tar.gz files
     * @param mediaWikiFileName
     * Uncompress tar.gz
     */
    private void uncompressMW(String mediaWikiFileName, File dest) {
	try {
	    //assuming the file you pass in is not a dir
	    dest.mkdir();
	    //create tar input stream from a .tar.gz file
	    TarInputStream tin = new TarInputStream(
		    new GZIPInputStream(new FileInputStream(new File(mediaWikiFileName))));

	    //get the first entry in the archive
	    TarEntry tarEntry = tin.getNextEntry();
	    while (tarEntry != null) {//create a file with the same name as the tarEntry
		File destPath = new File(dest.toString() + File.separatorChar + tarEntry.getName());
		if (tarEntry.isDirectory()) {
		    destPath.mkdir();
		} else {
		    FileOutputStream fout = new FileOutputStream(destPath);
		    tin.copyEntryContents(fout);
		    fout.close();
		}
		tarEntry = tin.getNextEntry();
	    }
	    tin.close();
	    // Now, rename the folder as the wiki name (e.g., from desktop/mediawiki-1.16.1 to desktop/wikiName)
	    File oldMWDir = new File(installationDir.getText() + File.separatorChar
		    + mediaWikiFileName.substring(0, mediaWikiFileName.length() - 7));
	    oldMWDir.renameTo(new File(installationDir.getText() + File.separatorChar + wikiName.getText()));

	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * This methods deletes a file
     * @param mediaWikiFileName
     */
    private void deleteFile(String fileToDelete) {
	File file = new File(fileToDelete);
	// Make sure the file or directory exists and isn't write protected
	if (!file.exists()) {
	    throw new IllegalArgumentException("Delete: no such file or directory: " + fileToDelete);
	}
	if (!file.canWrite()) {
	    throw new IllegalArgumentException("Delete: write protected: " + fileToDelete);
	}
	// If it is a directory, make sure it is empty
	if (file.isDirectory()) {
	    String[] files = file.list();
	    if (files.length &gt; 0)
		throw new IllegalArgumentException("Delete: directory not empty: " + fileToDelete);
	}
	// Attempt to delete it
	boolean success = file.delete();
	if (!success) {
	    throw new IllegalArgumentException("Delete: deletion failed");
	}
    }

    /**
    * This method creates a new database in mysql, with MediaWiki tables
    * and with an admin user "WikiSysop"
    */
    private void createDB() {
	String command;
	Process child;
	int exitVal1 = -1, exitVal2 = -1, exitVal3 = -1;
	try {
	    // Create database
	    command = "mysql --user=" + dbUser.getText() + " --password=" + new String(passwordField.getPassword())
		    + " --host=" + host.getText() + " --execute=\"CREATE DATABASE IF NOT EXISTS " + dbName.getText()
		    + "\"";
	    child = Runtime.getRuntime().exec(command);
	    exitVal1 = child.waitFor();
	    // Create tables
	    command = "mysql " + dbName.getText() + " --user=" + dbUser.getText() + " --password="
		    + new String(passwordField.getPassword()) + " --host=" + host.getText() + " -e \"SOURCE " + curDir
		    + "/plugins/WSL/resources/tables.sql\"";
	    child = Runtime.getRuntime().exec(command);
	    exitVal2 = child.waitFor();
	    // Create WikiSysop user
	    command = "mysql " + dbName.getText() + " --user=" + dbUser.getText() + " --password="
		    + new String(passwordField.getPassword()) + " --host=" + host.getText() + " -e \"SOURCE " + curDir
		    + "/plugins/WSL/resources/WikiSysop.sql\"";
	    child = Runtime.getRuntime().exec(command);
	    exitVal3 = child.waitFor();
	} catch (IOException e) {
	    System.out.println("Exit codes =&gt; Create database: " + exitVal1 + " Create tables: " + exitVal2
		    + " Create WikiSysop user:" + exitVal3);
	    System.out.println(e.toString());
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    System.out.println("Exit codes =&gt; Create database: " + exitVal1 + " Create tables: " + exitVal2
		    + " Create WikiSysop user:" + exitVal3);
	    System.out.println(e.toString());
	    e.printStackTrace();
	}
    }

}

