/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.Arrays;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class CaseShorthandImpl implements ChoiceCaseNode {

    private final DataSchemaNode caseShorthandNode;
    private final QName qName;
    private final SchemaPath path;

    public CaseShorthandImpl(DataSchemaNode caseShorthandNode) {
        this.caseShorthandNode = caseShorthandNode;
        this.qName = caseShorthandNode.getQName();

        SchemaPath caseShorthandNodePath = caseShorthandNode.getPath();
        Iterable<QName> pathFromRoot = caseShorthandNodePath.getPathFromRoot();
        this.path = SchemaPath
                .create(Iterables.limit(pathFromRoot,
                        Iterables.size(pathFromRoot) - 1),
                        caseShorthandNodePath.isAbsolute());
    }

    @Override
    public boolean isAugmenting() {
        return caseShorthandNode.isAugmenting();
    }

    @Override
    public boolean isAddedByUses() {
        return caseShorthandNode.isAddedByUses();
    }

    @Override
    public boolean isConfiguration() {
        return caseShorthandNode.isConfiguration();
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return caseShorthandNode.getConstraints();
    }

    @Override
    public QName getQName() {
        return caseShorthandNode.getQName();
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return caseShorthandNode.getUnknownSchemaNodes();
    }

    @Override
    public String getDescription() {
        return caseShorthandNode.getDescription();
    }

    @Override
    public String getReference() {
        return caseShorthandNode.getReference();
    }

    @Override
    public Status getStatus() {
        return caseShorthandNode.getStatus();
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return Collections.emptySet();
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return Arrays.asList(caseShorthandNode);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return Collections.emptySet();
    }

    @Override
    public DataSchemaNode getDataChildByName(QName name) {
        if (qName.equals(name)) {
            return caseShorthandNode;
        } else {
            return null;
        }
    }

    @Override
    public DataSchemaNode getDataChildByName(String name) {
        if (qName.getLocalName().equals(name)) {
            return caseShorthandNode;
        } else {
            return null;
        }
    }

    @Override
    public Set<UsesNode> getUses() {
        return Collections.emptySet();
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return Collections.emptySet();
    }
}