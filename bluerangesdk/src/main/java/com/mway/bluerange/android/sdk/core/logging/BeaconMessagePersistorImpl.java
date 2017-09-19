//
//  BeaconMessagePersistorImpl.java
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

package com.mway.bluerange.android.sdk.core.logging;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mway.bluerange.android.sdk.core.scanning.messages.NullBeaconMessage;
import com.mway.bluerange.android.sdk.utils.io.FileAccessor;
import com.mway.bluerange.android.sdk.utils.io.FileAccessorImpl;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

/**
 * This class is the default implementation of the {@link BeaconMessagePersistor} interface. The
 * log has a default maximum size of {@link #DEFAULT_MAX_LOG_SIZE}. In order to increase
 * performance and compression efficiency and decrease energy consumption, the messages are saved
 * in chunks with a chunk size of {@link #chunkSize}. The persisting strategy is defined by the
 * {@link FileAccessor}. By default, each chunk is compressed with the gzip compression algorithm
 * before it is being saved. However, to increase the persisting efficiency, the compression
 * might be turned off.
 */
public class BeaconMessagePersistorImpl implements BeaconMessagePersistor {

    // Configuration
    // By default we limit the log size to about 100 MB,
    // if the log was saved without compression.
    // We assume an average message size of 100 byte per message.
    // Therefore we set the default maximum log size to 1000000 messages.
    private static final int DEFAULT_MAX_LOG_SIZE = 1000000;
    private int maxLogSize = DEFAULT_MAX_LOG_SIZE;

    // Tracing
    private ITracer tracer = Tracer.getInstance();
    private static final String kLogTag = BeaconMessagePersistorImpl.class.getSimpleName();

    // Caching
    private List<BeaconMessage> cachedMessages = new ArrayList<BeaconMessage>();
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private int chunkSize = DEFAULT_CHUNK_SIZE;

    // Serialization
    private SerializationMode serializationMode = SerializationMode.OBJECT_SERIALIZATION;

    // Persisting
    private FileAccessor fileAccessor;
    private static final String DEFAULT_FILE_NAME_PREFIX = "scanlog_";
    private String fileNamePrefix = DEFAULT_FILE_NAME_PREFIX;
    // Due to performance reasons, we cache the list of chunk file names.
    private List<String> chunkFileNames = new ArrayList<>();

    // Compressing
    private boolean zippingEnabled = true;

    // Serialization Mode
    public enum SerializationMode {
        OBJECT_SERIALIZATION,
        JSON_SERIALIZATION
    }

    // Log iterator class
    private class LogIterator implements Iterator<BeaconMessage> {

        // Pointer to current chunk.
        private String currentChunkFileName = null;
        private boolean currentChunkIsCacheChunk = false;
        private List<BeaconMessage> currentChunk = new ArrayList<>();

        // Pointer to current message inside chunk
        int messagePointer = 0;

        @Override
        public boolean hasNext() {
            // To overcome dirty read problems we need
            // to synchronize all threads accessing the fileAccessor and preload
            // the chunk yet when hasNext is called.
            synchronized (fileAccessor) {
                readNextChunkIfNecessary();
                return hasRemainingMessagesInChunk();
            }
        }

        private boolean hasRemainingMessagesInChunk() {
            return messagePointer < currentChunk.size();
        }

