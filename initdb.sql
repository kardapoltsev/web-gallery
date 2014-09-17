drop database if exists webgallery;

drop user webgallery;
create user webgallery with password 'webgallery';

create database webgallery;

grant all privileges on database webgallery to webgallery;
