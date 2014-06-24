drop database if exists webgallery;

drop user webgallery;
create user webgallery with password 'webgallery';

create database webgallery;

grant all privileges on database webgallery to webgallery;

\c webgallery;


--drop table if exists user;
create table users
(
  id serial primary key,
  name varchar not null,
  registration_time timestamp with time zone default now() not null
);
alter table users owner to webgallery;
grant all on table users to webgallery;


--drop table if exists credentials;
create table credentials
(
  id serial primary key,
  auth_id varchar not null,
  auth_type varchar not null,
  password_hash varchar,
  user_id integer references users (id) on update cascade on delete cascade not null unique,
  unique(auth_id, auth_type)
);
comment on column credentials.auth_id is 'unique user auth id. It could be email or vk.com user id';
comment on column credentials.auth_type is 'auth type like Direct, VK, Github etc';
alter table credentials owner to webgallery;
grant all on table credentials to webgallery;

--drop table if exists user;
create table sessions
(
  id serial primary key,
  user_id integer references users (id) on update cascade on delete cascade not null,
  update_time timestamp with time zone default now() not null
);
alter table sessions owner to webgallery;
grant all on table sessions to webgallery;

--drop table if exists tags;
create table tags
(
  id serial primary key,
  name varchar unique not null
);
alter table tags owner to webgallery;
grant all on table tags to webgallery;

--drop table if exists acl;
create table acl
(
  id serial primary key,
  tag_id integer references tags (id) on update cascade on delete cascade not null,
  user_id integer references users (id) on update cascade on delete cascade not null,
  constraint unique_acl unique (tag_id, user_id)
);
alter table acl owner to webgallery;
grant all on table acl to webgallery;

--drop table if exists image;
create table image 
(
  id serial primary key, 
  name varchar not null,
  filename varchar not null,
  owner_id integer references users (id) on update cascade on delete cascade not null
);
alter table image owner to webgallery;
grant all on table image to webgallery;


--drop table if exists metadata;
create table metadata 
(
  id serial primary key,
  image_id integer references image (id) on update cascade on delete cascade not null,
  camera_model varchar,
  creation_time timestamp with time zone
);
alter table metadata owner to webgallery;
grant all on table metadata to webgallery;

--create type scale_type as enum('FitSource', 'FillDest');

--drop table if exists alternative;
create table alternative 
(
  id serial primary key, 
  image_id integer references image (id) on delete cascade not null,
  filename varchar not null,
  width integer not null,
  height integer not null,
  scale_type varchar not null
);
alter table alternative owner to webgallery;
grant all on table alternative to webgallery;


--drop table if exists image_tags;
create table image_tag
(
  image_id integer references image (id) on delete cascade,
  tag_id integer references tags (id) on delete cascade,

  primary key (image_id, tag_id)
);
alter table image_tag owner to webgallery;
grant all on table image_tag to webgallery;
