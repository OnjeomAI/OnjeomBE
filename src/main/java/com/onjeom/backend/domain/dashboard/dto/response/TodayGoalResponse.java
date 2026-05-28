package com.onjeom.backend.domain.dashboard.dto.response;

import com.onjeom.backend.domain.learning.dto.response.ReviewScheduleResponse;

import java.util.List;

public record TodayGoalResponse(
        int dailyGoal,
        int completedToday,
        boolean goalAchieved,
        List<ReviewScheduleResponse> dueReviews
) {}
