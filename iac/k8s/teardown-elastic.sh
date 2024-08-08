oc delete -n control-bus -f elastic/apm.yaml
oc delete -n control-bus -f elastic/kibana.yaml
oc delete -n control-bus -f elastic/elastic.yaml
oc delete -n control-bus all --all --force
