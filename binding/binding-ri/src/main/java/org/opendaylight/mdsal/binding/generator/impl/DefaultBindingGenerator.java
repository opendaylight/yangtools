/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.generator.BindingGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.Generator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Default implementation of {@link BindingGenerator}.
 */
@MetaInfServices
// Note: not exposed in DI on purpose, as this should only be needed at compile-time
public final class DefaultBindingGenerator implements BindingGenerator {
    @Override
    public List<GeneratedType> generateTypes(final EffectiveModelContext context,
            final Collection<? extends Module> modules) {
        return generateFor(context, modules);
    }

    @VisibleForTesting
    static @NonNull List<GeneratedType> generateFor(final EffectiveModelContext context) {
        return generateFor(context, context.getModules());
    }

    /**
     * Resolves generated types from {@code context} schema nodes only for modules specified in {@code modules}.
     * Generated types are created for modules, groupings, types, containers, lists, choices, augments, rpcs,
     * notification, identities and actions.
     *
     * @param context schema context which contains data about all schema nodes saved in modules
     * @param modules set of modules for which schema nodes should be generated types
     * @return list of types (usually a {@link GeneratedType} or an {@link GeneratedTransferObject}), which:
     *         <ul>
     *           <li>are generated from {@code context} schema nodes and</li>
     *           <li>are also part of some of the module in {@code modules} set.</li>
     *         </ul>
     * @throws NullPointerException if any argument is {@code null}, or if {@code modules} contains a {@code null}
     *                              element
     */
    @VisibleForTesting
    static @NonNull List<GeneratedType> generateFor(final EffectiveModelContext context,
            final Collection<? extends Module> modules) {
        final var filter = modules.stream().map(Module::asEffectiveStatement)
            .collect(Collectors.toUnmodifiableSet());

        final var result = new ArrayList<GeneratedType>();
        for (var gen : new GeneratorReactor(context).execute(TypeBuilderFactory.codegen()).values()) {
            if (filter.contains(gen.statement())) {
                addTypes(result, gen);
            }
        }

        return result;
    }

    private static void addTypes(final List<GeneratedType> result, final Generator gen) {
        final var type = gen.generatedType();
        if (type != null) {
            if (type.getIdentifier().immediatelyEnclosingClass().isEmpty()) {
                result.add(type);
            }
        }

        result.addAll(gen.auxiliaryGeneratedTypes());
        for (var child : gen) {
            addTypes(result, child);
        }
    }
}
