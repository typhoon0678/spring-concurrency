package dev.typhoon.spring_concurrency.service;

import java.util.Collections;

import org.hibernate.StaleObjectStateException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import dev.typhoon.spring_concurrency.model.dto.TicketReduceRequest;
import dev.typhoon.spring_concurrency.model.dto.TicketRequest;
import dev.typhoon.spring_concurrency.model.dto.TicketUpdateRequest;
import dev.typhoon.spring_concurrency.model.entity.Ticket;
import dev.typhoon.spring_concurrency.model.entity.TicketVersion;
import dev.typhoon.spring_concurrency.repository.TicketRepository;
import dev.typhoon.spring_concurrency.repository.TicketVersionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketVersionRepository ticketVersionRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // 기본 메서드
    public void reduceTicket(TicketReduceRequest request) {
        Ticket ticket = ticketRepository.findByName(request.getTicketName())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.reduceQuantity();

        ticketRepository.save(ticket);
    }

    // 비관적 락
    @Transactional
    public void reduceTicketWithPessimisticLock(TicketReduceRequest request) {
        Ticket ticket = ticketRepository.findByNameWithPessimisticLock(request.getTicketName())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.reduceQuantity();

        ticketRepository.save(ticket);
    }

    // 낙관적 락
    @Retryable(value = { ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class }, // 낙관적 락 예외
            maxAttempts = 5, // 최대 5번 재시도
            backoff = @Backoff(delay = 1000) // 1초 간격으로 재시도
    )
    @Transactional
    public void reduceTicketWithOptimisticLock(TicketReduceRequest request) {
        TicketVersion ticket = ticketVersionRepository.findByName(request.getTicketName())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.reduceQuantity();

        ticketVersionRepository.save(ticket);
    }

    // 분산 락 (redis 원자적 연산 활용)
    public void reduceTicketWithDistributedLock(TicketReduceRequest request) {
        String countKey = "ticket:count:" + request.getTicketName();
        
        // Redis DECR 명령어 사용 (원자적 감소)
        Long newCount = redisTemplate.opsForValue().decrement(countKey);
        
        if (newCount == null) {
            throw new IllegalArgumentException("Ticket not found in Redis");
        }

        if (newCount < 0) {
            // 감소 후 수량이 0 미만이면 롤백
            // 해당 과정에서는 동시성 문제를 완전히 해결해주지 못할 수 있음
            redisTemplate.opsForValue().increment(countKey);
            throw new RuntimeException("티켓이 모두 소진되었습니다");
        }
    }
    
    // 분산 락 (redis lua 스크립트 활용)
    public void reduceTicketWithLuaScript(TicketReduceRequest request) {
        String countKey = "ticket:count:" + request.getTicketName();
        
        // Lua 스크립트: 0 이상일 때만 감소 (동시성 문제 완전히 해결)
        String luaScript = 
            "local current = redis.call('GET', KEYS[1]) " +
            "if current == false then " +
            "    return -1 " +
            "end " +
            "local count = tonumber(current) " +
            "if count > 0 then " +
            "    return redis.call('DECR', KEYS[1]) " +
            "else " +
            "    return -2 " +
            "end";
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, Collections.singletonList(countKey));
        
        if (result == null || result == -1) {
            throw new IllegalArgumentException("Ticket not found in Redis");
        }
        
        if (result == -2) {
            throw new RuntimeException("티켓이 모두 소진되었습니다");
        }
    }

    public void createTicket(TicketRequest request) {

        if (request.isVersion()) {
            TicketVersion savedTicket = TicketVersion.builder()
                    .name(request.getTicketName())
                    .quantity(request.getQuantity())
                    .build();

            ticketVersionRepository.save(savedTicket);
        } else {
            Ticket savedTicket = Ticket.builder()
                    .name(request.getTicketName())
                    .quantity(request.getQuantity())
                    .build();

            ticketRepository.save(savedTicket);
        }
    }

    public void updateTicketQuantity(TicketUpdateRequest request) {
        if (request.isVersion()) {
            TicketVersion savedTicket = ticketVersionRepository.findByName(request.getTicketName())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

            savedTicket.updateQuantity(request.getQuantity());

            ticketVersionRepository.save(savedTicket);
        } else {
            Ticket ticket = ticketRepository.findByName(request.getTicketName())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

            ticket.updateQuantity(request.getQuantity());

            ticketRepository.save(ticket);
        }
    }

    // Redis 티켓 카운터 초기화 메서드
    public void initializeRedisTicketCount(String ticketName, int initialCount) {
        String countKey = "ticket:count:" + ticketName;
        redisTemplate.opsForValue().set(countKey, String.valueOf(initialCount));
    }
    
    // Redis 티켓 수량 조회 메서드
    public int getRedisTicketCount(String ticketName) {
        String countKey = "ticket:count:" + ticketName;
        String countStr = redisTemplate.opsForValue().get(countKey);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }
}
