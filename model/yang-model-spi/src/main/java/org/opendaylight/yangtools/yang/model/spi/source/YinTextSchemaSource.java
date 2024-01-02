/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YIN text schema source representation. Exposes an RFC6020 or RFC7950 XML representation as an {@link InputStream}.
 */
public abstract class YinTextSchemaSource extends ByteSource implements YinSchemaSourceRepresentation {
    private static final Logger LOG = LoggerFactory.getLogger(YinTextSchemaSource.class);
    private static final String XML_EXTENSION = ".xml";

    private final @NonNull SourceIdentifier sourceId;

    protected YinTextSchemaSource(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    public static @NonNull SourceIdentifier identifierFromFilename(final String name) {
        final String baseName;
        if (name.endsWith(YangConstants.RFC6020_YIN_FILE_EXTENSION)) {
            baseName = name.substring(0, name.length() - YangConstants.RFC6020_YIN_FILE_EXTENSION.length());
        } else if (name.endsWith(XML_EXTENSION)) {
            // FIXME: BUG-7061: remove this once we do not need it
            LOG.warn("XML file {} being loaded as YIN", name);
            baseName = name.substring(0, name.length() - XML_EXTENSION.length());
        } else {
            throw new IllegalArgumentException("Filename " + name + " does not have a .yin or .xml extension");
        }

        final var parsed = YangNames.parseFilename(baseName);
        return new SourceIdentifier(parsed.getKey(), parsed.getValue());
    }

    @Override
    public final SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public final Class<YinTextSchemaSource> getType() {
        return YinTextSchemaSource.class;
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
    public static @NonNull YinTextSchemaSource delegateForByteSource(final SourceIdentifier identifier,
            final ByteSource delegate) {
        return new DelegatedYinTextSchemaSource(identifier, delegate);
    }

    public static @NonNull YinTextSchemaSource forPath(final Path path) {
        checkArgument(Files.isRegularFile(path), "Supplied path %s is not a regular file", path);
        return new YinTextFileSchemaSource(identifierFromFilename(path.toFile().getName()), path);
    }

    public static @NonNull YinTextSchemaSource forResource(final Class<?> clazz, final String resourceName) {
        final String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
        return new ResourceYinTextSchemaSource(identifierFromFilename(fileName),
            Resources.getResource(clazz, resourceName));
    }
}
