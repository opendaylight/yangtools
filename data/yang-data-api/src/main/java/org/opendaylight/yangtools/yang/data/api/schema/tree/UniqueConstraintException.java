/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangErrorInfo;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Exception thrown when a {@code unique} statement restrictions are violated.
 *
 * @author Robert Varga
 */
@Beta
public class UniqueConstraintException extends DataValidationFailedException implements YangNetconfErrorAware {
    private static final long serialVersionUID = 1L;

    // Note: this cannot be an ImmutableMap because we must support null values
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Best effort on serialization")
    private final @NonNull YangErrorInfo<?, ?> info;

    public UniqueConstraintException(final YangInstanceIdentifier path, final String message,
            final YangErrorInfo<?, ?> info) {
        super(path, message);
        this.info = requireNonNull(info);
    }

    public final @NonNull YangErrorInfo<?, ?> getInfo() {
        return info;
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.APPLICATION)
            .tag(ErrorTag.OPERATION_FAILED)
            .appTag("data-not-unique")
            .info(info)
            .build());
    }
}
