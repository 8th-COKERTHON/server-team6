package com.team6.server.match.service;

import com.team6.server.match.dto.MatchResponse;
import com.team6.server.match.repository.MatchingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchingEventRepository matchingEventRepository;

    /* 진행할 수 있는 매칭 리스트 조회 */
    public List<MatchResponse> getActiveLeagues() {
        return matchingEventRepository.findByStatus("OPEN").stream()
                .map(MatchResponse::from)
                .collect(Collectors.toList());
    }

    /* 앞으로 진행 가능한 매칭 리스트 조회 */
    public List<MatchResponse> getUpcomingLeagues() {
        return matchingEventRepository.findByStatus("DRAFT").stream()
                .map(MatchResponse::from)
                .collect(Collectors.toList());
    }
}
