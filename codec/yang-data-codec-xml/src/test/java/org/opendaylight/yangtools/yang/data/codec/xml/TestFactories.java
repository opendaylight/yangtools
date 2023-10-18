/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.util.stream.Stream;
import javax.xml.stream.XMLOutputFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

final class TestFactories implements ArgumentsProvider {
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

    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
        return Stream.of(
            Arguments.of("default", DEFAULT_OUTPUT_FACTORY),
            Arguments.of("repairing", REPAIRING_OUTPUT_FACTORY));
    }
}
