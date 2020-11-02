SET FOREIGN_KEY_CHECKS=0;
drop table if exists wall cascade ;
drop table if exists user cascade ;
drop table if exists user_page cascade ;
drop table if exists state cascade ;
drop table if exists page cascade ;
drop table if exists photo cascade ;
drop table if exists post cascade ;
drop table if exists post_like cascade ;
drop table if exists comment_like cascade ;
drop table if exists comment cascade ;
drop table if exists subscribed_to cascade ;
drop table if exists chat cascade ;
drop table if exists user_chat cascade ;
drop table if exists message cascade ;

CREATE TABLE state
(
    id int auto_increment primary key ,
    name varchar(64) not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false
);

INSERT INTO state (name) VALUES ('Slovak republic'), ('Czech republic'), ('Austria'), ('Germany'), ('Poland'), ('Hungary');

CREATE TABLE wall
(
    id bigint auto_increment primary key ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false
);

CREATE TABLE photo
(
    id bigint auto_increment primary key ,
    path varchar(256) not null ,
    user_id bigint not null ,
    page_id bigint not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false
);



CREATE TABLE user
(
    id bigint auto_increment primary key ,
    name varchar(32) not null ,
    surname varchar(32) not null ,
    email varchar(64) not null ,
    password_hash char(60) not null ,
    profile_photo_id bigint,
    gender enum('M', 'F') not null ,
    address varchar(64),
    city varchar(32),
    state_id int,
    wall_id bigint not null,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_user_wall_id FOREIGN KEY (wall_id) REFERENCES wall (id),
    CONSTRAINT fk_user_state_id FOREIGN KEY (state_id) REFERENCES state (id),
    CONSTRAINT fk_user_profile_photo_id FOREIGN KEY (profile_photo_id) REFERENCES photo (id)
);

CREATE TABLE page
(
    id bigint auto_increment primary key ,
    name varchar(32) not null ,
    admin_id bigint not null ,
    profile_photo_id bigint,
    wall_id bigint not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_page_admin_id FOREIGN KEY (admin_id) REFERENCES user (id),
    CONSTRAINT fk_page_profile_photo_id FOREIGN KEY (profile_photo_id) REFERENCES photo (id),
    CONSTRAINT fk_page_wall_id FOREIGN KEY (wall_id) REFERENCES wall (id)
);

ALTER TABLE photo ADD CONSTRAINT fk_photo_user_id FOREIGN KEY (user_id) REFERENCES user (id);
ALTER TABLE photo ADD CONSTRAINT fk_photo_page_id FOREIGN KEY (page_id) REFERENCES page (id);

CREATE TABLE user_page
(
    id bigint auto_increment primary key ,
    user_id bigint not null ,
    page_id bigint not null ,
    is_admin bool default true,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_user_page_user_id FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_user_page_page_id FOREIGN KEY (page_id) REFERENCES page (id)
);

CREATE TABLE post
(
    id bigint auto_increment primary key ,
    content_type enum('text', 'image') not null ,
    content text not null ,
    user_id bigint,
    page_id bigint,
    wall_id bigint,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_post_user_id FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_post_page_id FOREIGN KEY (page_id) REFERENCES page (id),
    CONSTRAINT fk_post_wall_id FOREIGN KEY (wall_id) REFERENCES wall (id)
);

CREATE TABLE subscribed_to
(
    id bigint auto_increment primary key ,
    subscriber_id bigint not null ,
    subscribed_to_user bigint not null ,
    subscribed_to_page bigint not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_subscribed_to_subscriber_id FOREIGN KEY (subscriber_id) REFERENCES user (id),
    CONSTRAINT fk_subscribed_to_subscribed_to_user FOREIGN KEY (subscribed_to_user) REFERENCES user (id),
    CONSTRAINT fk_subscribed_to_subscribed_to_page FOREIGN KEY (subscribed_to_page) REFERENCES page (id)
);

CREATE TABLE chat
(
  id bigint auto_increment primary key ,
  name varchar(32),
  created_at timestamp default CURRENT_TIMESTAMP,
  updated_at timestamp default CURRENT_TIMESTAMP,
  deleted bool default false
);

CREATE TABLE user_chat
(
  id bigint auto_increment primary key ,
  user_id bigint not null ,
  chat_id bigint not null ,
  created_at timestamp default CURRENT_TIMESTAMP,
  updated_at timestamp default CURRENT_TIMESTAMP,
  deleted bool default false,
  CONSTRAINT fk_user_chat_user_id FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT fk_user_chat_chat_id FOREIGN KEY (chat_id) REFERENCES chat (id)
);

CREATE TABLE message
(
    id bigint auto_increment primary key ,
    content text not null ,
    author_id bigint not null ,
    chat_id bigint not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_message_author_id FOREIGN KEY (author_id) REFERENCES user (id),
    CONSTRAINT fk_message_chat_id FOREIGN KEY (chat_id) REFERENCES chat (id)
);

CREATE TABLE comment
(
    id bigint auto_increment primary key ,
    content text not null ,
    user_id bigint not null ,
    page_id bigint not null ,
    post_id bigint not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_comment_user_id FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_comment_page_id FOREIGN KEY (page_id) REFERENCES page (id),
    CONSTRAINT fk_comment_post_id FOREIGN KEY (post_id) REFERENCES post (id)
);

CREATE TABLE comment_like
(
    id bigint auto_increment primary key ,
    user_id bigint not null ,
    comment_id bigint not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_comment_like_user_id FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_comment_like_comment_id FOREIGN KEY (comment_id) REFERENCES comment (id)
);

CREATE TABLE post_like
(
    id bigint auto_increment primary key ,
    user_id bigint not null ,
    post_id bigint not null ,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    deleted bool default false,
    CONSTRAINT fk_post_like_user_id FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_post_like_post_id FOREIGN KEY (post_id) REFERENCES post (id)
);
