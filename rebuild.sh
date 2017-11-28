#!/usr/bin/env bash
JENKINS_URL="http://`kubectl get ingress jenkins  -ojsonpath='{.spec.rules[0].host}'`"
JOB_NAME="test-maven-flow-project-pod-template-byname"

echo "Using Jenkins ${JENKINS_URL}"

mvn -Dtest=false -DfailIfNoTests=false clean install $*

POD=$(kubectl get pod -l app=fabric8-jenkins -ojsonpath="{.items[*].metadata.name}")

echo "Found Jenkins pod ${POD}"

if [ -z  "$POD" ]; then 
    echo "No Jenkins pod found!"
    exit 1
fi

kubectl cp target/fabric8-pipelines.hpi $POD:/var/jenkins_home/plugins/fabric8-pipelines.jbi
kubectl exec $POD -- rm -rf /var/jenkins_home/plugins/fabric8-pipelines
kubectl delete pod $POD

sleep 1

echo "waiting for the jenkins pod to restart"

(kubectl get pod -l app=fabric8-jenkins -w & ) | grep -q  '1/1       Running'

echo "new jenkins pod running now!"

BUILD_URL="${JENKINS_URL}/job/${JOB_NAME}/build"

echo "Now triggering job ${BUILD_URL} as user ${JENKINS_USER}"

SC="404"
while [ "$SC" == "404" ]
do
  SC=`curl -X POST ${BUILD_URL} --user ${JENKINS_USER}:${JENKINS_PASSWORD}  -o /dev/null`
  echo "got result ${SC}"
  sleep 2
done

echo "Build triggered!"




