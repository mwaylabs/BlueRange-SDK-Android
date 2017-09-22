//
//  DummyFileAccessor.java
//  BlueRangeSDK
//
// Copyright (c) 2016-2017, M-Way Solutions GmbH
// All rights reserved.
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.mway.bluerange.android.sdk.core.logging.dummys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.mway.bluerange.android.sdk.utils.io.FileAccessor;

/**
 *
 */
public class DummyFileAccessor implements FileAccessor {

    private List<String> fileNames = new ArrayList<String>();
    private List<ByteArrayOutputStream> files = new ArrayList<>();

    @Override
    public List<String> getFileNames() {
        return new ArrayList<String>(fileNames);
    }

    @Override
    public InputStream openFileInputStream(String fileName) throws FileNotFoundException {
        int index = fileNames.indexOf(fileName);
        if (index == -1) {
            throw new FileNotFoundException();
        }
        ByteArrayOutputStream outFile = files.get(index);
        ByteArrayInputStream inFile = new ByteArrayInputStream(outFile.toByteArray());
        return inFile;
    }

    @Override
    public OutputStream openFileOutputStream(String fileName) throws FileNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        fileNames.add(fileName);
        files.add(byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    public void corruptAllFiles() {
        byte[] corruptFile = new byte[1];
        corruptFile[0] = 0;
        for (int i = 0;i < files.size();i++) {
            ByteArrayOutputStream file = new ByteArrayOutputStream();
            try {
                file.write(corruptFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            files.set(i, file);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        int index = fileNames.indexOf(fileName);
        fileNames.remove(index);
        files.remove(index);
    }
}
