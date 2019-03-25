# ilo-exporter
This is HP Ilo Exporter for promethesus
This uses the rest api provided by HP server with ilo to expose metrics the node exporter does not have.
This includes:
 * Power Reading
 * Fan speeds in percentages
 * all temperatures monitored by ilo.
 The exported can be run in several ways:
 
 standalone:
 
 TODO: run from jar command
 
 docker:
 
 TODO: docker command
 
 kubernetes:
 
``` kubectl apply -f 	iloexporter-deployment.yaml ```

The exporter is configured via environment variables:


| variable | description |
| ---------- | ------------- |
|ilo.username| username to talk to ilo|
|ilo.password| password used to login into ilo|
|ilo.hosts|comma separated list of ilo hosts|
|ilo.cache.refresh|how often the ilo cache should be refreshed. default is (PT30S)30 seconds defined in iso8601 duration|

Example Usage:
![ilo-metrics](https://user-images.githubusercontent.com/718117/54949540-b587e380-4f15-11e9-8725-2062f491e5b4.jpg)
