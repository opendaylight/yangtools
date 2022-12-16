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
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.generator.impl.reactor.AbstractExplicitGenerator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.Generator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.yangtools.binding.generator.impl.reactor.IdentityGenerator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.ModuleGenerator;
import org.opendaylight.yangtools.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultBindingRuntimeTypes;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.yangtools.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
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
    // All case to cases mapping, values are the cases that can substitute case that is the key
    private final Multimap<GeneratedType, CaseRuntimeType> caseToSubstitutionCases = HashMultimap.create();
    // All augment to augments mapping where values are augments that can substitute augment that is the key
    private final Multimap<GeneratedType, AugmentRuntimeType> augmentToSubstitutionAugments = HashMultimap.create();

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
            factory.choiceToCases, factory.caseToSubstitutionCases,
            factory.augmentToSubstitutionAugments);
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

        final Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildren = new HashMap<>();
        final Map<AugmentRuntimeType, List<EffectiveStatement<?, ?>>> augmentToChildren = new HashMap<>();
        indexRuntimeTypes(moduleGens.values(), caseToChildren, augmentToChildren);

        collectSubstsForCase(caseToChildren);
        collectSubstsForAugment(augmentToChildren);

    }

    private void indexRuntimeTypes(final Iterable<? extends Generator> generators,
            final Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildren,
            final Map<AugmentRuntimeType, List<EffectiveStatement<?, ?>>> augmentToChildren) {
        for (var gen : generators) {
            if (gen instanceof AbstractExplicitGenerator<?, ?> explicit) {
                final var type = explicit.generatedRuntimeType();
                // calling iterator() returns, in case of an AbstractCompositeGenerator, its childGenerators
                final var childGenIt = gen.iterator();
                if (type != null && type.javaType() instanceof GeneratedType genType) {
                    final var name = genType.getIdentifier();
                    final var prev = allTypes.put(name, type);
                    verify(prev == null || prev == type, "Conflict on runtime type mapping of %s between %s and %s",
                        name, prev, type);

                    // Global indexing of cases generated for a particular choice. We look at the Generated type
                    // and make assumptions about its shape -- which works just fine without touching
                    // the ChoiceRuntimeType for cases.
                    if (type instanceof CaseRuntimeType caseType) {
                        caseToChildren.put(caseType, generatorsToStatements(childGenIt));
                        final var ifaces = genType.getImplements();
                        // The appropriate choice and DataObject at the very least. The choice interface is the first
                        // one mentioned.
                        verify(ifaces.size() >= 2, "Unexpected implemented interfaces %s", ifaces);
                        choiceToCases.put(ifaces.getFirst().getIdentifier(), caseType);
                    }

                    if (type instanceof AugmentRuntimeType augmentType) {
                        augmentToChildren.put(augmentType, generatorsToStatements(childGenIt));
                    }
                }
            }
            indexRuntimeTypes(gen, caseToChildren, augmentToChildren);
        }
    }

    /**
     * This method maps {@link Iterator} of {@link Generator}s to {@link List} of {@link EffectiveStatement}s.
     *
     * @param iterator iterator over a collection of child generators
     * @return list of statements corresponding to the generators
     */
    private static List<EffectiveStatement<?, ?>> generatorsToStatements(final Iterator<Generator> iterator) {
        final var preResult = new ArrayList<Generator>();
        iterator.forEachRemaining(preResult::add);
        return preResult.stream()
            .filter(AbstractExplicitGenerator.class::isInstance)
            .map(gen -> ((AbstractExplicitGenerator<?, ?>) gen).statement())
            .collect(Collectors.toList());
    }

    /**
     * This method populates {@code caseToSubstitutionCases} that maps each case to its possible substitutions.
     *
     * @param caseToChildrenStmts map of case to its corresponding children
     */
    private void collectSubstsForCase(final Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildrenStmts) {
        final var localToSubstitutions = HashMultimap.<GeneratedType, CaseRuntimeType>create();
        for (final var entry : choiceToCases.entries()) {
            final var choice = entry.getKey();
            // CaseRuntimeTypes associated with this choice
            final var caseTypes = choiceToCases.get(choice);

            for (final var caseType : caseTypes) {
                addSubstitutionalCases(localToSubstitutions, caseType, caseTypes, caseToChildrenStmts);
            }
        }
        caseToSubstitutionCases.putAll(localToSubstitutions);
    }

    /**
     * Update substitutions. Put to map:
     * <ul>
     *  <li>key - {@link GeneratedType} - the {@link CaseRuntimeType#javaType} we want to substitute</li>
     *  <li>value - {@link CaseRuntimeType} - the type that can be used as a substitution</li>
     * </ul>
     *
     * <p>Two cases could be substituted if and only if:
     * <ul>
     *   <li>Both implements same interfaces</li>
     *   <li>Both have same children</li>
     *   <li>Both are from same choice. Choice class was generated for data node in grouping.</li>
     * </ul>
     *
     * <p>The last condition can be ignored in this case, since the {@code candidates} are extracted
     * from {@code choiceToCases}, which already provides only cases from one particular choice (containing also
     * all the augmented cases belonging to that choice).
     *
     * @param localToSubstitutions mapping from case to all cases that could be used as a substitution
     * @param local                current {@link CaseRuntimeType} for which substitutions we are looking for
     * @param candidates           available cases from one particular choice
     */
    private static void addSubstitutionalCases(final Multimap<GeneratedType, CaseRuntimeType> localToSubstitutions,
            final CaseRuntimeType local, final Collection<CaseRuntimeType> candidates,
            final Map<CaseRuntimeType, List<EffectiveStatement<?, ?>>> caseToChildrenStmts) {
        for (final var candidate : candidates) {
            if (candidate.equals(local)) {
                continue;
            }
            boolean substitutional;
            // check if both have same interfaces
            substitutional = candidate.javaType().getImplements().equals(local.javaType().getImplements());
            // check if both have same children
            substitutional &= caseToChildrenStmts.get(candidate).equals(caseToChildrenStmts.get(local));

            // no need to check if they are from same choice
            if (substitutional) {
                localToSubstitutions.put(local.javaType(), candidate);
            }
        }
    }

    /**
     * This method populates the {@code augmentToSubstitutionAugments} map, that maps an augmentation to its possible
     * substitution augments.
     *
     * <p>Two augmentations could be substituted if and only if:
     * <ul>
     *   <li>Both implement same interfaces</li>
     *   <li>Both have same children</li>
     *   <li>Both target (augment) the same class</li>
     * </ul>
     *
     * @param augToChildrenStmts mapping augmentType to its children
     */
    private void collectSubstsForAugment(
            final Map<AugmentRuntimeType, List<EffectiveStatement<?, ?>>> augToChildrenStmts) {
        // let's use allStmts for now
        for (final var runtimeType : allTypes.values()) {
            // skip types that are not interesting
            if (!(runtimeType instanceof AugmentRuntimeType augmentType)) {
                continue;
            }
            for (final var entryAugToChildren : augToChildrenStmts.entrySet()) {
                // do not compare the same instance to itself
                if (augmentType.equals(entryAugToChildren.getKey())) {
                    continue;
                }
                final var substitution = entryAugToChildren.getKey();
                final var substitChildren = entryAugToChildren.getValue();
                final var substitIfaces = substitution.javaType().getImplements();

                final var runtimeTypeChildren = augToChildrenStmts.get(runtimeType);
                final var runtimeTypeIfaces = augmentType.javaType().getImplements();

                boolean substitutional;
                // check if both have same interfaces
                substitutional = substitIfaces.equals(runtimeTypeIfaces);
                // check if both have same children
                substitutional &= substitChildren.equals(runtimeTypeChildren);
                // the last check needs to verify if they have the same target class
                substitutional &= haveSameTarget(augmentType, substitution);

                // if the 'augmentType' can be substituted for 'substitution', add it to the map
                if (substitutional) {
                    augmentToSubstitutionAugments.put(augmentType.javaType(), substitution);
                }
            }
        }
    }

    /**
     * This method tells us, whether the two runtime types augment the same element.
     *
     * <p>One of the requirements for substitution is that the augmentations target the same class.
     *
     * @param type         {@link AugmentRuntimeType} type for whose substitutions we are looking
     * @param substitution Candidate that can be used as a substitution
     * @return whether the parameters augment the same element
     */
    private static boolean haveSameTarget(final AugmentRuntimeType type, final AugmentRuntimeType substitution) {
        final var typeAugmentation = getAugmentationInterface(type);
        if (typeAugmentation.isEmpty()) {
            LOG.error("AugmentRuntimeType {} does not have augmentation target (missing augmentation interface)", type);
            return false;
        }

        final var substitutionAugmentation = getAugmentationInterface(substitution);
        if (substitutionAugmentation.isEmpty()) {
            LOG.error("AugmentRuntimeType {} does not have augmentation target (missing augmentation interface)",
                substitution);
            return false;
        }

        // augmentation has exactly 1 argument ... Augmentation<T> (we want the T), we can access it on index 0
        final var typeAugTarget = typeAugmentation.orElseThrow().getActualTypeArguments()[0];
        final var substitutionAugTarget = substitutionAugmentation.orElseThrow().getActualTypeArguments()[0];

        return typeAugTarget.equals(substitutionAugTarget);
    }

    private static Optional<ParameterizedType> getAugmentationInterface(final AugmentRuntimeType runtimeType) {
        // create a reference
        final var augBase = JavaTypeName.create(Augmentation.class);
        // get all interfaces
        final var interfaces = runtimeType.javaType().getImplements();
        // filter them to get just the augmentation
        final var augmentation = interfaces.stream().filter(i -> i.getIdentifier().equals(augBase)).findFirst();

        return augmentation.map(ParameterizedType.class::cast);
    }

    private static <K, V> void safePut(final Map<K, V> map, final String name, final K key, final V value) {
        final var prev = map.put(key, value);
        verify(prev == null, "Conflict in %s, key %s conflicts on %s versus %s", name, key, prev, value);
    }
}
