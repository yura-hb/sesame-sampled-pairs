import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import java.io.*;

class ModelGuesser {
    /**
     * Load the model from the given file path
     * @param path the path of the file to "guess"
     *
     * @return the loaded model
     * @throws Exception
     */
    public static Object loadConfigGuess(String path) throws Exception {
	String input = FileUtils.readFileToString(new File(path));
	//note here that we load json BEFORE YAML. YAML
	//turns out to load just fine *accidentally*
	try {
	    return MultiLayerConfiguration.fromJson(input);
	} catch (Exception e) {
	    log.warn("Tried multi layer config from json", e);
	    try {
		return KerasModelImport.importKerasModelConfiguration(path);
	    } catch (Exception e1) {
		log.warn("Tried keras model config", e);
		try {
		    return KerasModelImport.importKerasSequentialConfiguration(path);
		} catch (Exception e2) {
		    log.warn("Tried keras sequence config", e);
		    try {
			return ComputationGraphConfiguration.fromJson(input);
		    } catch (Exception e3) {
			log.warn("Tried computation graph from json");
			try {
			    return MultiLayerConfiguration.fromYaml(input);
			} catch (Exception e4) {
			    log.warn("Tried multi layer configuration from yaml");
			    try {
				return ComputationGraphConfiguration.fromYaml(input);
			    } catch (Exception e5) {
				throw new ModelGuesserException("Unable to load configuration from path " + path
					+ " (invalid config file or not a known config type)");
			    }
			}
		    }
		}
	    }
	}
    }

}

