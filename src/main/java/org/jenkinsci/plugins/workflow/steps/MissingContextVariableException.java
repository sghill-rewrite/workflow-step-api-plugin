package org.jenkinsci.plugins.workflow.steps;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Indicates that a required context was not available.
 *
 * @author Kohsuke Kawaguchi
 * @see StepContext#get(Class)
 * @see StepDescriptor#checkContextAvailability(StepContext)
 *
 * TODO: extend from AbortException if it's not final
 */
public class MissingContextVariableException extends Exception {
    private final @Nonnull Class<?> type;

    public MissingContextVariableException(@Nonnull Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    @Override public String getMessage() {
        StringBuilder b = new StringBuilder("Required context ").append(type).append(" is missing");
        boolean first = true;
        for (StepDescriptor p : getProviders()) {
            if (first) {
                b.append("\nPerhaps you forgot to surround the code with a step that provides this, such as: ");
                first = false;
            } else {
                b.append(", ");
            }
            b.append(p.getFunctionName());
        }
        return b.toString();
    }

    /**
     * Returns {@link StepDescriptor}s with body that can provide a context of this type,
     * so that the error message can diagnose what steps were likely missing.
     *
     * <p>
     * In such error diagnosing context, we don't care about {@link StepDescriptor}s that just
     * decorates/modifies the existing context, so we check required context as well to
     * exclude them
     */
    public @Nonnull List<StepDescriptor> getProviders() {
        List<StepDescriptor> r = new ArrayList<>();
        for (StepDescriptor sd : StepDescriptor.all()) {
            if (isIn(sd.getProvidedContext()) && !isIn(sd.getRequiredContext()))
                r.add(sd);
        }
        return r;
    }

    /**
     * Is type in the given set, in consideration of the subtyping relationship?
     */
    private boolean isIn(Set<? extends Class<?>> classes) {
        for (Class<?> t : classes)
            if (type.isAssignableFrom(t))
                return true;
        return false;
    }

    private static final long serialVersionUID = 1L;
}
