/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;

/**
 * Utility class for implementing {@link DataNodeContainer}-type statements.
 */
@Beta
public abstract class AbstractDataNodeContainer<A, D extends DeclaredStatement> extends Default<A, D>
        implements DataNodeContainerMixin<A, D> {
    private final @NonNull ImmutableMap<QName, DataSchemaNode> dataChildren;
    private final @NonNull Object substatements;

    protected AbstractDataNodeContainer(final D declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.substatements = maskList(substatements);

        // Note: we do not leak this map, so iteration order does not matter
        final Map<QName, DataSchemaNode> tmp = new HashMap<>();

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DataSchemaNode) {
                final DataSchemaNode node = (DataSchemaNode) stmt;
                final QName id = node.getQName();
                final DataSchemaNode prev = tmp.put(id, node);
                if (prev != null) {
                    throw new SubstatementIndexingException(
                        "Cannot add child with name " + id + ", a conflicting child already exists");
                }
            }
        }

        dataChildren = ImmutableMap.copyOf(tmp);
    }

    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }

    @Override
    public final DataSchemaNode dataChildByName(final QName name) {
        return dataChildren.get(requireNonNull(name));
    }
}