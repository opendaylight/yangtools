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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An element of {@code error-info} container, as modeled in {@code errorInfoType} of
 * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-B">RFC6241, Appendix B</a>.
 *
 * @param <T> Value type
 */
@Beta
@NonNullByDefault
public final class YangErrorInfo<T extends NormalizedNode> {
    private final T value;

    private YangErrorInfo(final T value) {
        this.value = requireNonNull(value);
    }

    public static <T extends NormalizedNode> YangErrorInfo<T> of(final T value) {
        return new YangErrorInfo<>(value);
    }

    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("value", value);
    }
}
