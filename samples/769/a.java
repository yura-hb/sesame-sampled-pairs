import org.nd4j.linalg.io.ClassPathResource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

class InstrumentationApplication extends Application&lt;Nd4jInstrumentationConfiguration&gt; {
    /**
     * Start the server
     */
    public void start() {
	try {
	    InputStream is = new ClassPathResource(resourcePath, InstrumentationApplication.class.getClassLoader())
		    .getInputStream();
	    File tmpConfig = new File(resourcePath);
	    if (!tmpConfig.getParentFile().exists())
		tmpConfig.getParentFile().mkdirs();
	    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpConfig));
	    IOUtils.copy(is, bos);
	    bos.flush();
	    run(new String[] { "server", tmpConfig.getAbsolutePath() });
	    tmpConfig.deleteOnExit();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private String resourcePath = "org/nd4j/instrumentation/dropwizard.yml";
    private Environment env;

    @Override
    public void run(Nd4jInstrumentationConfiguration nd4jInstrumentationConfiguration, Environment environment)
	    throws Exception {
	environment.jersey().register(new InstrumentationResource());
	this.env = environment;
    }

}

