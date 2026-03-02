package com.raslan.taskmanager.service;

import com.raslan.taskmanager.dto.File.FileDto;
import com.raslan.taskmanager.dto.File.UpdateFileDto;
import com.raslan.taskmanager.enums.MembershipStatus;
import com.raslan.taskmanager.enums.WorkspaceUserRole;
import com.raslan.taskmanager.exception.ResourceNotFoundException;
import com.raslan.taskmanager.model.*;
import com.raslan.taskmanager.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    private final FileRepository fileRepo;
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceUserRepository workspaceUserRepo;
     public FileService(FileRepository fileRepository,  TaskRepository taskRepository, UserRepository userRepository,  WorkspaceUserRepository workspaceUserRepository,  WorkspaceRepository workspaceRepository) {
        this.fileRepo = fileRepository;
        this.taskRepo = taskRepository;
        this.userRepo = userRepository;
        this.workspaceUserRepo = workspaceUserRepository;
        this.workspaceRepo = workspaceRepository;
    }

    public List<FileDto> getWorkspaceFiles(Long workspaceId, Long userId) {
        boolean isMembership = workspaceUserRepo.existsByUserIdAndWorkspaceIdAndStatus(userId, workspaceId, MembershipStatus.ACCEPTED);
        if(!isMembership)
            throw new AccessDeniedException("you not a member of this workspace");

        Workspace workspace = workspaceRepo.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<File> files = fileRepo.findByWorkspaceId(workspaceId);

        return files.stream().map(this::toFileDto).toList();
    }

    public FileDto uploadToWorkspace(Long workspaceId, MultipartFile file, Long userId) throws IOException {
         boolean isMembership = workspaceUserRepo.existsByUserIdAndWorkspaceIdAndStatus(userId, workspaceId, MembershipStatus.ACCEPTED);
         if(!isMembership)
             throw new AccessDeniedException("you not a member of this workspace");


        User user = userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException("user not found"));
        Workspace workspace = workspaceRepo.findById(workspaceId).orElseThrow(()->new ResourceNotFoundException("workspace not found"));

        String fileName = file.getOriginalFilename();
        String path = saveFile(file, "uploads/workspaces/");
        File fileToSave = buildWorkspaceFile(fileName, path, workspace, user);
        fileRepo.save(fileToSave);

        return toFileDto(fileToSave);
    }


    @Transactional
    public FileDto updateFile(Long fileId, UpdateFileDto dto, Long userId) {
             File file = fileRepo.findById(fileId).orElseThrow(() -> new ResourceNotFoundException("File not found"));

             file.setName(dto.getName());

             return toFileDto(file);
    }

    public File findById(Long id){
         return fileRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("File not found!"));
    }

    public void deleteFile(Long fileId, long userId){
         File file =  fileRepo.findById(fileId).orElseThrow(()->new ResourceNotFoundException("File not found!"));
         User user = getUser(userId);

        validateFilePermissions(file, user);
        fileRepo.deleteById(fileId);
    }

    public List<FileDto> getTaskFiles(Long taskId, Long userId) {
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validatePermissions(task, user);

        List<File> files = fileRepo.findByTaskId(taskId);

        return files.stream().map(this::toFileDto).toList();
    }

    public FileDto uploadToTask(Long taskId, MultipartFile file, Long userId) throws IOException {
         Task task = getTask(taskId);
         User user = getUser(userId);

         validatePermissions(task,user);

         String fileName = file.getOriginalFilename();
         String path = saveFile(file, "uploads/tasks/");
         File fileToSave = buildFile(fileName, path, task, user);
         fileRepo.save(fileToSave);

         return toFileDto(fileToSave);
    }

    private Task getTask(Long id){
        return taskRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Task not found"));
    }

    private User getUser(Long id){
        return userRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Task not found"));
    }
    private void validatePermissions(Task task, User user) {
        boolean allowed = task.getAssignedTo().getId().equals(user.getId())
                         ||  task.getCreatedBy().getId().equals(user.getId());

        if (!allowed)
            throw new AccessDeniedException("Not authorized");
     }


    private void validateFilePermissions(File file, User user) {
        boolean allowed = file.getUploadedBy().getId().equals(user.getId());

        if (!allowed)
            throw new AccessDeniedException("Can not delete file");
    }

     private String generateFileName(String originalFileName) {
         String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
         return UUID.randomUUID().toString() + extension;
     }

     private Path generateFilePath(String uploadDir, String fileName) throws IOException {
         Path uploadPath = Paths.get(uploadDir);
         if(!Files.exists(uploadPath))
             Files.createDirectories(uploadPath);

         Path filePath = uploadPath.resolve(fileName);
         return filePath;
     }

     // save physical file
    private String saveFile(MultipartFile file, String uploadDir) throws IOException {
        String storedName = generateFileName(file.getOriginalFilename());

        Path filePath = generateFilePath(uploadDir, storedName);

        // save file
        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }

     private File buildFile(String name, String path, Task task, User user) {
        return File.builder()
                .name(name).path(path)
                .task(task).uploadedBy(user)
                .build();
     }

    private File buildWorkspaceFile(String name, String path, Workspace workspace, User user){
        return File.builder()
                .name(name).path(path)
                .workspace(workspace).uploadedBy(user)
                .build();
    }
     private FileDto toFileDto(File file) {
         return FileDto.builder()
                 .id(file.getId())
                 .name(file.getName())
                 .build();
     }

}
