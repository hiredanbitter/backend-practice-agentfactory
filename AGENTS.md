# AGENTS.md

## Project purpose

This repository is a backend Java interview practice project.
The goal is to build a realistic Spring Boot + PostgreSQL backend over multiple tickets.
The resulting system should be functional but may intentionally contain design flaws, technical debt, or suboptimal patterns for interview review and refactoring practice.

## Tech stack

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- JUnit 5

## Build and test

- Run tests: `./mvnw test`
- Run app: `./mvnw spring-boot:run`
- Package app: `./mvnw clean package`

## Coding rules

- Follow TDD when the ticket says so: write a failing test first.
- Keep each ticket scoped narrowly.
- Prefer small commits and minimal file churn.
- Do not modify unrelated files.
- Do not perform git push, branch deletion, or PR merge automatically.
- Ask before any git command.

## Architectural rules

- Use layered architecture: controller -> service -> repository
- Use DTOs for API boundaries unless a ticket intentionally allows shortcuts
- Use Flyway for schema changes
- Keep SQL schema and Java entities aligned

## Intentional imperfection rules

This project is for interview practice.
Not every implementation should be ideal.
Some tickets may intentionally introduce:

- weak validation
- leaky entity exposure
- naive SQL queries
- incomplete error handling
- over-coupled service logic
  Only introduce flaws when explicitly requested by the ticket.

## Validation before completion

For each ticket:

1. Confirm acceptance criteria
2. Run `./mvnw test`
3. Summarize changed files
4. Explain any tradeoffs or intentional flaws
