#!/bin/bash
# Create the data directories on the volume, then hand over to the official entrypoint.
set -e
mkdir -p "$ZOO_DATA_DIR" "$ZOO_DATA_LOG_DIR"
exec /docker-entrypoint.sh "$@"
