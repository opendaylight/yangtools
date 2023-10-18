/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;

class QNameFactoryTest {
    @Test
    void testBasic() {
        var expected = TestModel.AUG_NAME_QNAME;
        var created = lookup(expected);
        assertNotSame(expected, created);
        assertEquals(expected, created);

        var cached = lookup(expected);
        assertSame(created, cached);
    }

    private static QName lookup(final QName qname) {
        return QNameFactory.create(qname.getLocalName(), qname.getNamespace().toString(),
            qname.getRevision().map(Revision::toString).orElse(null));
    }
}
