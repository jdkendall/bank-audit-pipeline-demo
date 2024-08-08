oc create namespace control-bus
oc apply -n control-bus -f elastic/elastic.yaml
oc apply -n control-bus -f elastic/kibana.yaml
oc apply -n control-bus -f elastic/apm.yaml
