# Lovable Core

AI-powered app builder — describe what you want, watch it get generated, previewed, and deployed live.

**Live app:** [http://34.14.138.43](http://34.14.138.43)
**Architecture:** [documentation/architecture.svg](documentation/architecture.svg)

## Stack

- **Frontend:** React, Vite, TypeScript, Tailwind CSS, shadcn/ui
- **Backend:** Spring Boot microservices (Java 21)
- **AI:** Spring AI + OpenRouter (Claude Sonnet 4.5)
- **Data:** PostgreSQL (pgvector), Redis, Kafka, MinIO
- **Infra:** Kubernetes (GKE), Docker, GitHub Actions CI/CD

## Services

| Service | Purpose |
|---|---|
| `lovable-ui` | React frontend |
| `api-gateway` | Routing, JWT auth |
| `account-service` | Auth, users, billing (Stripe) |
| `workspace-service` | Projects, files, live preview orchestration |
| `intelligence-service` | AI chat & code generation streaming |
| `config-service` | Centralized config |
| `discovery-service` | Service discovery |
| `reverse-proxy` | Wildcard subdomain routing for project previews |
