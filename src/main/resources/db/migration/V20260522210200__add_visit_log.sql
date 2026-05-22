CREATE TABLE visit_log
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    ip         VARCHAR(45) NOT NULL,
    visited_at DATE        NOT NULL,
    PRIMARY KEY (id),
    INDEX      idx_visited_at (visited_at),
    UNIQUE INDEX idx_ip_date (ip, visited_at)
);
