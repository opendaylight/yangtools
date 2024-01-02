/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;

/**
 * Exception thrown when a Schema Source fails to resolve.
 */
@Beta
public class SchemaResolutionException extends SchemaSourceException {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull ImmutableMultimap<SourceIdentifier, ModuleImport> unsatisfiedImports;
    private final @NonNull ImmutableList<SourceIdentifier> resolvedSources;

    public SchemaResolutionException(final @NonNull String message, final SourceIdentifier failedSource,
            final Throwable cause) {
        this(message, failedSource, cause, ImmutableList.of(), ImmutableMultimap.of());
    }

    public SchemaResolutionException(final @NonNull String message, final SourceIdentifier failedSource,
            final @NonNull Collection<SourceIdentifier> resolvedSources,
            final @NonNull Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports) {
        super(failedSource, formatMessage(message, failedSource, resolvedSources, unsatisfiedImports), null);
        this.unsatisfiedImports = ImmutableMultimap.copyOf(unsatisfiedImports);
        this.resolvedSources = ImmutableList.copyOf(resolvedSources);
    }

    public SchemaResolutionException(final @NonNull String message, final SourceIdentifier failedSource,
            final Throwable cause, final @NonNull Collection<SourceIdentifier> resolvedSources,
            final @NonNull Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports) {
        super(failedSource, formatMessage(message, failedSource, resolvedSources, unsatisfiedImports), cause);
        this.unsatisfiedImports = ImmutableMultimap.copyOf(unsatisfiedImports);
        this.resolvedSources = ImmutableList.copyOf(resolvedSources);
    }

    private static String formatMessage(final String message, final SourceIdentifier failedSource,
            final Collection<SourceIdentifier> resolvedSources,
            final Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports) {
        return String.format("%s, failed source: %s, resolved sources: %s, unsatisfied imports: %s", message,
                failedSource, resolvedSources, unsatisfiedImports);
    }

    /**
     * Return the list of sources which failed to resolve along with reasons why they were not resolved.
     *
     * @return Source/reason map.
     */
    public final @NonNull Multimap<SourceIdentifier, ModuleImport> getUnsatisfiedImports() {
        return unsatisfiedImports;
    }

    // FIXME: should be leak actual mapping?
    public final @NonNull Collection<SourceIdentifier> getResolvedSources() {
        return resolvedSources;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("unsatisfiedImports", unsatisfiedImports))
            .toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper;
    }
}
