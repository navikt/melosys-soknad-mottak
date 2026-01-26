#!/usr/bin/env sh

set -x
exec java \
    ${DEFAULT_JVM_OPTS} \
    ${JAVA_OPTS} \
    -jar ${APP_JAR} \
    ${RUNTIME_OPTS} \
    $@
