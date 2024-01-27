/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Abstract base class for tests of the model context available in {@code src/test/resources/model}.
 */
public abstract class AbstractModelTest extends AbstractYangTest {
    private static final QNameModule FOO_NS = QNameModule.of("urn:opendaylight.foo", "2013-02-27");
    private static final QNameModule BAR_NS = QNameModule.of("urn:opendaylight.bar", "2013-07-03");
    private static final QNameModule BAZ_NS = QNameModule.of("urn:opendaylight.baz", "2013-02-27");

    static EffectiveModelContext CTX;
    static Module FOO;
    static Module BAR;
    static Module BAZ;

    @BeforeAll
    static void beforeClass() throws Exception {
        CTX = assertEffectiveModelDir("/model");
        assertEquals(3, CTX.getModules().size());

        FOO = CTX.findModules("foo").iterator().next();
        BAR = CTX.findModules("bar").iterator().next();
        BAZ = CTX.findModules("baz").iterator().next();
    }

    static final @NonNull QName fooQName(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    static final @NonNull QName barQName(final String localName) {
        return QName.create(BAR_NS, localName);
    }

    static final @NonNull QName bazQName(final String localName) {
        return QName.create(BAZ_NS, localName);
    }
}
