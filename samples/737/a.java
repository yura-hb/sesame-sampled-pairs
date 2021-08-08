import java.util.List;

class MultiLayerSpace extends BaseNetworkSpace&lt;DL4JConfiguration&gt; {
    class Builder extends Builder&lt;Builder&gt; {
	/**
	 * duplicateConfig not supported. Will always be true
	 * @param layerSpace
	 * @param numLayersDistribution
	 * @param duplicateConfig
	 * @return
	 */
	@Deprecated
	public Builder addLayer(LayerSpace&lt;? extends Layer&gt; layerSpace, ParameterSpace&lt;Integer&gt; numLayersDistribution,
		boolean duplicateConfig) {
	    if (!duplicateConfig)
		throw new IllegalArgumentException("Duplicate Config false not supported");
	    String layerName = "layer_" + layerSpaces.size();
	    duplicateConfig = true; //hard coded to always duplicate layers
	    layerSpaces.add(new LayerConf(layerSpace, layerName, null, numLayersDistribution, duplicateConfig, null));
	    return this;
	}

	protected List&lt;LayerConf&gt; layerSpaces = new ArrayList&lt;&gt;();

    }

}

