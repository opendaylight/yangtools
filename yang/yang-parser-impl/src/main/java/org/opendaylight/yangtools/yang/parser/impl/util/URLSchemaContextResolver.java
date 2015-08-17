/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Use {@link org.opendaylight.yangtools.yang.parser.repo.URLSchemaContextResolver}
 * instead.
 */
@Deprecated
@ThreadSafe
public class URLSchemaContextResolver implements AdvancedSchemaSourceProvider<InputStream> {

    private static final Logger LOG = LoggerFactory.getLogger(URLSchemaContextResolver.class);

    @GuardedBy("this")
    private final ConcurrentMap<SourceIdentifier, SourceContext> availableSources = new ConcurrentHashMap<>();
    @GuardedBy("this")
    private YangSourceContext currentSourceContext;
    @GuardedBy("this")
    private Optional<SchemaContext> currentSchemaContext = Optional.absent();

    /**
     * Register new yang schema when it appears.
     * @param source URL of a yang file
     * @return new instance of SourceContext if the source is not null
     */
    public synchronized ObjectRegistration<URL> registerSource(final URL source) {
        checkArgument(source != null, "Supplied source must not be null");
        InputStream yangStream = getInputStream(source);
        YangModelDependencyInfo modelInfo = YangModelDependencyInfo.fromInputStream(yangStream);
        SourceIdentifier identifier = SourceIdentifier.create(modelInfo.getName(),
                Optional.of(modelInfo.getFormattedRevision()));
        SourceContext sourceContext = new SourceContext(source, identifier, modelInfo);
        availableSources.putIfAbsent(identifier, sourceContext);
        return sourceContext;
    }

    public synchronized Optional<SchemaContext> getSchemaContext() {
        return currentSchemaContext;
    }

    @Override
    public synchronized Optional<InputStream> getSchemaSource(final SourceIdentifier key) {
        SourceContext ctx = availableSources.get(key);
        if (ctx != null) {
            InputStream stream = getInputStream(ctx.getInstance());
            return Optional.fromNullable(stream);
        }
        return Optional.absent();
    }

    @Override
    public Optional<InputStream> getSchemaSource(final String name, final Optional<String> version) {
        return getSchemaSource(SourceIdentifier.create(name, version));
    }

    private static InputStream getInputStream(final URL source) {
        InputStream stream;
        try {
            stream = source.openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Supplied stream: " + source + " is not available", e);
        }
        return stream;
    }

    private final class SourceContext extends AbstractObjectRegistration<URL>
            implements Identifiable<SourceIdentifier> {

        final SourceIdentifier identifier;
        final YangModelDependencyInfo dependencyInfo;

        public SourceContext(final URL instance, final SourceIdentifier identifier, final YangModelDependencyInfo modelInfo) {
            super(instance);
            this.identifier = identifier;
            this.dependencyInfo = modelInfo;
        }

        @Override
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

    private synchronized void removeSource(final SourceContext sourceContext) {
        boolean removed = availableSources.remove(sourceContext.getIdentifier(), sourceContext);
        if (removed) {
            tryToUpdateSchemaContext();
        }
    }

    /**
     * Try to parse all currently available yang files and build new schema context.
     * @return new schema context iif there is at least 1 yang file registered and new schema context was successfully built.
     */
    public synchronized Optional<SchemaContext> tryToUpdateSchemaContext() {
        if (availableSources.isEmpty()) {
            return Optional.absent();
        }
        ImmutableMap<SourceIdentifier, SourceContext> actualSources = ImmutableMap.copyOf(availableSources);
        Builder<SourceIdentifier, YangModelDependencyInfo> builder = ImmutableMap.<SourceIdentifier, YangModelDependencyInfo>builder();
        for (Entry<SourceIdentifier, SourceContext> entry : actualSources.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().getDependencyInfo());
        }
        ImmutableMap<SourceIdentifier, YangModelDependencyInfo> sourcesMap = builder.build();
        YangSourceContext yangSourceContext = YangSourceContext.createFrom(sourcesMap, this);
        LOG.debug("Trying to create schema context from {}", sourcesMap.keySet());

        if (!yangSourceContext.getMissingDependencies().isEmpty()) {
            LOG.debug("Omitting {} because of unresolved dependencies", yangSourceContext.getMissingDependencies().keySet());
            LOG.debug("Missing model sources for {}", yangSourceContext.getMissingSources());
        }
        if (currentSourceContext == null || !yangSourceContext.getValidSources().equals(currentSourceContext.getValidSources())) {
            try {
                Collection<ByteSource> byteSources = yangSourceContext.getValidByteSources();
                YangParserImpl parser = YangParserImpl.getInstance();
                SchemaContext schemaContext = parser.parseSources(byteSources);
                currentSchemaContext = Optional.of(schemaContext);
                currentSourceContext = yangSourceContext;
                return Optional.of(schemaContext);
            } catch (Exception e) {
                LOG.error("Could not create schema context for {} ", yangSourceContext.getValidSources(), e);
                return Optional.absent();
            }
        } else {
            currentSourceContext = yangSourceContext;
            return Optional.absent();
        }
    }
}
