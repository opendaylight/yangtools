/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.Test;

public class NameCollisionWithinCaseTest extends AbstractYangTest {
    @Test
    public void testChildNameCollisionOfAugmentCase() {
        assertSourceException(startsWith("Cannot add data tree child with name (foo?revision=2018-02-11)bar, "
                + "a conflicting child already exists [at "), "/bugs/name-collision-within-case/foo.yang");
    }

    @Test
    public void testChildNameCollisionOfAugmentChoice() {
        assertSourceException(startsWith("Cannot add data tree child with name (bar?revision=2018-02-11)bar, "
                + "a conflicting child already exists [at "), "/bugs/name-collision-within-case/bar.yang");
    }

    @Test
    public void testChildNameCollisionNormal() {
        assertSourceException(startsWith("Error in module 'baz': cannot add '(baz?revision=2018-02-28)bar'."
                + " Node name collision: "), "/bugs/name-collision-within-case/baz.yang");
    }
}
