/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.AbstractEffectiveStatementInference.WithPath;

/**
 * Default implementation of a a {@link SchemaTreeInference}. Guaranteed to be consistent with its
 * {@link #getEffectiveModelContext()}.
 */
@Beta
@NonNullByDefault
public final class DefaultSchemaTreeInference extends WithPath<SchemaTreeEffectiveStatement<?>>
        implements SchemaTreeInference {
    private DefaultSchemaTreeInference(final EffectiveModelContext modelContext,
            final ImmutableList<SchemaTreeEffectiveStatement<?>> path) {
        super(modelContext, path);
    }

    /**
     * Create a new instance.
     *
     * @param modelContext Associated {@link EffectiveModelContext}
     * @param path An absolute schema node identifier
     * @return A new instance
     */
    public static DefaultSchemaTreeInference of(final EffectiveModelContext modelContext, final Absolute path) {
        final List<QName> steps = path.getNodeIdentifiers();
        final QName first = steps.get(0);
        final ModuleEffectiveStatement module = modelContext.findModuleStatement(first.getModule()).orElseThrow(
            () -> new IllegalArgumentException("No module for " + first));

        final ImmutableList.Builder<SchemaTreeEffectiveStatement<?>> builder =
            ImmutableList.builderWithExpectedSize(steps.size());
        SchemaTreeAwareEffectiveStatement<?, ?> parent = module;
        final Iterator<QName> it = steps.iterator();
        while (true) {
            final QName qname = it.next();
            final SchemaTreeEffectiveStatement<?> found = parent.findSchemaTreeNode(qname).orElseThrow(
                () -> new IllegalArgumentException("Cannot resolve step " + qname + " in " + builder.build()));
            builder.add(found);

            if (it.hasNext()) {
                checkArgument(found instanceof SchemaTreeAwareEffectiveStatement, "Cannot resolve steps %s past %s",
                    steps, found);
                parent = (SchemaTreeAwareEffectiveStatement<?, ?>) found;
            } else {
                break;
            }
        }
        return new DefaultSchemaTreeInference(modelContext, builder.build());
    }

    public static DefaultSchemaTreeInference unsafeOf(final EffectiveModelContext modelContext,
            final ImmutableList<SchemaTreeEffectiveStatement<?>> path) {
        String prop = System.getProperty("org.opendaylight.yangtools.yang.model.spi.validation", "unsafe");
        if (prop.equals("safe")) {
            var absolute = Absolute.of(ImmutableList.<QName>builderWithExpectedSize(path.size())
                    .addAll(Iterators.transform(path.stream().iterator(), ModelStatement::argument))
                    .build());
            return of(modelContext, absolute);
        }
        return new DefaultSchemaTreeInference(modelContext, path);
    }
}
