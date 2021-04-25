/*
 * Copyright (c) 2015, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.opendaylight.yangtools.rfc6643.model.api.AliasEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;

public class IetfYangSmiv2ExtensionsMappingTest {
    @Test
    public void testGetEffectiveRepresentationClass() {
        IetfYangSmiv2ExtensionsMapping extensionMapping = IetfYangSmiv2ExtensionsMapping.ALIAS;
        assertEquals(extensionMapping.getEffectiveRepresentationClass(), AliasEffectiveStatement.class);
    }

    @Test
    public void testIsArgumentYinElement() {
        IetfYangSmiv2ExtensionsMapping extensionMapping = IetfYangSmiv2ExtensionsMapping.ALIAS;
        assertFalse(extensionMapping.getArgumentDefinition().get().isYinElement());
    }
}
