# Social Studio

Social Studio is a Spring Boot MVP for generating, organizing, and scheduling social media content from one focused workspace.

## Run locally

Requirements: Java 17+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). The app seeds three example posts when it starts. Content is stored in memory, so restarting the application resets the workspace.

## Included workflow

- Create a post for Instagram, LinkedIn, or X.
- Generate a platform-aware starting point from a topic.
- Save posts as drafts or schedule them for a date and time.
- Filter the queue by all posts, scheduled posts, or drafts.
- Remove posts from the queue.

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/posts` | List posts, optionally filtered by `status` and `platform` |
| `POST` | `/api/posts` | Create a draft or scheduled post |
| `POST` | `/api/posts/generate` | Generate and save a post from a topic |
| `PATCH` | `/api/posts/{id}/schedule` | Schedule an existing post |
| `DELETE` | `/api/posts/{id}` | Remove a post |

The API is intentionally backed by an in-memory store for this demo and can be moved to a database without changing the frontend contract.
