/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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

public class Bug4504Test extends AbstractYinExportTest {
    @Test
    public void test() throws IOException, SAXException, XMLStreamException {
        exportYinModules("/bugs/bug4504", null);
    }
}
