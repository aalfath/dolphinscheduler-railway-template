# Apache ZooKeeper (DolphinScheduler's registry) for Railway.
FROM zookeeper:3.8.8@sha256:be95485f7fccfe8e7845fd5024eb56dd197db4d5a150939ca5e873f0a3bdf45e

# Keep snapshots and transaction logs on the Railway volume mounted at /data,
# purge old snapshots daily, and serve the AdminServer (health check) on 8080.
ENV ZOO_DATA_DIR=/data/data \
    ZOO_DATA_LOG_DIR=/data/datalog \
    ZOO_AUTOPURGE_PURGEINTERVAL=24 \
    ZOO_AUTOPURGE_SNAPRETAINCOUNT=3 \
    ZOO_ADMINSERVER_ENABLED=true \
    ZOO_4LW_COMMANDS_WHITELIST=srvr,ruok

COPY start.sh /railway-start.sh
RUN chmod +x /railway-start.sh

ENTRYPOINT ["/railway-start.sh"]
CMD ["zkServer.sh", "start-foreground"]
