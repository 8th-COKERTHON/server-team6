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

        // 대결 가능한 본인 에피소드 요약 리스트
        List<RingResponse.AvailableEpisodeDto> availableEpisodes = episodeRepository.findAllByMemberIdAndStatus(memberId, "AVAILABLE").stream()
                .map(episode -> new RingResponse.AvailableEpisodeDto(
                        episode.getId(),
                        episode.getTitle(),
                        episode.getEpisodeDate() != null ? episode.getEpisodeDate().toString() : "" // 💡 LocalDateTime을 String으로 변환하여 DTO 스펙 일치
                ))
                .collect(Collectors.toList());

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
        // 1. 요청한 두 ID가 같은 경우 검증
        if (request.getEpisodeAId().equals(request.getEpisodeBId())) {
            throw new IllegalArgumentException("동일한 에피소드는 대결할 수 없습니다.");
        }

        // 비관적 락 획득하며 조회
        List<Episode> episodes = episodeRepository.findAllByIdWithPessimisticLock(
                List.of(request.getEpisodeAId(), request.getEpisodeBId()));

        if (episodes.size() < 2) {
            throw new IllegalArgumentException("요청하신 에피소드 데이터를 찾을 수 없습니다.");
        }

        // ID 순서 보장하며 데이터 매핑
        Episode e1 = episodes.stream()
                .filter(e -> e.getId().equals(request.getEpisodeAId()))
                .findFirst().orElseThrow();
        Episode e2 = episodes.stream()
                .filter(e -> e.getId().equals(request.getEpisodeBId()))
                .findFirst().orElseThrow();

        // 예외 검증: 두 에피소드 모두 로그인한 회원의 소유여야 함
        if (!e1.getMemberId().equals(memberId) || !e2.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 에피소드만 대결을 진행할 수 있습니다.");
        }

        // 대결 가능한 상태(AVAILABLE)인지 검증
        if (!"AVAILABLE".equals(e1.getStatus()) || !"AVAILABLE".equals(e2.getStatus())) {
            throw new IllegalStateException("대결 가능한 상태가 아닙니다.");
        }

        Match match = Match.builder()
                .memberId(memberId)
                .episodeAId(e1.getId())
                .episodeBId(e2.getId())
                .status("IN_PROGRESS")
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

        // 이미 완료된 대결은 취소 불가 검증
        if (!"IN_PROGRESS".equals(match.getStatus())) {
            throw new IllegalStateException("진행 중인 대결만 취소할 수 있습니다.");
        }

        // 대결 데이터 삭제 (에피소드 상태는 변경하지 않았으므로 복구 로직 불필요)
        matchRepository.delete(match);
    }
}