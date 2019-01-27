# Introducing Grakn.AI and DevOps

![titlepic](https://github.com/KingMus/grakn-devops-blogpost/blob/master/blogpost/blog-src/titlepicforpost.jpeg)

This blog post is about some DevOps principles and how to transfer them to Grakn.AI projects. More precisely, this means creating a Jenkins pipeline which will build a Grakn image for Docker and run our Java code inside. This post will evaluate the steps to achieve this goal critically and objectively and has an adaptable procedure for many projects as a result.

There will be some coding later, so if you want to follow while reading, you can find the complete code of this blog post in this [GitHub-Repo](https://github.com/KingMus/grakn-devops-blogpost).

### Introduction

Writing working code isn't enough today. Well, maybe it was, a few years ago, where you just made your program work on your computer, give it to your IT-operator afterward and say "_well, it works on my machine_". In large companies there even are many divisions which have different responsibilities in the deployment process. It's is normal that in this process the technical and cultural aspects hinder you from a fast and simple go-through.

When following the philosophy behind DevOps, you as a developer are able to change this situation in a better way for both sides. Even if just as a reminder, let us take a short look at the things DevOps stands for.

### The idea behind DevOps

Normally, the thing DevOps wants to achieve is that the developer itself (better: a team) is in the responsibility for all the steps necessary for deployment. Of course, you can't just say that now the work of different teams is combined in one team without taking a critical look at the process and the way of doing things itself. There would be too much work and they would be overloaded. Soon it became clear that more automation is needed. The attempt of solving this (no matter if inside or outside the DevOps-Movement) led to the birth of tools like Docker and Jenkins.

It is important to remember that DevOps isn't a real method or project structure, instead, most people describe it more to a philosophy or a cultural way to work together. When applying the principles you are able to change things to fit better in your situation. This helps to get the best out of the advantages like a faster time-to-market, improved collaboration or higher stability.

### Grakn and Graql

It would be interesting to know if we could also achieve some of these advantages with a Grakn project. To do so, we first need to look at Grakn itself.

GRAKN.AI is an open source tool which can be used to represent data as a "knowledge graph", with ideas similar to Semantic Web technologies. With its own query language, Graql, you are able to structure your data in knowledge models and visualize them afterward. For more information, visit the [documentation](https://dev.grakn.ai/docs/general/quickstart) or read some other posts on this blog (maybe [this](https://blog.grakn.ai/loading-data-into-a-grakn-knowledge-graph-using-the-java-client-5f2f1a7f9903) one).

The next step is to build a simple Grakn project and take a closer look at it.

### Build our Grakn project

First of all, we install Grakn on our machine or server and start it with `/.grakn server start`. Now we can use the qraql shell to define our schema of the graph and insert some data. As an alternative, we can use Grakn's [Java API](https://dev.grakn.ai/docs/java-library/setup) to do this. In the following, I will use the Java API. The setup we want to achieve looks like this:

![arc1](https://github.com/KingMus/grakn-devops-blogpost/blob/master/blogpost/blog-src/arc1.png)

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

So what did we achieve? Sharing our code is easy now, but we aren't able to do this with our environment yet. At this point, Docker is really helpful.

### "Dockerize" our Grakn

Docker gives us the possibility to create an environment with software and dependencies we specified and wrap it up all together in one single place, having all the things it needs to work. This means code, libraries, and other stuff. This place is called an image and can be used to create a container which will run and behave the same, independent from the deployment place. For installation and further information about Docker check out the official [Docker documentation](https://docs.docker.com/get-started/).

When installing Docker, we should think about the following things: Installing it on our local machine would work, but if we want to achieve the advantage that our container is accessible for other people we should consider installing it on an accessible machine (like a server).

After installing we should create our container. Grakn delivers a ready-to-use Docker Image which can be pulled from their [Docker Hub site](https://hub.docker.com/r/graknlabs/grakn). But: according to this [issue](https://github.com/graknlabs/grakn/issues/2898) there isn't a support for accessing the container with the Java API yet. Building our own image seems to solve this issue. We can use the code from [BFergerson](https://github.com/BFergerson/grakn-docker-toolbox), who already did this before us (thank you). Run the following code on the system where your docker is installed to build and run the container:

```
git clone https://github.com/BFergerson/grakn-docker-toolbox.git
cd grakn-docker-toolbox/
docker build -t grakn .
docker run -p 4567:4567 -p 9160:9160 -p 48555:48555 grakn
```

If necessary, change the URL inside the Java code to the destination where docker is installed and run the code to verify that everything works. If so, we can take a moment to look at our actual situation.

### Short interim conclusion

We achieved the following things:

* our code is stored in a VCS, making it way more safe and shareable
* we use Grakn in a container, which allows us to run our code without the necessity to install an environment locally

But:

* when deploying a new code or image version, there are many manual things to do
* the initial installation effort is higher

Our architecture is a bit more complex now:

![arc2](https://github.com/KingMus/grakn-devops-blogpost/blob/master/blogpost/blog-src/arc2.png)

This will become even more complex when using it in a larger project. We don't want to work with this the hard way. Since we cannot change the initial installation effort, we will try to automate our steps instead. Jenkins can help us with this.

### Use Jenkins to automate the build process

Jenkins calls itself a self-contained automation server which gives us the possibility to create our own automated process of building, testing and deploying your software. Just like with Docker, I recommend reading the [Jenkins documentation](https://jenkins.io/doc/).

We need to install Jenkins, best also where Docker is installed. This will avoid many possible errors. After installation, we can set up a Jenkins routine which uses the repository as an entry point. Add a new entry and select "pipeline". In the settings, make sure to use the "pipeline script from scm"-option and link the repository there. After the setup, we are able to run the pipeline (when selected, you can build automatically when you push a new version of code into the repository).

The steps in the pipeline are defined by us when we write a so-called Jenkinsfile. The [pipeline documentation](https://jenkins.io/doc/book/pipeline/getting-started/) and the following sites explain how to do this. For us, this means we need to know about the steps in our process to "translate" our Jenkinsfile out of them. The steps could look like this:

1) build the image
2) create the container with a running Grakn instance
3) compile and run our project source code

To build our image without having to use commands in the terminal, docker gives us the possibility to write a Dockerfile. BFergerson also used this to create the custom Grakn image. Inside the Dockerfile we define a base image if wanted and then use instructions to adapt it to our requirements.

Luckily Jenkins can interpret and run it. It just needs to know the location of the installed docker. Normally you have to "introduce" Jenkins and Docker to each other to make this work. Depending on your setup, this process differs in the effort. In my case, adding Jenkins to the docker group with `sudo usermod -aG docker Jenkins` solved the errors.

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

Now we just need to combine these parts and we are able to build our defined pipeline inside the Jenkins UI. Depending on the system and the way you installed Docker and Jenkins, several errors could occur, but thanks to the large community, most of them are easy to solve.

The build was green? Perfect, because this means we created our pipeline successfully. The output should be an image inside your docker environment which you can share or use to create a container.

![jenkins](https://github.com/KingMus/grakn-devops-blogpost/blob/master/blogpost/blog-src/pipeline_jenkins.png)

At this point it is time for another, final evaluation.

### Conclusion

Let us look at the actual situation again. The final project setup of this blog post looks more or less like this:

![arc3](https://github.com/KingMus/grakn-devops-blogpost/blob/master/blogpost/blog-src/arc3.png)

We achieved the following things:

* our code is stored in a VCS and we use Grakn in a container
* when committing code, our new version will be deployed almost automatically

But:

* the initial installation effort is even higher
* building the pipeline costs time

As always, you should think about the usage of this procedure in your own context. If you just want to try out a new graph I wouldn't recommend setting up the whole pipeline. But if you want to collaborate as a team over a long term it is worth a try.

To be fair, this isn't an end. Just a good point to stop. If you want to delve deeper than this, you could try out [Watchtower](https://github.com/v2tec/watchtower) for automated image updating of containers. Also, [Portainer](https://www.portainer.io/) is a cool project providing an easy-to-use UI for Docker.

Send me your feedback, criticism or suggestions. I'm learning every day, too :D. Thank you for reading.
