/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.export;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SchemaContextEmitterTest extends AbstractYinExportTest {

    @Test
    public void testSchemaContextEmitter() throws IOException, XMLStreamException, SAXException {
        exportYinModules("/schema-context-emitter-test", "/schema-context-emitter-test");
    }
}
