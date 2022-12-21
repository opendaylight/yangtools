/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class YT1448Test extends AbstractYangTest {
    @Test
    void deviationFromSubmodule() {
        assertEffectiveModelDir("/bugs/YT1448/valid");
    }

    @Test
    void deviationFromSubmoduleTargetedOwnModule() {
        assertInferenceExceptionDir("/bugs/YT1448/invalid",
            startsWith("Deviation must not target the same module as the one it is defined in"));
    }
}
