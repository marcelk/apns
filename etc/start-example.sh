#!/bin/bash

CERT_FILE_NAME=$1
CERT_FILE_PWD=$2

docker create \
  -p 8080:8080 \
  -e APNS_CERTIFICATE_FILE_NAME=/opt/jboss/wildfly/standalone/configuration/certificate.p12 \
  -e APNS_CERTIFICATE_FILE_PASSWORD=$CERT_FILE_PWD \
  -ti \
  --name=apns-notifier \
  apns-example-notifier
docker cp $CERT_FILE_NAME apns-notifier:/opt/jboss/wildfly/standalone/configuration/certificate.p12
docker start apns-notifier
docker logs -f apns-notifier
docker stop apns-notifier
docker rm apns-notifier
