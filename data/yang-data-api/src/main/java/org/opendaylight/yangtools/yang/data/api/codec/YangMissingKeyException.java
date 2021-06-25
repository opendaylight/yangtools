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
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Netconf.ErrorTag;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangError;

/**
 * Dedicated exception for reporting conditions where {@code error-tag} should be reported as {@code missing-element}.
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
public class YangMissingKeyException extends IllegalArgumentException implements YangError {
    private static final long serialVersionUID = 1L;

    private final @NonNull ImmutableSet<QName> missingKeys;

    public YangMissingKeyException(final Set<QName> missingKeys) {
        super(requireNonNull("List entry is missing keys " + missingKeys));
        // Note: retains iteration order
        this.missingKeys = ImmutableSet.copyOf(missingKeys);
        checkArgument(!this.missingKeys.isEmpty(), "Missing keys cannot be empty");
    }

    @Override
    public final ErrorTag getErrorTag() {
        return ErrorTag.MISSING_ELEMENT;
    }

    public final Set<QName> getMissingKeys() {
        return missingKeys;
    }
}
