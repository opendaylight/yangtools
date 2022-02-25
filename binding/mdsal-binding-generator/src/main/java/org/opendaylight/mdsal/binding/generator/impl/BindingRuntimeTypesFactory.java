/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Verify.verify;

import com.google.common.base.Stopwatch;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.AbstractExplicitGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.Generator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.mdsal.binding.generator.impl.reactor.ModuleGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BindingRuntimeTypesFactory implements Mutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeTypesFactory.class);

    private final Map<Type, AugmentationSchemaNode> augmentationToSchema = new HashMap<>();
    private final Map<Type, WithStatus> typeToSchema = new HashMap<>();
    private final Map<QName, Type> identities = new HashMap<>();

    // Note: we are keying through WithStatus, but these nodes compare on semantics, so equivalent schema nodes
    //       can result in two distinct types. We certainly need to keep them separate.
    private final Map<WithStatus, Type> schemaToType = new IdentityHashMap<>();

    private BindingRuntimeTypesFactory() {
        // Hidden on purpose
    }

    static @NonNull BindingRuntimeTypes createTypes(final @NonNull EffectiveModelContext context) {
        final Collection<ModuleGenerator> moduleGens = new GeneratorReactor(context)
            .execute(TypeBuilderFactory.runtime())
            .values();

        final Stopwatch sw = Stopwatch.createStarted();
        final BindingRuntimeTypesFactory factory = new BindingRuntimeTypesFactory();
        factory.indexTypes(moduleGens);
        LOG.debug("Indexed {} generators in {}", moduleGens.size(), sw);

        return new DefaultBindingRuntimeTypes(context, factory.augmentationToSchema, factory.typeToSchema,
            factory.schemaToType, factory.identities);
    }

    private void indexTypes(final Iterable<? extends Generator> generators) {
        for (Generator gen : generators) {
            gen.generatedType().ifPresent(type -> indexType(gen, type));
            indexTypes(gen);
        }
    }

    private void indexType(final @NonNull Generator generator, final @NonNull GeneratedType type) {
        if (generator instanceof AbstractExplicitGenerator) {
            final EffectiveStatement<?, ?> stmt = ((AbstractExplicitGenerator<?>) generator).statement();
            if (stmt instanceof IdentityEffectiveStatement) {
                identities.put(((IdentityEffectiveStatement) stmt).argument(), type);
            } else if (stmt instanceof AugmentEffectiveStatement) {
                verify(stmt instanceof AugmentationSchemaNode, "Unexpected statement %s", stmt);
                augmentationToSchema.put(type, (AugmentationSchemaNode) stmt);
            }

            final WithStatus schema;
            if (stmt instanceof TypedDataSchemaNode) {
                schema = ((TypedDataSchemaNode) stmt).getType();
            } else if (stmt instanceof TypedefEffectiveStatement) {
                schema = ((TypedefEffectiveStatement) stmt).getTypeDefinition();
            } else if (stmt instanceof WithStatus) {
                schema = (WithStatus) stmt;
            } else {
                return;
            }

            typeToSchema.put(type, schema);
            final var prevType = schemaToType.put(schema, type);
            verify(prevType == null, "Conflicting types %s and %s on %s", type, prevType, schema);
        }
    }
}
