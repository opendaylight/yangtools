/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import javax.xml.transform.Source;
import org.eclipse.jdt.annotation.NonNull;

/**
 * YIN text schema source representation. Exposes an RFC6020 or RFC7950 XML representation as an XML {@link Source}.
 */
public interface YinXmlSchemaSource extends YinSchemaSourceRepresentation {
    @Override
    Class<? extends YinXmlSchemaSource> getType();

    /**
     * Return an XML {@link Source} of the YIN document.
     *
     * @return An XML {@link Source} instance.
     */
    @NonNull Source getSource();
}
