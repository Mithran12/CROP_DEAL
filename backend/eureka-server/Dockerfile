FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.war app.war
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.war"]
