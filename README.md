# VSBEC Faculty Feedback Management System

A production-oriented Faculty Feedback Management System for V.S.B. Engineering College, Karur,
built to replicate the existing manual "Students' Feedback on Course Delivery" report exactly.

- **Frontend:** React + Tailwind CSS (Vite)
- **Backend:** Spring Boot 3 (Java 17)
- **Database:** MySQL 8
- **Auth:** JWT (Admin: username/password, Student: register number only)
- **Reports:** Apache POI (DOCX) + LibreOffice headless (PDF)

---

## 1. What's implemented

- Admin CRUD for Academic Year, Department, Class, Faculty, Subject (Theory/Lab as separate entries)
- Excel bulk student upload (`RegisterNumber`, `Name`, `Email` columns)
- Admin open/close feedback window per class
- Student login by register number, one submission per subject, enforced by a **database unique
  constraint** (`feedback_submissions(student_id, subject_id)`) — not just app-level checks
- **Anonymous answers by design**: `feedback_answers` has no student reference at all. The only
  table linking a student to a feedback act is `feedback_submissions`, and it stores no answer
  content. No query in the codebase joins the two back together.
- Auto-calculated per-question averages and totals, scaled to the same percentage format as your
  original Excel/Word report
- One-click DOCX + PDF report generation matching the original report's page layout, fonts, table
  structure, references, question legend, signatures, and footer
- Admin dashboard: total students, submitted, pending, completion %, faculty list, subject list

## 2. What you'll need to finish for a first deploy

This is a real, complete codebase, but it was written and reviewed without a live Maven/npm build
in this environment (no internet access to Maven Central here) — so budget an hour for a first
`mvn spring-boot:run` / `npm run dev` pass to shake out anything environment-specific.

- Add your actual VSBEC letterhead logo image to the report if you want it (current version uses
  styled text only, since the source .doc's logo slot turned out to just be a chart image, not a
  crest — easy to add via `XWPFParagraph.createRun().addPicture(...)` in `ReportGeneratorService`)
- Wire up a proper "manage admins" screen if you want more than the single seeded super admin
- The rating scale is 1–5 per your spec, scaled ×20 for the report's percentage columns
  (`app.scoring.percentage-multiplier` in `application.yml`) — change to 10/×10 if you'd rather
  match the raw 1–10 scale your existing Excel actually used

## 3. Prerequisites

