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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;

@Beta
public class ModuleInfoBackedContext extends AbstractModuleInfoTracker implements ClassLoadingStrategy,
        EffectiveModelContextProvider, SchemaSourceProvider<YangTextSchemaSource> {
    private static final class WithFallback extends ModuleInfoBackedContext {
        private final @NonNull ClassLoadingStrategy fallback;

        WithFallback(final YangTextSchemaContextResolver resolver, final ClassLoadingStrategy fallback) {
            super(resolver);
            this.fallback = requireNonNull(fallback);
        }

        @Override
        Class<?> loadUnknownClass(final String fullyQualifiedName) throws ClassNotFoundException {
            // We have not found a matching registration, consult the backing strategy
            final Class<?> cls = fallback.loadClass(fullyQualifiedName);
            registerImplicitBindingClass(cls);
            return cls;
        }
    }

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
                                    context.registerModuleInfos(key);
                                    return context;
                                }
                            });
                    }
            });

    ModuleInfoBackedContext(final YangTextSchemaContextResolver resolver) {
        super(resolver);
    }

    @Beta
    public static ModuleInfoBackedContext cacheContext(final ClassLoadingStrategy loadingStrategy,
            final ImmutableSet<YangModuleInfo> infos) {
        return CONTEXT_CACHES.getUnchecked(loadingStrategy).getUnchecked(infos);
    }

    public static ModuleInfoBackedContext create() {
        return create("unnamed");
    }

    public static ModuleInfoBackedContext create(final String id) {
        return new ModuleInfoBackedContext(YangTextSchemaContextResolver.create(id));
    }

    public static ModuleInfoBackedContext create(final ClassLoadingStrategy loadingStrategy) {
        return create("unnamed", loadingStrategy);
    }

    public static ModuleInfoBackedContext create(final String id, final ClassLoadingStrategy loadingStrategy) {
        return new WithFallback(YangTextSchemaContextResolver.create(id), loadingStrategy);
    }

    public static ModuleInfoBackedContext create(final String id, final YangParserFactory factory) {
        return new ModuleInfoBackedContext(YangTextSchemaContextResolver.create(id, factory));
    }

    public static ModuleInfoBackedContext create(final String id, final YangParserFactory factory,
            final ClassLoadingStrategy loadingStrategy) {
        return new WithFallback(YangTextSchemaContextResolver.create(id, factory), loadingStrategy);
    }

    @Override
    public final EffectiveModelContext getEffectiveModelContext() {
        final Optional<? extends EffectiveModelContext> contextOptional = tryToCreateModelContext();
        checkState(contextOptional.isPresent(), "Unable to recreate SchemaContext, error while parsing");
        return contextOptional.get();
    }

    @Override
    public final synchronized Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        final ClassLoader loader = findClassLoader(fullyQualifiedName);
        return loader != null ? ClassLoaderUtils.loadClass(loader, fullyQualifiedName)
                : loadUnknownClass(fullyQualifiedName);
    }

    @Override
    public final ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return getResolverSource(sourceIdentifier);
    }

    @Beta
    public final @NonNull BindingRuntimeContext createRuntimeContext(final BindingRuntimeGenerator generator) {
        return DefaultBindingRuntimeContext.create(
            generator.generateTypeMapping(tryToCreateModelContext().orElseThrow()), this);
    }

    // TODO finish schema parsing and expose as SchemaService
    // Unite with current SchemaService

    public final Optional<? extends EffectiveModelContext> tryToCreateModelContext() {
        return getResolverEffectiveModel();
    }

    @Holding("this")
    Class<?> loadUnknownClass(final String fullyQualifiedName) throws ClassNotFoundException {
        throw new ClassNotFoundException(fullyQualifiedName);
    }
}
