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

package org.apache.jackrabbit.oak.plugins.document;

import java.util.Map;
import java.util.SortedMap;

import javax.annotation.CheckForNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.jackrabbit.oak.plugins.document.Collection.NODES;

/**
 * Checkpoints provide details around which revision are to be kept. Currently these
 * are stored in NODES collection itself.
 */
class Checkpoints {
    /**
     * Id of checkpoint document. It differs from normal convention of ID used for NodeDocument
     * which back JCR Nodes as it is internal to DocumentNodeStore
     */
    private static final String ID = "/checkpoint";

    /**
     * Property name to store all checkpoint data. The data is stored as Revision => expiryTime
     */
    private static final String PROP_CHECKPOINT = "checkpoint";

    private final DocumentNodeStore nodeStore;

    private final DocumentStore store;

    private final Logger log = LoggerFactory.getLogger(getClass());

    Checkpoints(DocumentNodeStore store) {
        this.nodeStore = store;
        this.store = store.getDocumentStore();
        createIfNotExist();
    }

    public Revision create(long lifetimeInMillis) {
        Revision r = nodeStore.getHeadRevision();
        UpdateOp op = new UpdateOp(ID, false);
        long endTime = nodeStore.getClock().getTime() + lifetimeInMillis;
        op.setMapEntry(PROP_CHECKPOINT, r, Long.toString(endTime));
        store.createOrUpdate(NODES, op);
        return r;
    }


    /**
     * Returns the oldest valid checkpoint registered.
     *
     * @return oldest valid checkpoint registered. Might return null if no valid
     * checkpoint found
     */
    @CheckForNull
    public Revision getOldestRevisionToKeep() {
        //Get uncached doc
        NodeDocument cdoc = store.find(NODES, ID, 0);
        SortedMap<Revision, String> checkpoints = cdoc.getLocalMap(PROP_CHECKPOINT);

        final long currentTime = nodeStore.getClock().getTime();
        UpdateOp op = new UpdateOp(ID, false);
        Revision lastAliveRevision = null;
        long oldestExpiryTime = 0;

        for (Map.Entry<Revision, String> e : checkpoints.entrySet()) {
            final long expiryTime = Long.parseLong(e.getValue());
            if (currentTime > expiryTime) {
                op.removeMapEntry(PROP_CHECKPOINT, e.getKey());
            } else if (expiryTime > oldestExpiryTime) {
                oldestExpiryTime = expiryTime;
                lastAliveRevision = e.getKey();
            }
        }

        if (op.hasChanges()) {
            store.findAndUpdate(NODES, op);
            log.info("Purged {} expired checkpoints", op.getChanges().size());
        }

        return lastAliveRevision;
    }

    private void createIfNotExist() {
        if (store.find(NODES, ID) == null) {
            UpdateOp updateOp = new UpdateOp(ID, true);
            updateOp.set(Document.ID, ID);
            store.createOrUpdate(NODES, updateOp);
        }
    }
}
