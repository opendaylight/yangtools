/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YIN text schema source representation. Exposes an RFC6020 or RFC7950 XML representation as an {@link InputStream}.
 */
@Beta
public abstract class YinTextSchemaSource extends ByteSource implements YinSchemaSourceRepresentation {
    private static final Logger LOG = LoggerFactory.getLogger(YinTextSchemaSource.class);
    private static final String XML_EXTENSION = ".xml";

    private final SourceIdentifier identifier;

    protected YinTextSchemaSource(final SourceIdentifier identifier) {
        this.identifier = requireNonNull(identifier);
    }

    public static SourceIdentifier identifierFromFilename(final String name) {
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

        final Entry<String, String> parsed = YangNames.parseFilename(baseName);
        return RevisionSourceIdentifier.create(parsed.getKey(), Optional.ofNullable(parsed.getValue()));
    }

    @Override
    public final SourceIdentifier getIdentifier() {
        return identifier;
    }

    @Nonnull
    @Override
    public Class<? extends YinTextSchemaSource> getType() {
        return YinTextSchemaSource.class;
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

    /**
     * Create a new YinTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing ByteSource instance
     * @return A new YinTextSchemaSource
     */
    public static YinTextSchemaSource delegateForByteSource(final SourceIdentifier identifier,
            final ByteSource delegate) {
        return new DelegatedYinTextSchemaSource(identifier, delegate);
    }

    private static final class DelegatedYinTextSchemaSource extends YinTextSchemaSource
            implements Delegator<ByteSource> {
        private final ByteSource delegate;

        private DelegatedYinTextSchemaSource(final SourceIdentifier identifier, final ByteSource delegate) {
            super(identifier);
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public ByteSource getDelegate() {
            return delegate;
        }

        @Override
        public InputStream openStream() throws IOException {
            return delegate.openStream();
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper.add("delegate", delegate);
        }
    }
}
