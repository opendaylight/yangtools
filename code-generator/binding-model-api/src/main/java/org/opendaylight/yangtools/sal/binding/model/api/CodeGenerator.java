/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api;

import java.io.IOException;

/**
 *
 *
 */
public interface CodeGenerator {

    /**
     * @param type Input type to be processed
     * @return generated code
     * @throws IOException
     */
    String generate(Type type);

    /**
     * @param type Input type to be processed
     * @return true if type is acceptable for processing.
     */
    boolean isAcceptable(Type type);

    /**
     * @param type Input type to be processed
     * @return name of generated unit
     */
    String getUnitName(Type type);

}
