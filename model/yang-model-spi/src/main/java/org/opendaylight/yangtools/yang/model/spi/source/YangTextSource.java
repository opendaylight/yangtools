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
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
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
     * Create a new YangTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing ByteSource instance
     * @param charset Expected character set
     * @return A new YangTextSchemaSource
     */
    public static @NonNull YangTextSource delegateForByteSource(final SourceIdentifier identifier,
            final ByteSource delegate, final Charset charset) {
        return delegateForCharSource(identifier, delegate.asCharSource(charset));
    }

    /**
     * Create a new YangTextSchemaSource with {@link SourceIdentifier} derived from a supplied filename and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param fileName File name
     * @param delegate Backing ByteSource instance
     * @return A new YangTextSchemaSource
     * @throws IllegalArgumentException if the file name has invalid format
     */
    public static @NonNull YangTextSource delegateForByteSource(final String fileName,
            final ByteSource delegate, final Charset charset) {
        return delegateForCharSource(fileName, delegate.asCharSource(charset));
    }

    /**
     * Create a new YangTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing CharSource instance
     * @return A new YangTextSchemaSource
     */
    public static @NonNull YangTextSource delegateForCharSource(final SourceIdentifier identifier,
            final CharSource delegate) {
        return new DelegatedYangTextSource(identifier, delegate);
    }

    /**
     * Create a new YangTextSchemaSource with {@link SourceIdentifier} derived from a supplied filename and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param fileName File name
     * @param delegate Backing CharSource instance
     * @return A new YangTextSchemaSource
     * @throws IllegalArgumentException if the file name has invalid format
     */
    public static @NonNull YangTextSource delegateForCharSource(final String fileName,
            final CharSource delegate) {
        return new DelegatedYangTextSource(SourceIdentifier.ofYangFileName(fileName), delegate);
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

    /**
     * Create a new {@link YangTextSource} backed by a resource available in the ClassLoader where this
     * class resides.
     *
     * @param resourceName Resource name
     * @return A new instance.
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public static @NonNull YangTextSource forResource(final String resourceName) {
        return forResource(YangTextSource.class, resourceName);
    }

    /**
     * Create a new {@link YangTextSource} backed by a resource by a resource available on the ClassLoader
     * which loaded the specified class.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @return A new instance.
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public static @NonNull YangTextSource forResource(final Class<?> clazz, final String resourceName) {
        return forResource(clazz, resourceName, StandardCharsets.UTF_8);
    }

    /**
     * Create a new {@link YangTextSource} backed by a resource by a resource available on the ClassLoader
     * which loaded the specified class.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @param charset Expected character set
     * @return A new instance.
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public static @NonNull YangTextSource forResource(final Class<?> clazz, final String resourceName,
            final Charset charset) {
        final var fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        final var identifier = SourceIdentifier.ofYangFileName(fileName);
        final var url = Resources.getResource(clazz, resourceName);
        return new ResourceYangTextSource(identifier, url, charset);
    }


    /**
     * Create a new {@link YangTextSource} backed by a URL.
     *
     * @param url Backing URL
     * @param identifier Source identifier
     * @return A new instance.
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull YangTextSource forURL(final URL url, final SourceIdentifier identifier) {
        return forURL(url, identifier, StandardCharsets.UTF_8);
    }

    /**
     * Create a new {@link YangTextSource} backed by a URL.
     *
     * @param url Backing URL
     * @param identifier Source identifier
     * @param charset Expected character set
     * @return A new instance.
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull YangTextSource forURL(final URL url, final SourceIdentifier identifier,
            final Charset charset) {
        return new ResourceYangTextSource(identifier, url, charset);
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
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("identifier", sourceId);
    }
}
