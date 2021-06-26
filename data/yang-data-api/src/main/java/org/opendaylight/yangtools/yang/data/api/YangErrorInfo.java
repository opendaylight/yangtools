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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An element of {@code error-info} container, as modeled in {@code errorInfoType} of
 * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-B">RFC6241, Appendix B</a>.
 */
@Beta
@NonNullByDefault
public final class YangErrorInfo {
    private final NormalizedNode value;

    private YangErrorInfo(final NormalizedNode value) {
        this.value = requireNonNull(value);
    }

    public static YangErrorInfo of(final NormalizedNode value) {
        return new YangErrorInfo(value);
    }

    public NormalizedNode value() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof YangErrorInfo && value.equals(((YangErrorInfo) obj).value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).toString();
    }
}
