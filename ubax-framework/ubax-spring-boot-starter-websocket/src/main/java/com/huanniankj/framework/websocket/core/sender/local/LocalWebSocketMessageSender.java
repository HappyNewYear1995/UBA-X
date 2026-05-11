package com.huanniankj.framework.websocket.core.sender.local;

import com.huanniankj.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import com.huanniankj.framework.websocket.core.sender.WebSocketMessageSender;
import com.huanniankj.framework.websocket.core.session.WebSocketSessionManager;

/**
 * 本地的 {@link WebSocketMessageSender} 实现类
 * <p>
 * 仅适合单机场景
 *
 * @author zhaoff
 */
public class LocalWebSocketMessageSender extends AbstractWebSocketMessageSender {

    public LocalWebSocketMessageSender(WebSocketSessionManager sessionManager) {
        super(sessionManager);
    }

}
