# ESI2023 - Week 3 - Docker 
The code of Week 3 with Dockerfile and docker-compose configerations.

You can refer to [Docker - course Wiki](https://courses.cs.ut.ee/2023/esi/spring/Main/PS035)


## Docker & Docker compose

** [[Attach:Docker and Docker compose.pdf | Docker & Docker compose presentation]]

* [Docker & Docker compose presentation](https://courses.cs.ut.ee/MTAT.03.229/2023_spring/uploads/Main/Docker%20and%20Docker%20compose.pdf)

## Docker 
* [Docker overview](https://docs.docker.com/get-started/overview/)
* [Use the Docker command line (list of commands](https://docs.docker.com/engine/reference/commandline/cli/)
* [Docker architecture](https://docs.docker.com/get-started/overview/#docker-architecture)

**What is Docker:**  
Docker is an open platform for developing, shipping, and running applications. Docker enables you to separate your applications from your infrastructure so you can deliver software quickly. With Docker, you can manage your infrastructure in the same ways you manage your applications. By taking advantage of Docker’s methodologies for shipping, testing, and deploying code quickly, you can significantly reduce the delay between writing code and running it in production.

**Docker Desktop** is an easy-to-install application for your Mac, Windows or Linux environment that enables you to build and share containerized applications and microservices. Docker Desktop includes the Docker daemon (dockerd), the Docker client (docker), Docker Compose, Docker Content Trust, Kubernetes, and Credential Helper. 

For Installing Docker desktop, see [Docker Desktop](https://docs.docker.com/get-docker/)

**Note:**  not very easy-to-install for on Windows.

**The Docker platform –  a very short description** 

Docker provides the ability to package and run an application in a loosely isolated environment called a '''container'''.

* **Containers** are lightweight and contain everything needed to run the application, so you do not need to rely on what is currently installed on the host. You can easily share containers while you work, and be sure that everyone you share with gets the same container that works in the same way.

* **Images** are read-only templates with instructions for creating a Docker container. Often, an image is based on another image, with some additional customization. 

You might create your own images or you might only use those created by others and published in a registry. To build your own image, you create a Dockerfile with a simple syntax for defining the steps needed to create the image and run it. Each instruction in a Dockerfile creates a layer in the image. When you change the Dockerfile and rebuild the image, only those layers which have changed are rebuilt. This is part of what makes images so lightweight, small, and fast when compared to other virtualization technologies.

 

[1. Creating a Dockerfile for a Spring Boot project](https://courses.cs.ut.ee/2023/esi/spring/Main/PS0351)

[2. Publishing Docker Image To Docker Hub](https://courses.cs.ut.ee/2023/esi/spring/Main/PS0352)

## Docker Compose 
[Docker Compose](https://docs.docker.com/compose/) is a tool for defining and running multi-container Docker applications. With Compose, you use a YAML file to configure your application’s services. Then, with a single command, you create and start all the services from your configuration. 

The key features of Compose that make it effective are:
* Have multiple isolated environments on a single host
* Preserves volume data when containers are created
* Only recreate containers that have changed
* Supports variables and moving a composition between environments

[3. Creating a Docker Compose File (YAML) for a Spring Boot Project and PostgreSQL ](https://courses.cs.ut.ee/2023/esi/spring/Main/PS0353)

 
[4. Play with Docker  ](https://courses.cs.ut.ee/2023/esi/spring/Main/PS0354)