- Java 17+, Maven 3.9+
- Node.js 18+
- A [Supabase](https://supabase.com) project (free tier is plenty for this)
- **LibreOffice** installed on the backend host (provides the `soffice` binary used for PDF export)
  - Ubuntu/Debian: `sudo apt install libreoffice`
  - macOS: `brew install --cask libreoffice`
  - Windows: install LibreOffice, then set `SOFFICE_PATH` to the full path of `soffice.exe`

## 4. Database setup (Supabase)

1. Create a project at [supabase.com](https://supabase.com) (or use one you already have).
2. Open **SQL Editor** in the Supabase dashboard, paste the contents of
   `database/schema.supabase.sql`, and run it. This creates every table, the fixed 10-question
   Theory/Lab question banks (seeded verbatim from your report), and a default super admin:
   - **Username:** `admin`
   - **Password:** `Admin@123`
   - ⚠️ Change this password immediately after your first login.
3. Get your connection string from **Project Settings → Database → Connection string**. Use the
   **Session pooler** (port `5432`) or the direct connection, not the Transaction pooler (port
   `6543`) — see the comment at the top of `application.yml`'s datasource block for why.

> Prefer plain MySQL instead? `database/schema.sql` (the original MySQL version) is still in the
> repo — swap the `postgresql` dependency in `pom.xml` back for `mysql-connector-j` and revert the
> datasource block in `application.yml` if you'd rather self-host.

## 5. Backend setup

```bash
cd backend
# Set these env vars (or edit application.yml directly):
#   DB_HOST=db.<your-project-ref>.supabase.co
#   DB_PORT=5432
#   DB_NAME=postgres
#   DB_USER=postgres
#   DB_PASSWORD=<your supabase db password>
#   JWT_SECRET=<something long and random>
#   CORS_ORIGINS=http://localhost:5173
#   SOFFICE_PATH=soffice
mvn spring-boot:run
```

Runs on `http://localhost:8080`. `ddl-auto=validate` is set by default since you already ran
`schema.supabase.sql` by hand — Hibernate will just check the entities match, not alter anything.

## 6. Frontend setup

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173` and proxies `/api` calls to the backend on port 8080 (see
`vite.config.js`). For production, `npm run build` and serve the `dist/` folder from any static
host or from Spring Boot itself.

## 7. Project layout

```
faculty-feedback-system/
├── database/
│   ├── schema.supabase.sql         # PostgreSQL/Supabase schema + seed data (primary)
│   └── schema.sql                  # Original MySQL schema, kept for self-hosted deployments
├── backend/
│   └── src/main/java/com/vsbec/feedback/
│       ├── entity/                 # JPA entities
│       ├── repository/             # Spring Data repositories
│       ├── service/                # Business logic (auth, academic CRUD, feedback, dashboard)
│       ├── report/                 # DOCX generation (Apache POI) + PDF conversion (LibreOffice)
│       ├── controller/             # REST endpoints
│       ├── security/               # JWT filter + util
│       ├── config/                 # Security/CORS config
│       └── dto/                    # Request/response records
└── frontend/
    └── src/
        ├── pages/student/          # Register-number login → subject list → rating form
        ├── pages/admin/            # Dashboard + all management screens
        ├── components/             # Shared layout/UI
        ├── context/                # Auth state
        └── api/                    # Axios client with JWT interceptor
```

## 8. How the report generation matches your original

| Original report element | How it's reproduced |
|---|---|
| US Letter, narrow margins | `CTPageSz`/`CTPageMar` set explicitly to match the source `.docx`'s section properties |
| Times New Roman throughout | Every run sets `setFontFamily("Times New Roman")` |
| College name / title, bold + underlined | Configurable via `application.yml` (`app.reports.college-name`, `report-title`) |
| Academic Year / Branch / Semester / Year line | Pulled live from the `classes` row you configured |
| Staff Name / Subject / Que1–10 / Total table | Built per subject type (Theory → page 1, Lab → page 2), columns sized to match the original proportions |
| Values in the table | Average rating per question × `percentageMultiplier`, rounded to 1 decimal — same math your Excel's "×10 row" used |
| References list | Numbered list of each subject's assigned faculty |
| Question 1–10 legend | Pulled from the `feedback_questions` table (seeded verbatim from your report) |
| Signature line | Class Advisor / HOD / Principal, tab-separated |
| Footer | Form No. 14b, Effective Date, copyright line — all configurable |

One deliberate deviation: the original document has the "Academic Year / Branch / Semester / Year"
line placed *above* the table on page 1 but *below* the table on page 2. This looked like an
inconsistency introduced during manual editing rather than an intentional design choice, so the
generated report places it above the table consistently on both pages. Flag it if you'd rather I
match the inconsistency exactly.

## 9. API summary

| Endpoint | Who | Purpose |
|---|---|---|
| `POST /api/auth/admin/login` | Public | Admin login (username + password) |
| `POST /api/auth/student/login` | Public | Student login (register number only) |
| `GET/POST/DELETE /api/admin/academic-years` | Admin | Academic year CRUD |
| `GET/POST/DELETE /api/admin/departments` | Admin | Department CRUD |
| `GET/POST/DELETE /api/admin/classes` | Admin | Class CRUD |
| `PATCH /api/admin/classes/{id}/feedback-window` | Admin | Open/close feedback |
| `GET/POST/DELETE /api/admin/faculty` | Admin | Faculty CRUD |
| `GET/POST/DELETE /api/admin/subjects` | Admin | Subject CRUD |
| `POST /api/admin/classes/{id}/students/upload` | Admin | Bulk student Excel upload |
| `GET /api/admin/dashboard/classes/{id}` | Admin | Dashboard stats |
| `GET /api/student/subjects` | Student | Subject list with submitted/pending flags |
| `GET /api/student/subjects/{id}/questions` | Student | 10 questions for that subject |
| `POST /api/student/feedback` | Student | Submit anonymous feedback (once per subject) |
| `POST /api/reports/classes/{id}/generate/docx/download` | Admin | Generate + download DOCX |
| `POST /api/reports/classes/{id}/generate/pdf/download` | Admin | Generate + download PDF |
