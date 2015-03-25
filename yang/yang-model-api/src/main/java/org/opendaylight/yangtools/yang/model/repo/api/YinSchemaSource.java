/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import org.w3c.dom.Document;

/**
 * Yin schema source representation. Exposes an RFC6020 YIN XML representation
 * as an W3C {@link Document}.
 */
@Beta
public interface YinSchemaSource extends SchemaSourceRepresentation {
    /**
     * {@inheritDoc}
     */
    @Override
    SourceIdentifier getIdentifier();

    /**
     * {@inheritDoc}
     */
    @Override
    Class<? extends YinSchemaSource> getType();

    /**
     * Return schema source as a Yin-compliant Document.
     *
     * @return W3C DOM document.
     */
    Document getYinDocument();
}
