CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    google_subject VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_google_subject UNIQUE (google_subject)
);

CREATE TABLE profiles (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    phone VARCHAR(50),
    location VARCHAR(255),
    headline VARCHAR(255),
    summary TEXT,
    total_experience_years INTEGER,
    resume_file_name VARCHAR(255),
    resume_uploaded_at TIMESTAMPTZ,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE profile_skills (
    profile_id UUID NOT NULL REFERENCES profiles (user_id) ON DELETE CASCADE,
    skill VARCHAR(255) NOT NULL,
    PRIMARY KEY (profile_id, skill)
);

CREATE TABLE experiences (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES profiles (user_id) ON DELETE CASCADE,
    company VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    description TEXT
);

CREATE TABLE educations (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES profiles (user_id) ON DELETE CASCADE,
    institution VARCHAR(255) NOT NULL,
    degree VARCHAR(255),
    field VARCHAR(255),
    start_year INTEGER,
    end_year INTEGER
);

CREATE TABLE job_preferences (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    min_experience INTEGER,
    max_experience INTEGER,
    notice_period_days INTEGER,
    expected_ctc_lpa NUMERIC(10, 2),
    work_authorization VARCHAR(255),
    open_to_relocation BOOLEAN
);

CREATE TABLE preferred_roles (
    user_id UUID NOT NULL REFERENCES job_preferences (user_id) ON DELETE CASCADE,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE preferred_locations (
    user_id UUID NOT NULL REFERENCES job_preferences (user_id) ON DELETE CASCADE,
    location VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, location)
);

CREATE TABLE standard_answers (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    requires_sponsorship BOOLEAN,
    willing_to_relocate BOOLEAN,
    linkedin_url VARCHAR(2048),
    github_url VARCHAR(2048),
    portfolio_url VARCHAR(2048)
);

CREATE TABLE custom_answers (
    user_id UUID NOT NULL REFERENCES standard_answers (user_id) ON DELETE CASCADE,
    question VARCHAR(1000) NOT NULL,
    answer TEXT NOT NULL,
    PRIMARY KEY (user_id, question)
);

CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    apply_url VARCHAR(2048),
    description TEXT,
    min_experience INTEGER,
    max_experience INTEGER,
    posted_at TIMESTAMPTZ,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_jobs_source_external_id UNIQUE (source, external_id)
);

CREATE TABLE job_skills (
    job_id UUID NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    skill VARCHAR(255) NOT NULL,
    PRIMARY KEY (job_id, skill)
);

CREATE TABLE user_job_state (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    saved BOOLEAN NOT NULL DEFAULT FALSE,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, job_id)
);

CREATE TABLE applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    application_mode VARCHAR(50) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_applications_user_job UNIQUE (user_id, job_id)
);

CREATE TABLE auto_apply_settings (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    min_match_score INTEGER,
    daily_cap INTEGER,
    require_approval BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE excluded_companies (
    user_id UUID NOT NULL REFERENCES auto_apply_settings (user_id) ON DELETE CASCADE,
    company VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, company)
);

CREATE TABLE signup_drafts (
    id UUID PRIMARY KEY,
    resume_path VARCHAR(512) NOT NULL,
    parsed_profile_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_experiences_profile_id ON experiences (profile_id);
CREATE INDEX idx_educations_profile_id ON educations (profile_id);
CREATE INDEX idx_jobs_active_posted_at ON jobs (active, posted_at DESC);
CREATE INDEX idx_user_job_state_job_id ON user_job_state (job_id);
CREATE INDEX idx_applications_job_id ON applications (job_id);
CREATE INDEX idx_signup_drafts_expires_at ON signup_drafts (expires_at);