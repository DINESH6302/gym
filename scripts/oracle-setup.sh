#!/usr/bin/env bash
# oracle-setup.sh — One-shot setup for openGym on an Oracle Cloud Free Tier VM (Ubuntu).
# Run as: ssh ubuntu@<IP> 'bash -s' < scripts/oracle-setup.sh
# Or copy it to the VM and run: chmod +x oracle-setup.sh && ./oracle-setup.sh
set -euo pipefail

REPO="https://github.com/DINESH6302/gym.git"
APP_DIR="$HOME/opengym"
DOMAIN=""  # set to your domain if you have one, otherwise uses IP

echo "=== [1/6] Updating system ==="
sudo apt-get update -qq
sudo DEBIAN_FRONTEND=noninteractive apt-get upgrade -y -qq

echo "=== [2/6] Installing Docker ==="
if ! command -v docker &>/dev/null; then
  curl -fsSL https://get.docker.com | sudo sh
  sudo usermod -aG docker "$USER"
  echo "Docker installed. User added to docker group."
else
  echo "Docker already installed."
fi

echo "=== [3/6] Installing Docker Compose plugin ==="
if ! docker compose version &>/dev/null 2>&1; then
  sudo apt-get install -y -qq docker-compose-plugin
fi

echo "=== [4/6] Opening firewall ports (iptables) ==="
# Oracle Ubuntu images use iptables rules that block 80/443 even after
# the cloud security list is opened. These rules let traffic through.
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
# Persist across reboots
sudo sh -c 'iptables-save > /etc/iptables/rules.v4' 2>/dev/null || \
  sudo sh -c 'mkdir -p /etc/iptables && iptables-save > /etc/iptables/rules.v4'

echo "=== [5/6] Cloning repo ==="
if [ -d "$APP_DIR" ]; then
  echo "Directory exists — pulling latest."
  cd "$APP_DIR" && git pull
else
  git clone "$REPO" "$APP_DIR"
  cd "$APP_DIR"
fi

echo "=== [6/6] Starting openGym ==="
# Get the VM's public IP for the env config
PUBLIC_IP=$(curl -s ifconfig.me || curl -s icanhazip.com || echo "localhost")
echo "Public IP: $PUBLIC_IP"

# Write .env
cat > "$APP_DIR/.env" <<ENVEOF
RP_ID=$PUBLIC_IP
ORIGIN=http://$PUBLIC_IP:8080
WEB_PORT=8080
RP_NAME=openGym
ALLOW_GUEST=1
ENVEOF

# Need newgrp to pick up docker group in same session
sg docker -c "docker compose up -d --build"

echo ""
echo "============================================"
echo "  openGym is running!"
echo "  Open: http://$PUBLIC_IP:8080"
echo "============================================"
echo ""
echo "Useful commands:"
echo "  cd $APP_DIR"
echo "  docker compose logs -f     # view logs"
echo "  docker compose down        # stop"
echo "  docker compose up -d       # start"
echo "  docker compose pull        # update images"
