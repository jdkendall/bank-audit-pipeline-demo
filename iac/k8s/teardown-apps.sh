oc delete -n bank-audit secret ca-cert-binding
oc delete -n bank-audit -f applications/bankaudit.yaml
