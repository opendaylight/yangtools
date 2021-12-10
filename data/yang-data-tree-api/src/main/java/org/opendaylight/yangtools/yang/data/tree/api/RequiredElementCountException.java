/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.OptionalInt;
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
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class RequiredElementCountException extends DataValidationFailedException
        implements YangNetconfErrorAware {
    private static final long serialVersionUID = 1L;

    private final int actualCount;
    private final int minimumCount;
    private final int maximumCount;

    public RequiredElementCountException(final YangInstanceIdentifier path, final int actualCount,
            final int minimumCount, final int maximumCount, final String message) {
        super(path, message);
        this.minimumCount = minimumCount;
        this.maximumCount = maximumCount;
        this.actualCount = actualCount;
    }

    public RequiredElementCountException(final YangInstanceIdentifier path, final int actualCount,
            final int minimumCount, final int maximumCount, final String format, final Object... args) {
        this(path, actualCount, minimumCount, maximumCount, String.format(format, args));
    }

    public OptionalInt getMinimumCount() {
        return minimumCount == 0 ? OptionalInt.empty() : OptionalInt.of(minimumCount);
    }

    public OptionalInt getMaximumCount() {
        return maximumCount == Integer.MAX_VALUE ? OptionalInt.empty() : OptionalInt.of(maximumCount);
    }

    public int getActualCount() {
        return actualCount;
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        final String appTag;
        if (actualCount < minimumCount) {
            appTag = "too-few-elements";
        } else if (actualCount > maximumCount) {
            appTag = "too-many-elements";
        } else {
            throw new IllegalStateException(
                "Invalid min " + minimumCount + " max " + maximumCount + " actual " + actualCount);
        }

        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.APPLICATION)
            .tag(ErrorTag.OPERATION_FAILED)
            .appTag(appTag)
            .build());
    }
}
