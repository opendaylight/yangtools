/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkArgument;

public class URLSchemaContextResolver implements AdvancedSchemaSourceProvider<InputStream> {

    private static final Logger LOG = LoggerFactory.getLogger(URLSchemaContextResolver.class);
    private final ConcurrentMap<SourceIdentifier, SourceContext> availableSources = new ConcurrentHashMap<>();

    private YangSourceContext currentSourceContext;
    private Optional<SchemaContext> currentSchemaContext = Optional.absent();
    
    public Registration<URL> registerSource(URL source) {
        checkArgument(source != null, "Supplied source must not be null");
        InputStream yangStream = getInputStream(source);
        YangModelDependencyInfo modelInfo = YangModelDependencyInfo.fromInputStream(yangStream);
        SourceIdentifier identifier = SourceIdentifier.create(modelInfo.getName(),
                Optional.of(modelInfo.getFormattedRevision()));
        SourceContext sourceContext = new SourceContext(source, identifier, modelInfo);
        availableSources.putIfAbsent(identifier, sourceContext);
        return sourceContext;
    }

    public Optional<SchemaContext> getSchemaContext() {
        return currentSchemaContext;
    }

    @Override
    public Optional<InputStream> getSchemaSource(SourceIdentifier key) {
        SourceContext ctx = availableSources.get(key);
        if (ctx != null) {
            InputStream stream = getInputStream(ctx.getInstance());
            return Optional.fromNullable(stream);
        }
        return Optional.absent();
    }

    @Override
    public Optional<InputStream> getSchemaSource(String name, Optional<String> version) {
        return getSchemaSource(SourceIdentifier.create(name, version));
    }

    private InputStream getInputStream(URL source) {
        InputStream stream;
        try {
            stream = source.openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Supplied stream: " + source + " is not available", e);
        }
        return stream;
    }

    private final class SourceContext extends AbstractObjectRegistration<URL> //
            implements Identifiable<SourceIdentifier> {

        final SourceIdentifier identifier;
        final YangModelDependencyInfo dependencyInfo;

        public SourceContext(URL instance, SourceIdentifier identifier, YangModelDependencyInfo modelInfo) {
            super(instance);
            this.identifier = identifier;
            this.dependencyInfo = modelInfo;
        }

        public SourceIdentifier getIdentifier() {
            return identifier;
        }

        @Override
        protected void removeRegistration() {
            removeSource(this);
        }

        public YangModelDependencyInfo getDependencyInfo() {
            return dependencyInfo;
        }
    }

    private void removeSource(SourceContext sourceContext) {
        boolean removed = availableSources.remove(sourceContext.getIdentifier(), sourceContext);
        if(removed) {
            tryToUpdateSchemaContext();
        }
    }

    public synchronized Optional<SchemaContext> tryToUpdateSchemaContext() {
        if(availableSources.isEmpty()) {
            return Optional.absent();
        }
        ImmutableMap<SourceIdentifier, SourceContext> actualSources = ImmutableMap.copyOf(availableSources);
        Builder<SourceIdentifier, YangModelDependencyInfo> builder = ImmutableMap.<SourceIdentifier, YangModelDependencyInfo> builder();
        for(Entry<SourceIdentifier, SourceContext> entry : actualSources.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().getDependencyInfo());
        }
        ImmutableMap<SourceIdentifier, YangModelDependencyInfo> sourcesMap = builder.build();
        YangSourceContext context = YangSourceContext.createFrom(sourcesMap);
        LOG.debug("Trying to create schema context from {}",sourcesMap.keySet());
        LOG.debug("Ommiting {} because of unresolved dependencies",context.getMissingDependencies().keySet());
        
        try {
            if(currentSourceContext == null || !context.getValidSources().equals(currentSourceContext.getValidSources())) {
                List<InputStream> streams = YangSourceContext.getValidInputStreams(context, this);
                YangParserImpl parser = new YangParserImpl();
                Set<Module> modules = parser.parseYangModelsFromStreams(streams);
                SchemaContext schemaContext = parser.resolveSchemaContext(modules);
                currentSchemaContext = Optional.of(schemaContext);
                currentSourceContext = context;
                return currentSchemaContext;
            } 
            currentSourceContext = context;
        } catch (Exception e) {
            LOG.error("Could not create schema context for {} ",context.getValidSources());
        }
        return Optional.absent();
    }

}
