package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务，用于向管理端推送来单提醒
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSION_MAP.put(sid, session);
        log.info("WebSocket客户端建立连接：{}", sid);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到WebSocket客户端消息：sid={}, message={}", sid, message);
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("WebSocket客户端断开连接：{}", sid);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("WebSocket连接异常：{}", session == null ? null : session.getId(), error);
    }

    /**
     * 向所有管理端客户端推送消息
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception ex) {
                log.warn("WebSocket消息推送失败：{}", session.getId(), ex);
            }
        }
    }

}
