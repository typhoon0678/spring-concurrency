#!/bin/bash

# Redis 서버가 시작될 때까지 대기
echo "Redis 서버 시작 대기중..."
until redis-cli -h redis ping > /dev/null 2>&1; do
    echo "Redis 연결 대기중..."
    sleep 1
done

echo "Redis 초기 데이터 설정 시작..."

# test4, test5 티켓을 각각 300개로 초기화
redis-cli -h redis SET "ticket:count:test4" 300
redis-cli -h redis SET "ticket:count:test5" 300

echo "Redis 초기 데이터 설정 완료!"
echo "test4 티켓: $(redis-cli -h redis GET ticket:count:test4)개"
echo "test5 티켓: $(redis-cli -h redis GET ticket:count:test5)개"