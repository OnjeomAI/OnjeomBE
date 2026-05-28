package com.onjeom.backend.domain.response.service;

import com.onjeom.backend.domain.ai.service.AiGradingResult;
import com.onjeom.backend.domain.ai.service.AiKeyword;
import com.onjeom.backend.domain.ai.service.AiScoringService;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.repository.ProblemKeywordRepository;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.response.dto.request.SubmitAnswerRequest;
import com.onjeom.backend.domain.response.dto.response.ResponseDetailResponse;
import com.onjeom.backend.domain.response.entity.Response;
import com.onjeom.backend.domain.response.repository.ResponseRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final ProblemRepository problemRepository;
    private final ProblemKeywordRepository problemKeywordRepository;
    private final UserRepository userRepository;
    private final AiScoringService aiScoringService;

    @Transactional
    public ResponseDetailResponse submit(Long userId, SubmitAnswerRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        List<AiKeyword> keywords = problemKeywordRepository.findByProblemId(problem.getId())
                .stream()
                .map(k -> new AiKeyword(k.getKeyword(), k.getWeight()))
                .toList();

        AiGradingResult result = aiScoringService.scoreAnswer(
                problem.getPassageText(),
                problem.getQuestionText(),
                problem.getModelAnswer(),
                keywords,
                request.answerText()
        );

        int attemptNumber = responseRepository.countByUserIdAndProblemId(userId, problem.getId()) + 1;

        Response response = Response.builder()
                .user(user)
                .problem(problem)
                .curriculumItemId(request.curriculumItemId())
                .answerText(request.answerText())
                .responseTimeSec(request.responseTimeSec())
                .rawScore(result.stage1Score())
                .finalScore(result.score())
                .feedbackText(result.feedback())
                .scoringBasis(result.gradeReason())
                .attemptNumber(attemptNumber)
                .build();

        responseRepository.save(response);

        return ResponseDetailResponse.from(response, result.foundKeywords());
    }

    @Transactional(readOnly = true)
    public ResponseDetailResponse getById(Long userId, Long responseId) {
        Response response = responseRepository.findById(responseId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESPONSE_NOT_FOUND));

        if (!responseRepository.existsByIdAndUserId(responseId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return ResponseDetailResponse.from(response, List.of());
    }

    @Transactional(readOnly = true)
    public List<ResponseDetailResponse> getByProblemId(Long userId, Long problemId) {
        return responseRepository.findByUserIdAndProblemIdOrderByCreatedAtAsc(userId, problemId)
                .stream()
                .map(r -> ResponseDetailResponse.from(r, List.of()))
                .toList();
    }
}
