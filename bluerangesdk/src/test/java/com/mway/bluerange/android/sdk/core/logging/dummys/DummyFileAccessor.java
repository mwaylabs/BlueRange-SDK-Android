//
//  DummyFileAccessor.java
//  BlueRangeSDK
//
// Copyright (c) 2016-2017, M-Way Solutions GmbH
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the M-Way Solutions GmbH nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY M-Way Solutions GmbH ''AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL M-Way Solutions GmbH BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
