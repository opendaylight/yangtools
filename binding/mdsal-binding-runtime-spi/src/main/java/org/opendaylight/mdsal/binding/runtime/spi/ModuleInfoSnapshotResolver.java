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
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaSourceRegistration;
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
        final YangTextSchemaSourceRegistration reg;
        final YangModuleInfo info;
        final ClassLoader loader;

        private int refcount = 1;

        RegisteredModuleInfo(final YangModuleInfo info, final YangTextSchemaSourceRegistration reg,
            final ClassLoader loader) {
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
            return MoreObjects.toStringHelper(this).add("info", info).add("registration", reg)
                    .add("classLoader", loader).add("refCount", refcount).toString();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoSnapshotResolver.class);

    private final YangTextSchemaContextResolver ctxResolver;

    @GuardedBy("this")
    private final ListMultimap<SourceIdentifier, RegisteredModuleInfo> sourceToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();
    @GuardedBy("this")
    private @Nullable ModuleInfoSnapshot currentSnapshot;

    public ModuleInfoSnapshotResolver(final String name, final YangParserFactory parserFactory) {
        ctxResolver = YangTextSchemaContextResolver.create(name, parserFactory);
    }

    public synchronized List<ObjectRegistration<YangModuleInfo>> registerModuleInfos(
            final Iterable<? extends YangModuleInfo> moduleInfos) {
        final List<ObjectRegistration<YangModuleInfo>> ret = new ArrayList<>();
        for (YangModuleInfo yangModuleInfo : moduleInfos) {
            ret.add(register(requireNonNull(yangModuleInfo)));
        }
        return ret;
    }

    @Holding("this")
    private ObjectRegistration<YangModuleInfo> register(final @NonNull YangModuleInfo moduleInfo) {
        final Builder<RegisteredModuleInfo> regBuilder = ImmutableList.builder();
        for (YangModuleInfo info : flatDependencies(moduleInfo)) {
            regBuilder.add(registerModuleInfo(info));
        }
        final ImmutableList<RegisteredModuleInfo> regInfos = regBuilder.build();

        return new AbstractObjectRegistration<>(moduleInfo) {
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
        final SourceIdentifier sourceId = sourceIdentifierFrom(info);
        for (RegisteredModuleInfo reg : sourceToInfoReg.get(sourceId)) {
            if (info.equals(reg.info)) {
                reg.incRef();
                LOG.debug("Reusing registration {}", reg);
                return reg;
            }
        }

        // Create an explicit registration
        final YangTextSchemaSourceRegistration reg;
        try {
            reg = ctxResolver.registerSource(toYangTextSource(sourceId, info));
        } catch (YangSyntaxErrorException | SchemaSourceException | IOException e) {
            throw new IllegalStateException("Failed to register info " + info, e);
        }

        final RegisteredModuleInfo regInfo = new RegisteredModuleInfo(info, reg, info.getClass().getClassLoader());
        LOG.debug("Created new registration {}", regInfo);

        sourceToInfoReg.put(sourceId, regInfo);
        return regInfo;
    }

    public synchronized @NonNull ModuleInfoSnapshot takeSnapshot() {
        final EffectiveModelContext effectiveModel = ctxResolver.getEffectiveModelContext().orElseThrow();
        final ModuleInfoSnapshot local = currentSnapshot;
        if (local != null && local.getEffectiveModelContext().equals(effectiveModel)) {
            return local;
        }

        return updateSnapshot(effectiveModel);
    }

    @Holding("this")
    private @NonNull ModuleInfoSnapshot updateSnapshot(final EffectiveModelContext effectiveModel) {
        // Alright, now let's find out which sources got captured
        final Set<SourceIdentifier> sources = new HashSet<>();
        for (Entry<QNameModule, ModuleEffectiveStatement> entry : effectiveModel.getModuleStatements().entrySet()) {
            final Optional<Revision> revision = entry.getKey().getRevision();
            final ModuleEffectiveStatement module = entry.getValue();

            sources.add(RevisionSourceIdentifier.create(module.argument(), revision));
            module.streamEffectiveSubstatements(SubmoduleEffectiveStatement.class)
                .map(submodule -> RevisionSourceIdentifier.create(submodule.argument(), revision))
                .forEach(sources::add);
        }

        final Map<SourceIdentifier, YangModuleInfo> moduleInfos = new HashMap<>();
        final Map<String, ClassLoader> classLoaders = new HashMap<>();
        for (SourceIdentifier source : sources) {
            final List<RegisteredModuleInfo> regs = sourceToInfoReg.get(source);
            checkState(!regs.isEmpty(), "No registration for %s", source);

            final RegisteredModuleInfo reg = regs.get(0);
            final YangModuleInfo info = reg.info;
            moduleInfos.put(source, info);
            final Class<?> infoClass = info.getClass();
            classLoaders.put(BindingReflections.getModelRootPackageName(infoClass.getPackage()),
                infoClass.getClassLoader());
        }

        final ModuleInfoSnapshot next = new DefaultModuleInfoSnapshot(effectiveModel, moduleInfos, classLoaders);
        currentSnapshot = next;
        return next;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
                justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private synchronized void unregister(final ImmutableList<RegisteredModuleInfo> regInfos) {
        for (RegisteredModuleInfo regInfo : regInfos) {
            if (!regInfo.decRef()) {
                LOG.debug("Registration {} has references, not removing it", regInfo);
                continue;
            }

            final SourceIdentifier sourceId = sourceIdentifierFrom(regInfo.info);
            if (!sourceToInfoReg.remove(sourceId, regInfo)) {
                LOG.warn("Failed to find {} registered under {}", regInfo, sourceId);
            }

            regInfo.reg.close();
        }
    }

    private static @NonNull YangTextSchemaSource toYangTextSource(final SourceIdentifier identifier,
            final YangModuleInfo moduleInfo) {
        return YangTextSchemaSource.delegateForByteSource(identifier, moduleInfo.getYangTextByteSource());
    }

    private static SourceIdentifier sourceIdentifierFrom(final YangModuleInfo moduleInfo) {
        final QName name = moduleInfo.getName();
        return RevisionSourceIdentifier.create(name.getLocalName(), name.getRevision());
    }

    private static @NonNull List<@NonNull YangModuleInfo> flatDependencies(final YangModuleInfo moduleInfo) {
        // Flatten the modules being registered, with the triggering module being first...
        final Set<YangModuleInfo> requiredInfos = new LinkedHashSet<>();
        flatDependencies(requiredInfos, moduleInfo);

        // ... now reverse the order in an effort to register dependencies first (triggering module last)
        return ImmutableList.copyOf(requiredInfos).reverse();
    }

    private static void flatDependencies(final Set<YangModuleInfo> set, final YangModuleInfo moduleInfo) {
        if (set.add(moduleInfo)) {
            for (YangModuleInfo dep : moduleInfo.getImportedModules()) {
                flatDependencies(set, dep);
            }
        }
    }
}
