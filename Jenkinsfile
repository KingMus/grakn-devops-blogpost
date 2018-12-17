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

		}
	}
