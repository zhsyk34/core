CREATE TABLE IF NOT EXISTS udpRecord (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ip         VARCHAR(60)     NOT NULL,
  sn         VARCHAR(60)     NOT NULL,
  port       VARCHAR(60)     NOT NULL,
  createTime TIMESTAMP       NOT NULL,
  updateTime TIMESTAMP       NOT NULL,
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = UTF8;

ALTER TABLE udpRecord
  ADD CONSTRAINT unique_ip_sn UNIQUE KEY (ip, sn);

