/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizableAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * An unresolved child within a mount point. This is similar in functionality to {@link NormalizableAnydata}, but
 * rather than normalizing, the data is fed into a combination of a SchemaContext and NormalizedNodeStreamWriter.
 */
@Beta
@NonNullByDefault
public interface MountPointChild {
    /**
     * Stream this child into a writer, with the help of a SchemaContext.
     *
     * @param writer Writer to emit the child into
     * @param mountCtx MountPointContext for normalization purposes
     * @throws IOException if an underlying error occurs
     * @throws NullPointerException if any of the arguments is null
     */
    void writeTo(NormalizedNodeStreamWriter writer, MountPointContext mountCtx) throws IOException;

    /**
     * Normalized this child to a particular SchemaContext.
     *
     * @param schemaContext SchemaContext for normalization purposes
     * @return A NormalizedNode representation of this child
     * @throws IOException if an underlying error occurs
     * @throws NullPointerException if any of the arguments is null
     */
    NormalizedNode<?, ?> normalizeTo(SchemaContext schemaContext) throws IOException;
}
