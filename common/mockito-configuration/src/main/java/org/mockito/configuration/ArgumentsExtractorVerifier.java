/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.mockito.configuration;

import java.util.List;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.InvocationsFinder;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.mockito.verification.VerificationMode;

/**
 * Verifier that extracts arguments from actual invocation. Useful when deeper validation of arguments is needed.
 */
public class ArgumentsExtractorVerifier implements VerificationMode {
    private Object[] arguments = null;

    /**
     * Default constructor.
     */
    public ArgumentsExtractorVerifier() {
        // Nothing else
    }

    @Override
    public void verify(final VerificationData data) {
        List<Invocation> actualInvocations =
            InvocationsFinder.findInvocations(data.getAllInvocations(), data.getTarget());
        if (actualInvocations.size() != 1) {
            throw new MockitoException("This verifier can only be used with 1 invocation, got "
                    + actualInvocations.size());
        }
        Invocation invocation = actualInvocations.get(0);
        arguments = invocation.getArguments();
        invocation.markVerified();
    }

    @Override
    public VerificationMode description(final String description) {
        return VerificationModeFactory.description(this, description);
    }

    public Object[] getArguments() {
        return arguments == null ? null : arguments.clone();
    }
}
