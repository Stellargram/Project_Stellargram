package com.instargram101.chat.repository;

import com.instargram101.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<ChatMessage, String> {

    Page<ChatMessage> findByRoomIdOrderByUnixTimestampDesc(Long roomId, Pageable pageable);

    Page<ChatMessage> findByRoomId(Long roomId, Pageable pageable);
}
