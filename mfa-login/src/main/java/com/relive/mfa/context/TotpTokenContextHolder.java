package com.relive.mfa.context;

/**
 * {@link TotpTokenContext} 的持有者使用 {@code ThreadLocal} 将其与当前线程相关联
 *
 * @author: ReLive
 * @date: 2023/1/7 23:20
 */
public class TotpTokenContextHolder {
    private static final ThreadLocal<TotpTokenContext> holder = new ThreadLocal<>();

    private TotpTokenContextHolder() {
    }

    /**
     * 返回当前线程绑定的 {@link TotpTokenContext}
     *
     * @return
     */
    public static TotpTokenContext getTotpTokenContext() {
        return holder.get();
    }

    /**
     * 将给定的 {@link TotpTokenContext} 绑定到当前线程
     *
     * @param tokenContext
     */
    public static void setTotpTokenContext(TotpTokenContext tokenContext) {
        if (tokenContext == null) {
            resetTotpTokenContext();
        } else {
            holder.set(tokenContext);
        }
    }

    /**
     * 重置绑定到当前线程的 {@link TotpTokenContext}
     */
    public static void resetTotpTokenContext() {
        holder.remove();
    }
}
