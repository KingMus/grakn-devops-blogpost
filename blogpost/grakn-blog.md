Todo:

* another text review (grammar, duplications, ...)
* draw some pics
    * traditional pipeline
    * maybe some architecture-pics 

<hr>

# Introducing Grakn and DevOps

This blog post is about some DevOps principles and how to transfer them to Grakn.AI projects. More precisely, this means creating a Jenkins pipeline which will build a Grakn image for Docker and run our Java code inside. A regular review of the steps made is also done. The result will be an adaptable solution for most projects.

The complete code can be found on the [GitHub-Repo](https://github.com/KingMus/grakn-devops-blogpost) of this blog post.

### Introduction

Writing working code isn't enough today. Well, maybe it was, a few years ago, where you just made your program work on your computer, give it to your it-operator afterward and say _"well, works on my machine"_. But since DevOps came up, you as a developer are able to make the situation a lot better for both sides. Even if just as a reminder, let us take a short look at the main idea behind DevOps and which advantages can be gained.

### The traditional way of deployment

For the start, let us think about the "normal" way of creating a software solution:

1) developer(-team) --> (produces code)
2) test division --> (tests the code)
3) security division --> (checks for the security)
4) package & release --> (here it will become a deployed product)
5) monitoring & maintenance --> (after-work)

Looking at this process itself doesn't show a clear reason why you should change something. This "pipeline" is useful and makes sense. The main problem behind it is how it behaves when applied in a company: normally there are many divisions which all have one responsibility in this pipeline and needs to work together. It's is normal that in this process technical and cultural aspects hinder you from a fast and simple go-through. Instead of releasing small increments, most companies used to have a large update for the new version.

### What makes DevOps different?

When following DevOps, there is the developer itself (better: a team) which is in the responsibility for all the steps in this process. Every single task depends on the code the developer produces, so we should give him that responsibility too.

Of course, it isn't that easy. You can't just say that now the work of different teams is combined in one team without taking a critical look at the process and the way of doing things itself. Too big would be the work overload they had to do. Soon it became clear that more automation is needed. The attempt of solving this (no matter if inside or outside the DevOps-Movement) lead to the birth of tools like Docker and Jenkins. 

DevOps isn't a real method or project structure, instead, most people describe it more like a philosophy or a cultural way to work together. But what is for sure are the advantages. The most important ones are faster time-to-market, improved collaboration, and higher stability.

### Build our Grakn.AI project

