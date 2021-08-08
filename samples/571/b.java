import java.util.concurrent.TimeUnit;
import org.graalvm.compiler.code.CompilationResult;
import org.graalvm.compiler.core.GraalCompilerOptions;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.debug.TTY;
import org.graalvm.compiler.serviceprovider.GraalServices;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.runtime.JVMCICompiler;

class AOTCompilationTask implements Runnable, Comparable&lt;Object&gt; {
    /**
     * Compile a method or a constructor.
     */
    @SuppressWarnings("try")
    public void run() {
	// Ensure a JVMCI runtime is initialized prior to Debug being initialized as the former
	// may include processing command line options used by the latter.
	HotSpotJVMCIRuntime.runtime();

	AOTCompiler.logCompilation(JavaMethodInfo.uniqueMethodName(method), "Compiling");

	final long threadId = Thread.currentThread().getId();

	final boolean printCompilation = GraalCompilerOptions.PrintCompilation.getValue(graalOptions)
		&& !TTY.isSuppressed() && GraalServices.isThreadAllocatedMemorySupported();
	if (printCompilation) {
	    TTY.println(getMethodDescription() + "...");
	}

	final long start;
	final long allocatedBytesBefore;
	if (printCompilation) {
	    start = System.currentTimeMillis();
	    allocatedBytesBefore = GraalServices.getThreadAllocatedBytes(threadId);
	} else {
	    start = 0L;
	    allocatedBytesBefore = 0L;
	}

	CompilationResult compResult = null;
	final long startTime = System.currentTimeMillis();
	SnippetReflectionProvider snippetReflection = aotBackend.getProviders().getSnippetReflection();
	try (DebugContext debug = DebugContext.create(graalOptions, new GraalDebugHandlersFactory(snippetReflection));
		Activation a = debug.activate()) {
	    compResult = aotBackend.compileMethod(method, debug);
	}
	final long endTime = System.currentTimeMillis();

	if (printCompilation) {
	    final long stop = System.currentTimeMillis();
	    final int targetCodeSize = compResult != null ? compResult.getTargetCodeSize() : -1;
	    final long allocatedBytesAfter = GraalServices.getThreadAllocatedBytes(threadId);
	    final long allocatedBytes = (allocatedBytesAfter - allocatedBytesBefore) / 1024;

	    TTY.println(getMethodDescription()
		    + String.format(" | %4dms %5dB %5dkB", stop - start, targetCodeSize, allocatedBytes));
	}

	if (compResult == null) {
	    result = null;
	    return;
	}

	// For now precision to the nearest second is sufficient.
	LogPrinter.writeLog("    Compile Time: " + TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) + "secs");
	if (main.options.debug) {
	    aotBackend.printCompiledMethod((HotSpotResolvedJavaMethod) method, compResult);
	}

	result = new CompiledMethodInfo(compResult,
		new AOTHotSpotResolvedJavaMethod((HotSpotResolvedJavaMethod) method, aotBackend.getBackend()));
    }

    /**
     * Method this task represents.
     */
    private final ResolvedJavaMethod method;
    private OptionValues graalOptions;
    private final AOTBackend aotBackend;
    /**
     * The result of this compilation task.
     */
    private CompiledMethodInfo result;
    private final Main main;
    /**
     * The compilation id of this task.
     */
    private final int id;

    private String getMethodDescription() {
	return String.format("%-6d aot %s %s", getId(), JavaMethodInfo.uniqueMethodName(method),
		getEntryBCI() == JVMCICompiler.INVOCATION_ENTRY_BCI ? "" : "(OSR@" + getEntryBCI() + ") ");
    }

    private int getId() {
	return id;
    }

    private static int getEntryBCI() {
	return JVMCICompiler.INVOCATION_ENTRY_BCI;
    }

}

