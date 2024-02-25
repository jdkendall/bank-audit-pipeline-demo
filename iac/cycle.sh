podman compose -f applications.compose.yml down
podman compose -f control-bus.compose.yml down

yes | podman container prune
yes | podman image prune
yes | podman volume prune

podman compose -f control-bus.compose.yml up -d
sleep 5
podman compose -f applications.compose.yml up -d
