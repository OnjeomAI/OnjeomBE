package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompareAnswersAiResponse(
        @JsonProperty("score_diff")               int scoreDiff,
        @JsonProperty("is_improved")              boolean isImproved,
        @JsonProperty("growth_message")           String growthMessage,
        @JsonProperty("newly_included_keywords")  List<String> newlyIncludedKeywords,
        @JsonProperty("still_missing_keywords")   List<String> stillMissingKeywords,
        @JsonProperty("analysis")                 String analysis
) {}
