# Safe File Deleter

A full-stack demo application that safely deletes files within a restricted directory (`allowed_root`). The backend enforces strict path validation (no symlinks, no traversal outside the root, regular files only), and the React frontend provides a simple UI for submitting paths and viewing per-path results.

## Stack

Spring Boot 3.2.5 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/
│               ├── application.properties
│               └── static/            # populated by frontend build
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

**Backend**

```bash
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Files are deleted relative to `app.allowed-root` (default `./allowed_root`).

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

The dev server runs on `http://localhost:5173` and proxies `/api` requests to the backend.

**Production build**

```bash
cd frontend
npm run build
```

This outputs the compiled frontend into `backend/src/main/resources/static/`, so the Spring Boot app serves the UI from the same origin.

## API

### `POST /api/delete`

Deletes one or more files relative to `allowed_root`.

**Request body**

```json
{
  "paths": ["docs/old.txt", "tmp/cache.bin"]
}
```

**Response**

```json
{
  "allowedRoot": "/absolute/path/to/allowed_root",
  "results": [
    { "path": "docs/old.txt", "status": "deleted" },
    { "path": "tmp/cache.bin", "status": "rejected", "reason": "not found" }
  ]
}
```

Each result has a `status` of `deleted`, `rejected`, or `error`, and an optional `reason`.

## Notes

- `allowed_root` must exist and be accessible; otherwise the endpoint returns HTTP 500.
- Paths are resolved and normalized against `allowed_root`. Any path that escapes the root (via `..`, absolute paths, or symlink resolution) is rejected.
- Symbolic links are refused, as are non-regular files (directories, devices, etc.).
- Deletion is performed per-path; one failure does not abort the rest of the batch.
- The endpoint is unauthenticated — do not expose it publicly without adding authentication and authorization.
- There is no confirmation step in the UI; the delete action is immediate.
- The frontend build writes into `backend/src/main/resources/static/` and will overwrite existing contents (`emptyOutDir: true`).