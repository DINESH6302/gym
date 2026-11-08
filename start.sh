#!/bin/bash
# Start openGym backend + frontend

trap 'kill 0' EXIT

echo "Starting backend on :3000..."
cd backend && no_proxy='*' NO_PROXY='*' http_proxy='' https_proxy='' mvn spring-boot:run &

echo "Starting frontend on :5173..."
cd frontend && ~/.local/bin/bun run vite &

wait
