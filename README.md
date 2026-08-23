# NEXUSBANK

> A production-style enterprise digital banking platform built using Java, Spring Boot, Microservices, Angular, PostgreSQL, Docker, and modern DevOps practices.

## Project Vision

NEXUSBANK is a full-stack digital banking platform designed to simulate the architecture and workflows of a modern enterprise banking application.

The platform will provide secure customer authentication, customer profile management, bank account management, fund transfers, transaction processing, and other core digital banking capabilities.

## Core Features

### Authentication & Security

- Customer registration
- Secure login
- JWT-based authentication
- Role-based authorization
- Password encryption
- Refresh token support

### Customer Management

- Customer profile creation
- Customer profile management
- Customer status management
- KYC-ready architecture

### Account Management

- Savings accounts
- Current accounts
- Account creation
- Account status management
- Balance management

### Transactions

- Fund transfers
- Transaction history
- Transaction status tracking
- Transaction reference numbers
- Transaction reversal architecture
- Idempotency support

### Platform Features

- API Gateway
- Centralized configuration
- Health checks
- Correlation IDs
- Centralized exception handling
- API documentation
- Dockerized services

## Planned Architecture

```text
                         Angular Web Application
                                  │
                                  ▼
                           API Gateway
                                  │
                ┌─────────────────┼─────────────────┐
                ▼                 ▼                 ▼
          Auth Service     Customer Service    Account Service
                │                 │                 │
                └─────────────────┼─────────────────┘
                                  ▼
                         Transaction Service