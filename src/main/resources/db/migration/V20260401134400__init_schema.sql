CREATE TABLE users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE category
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(50)  NOT NULL,
    slug          VARCHAR(100) NOT NULL,
    display_order INT          NOT NULL DEFAULT 0,
    post_count    INT          NOT NULL DEFAULT 0,
    created_at    DATETIME(6),
    updated_at    DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name),
    UNIQUE KEY uk_category_slug (slug)
);

CREATE TABLE tag
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(30) NOT NULL,
    slug       VARCHAR(50) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name),
    UNIQUE KEY uk_tag_slug (slug)
);

CREATE TABLE post
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    author_id    BIGINT       NOT NULL,
    category_id  BIGINT,
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    content_html TEXT         NOT NULL,
    summary      VARCHAR(500),
    slug         VARCHAR(250) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    view_count   INT          NOT NULL DEFAULT 0,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_slug (slug),
    CONSTRAINT fk_post_to_user FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_post_to_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE post_tag
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    post_id    BIGINT,
    tag_id     BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_post_tag_to_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_tag_to_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);
