/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1890Test extends AbstractYangTest {
    @Test
    void allowNonConfigAugment() {
        assertEffectiveModel("/bugs/YT1890/foo.yang", "/bugs/YT1890/baz.yang");
    }

    @Test
    void disallowConfigAugment() {
        assertInferenceExceptionMessage("/bugs/YT1890/foo.yang", "/bugs/YT1890/bar.yang")
            .startsWith("""
                An augment cannot add node 'bar' because it is mandatory and in module different than target [at """)
            .endsWith("/bar.yang:9:5]");
    }
}
