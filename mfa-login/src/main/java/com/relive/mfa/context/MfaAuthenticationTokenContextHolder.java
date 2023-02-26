package com.relive.mfa.context;

/**
 * {@link MfaTokenContext} 的持有者使用 {@code ThreadLocal} 将其与当前线程相关联
 *
 * @author: ReLive
 * @date: 2023/1/7 23:20
 */
public class MfaAuthenticationTokenContextHolder {
    private static final ThreadLocal<MfaTokenContext> holder = new ThreadLocal<>();

    private MfaAuthenticationTokenContextHolder() {
    }

    /**
     * 返回当前线程绑定的 {@link MfaTokenContext}
     *
     * @return
     */
    public static MfaTokenContext getMfaTokenContext() {
        return holder.get();
    }

    /**
     * 将给定的 {@link MfaTokenContext} 绑定到当前线程
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
     * 重置绑定到当前线程的 {@link MfaTokenContext}
     */
    public static void resetMfaTokenContext() {
        holder.remove();
    }
}
