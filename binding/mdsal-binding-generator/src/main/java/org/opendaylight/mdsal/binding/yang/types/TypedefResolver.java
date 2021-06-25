/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

final class TypedefResolver {
    private TypedefResolver() {
        throw new UnsupportedOperationException();
    }

    static List<TypeDefinition<?>> getAllTypedefs(final Module module) {
        final List<TypeDefinition<?>> ret = new ArrayList<>();

        fillRecursively(ret, module);

        for (NotificationDefinition notificationDefinition : module.getNotifications()) {
            fillRecursively(ret, notificationDefinition);
        }

        for (RpcDefinition rpcDefinition : module.getRpcs()) {
            ret.addAll(rpcDefinition.getTypeDefinitions());
            fillRecursively(ret, rpcDefinition.getInput());
            fillRecursively(ret, rpcDefinition.getOutput());
        }

        return ret;
    }

    private static void fillRecursively(final List<TypeDefinition<?>> list, final DataNodeContainer container) {
        for (DataSchemaNode childNode : container.getChildNodes()) {
            if (!childNode.isAugmenting()) {
                if (childNode instanceof ContainerSchemaNode) {
                    fillRecursively(list, (ContainerSchemaNode) childNode);
                } else if (childNode instanceof ListSchemaNode) {
                    fillRecursively(list, (ListSchemaNode) childNode);
                } else if (childNode instanceof ChoiceSchemaNode) {
                    for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) childNode).getCases()) {
                        fillRecursively(list, caseNode);
                    }
                }
            }
        }

        list.addAll(container.getTypeDefinitions());

        for (GroupingDefinition grouping : container.getGroupings()) {
            fillRecursively(list, grouping);
        }
    }
}
