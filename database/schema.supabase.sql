-- ============================================================
-- VSBEC Faculty Feedback Management System
-- PostgreSQL / Supabase schema
-- ============================================================
-- Run this in the Supabase SQL Editor, or via `psql` / any Postgres client
-- connected to your Supabase project's connection string.

-- ---------- Admin / staff login ----------
CREATE TABLE admins (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'ADMIN' CHECK (role IN ('SUPER_ADMIN','ADMIN')),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- Academic structure ----------
CREATE TABLE academic_years (
    id              BIGSERIAL PRIMARY KEY,
    year_label      VARCHAR(20) NOT NULL UNIQUE,      -- e.g. "2026-2027"
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE departments (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,            -- e.g. "ARTIFICIAL INTELLIGENCE AND DATA SCIENCE"
    short_code      VARCHAR(20) NOT NULL UNIQUE,       -- e.g. "AIDS"
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE classes (
    id                  BIGSERIAL PRIMARY KEY,
    academic_year_id    BIGINT NOT NULL REFERENCES academic_years(id),
    department_id       BIGINT NOT NULL REFERENCES departments(id),
    year_of_study       VARCHAR(5) NOT NULL CHECK (year_of_study IN ('I','II','III','IV')),
    section             VARCHAR(5) NOT NULL,           -- e.g. "A","B","C"
    semester            VARCHAR(10) NOT NULL CHECK (semester IN ('ODD','EVEN')),
    class_label         VARCHAR(50) NOT NULL,          -- e.g. "III AI&DS C" (used verbatim in report)
    is_feedback_open     BOOLEAN NOT NULL DEFAULT FALSE,
    feedback_opened_at   TIMESTAMP NULL,
    feedback_closed_at   TIMESTAMP NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (academic_year_id, department_id, year_of_study, section, semester)
);

-- ---------- Faculty ----------
CREATE TABLE faculty (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,             -- e.g. "Mr.R. Muthuchelvan" (stored exactly as printed)
    email           VARCHAR(150),
    department_id   BIGINT NOT NULL REFERENCES departments(id),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- Subjects ----------
-- Theory and Lab are modeled as separate subject rows (each gets its own question set + report row),
-- exactly like the source report treats "Cloud Service Management" and "Cloud Service Management Lab"
-- as two distinct entries.
CREATE TABLE subjects (
    id              BIGSERIAL PRIMARY KEY,
    class_id        BIGINT NOT NULL REFERENCES classes(id),
    faculty_id      BIGINT NOT NULL REFERENCES faculty(id),
    subject_name    VARCHAR(150) NOT NULL,             -- e.g. "Cloud Service Management" or "...Lab"
    subject_code    VARCHAR(30),
    subject_type    VARCHAR(10) NOT NULL CHECK (subject_type IN ('THEORY','LAB')),
    display_order   INT NOT NULL DEFAULT 0,            -- controls row order in the printed report
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- Feedback question bank ----------
-- Two fixed sets of 10 questions (Theory / Lab), reusable across every subject of that type.
-- Seeded from the source report; editable by admin if wording ever needs to change.
CREATE TABLE feedback_questions (
    id                  BIGSERIAL PRIMARY KEY,
    subject_type        VARCHAR(10) NOT NULL CHECK (subject_type IN ('THEORY','LAB')),
    question_number     SMALLINT NOT NULL,              -- 1..10
    question_text       TEXT NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (subject_type, question_number)
);

-- ---------- Students ----------
CREATE TABLE students (
    id                  BIGSERIAL PRIMARY KEY,
    register_number     VARCHAR(30) NOT NULL UNIQUE,   -- login identifier
    name                VARCHAR(150) NOT NULL,         -- kept for admin roster view only; never joined into feedback
    class_id            BIGINT NOT NULL REFERENCES classes(id),
    email               VARCHAR(150),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- Submission ledger (enforces "once per subject", NOT linked to answers) ----------
-- This table is the ONLY place a student's identity is connected to a feedback act.
-- It records THAT student X submitted for subject Y, never WHAT they answered.
-- feedback_answers (below) has no student_id at all, which is what makes the responses anonymous.
CREATE TABLE feedback_submissions (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT NOT NULL REFERENCES students(id),
    subject_id      BIGINT NOT NULL REFERENCES subjects(id),
    submitted_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, subject_id)   -- DB-level guarantee of "once per subject"
);

-- ---------- Anonymous answers ----------
CREATE TABLE feedback_answers (
    id                  BIGSERIAL PRIMARY KEY,
    subject_id          BIGINT NOT NULL REFERENCES subjects(id),
    question_id         BIGINT NOT NULL REFERENCES feedback_questions(id),
    rating              SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    submission_batch_id BIGINT NOT NULL REFERENCES feedback_submissions(id),
                                                         -- used only to group the 10 answers of one
                                                         -- submission together; never exposed or
                                                         -- joined back to a student in the API.
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_answers_subject_question ON feedback_answers(subject_id, question_id);
CREATE INDEX idx_students_class ON students(class_id);
CREATE INDEX idx_subjects_class ON subjects(class_id);

-- ---------- Generated report log (for re-download without regenerating) ----------
CREATE TABLE generated_reports (
    id              BIGSERIAL PRIMARY KEY,
    class_id        BIGINT NOT NULL REFERENCES classes(id),
    docx_path       VARCHAR(500) NOT NULL,
    pdf_path        VARCHAR(500) NOT NULL,
    generated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    generated_by    BIGINT                              -- admins.id
);

-- ============================================================
-- Seed data: fixed question banks (verbatim from the source report)
-- ============================================================
INSERT INTO feedback_questions (subject_type, question_number, question_text) VALUES
('THEORY', 1,  'How is the faculty''s approach towards teaching?'),
('THEORY', 2,  'How has the faculty prepared for the classes?'),
('THEORY', 3,  'Does the faculty inform you about your expected competencies, course outcomes?'),
('THEORY', 4,  'How often does the faculty illustrate the concepts through examples and Practical applications?'),
('THEORY', 5,  'Whether Faculty covers syllabus in time?'),
('THEORY', 6,  'Do you agree that the faculty teaches content beyond syllabus?'),
('THEORY', 7,  'How does the faculty communicate?'),
('THEORY', 8,  'Whether Faculty returns answer script in time and produce helpful comments?'),
('THEORY', 9,  'How does the faculty identify your strengths and encourage you with high level of challenges?'),
('THEORY', 10, 'How does the Faculty counsel & encourage the Students?'),

('LAB', 1,  'All equipment''s/computer units were in working condition.'),
('LAB', 2,  'The laboratory manuals are available.'),
('LAB', 3,  'The laboratories were neat and clean with proper labeling and marking of equipment''s.'),
('LAB', 4,  'Each student gets a chance to perform the experiment/laboratory activities by his/her own hands.'),
('LAB', 5,  'The laboratory hours were mostly spent on performing experiments.'),
('LAB', 6,  'The laboratory was cooperative.'),
('LAB', 7,  'The laboratory teacher ensured the smooth conduct of the experiments.'),
('LAB', 8,  'The laboratory teacher had a full knowledge of the experiments.'),
('LAB', 9,  'The laboratory reports were checked timely with proper marking or grading.'),
('LAB', 10, 'Practical sessional exams were held in time.');

-- Default super admin (username: admin / password: Admin@123 — CHANGE IMMEDIATELY AFTER FIRST LOGIN)
-- Password hash below is a BCrypt hash of "Admin@123" — regenerate in production.
INSERT INTO admins (username, password_hash, full_name, role) VALUES
('admin', '$2a$10$m8gV.f4tncAv3CF5lR2.9ulCMDH0McFYo.j9vy3eKmNxqcPWFYgKC', 'System Administrator', 'SUPER_ADMIN');

-- ============================================================
-- Note on Row Level Security (RLS)
-- ============================================================
-- Supabase enables the Postgres REST/Realtime layer (PostgREST) by default,
-- which is bypassed entirely here since the Spring Boot backend talks to
-- Postgres directly over JDBC using a privileged connection string — not
-- through Supabase's public API. RLS policies are therefore NOT required
-- for this app to function correctly, since PostgREST is never in the
-- request path. If you later expose these tables via Supabase's
-- auto-generated REST/GraphQL API directly to any client, you MUST add RLS
-- policies first (in particular, feedback_answers and feedback_submissions
-- should never be readable by an anonymous/student role).
