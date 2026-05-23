package com.onjeom.backend.domain.problem.service;

import com.onjeom.backend.domain.problem.dto.response.ProblemDetailResponse;
import com.onjeom.backend.domain.problem.dto.response.ProblemKeywordResponse;
import com.onjeom.backend.domain.problem.dto.response.ProblemResponse;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.repository.ProblemChoiceRepository;
import com.onjeom.backend.domain.problem.repository.ProblemKeywordRepository;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemKeywordRepository problemKeywordRepository;
    private final ProblemChoiceRepository problemChoiceRepository;

    public Page<ProblemResponse> getProblems(Pageable pageable) {
        return problemRepository.findAll(pageable).map(this::toResponse);
    }

    public ProblemDetailResponse getProblemDetail(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
        List<ProblemKeyword> keywords = problemKeywordRepository.findByProblemId(problemId);
        return toDetailResponse(problem, keywords);
    }

    public List<ProblemResponse> getProblemsByReadingType(ReadingType readingType) {
        return problemRepository.findByReadingType(readingType).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProblemResponse toResponse(Problem problem) {
        return new ProblemResponse(
                problem.getId(),
                problem.getQuestionText(),
                problem.getProblemType(),
                problem.getReadingType(),
                problem.getDifficulty(),
                problem.getVectorIndexStatus(),
                problem.getCreatedAt()
        );
    }

    private ProblemDetailResponse toDetailResponse(Problem problem, List<ProblemKeyword> keywords) {
        List<ProblemKeywordResponse> keywordResponses = keywords.stream()
                .map(k -> new ProblemKeywordResponse(k.getId(), k.getKeyword(), k.getWeight()))
                .toList();
        return new ProblemDetailResponse(
                problem.getId(),
                problem.getPassageText(),
                problem.getQuestionText(),
                problem.getProblemType(),
                problem.getReadingType(),
                problem.getDifficulty(),
                problem.getModelAnswer(),
                problem.getVectorIndexed(),
                problem.getVectorIndexStatus(),
                keywordResponses,
                problem.getCreatedAt()
        );
    }
}
