package com.socialstudio.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class PostRequests {
    private PostRequests() {
    }

    public record CreatePost(
            @NotBlank @Size(max = 90) String title,
            @NotBlank @Size(max = 5000) String content,
            @NotBlank String platform,
            LocalDateTime scheduledFor
    ) {
    }

    public record GeneratePost(
            @NotBlank @Size(max = 120) String topic,
            @NotNull String platform,
            String tone
    ) {
    }

    public record SchedulePost(@NotNull LocalDateTime scheduledFor) {
    }
}
