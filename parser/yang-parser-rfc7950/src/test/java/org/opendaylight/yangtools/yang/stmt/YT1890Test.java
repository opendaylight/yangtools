/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class YT1890Test extends AbstractYangTest {
    @Test
    void allowNonConfigAugment() {
        // plain augments to contexts which are either ignoring config or are config=false
        assertEffectiveModel("/bugs/YT1890/foo.yang", "/bugs/YT1890/baz.yang");
    }

    @Test
    void disallowConfigAugment() {
        // plain augment to an implied config=true context
        assertThat(assertInferenceException("/bugs/YT1890/foo.yang", "/bugs/YT1890/bar.yang").getMessage())
            .startsWith("""
                An augment cannot add node 'bar' because it is mandatory and in module different than target [at """)
            .endsWith("/bar.yang:9:5]");
    }

    @Test
    void allowConfigAugmentDeviated() {
        // fun with deviations: plain augment to a nominally config=true context, which is deviated to be config=false
        assertEffectiveModel("/bugs/YT1890/foo.yang", "/bugs/YT1890/baz.yang", "/bugs/YT1890/xyzzy.yang");
    }

    @Test
    @Disabled("non-config support not complete")
    void allowConfigAugmentNonConfig() {
        // plain augment to an implied config=true context introducing a mandatory config=false statement
        assertEffectiveModel("/bugs/YT1890/foo.yang", "/bugs/YT1890/qux.yang");
    }
}
