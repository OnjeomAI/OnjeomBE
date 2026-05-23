package com.onjeom.backend.domain.cms.service;

import com.onjeom.backend.domain.cms.dto.request.UpdateKeywordRequest;
import com.onjeom.backend.domain.cms.dto.response.CmsProblemsResponse;
import com.onjeom.backend.domain.problem.dto.request.CreateProblemRequest;
import com.onjeom.backend.domain.problem.dto.request.ProblemKeywordRequest;
import com.onjeom.backend.domain.problem.dto.request.UpdateProblemRequest;
import com.onjeom.backend.domain.problem.dto.response.ProblemDetailResponse;
import com.onjeom.backend.domain.problem.dto.response.ProblemKeywordResponse;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import com.onjeom.backend.domain.problem.enums.VectorIndexStatus;
import com.onjeom.backend.domain.problem.repository.ProblemChoiceRepository;
import com.onjeom.backend.domain.problem.repository.ProblemKeywordRepository;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CmsService {

    private final ProblemRepository problemRepository;
    private final ProblemKeywordRepository problemKeywordRepository;
    private final ProblemChoiceRepository problemChoiceRepository;

    @Transactional(readOnly = true)
    public Page<CmsProblemsResponse> getAllProblems(Pageable pageable) {
        return problemRepository.findAll(pageable)
                .map(p -> new CmsProblemsResponse(
                        p.getId(),
                        p.getQuestionText(),
                        p.getReadingType(),
                        p.getDifficulty(),
                        p.getVectorIndexStatus(),
                        problemKeywordRepository.countByProblemId(p.getId()),
                        p.getCreatedAt()
                ));
    }

    public ProblemDetailResponse createProblem(CreateProblemRequest request) {
        Problem problem = Problem.builder()
                .passageText(request.passageText())
                .questionText(request.questionText())
                .problemType(request.problemType())
                .readingType(request.readingType())
                .difficulty(request.difficulty())
                .modelAnswer(request.modelAnswer())
                .vectorIndexed(false)
                .vectorIndexStatus(VectorIndexStatus.PENDING)
                .build();
        problemRepository.save(problem);

        List<ProblemKeyword> keywords = new ArrayList<>();
        if (request.keywords() != null) {
            for (ProblemKeywordRequest kr : request.keywords()) {
                ProblemKeyword keyword = ProblemKeyword.builder()
                        .problem(problem)
                        .keyword(kr.keyword())
                        .weight(kr.weight())
                        .build();
                problemKeywordRepository.save(keyword);
                keywords.add(keyword);
            }
        }

        // TODO: STEP 6 — 벡터 인덱싱 비동기 이벤트 발행

        return toDetailResponse(problem, keywords);
    }

    public ProblemDetailResponse updateProblem(Long problemId, UpdateProblemRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
        problem.update(request);
        List<ProblemKeyword> keywords = problemKeywordRepository.findByProblemId(problemId);
        return toDetailResponse(problem, keywords);
    }

    public List<ProblemKeywordResponse> updateKeywords(Long problemId, UpdateKeywordRequest request) {
        problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        if (request.keywords().size() > 10) {
            throw new CustomException(ErrorCode.KEYWORD_LIMIT_EXCEEDED);
        }

        problemKeywordRepository.deleteAllByProblemId(problemId);

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        List<ProblemKeywordResponse> responses = new ArrayList<>();
        for (ProblemKeywordRequest kr : request.keywords()) {
            ProblemKeyword keyword = ProblemKeyword.builder()
                    .problem(problem)
                    .keyword(kr.keyword())
                    .weight(kr.weight())
                    .build();
            ProblemKeyword saved = problemKeywordRepository.save(keyword);
            responses.add(new ProblemKeywordResponse(saved.getId(), saved.getKeyword(), saved.getWeight()));
        }

        return responses;
    }

    public void deleteProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
        problemKeywordRepository.deleteAllByProblemId(problemId);
        problemChoiceRepository.deleteAllByProblemId(problemId);
        problemRepository.delete(problem);
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
