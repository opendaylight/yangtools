/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.repo.AdvancedSchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

public class YangSourceContext implements AdvancedSchemaSourceProvider<InputStream>, AutoCloseable {

    private final ImmutableSet<SourceIdentifier> validSources;

    private final ImmutableSet<SourceIdentifier> missingSources;
    private final ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependencies;
    private AdvancedSchemaSourceProvider<InputStream> sourceProvider;

    YangSourceContext(final ImmutableSet<SourceIdentifier> validSourcesSet,
            final ImmutableSet<SourceIdentifier> missingSourcesSet,
            final ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependenciesMap,
            final AdvancedSchemaSourceProvider<InputStream> sourceProvicer) {
        validSources = validSourcesSet;
        missingSources = missingSourcesSet;
        missingDependencies = missingDependenciesMap;
        sourceProvider = sourceProvicer;
    }

    public ImmutableSet<SourceIdentifier> getValidSources() {
        return validSources;
    }

    public ImmutableSet<SourceIdentifier> getMissingSources() {
        return missingSources;
    }

    public ImmutableMultimap<SourceIdentifier, ModuleImport> getMissingDependencies() {
        return missingDependencies;
    }

    @Override
    public Optional<InputStream> getSchemaSource(final String moduleName, final Optional<String> revision) {
        return getSchemaSource(SourceIdentifier.create(moduleName, revision));
    }

    @Override
    public Optional<InputStream> getSchemaSource(final SourceIdentifier sourceIdentifier) {
        if (validSources.contains(sourceIdentifier)) {
            return getDelegateChecked().getSchemaSource(sourceIdentifier);
        }
        return Optional.absent();
    }

    private AdvancedSchemaSourceProvider<InputStream> getDelegateChecked() {
        Preconditions.checkState(sourceProvider != null, "Instance is already closed.");
        return sourceProvider;
    }

    @Override
    public void close() {
        if (sourceProvider != null) {
            sourceProvider = null;
        }
    }

    public static final YangSourceContext createFrom(final Iterable<QName> capabilities,
            final SchemaSourceProvider<InputStream> schemaSourceProvider) {
        YangSourceContextResolver resolver = new YangSourceFromCapabilitiesResolver(capabilities, schemaSourceProvider);
        return resolver.resolveContext();
    }

    public static final YangSourceContext createFrom(final Map<SourceIdentifier, YangModelDependencyInfo> moduleDependencies) {
        YangSourceContextResolver resolver = new YangSourceFromDependencyInfoResolver(moduleDependencies);
        return resolver.resolveContext();
    }

    public static final SchemaContext toSchemaContext(final YangSourceContext context) {
        List<InputStream> inputStreams = getValidInputStreams(context);
        YangParserImpl parser = new YangParserImpl();
        Set<Module> models = parser.parseYangModelsFromStreams(inputStreams);
        return parser.resolveSchemaContext(models);
    }

    public static List<InputStream> getValidInputStreams(final YangSourceContext context) {
        return getValidInputStreams(context, context.sourceProvider);
    }

    public static List<InputStream> getValidInputStreams(final YangSourceContext context,
            final AdvancedSchemaSourceProvider<InputStream> provider) {
        final HashSet<SourceIdentifier> sourcesToLoad = new HashSet<>();
        sourcesToLoad.addAll(context.getValidSources());
        for (SourceIdentifier source : context.getValidSources()) {
            if (source.getRevision() != null) {
                SourceIdentifier sourceWithoutRevision = SourceIdentifier.create(source.getName(),
                        Optional.<String> absent());
                sourcesToLoad.removeAll(Collections.singleton(sourceWithoutRevision));
            }
        }

        ImmutableList.Builder<InputStream> ret = ImmutableList.<InputStream> builder();
        for (SourceIdentifier sourceIdentifier : sourcesToLoad) {
            Optional<InputStream> source = provider.getSchemaSource(sourceIdentifier);
            ret.add(source.get());
        }
        return ret.build();
    }
}
