echo "building web"
cd web
r.js -o app.build.js
cd ..

echo "building server"
sbt 'debian:packageBin'
filename=webgallery_1.0.0-SNAPSHOT_amd64.deb
scp ./target/$filename sunrise:
ssh -t fsunrise.ru "sudo dpkg -i $filename"
ssh -t fsunrise.ru "rm $filename"
rm ./target/$filename
