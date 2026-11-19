# Apache DolphinScheduler worker server (runs the tasks) for Railway.
FROM apache/dolphinscheduler-worker:3.4.3@sha256:0ac2516d92ff704aaee9b5d17f3f4550a9920ed0ddd2faa8a4f0b9204f4bee40

COPY DsInit.java /opt/ds-init/DsInit.java
RUN javac -d /opt/ds-init /opt/ds-init/DsInit.java && rm /opt/ds-init/DsInit.java
COPY start.sh /opt/railway/start.sh
RUN chmod +x /opt/railway/start.sh

# Fixed settings (see README). Connection settings come from Railway variables.
ENV TZ=UTC \
    SPRING_JACKSON_TIME_ZONE=UTC \
    DATABASE=postgresql \
    WORKER_SERVERLOADPROTECTION_ENABLED=false \
    WORKER_TENANTCONFIG_DEFAULTTENANTENABLED=true \
    DATA_BASEDIR_PATH=/data/exec \
    RESOURCE_STORAGE_TYPE=S3 \
    RESOURCE_STORAGE_UPLOAD_BASE_PATH=/dolphinscheduler \
    AWS_S3_CREDENTIALS_PROVIDER_TYPE=AWSStaticCredentialsProvider \
    REMOTE_LOGGING_ENABLE=true \
    REMOTE_LOGGING_TARGET=S3 \
    REMOTE_LOGGING_BASE_DIR=logs

ENTRYPOINT ["/opt/railway/start.sh"]
CMD []
