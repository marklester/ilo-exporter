# ilo-exporter
A Prometheus Exporter used to export HP ILO metrics 
This uses the rest api provided by the ILO on HP servers to expose metrics the node exporter does not.
This includes:
 * Power Reading
 * Fan speeds in percentages
 * all temperatures monitored by ilo.
 
 Example Usage:
![ilo-metrics](https://user-images.githubusercontent.com/718117/54949540-b587e380-4f15-11e9-8725-2062f491e5b4.jpg)

 The exporter can be run in several ways:
 
 standalone:
 
 TODO: run from jar command
 
 docker:
 
 TODO: docker command
 
 kubernetes:
 
``` 
./create-secret.sh
kubectl apply -f 	iloexporter-deployment.yaml 
```

The exporter is configured via environment variables:


| variable | description |
| ---------- | ------------- |
|ilo.username| username to talk to ilo|
|ilo.password| password used to login into ilo|
|ilo.hosts|comma separated list of ilo hosts. Supports sequential expansion for ipv4 addressess e.g. 192.168.1.1-3|
|ilo.port| port to listen on for metrics http server. defaults to 9416|
|ilo.cache.refresh|how often the ilo cache should be refreshed. default is (PT30S)30 seconds defined in iso8601 duration|

