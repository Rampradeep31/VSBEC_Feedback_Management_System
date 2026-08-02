-- Seed data for local H2 dev profile
-- Runs AFTER Hibernate creates the schema (ddl-auto: create).
-- Mirrors the seed rows from database/schema.supabase.sql.

INSERT INTO feedback_questions (subject_type, question_number, question_text, is_active) VALUES
('THEORY', 1,  'How is the faculty''s approach towards teaching?', TRUE),
('THEORY', 2,  'How has the faculty prepared for the classes?', TRUE),
('THEORY', 3,  'Does the faculty inform you about your expected competencies, course outcomes?', TRUE),
('THEORY', 4,  'How often does the faculty illustrate the concepts through examples and Practical applications?', TRUE),
('THEORY', 5,  'Whether Faculty covers syllabus in time?', TRUE),
('THEORY', 6,  'Do you agree that the faculty teaches content beyond syllabus?', TRUE),
('THEORY', 7,  'How does the faculty communicate?', TRUE),
('THEORY', 8,  'Whether Faculty returns answer script in time and produce helpful comments?', TRUE),
('THEORY', 9,  'How does the faculty identify your strengths and encourage you with high level of challenges?', TRUE),
('THEORY', 10, 'How does the Faculty counsel & encourage the Students?', TRUE),

('LAB', 1,  'All equipment''s/computer units were in working condition.', TRUE),
('LAB', 2,  'The laboratory manuals are available.', TRUE),
('LAB', 3,  'The laboratories were neat and clean with proper labeling and marking of equipment''s.', TRUE),
('LAB', 4,  'Each student gets a chance to perform the experiment/laboratory activities by his/her own hands.', TRUE),
('LAB', 5,  'The laboratory hours were mostly spent on performing experiments.', TRUE),
('LAB', 6,  'The laboratory was cooperative.', TRUE),
('LAB', 7,  'The laboratory teacher ensured the smooth conduct of the experiments.', TRUE),
('LAB', 8,  'The laboratory teacher had a full knowledge of the experiments.', TRUE),
('LAB', 9,  'The laboratory reports were checked timely with proper marking or grading.', TRUE),
('LAB', 10, 'Practical sessional exams were held in time.', TRUE);

-- Default super admin (username: admin / password: Admin@123 — CHANGE IMMEDIATELY AFTER FIRST LOGIN)
-- BCrypt hash of "Admin@123"
INSERT INTO admins (username, password_hash, full_name, role, is_active) VALUES
('admin', '$2a$10$m8gV.f4tncAv3CF5lR2.9ulCMDH0McFYo.j9vy3eKmNxqcPWFYgKC', 'System Administrator', 'SUPER_ADMIN', TRUE);
