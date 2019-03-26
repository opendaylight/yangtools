/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;

/**
 * {@link DataTree} type, specifying which YANG modeled content is valid with a data tree. This reflects
 * <a href="https://tools.ietf.org/html/rfc6020#section-7.21.1">RFC6020</a>/
 * <a href="https://tools.ietf.org/html/rfc6020#section-7.19.1">RFC7950</a> data combinations based on {@code config}
 * statement.
 */
// FIXME: 4.0.0: Consider defining a 'config false'-only type
// FIXME: 4.0.0: Consider renaming this enum
@Beta
public enum TreeType {
    /**
     * Only {@code config true} nodes are allowed. This corresponds, but is not limited, to @{code candidate},
     * {@code startup} and {@code running} data stores defined in
     * <a href="https://tools.ietf.org/html/rfc8342#section-4.1">RFC8342 section 4.1</a> as well as {@code intended}
     * data store defined in <a href="https://tools.ietf.org/html/rfc8342#section-5">RFC8342 section 4.1</a>.
     */
    CONFIGURATION,
    /**
     * Only {@code config true} and {@code config false} nodes are allowed. This corresponds, but is not limited, to
     * {@code operational state} defined in
     * <a href="https://tools.ietf.org/html/rfc8342#section-4.1">RFC8342 section 4.1</a> as well as {@code operational}
     * data store defined in <a href="https://tools.ietf.org/html/rfc8342#section-5">RFC8342 section 4.1</a>.
     */
    OPERATIONAL,
}
