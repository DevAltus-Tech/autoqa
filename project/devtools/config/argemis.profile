# Set the Artemis home directory
ARTEMIS_HOME=/var/lib/artemis

# Set the Artemis instance directory
ARTEMIS_INSTANCE=/var/lib/artemis-instance

# Set the Artemis username and password
ARTEMIS_USERNAME=admin
ARTEMIS_PASSWORD=admin

# Set the Java options
JAVA_ARGS="-Xmx1G -Djava.net.preferIPv4Addresses=true -XX:+UnlockExperimentalVMOptions -XX:MaxRAMFraction=2 -XX:+PrintClassHistogram -XX:+UseG1GC -XX:+UseStringDeduplication -Dhawtio.rolePrincipalClasses=org.apache.activemq.artemis.spi.core.security.jaas.RolePrincipal -Djolokia.policyLocation=file:///var/lib/artemis/etc/jolokia-access.xml -Dhawtio.csrfEnabled=false"

# Set the extra arguments for Artemis
EXTRA_ARGS="--http-host 0.0.0.0 --relax-jolokia"

HAWTIO_ROLE='amq'
ARTEMIS_ETC_DIR='/var/lib/artemis-instance/etc'
