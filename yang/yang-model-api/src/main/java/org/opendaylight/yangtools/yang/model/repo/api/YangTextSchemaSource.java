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
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * YANG text schema source representation. Exposes an RFC6020 or RFC7950 text representation
 * as an {@link InputStream}.
 */
@Beta
public abstract class YangTextSchemaSource extends ByteSource implements YangSchemaSourceRepresentation {
    private final @NonNull SourceIdentifier identifier;

    protected YangTextSchemaSource(final @NonNull SourceIdentifier identifier) {
        this.identifier = requireNonNull(identifier);
    }

    public static @NonNull SourceIdentifier identifierFromFilename(final @NonNull String name) {
        checkArgument(name.endsWith(RFC6020_YANG_FILE_EXTENSION), "Filename %s does not end with '%s'",
            RFC6020_YANG_FILE_EXTENSION, name);

        final String baseName = name.substring(0, name.length() - RFC6020_YANG_FILE_EXTENSION.length());
        final Entry<String, String> parsed = parseFilename(baseName);
        return RevisionSourceIdentifier.create(parsed.getKey(), Revision.ofNullable(parsed.getValue()));
    }

    /**
     * Create a new YangTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing ByteSource instance
     * @return A new YangTextSchemaSource
     */
    public static @NonNull YangTextSchemaSource delegateForByteSource(final @NonNull SourceIdentifier identifier,
            final @NonNull ByteSource delegate) {
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
    public static @NonNull YangTextSchemaSource delegateForByteSource(final @NonNull String fileName,
            final @NonNull ByteSource delegate) {
        return new DelegatedYangTextSchemaSource(identifierFromFilename(fileName), delegate);
    }

    /**
     * Create a new YangTextSchemaSource backed by a {@link File} with {@link SourceIdentifier} derived from the file
     * name.
     *
     * @param file Backing File
     * @return A new YangTextSchemaSource
     * @throws IllegalArgumentException if the file name has invalid format or if the supplied File is not a file
     * @throws NullPointerException if file is null
     */
    public static @NonNull YangTextSchemaSource forFile(final @NonNull File file) {
        checkArgument(file.isFile(), "Supplied file %s is not a file", file);
        return new YangTextFileSchemaSource(identifierFromFilename(file.getName()), file);
    }

    /**
     * Create a new {@link YangTextSchemaSource} backed by a resource available in the ClassLoader where this
     * class resides.
     *
     * @param resourceName Resource name
     * @return A new instance.
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    // FIXME: 3.0.0: YANGTOOLS-849: return YangTextSchemaSource
    public static @NonNull ResourceYangTextSchemaSource forResource(final @NonNull String resourceName) {
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
    // FIXME: 3.0.0: YANGTOOLS-849: return YangTextSchemaSource
    public static @NonNull ResourceYangTextSchemaSource forResource(final @NonNull Class<?> clazz,
            final @NonNull String resourceName) {
        final String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        final SourceIdentifier identifier = identifierFromFilename(fileName);
        final URL url = Resources.getResource(clazz, resourceName);
        return new ResourceYangTextSchemaSource(identifier, url);
    }

    @Override
    public final SourceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public Class<? extends YangTextSchemaSource> getType() {
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
