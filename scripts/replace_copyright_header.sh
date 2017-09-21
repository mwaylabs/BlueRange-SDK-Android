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
// Redistribution and use in source and binary forms, with or without\\
// modification, are permitted provided that the following conditions are met:\\
//     * Redistributions of source code must retain the above copyright\\
//       notice, this list of conditions and the following disclaimer.\\
//     * Redistributions in binary form must reproduce the above copyright\\
//       notice, this list of conditions and the following disclaimer in the\\
//       documentation and/or other materials provided with the distribution.\\
//     * Neither the name of the ${COMPANY} nor the\\
//       names of its contributors may be used to endorse or promote products\\
//       derived from this software without specific prior written permission.\\
//\\
// THIS SOFTWARE IS PROVIDED BY ${COMPANY} ''AS IS'' AND ANY\\
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\\
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\\
// DISCLAIMED. IN NO EVENT SHALL ${COMPANY} BE LIABLE FOR ANY\\
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\\
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\\
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND\\
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\\
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\\
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\\
//\\
\\
"
	sed -i . "1s|^|${HEADER}|g" "${TMP_PATH}"

	# Produce output
	mv -f "${TMP_PATH}" "${FILE_PATH}"
done
