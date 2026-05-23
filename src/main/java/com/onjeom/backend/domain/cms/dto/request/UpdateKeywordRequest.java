package com.onjeom.backend.domain.cms.dto.request;

import com.onjeom.backend.domain.problem.dto.request.ProblemKeywordRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateKeywordRequest(
        @NotNull @Size(min = 1, max = 10) List<ProblemKeywordRequest> keywords
) {}
