FROM ubuntu:latest

COPY WJRater.jar /webapp/app.jar
COPY JRater.jar /webapp/jrater.jar
COPY Jplag.jar /webapp/jplag.jar
COPY build.properties /webapp/build.properties

RUN apt-get update
RUN apt-get install -y default-jdk
RUN apt-get install -y zip unzip
RUN javac -version

WORKDIR /webapp

EXPOSE 8080

CMD ["java" , "-jar", "app.jar", \
    "--spring.jpa.hibernate.ddl-auto=update", \
    "--spring.datasource.url=jdbc:mysql://localhost:3308/rater-web-db?useSSL=false&allowPublicKeyRetrieval=true", \
    "--spring.datasource.username=root", \
    "--spring.datasource.password=1234", \
    "--spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver", \
    "--server.port=8080", \
    "--projects.path=webapp/students", \
    "--references.path=/webapp/references", \
    "--jplagDir.path=/webapp/jplag", \
    "--jar.path=/webapp/jrater.jar", \
    "--jplag.path=/webapp/jplag.jar", \
    "--nb.properties.file=/webapp/build.properties", \
    "--app.username=q", \
    "--app.password=q"]