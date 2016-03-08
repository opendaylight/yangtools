/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainer;

/**
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
final class AugmentationSchemaImpl extends AbstractDocumentedDataNodeContainer implements AugmentationSchema, NamespaceRevisionAware, Comparable<AugmentationSchemaImpl> {
    private final int order;
    private final SchemaPath targetPath;
    RevisionAwareXPath whenCondition;

    URI namespace;
    Date revision;
    ImmutableList<UnknownSchemaNode> unknownNodes;
    private AugmentationSchema copyOf;

    public AugmentationSchemaImpl(final SchemaPath targetPath, final int order, final AugmentationSchemaBuilderImpl builder) {
        super(builder);
        this.targetPath = targetPath;
        this.order = order;
    }

    public void setCopyOf(final AugmentationSchema build) {
        this.copyOf = build;
    }

    @Override
    public Optional<AugmentationSchema> getOriginalDefinition() {
        return Optional.fromNullable(this.copyOf);
    }

    @Override
    public SchemaPath getTargetPath() {
        return targetPath;
    }

    @Override
    public RevisionAwareXPath getWhenCondition() {
        return whenCondition;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public URI getNamespace() {
        return namespace;
    }

    @Override
    public Date getRevision() {
        return revision;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + Objects.hashCode(targetPath);
        result = prime * result + Objects.hashCode(whenCondition);
        result = prime * result + getChildNodes().hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AugmentationSchemaImpl other = (AugmentationSchemaImpl) obj;
        if (targetPath == null) {
            if (other.targetPath != null) {
                return false;
            }
        } else if (!targetPath.equals(other.targetPath)) {
            return false;
        }
        if (whenCondition == null) {
            if (other.whenCondition != null) {
                return false;
            }
        } else if (!whenCondition.equals(other.whenCondition)) {
            return false;
        }
        if (!getChildNodes().equals(other.getChildNodes())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return AugmentationSchemaImpl.class.getSimpleName() + "[" +
                "targetPath=" + targetPath +
                ", when=" + whenCondition +
                "]";
    }

    @Override
    public int compareTo(final AugmentationSchemaImpl o) {
        checkNotNull(o);
        Iterator<QName> thisIt = this.targetPath.getPathFromRoot().iterator();
        Iterator<QName> otherIt = o.getTargetPath().getPathFromRoot().iterator();
        while (thisIt.hasNext()) {
            if (otherIt.hasNext()) {
                int comp = thisIt.next().compareTo(otherIt.next());
                if (comp != 0) {
                    return comp;
                }
            } else {
                return 1;
            }
        }
        if (otherIt.hasNext()) {
            return -1;
        }
        return this.order - o.order;
    }
}
