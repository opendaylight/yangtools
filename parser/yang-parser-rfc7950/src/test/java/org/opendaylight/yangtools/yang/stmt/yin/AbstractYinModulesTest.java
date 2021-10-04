/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public abstract class AbstractYinModulesTest extends AbstractYangTest {
    static final EffectiveModelContext CONTEXT;

    static {
        try {
            CONTEXT = TestUtils.loadYinModules(
                AbstractYinModulesTest.class.getResource("/semantic-statement-parser/yin/modules").toURI());
        } catch (ReactorException | SAXException | IOException | URISyntaxException e) {
            throw new ExceptionInInitializerError(e);
        }
        assertEquals(9, CONTEXT.getModules().size());
    }
}
