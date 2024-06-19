/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.YangFeature;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.spi.source.DelegatedYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dynamic registry of {@link YangModuleInfo} objects, capable of combining them into an
 * {@link ModuleInfoSnapshot}. If you need a one-shot way of creating an ModuleInfoSnapshot, prefer
 * {@link ModuleInfoSnapshotBuilder} instead.
 */
@Beta
public final class ModuleInfoSnapshotResolver implements Mutable {
    private static final class RegisteredModuleInfo {
        final Registration reg;
        final YangModuleInfo info;
        final ClassLoader loader;

        private int refcount = 1;

        RegisteredModuleInfo(final YangModuleInfo info, final Registration reg, final ClassLoader loader) {
            this.info = requireNonNull(info);
            this.reg = requireNonNull(reg);
            this.loader = requireNonNull(loader);
        }

        void incRef() {
            ++refcount;
        }

        boolean decRef() {
            return --refcount == 0;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("info", info)
                .add("registration", reg)
                .add("classLoader", loader)
                .add("refCount", refcount)
                .toString();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoSnapshotResolver.class);

    private final YangTextSchemaContextResolver ctxResolver;

    @GuardedBy("this")
    private final ListMultimap<SourceIdentifier, RegisteredModuleInfo> sourceToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();
    @GuardedBy("this")
    private final ListMultimap<Class<? extends DataRoot>, ImmutableSet<YangFeature<?, ?>>> moduleToFeatures =
            MultimapBuilder.hashKeys().arrayListValues().build();
    @GuardedBy("this")
    private @Nullable ModuleInfoSnapshot currentSnapshot;

    public ModuleInfoSnapshotResolver(final String name, final YangParserFactory parserFactory) {
        ctxResolver = YangTextSchemaContextResolver.create(name, parserFactory);
    }

    public synchronized <R extends @NonNull DataRoot> Registration registerModuleFeatures(final Class<R> module,
            final Set<? extends YangFeature<?, R>> supportedFeatures) {
        final var features = supportedFeatures.stream().map(YangFeature::qname).map(QName::getLocalName).sorted()
            .collect(ImmutableSet.toImmutableSet());
        return ctxResolver.registerSupportedFeatures(BindingReflections.getQNameModule(module), features);
    }

    public synchronized List<Registration> registerModuleInfos(final Iterable<? extends YangModuleInfo> moduleInfos) {
        final var ret = new ArrayList<Registration>();
        for (var moduleInfo : moduleInfos) {
            ret.add(register(requireNonNull(moduleInfo)));
        }
        return ret;
    }

    @Holding("this")
    private Registration register(final @NonNull YangModuleInfo moduleInfo) {
        final var regInfos = flatDependencies(moduleInfo).stream()
            .map(this::registerModuleInfo)
            .collect(ImmutableList.toImmutableList());

        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                unregister(regInfos);
            }
        };
    }

    /*
     * Perform registration of a YangModuleInfo.
     */
    @Holding("this")
    private RegisteredModuleInfo registerModuleInfo(final @NonNull YangModuleInfo info) {
        // First search for an existing explicit registration
        final var sourceId = sourceIdentifierFrom(info);
        for (var reg : sourceToInfoReg.get(sourceId)) {
            if (info.equals(reg.info)) {
                reg.incRef();
                LOG.debug("Reusing registration {}", reg);
                return reg;
            }
        }

        // Create an explicit registration
        final Registration reg;
        try {
            reg = ctxResolver.registerSource(toYangTextSource(sourceId, info));
        } catch (YangSyntaxErrorException | SchemaSourceException | IOException e) {
            throw new IllegalStateException("Failed to register info " + info, e);
        }

        final var regInfo = new RegisteredModuleInfo(info, reg, info.getClass().getClassLoader());
        LOG.debug("Created new registration {}", regInfo);

        sourceToInfoReg.put(sourceId, regInfo);
        return regInfo;
    }

    public synchronized @NonNull ModuleInfoSnapshot takeSnapshot() {
        final var effectiveModel = ctxResolver.getEffectiveModelContext().orElseThrow();
        final var local = currentSnapshot;
        if (local != null && local.modelContext().equals(effectiveModel)) {
            return local;
        }

        return updateSnapshot(effectiveModel);
    }

    @Holding("this")
    private @NonNull ModuleInfoSnapshot updateSnapshot(final EffectiveModelContext modelContext) {
        // Alright, now let's find out which sources got captured
        final var sources = new HashSet<SourceIdentifier>();
        for (var entry : modelContext.getModuleStatements().entrySet()) {
            final var revision = entry.getKey().getRevision().orElse(null);
            final var module = entry.getValue();

            sources.add(new SourceIdentifier(module.argument(), revision));
            module.streamEffectiveSubstatements(SubmoduleEffectiveStatement.class)
                .map(submodule -> new SourceIdentifier(submodule.argument(), revision))
                .forEach(sources::add);
        }

        final var moduleInfos = new HashMap<SourceIdentifier, YangModuleInfo>();
        final var classLoaders = new HashMap<String, ClassLoader>();
        for (var source : sources) {
            final var regs = sourceToInfoReg.get(source);
            checkState(!regs.isEmpty(), "No registration for %s", source);

            final var reg = regs.get(0);
            final var info = reg.info;
            moduleInfos.put(source, info);
            classLoaders.put(Naming.getRootPackageName(info.getName().getModule()), info.getClass().getClassLoader());
        }

        final var next = new DefaultModuleInfoSnapshot(modelContext, moduleInfos, classLoaders);
        currentSnapshot = next;
        return next;
    }

    private synchronized void unregister(final List<RegisteredModuleInfo> regInfos) {
        for (var regInfo : regInfos) {
            if (!regInfo.decRef()) {
                LOG.debug("Registration {} has references, not removing it", regInfo);
                continue;
            }

            final var sourceId = sourceIdentifierFrom(regInfo.info);
            if (!sourceToInfoReg.remove(sourceId, regInfo)) {
                LOG.warn("Failed to find {} registered under {}", regInfo, sourceId);
            }

            regInfo.reg.close();
        }
    }

    static @NonNull YangTextSource toYangTextSource(final YangModuleInfo moduleInfo) {
        return new DelegatedYangTextSource(sourceIdentifierFrom(moduleInfo), moduleInfo.getYangTextCharSource());
    }

    private static @NonNull YangTextSource toYangTextSource(final SourceIdentifier identifier,
            final YangModuleInfo moduleInfo) {
        return new DelegatedYangTextSource(identifier, moduleInfo.getYangTextCharSource());
    }

    private static SourceIdentifier sourceIdentifierFrom(final YangModuleInfo moduleInfo) {
        final var name = moduleInfo.getName();
        return new SourceIdentifier(name.getLocalName(), name.getRevision().orElse(null));
    }

    private static @NonNull List<@NonNull YangModuleInfo> flatDependencies(final YangModuleInfo moduleInfo) {
        // Flatten the modules being registered, with the triggering module being first...
        final var requiredInfos = new LinkedHashSet<YangModuleInfo>();
        flatDependencies(requiredInfos, moduleInfo);

        // ... now reverse the order in an effort to register dependencies first (triggering module last)
        return ImmutableList.copyOf(requiredInfos).reverse();
    }

    static void flatDependencies(final Set<YangModuleInfo> set, final YangModuleInfo moduleInfo) {
        if (set.add(moduleInfo)) {
            for (var dep : moduleInfo.getImportedModules()) {
                flatDependencies(set, dep);
            }
        }
    }
}
