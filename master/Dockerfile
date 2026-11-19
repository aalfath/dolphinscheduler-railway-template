# Apache DolphinScheduler master server (schedules workflows and dispatches tasks) for Railway.
FROM apache/dolphinscheduler-master:3.4.3@sha256:fef3a3998d16a66065f4cec5a3513d46122d6dacee473efeb0c3d1d5025883ac

COPY DsInit.java /opt/ds-init/DsInit.java
RUN javac -d /opt/ds-init /opt/ds-init/DsInit.java && rm /opt/ds-init/DsInit.java
COPY start.sh /opt/railway/start.sh
RUN chmod +x /opt/railway/start.sh

# Fixed settings (see README). Connection settings come from Railway variables.
ENV TZ=UTC \
    SPRING_JACKSON_TIME_ZONE=UTC \
    DATABASE=postgresql \
    MASTER_SERVERLOADPROTECTION_ENABLED=false \
    RESOURCE_STORAGE_TYPE=S3 \
    RESOURCE_STORAGE_UPLOAD_BASE_PATH=/dolphinscheduler \
    AWS_S3_CREDENTIALS_PROVIDER_TYPE=AWSStaticCredentialsProvider \
    REMOTE_LOGGING_ENABLE=true \
    REMOTE_LOGGING_TARGET=S3 \
    REMOTE_LOGGING_BASE_DIR=logs

ENTRYPOINT ["/opt/railway/start.sh"]
CMD []
