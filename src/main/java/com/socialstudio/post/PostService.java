package com.socialstudio.post;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PostService {
    private final ConcurrentHashMap<UUID, Post> posts = new ConcurrentHashMap<>();

    public PostService() {
        create(new PostRequests.CreatePost(
                "The quiet power of consistency",
                "Small creative habits compound. The best content systems make it easier to show up, share what you know, and keep the conversation moving.",
                "LinkedIn",
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(30)
        ));
        create(new PostRequests.CreatePost(
                "Behind the launch",
                "A little peek behind the scenes: a focused idea, a sharp point of view, and a team willing to make the tenth version better than the first.",
                "Instagram",
                LocalDateTime.now().plusDays(2).withHour(18).withMinute(0)
        ));
        create(new PostRequests.CreatePost(
                "Three prompts for better ideas",
                "What changed? What surprised you? What would you explain to a friend? Three simple prompts for turning a busy week into useful content.",
                "X",
                null
        ));
    }

    public List<Post> findAll(String status, String platform) {
        return posts.values().stream()
                .filter(post -> status == null || status.equalsIgnoreCase(post.status()))
                .filter(post -> platform == null || platform.equalsIgnoreCase(post.platform()))
                .sorted(Comparator.comparing(Post::scheduledFor, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public Post create(PostRequests.CreatePost request) {
        String status = request.scheduledFor() == null ? "DRAFT" : "SCHEDULED";
        Post post = new Post(UUID.randomUUID(), request.title().trim(), request.content().trim(),
                normalizePlatform(request.platform()), status, request.scheduledFor(), Instant.now());
        posts.put(post.id(), post);
        return post;
    }

    public Post schedule(UUID id, LocalDateTime scheduledFor) {
        Post current = get(id);
        Post updated = new Post(current.id(), current.title(), current.content(), current.platform(),
                "SCHEDULED", scheduledFor, current.createdAt());
        posts.put(id, updated);
        return updated;
    }

    public void delete(UUID id) {
        if (posts.remove(id) == null) {
            throw new PostNotFoundException(id);
        }
    }

    public Post generate(PostRequests.GeneratePost request) {
        String tone = request.tone() == null || request.tone().isBlank() ? "confident" : request.tone().trim();
        String title = request.topic().trim() + " — a fresh perspective";
        String content = switch (normalizePlatform(request.platform())) {
            case "Instagram" -> "A fresh take on " + request.topic().trim().toLowerCase()
                    + ". Save this for later, share it with someone who needs it, and tell us what you think. #creativework #ideas";
            case "X" -> "A useful thought on " + request.topic().trim().toLowerCase()
                    + ": the best ideas get clearer when you make room for the unexpected.";
            default -> "Here is a " + tone.toLowerCase() + " perspective on " + request.topic().trim().toLowerCase()
                    + ": start with the problem, stay close to the people you are helping, and let the work speak.";
        };
        return create(new PostRequests.CreatePost(title, content, request.platform(), null));
    }

    private Post get(UUID id) {
        Post post = posts.get(id);
        if (post == null) {
            throw new PostNotFoundException(id);
        }
        return post;
    }

    private String normalizePlatform(String platform) {
        return switch (platform.trim().toLowerCase()) {
            case "instagram" -> "Instagram";
            case "x", "twitter" -> "X";
            case "linkedin" -> "LinkedIn";
            default -> throw new IllegalArgumentException("Platform must be Instagram, LinkedIn, or X");
        };
    }
}
