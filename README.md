Experimental attempt to move the [fabric8-pipeline-library](https://github.com/fabric8io/fabric8-pipeline-library) into a Jenkins plugin which can then be used in [a scripted pipeline](https://github.com/jstrachan/test-maven-flow-project/blob/scripted/Jenkinsfile) or in [a declarative pipeline](https://github.com/jstrachan/test-maven-flow-project/blob/master/Jenkinsfile).


## Using in declarative pipelines

```groovy
pipeline {
  agent {
    kubernetes {
      label "fabric8-maven"
    }
  }
  stages {
    stage('Maven Release') {
      steps {
        mavenFlow {
        }
      }
    }
  }
}
```

## Using in scripted pipelines

```groovy
node {
  stage('Maven Release') {
    mavenFlow {
    }
  }
}
```