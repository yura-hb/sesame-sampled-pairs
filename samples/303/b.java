class AnnotationTypeRequiredMemberWriterImpl extends AbstractMemberWriter
	implements AnnotationTypeRequiredMemberWriter, MemberSummaryWriter {
    /**
     * Get the summary for the member summary table.
     *
     * @return a string for the table summary
     */
    // Overridden by AnnotationTypeOptionalMemberWriterImpl
    protected String getTableSummary() {
	return resources.getText("doclet.Member_Table_Summary",
		resources.getText("doclet.Annotation_Type_Required_Member_Summary"),
		resources.getText("doclet.annotation_type_required_members"));
    }

}

