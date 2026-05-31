package com.onjeom.backend.domain.cms.service;

import com.onjeom.backend.domain.cms.dto.request.GenerateProblemRequest;
import com.onjeom.backend.domain.cms.dto.request.UpdateCurriculumOrderRequest;
import com.onjeom.backend.domain.cms.dto.request.UpdateKeywordRequest;
import com.onjeom.backend.domain.cms.dto.response.CmsProblemsResponse;
import com.onjeom.backend.domain.curriculum.entity.Curriculum;
import com.onjeom.backend.domain.curriculum.entity.CurriculumItem;
import com.onjeom.backend.domain.curriculum.repository.CurriculumItemRepository;
import com.onjeom.backend.domain.curriculum.repository.CurriculumRepository;
import com.onjeom.backend.domain.problem.dto.request.CreateProblemRequest;
import com.onjeom.backend.domain.problem.dto.request.ProblemKeywordRequest;
import com.onjeom.backend.domain.problem.dto.request.UpdateProblemRequest;
import com.onjeom.backend.domain.problem.dto.response.ProblemDetailResponse;
import com.onjeom.backend.domain.problem.dto.response.ProblemKeywordResponse;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.entity.ProblemKeyword;
import com.onjeom.backend.domain.problem.enums.ProblemType;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.enums.VectorIndexStatus;
import com.onjeom.backend.domain.problem.repository.ProblemChoiceRepository;
import com.onjeom.backend.domain.problem.repository.ProblemKeywordRepository;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CmsService {

    private final ProblemRepository problemRepository;
    private final ProblemKeywordRepository problemKeywordRepository;
    private final ProblemChoiceRepository problemChoiceRepository;
    private final CurriculumRepository curriculumRepository;
    private final CurriculumItemRepository curriculumItemRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private RestClient aiRestClient;

    @PostConstruct
    public void init() {
        this.aiRestClient = RestClient.builder().baseUrl(aiServerUrl).build();
    }

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

        triggerIndexing(problem);

        return toDetailResponse(problem, keywords);
    }

    public ProblemDetailResponse generateProblem(GenerateProblemRequest request) {
        record AiGenerateRequest(int difficulty, String reading_type, String topic) {}
        record AiGenerateResponse(String passage_text, String question_text, String model_answer,
                                  String reading_type, int difficulty) {}

        AiGenerateResponse aiResponse;
        try {
            aiResponse = aiRestClient.post()
                    .uri("/api/problems/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AiGenerateRequest(
                            request.difficulty(),
                            request.readingType().name(),
                            request.topic()
                    ))
                    .retrieve()
                    .body(AiGenerateResponse.class);
        } catch (Exception e) {
            log.error("AI 문제 생성 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.AI_SERVER_ERROR);
        }

        if (aiResponse == null) throw new CustomException(ErrorCode.AI_SERVER_ERROR);

        ReadingType readingType;
        try {
            readingType = ReadingType.valueOf(aiResponse.reading_type());
        } catch (IllegalArgumentException e) {
            readingType = request.readingType();
        }

        Problem problem = Problem.builder()
                .passageText(aiResponse.passage_text())
                .questionText(aiResponse.question_text())
                .problemType(ProblemType.SHORT_ANSWER)
                .readingType(readingType)
                .difficulty(aiResponse.difficulty())
                .modelAnswer(aiResponse.model_answer())
                .vectorIndexed(false)
                .vectorIndexStatus(VectorIndexStatus.PENDING)
                .build();
        problemRepository.save(problem);
        triggerIndexing(problem);

        return toDetailResponse(problem, List.of());
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

    public void reindexProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));
        triggerIndexing(problem);
    }

    private void triggerIndexing(Problem problem) {
        record IndexRequest(String content_id, String passage, String question, String model_answer, java.util.List<String> keywords) {}
        try {
            problem.updateVectorIndexStatus(VectorIndexStatus.INDEXING);
            java.util.List<String> keywords = problemKeywordRepository.findByProblemId(problem.getId())
                    .stream().map(ProblemKeyword::getKeyword).toList();
            aiRestClient.post()
                    .uri("/api/indexing/index")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new IndexRequest(
                            problem.getId().toString(),
                            problem.getPassageText(),
                            problem.getQuestionText(),
                            problem.getModelAnswer(),
                            keywords
                    ))
                    .retrieve()
                    .toBodilessEntity();
            problem.updateVectorIndexStatus(VectorIndexStatus.DONE);
        } catch (Exception e) {
            log.error("벡터 인덱싱 요청 실패 (problemId={}): {}", problem.getId(), e.getMessage());
            problem.updateVectorIndexStatus(VectorIndexStatus.PENDING);
        }
    }

    public void reorderCurriculum(Long curriculumId, UpdateCurriculumOrderRequest request) {
        Curriculum curriculum = curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new CustomException(ErrorCode.CURRICULUM_NOT_FOUND));

        List<CurriculumItem> items = curriculumItemRepository
                .findByCurriculumOrderByStageAscOrderIndexAsc(curriculum);

        Map<Long, CurriculumItem> byProblemId = new java.util.HashMap<>();
        for (CurriculumItem item : items) {
            byProblemId.put(item.getProblem().getId(), item);
        }

        List<Long> problemIds = request.problemIds();
        for (int i = 0; i < problemIds.size(); i++) {
            CurriculumItem item = byProblemId.get(problemIds.get(i));
            if (item != null) {
                item.updateOrderIndex(i + 1);
            }
        }
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
