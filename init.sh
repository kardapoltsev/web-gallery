echo "NOTE: should be run as root, first parameter should be user that will run webgallery"
user=root
if [ ! -z $1 ]; then
  user=$1
fi
install -o $user -g $user -d /var/lib/webgallery
su postgres -c "psql -f initdb.sql"
