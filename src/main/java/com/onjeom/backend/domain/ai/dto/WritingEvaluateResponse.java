package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WritingEvaluateResponse(
        @JsonProperty("keyword_score")      Integer keywordScore,
        @JsonProperty("raw_score")          int rawScore,
        @JsonProperty("normalized_score")   int normalizedScore,
        @JsonProperty("final_score")        int finalScore,
        @JsonProperty("feedback")           String feedback,
        @JsonProperty("feedback_type")      String feedbackType,
        @JsonProperty("score_feedback")     String scoreFeedback,
        @JsonProperty("matched_keywords")   List<String> matchedKeywords,
        @JsonProperty("missing_keywords")   List<String> missingKeywords,
        @JsonProperty("deep_analysis")      DeepAnalysisDto deepAnalysis
) {}
