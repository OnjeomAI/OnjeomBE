package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompareAnswersAiRequest(
        @JsonProperty("question_text")    String questionText,
        @JsonProperty("model_answer")     String modelAnswer,
        @JsonProperty("previous_answer")  String previousAnswer,
        @JsonProperty("previous_score")   int previousScore,
        @JsonProperty("current_answer")   String currentAnswer,
        @JsonProperty("current_score")    int currentScore,
        @JsonProperty("keywords")         List<KeywordDto> keywords
) {}
