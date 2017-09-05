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

package org.apache.ignite.internal.processors.cache.tree;

import org.apache.ignite.internal.pagemem.PageUtils;
import org.apache.ignite.internal.processors.cache.GridCacheUtils;
import org.apache.ignite.internal.processors.cache.mvcc.TxMvccVersion;
import org.apache.ignite.internal.processors.cache.persistence.CacheDataRowAdapter;
import org.apache.ignite.internal.processors.cache.persistence.CacheSearchRow;
import org.apache.ignite.internal.processors.cache.persistence.tree.BPlusTree;
import org.apache.ignite.internal.processors.cache.persistence.tree.io.BPlusIO;
import org.apache.ignite.internal.processors.cache.persistence.tree.io.BPlusInnerIO;
import org.apache.ignite.lang.IgniteInClosure;

/**
 *
 */
public abstract class AbstractDataInnerIO extends BPlusInnerIO<CacheSearchRow> implements RowLinkIO {
    /**
     * @param type Page type.
     * @param ver Page format version.
     * @param canGetRow If we can get full row from this page.
     * @param itemSize Single item size on page.
     */
    protected AbstractDataInnerIO(int type, int ver, boolean canGetRow, int itemSize) {
        super(type, ver, canGetRow, itemSize);
    }

    /** {@inheritDoc} */
    @Override public void storeByOffset(long pageAddr, int off, CacheSearchRow row) {
        assert row.link() != 0;

        PageUtils.putLong(pageAddr, off, row.link());
        off += 8;

        PageUtils.putInt(pageAddr, off, row.hash());
        off += 4;

        if (storeCacheId()) {
            assert row.cacheId() != GridCacheUtils.UNDEFINED_CACHE_ID : row;

            PageUtils.putInt(pageAddr, off, row.cacheId());
            off += 4;
        }

        if (storeMvccVersion()) {
            assert row.mvccUpdateTopologyVersion() > 0 : row;
            assert row.mvccUpdateCounter() != TxMvccVersion.COUNTER_NA : row;

            PageUtils.putLong(pageAddr, off, row.mvccUpdateTopologyVersion());
            off += 8;

            PageUtils.putLong(pageAddr, off, row.mvccUpdateCounter());
        }
    }

    /** {@inheritDoc} */
    @Override public CacheSearchRow getLookupRow(BPlusTree<CacheSearchRow, ?> tree, long pageAddr, int idx) {
        int cacheId = getCacheId(pageAddr, idx);
        int hash = getHash(pageAddr, idx);
        long link = getLink(pageAddr, idx);

        return ((CacheDataTree)tree).rowStore().keySearchRow(cacheId, hash, link);
    }

    /** {@inheritDoc} */
    @Override public void store(long dstPageAddr, int dstIdx, BPlusIO<CacheSearchRow> srcIo, long srcPageAddr,
                                int srcIdx) {
        int hash = ((RowLinkIO)srcIo).getHash(srcPageAddr, srcIdx);
        long link = ((RowLinkIO)srcIo).getLink(srcPageAddr, srcIdx);
        int off = offset(dstIdx);

        PageUtils.putLong(dstPageAddr, off, link);
        PageUtils.putInt(dstPageAddr, off + 8, hash);

        if (storeCacheId()) {
            int cacheId = ((RowLinkIO)srcIo).getCacheId(srcPageAddr, srcIdx);

            assert cacheId != GridCacheUtils.UNDEFINED_CACHE_ID;

            PageUtils.putInt(dstPageAddr, off + 12, cacheId);
        }
    }

    /** {@inheritDoc} */
    @Override public long getLink(long pageAddr, int idx) {
        assert idx < getCount(pageAddr) : idx;

        return PageUtils.getLong(pageAddr, offset(idx));
    }

    /** {@inheritDoc} */
    @Override public int getHash(long pageAddr, int idx) {
        return PageUtils.getInt(pageAddr, offset(idx) + 8);
    }

    /** {@inheritDoc} */
    @Override public void visit(long pageAddr, IgniteInClosure<CacheSearchRow> c) {
        int cnt = getCount(pageAddr);

        for (int i = 0; i < cnt; i++)
            c.apply(new CacheDataRowAdapter(getLink(pageAddr, i)));
    }

    /**
     * @return {@code True} if cache ID has to be stored.
     */
    protected abstract boolean storeCacheId();

    /**
     * @return {@code True} if mvcc version has to be stored.
     */
    protected abstract boolean storeMvccVersion();
}