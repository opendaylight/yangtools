/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.Beta;
import javax.xml.transform.TransformerException;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinXmlSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.YinDOMStatementStreamSource;

/**
 * A {@link StatementStreamSource} based on a {@link YinXmlSource}. Internal implementation works on top
 * of {@link YinDomSource} and its DOM document.
 */
@Beta
public final class YinStatementStreamSource {
    private YinStatementStreamSource() {
        // Hidden on purpose
    }

    public static StatementStreamSource create(final YinXmlSource source) throws TransformerException {
        return create(YinDomSource.transform(source));
    }

    @Deprecated(since = "15.0.0", forRemoval = true)
    public static StatementStreamSource create(final YinDomSource source) {
        return new YinDOMStatementStreamSource(source);
    }
}
