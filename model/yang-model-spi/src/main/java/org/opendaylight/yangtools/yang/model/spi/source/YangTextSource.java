/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.CharSource;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;

/**
 * YANG text schema source representation. Exposes an RFC6020 or RFC7950 text representation as an {@link InputStream}.
 */
public abstract class YangTextSource extends CharSource implements YangSourceRepresentation {
    private final @NonNull SourceIdentifier sourceId;

    protected YangTextSource(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    /**
     * Create a new YangTextSchemaSource backed by a {@link File} with {@link SourceIdentifier} derived from the file
     * name.
     *
     * @param path Backing path
     * @return A new YangTextSchemaSource
     * @throws IllegalArgumentException if the file name has invalid format or if the supplied File is not a file
     * @throws NullPointerException if file is {@code null}
     */
    public static @NonNull YangTextSource forPath(final Path path) {
        // FIXME: do not use .toFile() here
        return forPath(path, SourceIdentifier.ofYangFileName(path.toFile().getName()));
    }

    /**
     * Create a new YangTextSchemaSource backed by a {@link File} and specified {@link SourceIdentifier}.
     *
     * @param path Backing path
     * @param identifier source identifier
     * @return A new YangTextSchemaSource
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if the supplied path is not a regular file
     */
    public static @NonNull YangTextSource forPath(final Path path, final SourceIdentifier identifier) {
        return forPath(path, identifier, StandardCharsets.UTF_8);
    }

    /**
     * Create a new YangTextSchemaSource backed by a {@link File} and specified {@link SourceIdentifier}.
     *
     * @param path Backing path
     * @param identifier Source identifier
     * @param charset expected stream character set
     * @return A new YangTextSchemaSource
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if the supplied path is not a regular file
     */
    public static @NonNull YangTextSource forPath(final Path path, final SourceIdentifier identifier,
            final Charset charset) {
        if (Files.isRegularFile(path)) {
            return new YangTextFileSource(identifier, path, charset);
        }
        throw new IllegalArgumentException("Supplied path " + path + " is not a regular file");
    }

    @Override
    public final Class<YangTextSource> getType() {
        return YangTextSource.class;
    }

    @Override
    public final SourceIdentifier sourceId() {
        return sourceId;
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
}
