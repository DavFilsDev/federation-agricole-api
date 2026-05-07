package mg.federation.agricole.api.controller;

import mg.federation.agricole.api.dto.CollectivityLocalStatistics;
import mg.federation.agricole.api.dto.CollectivityOverallStatistics;
import mg.federation.agricole.api.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/collectivites/{id}/statistics")
    public ResponseEntity<List<CollectivityLocalStatistics>> getCollectivityLocalStatistics(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<CollectivityLocalStatistics> stats = statisticsService.getCollectivityLocalStatistics(id, from, to);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/collectivites/statistics")
    public ResponseEntity<List<CollectivityOverallStatistics>> getOverallStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<CollectivityOverallStatistics> stats = statisticsService.getOverallStatistics(from, to);
        return ResponseEntity.ok(stats);
    }
}