package com.socialstudio.post;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record Post(
        UUID id,
        String title,
        String content,
        String platform,
        String status,
        LocalDateTime scheduledFor,
        Instant createdAt
) {
}
