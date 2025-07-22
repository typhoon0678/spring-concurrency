package dev.typhoon.spring_concurrency.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.typhoon.spring_concurrency.model.dto.TicketReduceRequest;
import dev.typhoon.spring_concurrency.model.dto.TicketRequest;
import dev.typhoon.spring_concurrency.model.dto.TicketUpdateRequest;
import dev.typhoon.spring_concurrency.service.TicketService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    
    private final TicketService ticketService;
    
    // 기본 메서드
    @PatchMapping("/v1")
    public ResponseEntity<?> reduceTicket(@RequestBody TicketReduceRequest request) {
        ticketService.reduceTicket(request);

        return ResponseEntity.ok().build();
    }

    // 비관적 락
    @PatchMapping("/v2")
    public ResponseEntity<?> reduceTicketV2(@RequestBody TicketReduceRequest request) {
        ticketService.reduceTicketWithPessimisticLock(request);

        return ResponseEntity.ok().build();
    }

    // 낙관적 락
    @PatchMapping("/v3")
    public ResponseEntity<?> reduceTicketV3(@RequestBody TicketReduceRequest request) {
        ticketService.reduceTicketWithOptimisticLock(request);

        return ResponseEntity.ok().build();
    }

    // 분산 락 (redis) - 원자적 연산
    @PatchMapping("/v4")
    public ResponseEntity<?> reduceTicketV4(@RequestBody TicketReduceRequest request) {
        ticketService.reduceTicketWithDistributedLock(request);

        return ResponseEntity.ok().build();
    }
    
    // 분산 락 (redis) - Lua 스크립트 버전 (더 안전)
    @PatchMapping("/v5")
    public ResponseEntity<?> reduceTicketV5(@RequestBody TicketReduceRequest request) {
        ticketService.reduceTicketWithLuaScript(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TicketRequest request) {
        ticketService.createTicket(request);
        
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/quantity")
    public ResponseEntity<?> updateQuantity(@RequestBody TicketUpdateRequest request) {
        ticketService.updateTicketQuantity(request);

        return ResponseEntity.ok().build();
    }

    // Redis 티켓 카운터 초기화
    @PostMapping("/redis/{ticketName}/init")
    public ResponseEntity<?> initRedisTicket(@PathVariable String ticketName, @RequestParam int count) {
        ticketService.initializeRedisTicketCount(ticketName, count);
        return ResponseEntity.ok("Redis 티켓 초기화 완료: " + ticketName + " -> " + count + "개");
    }
    
    // Redis 티켓 수량 조회
    @GetMapping("/redis/{ticketName}/count")
    public ResponseEntity<Integer> getRedisTicketCount(@PathVariable String ticketName) {
        int count = ticketService.getRedisTicketCount(ticketName);
        return ResponseEntity.ok(count);
    }
}
