/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.model.provider.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.file.api.AbstractYangModule;

@NonNullByDefault
public final class TestYangModule extends AbstractYangModule {
    private static final QName IDENTIFIER = QName.create("urn:opendaylight:test", "test", Revision.of("2019-12-26"))
            .intern();

    @Override
    public QName getIdentifier() {
        return IDENTIFIER;
    }
}
