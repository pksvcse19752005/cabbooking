FROM eclipse-temurin:17-jdk as build
WORKDIR /app
COPY backend.java /app/backend.java
RUN javac backend.java

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/*.class /app/
EXPOSE 8080
CMD ["java", "backend"]
