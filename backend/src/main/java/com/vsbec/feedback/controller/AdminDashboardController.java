package com.vsbec.feedback.controller;

import com.vsbec.feedback.dto.DashboardDtos.ClassDashboard;
import com.vsbec.feedback.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/classes/{classId}")
    public ClassDashboard getClassDashboard(@PathVariable Long classId) {
        return dashboardService.getClassDashboard(classId);
    }
}
