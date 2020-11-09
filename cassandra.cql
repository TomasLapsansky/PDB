CREATE KEYSPACE IF NOT EXISTS pdb2020 WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};
USE pdb2020;

 CREATE TYPE IF NOT EXISTS like (
     author_name text,
     author_profile_url text,
     author_picture_url text,
     created_at timestamp,
 );

 CREATE TYPE IF NOT EXISTS comment (
     author_name text,
     author_profile_url text,
     author_picture_url text,
     content text,
     comment_likes list<frozen <like>>,
     created_at timestamp,
 );

CREATE TABLE IF NOT EXISTS user (
    email text,
    name text,
    surname text,
    password_hash text,
    profile_path text,
    profile_photo_path text,
    last_active timestamp,
    status boolean,
    created_at timestamp,
    PRIMARY KEY (email)
);

INSERT INTO user (email, name, surname, password_hash, profile_path, last_active, created_at) VALUES ('svetr@teply.cz', 'Peter', 'Sveter', '2$asf$afasf654', '/', '1904421664', '1604421664');

CREATE TABLE IF NOT EXISTS user_follows (
  user_email text,
  follows_email text,
  created_at timestamp,
  PRIMARY KEY (follows_email, user_email)
);

CREATE TABLE IF NOT EXISTS user_follower (
  user_email text,
  follower_email text,
  created_at timestamp,
  PRIMARY KEY (user_email, follower_email)
);

CREATE TABLE IF NOT EXISTS chat (
    id uuid,
    name text,
    updated_at timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_chat (
  user_email text,
  chat_id uuid,
  created_at timestamp,
  PRIMARY KEY (chat_id)
);

CREATE TABLE IF NOT EXISTS chat_message (
  from_user text,
  chat_id uuid,
  message text,
  created_at timestamp,
  primary key ((chat_id, from_user), created_at)
);

CREATE TABLE IF NOT EXISTS page (
    id uuid,
    name text,
    admin_email text,
    profile_photo_path text,
    last_active timestamp,
    PRIMARY KEY (name, id)
);

CREATE TABLE IF NOT EXISTS user_post (
    user_email text,
    user_profile_path text,
    content_type text,
    content text,
    likes list<frozen <like>>,
    comments list<frozen <comment>>,
    created_at timestamp,
    PRIMARY KEY ((user_email, content_type), created_at)
);

CREATE TABLE IF NOT EXISTS page_post (
    page_id uuid,
    page_name text,
    content_type text,
    content text,
    likes list<frozen <like>>,
    comments list<frozen <comment>>,
    created_at timestamp,
    PRIMARY KEY ((page_id, content_type), created_at)
);

INSERT INTO user (email, name, surname, password_hash, profile_path, last_active, created_at) VALUES ('teply@svetr.cz', 'Sveter', 'Fajnovy', '2$asf$afasf654', '/sveter', '1904421664', '1604421664');
INSERT INTO user (email, name, surname, password_hash, profile_path, last_active, created_at) VALUES ('teply@petr.cz', 'Peter', 'Teply', '2$asf$afasf654', '/peter', '1904421664', '1604421664');

INSERT INTO user_post (user_email, user_profile_path, content_type, content, created_at) VALUES ('teply@svetr.cz', '/sveter', 'text', 'Ahoj, mam fajny novy sveter. Peter.', '190442154');

UPDATE user_post SET likes = likes + [{author_name : 'Peter', author_profile_url: '/peter', created_at : '1604421664'}] WHERE user_email = 'teply@svetr.cz' AND content_type = 'text' AND created_at = '190442154';
UPDATE user_post SET likes = likes + [{author_name : 'Peter', author_profile_url: '/', created_at : '1604421674'}] WHERE user_email = 'teply@svetr.cz' AND content_type = 'text' AND created_at = '190442154';

CREATE OR REPLACE FUNCTION likesCount(data list<frozen <like>>) CALLED ON NULL INPUT RETURNS int LANGUAGE java AS 'return data.size();';

SELECT likesCount(likes) FROM user_post WHERE user_email = 'teply@svetr.cz' AND content_type = 'text' AND created_at = '190442154';



