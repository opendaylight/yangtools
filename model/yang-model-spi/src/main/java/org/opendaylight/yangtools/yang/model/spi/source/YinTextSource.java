/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;

/**
 * YIN text schema source representation. Exposes an RFC6020 or RFC7950 XML representation as an {@link InputStream}.
 */
public abstract class YinTextSource extends ByteSource implements YinSourceRepresentation {
    private final @NonNull SourceIdentifier sourceId;

    protected YinTextSource(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    @Override
    public final SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public final Class<YinTextSource> getType() {
        return YinTextSource.class;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    /**
     * Add subclass-specific attributes to the output {@link #toString()} output. Since
     * subclasses are prevented from overriding {@link #toString()} for consistency
     * reasons, they can add their specific attributes to the resulting string by attaching
     * attributes to the supplied {@link ToStringHelper}.
     *
     * @param toStringHelper ToStringHelper onto the attributes can be added
     * @return ToStringHelper supplied as input argument.
     */
    protected ToStringHelper addToStringAttributes(final @NonNull ToStringHelper toStringHelper) {
        return toStringHelper.add("identifier", sourceId);
    }

    /**
     * Create a new YinTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing ByteSource instance
     * @return A new YinTextSchemaSource
     */
    public static @NonNull YinTextSource delegateForByteSource(final SourceIdentifier identifier,
            final ByteSource delegate) {
        return new DelegatedYinTextSource(identifier, delegate);
    }

    public static @NonNull YinTextSource forPath(final Path path) {
        if (Files.isRegularFile(path)) {
            // FIXME: do not use toFile() here
            return new YinTextFileSource(SourceIdentifier.ofYinFileName(path.toFile().getName()), path);
        }
        throw new IllegalArgumentException("Supplied path " + path + " is not a regular file");
    }

    public static @NonNull YinTextSource forResource(final Class<?> clazz, final String resourceName) {
        final String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        return new ResourceYinTextSource(SourceIdentifier.ofYinFileName(fileName),
            Resources.getResource(clazz, resourceName));
    }
}
