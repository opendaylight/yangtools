/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal766.norev.GlobalFoo;
import org.opendaylight.yang.gen.v1.mdsal766.norev.Mdsal766Data;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.yang.common.QName;

public class Mdsal766Test {
    @Test
    public void featureClassShape() {
        final var instance = GlobalFoo.VALUE;
        assertInstanceOf(YangFeature.class, instance);
        assertEquals("public final", Modifier.toString(GlobalFoo.class.getModifiers()));

        assertEquals(GlobalFoo.class, instance.implementedInterface());
        assertEquals(Mdsal766Data.class, instance.definingModule());
        assertEquals(QName.create("mdsal766", "global-foo"), instance.qname());
    }
}
