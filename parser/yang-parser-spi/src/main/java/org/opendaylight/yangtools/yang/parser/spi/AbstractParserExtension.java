/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for {@link ParserExtension} implementations.
 */
@NonNullByDefault
public abstract class AbstractParserExtension implements ParserExtension {
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    /**
     * Add any extension-specific attributes.
     *
     * @implSpec default implementation adds to
     *
     * @param helper the {@link ToStringHelper}, ignoring {@code null} values
     * @return the helper
     */
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}
