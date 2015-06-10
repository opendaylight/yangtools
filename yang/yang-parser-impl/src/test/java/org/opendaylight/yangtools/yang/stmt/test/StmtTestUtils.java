/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class StmtTestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(StmtTestUtils.class);

    private StmtTestUtils() {

    }

    public static void log(Throwable e, String indent) {
        LOG.debug(indent + e.getMessage());

        Throwable[] suppressed = e.getSuppressed();
        for (Throwable throwable : suppressed) {
            log(throwable, indent + "        ");
        }
    }

    public static void addSources(
            CrossSourceStatementReactor.BuildAction reactor,
            YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

    public static void printReferences(Module module, boolean isSubmodule,
            String indent) {
        LOG.debug(indent + (isSubmodule ? "Submodule " : "Module ")
                + module.getName());
        Set<Module> submodules = module.getSubmodules();
        for (Module submodule : submodules) {
            printReferences(submodule, true, indent + "      ");
            printChilds(submodule.getChildNodes(), indent + "            ");
        }
    }

    public static void printChilds(Collection<DataSchemaNode> childNodes,
            String indent) {

        for (DataSchemaNode child : childNodes) {
            LOG.debug(indent + "Child "
                    + child.getQName().getLocalName());
            if (child instanceof DataNodeContainer) {
                printChilds(((DataNodeContainer) child).getChildNodes(), indent
                        + "      ");
            }
        }
    }
}
