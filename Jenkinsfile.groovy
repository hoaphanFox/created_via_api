pipeline {
  agent {
    label "streamotion-maven"
  }
  environment {
//    ORG = 'customize!project.org'
    ORG = 'fsa-streamotion' // Please do not edit this line! Managed by customize.sh
//    APP_NAME = 'customize!project.name'
      APP_NAME = 'bedrock-streamotion-webflux-k8s' // Please do not edit this line! Managed by customize.sh

  }

  stages {

    stage('Compile & Test') {
      when {
        branch 'feature/*'
      }
      environment {
//        PREVIEW_VERSION = get_previewVersion(APP_NAME, BRANCH_NAME, BUILD_NUMBER)
        PREVIEW_NAMESPACE = get_previewNameSpace(APP_NAME, BRANCH_NAME, BUILD_NUMBER)
        PREVIEW_VERSION = "0.0.0-SNAPSHOT-$PREVIEW_NAMESPACE-$BUILD_NUMBER"

        HELM_RELEASE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
      }
      steps {
//        PREVIEW_VERSION = previewNames("1", BRANCH_NAME, BUILD_NUMBER)["previewName"]
        container('maven') {
          sh "echo **************** PREVIEW_VERSION: $PREVIEW_VERSION , PREVIEW_NAMESPACE: $PREVIEW_NAMESPACE, HELM_RELEASE: $HELM_RELEASE"
//          previewNames("3",BRANCH_NAME, BUILD_NUMBER)

          sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
          sh "echo $PREVIEW_VERSION > PREVIEW_VERSION"
          sh "mvn clean test"
        }
      }
    }

    stage('CI Build + PREVIEW') {
      when {
        branch 'PR-*'
      }
      environment {
//        PREVIEW_VERSION = get_previewVersion(APP_NAME, BRANCH_NAME, BUILD_NUMBER)
        PREVIEW_NAMESPACE = get_previewNameSpace(APP_NAME, BRANCH_NAME, BUILD_NUMBER)
        PREVIEW_VERSION = "0.0.0-SNAPSHOT-$PREVIEW_NAMESPACE-$BUILD_NUMBER"

        HELM_RELEASE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
      }
      steps {
//        PREVIEW_VERSION = previewNames("1", BRANCH_NAME, BUILD_NUMBER)["previewName"]
        container('maven') {
          sh "echo **************** PREVIEW_VERSION: $PREVIEW_VERSION , PREVIEW_NAMESPACE: $PREVIEW_NAMESPACE, HELM_RELEASE: $HELM_RELEASE"
//          previewNames("3",BRANCH_NAME, BUILD_NUMBER)

          sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
          sh "echo $PREVIEW_VERSION > PREVIEW_VERSION"
          sh "mvn install"
          sh "skaffold version"
          sh "rm -rf mvnw"
          sh "export VERSION=$PREVIEW_VERSION && skaffold build -f skaffold.yaml"
          sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$PREVIEW_VERSION"

          script {
            def buildVersion =  readFile "${env.WORKSPACE}/PREVIEW_VERSION"
            currentBuild.description = "$APP_NAME.$PREVIEW_NAMESPACE"
          }


          dir('charts/preview') {
            sh "make preview"
            sh "jx preview --app $APP_NAME --namespace=$PREVIEW_NAMESPACE --dir ../.."
          }


        }
      }
    }

    stage('Component Test') {
      when {
        branch 'PR-*'
      }

      environment {
        def previewNamespace = get_previewNameSpace(APP_NAME, BRANCH_NAME, BUILD_NUMBER).toLowerCase()
        PREVIEW_URL = "http://${APP_NAME}.${previewNamespace}.svc.cluster.local"
      }

      steps {
        container('maven') {
          waitForPreviewEnvironment PREVIEW_URL
          echo 'Running component tests...'
          sh "mvn clean verify -Dtest.skip.unit=true -Dtest.skip.component=false -Dtest.targetUrl=$PREVIEW_URL"
          echo 'Finished running component tests.'
        }
      }
    }

    stage('Build Release') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {

          // ensure we're not on a detached head
          sh "git checkout master"
          sh "git config --global credential.helper store"
          sh "jx step git credentials"

          // so we can retrieve the version in later steps
          sh "echo \$(jx-release-version) > VERSION"
          sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
          sh "jx step tag --version \$(cat VERSION)"
          sh "mvn clean deploy"
          sh "rm -rf mvnw"
          sh "skaffold version"
          sh "export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml"

          script {
            def buildVersion =  readFile "${env.WORKSPACE}/VERSION"
            currentBuild.description = "$buildVersion"
            currentBuild.displayName = "$buildVersion"
          }

          sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:\$(cat VERSION)"
        }
      }
    }
    stage('Promote to Environments') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          sh "mv charts/helm-release  charts/$APP_NAME"
          dir("charts/$APP_NAME") {
            sh "jx step changelog --generate-yaml=false --version v\$(cat ../../VERSION)"

            // release the helm chart
            // sh "jx step helm release"
            // sh "ls -la"
            sh "make release"
            // promote through all 'Auto' promotion Environments
            sh "jx promote -b --no-poll=true  --helm-repo-url=$CHART_REPOSITORY --no-poll=true --no-merge=true --no-wait=true --env=staging --version \$(cat ../../VERSION)"

            sh "jx promote -b --no-poll=true --helm-repo-url=$CHART_REPOSITORY --no-poll=true --no-merge=true --no-wait=true --env=production --version \$(cat ../../VERSION)"

          }
        }
      }
    }
  }
  post {
        always {
          cleanWs()
        }
  }
}

