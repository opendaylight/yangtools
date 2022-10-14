/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;
import static org.opendaylight.yangtools.yang.common.YangNames.parseFilename;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNull;

/**
 * YANG text schema source representation. Exposes an RFC6020 or RFC7950 text representation as an {@link InputStream}.
 */
@Beta
public abstract class YangTextSchemaSource extends ByteSource implements YangSchemaSourceRepresentation {
    private final @NonNull SourceIdentifier identifier;

    protected YangTextSchemaSource(final SourceIdentifier identifier) {
        this.identifier = requireNonNull(identifier);
    }

    public static @NonNull SourceIdentifier identifierFromFilename(final String name) {
        checkArgument(name.endsWith(RFC6020_YANG_FILE_EXTENSION), "Filename '%s' does not end with '%s'", name,
            RFC6020_YANG_FILE_EXTENSION);

        final String baseName = name.substring(0, name.length() - RFC6020_YANG_FILE_EXTENSION.length());
        final var parsed = parseFilename(baseName);
        return new SourceIdentifier(parsed.getKey(), parsed.getValue());
    }

    /**
     * Create a new YangTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing ByteSource instance
     * @return A new YangTextSchemaSource
     */
    public static @NonNull YangTextSchemaSource delegateForByteSource(final SourceIdentifier identifier,
            final ByteSource delegate) {
        return new DelegatedYangTextSchemaSource(identifier, delegate);
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
    public static @NonNull YangTextSchemaSource delegateForByteSource(final String fileName,
            final ByteSource delegate) {
        return new DelegatedYangTextSchemaSource(identifierFromFilename(fileName), delegate);
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
    public static @NonNull YangTextSchemaSource forPath(final Path path) {
        return forPath(path, identifierFromFilename(path.toFile().getName()));
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
    public static @NonNull YangTextSchemaSource forPath(final Path path, final SourceIdentifier identifier) {
        checkArgument(Files.isRegularFile(path), "Supplied path %s is not a regular file", path);
        return new YangTextFileSchemaSource(identifier, path);
    }

    /**
     * Create a new {@link YangTextSchemaSource} backed by a resource available in the ClassLoader where this
     * class resides.
     *
     * @param resourceName Resource name
     * @return A new instance.
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public static @NonNull YangTextSchemaSource forResource(final String resourceName) {
        return forResource(YangTextSchemaSource.class, resourceName);
    }

    /**
     * Create a new {@link YangTextSchemaSource} backed by a resource by a resource available on the ClassLoader
     * which loaded the specified class.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @return A new instance.
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public static @NonNull YangTextSchemaSource forResource(final Class<?> clazz, final String resourceName) {
        final String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        final SourceIdentifier identifier = identifierFromFilename(fileName);
        final URL url = Resources.getResource(clazz, resourceName);
        return new ResourceYangTextSchemaSource(identifier, url);
    }

    /**
     * Create a new {@link YangTextSchemaSource} backed by a URL.
     *
     * @param url Backing URL
     * @param identifier Source identifier
     * @return A new instance.
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull YangTextSchemaSource forURL(final URL url, final SourceIdentifier identifier) {
        return new ResourceYangTextSchemaSource(identifier, url);
    }

    @Override
    public final SourceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public final Class<YangTextSchemaSource> getType() {
        return YangTextSchemaSource.class;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("identifier", identifier)).toString();
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
    protected abstract ToStringHelper addToStringAttributes(ToStringHelper toStringHelper);
}
