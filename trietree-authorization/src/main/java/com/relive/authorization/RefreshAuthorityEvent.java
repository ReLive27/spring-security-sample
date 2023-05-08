package com.relive.authorization;

import org.springframework.context.ApplicationEvent;

/**
 * @author: ReLive
 * @date: 2023/5/5 21:18
 */
public class RefreshAuthorityEvent extends ApplicationEvent {

    public RefreshAuthorityEvent(Object source) {
        super(source);
    }
}
