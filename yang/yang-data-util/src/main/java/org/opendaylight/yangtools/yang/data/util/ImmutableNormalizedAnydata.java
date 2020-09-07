/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.AbstractEffectiveModelContextProvider;

@Beta
@NonNullByDefault
public class ImmutableNormalizedAnydata extends AbstractEffectiveModelContextProvider implements NormalizedAnydata {
    private final DataSchemaNode contextNode;
    private final NormalizedNode<?, ?> data;

    public ImmutableNormalizedAnydata(final EffectiveModelContext schemaContext, final DataSchemaNode contextNode,
            final NormalizedNode<?, ?> data) {
        super(schemaContext);
        this.contextNode = requireNonNull(contextNode);
        this.data = requireNonNull(data);
    }

    @Override
    public final DataSchemaNode getContextNode() {
        return contextNode;
    }

    @Override
    public final NormalizedNode<?, ?> getData() {
        return data;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("node", contextNode).add("data", data);
    }
}
