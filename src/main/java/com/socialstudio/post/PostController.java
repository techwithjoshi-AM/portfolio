package com.socialstudio.post;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping
    public List<Post> list(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String platform) {
        return service.findAll(status, platform);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@Valid @RequestBody PostRequests.CreatePost request) {
        return service.create(request);
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public Post generate(@Valid @RequestBody PostRequests.GeneratePost request) {
        return service.generate(request);
    }

    @PatchMapping("/{id}/schedule")
    public Post schedule(@PathVariable UUID id, @Valid @RequestBody PostRequests.SchedulePost request) {
        return service.schedule(id, request.scheduledFor());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @ExceptionHandler({IllegalArgumentException.class, PostNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(RuntimeException exception) {
        return new ErrorResponse(exception.getMessage(), LocalDateTime.now());
    }

    public record ErrorResponse(String message, LocalDateTime timestamp) {
    }
}
