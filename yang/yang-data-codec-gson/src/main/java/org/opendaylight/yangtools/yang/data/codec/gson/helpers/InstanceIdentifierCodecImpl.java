/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.helpers;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.codec.InstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.IdentityValuesDTO.IdentityValue;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.IdentityValuesDTO.Predicate;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InstanceIdentifierCodecImpl extends AbstractCodecImpl implements InstanceIdentifierCodec<IdentityValuesDTO> {
    private static final Logger LOG = LoggerFactory.getLogger(InstanceIdentifierCodecImpl.class);

    InstanceIdentifierCodecImpl(final SchemaContextUtils schema) {
        super(schema);
    }

    @Override
    public IdentityValuesDTO serialize(final YangInstanceIdentifier data) {
        IdentityValuesDTO identityValuesDTO = new IdentityValuesDTO();
        for (PathArgument pathArgument : data.getPathArguments()) {
            IdentityValue identityValue = qNameToIdentityValue(pathArgument.getNodeType());
            if (pathArgument instanceof NodeIdentifierWithPredicates && identityValue != null) {
                List<Predicate> predicates = keyValuesToPredicateList(((NodeIdentifierWithPredicates) pathArgument)
                        .getKeyValues());
                identityValue.setPredicates(predicates);
            } else if (pathArgument instanceof NodeWithValue && identityValue != null) {
                List<Predicate> predicates = new ArrayList<>();
                String value = String.valueOf(((NodeWithValue) pathArgument).getValue());
                predicates.add(new Predicate(null, value));
                identityValue.setPredicates(predicates);
            }
            identityValuesDTO.add(identityValue);
        }
        return identityValuesDTO;
    }

    @Override
    public YangInstanceIdentifier deserialize(final IdentityValuesDTO data) {
        List<PathArgument> result = new ArrayList<PathArgument>();
        IdentityValue valueWithNamespace = data.getValuesWithNamespaces().get(0);
        Module module = getModuleByNamespace(valueWithNamespace.getNamespace());
        if (module == null) {
            LOG.info("Module by namespace '{}' of first node in instance-identiefier was not found.",
                    valueWithNamespace.getNamespace());
            LOG.info("Instance-identifier will be translated as NULL for data - {}",
                    String.valueOf(valueWithNamespace.getValue()));
            return null;
        }

        DataNodeContainer parentContainer = module;
        List<IdentityValue> identities = data.getValuesWithNamespaces();
        for (int i = 0; i < identities.size(); i++) {
            IdentityValue identityValue = identities.get(i);
            URI validNamespace = resolveValidNamespace(identityValue.getNamespace());
            DataSchemaNode node = getSchema().findInstanceDataChildByNameAndNamespace(
                    parentContainer, identityValue.getValue(), validNamespace);
            if (node == null) {
                LOG.info("'{}' node was not found in {}", identityValue, parentContainer.getChildNodes());
                LOG.info("Instance-identifier will be translated as NULL for data - {}",
                        String.valueOf(identityValue.getValue()));
                return null;
            }
            QName qName = node.getQName();
            PathArgument pathArgument = null;
            if (identityValue.getPredicates().isEmpty()) {
                pathArgument = new NodeIdentifier(qName);
            } else {
                if (node instanceof LeafListSchemaNode) { // predicate is value of leaf-list entry
                    Predicate leafListPredicate = identityValue.getPredicates().get(0);
                    if (!leafListPredicate.isLeafList()) {
                        LOG.info("Predicate's data is not type of leaf-list. It should be in format \".='value'\"");
                        LOG.info("Instance-identifier will be translated as NULL for data - {}",
                                String.valueOf(identityValue.getValue()));
                        return null;
                    }
                    pathArgument = new NodeWithValue(qName, leafListPredicate.getValue());
                } else if (node instanceof ListSchemaNode) { // predicates are keys of list
                    DataNodeContainer listNode = (DataNodeContainer) node;
                    Map<QName, Object> predicatesMap = new HashMap<>();
                    for (Predicate predicate : identityValue.getPredicates()) {
                        validNamespace = resolveValidNamespace(predicate.getName().getNamespace());
                        DataSchemaNode listKey = getSchema()
                                .findInstanceDataChildByNameAndNamespace(listNode, predicate.getName().getValue(),
                                        validNamespace);
                        predicatesMap.put(listKey.getQName(), predicate.getValue());
                    }
                    pathArgument = new NodeIdentifierWithPredicates(qName, predicatesMap);
                } else {
                    LOG.info("Node {} is not List or Leaf-list.", node);
                    LOG.info("Instance-identifier will be translated as NULL for data - {}",
                            String.valueOf(identityValue.getValue()));
                    return null;
                }
            }
            result.add(pathArgument);
            if (i < identities.size() - 1) { // last element in instance-identifier can be other than
                // DataNodeContainer
                if (node instanceof DataNodeContainer) {
                    parentContainer = (DataNodeContainer) node;
                } else {
                    LOG.info("Node {} isn't instance of DataNodeContainer", node);
                    LOG.info("Instance-identifier will be translated as NULL for data - {}",
                            String.valueOf(identityValue.getValue()));
                    return null;
                }
            }
        }

        return result.isEmpty() ? null : YangInstanceIdentifier.create(result);
    }

    private static List<Predicate> keyValuesToPredicateList(final Map<QName, Object> keyValues) {
        List<Predicate> result = new ArrayList<>();
        for (QName qName : keyValues.keySet()) {
            Object value = keyValues.get(qName);
            result.add(new Predicate(qNameToIdentityValue(qName), String.valueOf(value)));
        }
        return result;
    }

    private static IdentityValue qNameToIdentityValue(final QName qName) {
        if (qName != null) {
            // FIXME: the prefix here is completely arbitrary
            return new IdentityValue(qName.getNamespace().toString(), qName.getLocalName(), qName.getPrefix());
        }
        return null;
    }
}