package com.onjeom.backend.domain.writing.service;

import com.onjeom.backend.domain.ai.dto.CompareAnswersAiRequest;
import com.onjeom.backend.domain.ai.dto.CompetencyHistoryDto;
import com.onjeom.backend.domain.ai.dto.CompetencyScoreDto;
import com.onjeom.backend.domain.ai.dto.CurriculumAdjustAiRequest;
import com.onjeom.backend.domain.ai.dto.KeywordDto;
import com.onjeom.backend.domain.ai.dto.WeaknessReportAiRequest;
import com.onjeom.backend.domain.ai.service.AiWritingService;
import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import com.onjeom.backend.domain.problem.repository.ProblemKeywordRepository;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.writing.dto.request.CompareAnswersRequest;
import com.onjeom.backend.domain.writing.dto.request.CurriculumAdjustRequest;
import com.onjeom.backend.domain.writing.dto.request.WeaknessReportRequest;
import com.onjeom.backend.domain.writing.dto.response.CompareAnswersResponse;
import com.onjeom.backend.domain.writing.dto.response.CurriculumAdjustResponse;
import com.onjeom.backend.domain.writing.dto.response.WeakCompetencyDetail;
import com.onjeom.backend.domain.writing.dto.response.WeaknessReportResponse;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WritingAiService {

    private final AiWritingService aiWritingService;
    private final ProblemRepository problemRepository;
    private final ProblemKeywordRepository problemKeywordRepository;

    public CurriculumAdjustResponse adjustCurriculum(CurriculumAdjustRequest request) {
        List<CompetencyHistoryDto> historyDtos = request.competencyHistory().stream()
                .map(h -> new CompetencyHistoryDto(h.competency(), h.scores()))
                .toList();

        var result = aiWritingService.adjustCurriculum(new CurriculumAdjustAiRequest(historyDtos));

        return new CurriculumAdjustResponse(
                result.needsAdjustment(),
                result.weakCompetencies(),
                result.adjustmentMessage(),
                result.recommendedFocus()
        );
    }

    public CompareAnswersResponse compareAnswers(CompareAnswersRequest request) {
        var problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        List<KeywordDto> keywordDtos = problemKeywordRepository.findByProblemId(problem.getId())
                .stream()
                .map(k -> new KeywordDto(k.getKeyword(), k.getWeight()))
                .toList();

        var result = aiWritingService.compareAnswers(new CompareAnswersAiRequest(
                problem.getQuestionText(),
                problem.getModelAnswer(),
                request.previousAnswer(),
                request.previousScore(),
                request.currentAnswer(),
                request.currentScore(),
                keywordDtos
        ));

        return new CompareAnswersResponse(
                result.scoreDiff(),
                result.isImproved(),
                result.growthMessage(),
                result.newlyIncludedKeywords(),
                result.stillMissingKeywords(),
                result.analysis()
        );
    }

    public WeaknessReportResponse generateWeaknessReport(WeaknessReportRequest request) {
        List<CompetencyScoreDto> scoreDtos = request.competencyScores().stream()
                .map(c -> new CompetencyScoreDto(c.competency(), c.score()))
                .toList();

        var result = aiWritingService.generateWeaknessReport(new WeaknessReportAiRequest(scoreDtos));

        List<WeakCompetencyDetail> weakDetails = result.weakCompetencies().stream()
                .map(w -> new WeakCompetencyDetail(w.competency(), w.score(), w.level()))
                .toList();

        return new WeaknessReportResponse(
                weakDetails,
                result.report(),
                result.recommendations(),
                result.priorityCompetency()
        );
    }
}
