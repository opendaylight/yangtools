/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * A CaseSchemaNode for implicit cases.
 *
 * @deprecated This class is no longer used as all implicit statements are handled transparently.
 */
// FIXME: hide this somewhere
@Deprecated
public final class ImplicitCaseSchemaNode implements CaseSchemaNode, DerivableSchemaNode {

    private final @NonNull DataSchemaNode caseShorthandNode;
    private final @Nullable CaseSchemaNode original;
    private final @NonNull SchemaPath path;
    private final boolean augmenting;

    public ImplicitCaseSchemaNode(final DataSchemaNode caseShorthandNode) {
        this.caseShorthandNode = requireNonNull(caseShorthandNode);
        this.path = requireNonNull(caseShorthandNode.getPath().getParent());
        this.original = getOriginalIfPresent(caseShorthandNode);

        // We need to cache this, as it will be reset
        this.augmenting = caseShorthandNode.isAugmenting();
    }

    @Deprecated
    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Deprecated
    @Override
    public boolean isAddedByUses() {
        return caseShorthandNode.isAddedByUses();
    }

    @Override
    public boolean isConfiguration() {
        return caseShorthandNode.isConfiguration();
    }

    @Override
    public QName getQName() {
        return caseShorthandNode.getQName();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public Optional<String> getDescription() {
        return caseShorthandNode.getDescription();
    }

    @Override
    public Optional<String> getReference() {
        return caseShorthandNode.getReference();
    }

    @Override
    public Status getStatus() {
        return caseShorthandNode.getStatus();
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return ImmutableSet.of();
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return ImmutableList.of(caseShorthandNode);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return name.equals(getQName()) ? Optional.of(caseShorthandNode) : Optional.empty();
    }

    @Override
    public Set<UsesNode> getUses() {
        return ImmutableSet.of();
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<? extends SchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return caseShorthandNode.getWhenCondition();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
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
        ImplicitCaseSchemaNode other = (ImplicitCaseSchemaNode) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        return ImplicitCaseSchemaNode.class.getSimpleName() + "[" + "qname=" + getQName() + "]";
    }

    private static CaseSchemaNode getOriginalIfPresent(final SchemaNode caseShorthandNode) {
        if (caseShorthandNode instanceof DerivableSchemaNode) {
            final Optional<? extends SchemaNode> original = ((DerivableSchemaNode) caseShorthandNode).getOriginal();
            if (original.isPresent()) {
                return new ImplicitCaseSchemaNode((DataSchemaNode) original.get());
            }
        }
        return null;
    }
}
