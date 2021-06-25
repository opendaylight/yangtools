/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.ImmutableYangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangErrorInfo;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;

/**
 * Dedicated exception for reporting conditions where {@code error-tag} should be reported as {@code missing-element}.
 * This is covered by <a href="https://tools.ietf.org/html/rfc7950#section-8.3.1">RFC7950 section 8.3.1</a>:
 * <pre>
 *   If all keys of a list entry are not present, the server MUST reply
 *   with a "missing-element" error-tag in the rpc-error.
 * </pre>
 *
 * <p>
 * This error tag also references <a href="https://tools.ietf.org/html/rfc6241#appendix-A">RFC6241 Appendix A</a>,
 * which defines the appropriate severity and adds more semantics.
 */
@Beta
public class YangMissingKeyException extends IllegalArgumentException implements YangNetconfErrorAware {
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "NormalizedNode-based storage")
    private final @NonNull YangNetconfError error;

    public YangMissingKeyException(final Collection<YangErrorInfo> missingKeys) {
        super("List entry is missing keys " + requireNonNull(missingKeys));
        checkArgument(!missingKeys.isEmpty(), "Missing keys cannot be empty");
        error = ImmutableYangNetconfError.builder()
            .severity(ErrorSeverity.ERROR)
            .type(ErrorType.APPLICATION)
            .tag(ErrorTag.MISSING_ELEMENT)
            // Note: retains iteration order
            .addAllInfo(ImmutableSet.copyOf(missingKeys))
            .build();
    }

    @Override
    public List<YangNetconfError> getNetconfErrors() {
        return List.of(error);
    }
}
