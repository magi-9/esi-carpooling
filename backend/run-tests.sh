#!/bin/bash

# ==============================================================================
# Configuration
# Update these paths to point to the respective JDK installations on your machine
# ==============================================================================
export JAVA21_HOME="/usr/lib/jvm/java-21-openjdk-amd64" # Example path, update as needed
export JAVA25_HOME="/usr/lib/jvm/java-25-openjdk-amd64" # Example path, update as needed

# Define service groupings
MAVEN_SERVICES=(
	"api-gateway-service"
	"ride-discovery-service"
	"payment-service"
	"ride-booking-service"
	"validation-service"
)

GRADLE_SERVICES=(
	"auth-service"
	"profile-service"
)

# Colors for terminal output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Arrays to keep track of successes and failures
FAILED_SERVICES=()
SUCCESSFUL_SERVICES=()

# ==============================================================================
# Helper Function to Run Tests
# ==============================================================================
run_test() {
	local SERVICE_NAME=$1
	local BUILD_TOOL=$2
	local TARGET_JAVA_HOME=$3

	echo -e "${YELLOW}======================================================${NC}"
	echo -e "${YELLOW}Running tests for: ${SERVICE_NAME}${NC}"
	echo -e "Build Tool: ${BUILD_TOOL} | JDK Home: ${TARGET_JAVA_HOME}"
	echo -e "${YELLOW}======================================================${NC}"

	# Check if directory exists
	if [ ! -d "$SERVICE_NAME" ]; then
		echo -e "${RED}Error: Directory '$SERVICE_NAME' not found.${NC}\n"
		FAILED_SERVICES+=("$SERVICE_NAME (Directory not found)")
		return 1
	fi

	cd "$SERVICE_NAME" || exit

	# Set JAVA_HOME temporarily for this sub-shell/execution
	export JAVA_HOME="$TARGET_JAVA_HOME"
	export PATH="$JAVA_HOME/bin:$PATH"

	# Execute the appropriate test command
	if [ "$BUILD_TOOL" == "Maven" ]; then
		# Using Maven wrapper if it exists, otherwise fallback to global mvn
		if [ -f "./mvnw" ]; then
			./mvnw clean test
		else
			mvn clean test
		fi
	elif [ "$BUILD_TOOL" == "Gradle" ]; then
		# Using Gradle wrapper if it exists, otherwise fallback to global gradle
		if [ -f "./gradlew" ]; then
			./gradlew clean test
		else
			gradle clean test
		fi
	fi

	# Capture the exit code of the test command
	if [ $? -eq 0 ]; then
		echo -e "\n${GREEN}✔ Tests passed for $SERVICE_NAME${NC}\n"
		SUCCESSFUL_SERVICES+=("$SERVICE_NAME")
	else
		echo -e "\n${RED}✘ Tests failed for $SERVICE_NAME${NC}\n"
		FAILED_SERVICES+=("$SERVICE_NAME")
	fi

	# Go back to the root directory
	cd ..
}

# ==============================================================================
# Main Execution
# ==============================================================================

echo "Starting test suite..."

# 1. Run Maven Services (Java 21)
for SERVICE in "${MAVEN_SERVICES[@]}"; do
	run_test "$SERVICE" "Maven" "$JAVA21_HOME"

	# pause execution after each test
	read
done

# 2. Run Gradle Services (Java 25)
for SERVICE in "${GRADLE_SERVICES[@]}"; do
	run_test "$SERVICE" "Gradle" "$JAVA25_HOME"

	# pause execution after each test
	read
done

# ==============================================================================
# Summary Report
# ==============================================================================
echo -e "${YELLOW}======================================================${NC}"
echo -e "${YELLOW}                    TEST SUMMARY                      ${NC}"
echo -e "${YELLOW}======================================================${NC}"

echo -e "Successful Services (${#SUCCESSFUL_SERVICES[@]}):"
for SERVICE in "${SUCCESSFUL_SERVICES[@]}"; do
	echo -e "  ${GREEN}✔ $SERVICE${NC}"
done

echo ""

if [ ${#FAILED_SERVICES[@]} -ne 0 ]; then
	echo -e "Failed Services (${#FAILED_SERVICES[@]}):"
	for SERVICE in "${FAILED_SERVICES[@]}"; do
		echo -e "  ${RED}✘ $SERVICE${NC}"
	done
	echo -e "\n${RED}Some tests failed. Please review the logs above.${NC}"
	exit 1
else
	echo -e "${GREEN}All services passed their unit tests successfully!${NC}"
	exit 0
fi
