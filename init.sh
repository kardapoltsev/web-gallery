echo "NOTE: should be run as root"
su postgres -c "psql -f initdb.sql"
