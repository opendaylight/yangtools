/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.mockito.configuration;

import org.mockito.stubbing.Answer;
import org.opendaylight.mockito.MoreAnswers;

/**
 * Configuration customization for Mockito. Change default answer to  {@link MoreAnswers#THROWS_UNSTUBBED_METHOD}.
 */
public class MockitoConfiguration extends DefaultMockitoConfiguration {
    @Override
    public Answer<Object> getDefaultAnswer() {
        return MoreAnswers.THROWS_UNSTUBBED_METHOD;
    }
}
