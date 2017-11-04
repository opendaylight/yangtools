/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;

/**
 * Basic entry point into a YANG parser implementation. Yang
 *
 * @author Robert Varga
 */
@Beta
public interface YangParserFactory {

    Collection<Class<? extends SchemaSourceRepresentation>> supportedModelRepresentations();

    default YangParser createParser() {
        return createParser(StatementParserMode.DEFAULT_MODE);
    }

    YangParser createParser(StatementParserMode parserMode);

}
