#!/bin/bash

# Variables
SCRIPTS_DIR="./Scripts"
README="README.md"
README_TEMPLATE="${SCRIPTS_DIR}/README_template.md"
README_TMP="${SCRIPTS_DIR}/README_tmp.md"
cp $README_TEMPLATE $README_TMP 

# Remove iOS content
echo "Removing iOS content"
sed -i -e '
  /<iOS>/,/<\/iOS>/ {
    1 {
      s/^.*$//
      b
    }
    d
  }
' ${README_TMP}

# Enable Android content
echo "Adding Android content"
sed -i -e 's/<android>//g' ${README_TMP}
sed -i -e 's/<\/android>//g' ${README_TMP}

cp ${README_TMP} $README

rm ${SCRIPTS_DIR}/README_tmp*
