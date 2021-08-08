import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

class FreeMindProgressMonitor extends JDialog {
    /**
     * Test method for this dialog.
     */
    public static void main(String[] args) throws InterruptedException {
	FreeMindMainMock mock = new FreeMindMainMock();
	Resources.createInstance(mock);
	FreeMindProgressMonitor progress = new FreeMindProgressMonitor("title");
	progress.setVisible(true);
	for (int i = 0; i &lt; 10; i++) {
	    boolean canceled = progress.showProgress(i, 10, "inhalt {0}", new Object[] { Integer.valueOf(i) });
	    if (canceled) {
		progress.dismiss();
		System.exit(1);
	    }
	    Thread.sleep(1000l);
	}
	progress.dismiss();
	System.exit(0);
    }

    private JLabel mLabel;
    private JProgressBar mProgressBar;
    private JButton mCancelButton;
    protected boolean mCanceled = false;
    /**
     * 
     */
    private static final String PROGRESS_MONITOR_WINDOW_CONFIGURATION_STORAGE = "progress_monitor_window_configuration_storage";

    /**
     * 
     */
    public FreeMindProgressMonitor(String pTitle) {
	setTitle(getString(pTitle));
	mLabel = new JLabel("!");
	mProgressBar = new JProgressBar();
	mCancelButton = new JButton();
	Tools.setLabelAndMnemonic(mCancelButton, getString(("cancel")));
	mCancelButton.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent pE) {
		mCanceled = true;
	    }
	});
	setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0);
	add(mLabel, constraints);
	constraints.gridy = 1;
	add(mProgressBar, constraints);
	constraints.gridy = 2;
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.EAST;
	add(mCancelButton, constraints);
	// Tools.addEscapeActionToDialog(this);
	pack();
	setSize(new Dimension(600, 200));
	String marshaled = Resources.getInstance().getProperty(PROGRESS_MONITOR_WINDOW_CONFIGURATION_STORAGE);
	if (marshaled != null) {
	    XmlBindingTools.getInstance().decorateDialog(marshaled, this);
	}
    }

    /**
     * @param pCurrent
     * @param pMax
     * @param pName
     *            resource string to be displayed as progress string (maybe with
     *            parameters pParameters)
     * @param pParameters
     *            objects to be put in the resource string for pName
     * @return
     */
    public boolean showProgress(int pCurrent, final int pMax, String pName, Object[] pParameters) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		mProgressBar.setMaximum(pMax);
	    }
	});
	return showProgress(pCurrent, pName, pParameters);
    }

    public void dismiss() {
	WindowConfigurationStorage storage = new WindowConfigurationStorage();
	String marshalled = XmlBindingTools.getInstance().storeDialogPositions(storage, this);
	Resources.getInstance().getProperties().setProperty(PROGRESS_MONITOR_WINDOW_CONFIGURATION_STORAGE, marshalled);
	this.setVisible(false);
    }

    protected String getString(String resource) {
	return Resources.getInstance().getResourceString(resource);
    }

    public boolean showProgress(int pCurrent, String pName, Object[] pParameters) {
	final String format = Resources.getInstance().format(pName, pParameters);
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		mLabel.setText(format);
	    }
	});
	return setProgress(pCurrent);
    }

    public boolean setProgress(final int pCurrent) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		mProgressBar.setValue(pCurrent);
	    }
	});
	return mCanceled;
    }

}

