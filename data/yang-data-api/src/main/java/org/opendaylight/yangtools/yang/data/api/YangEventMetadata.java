/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Netconf.AbstractEventMetadata;
import org.opendaylight.yangtools.yang.common.Netconf.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.Netconf.ErrorTag;
import org.opendaylight.yangtools.yang.common.Netconf.ErrorType;
import org.opendaylight.yangtools.yang.common.YangError;

/**
 * A {@link YangError} potentially carrying an {@code error-path}.
 */
@Beta
@NonNullByDefault
public final class YangEventMetadata extends AbstractEventMetadata {
    private final @Nullable YangInstanceIdentifier errorPath;

    public YangEventMetadata(final ErrorSeverity severity, final ErrorType type, final ErrorTag tag) {
        this(severity, type, tag, null, null, null);
    }

    public YangEventMetadata(final ErrorSeverity severity, final ErrorType type, final ErrorTag tag,
            final @Nullable String message, final @Nullable String appTag,
            final @Nullable YangInstanceIdentifier errorPath) {
        super(severity, type, tag, message, appTag);
        this.errorPath = errorPath;
    }
    /**
     * Returns the data path triggering this error, if available.
     *
     * @return path triggering this error, or null.
     */
    public @Nullable YangInstanceIdentifier errorPath() {
        return errorPath;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("errorPath", errorPath);
    }
}
