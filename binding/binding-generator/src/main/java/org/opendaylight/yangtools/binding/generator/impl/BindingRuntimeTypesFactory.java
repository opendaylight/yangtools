/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static com.google.common.base.Verify.verify;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.generator.impl.reactor.AbstractExplicitGenerator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.Generator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.yangtools.binding.generator.impl.reactor.IdentityGenerator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.ModuleGenerator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultBindingRuntimeTypes;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.yangtools.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BindingRuntimeTypesFactory implements Mutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeTypesFactory.class);

    // Modules, indexed by their QNameModule
    private final Map<QNameModule, ModuleRuntimeType> modules = new HashMap<>();
    // Identities, indexed by their QName
    private final Map<QName, IdentityRuntimeType> identities = new HashMap<>();
    // All known types, indexed by their JavaTypeName
    private final Map<JavaTypeName, RuntimeType> allTypes = new HashMap<>();
    // All known 'choice's to their corresponding cases
    private final SetMultimap<JavaTypeName, CaseRuntimeType> choiceToCases = HashMultimap.create();

    private BindingRuntimeTypesFactory() {
        // Hidden on purpose
    }

    static @NonNull BindingRuntimeTypes createTypes(final @NonNull EffectiveModelContext modelContext) {
        final var moduleGens = new GeneratorReactor(modelContext).execute(TypeBuilderFactory.runtime());

        final var sw = Stopwatch.createStarted();
        final var factory = new BindingRuntimeTypesFactory();
        factory.indexModules(moduleGens);
        LOG.debug("Indexed {} generators in {}", moduleGens.size(), sw);

        return new DefaultBindingRuntimeTypes(modelContext, factory.modules, factory.allTypes, factory.identities,
            factory.choiceToCases);
    }

    private void indexModules(final Map<QNameModule, ModuleGenerator> moduleGens) {
        for (var entry : moduleGens.entrySet()) {
            final var modGen = entry.getValue();

            // index the module's runtime type
            safePut(modules, "modules", entry.getKey(), modGen.getRuntimeType());

            // index module's identities and RPC input/outputs
            for (var gen : modGen) {
                if (gen instanceof IdentityGenerator idGen) {
                    safePut(identities, "identities", idGen.statement().argument(), idGen.getRuntimeType());
                }
            }
        }

        indexRuntimeTypes(moduleGens.values());
    }

    private void indexRuntimeTypes(final Iterable<? extends Generator> generators) {
        for (var gen : generators) {
            if (gen instanceof AbstractExplicitGenerator<?, ?> explicit) {
                final var type = explicit.generatedRuntimeType();
                if (type != null && type.javaType() instanceof GeneratedType genType) {
                    final var name = genType.getIdentifier();
                    final var prev = allTypes.put(name, type);
                    verify(prev == null || prev == type, "Conflict on runtime type mapping of %s between %s and %s",
                        name, prev, type);

                    // Global indexing of cases generated for a particular choice. We look at the Generated type
                    // and make assumptions about its shape -- which works just fine without touching
                    // the ChoiceRuntimeType for cases.
                    if (type instanceof CaseRuntimeType caseType) {
                        final var ifaces = genType.getImplements();
                        // The appropriate choice and DataObject at the very least. The choice interface is the first
                        // one mentioned.
                        verify(ifaces.size() >= 2, "Unexpected implemented interfaces %s", ifaces);
                        choiceToCases.put(ifaces.getFirst().getIdentifier(), caseType);
                    }
                }
            }
            indexRuntimeTypes(gen);
        }
    }

    private static <K, V> void safePut(final Map<K, V> map, final String name, final K key, final V value) {
        final var prev = map.put(key, value);
        verify(prev == null, "Conflict in %s, key %s conflicts on %s versus %s", name, key, prev, value);
    }
}
