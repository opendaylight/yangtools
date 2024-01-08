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
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 * An element of {@code error-info} container, as modeled in {@code errorInfoType} of
 * <a href="https://www.rfc-editor.org/rfc/rfc6241#appendix-B">RFC6241, Appendix B</a>.
 */
@Beta
@NonNullByDefault
// FIXME: 8.0.0: Split this into two interfaces + scenery:
//               - yang.common.ErrorInfoRepresentation
//               - make NormalizedNodeContainer implement ErrorInfoRepresentation
public final class YangErrorInfo {
    // FIXME: 8.0.0: implies it extends ErrorInfoRepresentation, but ... perhaps NormalizedErrorInfo
    private final DataContainerChild value;

    private YangErrorInfo(final DataContainerChild value) {
        this.value = requireNonNull(value);
    }

    public static YangErrorInfo of(final DataContainerChild value) {
        return new YangErrorInfo(value);
    }

    // FIXME: 8.0.0: yang.common version returns ErrorInfoRepresentation
    public DataContainerChild value() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof YangErrorInfo other && value.equals(other.value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).toString();
    }
}
