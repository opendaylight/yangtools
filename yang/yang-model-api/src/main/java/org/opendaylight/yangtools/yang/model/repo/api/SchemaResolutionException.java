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
import java.util.Collections;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;

/**
 * Exception thrown when a Schema Source fails to resolve.
 */
@Beta
public class SchemaResolutionException extends SchemaSourceException {

    private static final long serialVersionUID = 1L;
    private final SourceIdentifier failedSource;
    private final Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports;
    private final Collection<SourceIdentifier> resolvedSources;

    public SchemaResolutionException(@Nonnull final String message) {
        this(message, null);
    }

    public SchemaResolutionException(@Nonnull final String message, final Throwable cause) {
        this(message, null, cause, Collections.emptySet(), ImmutableMultimap.of());
    }

    public SchemaResolutionException(@Nonnull final String message, final SourceIdentifier failedSource,
            final Throwable cause) {
        this(message, failedSource, cause, Collections.emptySet(), ImmutableMultimap.of());
    }

    public SchemaResolutionException(@Nonnull final String message, final Collection<SourceIdentifier> resolvedSources,
            final @Nonnull Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports) {
        this(message, null, null, resolvedSources, unsatisfiedImports);
    }

    public SchemaResolutionException(@Nonnull final String message, final SourceIdentifier failedSource,
            final Throwable cause, @Nonnull final Collection<SourceIdentifier> resolvedSources,
            @Nonnull final Multimap<SourceIdentifier, ModuleImport> unsatisfiedImports) {
        super(formatMessage(message, failedSource, resolvedSources, unsatisfiedImports), cause);
        this.failedSource = failedSource;
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
     * Return YANG schema source identifier consisting of name and revision of the module which caused this exception
     *
     * @return YANG schema source identifier
     */
    public final SourceIdentifier getFailedSource() {
        return this.failedSource;
    }

    /**
     * Return the list of sources which failed to resolve along with reasons
     * why they were not resolved.
     *
     * @return Source/reason map.
     */
    public final Multimap<SourceIdentifier, ModuleImport> getUnsatisfiedImports() {
        return unsatisfiedImports;
    }


    // FIXME: should be leak actual mapping?
    public final Collection<SourceIdentifier> getResolvedSources() {
        return resolvedSources;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("unsatisfiedImports", unsatisfiedImports)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper;
    }
}
