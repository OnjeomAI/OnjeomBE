package com.onjeom.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WritingEvaluateRequest(
        @JsonProperty("passage_text")  String passageText,
        @JsonProperty("question_text") String questionText,
        @JsonProperty("model_answer")  String modelAnswer,
        @JsonProperty("user_answer")   String userAnswer,
        @JsonProperty("keywords")      List<KeywordDto> keywords
) {}
