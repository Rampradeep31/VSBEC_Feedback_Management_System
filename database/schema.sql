-- ============================================================
-- VSBEC Faculty Feedback Management System
-- MySQL 8 schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS vsbec_feedback
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vsbec_feedback;

-- ---------- Admin / staff login ----------
CREATE TABLE admins (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    role            ENUM('SUPER_ADMIN','ADMIN') NOT NULL DEFAULT 'ADMIN',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------- Academic structure ----------
CREATE TABLE academic_years (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    year_label      VARCHAR(20) NOT NULL UNIQUE,      -- e.g. "2026-2027"
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE departments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,            -- e.g. "ARTIFICIAL INTELLIGENCE AND DATA SCIENCE"
    short_code      VARCHAR(20) NOT NULL UNIQUE,       -- e.g. "AIDS"
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE classes (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    academic_year_id    BIGINT NOT NULL,
    department_id       BIGINT NOT NULL,
    year_of_study       ENUM('I','II','III','IV') NOT NULL,
    section             VARCHAR(5) NOT NULL,           -- e.g. "A","B","C"
    semester            ENUM('ODD','EVEN') NOT NULL,
    class_label         VARCHAR(50) NOT NULL,          -- e.g. "III AI&DS C" (denormalized, used verbatim in report)
    is_feedback_open     BOOLEAN NOT NULL DEFAULT FALSE,
    feedback_opened_at   DATETIME NULL,
    feedback_closed_at   DATETIME NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT fk_class_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    UNIQUE KEY uq_class (academic_year_id, department_id, year_of_study, section, semester)
) ENGINE=InnoDB;

-- ---------- Faculty ----------
CREATE TABLE faculty (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,             -- e.g. "Mr.R. Muthuchelvan" (stored exactly as printed)
    email           VARCHAR(150),
    department_id   BIGINT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_faculty_dept FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB;

-- ---------- Subjects ----------
-- Theory and Lab are modeled as separate subject rows (each gets its own question set + report row),
-- exactly like the source report treats "Cloud Service Management" and "Cloud Service Management Lab"
-- as two distinct entries.
CREATE TABLE subjects (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id        BIGINT NOT NULL,
    faculty_id      BIGINT NOT NULL,
    subject_name    VARCHAR(150) NOT NULL,             -- e.g. "Cloud Service Management" or "...Lab"
    subject_code    VARCHAR(30),
    subject_type    ENUM('THEORY','LAB') NOT NULL,
    display_order   INT NOT NULL DEFAULT 0,            -- controls row order in the printed report
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subject_class FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT fk_subject_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id)
) ENGINE=InnoDB;

-- ---------- Feedback question bank ----------
-- Two fixed sets of 10 questions (Theory / Lab), reusable across every subject of that type.
-- Seeded from the source report; editable by admin if wording ever needs to change.
CREATE TABLE feedback_questions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_type        ENUM('THEORY','LAB') NOT NULL,
    question_number     TINYINT NOT NULL,              -- 1..10
    question_text       TEXT NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uq_question (subject_type, question_number)
) ENGINE=InnoDB;

-- ---------- Students ----------
CREATE TABLE students (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    register_number     VARCHAR(30) NOT NULL UNIQUE,   -- login identifier
    name                VARCHAR(150) NOT NULL,         -- kept for admin roster view only; never joined into feedback
    class_id            BIGINT NOT NULL,
    email               VARCHAR(150),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES classes(id)
) ENGINE=InnoDB;

-- ---------- Submission ledger (enforces "once per subject", NOT linked to answers) ----------
-- This table is the ONLY place a student's identity is connected to a feedback act.
-- It records THAT student X submitted for subject Y, never WHAT they answered.
-- feedback_answers (below) has no student_id at all, which is what makes the responses anonymous.
CREATE TABLE feedback_submissions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    subject_id      BIGINT NOT NULL,
    submitted_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_sub_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    UNIQUE KEY uq_submission (student_id, subject_id)   -- DB-level guarantee of "once per subject"
) ENGINE=InnoDB;

-- ---------- Anonymous answers ----------
CREATE TABLE feedback_answers (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id          BIGINT NOT NULL,
    question_id         BIGINT NOT NULL,
    rating              TINYINT NOT NULL,              -- 1-5
    submission_batch_id BIGINT NOT NULL,                -- FK to feedback_submissions.id, used only to
                                                         -- group the 10 answers of one submission together;
                                                         -- never exposed or joined back to a student in the API.
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_answer_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES feedback_questions(id),
    CONSTRAINT fk_answer_batch FOREIGN KEY (submission_batch_id) REFERENCES feedback_submissions(id),
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE INDEX idx_answers_subject_question ON feedback_answers(subject_id, question_id);
CREATE INDEX idx_students_class ON students(class_id);
CREATE INDEX idx_subjects_class ON subjects(class_id);

-- ---------- Generated report log (for re-download without regenerating) ----------
CREATE TABLE generated_reports (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id        BIGINT NOT NULL,
    docx_path       VARCHAR(500) NOT NULL,
    pdf_path        VARCHAR(500) NOT NULL,
    generated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by    BIGINT,                             -- admins.id
    CONSTRAINT fk_report_class FOREIGN KEY (class_id) REFERENCES classes(id)
) ENGINE=InnoDB;

-- ============================================================
-- Seed data: fixed question banks (verbatim from the source report)
-- ============================================================
INSERT INTO feedback_questions (subject_type, question_number, question_text) VALUES
('THEORY', 1,  'How is the faculty’s approach towards teaching?'),
('THEORY', 2,  'How has the faculty prepared for the classes?'),
('THEORY', 3,  'Does the faculty inform you about your expected competencies, course outcomes?'),
('THEORY', 4,  'How often does the faculty illustrate the concepts through examples and Practical applications?'),
('THEORY', 5,  'Whether Faculty covers syllabus in time?'),
('THEORY', 6,  'Do you agree that the faculty teaches content beyond syllabus?'),
('THEORY', 7,  'How does the faculty communicate?'),
('THEORY', 8,  'Whether Faculty returns answer script in time and produce helpful comments?'),
('THEORY', 9,  'How does the faculty identify your strengths and encourage you with high level of challenges?'),
('THEORY', 10, 'How does the Faculty counsel & encourage the Students?'),

('LAB', 1,  'All equipment’s/computer units were in working condition.'),
('LAB', 2,  'The laboratory manuals are available.'),
('LAB', 3,  'The laboratories were neat and clean with proper labeling and marking of equipment’s.'),
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