Okay, let us get to the point of this post: Trying to combine our Grakn.AI project with some ideas from DevOps. Grakn.AI is an open source tool which can be used to represent data as a "knowledge graph". Its origin is found in the Semantic Web technology. With its own query language, Graql, you are able to structure your data in knowledge models and visualize them afterward. For more information, visit the [documentation](https://dev.grakn.ai/docs/index) or read some other posts on this blog (maybe [this](https://blog.grakn.ai/loading-data-into-a-grakn-knowledge-graph-using-the-java-client-5f2f1a7f9903) one).

First of all, let us make Grakn run in a normal project setup. We install Grakn.AI on our machine or server and start it there with `/.grakn server start`. Now we can use the qraql shell to define our schema of the graph and insert some data. As an alternative, we can use the [Java API](https://dev.grakn.ai/docs/java-library/setup) of Grakn to do this. In the following, I will use the Java API.

My example project for this setup is the code used in the explanation of the [Core API Documentation](https://dev.grakn.ai/docs/java-library/core-api). First, connect to Grakn. If it runs on your own machine, you just define the destination inside the code like this:

```
Grakn grakn = new Grakn(new SimpleURI("localhost:48555"));
Grakn.Session session = grakn.session(Keyspace.of("grakndevops"));
Grakn.Transaction tx = session.transaction(GraknTxType.WRITE);
```

Then, we use the correct syntax to create our shema and fill it with some values:

```
AttributeType firstname = tx.putAttributeType("firstname", AttributeType.DataType.STRING);
AttributeType surname = tx.putAttributeType("surname", AttributeType.DataType.STRING);

Role spouse1 = tx.putRole("spouse1");
Role spouse2 = tx.putRole("spouse2");
RelationshipType marriage = tx.putRelationshipType("marriage").relates(spouse1).relates(spouse2);

EntityType person = tx.putEntityType("person").plays(spouse1).plays(spouse2);

person.has(firstname);
person.has(surname);

        // Load data

Attribute johnName = firstname.create("John"); // Create the attribute
Attribute maryName = firstname.create("Mary");

Entity john = person.create().has(johnName); // Link it to an entity
Entity mary = person.create().has(maryName);

Relationship theMarriage = marriage.create().assign(spouse1, john).assign(spouse2, mary);

tx.commit();
```

Now we can run the code. When everything works we should be able to access the graph on the dashboard. Access it with `localhost:4567`. The [Grakn Visualiser](https://dev.grakn.ai/docs/visualisation-dashboard/visualiser) documentation contains more information about how to use the dashboard properly. The following output is a possible visualization:

![grakn](https://github.com/KingMus/grakn-devops-blogpost/blob/master/blogpost/blog-src/grakn_result.png)

### Using a Version Control System

So far so good, but: The written code for the project is only on our local machine, which is bad for collaboration. We should change this with a VCS tool like "git". We create a new repository and commit and push our code there. In this setup, I'll use just GitHub. I recommend the [GitHub Help](https://help.github.com/) page and their [Learning Lab](https://lab.github.com/courses) when you are new to Git(-Hub).

### "Dockerize" our Grakn

Sharing our code is easy now, but we aren't able to do this with our environment yet. At this point, Docker is really helpful. Docker gives us the possibility to create an environment with software and dependencies we specified and wrap it up all together in one single place, having all the things it needs to work. This means code, libraries, and other stuff. This place is called an image and can be used to create a container which will run and behave the same, independent from the deployment place. For installation and further information about Docker check out the official [Docker documentation](https://docs.docker.com/get-started/). 

Installing Docker on your local machine would work, but if we want to achieve the advantage that our container is accessible for other people we should consider installing it on an accessible machine (like a server).

After installing we should create our container. Grakn.AI delivers a ready-to-use Docker Image which can be pulled from their [Docker Hub site](https://hub.docker.com/r/graknlabs/grakn). But: according to this [issue](https://github.com/graknlabs/grakn/issues/2898) there isn't a support for accessing the container with the Java API yet. Building an own image seems to solve this issue. We can use the code from [BFergerson](https://github.com/BFergerson/grakn-docker-toolbox), who already did this before us (thank you). Run the following code on the system where your docker is installed to build and run the container:

```
git clone https://github.com/BFergerson/grakn-docker-toolbox.git
cd grakn-docker-toolbox/
docker build -t grakn .
docker run -p 4567:4567 -p 9160:9160 -p 48555:48555 grakn
```

If necessary, change the URL inside the Grakn java code to the place where docker is installed and run the code to verify that everything works.

### Short interim conclusion

We achieved the following things:

* +our code is stored in a VCS, making it way more safe and shareable
* +we use Grakn.ai in a container, which allows us to run our code without the necessity to install an environment locally

But:

* -when deploying a new code or image version, there are many manual things to do
* -the initial installation effort is higher

### Use Jenkins to automate the build process

Since we cannot change the initial installation effort, we will try to automate our steps instead. Jenkins can help us with this. Jenkins calls itself a self-contained automation server which gives us the possibility to create our own automated process of building, testing and deploying your software. Just like with Docker, I recommend reading the [Jenkins documentation](https://jenkins.io/doc/).

We need to install Jenkins, best where also Docker is installed. This will avoid many possible errors. After installation, we can set up a Jenkins routine which uses the repository as an entry point. Add a new entry and select "pipeline". In the settings, make sure to use the "pipeline script from scm"-option and link the repository there. After the setup, we are able to run the pipeline (when selected, you can build automatically when you push a new version of code into the repository).

The steps in the pipeline are defined by us when we write a so-called Jenkinsfile. The [pipeline documentation](https://jenkins.io/doc/book/pipeline/getting-started/) and the following sites explain how to do this. For us, this means we need to know about the steps in our process to "translate" our Jenkinsfile out of them. The steps could look like this:

1) build the image
2) create the container with a running Grakn instance
3) compile and run our project source code

To build our image without having to use commands in the terminal, docker gives us the possibility to write a Dockerfile. BFergerson also used this to create the custom Grakn image. Inside the Dockerfile we define a base image if wanted and then use instructions to adapt it to our requirements.

Luckily Jenkins can interpret and run it. It just needs to know the location of the installed docker. Normally you have to "introduce" Jenkins and Docker to each other to make this work. Depending on your setup, this process differs in the effort. 
In my case, adding Jenkins to the docker group with `sudo usermod -aG docker Jenkins` solved the errors.

We will look at the given Dockerfile and customize it a bit:

```

# Grakn Dockerfile from https://github.com/bfergerson/grakn-docker-toolbox

#use a base image (this is really helpful)
FROM maven:3.5-jdk-8

#keep the credit
LABEL maintainer="github.com/bfergerson"

ARG GRAKN_VERSION=1.4.2

ENV GRAKN_HOME=/opt/grakn

#install grakn with the given arguments
RUN mkdir -p $GRAKN_HOME && \
    wget https://github.com/graknlabs/grakn/releases/download/v${GRAKN_VERSION}/grakn-core-${GRAKN_VERSION}.zip && \
    unzip grakn-core-${GRAKN_VERSION}.zip -d $GRAKN_HOME && cp -Rf $GRAKN_HOME/grakn-core-${GRAKN_VERSION}/* $GRAKN_HOME

ENV PATH=$PATH:$GRAKN_HOME
WORKDIR $GRAKN_HOME

COPY cassandra.yaml $GRAKN_HOME/services/cassandra

COPY simple-graph /usr/share/simple-graph

# Expose the ports
# Grakn Server
EXPOSE 4567
# Thrift client API
EXPOSE 9160
# Grakn gRPC
EXPOSE 48555
```

Now we tell Jenkins to use this Dockerfile inside of the Jenkinsfile:

```
agent {
    dockerfile {
        filename 'Dockerfile'
        args '-u root:root'
    }
}
```

The next step in our process is to start our container and have a running Grakn server inside. Jenkins runs the container after executing the Dockerfiles so we need to tell Jenkins to run the `grakn server start` command inside. This can be realized through a "stage" inside the Jenkinsfile:

```
stage('Grakn start') {
    steps {
          sh '''
              /opt/grakn/grakn-core-1.4.2/grakn server start
          '''
        }
}
```

For the last step, we need to define another stage where we run our code. We should use Maven for this because our Java project has many dependencies. Our stage (after looking up the commands for packaging and running the Maven project) is defined like this:

```
stage('Maven build and Run') {
      steps {
          sh '''
            cd /usr/share/simple-graph
            mvn -T 4 clean install
            java -cp target/simple-graph-0.0.1-SNAPSHOT-jar-with-dependencies.jar grakndevops.GraphCreator
              '''
          }
}
```

Now we just need to combine these parts (the final Jenkinsfile can be found in the code repository of this blog post). Now let us build our defined pipeline inside the Jenkins UI. Depending on the system and the way you installed Docker and Jenkins, several errors could occur, but thanks to the large community, most of them are easy to solve.

The build was green? Perfect, because this means we created our pipeline successfully. The output should be an image inside your docker environment which you can share or use to create a container. 

![jenkins](https://github.com/KingMus/grakn-devops-blogpost/blob/master/blogpost/blog-src/pipeline_jenkins.png)

### Conclusion

Let us look again at the actual situation:

* +our code is stored in a VCS and we use grakn.ai in a container
* +when committing code, our new version will be deployed almost automatically

But:

* -the initial installation effort is even higher
* -building the pipeline costs time

As always, you should think about the usage of this procedure in your own context. If you just want to try out a new graph I wouldn't recommend setting up the whole pipeline. But if you want to collaborate as a team over a long term it is worth a try. 

To be fair, this isn't an end. Just a good point to stop. If you want to delve deeper than this, you could try out [Watchtower](https://github.com/v2tec/watchtower) for automated image updating of containers. Also, [Portainer](https://www.portainer.io/) is a cool project providing an easy-to-use UI for Docker.

Send me your feedback, criticism or suggestions. I'm learning every day, too :D. Thank you for reading.

<hr>

sources:
* https://www.docker.com/get-started
* https://jenkins.io/doc/
* https://grakn.ai/
