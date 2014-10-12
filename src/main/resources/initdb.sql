drop table if exists users cascade;
create table users
(
  id serial primary  key,
  name varchar not null,
  avatar_id integer not null,
  registration_time timestamp with time zone default now() not null,
  search_info tsvector not null
);
comment on column users.avatar_id is 'references to images (id). Not using constraint because of cross references of this tables';
create index "user_search_info_idx" on users using gin(search_info);
alter table users owner to webgallery;
grant all on table users to webgallery;


drop table if exists credentials cascade;
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

drop table if exists sessions cascade;
create table sessions
(
  id varchar primary key,
  user_id integer references users (id) on update cascade on delete cascade not null,
  update_time timestamp with time zone default now() not null
);
alter table sessions owner to webgallery;
grant all on table sessions to webgallery;

drop table if exists images cascade;
create table images
(
  id serial primary key,
  name varchar not null,
  filename varchar not null,
  owner_id integer references users (id) on update cascade on delete cascade not null
);
create index on images using btree(owner_id);
alter table images owner to webgallery;
grant all on table images to webgallery;

drop table if exists tags cascade;
create table tags
(
  id serial primary key,
  owner_id integer references users (id) on update cascade on delete cascade not null,
  name varchar not null,
  cover_id integer references images (id) on update cascade on delete restrict not null,
	manual_cover boolean not null,
	system boolean not null,
	auto boolean not null,
  update_time timestamp with time zone default now() not null,
  unique(owner_id, name)
);
create index on tags using btree(owner_id);
alter table tags owner to webgallery;
grant all on table tags to webgallery;

drop table if exists acl cascade;
create table acl
(
  id serial primary key,
  tag_id integer references tags (id) on update cascade on delete cascade not null,
  user_id integer references users (id) on update cascade on delete cascade not null,
  constraint unique_acl unique (tag_id, user_id)
);
create index on acl using btree(tag_id);
create index on acl using btree(user_id);
alter table acl owner to webgallery;
grant all on table acl to webgallery;

drop table if exists comment cascade;
create table comment
(
  id serial primary key,
  image_id integer references images (id) on update cascade on delete cascade not null,
  parent_comment_id integer references comment (id) on update cascade on delete cascade,
  text varchar not null,
  create_time timestamp with time zone default now() not null,
  owner_id integer references users (id) on update cascade on delete cascade not null
);
create index on comment(parent_comment_id);
create index on comment(image_id);
alter table comment owner to webgallery;
grant all on table comment to webgallery;

drop table if exists likes cascade;
create table "likes"
(
  id serial primary key,
  image_id integer references images (id) on update cascade on delete cascade not null,
  owner_id integer references users (id) on update cascade on delete cascade not null,
  create_time timestamp with time zone default now() not null,
  unique (image_id, owner_id)
);
create index on likes using btree(image_id);
alter table "likes" owner to webgallery;
grant all on table "likes" to webgallery;


drop table if exists metadata cascade;
create table metadata
(
  id serial primary key,
  image_id integer references images (id) on update cascade on delete cascade not null,
  camera_model varchar,
  creation_time timestamp with time zone
);
create index on metadata using btree(image_id);
alter table metadata owner to webgallery;
grant all on table metadata to webgallery;

drop table if exists alternative cascade;
create table alternative
(
  id serial primary key,
  image_id integer references images (id) on delete cascade not null,
  filename varchar not null,
  width integer,
  height integer,
  scale_type varchar not null
);
alter table alternative owner to webgallery;
grant all on table alternative to webgallery;


drop table if exists image_tag cascade;
create table image_tag
(
  image_id integer references images (id) on delete cascade,
  tag_id integer references tags (id) on delete cascade,

  primary key (image_id, tag_id)
);
create index on image_tag using btree(tag_id);
alter table image_tag owner to webgallery;
grant all on table image_tag to webgallery;

drop table if exists settings cascade;
create table settings (
  id serial primary key,
  version integer not null
);
alter table settings owner to webgallery;
grant all on table settings to webgallery;
