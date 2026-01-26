#!/bin/sh

# Import environment variables from Vault (mimics NAV baseimage behavior)
if test -d /var/run/secrets/nais.io/vault; then
    for FILE in $(find /var/run/secrets/nais.io/vault -maxdepth 1 -name "*.env" 2>/dev/null); do
        while IFS= read -r line || [ -n "$line" ]; do
            _key=${line%%=*}
            _val=${line#*=}
            if test "$_key" != "$line"; then
                echo "- exporting $_key"
                # Remove surrounding quotes from value
                _val=$(echo "$_val" | sed -e "s/^['\"]//" -e "s/['\"]$//")
                export "$_key"="$_val"
            fi
        done < "$FILE"
    done
fi

# Configure truststore if available (mimics NAV baseimage 00-truststore.sh)
if test -r "${NAV_TRUSTSTORE_PATH}"; then
    if ! keytool -list -keystore "${NAV_TRUSTSTORE_PATH}" -storepass "${NAV_TRUSTSTORE_PASSWORD}" > /dev/null 2>&1; then
        echo "Warning: Truststore is corrupt, or bad password"
    else
        JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.trustStore=${NAV_TRUSTSTORE_PATH}"
        JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.trustStorePassword=${NAV_TRUSTSTORE_PASSWORD}"
        export JAVA_OPTS
        echo "Truststore configured: ${NAV_TRUSTSTORE_PATH}"
    fi
fi

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar
