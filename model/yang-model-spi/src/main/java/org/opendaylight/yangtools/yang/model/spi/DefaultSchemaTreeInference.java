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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.AbstractEffectiveStatementInference.WithPath;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a {@link SchemaTreeInference}. Guaranteed to be consistent with its
 * {@link #modelContext()}.
 */
@Beta
@NonNullByDefault
public final class DefaultSchemaTreeInference extends WithPath<SchemaTreeEffectiveStatement<?>>
        implements SchemaTreeInference {
    private static final String VERIFY_UNSAFE_PROP =
        "org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference.verifyUnsafeOf";
    private static final boolean VERIFY_UNSAFE = Boolean.getBoolean(VERIFY_UNSAFE_PROP);

    static {
        if (VERIFY_UNSAFE) {
            LoggerFactory.getLogger(DefaultSchemaTreeInference.class)
                .info("DefaultSchemaTreeInference.unsafeOf() arguments are being verified");
        }
    }

    private DefaultSchemaTreeInference(final EffectiveModelContext modelContext,
            final ImmutableList<? extends SchemaTreeEffectiveStatement<?>> path) {
        super(modelContext, path);
    }

    /**
     * Create a new instance based on an {@link EffectiveModelContext} and an {@link Absolute} schema node identifier.
     *
     * @param modelContext Associated {@link EffectiveModelContext}
     * @param path An absolute schema node identifier
     * @return A new instance
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the provided {@code path} cannot be resolved in {@code modelContext}
     */
    public static DefaultSchemaTreeInference of(final EffectiveModelContext modelContext, final Absolute path) {
        return new DefaultSchemaTreeInference(modelContext, resolveSteps(modelContext, path.getNodeIdentifiers()));
    }

    /**
     * Create a new instance based on an {@link EffectiveModelContext} and a resolved sequence of statements. Provided
     * statements are expected to have been produced in a validated manner and are normally trusted to be accurate.
     *
     * <p>
     * Run-time verification of {@code path} can be enabled by setting the {@value #VERIFY_UNSAFE_PROP} system property
     * to {@code true}.
     *
     * @param modelContext Associated {@link EffectiveModelContext}
     * @param path Resolved statement path
     * @return A new instance
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code path} is empty or when verification is enabled and the {@code path}
     *                                  does not match the {@code modelContext}'s schema tree
     */
    public static DefaultSchemaTreeInference unsafeOf(final EffectiveModelContext modelContext,
            final ImmutableList<? extends SchemaTreeEffectiveStatement<?>> path) {
        checkArgument(!path.isEmpty(), "Path must not be empty");
        return VERIFY_UNSAFE ? verifiedOf(modelContext, path) : new DefaultSchemaTreeInference(modelContext, path);
    }

    @VisibleForTesting
    static DefaultSchemaTreeInference verifiedOf(final EffectiveModelContext modelContext,
            final ImmutableList<? extends SchemaTreeEffectiveStatement<?>> path) {
        final var resolved = resolveSteps(modelContext, Lists.transform(path, SchemaTreeEffectiveStatement::argument));
        checkArgument(path.equals(resolved), "Provided path %s is not consistent with resolved path %s", path,
            resolved);
        return new DefaultSchemaTreeInference(modelContext, path);
    }

    private static ImmutableList<SchemaTreeEffectiveStatement<?>> resolveSteps(final EffectiveModelContext modelContext,
            final List<QName> steps) {
        final var first = steps.get(0);
        final var module = modelContext.findModuleStatement(first.getModule()).orElseThrow(
            () -> new IllegalArgumentException("No module for " + first));

        final var builder = ImmutableList.<SchemaTreeEffectiveStatement<?>>builderWithExpectedSize(steps.size());
        SchemaTreeAwareEffectiveStatement<?, ?> parent = module;
        final Iterator<QName> it = steps.iterator();
        while (true) {
            final var qname = it.next();
            final var found = parent.findSchemaTreeNode(qname).orElseThrow(
                () -> new IllegalArgumentException("Cannot resolve step " + qname + " in " + builder.build()));
            builder.add(found);

            if (!it.hasNext()) {
                break;
            }

            checkArgument(found instanceof SchemaTreeAwareEffectiveStatement, "Cannot resolve steps %s past %s", steps,
                found);
            parent = (SchemaTreeAwareEffectiveStatement<?, ?>) found;
        }

        return builder.build();
    }
}
