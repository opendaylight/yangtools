/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.spi;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaSourceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public class ModuleInfoBackedContext extends GeneratedClassLoadingStrategy
        implements ModuleInfoRegistry, EffectiveModelContextProvider, SchemaSourceProvider<YangTextSchemaSource> {
    private static final class WithFallback extends ModuleInfoBackedContext {
        private final @NonNull ClassLoadingStrategy fallback;

        WithFallback(final ClassLoadingStrategy fallback) {
            this.fallback = requireNonNull(fallback);
        }

        @Override
        Class<?> loadUnknownClass(final String fullyQualifiedName) throws ClassNotFoundException {
            // We have not found a matching registration, consult the backing strategy
            final Class<?> cls = fallback.loadClass(fullyQualifiedName);
            registerImplicitModuleInfo(BindingRuntimeHelpers.extractYangModuleInfo(cls));
            return cls;
        }
    }

    private abstract static class AbstractRegisteredModuleInfo {
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

    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoBackedContext.class);

    private static final LoadingCache<ClassLoadingStrategy,
        LoadingCache<ImmutableSet<YangModuleInfo>, ModuleInfoBackedContext>> CONTEXT_CACHES = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<ClassLoadingStrategy,
                LoadingCache<ImmutableSet<YangModuleInfo>, ModuleInfoBackedContext>>() {
                    @Override
                    public LoadingCache<ImmutableSet<YangModuleInfo>, ModuleInfoBackedContext> load(
                            final ClassLoadingStrategy strategy) {
                        return CacheBuilder.newBuilder().weakValues().build(
                            new CacheLoader<Set<YangModuleInfo>, ModuleInfoBackedContext>() {
                                @Override
                                public ModuleInfoBackedContext load(final Set<YangModuleInfo> key) {
                                    final ModuleInfoBackedContext context = ModuleInfoBackedContext.create(strategy);
                                    context.addModuleInfos(key);
                                    return context;
                                }
                            });
                    }
            });

    private final YangTextSchemaContextResolver ctxResolver = YangTextSchemaContextResolver.create("binding-context");

    @GuardedBy("this")
    private final ListMultimap<String, AbstractRegisteredModuleInfo> packageToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();
    @GuardedBy("this")
    private final ListMultimap<SourceIdentifier, AbstractRegisteredModuleInfo> sourceToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();

    ModuleInfoBackedContext() {
        // Hidden on purpose
    }

    @Beta
    public static ModuleInfoBackedContext cacheContext(final ClassLoadingStrategy loadingStrategy,
            final ImmutableSet<YangModuleInfo> infos) {
        return CONTEXT_CACHES.getUnchecked(loadingStrategy).getUnchecked(infos);
    }

    public static ModuleInfoBackedContext create() {
        return new ModuleInfoBackedContext();
    }

    public static ModuleInfoBackedContext create(final ClassLoadingStrategy loadingStrategy) {
        return new WithFallback(loadingStrategy);
    }

    @Override
    public final EffectiveModelContext getEffectiveModelContext() {
        final Optional<? extends EffectiveModelContext> contextOptional = tryToCreateModelContext();
        checkState(contextOptional.isPresent(), "Unable to recreate SchemaContext, error while parsing");
        return contextOptional.get();
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public final Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        // This performs an explicit check for binding classes
        final String modulePackageName = BindingReflections.getModelRootPackageName(fullyQualifiedName);

        synchronized (this) {
            // Try to find a loaded class loader
            // FIXME: two-step process, try explicit registrations first
            for (AbstractRegisteredModuleInfo reg : packageToInfoReg.get(modulePackageName)) {
                return ClassLoaderUtils.loadClass(reg.loader, fullyQualifiedName);
            }

            return loadUnknownClass(fullyQualifiedName);
        }
    }

    @Holding("this")
    Class<?> loadUnknownClass(final String fullyQualifiedName) throws ClassNotFoundException {
        throw new ClassNotFoundException(fullyQualifiedName);
    }

    @Override
    public final synchronized ObjectRegistration<YangModuleInfo> registerModuleInfo(
            final YangModuleInfo yangModuleInfo) {
        return register(requireNonNull(yangModuleInfo));
    }

    @Override
    public final ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return ctxResolver.getSource(sourceIdentifier);
    }

    final synchronized void addModuleInfos(final Iterable<? extends YangModuleInfo> moduleInfos) {
        for (YangModuleInfo yangModuleInfo : moduleInfos) {
            register(requireNonNull(yangModuleInfo));
        }
    }

    @Beta
    public final @NonNull BindingRuntimeContext createRuntimeContext(final BindingRuntimeGenerator generator) {
        return BindingRuntimeContext.create(generator.generateTypeMapping(tryToCreateModelContext().orElseThrow()),
            this);
    }

    // TODO finish schema parsing and expose as SchemaService
    // Unite with current SchemaService

    public final Optional<? extends EffectiveModelContext> tryToCreateModelContext() {
        return ctxResolver.getEffectiveModelContext();
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

    /*
     * Perform implicit registration of a YangModuleInfo and any of its dependencies. If there is a registration for
     * a particular source, we do not create a duplicate registration.
     */
    @Holding("this")
    final void registerImplicitModuleInfo(final @NonNull YangModuleInfo moduleInfo) {
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

    final synchronized void unregister(final ImmutableList<ExplicitRegisteredModuleInfo> regInfos) {
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

    private static List<YangModuleInfo> flatDependencies(final YangModuleInfo moduleInfo) {
        // Flatten the modules being registered, with the triggering module being first...
        final Set<YangModuleInfo> requiredInfos = new LinkedHashSet<>();
        flatDependencies(requiredInfos, moduleInfo);

        // ... now reverse the order in an effort to register dependencies first (triggering module last)
        final List<YangModuleInfo> intendedOrder = new ArrayList<>(requiredInfos);
        Collections.reverse(intendedOrder);

        return intendedOrder;
    }

    private static void flatDependencies(final Set<YangModuleInfo> set, final YangModuleInfo moduleInfo) {
        if (set.add(moduleInfo)) {
            for (YangModuleInfo dep : moduleInfo.getImportedModules()) {
                flatDependencies(set, dep);
            }
        }
    }
}
