/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.Serial;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;

/**
 * Dedicated exception for reporting conditions where {@code error-tag} should be reported as {@code invalid-value}.
 * This is covered by <a href="https://tools.ietf.org/html/rfc7950#section-8.3.1">RFC7950 section 8.3.1</a>:
 * <pre>
 *   If a leaf data value does not match the type constraints for the
 *   leaf, including those defined in the type's "range", "length", and
 *   "pattern" properties, the server MUST reply with an
 *   "invalid-value" &lt;error-tag&gt; in the &lt;rpc-error&gt;, and with the
 *   error-app-tag (Section 7.5.4.2) and error-message
 *   (Section 7.5.4.1) associated with the constraint, if any exist.
 * </pre>
 *
 * <p>
 * This error tag also references <a href="https://tools.ietf.org/html/rfc6241#appendix-A">RFC6241 Appendix A</a>,
 * which defines the appropriate severity and adds more semantics.
 */
@Beta
public class YangInvalidValueException extends IllegalArgumentException implements YangNetconfErrorAware {
    @Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull ErrorType errorType;
    private final @Nullable String errorAppTag;
    private final @Nullable String errorMessage;

    public YangInvalidValueException(final ErrorType errorType, final ConstraintMetaDefinition constraint,
            final String message) {
        super(requireNonNull(message));
        this.errorType = requireNonNull(errorType);
        errorAppTag = constraint.getErrorAppTag().orElse(null);
        errorMessage = constraint.getErrorMessage().orElse(null);
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return List.of(ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(errorType)
            .tag(ErrorTag.INVALID_VALUE)
            .message(errorMessage)
            .appTag(errorAppTag)
            .build());
    }
}
