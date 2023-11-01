package com.instargram101.chat.service;

import com.instargram101.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final RedisTemplate redisTemplate;

    public void publishMessage(Long roomId, ChatMessage message) {
        log.info("publish try in redis");
        redisTemplate.convertAndSend("room/"+roomId, message);
    }
}
