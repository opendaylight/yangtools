/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser.dagger;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc6536.parser.impl.Rfc6536ParserExtension;

class DaggerTestComponentTest {
    @Test
    void extensionsAreAvailable() {
        final var component = DaggerTestComponent.create();
        final var extensions = component.extensions();
        assertThat(extensions).hasSize(1);
        assertThat(extensions).hasAtLeastOneElementOfType(Rfc6536ParserExtension.class);
    }
}
