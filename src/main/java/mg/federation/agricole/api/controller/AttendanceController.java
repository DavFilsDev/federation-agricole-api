package mg.federation.agricole.api.controller;

import mg.federation.agricole.api.dto.ActivityMemberAttendance;
import mg.federation.agricole.api.dto.CreateActivityMemberAttendance;
import mg.federation.agricole.api.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/collectivities/{id}/activities/{activityId}/attendance")
    public ResponseEntity<List<ActivityMemberAttendance>> createAttendances(
            @PathVariable String id,
            @PathVariable String activityId,
            @RequestBody List<CreateActivityMemberAttendance> attendances) {
        List<ActivityMemberAttendance> created = attendanceService.createAttendances(id, activityId, attendances);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/collectivities/{id}/activities/{activityId}/attendance")
    public ResponseEntity<List<ActivityMemberAttendance>> getAttendances(
            @PathVariable String id,
            @PathVariable String activityId) {
        List<ActivityMemberAttendance> attendances = attendanceService.getAttendances(id, activityId);
        return ResponseEntity.ok(attendances);
    }
}