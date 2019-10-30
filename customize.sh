#!/usr/bin/env bash

# Run "customize" in Docker container
docker run --rm -v "$PWD:/src" kayosportsau/ubuntu-devops:1.0.2 -c "customize.sh"