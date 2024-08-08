oc delete -n bank-audit -f applications/apm-secret.yaml
rm applications/apm-secret.yaml
oc delete -n bank-audit -f applications/configmap.yaml
oc delete -n bank-audit -f applications/postgres.yaml
oc delete -n bank-audit -f applications/rabbitmq.yaml
