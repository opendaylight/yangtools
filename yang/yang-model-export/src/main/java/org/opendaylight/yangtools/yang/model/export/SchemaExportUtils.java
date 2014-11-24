/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class SchemaExportUtils {


    SchemaExportUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    static void writeToStatementWriter(final Module module, final SchemaContext ctx, final StatementWriter statementWriter) {
        final YangModuleWriter yangSchemaWriter = SchemaToStatementWriterAdaptor.from(statementWriter);
        final Map<QName, StatementDefinition> extensions = ExtensionStatement.mapFrom(ctx.getExtensions());
        new Emitter(yangSchemaWriter,extensions).emitModule(module);
    }


}
