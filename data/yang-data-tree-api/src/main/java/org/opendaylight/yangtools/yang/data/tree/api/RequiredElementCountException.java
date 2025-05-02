/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;

/**
 * Exception thrown when {@code min-elements} or {@code max-element} statement restrictions are violated.
 */
@Beta
@NonNullByDefault
public final class RequiredElementCountException extends DataValidationFailedException
        implements YangNetconfErrorAware {
    @java.io.Serial
    private static final long serialVersionUID = 2L;

    private final @NonNull String appTag;

    public RequiredElementCountException(final YangInstanceIdentifier path, final String appTag, final String message) {
        super(path, message);
        this.appTag = requireNonNull(appTag);
    }

    public RequiredElementCountException(final YangInstanceIdentifier path, final String appTag, final String format,
            final Object... args) {
        this(path, appTag, String.format(format, args));
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.APPLICATION)
            .tag(ErrorTag.OPERATION_FAILED)
            .appTag(appTag)
            .build());
    }
}
