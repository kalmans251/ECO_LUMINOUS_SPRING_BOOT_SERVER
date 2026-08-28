package com.ecoluminous.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. 클라이언트 구독 채널 접두사 (/sub: 단일/장비별, /topic: 전체 브로드캐스트)
        config.enableSimpleBroker("/sub", "/topic");
        
        // 2. 클라이언트 발행(전송) 접두사
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 순수 웹소켓 엔드포인트 (라즈베리파이 / Python websocket-client 전용)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*");

        // 2. SockJS 엔드포인트 (웹 브라우저 프론트엔드 전용)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 실시간 음성 스트리밍 버퍼 지연 및 세션 끊김 방지 설정
        registration.setMessageSizeLimit(128 * 1024);      // 단일 메시지 최대 128KB
        registration.setSendBufferSizeLimit(512 * 1024);   // 전송 큐 버퍼 512KB
        registration.setSendTimeLimit(10 * 1000);          // 타임아웃 10초
    }
}