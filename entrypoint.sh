
echo "🚀 Starting Spring Boot Application"
echo "🔧 App Name: $APP_NAME"
echo "🔢 App Version: $APP_VERSION"

JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"
JAVA_OPTS=${JAVA_OPTS:-"-Xms256m -Xmx512m"}

echo "📦 Running: java $JAVA_OPTS -jar $JAR_NAME"
# Start the Spring Boot app
exec java $JAVA_OPTS -jar "$JAR_NAME"