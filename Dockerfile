FROM amazoncorretto:21 AS builder

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN yum install -y tar gzip && yum clean all
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM amazoncorretto:21

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]