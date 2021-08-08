import java.io.File;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

class Main {
    /**
     * Entry point.
     * @param args the command line arguments.
     */
    public static void main(final String... args) {
	SwingUtilities.invokeLater(() -&gt; {
	    final MainFrame mainFrame = new MainFrame();

	    if (args.length &gt; 0) {
		final File sourceFile = new File(args[0]);
		mainFrame.openFile(sourceFile);
	    }
	    mainFrame.setTitle("Checkstyle GUI");
	    mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    mainFrame.setVisible(true);
	});
    }

}

