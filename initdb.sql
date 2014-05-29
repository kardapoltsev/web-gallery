drop database if exists webgallery;

drop user webgallery;
create user webgallery with password 'webgallery';

create database webgallery;

grant all privileges on database webgallery to webgallery;

\c webgallery;

--drop table if exists metadata;
create table metadata 
(
  id serial primary key,
  camera_model varchar,
  creation_time timestamp with time zone
);
alter table metadata owner to webgallery;
grant all on table metadata to webgallery;


--drop table if exists tags;
create table tags 
(
  id serial primary key,
  name varchar unique
);
alter table tags owner to webgallery;
grant all on table tags to webgallery;


--drop table if exists images;
create table images 
(
  id serial primary key,
  name varchar not null,
  filename varchar not null,
  metadata_id integer references metadata (id)
);
alter table images owner to webgallery;
grant all on table images to webgallery;


--drop table if exists images_tags;
create table images_tags
(
  image_id integer references images (id) on delete cascade,
  tag_id integer references tags (id) on delete cascade,

  primary key (image_id, tag_id)
);
alter table images_tags owner to webgallery;
grant all on table images_tags to webgallery;
