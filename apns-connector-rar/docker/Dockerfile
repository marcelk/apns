FROM jboss/wildfly:9.0.2.Final

ADD apns-connector-rar-0.0-SNAPSHOT.rar /

RUN /opt/jboss/wildfly/bin/jboss-cli.sh \
'embed-server -c=standalone-full.xml',\
'deploy /apns-connector-rar-0.0-SNAPSHOT.rar --name=apns-connector.rar',\
'/subsystem=ee:write-attribute(name=spec-descriptor-property-replacement, value=true)',\
'/subsystem=ee:write-attribute(name=annotation-property-replacement, value=true)',\
'/subsystem=resource-adapters/resource-adapter=apns:add(archive=apns-connector.rar)',\
'/subsystem=resource-adapters/resource-adapter=apns/connection-definitions=apns1:add(class-name=nl.meandi.apns.ManagedConnectionFactoryImpl, jndi-name=java:/eis/apns-connector)',\
'/subsystem=resource-adapters/resource-adapter=apns/connection-definitions=apns1/config-properties=certificateFileName:add(value=${env.APNS_CERTIFICATE_FILE_NAME})',\
'/subsystem=resource-adapters/resource-adapter=apns/connection-definitions=apns1/config-properties=certificateFilePassword:add(value=${env.APNS_CERTIFICATE_FILE_PASSWORD})',\
'/subsystem=resource-adapters/resource-adapter=apns:activate' && \
  rm -Rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
