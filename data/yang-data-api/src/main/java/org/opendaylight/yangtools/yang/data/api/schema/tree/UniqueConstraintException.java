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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
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
    private final Map<Descendant, @Nullable Object> values;

    // FIXME: 8.0.0: this maps to a list of 'non-unique' YangInstanceIdentifiers, really. we should be getting
    //               a list of YangErrorInfo containing them -- but what about Serializability then?
    public UniqueConstraintException(final YangInstanceIdentifier path, final Map<Descendant, @Nullable Object> values,
            final String message) {
        super(path, message);
        this.values = requireNonNull(values);
    }

    // FIXME: 8.0.0: this should be completely nonnull, there is no point in reporting missing values
    public final Map<Descendant, @Nullable Object> values() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.APPLICATION)
            .tag(ErrorTag.OPERATION_FAILED)
            .appTag("data-not-unique")
            // FIXME: 8.0.0: and then we need to append YangErrorInfo here
            .build());
    }
}
