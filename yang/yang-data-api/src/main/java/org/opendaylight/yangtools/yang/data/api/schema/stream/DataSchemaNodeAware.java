/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Mixin interface for {@link NormalizedNodeStreamWriter} allowing callers to inform the writer of the
 * {@link DataSchemaNode} corresponding to the next node which will either be started or emitted. This interface should
 * not be implemented directly.
 */
@Beta
public interface DataSchemaNodeAware {
    /**
     * Attach the specified {@link DataSchemaNode} to the next node which will get started or emitted.
     *
     * @param schema DataSchemaNode
     * @throws NullPointerException if the argument is null
     */
    void nextDataSchemaNode(@Nonnull DataSchemaNode schema);
}
