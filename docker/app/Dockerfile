# =====================================================================
# Az alkalmazás futtató image-e.
# A Jenkins build során elkészített blogplatform.jar fájlt másoljuk be.
# =====================================================================
FROM eclipse-temurin:21-jre

# Időzóna beállítása
ENV TZ=Europe/Budapest

WORKDIR /app

# A buildelt JAR az image build kontextusából (workspace) érkezik.
# A Jenkinsfile a backend/build/libs/blogplatform.jar fájlt másolja a build kontextusba.
COPY blogplatform.jar /app/blogplatform.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/blogplatform.jar"]
