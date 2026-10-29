#!/bin/sh
set -eu

: "${ANALITIQ_DB_URL:?Falta ANALITIQ_DB_URL}"
: "${ANALITIQ_DB_USER:?Falta ANALITIQ_DB_USER}"
: "${ANALITIQ_DB_PASSWORD:?Falta ANALITIQ_DB_PASSWORD}"
: "${ANALITIQ_DB_WRITE_USER:?Falta ANALITIQ_DB_WRITE_USER}"
: "${ANALITIQ_DB_WRITE_PASSWORD:?Falta ANALITIQ_DB_WRITE_PASSWORD}"

case "&${ANALITIQ_DB_URL#*\?}&" in
  *'&sslMode=VERIFY_IDENTITY&'*) ;;
  *) echo 'La URL JDBC debe incluir sslMode=VERIFY_IDENTITY.' >&2; exit 1 ;;
esac
port="${PORT:-10000}"
case "$port" in ''|*[!0-9]*) echo 'PORT debe ser numérico.' >&2; exit 1 ;; esac
if [ "$port" -lt 1024 ] || [ "$port" -gt 65535 ]; then
  echo 'PORT debe estar entre 1024 y 65535.' >&2; exit 1
fi

ca_file=/etc/secrets/aiven-ca.pem
if [ ! -r "$ca_file" ]; then
  echo 'Falta el certificado CA de Aiven en Secret Files: aiven-ca.pem.' >&2
  exit 1
fi
# Solo contiene un certificado público, nunca la contraseña de MySQL.
trust_dir=$(mktemp -d /tmp/analitiq-tls.XXXXXX)
keytool -importcert -noprompt -alias aiven-project-ca -file "$ca_file" \
  -keystore "$trust_dir/truststore.p12" -storetype PKCS12 -storepass changeit
export CATALINA_OPTS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m -Duser.timezone=America/Argentina/Buenos_Aires -Danalitiq.http.port=$port -Djavax.net.ssl.trustStore=$trust_dir/truststore.p12 -Djavax.net.ssl.trustStoreType=PKCS12 -Djavax.net.ssl.trustStorePassword=changeit"
exec /usr/local/tomcat/bin/catalina.sh run
