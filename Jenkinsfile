pipeline {
	agent {
		dockerfile {
		filename 'Dockerfile'
		args '-u root:root'
		}
	      }
	stages{
      	stage('Grakn start') {
  			steps {
  				sh '''
  					/opt/grakn/grakn-core-1.4.2/grakn server start
  				'''
          			}
  			}

	stage('Maven build and Run') {
  			steps {
  				sh '''
					cd /usr/share/simple-graph
					mvn -T 4 clean install
  					cd /usr/share/simple-graph/target
					echo $PWD
					ls
					cd ..
					java -cp target/simple-graph-0.0.1-SNAPSHOT-jar-with-dependencies.jar grakndevops.GraphCreator
  				'''
          			}
  			}

		}

	}
