PASS=$(oc -n control-bus get secret elastic-es-elastic-user -o go-template='{{.data.elastic | base64decode}}')
echo "User: elastic"
echo "Pass: $PASS"
echo

TOKEN=$(oc -n control-bus get secret apm-server-apm-token -o go-template='{{index .data "secret-token" | base64decode}}')
echo "APM Token: $TOKEN"
