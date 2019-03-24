#!/bin/bash
echo -n "username:" 
read username
# Read Password
echo -n "password:" 
read -s password
echo 
#echo "u: ${username} p: ${password}"
kubectl create secret generic ilo-credentials -n monitoring --from-literal=username="${username}" --from-literal=password="${password}"