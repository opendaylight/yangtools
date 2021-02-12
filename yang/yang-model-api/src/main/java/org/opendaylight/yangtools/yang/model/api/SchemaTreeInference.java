/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * An {@link EffectiveStatementInference} consisting purely of steps along the {@code schema tree} axis, so that it
 * represents a {@code schema tree node} based on resolution of {@code absolute-schema-nodeid} as specified by
 * <a href="https://tools.ietf.org/html/rfc7950#section-6.5">RFC7950 section 6.5</a>.
 */
public interface SchemaTreeInference extends EffectiveStatementInference {
    /**
     * {@inheritDoc}
     *
     * <p>
     * The statement path is always composed on {@link SchemaTreeEffectiveStatement}s and contains at least one element.
     * The path is ordered from conceptual YANG root, i.e. the first element corresponds to the first element in
     * {@link SchemaNodeIdentifier.Absolute#firstNodeIdentifier()}.
     */
    @Override
    List<@NonNull SchemaTreeEffectiveStatement<?>> statementPath();

    /**
     * Return the {@link SchemaNodeIdentifier.Absolute} which resulted in this inference.
     *
     * @implSpec
     *      Default implementation interprets {@link #statementPath()}'s arguments as the ordered source of
     *      {@link SchemaNodeIdentifier.Absolute} steps.
     *
     * @return An absolute SchemaNodeIdentifier
     */
    default SchemaNodeIdentifier.Absolute toSchemaNodeIdentifier() {
        return SchemaNodeIdentifier.Absolute.of(statementPath().stream()
            .map(SchemaTreeEffectiveStatement::argument)
            .collect(ImmutableList.toImmutableList()));
    }
}
