/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.export;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

interface StatementTextWriter {


    void startStatement(StatementDefinition statement);

    void writeArgument(RevisionAwareXPath xpath);

    void writeArgument(QName name);

    void writeArgument(String argStr);

    void writeArgument(SchemaPath targetPath);

    void endStatement();

}
