package com.team6.server.match.service;

import com.team6.server.match.dto.RingResponse;
import com.team6.server.match.repository.MatchingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchingEventRepository matchingEventRepository;

    /* 링 화면 조회 */
    public RingResponse getRingScreen(Long memberId) {

        // 현재 OPEN 상태인 활성화된 리그/이벤트 리스트 전체 조회
        List<RingResponse.ActiveEventDto> activeEvents = matchingEventRepository.findByStatus("OPEN").stream()
                .map(RingResponse.ActiveEventDto::from)
                .collect(Collectors.toList());

        // 대결 가능한 본인 기억 요약 리스트
        List<RingResponse.AvailableMemoryDto> availableMemories = new ArrayList<>();

        // 현재 진행 중인 대결 조회
        RingResponse.ActiveMatchDto activeMatch = null;

        return new RingResponse(
                null,
                availableMemories,
                activeMatch,
                activeEvents
        );
    }
}
