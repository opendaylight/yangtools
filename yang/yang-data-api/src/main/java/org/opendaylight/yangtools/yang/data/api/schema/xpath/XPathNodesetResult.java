/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An {@link XPathResult} containing a set of nodes. Resulting nodes are identified by their
 * {@link YangInstanceIdentifier}.
 */
@Beta
@Deprecated
public interface XPathNodesetResult
    extends XPathResult<Collection<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>>> {

}
