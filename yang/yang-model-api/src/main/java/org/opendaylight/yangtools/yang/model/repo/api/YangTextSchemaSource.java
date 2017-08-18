/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangNames;

/**
 * YANG text schema source representation. Exposes an RFC6020 or RFC7950 text representation
 * as an {@link InputStream}.
 */
@Beta
public abstract class YangTextSchemaSource extends ByteSource implements YangSchemaSourceRepresentation {
    private final SourceIdentifier identifier;

    protected YangTextSchemaSource(final SourceIdentifier identifier) {
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    public static SourceIdentifier identifierFromFilename(final String name) {
        checkArgument(name.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION),
            "Filename %s does not have a .yang extension", name);

        final String baseName = name.substring(0, name.length() - YangConstants.RFC6020_YANG_FILE_EXTENSION.length());
        final Entry<String, String> parsed = YangNames.parseFilename(baseName);
        return RevisionSourceIdentifier.create(parsed.getKey(), Optional.ofNullable(parsed.getValue()));
    }

    /**
     * Create a new YangTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing ByteSource instance
     * @return A new YangTextSchemaSource
     */
    public static YangTextSchemaSource delegateForByteSource(final SourceIdentifier identifier,
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
    public static YangTextSchemaSource delegateForByteSource(final String fileName, final ByteSource delegate) {
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
    public static YangTextSchemaSource forFile(final File file) {
        Preconditions.checkArgument(file.isFile(), "Supplied file %s is not a file");
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
    public static ResourceYangTextSchemaSource forResource(final String resourceName) {
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
    public static ResourceYangTextSchemaSource forResource(final Class<?> clazz, final String resourceName) {
        final String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        final SourceIdentifier identifier = identifierFromFilename(fileName);
        final URL url = Resources.getResource(clazz, resourceName);
        return new ResourceYangTextSchemaSource(identifier, url);
    }

    @Override
    public final SourceIdentifier getIdentifier() {
        return identifier;
    }

    @Nonnull
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
