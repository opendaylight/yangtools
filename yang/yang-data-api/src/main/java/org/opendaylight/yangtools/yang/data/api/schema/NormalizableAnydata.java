/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * An {@link AnydataNode#getValueObjectModel() anydata value object model} which can be normalized to
 * {@link NormalizedAnydata} when provided with proper context.
 */
@Beta
@NonNullByDefault
public interface NormalizableAnydata {
    /**
     * Attempt to interpret this anydata content in the context of specified tree and node.
     *
     * @param schemaContext Schema context
     * @param contextNode Corresponding schema node
     * @return Normalized anydata instance
     * @throws NullPointerException if any argument is null
     * @throws AnydataNormalizationException if this data cannot be interpreted in the requested context
     */
    NormalizedAnydata normalizeTo(EffectiveModelContext schemaContext, DataSchemaNode contextNode)
            throws AnydataNormalizationException;
}
