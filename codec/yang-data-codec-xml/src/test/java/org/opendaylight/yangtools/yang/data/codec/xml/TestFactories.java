/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;

final class TestFactories {
    /**
     * Non-repairing XMLOutputFactory.
     */
    static final XMLOutputFactory DEFAULT_OUTPUT_FACTORY = XMLOutputFactory.newFactory();

    /**
     * Repairing XMLOuputFactory.
     */
    static final XMLOutputFactory REPAIRING_OUTPUT_FACTORY;

    static {
        final var f = XMLOutputFactory.newFactory();
        f.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        REPAIRING_OUTPUT_FACTORY = f;
    }

    private TestFactories() {
        // Hidden on purpose
    }

    static Collection<Object[]> junitParameters() {
        return List.of(new Object[][] {
            { "default", DEFAULT_OUTPUT_FACTORY },
            { "repairing", REPAIRING_OUTPUT_FACTORY },
        });
    }
}
