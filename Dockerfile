FROM node:18 AS nodeBuilder
WORKDIR /app
COPY ./frontend ./
RUN npm install
RUN npx nuxi generate

FROM eclipse-temurin:17-jdk AS jdkBuilder
WORKDIR /app
COPY ./ ./
COPY --from=nodeBuilder /app/.output/public ./src/main/resources/static
RUN ./gradlew build -x test --no-daemon --stacktrace

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=jdkBuilder /app/build/libs/app.jar ./app.jar
EXPOSE 18080
ENTRYPOINT ["java", "-jar", "./app.jar"]
