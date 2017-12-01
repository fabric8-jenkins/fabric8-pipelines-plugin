pipeline {
  agent {
    kubernetes {
      label "fabric8-maven"
      podTemplateName "fabric8-maven"
    }
  }
  stages {
    stage('Maven Release') {
      steps {
        mavenFlow(
                pauseOnSuccess: "true",
                pauseOnFailure: "true",

                cdOrganisation: "fabric8-jenkins",
                useStaging: true,
                useSonatype: true
        )
      }
    }
  }
}