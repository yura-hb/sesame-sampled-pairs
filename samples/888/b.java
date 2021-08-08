import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.List;

class InferenceContext {
    /**
     * add a new inference var to this inference context
     */
    void addVar(TypeVar t) {
	this.undetvars = this.undetvars.prepend(infer.fromTypeVarFun.apply(t));
	this.inferencevars = this.inferencevars.prepend(t);
    }

    /** list of inference vars as undet vars */
    List&lt;Type&gt; undetvars;
    Infer infer;
    /** list of inference vars in this context */
    List&lt;Type&gt; inferencevars;

}

