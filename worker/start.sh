#!/bin/bash
# DolphinScheduler worker server for Railway: keeps task working directories and logs on the
# /data volume, waits for ZooKeeper, then starts the worker.
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

mkdir -p /data/exec /data/logs
if [ ! -L /opt/dolphinscheduler/logs ]; then
  rm -r /opt/dolphinscheduler/logs 2>/dev/null || true
  ln -s /data/logs /opt/dolphinscheduler/logs
fi

wait_zookeeper
exec /bin/bash /opt/dolphinscheduler/worker-server/bin/start.sh
