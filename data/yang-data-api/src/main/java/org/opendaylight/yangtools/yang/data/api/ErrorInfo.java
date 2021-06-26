/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * An element of {@code error-info} container, as modeled in {@code errorInfoType} of
 * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-B">RFC6241, Appendix B</a>.
 *
 * @param <T> Value type
 */
@Beta
@NonNullByDefault
public abstract class ErrorInfo<T> {
    private final QName name;
    private final T value;

    protected ErrorInfo(final QName name, final T value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }

    /**
     * The name of this {@code error-info} element.
     *
     * @return Element name.
     */
    final QName name() {
        return name;
    }

    /**
     * The value of this {@code error-info} element. This may be a simple or a complex type.
     *
     * @return Element value.
     */
    final T value() {
        return value;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("name", name).add("value", value);
    }
}