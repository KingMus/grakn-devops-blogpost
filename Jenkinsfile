pipeline {
	agent {
		dockerfile {
		filename 'Dockerfile'
		args '-u root:root'
		}
	      }
	stages{
		stage('Maven build') {
			steps {
				sh '''
					cd simple-graph
					mvn -T 4 clean install
				'''
        			}
			}
      stage('Grakn start') {
  			steps {
  				sh '''
  					/opt/grakn/grakn-core-1.4.2/grakn server start
  				'''
          			}
  			}

	stage('Java Run') {
  			steps {
  				sh '''
  					cd /usr/share/simple-graph/target
					echo $PWD
					ls
					cd ..
					java -cp target/simple-graph-0.0.1-SNAPSHOT.jar grakndevops.GraphCreator
  				'''
          			}
  			}
		
		}
	
	}


