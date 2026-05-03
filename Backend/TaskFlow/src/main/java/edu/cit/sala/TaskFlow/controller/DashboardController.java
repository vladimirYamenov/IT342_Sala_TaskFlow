package edu.cit.sala.TaskFlow.controller;

import edu.cit.sala.TaskFlow.dto.DashboardResponse;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.service.DashboardFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Facade Pattern - Exposes a single endpoint for dashboard data.
 * Delegates to DashboardFacade which coordinates multiple services internally.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardFacade dashboardFacade;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        DashboardResponse dashboard = dashboardFacade.getDashboardData(user.getId());
        return ResponseEntity.ok(dashboard);
    }
}
