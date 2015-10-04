/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

@Beta
public abstract class AbstractSchemaNodeBuilder<T extends SchemaNode> implements Mutable, Builder<T> {
    private List<UnknownSchemaNode> unknownSchemaNodes = Collections.emptyList();
    private SchemaPath path;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;

    public final AbstractSchemaNodeBuilder<T> setPath(SchemaPath path) {
        path = Preconditions.checkNotNull(path);
        return this;
    }

    public final AbstractSchemaNodeBuilder<T> setDescription(String description) {
        description = Preconditions.checkNotNull(description);
        return this;
    }

    public final AbstractSchemaNodeBuilder<T> setReference(final String reference) {
        this.reference = Preconditions.checkNotNull(reference);
        return this;
    }

    public final AbstractSchemaNodeBuilder<T> setStatus(final Status status) {
        this.status = Preconditions.checkNotNull(status);
        return this;
    }

    public final AbstractSchemaNodeBuilder<T> setUnknownSchemaNodes(final List<UnknownSchemaNode> unknownSchemaNodes) {
        this.unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodes);
        return this;
    }

    @Override
    public final T build() {
        return buildNode(path, status, description, reference, unknownSchemaNodes);
    }

    protected abstract T buildNode(SchemaPath path, Status status, String description, String reference,
            List<UnknownSchemaNode> unknownSchemaNodes);
}
