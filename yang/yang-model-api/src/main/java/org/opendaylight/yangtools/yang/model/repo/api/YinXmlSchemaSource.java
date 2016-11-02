/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.xml.transform.Source;

/**
 * YIN text schema source representation. Exposes an RFC6020 XML representation as an XML {@link Source}.
 */
@Beta
public interface YinXmlSchemaSource extends YinSchemaSourceRepresentation {
    @Nonnull
    @Override
    Class<? extends YinXmlSchemaSource> getType();

    /**
     * Return an XML {@link Source} of the YIN document.
     *
     * @return An XML {@link Source} instance.
     */
    @Nonnull Source getSource();
}
