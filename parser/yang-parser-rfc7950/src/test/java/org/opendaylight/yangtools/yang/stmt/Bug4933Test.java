/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Module;

class Bug4933Test extends AbstractYangTest {
    @Test
    void test() {
        final var context = assertEffectiveModelDir("/bugs/bug4933/correct");

        final Module foo = context.findModules("foo").iterator().next();
        Collection<? extends Deviation> deviations = foo.getDeviations();
        assertEquals(4, deviations.size());
    }

    @Test
    void incorrectKeywordTest() {
        assertSourceExceptionDir("/bugs/bug4933/incorrect",
            startsWith("String 'not_supported' is not valid deviate argument"));
    }
}
