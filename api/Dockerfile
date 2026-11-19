# Apache DolphinScheduler API server (serves the web UI) for Railway.
FROM caddy:2.11.9@sha256:4851e54a46b206517c574821bb5cc67f170d2156041e9fbc4c9193b45e84a75b AS caddy

FROM apache/dolphinscheduler-api:3.4.3@sha256:0da0b1c6ffb0979d4f359bd8d063b1a68f1fb71b7d08a3d884097893f7e2b177

COPY --from=caddy /usr/bin/caddy /usr/local/bin/caddy
COPY DsInit.java /opt/ds-init/DsInit.java
RUN javac -d /opt/ds-init /opt/ds-init/DsInit.java && rm /opt/ds-init/DsInit.java
COPY Caddyfile /etc/caddy/Caddyfile
COPY start.sh /opt/railway/start.sh
RUN chmod +x /opt/railway/start.sh

# Fixed settings (see README). Connection settings come from Railway variables.
ENV TZ=UTC \
    SPRING_JACKSON_TIME_ZONE=UTC \
    DATABASE=postgresql \
    RESOURCE_STORAGE_TYPE=S3 \
    RESOURCE_STORAGE_UPLOAD_BASE_PATH=/dolphinscheduler \
    AWS_S3_CREDENTIALS_PROVIDER_TYPE=AWSStaticCredentialsProvider \
    REMOTE_LOGGING_ENABLE=true \
    REMOTE_LOGGING_TARGET=S3 \
    REMOTE_LOGGING_BASE_DIR=logs

ENTRYPOINT ["/opt/railway/start.sh"]
CMD []
