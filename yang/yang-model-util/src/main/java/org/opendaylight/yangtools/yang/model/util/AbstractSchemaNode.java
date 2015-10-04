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
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

@Beta
public abstract class AbstractSchemaNode implements SchemaNode {
    private final List<UnknownSchemaNode> unknownNodes;
    private final String description;
    private final String reference;
    private final SchemaPath path;
    private final Status status;

    protected AbstractSchemaNode(final SchemaPath path, final String description, final String reference, final Status status,
            final List<UnknownSchemaNode> unknownNodes) {
        this.path = Preconditions.checkNotNull(path);
        this.description = description;
        this.reference = reference;
        this.status = Preconditions.checkNotNull(status);
        this.unknownNodes = ImmutableList.copyOf(unknownNodes);
    }

    @Override
    public final QName getQName() {
        return path.getLastComponent();
    }

    @Override
    public final SchemaPath getPath() {
        return path;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final String getReference() {
        return reference;
    }

    @Override
    public final Status getStatus() {
        return status;
    }

    @Override
    public final List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(path);
        result = prime * result + Objects.hashCode(unknownNodes);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !AbstractSchemaNode.class.equals(obj.getClass())) {
            return false;
        }
        AbstractSchemaNode other = (AbstractSchemaNode) obj;
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        if (!Objects.equals(unknownNodes, other.unknownNodes)) {
            return false;
        }
        return true;
    }
}
