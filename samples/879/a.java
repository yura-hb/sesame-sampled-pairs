import org.deeplearning4j.util.ModelSerializer;

class ModelSavingCallback implements EvaluationCallback {
    /**
     * This method saves model
     *
     * @param model
     * @param filename
     */
    protected void save(Model model, String filename) {
	try {
	    ModelSerializer.writeModel(model, filename, true);
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

}

