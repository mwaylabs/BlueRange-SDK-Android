//
//  FileAccessorImpl.java
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

package com.mway.bluerange.android.sdk.utils.io;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class FileAccessorImpl implements FileAccessor {

    private static final String kLogTag = "DefaultFileAccessor";
    private Context context;

    public FileAccessorImpl(Context context) {
        this.context = context;
    }

    @Override
    public List<String> getFileNames() {
        return Arrays.asList(this.context.fileList());
    }

    @Override
    public InputStream openFileInputStream(String fileName) throws FileNotFoundException {
        return this.context.openFileInput(fileName);
    }

    @Override
    public OutputStream openFileOutputStream(String fileName) throws FileNotFoundException {
        return this.context.openFileOutput(fileName, Context.MODE_PRIVATE);
    }

    @Override
    public void deleteFile(String fileName) {
        this.context.deleteFile(fileName);
    }
}
