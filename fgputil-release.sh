#!/bin/bash

# download standard release script and execute interactively
curl -O https://raw.githubusercontent.com/VEuPathDB/base-pom/main/release.sh && ./release.sh

# remove downloaded file regardless of success
rm -f release.sh
