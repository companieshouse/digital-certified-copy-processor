#!/bin/bash
#
# Start script for digital-certified-copy-processor
#
PORT=8080

exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "digital-certified-copy-processor.jar"
