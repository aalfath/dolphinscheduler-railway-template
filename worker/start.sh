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

# DolphinScheduler 3.4.3 writes IPv6 addresses as "[ip]:port" in its own config but as "ip:port"
# in heartbeats, so on Railway's IPv6 network the master never finds its own slot and never runs
# workflows. Pin the advertised address to the heartbeat form (DolphinScheduler's own NetUtils).
advertise_address() {
  local var="$1" port="$2" conf="$3" h
  [ -n "${!var:-}" ] && return 0
  h=$(java -cp "/opt/ds-init:/opt/dolphinscheduler/${conf}/conf:/opt/dolphinscheduler/libs/*" ${NET_OPTS} DsInit host 2>/dev/null | sed -n 's/^DSHOST=//p' | tail -1)
  if [ -n "$h" ]; then export "$var=${h}:${port}"; echo "[start] advertising ${!var} (${var})"; fi
}

wait_zookeeper
advertise_address WORKER_WORKERADDRESS "${WORKER_LISTENPORT:-1234}" worker-server
exec /bin/bash /opt/dolphinscheduler/worker-server/bin/start.sh
