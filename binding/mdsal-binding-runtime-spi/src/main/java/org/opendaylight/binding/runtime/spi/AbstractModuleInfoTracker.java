/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.spi;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.util.concurrent.ListenableFuture;
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
import org.opendaylight.binding.runtime.api.ModuleInfoSnapshot;
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
 * Abstract base class for things that create an EffectiveModuleContext or similar things from a (dynamic) set of
 * YangModuleInfo objects.
 *
 * <p>
 * Note this class has some locking quirks and may end up being further refactored.
 */
abstract class AbstractModuleInfoTracker implements Mutable {
    abstract static class AbstractRegisteredModuleInfo {
        final YangTextSchemaSourceRegistration reg;
        final YangModuleInfo info;
        final ClassLoader loader;

        AbstractRegisteredModuleInfo(final YangModuleInfo info, final YangTextSchemaSourceRegistration reg,
            final ClassLoader loader) {
            this.info = requireNonNull(info);
            this.reg = requireNonNull(reg);
            this.loader = requireNonNull(loader);
        }

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
        }

        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("info", info).add("registration", reg).add("classLoader", loader);
        }
    }

    private static final class ExplicitRegisteredModuleInfo extends AbstractRegisteredModuleInfo {
        private int refcount = 1;

        ExplicitRegisteredModuleInfo(final YangModuleInfo info, final YangTextSchemaSourceRegistration reg,
                final ClassLoader loader) {
            super(info, reg, loader);
        }

        void incRef() {
            ++refcount;
        }

        boolean decRef() {
            return --refcount == 0;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("refCount", refcount);
        }
    }

    private static final class ImplicitRegisteredModuleInfo extends AbstractRegisteredModuleInfo {
        ImplicitRegisteredModuleInfo(final YangModuleInfo info, final YangTextSchemaSourceRegistration reg,
                final ClassLoader loader) {
            super(info, reg, loader);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractModuleInfoTracker.class);

    private final YangTextSchemaContextResolver ctxResolver;

    @GuardedBy("this")
    private final ListMultimap<String, AbstractRegisteredModuleInfo> packageToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();
    @GuardedBy("this")
    private final ListMultimap<SourceIdentifier, AbstractRegisteredModuleInfo> sourceToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();
    @GuardedBy("this")
    private @Nullable ModuleInfoSnapshot currentSnapshot;

    AbstractModuleInfoTracker(final YangTextSchemaContextResolver resolver) {
        this.ctxResolver = requireNonNull(resolver);
    }

    public final synchronized List<ObjectRegistration<YangModuleInfo>> registerModuleInfos(
            final Iterable<? extends YangModuleInfo> moduleInfos) {
        final List<ObjectRegistration<YangModuleInfo>> ret = new ArrayList<>();
        for (YangModuleInfo yangModuleInfo : moduleInfos) {
            ret.add(register(requireNonNull(yangModuleInfo)));
        }
        return ret;
    }

    @Holding("this")
    private ObjectRegistration<YangModuleInfo> register(final @NonNull YangModuleInfo moduleInfo) {
        final Builder<ExplicitRegisteredModuleInfo> regBuilder = ImmutableList.builder();
        for (YangModuleInfo info : flatDependencies(moduleInfo)) {
            regBuilder.add(registerExplicitModuleInfo(info));
        }
        final ImmutableList<ExplicitRegisteredModuleInfo> regInfos = regBuilder.build();

        return new AbstractObjectRegistration<>(moduleInfo) {
            @Override
            protected void removeRegistration() {
                unregister(regInfos);
            }
        };
    }

    @Holding("this")
    final void registerImplicitBindingClass(final Class<?> bindingClass) {
        registerImplicitModuleInfo(BindingRuntimeHelpers.extractYangModuleInfo(bindingClass));
    }

    @Holding("this")
    final @Nullable ClassLoader findClassLoader(final String fullyQualifiedName) {
        // This performs an explicit check for binding classes
        final String modulePackageName = BindingReflections.getModelRootPackageName(fullyQualifiedName);

        // Try to find a loaded class loader
        // FIXME: two-step process, try explicit registrations first
        for (AbstractRegisteredModuleInfo reg : packageToInfoReg.get(modulePackageName)) {
            return reg.loader;
        }
        return null;
    }

    /*
     * Perform implicit registration of a YangModuleInfo and any of its dependencies. If there is a registration for
     * a particular source, we do not create a duplicate registration.
     */
    @Holding("this")
    private void registerImplicitModuleInfo(final @NonNull YangModuleInfo moduleInfo) {
        for (YangModuleInfo info : flatDependencies(moduleInfo)) {
            final Class<?> infoClass = info.getClass();
            final SourceIdentifier sourceId = sourceIdentifierFrom(info);
            if (sourceToInfoReg.containsKey(sourceId)) {
                LOG.debug("Skipping implicit registration of {} as source {} is already registered", info, sourceId);
                continue;
            }

            final YangTextSchemaSourceRegistration reg;
            try {
                reg = ctxResolver.registerSource(toYangTextSource(sourceId, info));
            } catch (YangSyntaxErrorException | SchemaSourceException | IOException e) {
                LOG.warn("Failed to register info {} source {}, ignoring it", info, sourceId, e);
                continue;
            }

            final ImplicitRegisteredModuleInfo regInfo = new ImplicitRegisteredModuleInfo(info, reg,
                infoClass.getClassLoader());
            sourceToInfoReg.put(sourceId, regInfo);
            packageToInfoReg.put(BindingReflections.getModelRootPackageName(infoClass.getPackage()), regInfo);
        }
    }

    /*
     * Perform explicit registration of a YangModuleInfo. This always results in a new explicit registration. In case
     * there is a pre-existing implicit registration, it is removed just after the explicit registration is made.
     */
    @Holding("this")
    private ExplicitRegisteredModuleInfo registerExplicitModuleInfo(final @NonNull YangModuleInfo info) {
        // First search for an existing explicit registration
        final SourceIdentifier sourceId = sourceIdentifierFrom(info);
        for (AbstractRegisteredModuleInfo reg : sourceToInfoReg.get(sourceId)) {
            if (reg instanceof ExplicitRegisteredModuleInfo && info.equals(reg.info)) {
                final ExplicitRegisteredModuleInfo explicit = (ExplicitRegisteredModuleInfo) reg;
                explicit.incRef();
                LOG.debug("Reusing explicit registration {}", explicit);
                return explicit;
            }
        }

        // Create an explicit registration
        final YangTextSchemaSourceRegistration reg;
        try {
            reg = ctxResolver.registerSource(toYangTextSource(sourceId, info));
        } catch (YangSyntaxErrorException | SchemaSourceException | IOException e) {
            throw new IllegalStateException("Failed to register info " + info, e);
        }

        final Class<?> infoClass = info.getClass();
        final String packageName = BindingReflections.getModelRootPackageName(infoClass.getPackage());
        final ExplicitRegisteredModuleInfo regInfo = new ExplicitRegisteredModuleInfo(info, reg,
            infoClass.getClassLoader());
        LOG.debug("Created new explicit registration {}", regInfo);

        sourceToInfoReg.put(sourceId, regInfo);
        removeImplicit(sourceToInfoReg.get(sourceId));
        packageToInfoReg.put(packageName, regInfo);
        removeImplicit(packageToInfoReg.get(packageName));

        return regInfo;
    }

    // Reconsider utility of this
    final Optional<? extends EffectiveModelContext> getResolverEffectiveModel() {
        return ctxResolver.getEffectiveModelContext();
    }

    @Deprecated
    final ListenableFuture<? extends YangTextSchemaSource> getResolverSource(final SourceIdentifier sourceIdentifier) {
        return ctxResolver.getSource(sourceIdentifier);
    }

    @Holding("this")
    final @NonNull ModuleInfoSnapshot updateSnapshot() {
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
            final List<AbstractRegisteredModuleInfo> regs = sourceToInfoReg.get(source);
            checkState(!regs.isEmpty(), "No registration for %s", source);

            AbstractRegisteredModuleInfo reg = regs.stream()
                    .filter(ExplicitRegisteredModuleInfo.class::isInstance).findFirst()
                    .orElse(null);
            if (reg == null) {
                reg = regs.get(0);
            }

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
    private synchronized void unregister(final ImmutableList<ExplicitRegisteredModuleInfo> regInfos) {
        for (ExplicitRegisteredModuleInfo regInfo : regInfos) {
            if (!regInfo.decRef()) {
                LOG.debug("Registration {} has references, not removing it", regInfo);
                continue;
            }

            final SourceIdentifier sourceId = sourceIdentifierFrom(regInfo.info);
            if (!sourceToInfoReg.remove(sourceId, regInfo)) {
                LOG.warn("Failed to find {} registered under {}", regInfo, sourceId);
            }

            final String packageName = BindingReflections.getModelRootPackageName(regInfo.info.getClass().getPackage());
            if (!packageToInfoReg.remove(packageName, regInfo)) {
                LOG.warn("Failed to find {} registered under {}", regInfo, packageName);
            }

            regInfo.reg.close();
        }
    }

    @Holding("this")
    private static void removeImplicit(final List<AbstractRegisteredModuleInfo> regs) {
        /*
         * Search for implicit registration for a sourceId/packageName.
         *
         * Since we are called while an explicit registration is being created (and has already been inserted, we know
         * there is at least one entry in the maps. We also know registrations retain the order in which they were
         * created and that implicit registrations are not created if there already is a registration.
         *
         * This means that if an implicit registration exists, it will be the first entry in the list.
         */
        final AbstractRegisteredModuleInfo reg = regs.get(0);
        if (reg instanceof ImplicitRegisteredModuleInfo) {
            LOG.debug("Removing implicit registration {}", reg);
            regs.remove(0);
            reg.reg.close();
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
