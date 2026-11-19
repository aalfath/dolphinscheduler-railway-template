#!/bin/bash
# DolphinScheduler master server for Railway: creates or upgrades the schema if needed
# (shared lock with the other services), waits for ZooKeeper, then starts the master.
set -e
cd /opt/dolphinscheduler

# Railway's private network is IPv6: make DolphinScheduler register and resolve IPv6 addresses.
NET_OPTS=""
[ -n "${RAILWAY_PRIVATE_DOMAIN:-}" ] && NET_OPTS="-Djava.net.preferIPv6Addresses=true"
export JAVA_OPTS="${JAVA_OPTS:--server -Xms256m -Xmx${DS_HEAP:-1g} -XX:+ExitOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -Duser.timezone=${SPRING_JACKSON_TIME_ZONE:-UTC} ${NET_OPTS}}"

wait_zookeeper() {
  local cs="${REGISTRY_ZOOKEEPER_CONNECT_STRING%%,*}"
  local host="${cs%:*}" port="${cs##*:}" i=0
  until timeout 3 bash -c "exec 3<>/dev/tcp/${host}/${port}" 2>/dev/null; do
    i=$((i+1)); [ "$i" -gt 300 ] && { echo "[start] ZooKeeper ${cs} not reachable, giving up"; exit 1; }
    [ $((i % 15)) -eq 1 ] && echo "[start] waiting for ZooKeeper at ${cs}"
    sleep 2
  done
  echo "[start] ZooKeeper ${cs} is reachable"
}
PGJAR=$(ls /opt/dolphinscheduler/libs/postgresql-*.jar | head -1)
ds_init() { java -cp "/opt/ds-init:${PGJAR}" ${NET_OPTS} DsInit "$@"; }

ds_init schema
wait_zookeeper
exec /bin/bash /opt/dolphinscheduler/master-server/bin/start.sh
