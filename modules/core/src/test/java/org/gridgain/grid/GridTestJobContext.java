/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridgain.grid;

import org.apache.ignite.compute.*;
import org.apache.ignite.lang.*;
import org.gridgain.grid.util.lang.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Test job context.
 */
public class GridTestJobContext extends GridMetadataAwareAdapter implements ComputeJobContext {
    /** */
    private final IgniteUuid jobId;

    /** */
    private final Map<Object, Object> attrs = new HashMap<>();

    /** */
    public GridTestJobContext() {
        jobId = IgniteUuid.randomUuid();
    }

    /**
     * @param jobId Job ID.
     */
    public GridTestJobContext(IgniteUuid jobId) {
        this.jobId = jobId;
    }

    /** {@inheritDoc} */
    @Override public IgniteUuid getJobId() {
        return jobId;
    }

    /** {@inheritDoc} */
    @Override public void setAttribute(Object key, @Nullable Object val) {
        attrs.put(key, val);
    }

    /** {@inheritDoc} */
    @Override public void setAttributes(Map<?, ?> attrs) {
        this.attrs.putAll(attrs);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public <K, V> V getAttribute(K key) {
        return (V)attrs.get(key);
    }

    /** {@inheritDoc} */
    @Override public Map<Object, Object> getAttributes() {
        return new HashMap<>(attrs);
    }

    /** {@inheritDoc} */
    @Override public boolean heldcc() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public String cacheName() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public <T> T affinityKey() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public <T> T holdcc() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public <T> T holdcc(long timeout) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void callcc() {
        // No-op.
    }
}
