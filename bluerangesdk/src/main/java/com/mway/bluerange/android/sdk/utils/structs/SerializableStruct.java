//
//  SerializableStruct.java
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

package com.mway.bluerange.android.sdk.utils.structs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javolution.io.Struct;

/**
 *
 */
public class SerializableStruct extends Struct implements Serializable {

    public class SerializableUnsigned16 extends Unsigned16 {
        private void writeObject(ObjectOutputStream oos)
                throws IOException {
            int value = this.get();
            oos.writeObject(value);
        }

        private void readObject(ObjectInputStream ois)
                throws ClassNotFoundException, IOException {
            int value = (int)ois.readObject();
            this.set(value);
        }
    }

    // Just used for testing purposes
    public List<Field> getAllFields() {
        return getDeclaredAndInheritedPrivateFields(this.getClass());
    }

    private List<Field> getDeclaredAndInheritedPrivateFields(Class<?> type) {
        List<Field> result = new ArrayList<Field>();

        Class<?> i = type;
        while (i != null && i != Object.class) {
            for (Field field : i.getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    result.add(field);
                }
            }
            i = i.getSuperclass();
        }

        return result;
    }
}
