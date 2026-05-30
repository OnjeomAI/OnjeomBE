package com.onjeom.backend.domain.cms.controller;

import com.onjeom.backend.domain.cms.api.CmsApi;
import com.onjeom.backend.domain.cms.dto.request.GenerateProblemRequest;
import com.onjeom.backend.domain.cms.dto.request.UpdateCurriculumOrderRequest;
import com.onjeom.backend.domain.cms.dto.request.UpdateKeywordRequest;
import com.onjeom.backend.domain.cms.service.CmsService;
import com.onjeom.backend.domain.problem.dto.request.CreateProblemRequest;
import com.onjeom.backend.domain.problem.dto.request.UpdateProblemRequest;
import com.onjeom.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cms")
@RequiredArgsConstructor
public class CmsController implements CmsApi {

    private final CmsService cmsService;

    @Override
    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<?>> getAllProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(cmsService.getAllProblems(pageable)));
    }

    @Override
    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<?>> createProblem(@Valid @RequestBody CreateProblemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.createProblem(request)));
    }

    @Override
    @PostMapping("/problems/generate")
    public ResponseEntity<ApiResponse<?>> generateProblem(@Valid @RequestBody GenerateProblemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.generateProblem(request)));
    }

    @Override
    @PatchMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<?>> updateProblem(
            @PathVariable Long problemId,
            @RequestBody UpdateProblemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.updateProblem(problemId, request)));
    }

    @Override
    @DeleteMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<?>> deleteProblem(@PathVariable Long problemId) {
        cmsService.deleteProblem(problemId);
        return ResponseEntity.ok(ApiResponse.success("문제가 삭제되었습니다.", null));
    }

    @Override
    @PutMapping("/problems/{problemId}/keywords")
    public ResponseEntity<ApiResponse<?>> updateKeywords(
            @PathVariable Long problemId,
            @Valid @RequestBody UpdateKeywordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.updateKeywords(problemId, request)));
    }

    @Override
    @PostMapping("/problems/{problemId}/reindex")
    public ResponseEntity<ApiResponse<?>> reindexProblem(@PathVariable Long problemId) {
        cmsService.reindexProblem(problemId);
        return ResponseEntity.ok(ApiResponse.success("벡터 인덱싱이 완료되었습니다.", null));
    }

    @Override
    @PutMapping("/curriculum/{curriculumId}/order")
    public ResponseEntity<ApiResponse<?>> reorderCurriculum(
            @PathVariable Long curriculumId,
            @Valid @RequestBody UpdateCurriculumOrderRequest request) {
        cmsService.reorderCurriculum(curriculumId, request);
        return ResponseEntity.ok(ApiResponse.success("커리큘럼 순서가 업데이트되었습니다.", null));
    }
}
