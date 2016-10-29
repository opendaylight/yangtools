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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.common.YangNames;

/**
 * YANG text schema source representation. Exposes an RFC6020 text representation
 * as an {@link InputStream}.
 */
@Beta
public abstract class YangTextSchemaSource extends ByteSource implements YangSchemaSourceRepresentation {
    private final SourceIdentifier identifier;

    protected YangTextSchemaSource(final SourceIdentifier identifier) {
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    public static SourceIdentifier identifierFromFilename(final String name) {
        checkArgument(name.endsWith(".yang"), "Filename %s does not have a .yang extension", name);

        final String baseName = name.substring(0, name.length() - 5);
        final Entry<String, String> parsed = YangNames.parseFilename(baseName);
        return RevisionSourceIdentifier.create(parsed.getKey(), Optional.fromNullable(parsed.getValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SourceIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
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
    protected abstract ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper);

    /**
     * Create a new YangTextSchemaSource with a specific source identifier and backed
     * by ByteSource, which provides the actual InputStreams.
     *
     * @param identifier SourceIdentifier of the resulting schema source
     * @param delegate Backing ByteSource instance
     * @return A new YangTextSchemaSource
     */
    public static YangTextSchemaSource delegateForByteSource(final SourceIdentifier identifier, final ByteSource delegate) {
        return new DelegatedYangTextSchemaSource(identifier, delegate);
    }

    private static final class DelegatedYangTextSchemaSource extends YangTextSchemaSource implements Delegator<ByteSource> {
        private final ByteSource delegate;

        private DelegatedYangTextSchemaSource(final SourceIdentifier identifier, final ByteSource delegate) {
            super(identifier);
            this.delegate = Preconditions.checkNotNull(delegate);
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
