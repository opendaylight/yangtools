/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.io.ByteSource;
import java.io.InputStream;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * YIN text schema source representation. Exposes an RFC6020 or RFC7950 XML representation as an {@link InputStream}.
 */
@NonNullByDefault
public abstract class YinTextSource extends ByteSource implements YinSourceRepresentation {
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
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("sourceId", sourceId());
    }
}
