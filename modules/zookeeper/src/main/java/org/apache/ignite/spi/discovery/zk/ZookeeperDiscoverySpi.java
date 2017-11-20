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

package org.apache.ignite.spi.discovery.zk;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.managers.discovery.JoiningNodesAware;
import org.apache.ignite.lang.IgniteProductVersion;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.spi.IgniteSpiAdapter;
import org.apache.ignite.spi.IgniteSpiContext;
import org.apache.ignite.spi.IgniteSpiException;
import org.apache.ignite.spi.IgniteSpiMultipleInstancesSupport;
import org.apache.ignite.spi.discovery.DiscoveryMetricsProvider;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.DiscoverySpiCustomMessage;
import org.apache.ignite.spi.discovery.DiscoverySpiDataExchange;
import org.apache.ignite.spi.discovery.DiscoverySpiHistorySupport;
import org.apache.ignite.spi.discovery.DiscoverySpiListener;
import org.apache.ignite.spi.discovery.DiscoverySpiNodeAuthenticator;
import org.apache.ignite.spi.discovery.DiscoverySpiOrderSupport;
import org.apache.ignite.spi.discovery.zk.internal.ZookeeperClusterNode;
import org.apache.ignite.spi.discovery.zk.internal.ZookeeperDiscoveryImpl;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
@IgniteSpiMultipleInstancesSupport(true)
@DiscoverySpiOrderSupport(true)
@DiscoverySpiHistorySupport(true)
public class ZookeeperDiscoverySpi extends IgniteSpiAdapter implements DiscoverySpi, JoiningNodesAware {
    /** */
    private String zkConnectionString;

    /** */
    private int sesTimeout = 5000;

    /** */
    private String basePath = "/apacheIgnite";

    /** */
    private String clusterName = "default";

    /** */
    private DiscoverySpiListener lsnr;

    /** */
    private DiscoverySpiDataExchange exchange;

    /** */
    private DiscoverySpiNodeAuthenticator auth;

    /** */
    private DiscoveryMetricsProvider metricsProvider;

    /** */
    private ZookeeperDiscoveryImpl impl;

    /** */
    private Map<String, Object> locNodeAttrs;

    /** */
    private IgniteProductVersion locNodeVer;

    /** */
    private Serializable consistentId;

    /** */
    @LoggerResource
    private IgniteLogger log;

    public String getBasePath() {
        return basePath;
    }

    public ZookeeperDiscoverySpi setBasePath(String basePath) {
        this.basePath = basePath;

        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public ZookeeperDiscoverySpi setClusterName(String clusterName) {
        this.clusterName = clusterName;

        return this;
    }

    public int getSessionTimeout() {
        return sesTimeout;
    }

    public ZookeeperDiscoverySpi setSessionTimeout(int sesTimeout) {
        this.sesTimeout = sesTimeout;

        return this;
    }

    public String getZkConnectionString() {
        return zkConnectionString;
    }

    public ZookeeperDiscoverySpi setZkConnectionString(String zkConnectionString) {
        this.zkConnectionString = zkConnectionString;

        return this;
    }

    /** {@inheritDoc} */
    @Override public boolean knownNode(UUID nodeId) {
        return impl.knownNode(nodeId);
    }

    /** {@inheritDoc} */
    @Nullable @Override public Serializable consistentId() throws IgniteSpiException {
        return consistentId;
    }

    /** {@inheritDoc} */
    @Override public Collection<ClusterNode> getRemoteNodes() {
        return impl.remoteNodes();
    }

    /** {@inheritDoc} */
    @Override public ClusterNode getLocalNode() {
        return impl != null ? impl.localNode() : null;
    }

    /** {@inheritDoc} */
    @Nullable @Override public ClusterNode getNode(UUID nodeId) {
        return impl.node(nodeId);
    }

    /** {@inheritDoc} */
    @Override public boolean pingNode(UUID nodeId) {
        return impl.pingNode(nodeId);
    }

    /** {@inheritDoc} */
    @Override public void setNodeAttributes(Map<String, Object> attrs, IgniteProductVersion ver) {
        assert locNodeAttrs == null;
        assert locNodeVer == null;

        if (log.isDebugEnabled()) {
            log.debug("Node attributes to set: " + attrs);
            log.debug("Node version to set: " + ver);
        }

        locNodeAttrs = attrs;
        locNodeVer = ver;
    }

    /** {@inheritDoc} */
    @Override public void setListener(@Nullable DiscoverySpiListener lsnr) {
        this.lsnr = lsnr;
    }

    /** {@inheritDoc} */
    @Override public void setDataExchange(DiscoverySpiDataExchange exchange) {
        this.exchange = exchange;
    }

    /** {@inheritDoc} */
    @Override public void setMetricsProvider(DiscoveryMetricsProvider metricsProvider) {
        this.metricsProvider = metricsProvider;
    }

    /** {@inheritDoc} */
    @Override public void disconnect() throws IgniteSpiException {
        // TODO ZK
    }

    /** {@inheritDoc} */
    @Override public void setAuthenticator(DiscoverySpiNodeAuthenticator auth) {
        // TODO ZK
        this.auth = auth;
    }

    /** {@inheritDoc} */
    @Override public long getGridStartTime() {
        return impl.gridStartTime();
    }

    /** {@inheritDoc} */
    @Override public void sendCustomEvent(DiscoverySpiCustomMessage msg) {
        impl.sendCustomEvent(msg);
    }

    /** {@inheritDoc} */
    @Override public void failNode(UUID nodeId, @Nullable String warning) {
        // TODO ZK
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public boolean isClientMode() throws IllegalStateException {
        return impl.localNode().isClient();
    }

    /** {@inheritDoc} */
    @Override protected void onContextInitialized0(IgniteSpiContext spiCtx) throws IgniteSpiException {
        super.onContextInitialized0(spiCtx);
    }

    /** {@inheritDoc} */
    @Override public void spiStart(@Nullable String igniteInstanceName) throws IgniteSpiException {
        ZookeeperClusterNode locNode = initLocalNode();

        log.info("Start Zookeeper discovery [zkConnectionString=" + zkConnectionString +
            ", sesTimeout=" + sesTimeout +
            ", basePath=" + basePath +
            ", clusterName=" + clusterName + ']');

        impl = new ZookeeperDiscoveryImpl(log,
            basePath,
            clusterName,
            locNode,
            lsnr,
            exchange);

        try {
            impl.joinTopology(igniteInstanceName, zkConnectionString, sesTimeout);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IgniteSpiException("Failed to join cluster, thread was interrupted", e);
        }
    }

    /** {@inheritDoc} */
    @Override public void spiStop() throws IgniteSpiException {
        if (impl != null)
            impl.stop();
    }

    /**
     * For testing only.
     *
     * @throws Exception If failed.
     */
    public void waitConnectStart() throws Exception {
        //connectStart.await();
    }

    /**
     * @return Local node instance.
     */
    private ZookeeperClusterNode initLocalNode() {
        assert ignite != null;

        consistentId = ignite.configuration().getConsistentId();

        UUID nodeId = ignite.configuration().getNodeId();

        // TODO ZK
        if (consistentId == null)
            consistentId = nodeId;

        ZookeeperClusterNode locNode = new ZookeeperClusterNode(nodeId,
            locNodeVer,
            locNodeAttrs,
            consistentId,
            ignite.configuration().isClientMode());

        locNode.local(true);

        DiscoverySpiListener lsnr = this.lsnr;

        if (lsnr != null)
            lsnr.onLocalNodeInitialized(locNode);

        if (log.isDebugEnabled())
            log.debug("Local node initialized: " + locNode);

        return locNode;
    }
}