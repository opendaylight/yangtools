/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.netconf;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ModelSupportTest {

    @Test
    public void testAnnotation() {
        assertNotNull(ModelSupport.OPERATION_ANNOTATION);
    }
}
