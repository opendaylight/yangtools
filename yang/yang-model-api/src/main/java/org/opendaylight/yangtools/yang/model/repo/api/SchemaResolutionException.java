/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Exception thrown when a Schema Source fails to resolve.
 */
public class SchemaResolutionException extends Exception {
    private static final long serialVersionUID = 1L;
    private final Map<SourceIdentifier, Throwable> unresolvedSources;

    public SchemaResolutionException(final @Nonnull String message) {
        this(message, (Throwable)null);
    }

    public SchemaResolutionException(final @Nonnull String message, final Throwable cause) {
        this(message, cause, ImmutableMap.<SourceIdentifier, Exception>of());
    }

    public SchemaResolutionException(final @Nonnull String message, final @Nonnull Map<SourceIdentifier, ? extends Throwable> unresolvedSources) {
        super(Preconditions.checkNotNull(message));
        this.unresolvedSources = ImmutableMap.copyOf(unresolvedSources);
    }

    public SchemaResolutionException(final @Nonnull String message, final Throwable cause, @Nonnull final Map<SourceIdentifier, ? extends Throwable> unresolvedSources) {
        super(message, cause);
        this.unresolvedSources = ImmutableMap.copyOf(unresolvedSources);
    }

    /**
     * Return the list of sources which failed to resolve along with reasons
     * why they were not resolved.
     *
     * @return Source/reason map.
     */
    public final Map<SourceIdentifier, Throwable> getUnresolvedSources() {
        return unresolvedSources;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(Objects.toStringHelper(this).add("unresolvedSources", unresolvedSources)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper;
    }
}
