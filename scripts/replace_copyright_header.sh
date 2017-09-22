#!/bin/bash

# Temp directory
TMP_DIR=$(mktemp -dt "XXXXXXXX")
COMPANY="M-Way Solutions GmbH"

for FILE_PATH in $(ls -R bluerangesdk/src/main/java/ bluerangesdk/src/test/java/ | awk '
/:$/&&f{s=$0;f=0}
/:$/&&!f{sub(/:$/,"");s=$0;f=1;next}
NF&&f{ print s"/"$0 }' | grep '\.java$')
do
	# Log
	echo "Updating copyright header for ${FILE_PATH}"

	# Temp path
	FILE_NAME="$(basename ${FILE_PATH})"
	TMP_PATH="${TMP_DIR}/${FILE_NAME}"
	cp -f "${FILE_PATH}" "${TMP_PATH}"

	# First strip leading blank lines
	sed -i . '/./,$!d' "${TMP_PATH}"

	# Do nothing if file start with block comment (/*)
	STARTS_WITH_BLOCK="$(head -n1 ${TMP_PATH} | grep '^/\*.*' )"
	[[ -n "${STARTS_WITH_BLOCK}" ]] && echo "File starts with block comment. Ignoring." && continue

	# Remove current header
	# Not an optimal way - first count // lines with awk, then removed that number of lines with sed
	N=$(cat "${TMP_PATH}" | awk '{ if(/^\/\//) print; else exit; }' | wc -l | xargs)
	# Do nothing if count is 0
	[[ ${N} -gt 0 ]] && sed -i . "1,${N}d" "${TMP_PATH}"

	# And cleanup leading blank lines again
	sed -i . '/./,$!d' "${TMP_PATH}"

	# Insert new header using current year
	HEADER="//\\
//  ${FILE_NAME}\\
//  BlueRangeSDK\\
//\\
// Copyright (c) 2016-$(date +'%Y'), ${COMPANY}\\
// All rights reserved.\\
//\\
// Licensed to the Apache Software Foundation (ASF) under one\\
// or more contributor license agreements.  See the NOTICE file\\
// distributed with this work for additional information\\
// regarding copyright ownership.  The ASF licenses this file\\
// to you under the Apache License, Version 2.0 (the\\
// \"License\"); you may not use this file except in compliance\\
// with the License.  You may obtain a copy of the License at\\
//\\
//   http://www.apache.org/licenses/LICENSE-2.0\\
//\\
// Unless required by applicable law or agreed to in writing,\\
// software distributed under the License is distributed on an\\
// \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\\
// KIND, either express or implied.  See the License for the\\
// specific language governing permissions and limitations\\
// under the License.\\
//\\
\\
"
	sed -i . "1s|^|${HEADER}|g" "${TMP_PATH}"

	# Produce output
	mv -f "${TMP_PATH}" "${FILE_PATH}"
done
