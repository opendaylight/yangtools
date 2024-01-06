/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.CharSource;
import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * YANG text schema source representation. Exposes an RFC6020 or RFC7950 text representation as an {@link InputStream}.
 */
@NonNullByDefault
public abstract class YangTextSource extends CharSource implements YangSourceRepresentation {
    private final SourceIdentifier sourceId;

    protected YangTextSource(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
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
