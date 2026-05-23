package com.onjeom.backend.domain.problem.controller;

import com.onjeom.backend.domain.problem.api.ProblemApi;
import com.onjeom.backend.domain.problem.enums.ReadingType;
import com.onjeom.backend.domain.problem.service.ProblemService;
import com.onjeom.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController implements ProblemApi {

    private final ProblemService problemService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(problemService.getProblems(pageable)));
    }

    @Override
    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<?>> getProblemDetail(@PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.success(problemService.getProblemDetail(problemId)));
    }

    @Override
    @GetMapping("/type/{readingType}")
    public ResponseEntity<ApiResponse<?>> getProblemsByReadingType(@PathVariable ReadingType readingType) {
        return ResponseEntity.ok(ApiResponse.success(problemService.getProblemsByReadingType(readingType)));
    }
}
