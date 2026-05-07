package mg.federation.agricole.api.controller;

import mg.federation.agricole.api.dto.CollectivityActivity;
import mg.federation.agricole.api.dto.CreateCollectivityActivity;
import mg.federation.agricole.api.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/collectivities/{id}/activities")
    public ResponseEntity<List<CollectivityActivity>> getActivities(@PathVariable String id) {
        List<CollectivityActivity> activities = activityService.getActivities(id);
        return ResponseEntity.ok(activities);
    }

    @PostMapping("/collectivities/{id}/activities")
    public ResponseEntity<List<CollectivityActivity>> createActivities(
            @PathVariable String id,
            @RequestBody List<CreateCollectivityActivity> createActivities) {
        List<CollectivityActivity> created = activityService.createActivities(id, createActivities);
        return ResponseEntity.ok(created);
    }
}