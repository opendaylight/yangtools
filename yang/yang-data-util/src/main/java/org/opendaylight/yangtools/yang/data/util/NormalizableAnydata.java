/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
public interface NormalizableAnydata {
    /**
     * Attempt to interpret this anydata content in the context of specified tree and node.
     *
     * @param contextTree Data schema tree
     * @param contextNode Data schema node
     * @return Normalized anydata instance
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if this data cannot be interpreted in the requested context
     */
    NormalizedAnydata normalizeTo(DataSchemaContextTree contextTree, DataSchemaContextNode<?> contextNode);
}
