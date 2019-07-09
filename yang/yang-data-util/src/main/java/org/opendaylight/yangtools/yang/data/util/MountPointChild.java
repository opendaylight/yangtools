/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Deque;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNormalizationException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizableAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A raw child of {@link MountPointData}. This is similar in functionality to {@link NormalizableAnydata}, but
 * additionally such objects must be able to resolve their namespace as a {@link QNameModule}.
 */
@Beta
@NonNullByDefault
public interface MountPointChild extends NormalizableAnydata {
    /**
     * Return the local name of this child.
     *
     * @return The local name
     */
    String getLocalName();

    /**
     * Normalize this node's namespace based on the contents of a SchemaContext.
     *
     * @param schemaContext SchemaContext to normalize to
     * @return A normalized namespace
     * @throws NullPointerException if schemaContext is null
     * @throws AnydataNormalizationException if the namespace cannot be interpreted in the requested context
     */
    QNameModule getNamespace(SchemaContext schemaContext) throws AnydataNormalizationException;

    /**
     *
     * @param schemaContext Schema context
     * @return Normalized anydata instance along with a schema path to the schema node
     * @throws NullPointerException if schemaContext is null
     * @throws AnydataNormalizationException if this data cannot be interpreted in the requested context
     */
    default Entry<NormalizedAnydata, Deque<DataSchemaNode>> normalizeTo(final SchemaContext schemaContext)
            throws AnydataNormalizationException {
        final QNameModule namespace = getNamespace(schemaContext);
        final Deque<DataSchemaNode> path = ParserStreamUtils.findSchemaNodeByNameAndNamespace(schemaContext,
            getLocalName(), namespace.getNamespace());
        if (path.isEmpty()) {
            throw new AnydataNormalizationException(
                "Failed to find context node for " + namespace + " node " + getLocalName());
        }

        return new SimpleImmutableEntry<>(normalizeTo(schemaContext, path.peek()), path);
    }
}
