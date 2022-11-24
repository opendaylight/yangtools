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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.AbstractExplicitGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.Generator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.mdsal.binding.generator.impl.reactor.IdentityGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.InputGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.ModuleGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.OutputGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.RpcGenerator;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultBindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
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
    // All RpcOutputs, indexed by their RPC's QName
    private final Map<QName, OutputRuntimeType> rpcOutputs = new HashMap<>();
    // All RpcInputs, indexed by their RPC's QName
    private final Map<QName, InputRuntimeType> rpcInputs = new HashMap<>();
    // All known 'choice's to their corresponding cases
    private final SetMultimap<JavaTypeName, CaseRuntimeType> choiceToCases = HashMultimap.create();

    private BindingRuntimeTypesFactory() {
        // Hidden on purpose
    }

    static @NonNull BindingRuntimeTypes createTypes(final @NonNull EffectiveModelContext context) {
        final var moduleGens = new GeneratorReactor(context).execute(TypeBuilderFactory.runtime());

        final Stopwatch sw = Stopwatch.createStarted();
        final BindingRuntimeTypesFactory factory = new BindingRuntimeTypesFactory();
        factory.indexModules(moduleGens);
        LOG.debug("Indexed {} generators in {}", moduleGens.size(), sw);

        return new DefaultBindingRuntimeTypes(context, factory.modules, factory.allTypes, factory.identities,
            factory.rpcInputs, factory.rpcOutputs, factory.choiceToCases);
    }

    private void indexModules(final Map<QNameModule, ModuleGenerator> moduleGens) {
        for (var entry : moduleGens.entrySet()) {
            final var modGen = entry.getValue();

            // index the module's runtime type
            safePut(modules, "modules", entry.getKey(), modGen.runtimeType().orElseThrow());

            // index module's identities and RPC input/outputs
            for (var gen : modGen) {
                if (gen instanceof IdentityGenerator idGen) {
                    idGen.runtimeType().ifPresent(identity -> {
                        safePut(identities, "identities", identity.statement().argument(), identity);
                    });
                }
                // FIXME: do not collect these once we they generate a proper RuntimeType
                if (gen instanceof RpcGenerator rpcGen) {
                    final QName rpcName = rpcGen.statement().argument();
                    for (var subgen : gen) {
                        if (subgen instanceof InputGenerator inputGen) {
                            inputGen.runtimeType().ifPresent(input -> rpcInputs.put(rpcName, input));
                        } else if (subgen instanceof OutputGenerator outputGen) {
                            outputGen.runtimeType().ifPresent(output -> rpcOutputs.put(rpcName, output));
                        }
                    }
                }
            }
        }

        indexRuntimeTypes(moduleGens.values());
    }

    private void indexRuntimeTypes(final Iterable<? extends Generator> generators) {
        for (Generator gen : generators) {
            if (gen instanceof AbstractExplicitGenerator<?, ?> explicitGen && gen.generatedType().isPresent()) {
                final var type = explicitGen.runtimeType().orElseThrow();
                if (type.javaType() instanceof GeneratedType genType) {
                    final var name = genType.getIdentifier();
                    final var prev = allTypes.put(name, type);
                    verify(prev == null || prev == type, "Conflict on runtime type mapping of %s between %s and %s",
                        name, prev, type);

                    // Global indexing of cases generated for a particular choice. We look at the Generated type
                    // and make assumptions about its shape -- which works just fine without touching the
                    // ChoiceRuntimeType for cases.
                    if (type instanceof CaseRuntimeType caseType) {
                        final var ifaces = genType.getImplements();
                        // The appropriate choice and DataObject at the very least. The choice interface is the first
                        // one mentioned.
                        verify(ifaces.size() >= 2, "Unexpected implemented interfaces %s", ifaces);
                        choiceToCases.put(ifaces.get(0).getIdentifier(), caseType);
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
