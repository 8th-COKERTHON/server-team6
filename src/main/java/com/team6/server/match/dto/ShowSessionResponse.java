package com.team6.server.match.dto;

public record ShowSessionResponse(Long sessionId, String type, String status, int totalMatches,
                                  int completedMatches) {}
