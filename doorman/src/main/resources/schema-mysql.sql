CREATE TABLE IF NOT EXISTS user_entity (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(8) NOT NULL UNIQUE,
  password VARCHAR(16) NOT NULL,
  last_accessed_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`)
);

insert ignore into user_entity(username, password) values('jordan', 'password');
