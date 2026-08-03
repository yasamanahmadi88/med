-- Sanitized legacy Spring Boot 2.7 / JHipster authority schema (name PK).
-- Used by OracleLiquibaseUpgradeIT to prove forward Liquibase migration.
-- Contains NO production secrets.

CREATE TABLE jhi_user (
    id NUMBER(19) NOT NULL,
    login VARCHAR2(50) NOT NULL,
    password_hash VARCHAR2(60) NOT NULL,
    first_name VARCHAR2(50),
    last_name VARCHAR2(50),
    email VARCHAR2(191),
    image_url VARCHAR2(256),
    activated NUMBER(1) DEFAULT 0 NOT NULL,
    lang_key VARCHAR2(10),
    activation_key VARCHAR2(20),
    reset_key VARCHAR2(20),
    created_by VARCHAR2(50) NOT NULL,
    created_date TIMESTAMP,
    reset_date TIMESTAMP,
    last_modified_by VARCHAR2(50),
    last_modified_date TIMESTAMP,
    CONSTRAINT pk_jhi_user PRIMARY KEY (id),
    CONSTRAINT ux_user_login UNIQUE (login)
);

CREATE TABLE jhi_authority (
    name VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_jhi_authority PRIMARY KEY (name)
);

CREATE TABLE jhi_user_authority (
    user_id NUMBER(19) NOT NULL,
    authority_name VARCHAR2(50) NOT NULL,
    CONSTRAINT pk_jhi_user_authority PRIMARY KEY (user_id, authority_name),
    CONSTRAINT fk_authority_name FOREIGN KEY (authority_name) REFERENCES jhi_authority (name),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES jhi_user (id)
);

CREATE SEQUENCE sequence_generator START WITH 1050 INCREMENT BY 50;

INSERT INTO jhi_authority (name) VALUES ('ROLE_ADMIN');
INSERT INTO jhi_authority (name) VALUES ('ROLE_USER');

-- password = "admin" (JHipster default test hash); activated for upgrade verification only
INSERT INTO jhi_user (
    id, login, password_hash, first_name, last_name, email, activated, lang_key, created_by, created_date
) VALUES (
    1, 'admin',
    '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC',
    'Admin', 'User', 'admin@localhost', 1, 'en', 'system', SYSTIMESTAMP
);

INSERT INTO jhi_user (
    id, login, password_hash, first_name, last_name, email, activated, lang_key, created_by, created_date
) VALUES (
    2, 'user',
    '$2a$10$VEjxoAgXbP7JxuMb1FGnIegqQNCBeQfGLO4xk1rCmEtF4iGm3uUeK',
    'User', 'User', 'user@localhost', 1, 'en', 'system', SYSTIMESTAMP
);

INSERT INTO jhi_user_authority (user_id, authority_name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO jhi_user_authority (user_id, authority_name) VALUES (1, 'ROLE_USER');
INSERT INTO jhi_user_authority (user_id, authority_name) VALUES (2, 'ROLE_USER');

COMMIT;
