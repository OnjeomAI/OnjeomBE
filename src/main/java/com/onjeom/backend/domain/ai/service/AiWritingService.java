package com.onjeom.backend.domain.ai.service;

import com.onjeom.backend.domain.ai.dto.CompareAnswersAiRequest;
import com.onjeom.backend.domain.ai.dto.CompareAnswersAiResponse;
import com.onjeom.backend.domain.ai.dto.CurriculumAdjustAiRequest;
import com.onjeom.backend.domain.ai.dto.CurriculumAdjustAiResponse;
import com.onjeom.backend.domain.ai.dto.WeaknessReportAiRequest;
import com.onjeom.backend.domain.ai.dto.WeaknessReportAiResponse;

public interface AiWritingService {

    CurriculumAdjustAiResponse adjustCurriculum(CurriculumAdjustAiRequest request);

    CompareAnswersAiResponse compareAnswers(CompareAnswersAiRequest request);

    WeaknessReportAiResponse generateWeaknessReport(WeaknessReportAiRequest request);
}
