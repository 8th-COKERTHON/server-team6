package com.team6.server.match.service;

import com.team6.server.episode.entity.Episode;
import com.team6.server.episode.repository.EpisodeRepository;
import com.team6.server.match.dto.MatchRequestDto;
import com.team6.server.match.dto.RingResponse;
import com.team6.server.match.entity.Match;
import com.team6.server.match.repository.MatchRepository;
import com.team6.server.match.repository.MatchingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchingEventRepository matchingEventRepository;
    private final EpisodeRepository episodeRepository;
    private final MatchRepository matchRepository;

    /* 링 화면 조회 로직 */
    public RingResponse getRingScreen(Long memberId) {

        // 현재 OPEN 상태인 활성화된 리그/이벤트 리스트 전체 조회
        List<RingResponse.ActiveEventDto> activeEvents = matchingEventRepository.findByStatus("OPEN").stream()
                .map(RingResponse.ActiveEventDto::from)
                .collect(Collectors.toList());

        // 대결 가능한 본인 기억 요약 리스트
        List<RingResponse.AvailableEpisodeDto> availableEpisodes = new ArrayList<>();

        // 현재 진행 중인 대결 조회
        RingResponse.ActiveMatchDto activeMatch = null;

        return new RingResponse(
                null,
                availableEpisodes,
                activeMatch,
                activeEvents
        );
    }

    /* 대결 시작 로직 */
    @Transactional
    public Long startMatch(Long memberId, MatchRequestDto request) {
        // 비관적 락 획득
        List<Episode> episodes = episodeRepository.findAllByIdWithPessimisticLock(
                List.of(request.getEpisodeAId(), request.getEpisodeBId()));

        Episode e1 = episodes.get(0);
        Episode e2 = episodes.get(1);

        // 예외 검증
        if (e1.getMemberId().equals(e2.getMemberId())) {
            throw new IllegalArgumentException("본인의 에피소드 간 대결은 불가능합니다.");
        }
        if (!"AVAILABLE".equals(e1.getStatus()) || !"AVAILABLE".equals(e2.getStatus())) {
            throw new IllegalStateException("대결 가능한 상태가 아닙니다.");
        }

        // 상태 변경 및 매칭 생성
        e1.updateStatus("MATCHING");
        e2.updateStatus("MATCHING");

        Match match = Match.builder()
                .memberId(memberId)
                .episodeAId(e1.getId())
                .episodeBId(e2.getId())
                .status("PROGRESS")
                .startedAt(LocalDateTime.now())
                .build();

        return matchRepository.save(match).getId();
    }

    /* 대결 취소 로직 */
    @Transactional
    public void cancelMatch(Long memberId, Long matchId) {
        // 대결 엔티티 조회
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("해당 대결을 찾을 수 없습니다."));

        // 본인의 대결인지 확인
        if (!match.getMemberId().equals(memberId)) {
            throw new IllegalStateException("본인의 대결만 취소할 수 있습니다.");
        }

        // 참여한 두 에피소드 조회 및 상태 복구
        List<Episode> episodes = episodeRepository.findAllByIdWithPessimisticLock(
                List.of(match.getEpisodeAId(), match.getEpisodeBId()));

        for (Episode episode : episodes) {
            episode.updateStatus("AVAILABLE");
        }

        // 대결 데이터 삭제
        matchRepository.delete(match);
    }
}
