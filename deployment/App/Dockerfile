FROM openjdk:8

WORKDIR /webapp

COPY WJRater.jar /webapp/app.jar
COPY JRater.jar /webapp/jrater.jar
COPY JPlag.jar /webapp/jplag.jar
COPY build.properties /webapp/build.properties

EXPOSE 8080

RUN apt-get update
RUN apt-get install -y zip unzip ant

CMD ["java" , "-jar", "app.jar", \
    "--spring.mustache.suffix=.html", \
    "--spring.mustache.cache=false", \
    "--spring.jpa.hibernate.ddl-auto=create-drop", \
    "--spring.jpa.open-in-view=true", \
    "--spring.datasource.url=jdbc:mysql://mysql:3306/rater-web-db?useSSL=false&allowPublicKeyRetrieval=true", \
    "--spring.datasource.username=root", \
    "--spring.datasource.password=1234", \
    "--spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver", \
    "--server.port=8080", \
    "--projects.path=/webapp/students", \
    "--references.path=/webapp/references", \
    "--jplagDir.path=/webapp/jplag", \
    "--jar.path=/webapp/jrater.jar", \
    "--jplag.path=/webapp/jplag.jar", \
    "--nb.properties.file=/webapp/build.properties", \
    "--app.username=q", \
    "--app.password=q"]
