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
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Netconf.ErrorTag;
import org.opendaylight.yangtools.yang.common.YangError;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Exception thrown when a {@code unique} statement restrictions are violated.
 *
 * @author Robert Varga
 */
@Beta
public class UniqueConstraintException extends DataValidationFailedException implements YangError {
    private static final long serialVersionUID = 1L;

    // Note: this cannot be an ImmutableMap because we must support null values
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Best effort on serialization")
    private final Map<Descendant, @Nullable Object> values;

    public UniqueConstraintException(final YangInstanceIdentifier path, final Map<Descendant, @Nullable Object> values,
            final String message) {
        super(path, message);
        this.values = requireNonNull(values);
    }

    @Override
    public final ErrorTag getErrorTag() {
        return ErrorTag.OPERATION_FAILED;
    }

    @Override
    public final Optional<String> getErrorAppTag() {
        return Optional.of("data-not-unique");
    }

    public final Map<Descendant, @Nullable Object> values() {
        return Collections.unmodifiableMap(values);
    }
}
