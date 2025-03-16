package com.bigprime.dk8.common.config.websocket;

import com.bigprime.dk8.common.config.kubernetes.K8sClientProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final K8sClientProvider clientProvider;

    public WebSocketConfig(K8sClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SSHWebSocketHandler(clientProvider), "/ssh").setAllowedOrigins("*");
    }
}