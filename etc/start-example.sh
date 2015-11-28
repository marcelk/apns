#!/bin/bash

CERT_FILE_NAME=$1
CERT_FILE_PWD=$2

docker create \
  -p 8080:8080 \
  -e APNS_CERTIFICATE_FILE_NAME=/opt/jboss/wildfly/standalone/configuration/certificate.p12 \
  -e APNS_CERTIFICATE_FILE_PASSWORD=$CERT_FILE_PWD \
  -ti \
  --name=apns-example \
  apns-example 
docker cp $CERT_FILE_NAME apns-example:/opt/jboss/wildfly/standalone/configuration/certificate.p12
docker start apns-example
docker logs -f apns-example
docker stop apns-example
docker rm apns-example
