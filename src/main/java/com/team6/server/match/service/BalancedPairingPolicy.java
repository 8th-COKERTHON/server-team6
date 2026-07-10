package com.team6.server.match.service;

import com.team6.server.episode.Episode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BalancedPairingPolicy {
    public List<EpisodePair> create(List<Episode> episodes, int requestedRounds) {
        List<EpisodePair> candidates = new ArrayList<>();
        for (int i = 0; i < episodes.size(); i++) {
            for (int j = i + 1; j < episodes.size(); j++) {
                candidates.add(new EpisodePair(episodes.get(i), episodes.get(j)));
            }
        }
        Map<Long, Integer> appearances = new HashMap<>();
        List<EpisodePair> result = new ArrayList<>();
        while (!candidates.isEmpty() && result.size() < requestedRounds) {
            candidates.sort(Comparator
                    .comparingInt((EpisodePair pair) -> Math.max(count(appearances, pair.a()), count(appearances, pair.b())))
                    .thenComparingInt(pair -> count(appearances, pair.a()) + count(appearances, pair.b()))
                    .thenComparing(pair -> pair.a().getId())
                    .thenComparing(pair -> pair.b().getId()));
            EpisodePair selected = candidates.removeFirst();
            result.add(selected);
            appearances.merge(selected.a().getId(), 1, Integer::sum);
            appearances.merge(selected.b().getId(), 1, Integer::sum);
        }
        return result;
    }

    private int count(Map<Long, Integer> appearances, Episode episode) {
        return appearances.getOrDefault(episode.getId(), 0);
    }

    public record EpisodePair(Episode a, Episode b) {}
}
