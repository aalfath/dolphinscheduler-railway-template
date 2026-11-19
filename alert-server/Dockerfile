# Apache DolphinScheduler alert server (sends alert notifications) for Railway.
FROM apache/dolphinscheduler-alert-server:3.4.3@sha256:b92d357082737c61c89e191a4604c3ea80f4335e2129d5df4492988c11660d9c

COPY DsInit.java /opt/ds-init/DsInit.java
RUN javac -d /opt/ds-init /opt/ds-init/DsInit.java && rm /opt/ds-init/DsInit.java
COPY start.sh /opt/railway/start.sh
RUN chmod +x /opt/railway/start.sh

# Fixed settings (see README). Connection settings come from Railway variables.
ENV TZ=UTC \
    SPRING_JACKSON_TIME_ZONE=UTC \
    DATABASE=postgresql \
    DS_HEAP=512m

ENTRYPOINT ["/opt/railway/start.sh"]
CMD []
