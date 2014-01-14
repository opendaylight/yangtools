/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class NamedFileInputStream extends FileInputStream {
    private final String fileDestination;

    public NamedFileInputStream(File file, String fileDestination) throws FileNotFoundException {
        super(file);
        this.fileDestination = fileDestination;
    }

    public String getFileDestination() {
        return fileDestination;
    }

}
