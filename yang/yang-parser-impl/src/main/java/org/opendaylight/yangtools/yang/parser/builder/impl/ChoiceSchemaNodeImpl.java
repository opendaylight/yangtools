/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
final class ChoiceSchemaNodeImpl implements ChoiceSchemaNode, DerivableSchemaNode {
    private final QName qname;
    private final SchemaPath path;
    String description;
    String reference;
    Status status;
    boolean augmenting;
    boolean addedByUses;
    ChoiceSchemaNode original;
    boolean configuration;
    ConstraintDefinition constraints;
    ImmutableSet<ChoiceCaseNode> cases;
    ImmutableSet<AugmentationSchema> augmentations;
    ImmutableList<UnknownSchemaNode> unknownNodes;
    String defaultCase;

    ChoiceSchemaNodeImpl(final QName qname, final SchemaPath path) {
        this.qname = qname;
        this.path = path;
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public Optional<ChoiceSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return constraints;
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return augmentations;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public Set<ChoiceCaseNode> getCases() {
        return cases;
    }

    @Override
    public ChoiceCaseNode getCaseNodeByName(final QName name) {
        if (name == null) {
            throw new IllegalArgumentException("Choice Case QName cannot be NULL!");
        }
        for (final ChoiceCaseNode caseNode : cases) {
            if (caseNode != null && name.equals(caseNode.getQName())) {
                return caseNode;
            }
        }
        return null;
    }

    @Override
    public ChoiceCaseNode getCaseNodeByName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Choice Case string Name cannot be NULL!");
        }
        for (final ChoiceCaseNode caseNode : cases) {
            if (caseNode != null && (caseNode.getQName() != null)
                    && name.equals(caseNode.getQName().getLocalName())) {
                return caseNode;
            }
        }
        return null;
    }

    @Override
    public String getDefaultCase() {
        return defaultCase;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(path);
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
        ChoiceSchemaNodeImpl other = (ChoiceSchemaNodeImpl) obj;
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ChoiceSchemaNodeImpl.class.getSimpleName() + "[" +
                "qname=" + qname +
                "]";
    }

}
