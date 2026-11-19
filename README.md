[![Deploy on Railway](https://railway.com/button.svg)](https://railway.com/new/template/apache-dolphinscheduler?utm_medium=integration&utm_source=button&utm_campaign=dolphinscheduler)

# Deploy and Host Apache DolphinScheduler on Railway

Apache DolphinScheduler is an open-source, low-code workflow orchestration platform from the Apache Software Foundation. You build data pipelines as visual DAGs in a web UI (shell, SQL, Python, Spark, Flink, HTTP and many more task types), schedule them with cron, and watch, rerun or backfill every run from one place.

## About Hosting Apache DolphinScheduler

DolphinScheduler is a distributed system: an API server that also serves the web UI, a master that schedules workflows, workers that run the tasks, an alert server, ZooKeeper as the registry, and PostgreSQL for metadata. Hosting it yourself means wiring these services together, creating the schema, replacing the default admin password and giving masters and workers shared storage for resources and task logs. This template deploys the full stack of DolphinScheduler 3.4.3 on Railway's private network. It creates or upgrades the schema on start, sets a generated admin password, and stores resources and task logs in a Railway Bucket, so data survives redeploys.

## Common Use Cases

* Scheduling and monitoring ETL and ELT pipelines (SQL, shell, Python, Spark, Flink, DataX)
* Replacing cron jobs spread over several servers with one visual, retryable workflow system
* Backfilling historical data and running dependent workflows across teams and projects

## Dependencies for Apache DolphinScheduler Hosting

* PostgreSQL (Railway's Postgres) for workflow definitions, run history and users
* Apache ZooKeeper as the registry for masters, workers and the alert server
* S3-compatible object storage (a Railway Bucket) for the resource center and task logs

### Deployment Dependencies

* [DolphinScheduler 3.4.3 release](https://github.com/apache/dolphinscheduler/releases/tag/3.4.3)
* [Official Docker images](https://hub.docker.com/u/apache) (`apache/dolphinscheduler-api`, `-master`, `-worker`, `-alert-server`)
* [Docker deployment guide](https://dolphinscheduler.apache.org/en-us/docs/3.4.3/guide/start/docker)
* [Template source](https://github.com/aalfath/dolphinscheduler-railway-template)

### Implementation Details

Services in this template:

| Service | What it does | Storage |
|---|---|---|
| API | Web UI and REST API (public URL); creates or upgrades the schema and sets the admin password on start | - |
| Master | Schedules workflows and dispatches tasks to workers | - |
| Worker | Runs the tasks (shell, SQL, Python and so on) | Volume `/data` (task working dirs, local logs) |
| Alert Server | Sends alert notifications (email, Slack, webhooks and more, set up in the UI) | - |
| ZooKeeper | Registry and coordination for the servers above | Volume `/data` |
| Postgres | Metadata database | Volume |
| Bucket | Resource center files and task logs (S3) | Railway Bucket |

**After deploy:** open the API service's public URL (it redirects to `/dolphinscheduler/ui/`) and log in as `admin` with the password in the API service's `ADMIN_PASSWORD` variable. Change it under *Profile → Edit password* if you like; a password changed in the UI is kept across redeploys. Then create a tenant and a project and build your first workflow.

**Variables:** none to fill in. `ADMIN_PASSWORD` is generated, and the database, ZooKeeper and bucket settings are reference variables. Optional features (LDAP/OIDC login, email or IM alerts, data sources) are configured in the UI.

**Scaling:** add workers by raising the Worker service's replicas. Every service keeps its state in Postgres, ZooKeeper or the bucket, so replicas share everything. Each Java service starts with a 1 GB heap (512 MB for the alert server); set `DS_HEAP` (for example `2g`) on a service to change it.

**Updates:** the template builds from `aalfath/dolphinscheduler-railway-template`. When a new DolphinScheduler release is pinned there, Railway offers you the update; on start the services run DolphinScheduler's own schema upgrade before serving. Read the [changelog](https://github.com/aalfath/dolphinscheduler-railway-template/blob/main/CHANGELOG.md) before you apply an update.

**Railway specifics:** Railway's private network is IPv6. DolphinScheduler 3.4.3 writes IPv6 addresses in two different forms, which stops the master from ever claiming its scheduling slot, so the start scripts set the advertised address of the master, worker and alert server explicitly.

## Why Deploy Apache DolphinScheduler on Railway?

<!-- Recommended: Keep this section as shown below -->
Railway is a singular platform to deploy your infrastructure stack. Railway will host your infrastructure so you don't have to deal with configuration, while allowing you to vertically and horizontally scale it.

By deploying Apache DolphinScheduler on Railway, you are one step closer to supporting a complete full-stack application with minimal burden. Host your servers, databases, AI agents, and more on Railway.
<!-- End recommended section -->
