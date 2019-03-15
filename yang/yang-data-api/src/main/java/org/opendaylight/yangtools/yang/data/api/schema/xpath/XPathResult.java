/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Base interface for various things an XPath evaluation can return.
 *
 * @param <T> type of returned value
 */
@Beta
@Deprecated
// FIXME: do we want to support all the modes of
//        <a href="http://www.w3.org/TR/DOM-Level-3-XPath/xpath.html#XPathResultType">DOM XPath</a> ?
//        The default DataTree (yang-data-impl) implementation can support ORDERED_NODE_SNAPSHOT_TYPE. The clustered
//        datastore may want to implement ORDERED_NODE_ITERATOR_TYPE (via iterators).
public interface XPathResult<T> {
    /**
     * Get the value contained in this result.
     *
     * @return Result value
     */
    @NonNull T getValue();
}
