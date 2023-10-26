/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class AbstractSchemaInferenceStackTest {
    static EffectiveModelContext CONTEXT;

    @BeforeAll
    static final void initContext() {
        CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/schema-context-util");
    }
}
