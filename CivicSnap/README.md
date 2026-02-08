# CivicSnap - Civic Issue Reporting Backend

Spring Boot backend for a government-style civic issue reporting platform. Users report issues via camera uploads with geotag overlays, authorities manage assigned communities, and citizens can upvote and comment.

## Tech Stack

- Spring Boot 4.0
- Spring Security + JWT
- Spring Data JPA + PostgreSQL (Supabase)
- Supabase Auth, Storage, Database
- Image processing (BufferedImage, Graphics2D) for geotag overlay

## Setup

### 1. Supabase Configuration

1. Create a [Supabase](https://supabase.com) project
2. In Supabase Dashboard:
   - **Storage**: Create a bucket named `issues` and set it to **Public**
   - **Database**: Note your connection string (Project Settings → Database)
   - **API**: Copy your `anon` key and `service_role` key from Project Settings → API

### 2. Environment Variables

Create `application-local.properties` or set environment variables:

```properties
# Required - get from Supabase Dashboard
supabase.anon-key=your-anon-key
supabase.service-role-key=your-service-role-key

# Optional - for authority JWT signing
authority.jwt.secret=your-secure-random-secret
```

### 3. Database

Tables are auto-created via `spring.jpa.hibernate.ddl-auto=update`. Ensure your `application.properties` datasource points to your Supabase PostgreSQL instance.

### 4. Seed Authority User (optional)

Insert an authority user to test:

```sql
INSERT INTO communities (name, description) VALUES ('Downtown', 'Downtown area');
INSERT INTO authority_users (name, unique_code, assigned_community_id) 
VALUES ('John Doe', 'AUTH001', 1);
```

## API Endpoints

### User Authentication (Supabase)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Sign up `{email, password}` → JWT |
| POST | `/api/users/login` | Log in `{email, password}` → JWT |
| POST | `/api/users/forgot-password` | `{email}` → trigger password reset |

### Authority

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/authority/login` | `{name, unique_code}` → JWT |
| GET | `/api/authority/issues` | Issues in assigned community |
| PUT | `/api/authority/issues/{id}/status` | Update status `{status: pending\|in-progress\|resolved}` |

### Issues

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/issues` | Create issue (multipart: image, description, tags, lat, lon, road, city, country) |
| GET | `/api/issues` | Community feed, optional `?search=keyword` |
| POST | `/api/issues/{id}/upvote` | Upvote (JWT required) |
| POST | `/api/issues/{id}/comment` | Comment `{comment_text}` (JWT required) |
| GET | `/api/issues/{id}/comments` | Get comments |

### News

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/news` | Latest 3 news items |

## Issue Create (Multipart)

```
POST /api/issues
Authorization: Bearer <supabase-jwt>
Content-Type: multipart/form-data

- image: file (camera capture)
- description: text (optional)
- tags: "road,garbage,water" (comma-separated)
- latitude: number
- longitude: number
- road: text
- city: text
- country: text
- community_id: number (optional, auto-derived from city if omitted)
```

## Run

```bash
./mvnw spring-boot:run
```

Server runs on `http://localhost:8080`.
