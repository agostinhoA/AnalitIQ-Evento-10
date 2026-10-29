FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
# Las pruebas MySQL se ejecutan en una base aislada, nunca durante el despliegue.
RUN mvn -B -ntp -DskipTests package

FROM tomcat:9.0-jdk17-temurin-noble
ENV TZ=America/Argentina/Buenos_Aires
COPY deploy/render/server.xml /usr/local/tomcat/conf/server.xml
COPY deploy/render/context.xml /usr/local/tomcat/conf/context.xml
COPY deploy/render/start.sh /usr/local/bin/analitiq-start
COPY --from=build /build/target/analitiq.war /usr/local/tomcat/webapps/ROOT.war
RUN groupadd --gid 10001 analitiq \
    && useradd --uid 10001 --gid 10001 --no-create-home analitiq \
    && chown -R analitiq:analitiq /usr/local/tomcat \
    && sed -i 's/\r$//' /usr/local/bin/analitiq-start \
    && chmod 755 /usr/local/bin/analitiq-start
USER 10001:10001
EXPOSE 10000
CMD ["/usr/local/bin/analitiq-start"]
