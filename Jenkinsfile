stage('Continuous Integration') {
    node {
        checkout scm
        mvn '-P release clean deploy'
    }
}
milestone 10
input message: 'Release ?', ok: 'Release', submitter: 'ymenager'
milestone 20
stage('Release') {
    node {
        checkout scm
        def pom = readMavenPom file: 'pom.xml'
        def version = pom.version.replace("-SNAPSHOT", "")
        echo "Releasing version ${version}"
        mvn 'release:update-versions -DdevelopmentVersion=${version}'
        mvn 'clean deploy'
    }
    milestone 30
}

def mvn(args) {
    withMaven( maven: 'maven', mavenSettingsConfig: 'e37672cf-602b-476f-8ec4-da37669113e6') {
        sh "mvn -Dmaven.test.failure.ignore -U ${args}"
    }
    junit '**/target/surefire-reports/TEST-*.xml'
    archive '**/target/*.jar'
}
