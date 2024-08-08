# Kubernetes / OpenShift setup
This provides a quick guide to a local development cluster with Elastic as well as the application. As with other parts of this demo repository, these examples are for demonstration purposes only and will prefer simplicity and defaults over security. Do not use this setup in any production-facing or security-sensitive environment.

## Prerequisites
**Requires either OpenShift or Kubernetes set up locally.** To set up OpenShift for local development (recommended), CRC is an easy way to get started: https://crc.dev/

## Setting up Elastic on K8s/OpenShift
### ECK
The Elastic stack uses Elastic Cloud for Kubernetes (ECK) to set up the Elastic stack within Kubernetes. This is considerably easier and more effective than attempting the equivalent via Docker or Podman with compose files.

The guide to add ECK can be found here: https://www.elastic.co/guide/en/cloud-on-k8s/current/k8s-deploy-eck.html 

If using CRC, login as the `kubeadmin` account via `oc login` prior to following the above guide, and substitute the `oc` command for the `kubectl` command where present.

### Deploying Elastic to the cluster
Apply the provided cluster templates in [elastic/](elastic/) to set up the Elastic stack via `oc` or `kubectl` (if using CRC, log in as `kubeadmin` account first):

```bash
oc apply -n elastic -f elastic/elastic.yaml
oc apply -n elastic -f elastic/kibana.yaml
```

### Administration
For administrative activities on Elastic and Kibana, the admin account `elastic` can be used with the password extracted from the cluster's secrets once deployed:

```bash
oc get secret "monitoring-es-elastic-user" -o go-template='{{.data.elastic | base64decode }}'
```

No adjustments will be made to the default settings for purposes of the demo.

## Setting up the Bank Audit Pipeline Demo on K8s/OpenShift
*TODO*

## Template Sources
### Elastic
OpenShift / K8s YAML files are based on those provided by the https://elastic.co/ quick-start guides, modified as necessary to fit into the demo.