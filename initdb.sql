drop database if exists webgallery;

drop user webgallery;
create user webgallery with password 'webgallery';

create database webgallery;

grant all privileges on database webgallery to webgallery;

\c webgallery;

drop sequence if exists s$images$id;
create sequence s$images$id;
grant all on sequence "s$images$id" to webgallery;


drop table if exists images;
create table images 
(
  id bigint not null default nextval('"s$images$id"'),
  filename varchar not null,
  name varchar not null,
  metadata_id bigint not null,

  constraint images_pk primary key (id)
);

alter table images owner to webgallery;
grant all on table images to webgallery;
