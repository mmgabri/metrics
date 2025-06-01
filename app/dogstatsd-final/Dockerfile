FROM openjdk:17-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
RUN apk add --no-cache curl
EXPOSE 8080
ENV JAVA_OPTS="-Xms2g -Xmx3g"
CMD ["java", "-jar", "app.jar"]