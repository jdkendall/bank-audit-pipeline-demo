oc create namespace bank-audit

if [ -f applications/apm-secret.yaml ]; then
	oc delete -n bank-audit -f applications/apm-secret.yaml
fi

DECODED_SECRET=$(oc -n control-bus get secret apm-server-apm-token -o go-template='{{index .data "secret-token" | base64decode}}')
oc -n bank-audit create secret generic apm-server-apm-token --from-literal=secret-token="$DECODED_SECRET" --dry-run=client -o yaml >applications/apm-secret.yaml
oc apply -n bank-audit -f applications/apm-secret.yaml
oc apply -n bank-audit -f applications/configmap.yaml
oc apply -n bank-audit -f applications/postgres.yaml
oc apply -n bank-audit -f applications/rabbitmq.yaml
