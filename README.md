web-gallery
===========

[![Build Status](https://travis-ci.org/kardapoltsev/web-gallery.svg?branch=master)](https://travis-ci.org/kardapoltsev/web-gallery)

# Simple photos gallery

## Requirements
### Run
- java 7 or higher
- postgresql 9.1 or higher

### Build
- sbt
- bower

## How to run

- init database with `sudo -u postgresql ./initdb.sh`
- load javascript libs with `cd web; bower install; cd ..`
- check config file `reference.conf` and fix routes
- sbt ~reStart