/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface YangModuleInfo {

    /**
     * Returns YANG module name.
     *
     * @return YANG module name.
     */
    String getName();

    /**
     * Returns revision of YANG module.
     *
     * @return YANG module revision.
     */
    String getRevision();

    /**
     * Returns XML namespace associated to the YANG module.
     *
     * @return XML namespace associated to the YANG module.
     */
    String getNamespace();

    InputStream getModuleSourceStream() throws IOException;

    Set<YangModuleInfo> getImportedModules();

}
