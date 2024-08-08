#!/bin/bash
oc -n bank-audit create secret generic regcred --from-file=.dockerconfigjson=/run/user/1000/containers/auth.json --type=kubernetes.io/dockerconfigjson
oc -n control-bus get secret apm-server-apm-http-certs-public -o go-template='{{index .data "ca.crt" | base64decode }}' >temp-ca.crt
oc -n bank-audit create secret generic ca-cert-binding --from-file=temp-ca.crt --from-literal=type=ca-certificates
rm temp-ca.crt
oc create namespace bank-audit
oc apply -n bank-audit -f applications/bankaudit.yaml