def previewNames(String loc, String branchName, String buildNumber) {

  println " $loc *****************************   br: $branchName , bn: $buildNumber"

  return [namespace: branchName, previewName: "$branchName+$buildNumber"]
}

def get_previewVersion(String appName, String branchName, String buildNumber) {
    return appName + "-" + buildNumber
}

/*
* branchName:     feature/MCDP-123-fix-issue
 */
def get_previewNameSpace(String appName, String branchName, String buildNumber) {

//
//    if(!branchName.startsWith("feature")) {
//      throw new Exception("BranchName Must Start with feature")
//    }

    def feature = branchName =~ /feature\/([A-Za-z0-9]+\-\d+).*/

    def pr = branchName =~ /(PR-.*)/


  if (feature) {
      println "$branchName matched BranchNaming pattern"
      def jiraNumber = feature[0][1]

      def namespace = appName + "-" + jiraNumber
      println "going to use namespace name for preview: $namespace"

      return namespace

    } else if(pr) {
      println "$branchName matched BranchNaming pattern"
      def jiraNumber = pr[0][1]

      def namespace = appName + "-" + jiraNumber
      println "going to use namespace name for preview: $namespace"

      return namespace
    }
    else {
      println "REJECTING BRANCHNAME THAT DOESN'T MATCH PATTERN"
      throw new RuntimeException("feature branch name does not feature branch naming convention of '(PR)-.*' or 'feature/([A-Za-z0-9]+\\-\\d+).*'  ")
    }
}

void waitForPreviewEnvironment(String url) {
  echo 'Waiting for preview environment to be up...'

  timeout(time: 3, unit: 'MINUTES') {
    waitUntil {
      script {
        try {
          echo "Checking if $url is up..."
          def checkCommand = """wget -qO- $url/actuator/health | grep -q '{"status":"UP"' && echo \$?"""
          def commandOutput = sh script: checkCommand, returnStdout: true
          echo "Response from command: $commandOutput"
          return '0' == commandOutput?.toString()?.trim()
        } catch (Exception e) {
          echo "Check failed due to: $e"
          return false
        }
      }
    }
  }

  echo 'Preview environment is up.'
}

