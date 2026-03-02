package com.raslan.taskmanager.controller;

import com.raslan.taskmanager.dto.File.FileDto;
import com.raslan.taskmanager.dto.File.UpdateFileDto;
import com.raslan.taskmanager.model.File;
import com.raslan.taskmanager.security.UserPrincipal;
import com.raslan.taskmanager.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService){
        this.fileService = fileService;
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<FileDto>> getTaskFiles(@PathVariable("taskId") Long taskId, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(fileService.getTaskFiles(taskId, user.getId()));
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<FileDto>> getWorkspaceFiles(@PathVariable("workspaceId") Long workspaceId, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(fileService.getWorkspaceFiles(workspaceId, user.getId()));
    }
    @PostMapping("/task/{taskId}")
    public ResponseEntity<FileDto> uploadToTask(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user
    ) throws IOException {
        return ResponseEntity.ok(fileService.uploadToTask(taskId, file, user.getId()));

    }

    @PostMapping("/workspace/{workspaceId}")
    public ResponseEntity<FileDto> uploadToWorkspace(
            @PathVariable Long workspaceId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user
    ) throws IOException {
        return ResponseEntity.ok(fileService.uploadToWorkspace(workspaceId, file, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileDto> updateFile(@PathVariable("id") Long fileId, @RequestBody UpdateFileDto dto, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(fileService.updateFile(fileId, dto, user.getId()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws MalformedURLException {
        File file = fileService.findById(id);

        Path filePath = Paths.get(file.getPath());
        Resource resource = new UrlResource(filePath.toUri());

        if(!resource.exists())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"").body(resource);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal user) {
        fileService.deleteFile(id, user.getId());
        return ResponseEntity.ok("File deleted");
    }
}
