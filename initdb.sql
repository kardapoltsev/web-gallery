drop database if exists webgallery;

drop user webgallery;
create user webgallery with password 'webgallery';

create database webgallery;

grant all privileges on database webgallery to webgallery;

\c webgallery;

--drop table if exists tag;
create table tag 
(
  id serial primary key,
  name varchar unique not null
);
alter table tag owner to webgallery;
grant all on table tag to webgallery;


--drop table if exists image;
create table image 
(
  id serial primary key,
  name varchar not null,
  filename varchar not null
);
alter table image owner to webgallery;
grant all on table image to webgallery;


--drop table if exists metadata;
create table metadata 
(
  id serial primary key,
  image_id integer references image (id) on delete cascade not null,
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


--drop table if exists image_tag;
create table image_tag
(
  image_id integer references image (id) on delete cascade,
  tag_id integer references tag (id) on delete cascade,

  primary key (image_id, tag_id)
);
alter table image_tag owner to webgallery;
grant all on table image_tag to webgallery;
