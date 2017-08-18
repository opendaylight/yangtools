/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

public class LengthConstraintImplTest {

    @Test
    public void testLengthConstraint() {
        LengthConstraint lengthConstraint = new LengthConstraintImpl(3, 5, Optional.of("test description"),
                Optional.of("test reference"));
        LengthConstraint lengthConstraint2 = new LengthConstraintImpl(3, 5, Optional.of("test description"),
                Optional.of("test reference"));
        assertTrue(lengthConstraint.equals(lengthConstraint2));

        assertFalse(lengthConstraint.equals(null));
        assertFalse(lengthConstraint.equals(new Object()));

        lengthConstraint2 = new LengthConstraintImpl(3, 5, Optional.of("another test description"),
                Optional.of("test reference"));
        assertFalse(lengthConstraint.equals(lengthConstraint2));

        lengthConstraint2 = new LengthConstraintImpl(3, 5, Optional.of("test description"),
                Optional.of("another test reference"));
        assertFalse(lengthConstraint.equals(lengthConstraint2));

        lengthConstraint = new LengthConstraintImpl(3, 5, Optional.of("test description"),
                Optional.of("test reference"), "error app-tag", "error message");
        lengthConstraint2 = new LengthConstraintImpl(2, 5, Optional.of("test description"),
                Optional.of("test reference"), "error app-tag", "error message");
        assertFalse(lengthConstraint.equals(lengthConstraint2));

        lengthConstraint2 = new LengthConstraintImpl(3, 6, Optional.of("test description"),
                Optional.of("test reference"), "error app-tag", "error message");
        assertFalse(lengthConstraint.equals(lengthConstraint2));

        lengthConstraint2 = new LengthConstraintImpl(3, 5, Optional.of("test description"),
                Optional.of("test reference"), "another error app-tag", "error message");
        assertFalse(lengthConstraint.equals(lengthConstraint2));

        lengthConstraint2 = new LengthConstraintImpl(3, 5, Optional.of("test description"),
                Optional.of("test reference"), "error app-tag", "another error message");
        assertFalse(lengthConstraint.equals(lengthConstraint2));
    }
}
