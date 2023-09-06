package com.relive27.mfa.context;

/**
 * A holder of the {@link MfaTokenContext} that associates it with the current thread using a {@code ThreadLocal}.
 *
 * @author: ReLive27
 * @date: 2023/1/7 23:20
 */
public class MfaAuthenticationTokenContextHolder {
    private static final ThreadLocal<MfaTokenContext> holder = new ThreadLocal<>();

    private MfaAuthenticationTokenContextHolder() {
    }

    /**
     * Returns the {@link MfaTokenContext} bound to the current thread.
     *
     * @return
     */
    public static MfaTokenContext getMfaTokenContext() {
        return holder.get();
    }

    /**
     * Bind the given {@link MfaTokenContext} to the current thread.
     *
     * @param tokenContext
     */
    public static void setMfaTokenContext(MfaTokenContext tokenContext) {
        if (tokenContext == null) {
            resetMfaTokenContext();
        } else {
            holder.set(tokenContext);
        }
    }

    /**
     * Reset the {@link MfaTokenContext} bound to the current thread.
     */
    public static void resetMfaTokenContext() {
        holder.remove();
    }
}
