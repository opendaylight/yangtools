/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mockito;

import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.InvocationsFinder;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

/**
 * Verifier that extracts arguments from actual invocation. Useful when deeper validation of arguments is needed.
 */
public final class ArgumentsExtractorVerifier implements VerificationMode {
    private Object[] arguments = null;

    @Override
    public void verify(final VerificationData data) {
        final var actualInvocations = InvocationsFinder.findInvocations(data.getAllInvocations(), data.getTarget());
        final var size = actualInvocations.size();
        switch (size) {
            case 1 -> {
                final var invocation = actualInvocations.get(0);
                arguments = invocation.getArguments();
                invocation.markVerified();
            }
            default -> throw new MockitoException("This verifier can only be used with 1 invocation, got " + size);
        }
    }

    @Override
    public VerificationMode description(final String description) {
        return VerificationModeFactory.description(this, description);
    }

    /**
     * Return verified invocation arguments, or {@code null} if not verified.
     *
     * @return verified invocation arguments, or {@code null} if not verified
     */
    public Object[] getArguments() {
        return arguments == null ? null : arguments.clone();
    }
}
