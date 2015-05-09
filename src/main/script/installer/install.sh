#! /bin/sh
# ${pom.artifactId} install script.
###

echo Adding user...
/usr/sbin/groupadd segmentationservice
/usr/sbin/useradd -g segmentationservice segmentationservice

echo Creating log folders...
mkdir -p /var/log/segmentationservice-service
/bin/chown -R segmentationservice:segmentationservice /var/log/segmentationservice-service
chmod -R 755 /var/log/segmentationservice-service

echo Creating log folders for pid...
mkdir -p $INSTALL_PATH/logs
/bin/chown -R segmentationservice:segmentationservice $INSTALL_PATH/logs
chmod -R 755 $INSTALL_PATH/logs

echo Configuring application...
/bin/chown -R segmentationservice:segmentationservice $INSTALL_PATH
/bin/chmod 755 $INSTALL_PATH/bin/${pom.artifactId}
/bin/chmod 755 $INSTALL_PATH/bin/wrapper-*
echo Done.

echo Installing service...
ln -s $INSTALL_PATH/bin/${pom.artifactId} /etc/init.d/${pom.artifactId}
/bin/chmod 755 /etc/init.d/${pom.artifactId}
/sbin/chkconfig --add ${pom.artifactId}
echo Done.