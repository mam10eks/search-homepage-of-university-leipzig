#!/bin/bash -e

TMP_DIR="tmp_dir"


rm -Rf ${TMP_DIR}
git clone https://github.com/guenthersebastian/LeipzigUniversityNetSearchEngineFrontend ${TMP_DIR}

cd ${TMP_DIR}
cp ../Dockerfile .

docker build -t uni-search-engine/frontend:0.0.1-SNAPSHOT .

cd ..
