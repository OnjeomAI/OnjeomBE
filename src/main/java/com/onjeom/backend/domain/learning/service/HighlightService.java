package com.onjeom.backend.domain.learning.service;

import com.onjeom.backend.domain.learning.dto.request.HighlightRequest;
import com.onjeom.backend.domain.learning.dto.response.HighlightResponse;
import com.onjeom.backend.domain.learning.entity.Highlight;
import com.onjeom.backend.domain.learning.repository.HighlightRepository;
import com.onjeom.backend.domain.problem.entity.Problem;
import com.onjeom.backend.domain.problem.repository.ProblemRepository;
import com.onjeom.backend.domain.user.entity.User;
import com.onjeom.backend.domain.user.repository.UserRepository;
import com.onjeom.backend.global.exception.CustomException;
import com.onjeom.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HighlightService {

    private final HighlightRepository highlightRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public HighlightResponse saveHighlight(Long userId, HighlightRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        if (request.startOffset() >= request.endOffset()) {
            throw new CustomException(ErrorCode.INVALID_HIGHLIGHT_RANGE);
        }

        Optional<Highlight> existing = highlightRepository
                .findByUserAndProblemAndStartOffsetAndEndOffset(
                        user, problem, request.startOffset(), request.endOffset());

        if (existing.isPresent()) {
            existing.get().updateColor(request.color());
            return toResponse(existing.get());
        }

        Highlight highlight = Highlight.builder()
                .user(user)
                .problem(problem)
                .startOffset(request.startOffset())
                .endOffset(request.endOffset())
                .color(request.color())
                .build();
        highlightRepository.save(highlight);
        return toResponse(highlight);
    }

    @Transactional(readOnly = true)
    public List<HighlightResponse> getHighlights(Long userId, Long problemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        return highlightRepository.findByUserAndProblem(user, problem).stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteHighlight(Long userId, Long problemId,
                                Integer startOffset, Integer endOffset) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

        highlightRepository.deleteByUserAndProblemAndStartOffsetAndEndOffset(
                user, problem, startOffset, endOffset);
    }

    private HighlightResponse toResponse(Highlight highlight) {
        return new HighlightResponse(
                highlight.getId(),
                highlight.getProblem().getId(),
                highlight.getStartOffset(),
                highlight.getEndOffset(),
                highlight.getColor(),
                highlight.getCreatedAt()
        );
    }
}
