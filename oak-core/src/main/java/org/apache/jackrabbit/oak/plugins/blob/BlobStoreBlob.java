/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jackrabbit.oak.plugins.blob;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.api.Blob;

/**
 * A blob implementation.
 */
public class BlobStoreBlob implements Blob {
    
    private final BlobStore blobStore;
    private final String id;
    
    public BlobStoreBlob(BlobStore blobStore, String id) {
        this.blobStore = blobStore;
        this.id = id;
    }

    @Override
    @Nonnull
    public InputStream getNewStream() {
        try {
            return blobStore.getInputStream(id);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while obtaining " +
                    "InputStream for blobId ["+id+"]",e);
        }
    }

    @Override
    public long length() {
        try {
            return blobStore.getBlobLength(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid blob id: " + id, e);
        }
    }

    @Override @CheckForNull
    public String getReference() {
        return id;
    }

    //------------------------------------------------------------< Object >--

    @Override
    public String toString() {
        return id;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } 
        if (other instanceof BlobStoreBlob) {
            BlobStoreBlob b = (BlobStoreBlob) other;
            // theoretically, the data could be the same  
            // even if the id is different
            return b.id.equals(id);
        }
        return false;
    }

}
