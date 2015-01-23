/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.api.ModuleInfoRegistry;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleInfoBackedContext extends GeneratedClassLoadingStrategy //
        implements //
        AdvancedSchemaSourceProvider<InputStream>, ModuleInfoRegistry, SchemaContextProvider {

    private ModuleInfoBackedContext(final ClassLoadingStrategy loadingStrategy) {
        this.backingLoadingStrategy = loadingStrategy;
    }

    public static ModuleInfoBackedContext create() {
        return new ModuleInfoBackedContext(getTCCLClassLoadingStrategy());
    }

    public static ModuleInfoBackedContext create(final ClassLoadingStrategy loadingStrategy) {
        return new ModuleInfoBackedContext(loadingStrategy);
    }

    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoBackedContext.class);

    private final ConcurrentMap<String, WeakReference<ClassLoader>> packageNameToClassLoader = new ConcurrentHashMap<>();
    private final ConcurrentMap<SourceIdentifier, YangModuleInfo> sourceIdentifierToModuleInfo = new ConcurrentHashMap<>();

    private final ClassLoadingStrategy backingLoadingStrategy;

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        String modulePackageName = BindingReflections.getModelRootPackageName(fullyQualifiedName);

        WeakReference<ClassLoader> classLoaderRef = packageNameToClassLoader.get(modulePackageName);
        ClassLoader classloader = null;
        if (classLoaderRef != null && (classloader = classLoaderRef.get()) != null) {
            return ClassLoaderUtils.loadClass(classloader, fullyQualifiedName);
        }
        if (backingLoadingStrategy == null) {
            throw new ClassNotFoundException(fullyQualifiedName);
        }
        Class<?> cls = backingLoadingStrategy.loadClass(fullyQualifiedName);
        if (BindingReflections.isBindingClass(cls)) {
            boolean newModule = resolveModuleInfo(cls);
            if (newModule) {
                recreateSchemaContext();
            }
        }
        return cls;
    }


    private synchronized Optional<SchemaContext> recreateSchemaContext() {
        try {
            ImmutableList<InputStream> streams = getAvailableStreams();
            YangParserImpl parser = new YangParserImpl();
            Set<Module> modules = parser.parseYangModelsFromStreams(streams);
            SchemaContext schemaContext = parser.resolveSchemaContext(modules);
            return Optional.of(schemaContext);
        } catch (IOException e) {
            LOG.error("Schema was not recreated.",e);
        }
        return Optional.absent();
    }

    // TODO finish schema parsing and expose as SchemaService
    // Unite with current SchemaService
    // Implement remove ModuleInfo to update SchemaContext

    public synchronized Optional<SchemaContext> tryToCreateSchemaContext() {
        return recreateSchemaContext();
    }

    private ImmutableList<InputStream> getAvailableStreams() throws IOException {
        ImmutableSet<YangModuleInfo> moduleInfos = ImmutableSet.copyOf(sourceIdentifierToModuleInfo.values());

        ImmutableList.Builder<InputStream> sourceStreams = ImmutableList.<InputStream> builder();
        for (YangModuleInfo moduleInfo : moduleInfos) {
            sourceStreams.add(moduleInfo.getModuleSourceStream());
        }
        return sourceStreams.build();
    }

    private boolean resolveModuleInfo(final Class<?> cls) {
        try {
            return resolveModuleInfo(BindingReflections.getModuleInfo(cls));
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to resolve module information for class %s", cls), e);
        }
    }

    private boolean resolveModuleInfo(final YangModuleInfo moduleInfo) {

        SourceIdentifier identifier = sourceIdentifierFrom(moduleInfo);
        YangModuleInfo previous = sourceIdentifierToModuleInfo.putIfAbsent(identifier, moduleInfo);
        ClassLoader moduleClassLoader = moduleInfo.getClass().getClassLoader();
        if (previous == null) {
            String modulePackageName = moduleInfo.getClass().getPackage().getName();
            packageNameToClassLoader.putIfAbsent(modulePackageName, new WeakReference<ClassLoader>(moduleClassLoader));

            for (YangModuleInfo importedInfo : moduleInfo.getImportedModules()) {
                resolveModuleInfo(importedInfo);
            }
        } else {
            return false;
        }
        return true;
    }

    private SourceIdentifier sourceIdentifierFrom(final YangModuleInfo moduleInfo) {
        return SourceIdentifier.create(moduleInfo.getName(), Optional.of(moduleInfo.getRevision()));
    }

    public void addModuleInfos(final Iterable<? extends YangModuleInfo> moduleInfos) {
        for (YangModuleInfo yangModuleInfo : moduleInfos) {
            registerModuleInfo(yangModuleInfo);
        }
    }

    @Override
    public ObjectRegistration<YangModuleInfo> registerModuleInfo(final YangModuleInfo yangModuleInfo) {
        YangModuleInfoRegistration registration = new YangModuleInfoRegistration(yangModuleInfo, this);

        resolveModuleInfo(yangModuleInfo);

        return registration;
    }

    @Override
    public Optional<InputStream> getSchemaSource(final SourceIdentifier sourceIdentifier) {
        YangModuleInfo info = sourceIdentifierToModuleInfo.get(sourceIdentifier);
        if (info == null) {
            return Optional.absent();
        }
        try {
            return Optional.of(info.getModuleSourceStream());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    @Override
    public Optional<InputStream> getSchemaSource(final String moduleName, final Optional<String> revision) {
        return getSchemaSource(SourceIdentifier.create(moduleName, revision));
    }

    private static class YangModuleInfoRegistration extends AbstractObjectRegistration<YangModuleInfo> {

        private final ModuleInfoBackedContext context;

        public YangModuleInfoRegistration(final YangModuleInfo instance, final ModuleInfoBackedContext context) {
            super(instance);
            this.context = context;
        }

        @Override
        protected void removeRegistration() {
            context.remove(this);
        }

    }

    private void remove(final YangModuleInfoRegistration registration) {
        // FIXME implement
    }

    @Override
    public SchemaContext getSchemaContext() {
        return tryToCreateSchemaContext().get();
    }
}
