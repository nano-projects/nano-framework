/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE TABLE users (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  username varchar(32) NOT NULL,
  password char(32) NOT NULL,
  password_salt varchar(256),
  email varchar(64) NOT NULL,
  status tinyint(1) NOT NULL DEFAULT 1,
  locked tinyint(1) NOT NULL DEFAULT 0,
  activate tinyint(1) NOT NULL DEFAULT 0,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modify_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY IDX_USERNAME (username)
);

INSERT INTO users VALUES 
(1,'admin','d578068f36c539d4ebd7c8a8cb6971aa','N0ZBOTVFODQxQjI4RTM3NjY3M0EwNTJBMUIzRTVEQzQyQzMzRUIxQjY1RkZFRDI3MTNCRjcxNUQ2Qjg3MzQyQTY5NjYyRDk1MzZEMUExQjFEQ0I0QzhFQ0VEQzA5RDJC0','admin@example.com',1,0,1,'2016-05-31 15:00:00','2016-06-03 04:09:44',0),
(2,'admin1','d578068f36c539d4ebd7c8a8cb6971aa','N0ZBOTVFODQxQjI4RTM3NjY3M0EwNTJBMUIzRTVEQzQyQzMzRUIxQjY1RkZFRDI3MTNCRjcxNUQ2Qjg3MzQyQTY5NjYyRDk1MzZEMUExQjFEQ0I0QzhFQ0VEQzA5RDJC0','admin1@example.com',1,0,1,'2016-05-31 15:00:00','2016-06-03 04:09:44',0);