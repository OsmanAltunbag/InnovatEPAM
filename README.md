# InnovatEPAM Portal 🚀

An enterprise-grade employee innovation management platform built during the EPAM AI Tech Bootcamp. This project demonstrates a strict **Spec-Driven Development (SDD)** approach, utilizing AI engineering tools to produce highly reliable, tested, and maintainable code.

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot 3.2.2, Spring Security (JWT)
* **Database:** PostgreSQL, Flyway Migrations
* **Frontend:** React 18, Vite, Tailwind CSS
* **Testing:** JUnit 5, Mockito, Testcontainers (Backend) | Vitest, MSW, React Testing Library (Frontend)
* **AI Engineering:** GitHub Copilot, SpecKit

## ✨ Key Features (MVP)

1.  **Role-Based Access Control:** Secure JWT authentication distinguishing between `Submitters` and `Evaluators/Admins`.
2.  **Idea Management:** Employees can submit detailed innovation proposals, including category tagging and secure file attachments.
3.  **Jury Evaluation Workflow:** A strict state-machine workflow (Submitted → Under Review → Accepted/Rejected) featuring mandatory feedback validation and an immutable chronological audit trail.
4.  **Modern UI/UX:** A fully responsive, dashboard-style interface built with Tailwind CSS.

## 🧠 Methodology: Beyond "Vibe Coding"

This project was built without ad-hoc AI prompting. Instead, it utilizes the **SpecKit** workflow:
1.  **Specify:** Clear user stories and constraints (`specs/*/spec.md`).
2.  **Plan:** Database schemas, API contracts, and architecture definitions (`API.md`, `data-model.md`).
3.  **Task:** Step-by-step execution roadmaps generated before any implementation (`tasks.md`).
4.  **Implement:** Strict RED-GREEN-REFACTOR cycles ensuring 80%+ test coverage.

## 🚀 Quick Start

### Prerequisites
* Java 21
* Node.js 18+
* Docker (for PostgreSQL Testcontainers/Local DB)

### Running the Backend
```bash
cd backend
mvn spring-boot:run
