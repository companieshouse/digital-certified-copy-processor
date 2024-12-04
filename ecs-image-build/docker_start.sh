#!/bin/bash
#
# Start script for digital-certified-copy-processor
#
PORT=8080

exec java -jar -Dserver.port="${PORT}" "digital-certified-copy-processor.jar"
