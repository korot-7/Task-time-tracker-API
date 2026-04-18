CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE time_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE RESTRICT,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,
    work_description TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_time_records_interval CHECK (ended_at > started_at)
);

CREATE INDEX idx_time_records_employee_id ON time_records (employee_id);
CREATE INDEX idx_time_records_task_id ON time_records (task_id);
CREATE INDEX idx_time_records_started_at ON time_records (started_at);
