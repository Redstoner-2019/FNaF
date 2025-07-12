#!/bin/bash
set -e

PROFILES=("Linux-arm32" "Linux-arm64" "Linux-x64" "MacOS-arm64" "MacOS-x64" "Windows-arm64" "Windows-x64" "Windows-x86")

mvn clean

for PROFILE in "${PROFILES[@]}"; do
    echo "Packaging for $PROFILE..."
    mvn package -P$PROFILE
done

echo "Zipping res folder..."
zip -r target/resources.zip ./res

rm -f target/original-*.jar

echo "Generating game.json..."
cat > target/game.json <<EOF
{
  "versions": {
$(for PROFILE in "${PROFILES[@]}"; do
    echo "    \"$PROFILE\": {"
    echo "      \"main\": {"
    echo "        \"name\": \"FiveNightsAtFreddys-$PROFILE.jar\","
    echo "        \"saveLocation\": \"/versions/%version%/\""
    echo "      },"
    echo "      \"files\": {"
    echo "        \"resources.zip\": {"
    echo "          \"location\": \"\","
    echo "          \"actions\": {"
    echo "            \"unzip\": {"
    echo "              \"location\": \"/\","
    echo "              \"deleteAfter\": true"
    echo "            }"
    echo "          }"
    echo "        }"
    echo "      },"
    echo "      \"launch\": \"java -jar %filename% %TOKEN% false\""
    echo "    },"
done | sed '$ s/},$/}/')
  },
  "storeAt": {}
}
EOF
