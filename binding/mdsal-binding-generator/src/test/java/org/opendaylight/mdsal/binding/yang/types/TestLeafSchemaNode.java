/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Mock Leaf Schema Node designated to increase branch coverage in test cases.
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 */
public class TestLeafSchemaNode implements LeafSchemaNode {
    @Override
    public TypeDefinition<?> getType() {
        return null;
    }

    @Override
    @Deprecated
    public boolean isAugmenting() {
        return false;
    }

    @Override
    @Deprecated
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public Optional<Boolean> effectiveConfig() {
        return Optional.of(Boolean.FALSE);
    }

    @Override
    public QName getQName() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public Optional<? extends QualifiedBound> getWhenCondition() {
        return Optional.empty();
    }

    @Override
    public Collection<@NonNull MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public LeafEffectiveStatement asEffectiveStatement() {
        throw new UnsupportedOperationException();
    }
}
