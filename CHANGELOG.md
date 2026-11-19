# Changelog

Breaking changes are listed first in each entry. Read the entry before you apply an update on Railway.

## 3.4.3-railway.2 (2026-11-19)

No breaking changes and no change to the deployed services.

- README: the Deploy on Railway button now links to the published template code `apache-dolphinscheduler` (it pointed at `apache-dolphinsche`, which does not exist).

## 3.4.3-railway.1 (2026-11-19)

First release of the template.

- Apache DolphinScheduler 3.4.3 (official `apache/dolphinscheduler-api|master|worker|alert-server` images, pinned by digest), ZooKeeper 3.8.8, Railway Postgres and a Railway Bucket.
- Schema created or upgraded on start (`upgrade-schema.sh` under a Postgres advisory lock), and the default `admin` password replaced with the generated `ADMIN_PASSWORD`.
- Resource center and task logs stored in the bucket (S3); worker data and ZooKeeper data on volumes.
- Fix for Railway's IPv6 private network: the master, worker and alert server advertise their address in the same form DolphinScheduler uses in heartbeats.
- Caddy in front of the API: `/` redirects to the UI, and only `/dolphinscheduler/actuator/health` of the actuator endpoints is public.
