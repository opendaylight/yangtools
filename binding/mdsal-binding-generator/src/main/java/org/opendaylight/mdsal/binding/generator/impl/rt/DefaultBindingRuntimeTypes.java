/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
public final class DefaultBindingRuntimeTypes implements BindingRuntimeTypes {
    private final @NonNull EffectiveModelContext context;
    private final ImmutableSetMultimap<JavaTypeName, CaseRuntimeType> choiceToCases;
    private final ImmutableMap<QNameModule, ModuleRuntimeType> modulesByNamespace;
    private final ImmutableSortedMap<String, ModuleRuntimeType> modulesByPackage;
    private final ImmutableMap<QName, IdentityRuntimeType> identities;
    private final ImmutableMap<QName, OutputRuntimeType> rpcOutputs;
    private final ImmutableMap<QName, InputRuntimeType> rpcInputs;
    private final ImmutableMap<JavaTypeName, RuntimeType> types;

    public DefaultBindingRuntimeTypes(final EffectiveModelContext context,
            final Map<QNameModule, ModuleRuntimeType> modules, final Map<JavaTypeName, RuntimeType> types,
            final Map<QName, IdentityRuntimeType> identities, final Map<QName, InputRuntimeType> rpcInputs,
            final Map<QName, OutputRuntimeType> rpcOutputs,
            final SetMultimap<JavaTypeName, CaseRuntimeType> choiceToCases) {
        this.context = requireNonNull(context);
        this.identities = ImmutableMap.copyOf(identities);
        this.types = ImmutableMap.copyOf(types);
        this.rpcInputs = ImmutableMap.copyOf(rpcInputs);
        this.rpcOutputs = ImmutableMap.copyOf(rpcOutputs);
        this.choiceToCases = ImmutableSetMultimap.copyOf(choiceToCases);

        modulesByNamespace = ImmutableMap.copyOf(modules);
        modulesByPackage = ImmutableSortedMap.copyOf(Maps.uniqueIndex(modules.values(),
            module -> module.getIdentifier().packageName()));
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return context;
    }

    @Override
    public Optional<IdentityRuntimeType> findIdentity(final QName qname) {
        return Optional.ofNullable(identities.get(requireNonNull(qname)));
    }

    @Override
    public Optional<RuntimeType> findSchema(final JavaTypeName typeName) {
        return Optional.ofNullable(types.get(requireNonNull(typeName)));
    }

    @Override
    public GeneratedRuntimeType bindingChild(final JavaTypeName typeName) {
        // The type can actually specify a sub-package, hence we to perform an inexact lookup
        final var entry = modulesByPackage.floorEntry(typeName.packageName());
        return entry == null ? null : entry.getValue().bindingChild(typeName);
    }

    @Override
    public RuntimeType schemaTreeChild(final QName qname) {
        final var module = modulesByNamespace.get(qname.getModule());
        return module == null ? null : module.schemaTreeChild(qname);
    }

    @Override
    public Optional<InputRuntimeType> findRpcInput(final QName rpcName) {
        return Optional.ofNullable(rpcInputs.get(requireNonNull(rpcName)));
    }

    @Override
    public Optional<OutputRuntimeType> findRpcOutput(final QName rpcName) {
        return Optional.ofNullable(rpcOutputs.get(requireNonNull(rpcName)));
    }

    @Override
    public Optional<YangDataRuntimeType> findYangData(final YangDataName templateName) {
        final var module = modulesByNamespace.get(templateName.module());
        return module == null ? Optional.empty() : Optional.ofNullable(module.yangDataChild(templateName));
    }

    @Override
    public Set<CaseRuntimeType> allCaseChildren(final ChoiceRuntimeType choiceType) {
        return choiceToCases.get(choiceType.getIdentifier());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("modules", modulesByNamespace.keySet())
            .add("identities", identities.size())
            .add("types", types.size())
            .toString();
    }
}
