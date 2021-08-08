import java.util.List;

class MethodGen extends FieldGenOrMethodGen {
    /**
     * Give an instruction a line number corresponding to the source code line.
     *
     * @param ih instruction to tag
     * @return new line number object
     * @see LineNumber
     */
    public LineNumberGen addLineNumber(final InstructionHandle ih, final int src_line) {
	final LineNumberGen l = new LineNumberGen(ih, src_line);
	line_number_vec.add(l);
	return l;
    }

    private final List&lt;LineNumberGen&gt; line_number_vec = new ArrayList&lt;&gt;();

}

