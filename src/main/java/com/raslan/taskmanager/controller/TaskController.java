package com.raslan.taskmanager.controller;

import com.raslan.taskmanager.dto.Task.*;
import com.raslan.taskmanager.security.UserPrincipal;
import com.raslan.taskmanager.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @GetMapping
    public ResponseEntity<Page<ReturnTaskDto>> getAllTasks(@RequestParam TaskFilter filter, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(taskService.getTasks(user.getId(), filter));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReturnTaskDto> getTaskById(@PathVariable("id") Long id, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(taskService.getById(id, user.getId()));
    }




    @PostMapping
    public ResponseEntity<ReturnTaskDto> createTask(@RequestBody CreateTaskDto task, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(taskService.createTask(user.getId(), task));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ReturnTaskDto> updateTask(@PathVariable("id") Long taskId, @RequestBody UpdateTaskDto task, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(taskService.updateTask(taskId, user.getId(), task));
    }


    @PatchMapping("/done/{id}")
    public ResponseEntity<ReturnTaskDto> updateTaskStatus(@PathVariable("id") Long taskId, @RequestBody UpdateTaskStatusDto dto, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, user.getId(), dto.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable("id") Long id, @AuthenticationPrincipal UserPrincipal user){
         taskService.deleteTask(id, user.getId());
         return ResponseEntity.ok("Task deleted");
    }
}