        private boolean hasRemainingChunks() {
            // Returns true if a file exists with the
            // chunk prefix and is lexicographically greater.
            for (String fileName : chunkFileNames) {
                if (fileName.startsWith(fileNamePrefix)) {
                    if (currentChunkFileName == null ||
                            currentChunkFileName.compareTo(fileName) < 0 ) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public BeaconMessage next() {
            // If hasNext is called always before the next method
            // this condition will never become true
            // because the chunk will always be preloaded
            // in the hasNext method. However, if the
            // user does not call hasNext or call next multiple
            // times, the chunk most be loaded here.
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            BeaconMessage message = this.currentChunk.get(this.messagePointer);
            this.messagePointer++;
            return message;
        }

        private void readNextChunkIfNecessary() {
            boolean hasRemainingMessagesInChunk = hasRemainingMessagesInChunk();
            boolean hasRemainingChunks = hasRemainingChunks();
            if ((!hasRemainingMessagesInChunk) && hasRemainingChunks) {
                readNextSavedChunk();
            } else if ((!hasRemainingMessagesInChunk && (!hasRemainingChunks)) && !currentChunkIsCacheChunk) {
                // Some messages may still be in the cache. They should
                // also be considered by the iterator. But the cache chunk
                // should of course only be loaded once.
                readCachedChunk();
            }
        }

        private void readNextSavedChunk() {
            String nextChunkFileName = null;
            List<BeaconMessage> nextChunk = null;
            nextChunkFileName = getNextChunkFileName();
            if (nextChunkFileName != null) {
                nextChunk = readChunk(nextChunkFileName);
            } else {
                throw new NoSuchElementException();
            }
            this.currentChunkFileName = nextChunkFileName;
            this.currentChunk = nextChunk;
            this.messagePointer = 0;
        }

        private String getNextChunkFileName() {
            for (String fileName : chunkFileNames) {
                if (fileName.startsWith(fileNamePrefix)) {
                    if (currentChunkFileName == null ||
                            currentChunkFileName.compareTo(fileName) < 0) {
                        return fileName;
                    }
                }
            }
            return null;
        }

        private List<BeaconMessage> readChunk(String fileName) {
            List<BeaconMessage> chunk = new ArrayList<>();
            try {
                if (zippingEnabled) {
                    InputStream fin = fileAccessor.openFileInputStream(fileName);
                    GZIPInputStream gzipIn = new GZIPInputStream(fin);
                    if (serializationMode == SerializationMode.JSON_SERIALIZATION) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        chunk = objectMapper.readValue(gzipIn, new TypeReference<List<BeaconMessage>>(){});
                    } else {
                        ObjectInputStream in = new ObjectInputStream(gzipIn);
                        chunk = (ArrayList<BeaconMessage>) in.readObject();
                        in.close();
                    }
                    gzipIn.close();
                    fin.close();
                } else {
                    InputStream fin = fileAccessor.openFileInputStream(fileName);
                    ObjectInputStream in = new ObjectInputStream(fin);
                    chunk = (ArrayList<BeaconMessage>) in.readObject();
                    in.close();
                    fin.close();
                }

            } catch (FileNotFoundException e) {
                // Can not be reached since we check for file existance
                // right before in a synchronized block.
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // Can not be reached since we do not modify
                // any data inside the chunk. The only case is
                // when logging data of an older SDK version
                // is read. In this case the chunk should
                // be ignored.
                // Therefore: Empty catch implementation
            } catch (EOFException e) {
                // This case can happen if the file is empty and
                // thus also corrupted. Like in the ClassNotFoundException
                // catch block, we do ignore this chunk
                // and just return an empty array of messages.
                // Therefore: Empty catch implementation.
            } catch (IOException e) {
                // Can not be reached since we check for file existance
                // right before in a synchronized block.
                e.printStackTrace();
            } catch (Exception e) {
                // All other exceptions should be logged.
                // The chunk should remain empty.
                e.printStackTrace();
            }
            return chunk;
        }

        private void readCachedChunk() {
            this.currentChunk = new ArrayList<>();
            // Copy all messages to a secondary cache in order
            // to avoid race conditions.
            this.currentChunkIsCacheChunk = true;
            this.currentChunk.addAll(cachedMessages);
            this.messagePointer = 0;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // Initialization

    public BeaconMessagePersistorImpl(Context context) {
        this(new FileAccessorImpl(context), DEFAULT_CHUNK_SIZE);
    }

    public BeaconMessagePersistorImpl(FileAccessor fileAccessor) {
        this(fileAccessor, DEFAULT_CHUNK_SIZE);
    }

    public BeaconMessagePersistorImpl(FileAccessor fileAccessor, int chunkSize) {
        this.fileAccessor = fileAccessor;
        this.chunkFileNames.addAll(this.fileAccessor.getFileNames());
        this.chunkSize = chunkSize;
    }

    // Reading

    /**
     * It is recommended to use the LogIterator to
     * read out the Log entries in a thread safe manner
     * because this method will block any modifications
     * on the activity log.
     * @return
     * @throws IOException
     */
    @Override
    public BeaconMessageLog readLog() {
        // We use the Iterator to prevent race conditions.
        List<BeaconMessage> messages = new ArrayList<BeaconMessage>();
        for (Iterator<BeaconMessage> iterator = getLogIterator(); iterator.hasNext();) {
            BeaconMessage beaconMessage = iterator.next();
            messages.add(beaconMessage);
        }
        return new BeaconMessageLog(messages);
    }

    @Override
    public Iterator<BeaconMessage> getLogIterator() {
        return new LogIterator();
    }

    @Override
    public int getTotalMessages() {
        int totalFiles = chunkFileNames.size();
        int totalMessagesInLog = this.chunkSize*totalFiles + this.cachedMessages.size();
        return totalMessagesInLog;
    }

    // Removing

    @Override
    public void clearMessages() {
        // We need to synchronize all threads
        // operating on the chunk files.
        synchronized (fileAccessor) {
            ListIterator<String> iterator = chunkFileNames.listIterator();
            while(iterator.hasNext()) {
                // Get the chunk file name.
                String fileName = iterator.next();
                // Delete the chunk;
                deleteChunk(fileName);
                // Remove the file name from the chunk file name list.
                iterator.remove();
            }
        }
        // Clear also the cached chunk
        this.cachedMessages.clear();
    }

    private void deleteChunk(String chunkFileName) {
        this.fileAccessor.deleteFile(chunkFileName);
    }

    // Writing

    @Override
    public void writeMessage(BeaconMessage beaconMessage) {
        // Do only add if log has not reached maximum size.
        if (getTotalMessages() < maxLogSize) {
            // Add message to the log cache.
            this.cachedMessages.add(beaconMessage);
            // Save the whole chunk if it is big enough
            if (this.cachedMessages.size() >= this.chunkSize) {
                saveChunk();
            }
        } else {
            tracer.logError(kLogTag, "Could not persist message to log. " +
                    "Maximum log size of " + maxLogSize + " bytes reached!");
        }
    }

    private void saveChunk() {
        // Persist chunk
        writeChunk();
        // Clear cache
        cachedMessages.clear();
    }

    @SuppressLint("DefaultLocale")
    private void writeChunk() {
        // We need to synchronize all threads
        // operating on the chunk files.
        synchronized (fileAccessor) {
            // Define file name for current chunk.
            final String fileName = fileNamePrefix + String.format("%03d", chunkFileNames.size());
            // Update the chunk file name list.
            chunkFileNames.add(fileName);

            OutputStream fos = null;
            try {
                if (zippingEnabled) {
                    fos = this.fileAccessor.openFileOutputStream(fileName);
                    GZIPOutputStream gzipOut = new GZIPOutputStream(fos);
                    if (this.serializationMode == SerializationMode.JSON_SERIALIZATION) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(gzipOut, this.cachedMessages);
                    } else {
                        ObjectOutputStream out = new ObjectOutputStream(gzipOut);
                        out.writeObject(this.cachedMessages);
                        out.close();
                    }
                    gzipOut.close();
                    fos.close();
                } else {
                    fos = this.fileAccessor.openFileOutputStream(fileName);
                    ObjectOutputStream out = new ObjectOutputStream(fos);
                    out.writeObject(this.cachedMessages);
                    out.close();
                    fos.close();
                }

            } catch (FileNotFoundException e) {
                // Can not be reached since we create a file
                // if it does not exist.
                e.printStackTrace();
            } catch (IOException e) {
                // Can not be reached since we always
                // create a new file and save the content.
                e.printStackTrace();
            }
        }
    }

    /**
     * Persists all remaining in-memory-cached messages.
     */
    public void flush() {
        if (cachedMessages.size() > 0) {
            fillChunkWithPadding();
            saveChunk();
        }
    }

    private void fillChunkWithPadding() {
        while(cachedMessages.size() < this.chunkSize) {
            cachedMessages.add(new NullBeaconMessage());
        }
    }

    // Helper methods

    public int getLogSizeInBytes() {
        synchronized (fileAccessor) {
            int numBytes = 0;
            for (String fileName : chunkFileNames) {
                try {
                    InputStream is = fileAccessor.openFileInputStream(fileName);
                    //BufferedInputStream buf = new BufferedInputStream(is);
                    // int size = buf.available();
                    byte[] bytes = getBytes(is);
                    is.close();
                    int size = bytes.length;
                    numBytes += size;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return numBytes;
        }
    }

    private byte[] getBytes(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toByteArray();
    }

    // Getters and setters

    public boolean isZippingEnabled() {
        return zippingEnabled;
    }

    public void setZippingEnabled(boolean zippingEnabled) {
        this.zippingEnabled = zippingEnabled;
    }

    public int getMaxLogSize() {
        return this.maxLogSize;
    }

    public void setMaxLogSize(int maxLogSize) {
        this.maxLogSize = maxLogSize;
    }

    public void setTracer(ITracer tracer) {
        this.tracer = tracer;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public SerializationMode getSerializationMode() {
        return serializationMode;
    }

    public void setSerializationMode(SerializationMode serializationMode) {
        this.serializationMode = serializationMode;
    }

    /**
     * Returns the logger's chunk size.
     * @return The number of {@link BeaconMessage}s that define one chunk.
     */
    public int getChunkSize() {
        return chunkSize;
    }
}
