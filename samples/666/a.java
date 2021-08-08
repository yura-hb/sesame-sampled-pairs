class PrecisionRecallCurve extends BaseCurve {
    /**
     * @return The area under the precision recall curve
     */
    public double calculateAUPRC() {
	if (area != null) {
	    return area;
	}

	area = calculateArea();
	return area;
    }

    private Double area;

}

