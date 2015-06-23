/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
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

public class CaseShorthandImpl implements ChoiceCaseNode, DerivableSchemaNode {

    private final DataSchemaNode caseShorthandNode;
    private final QName qName;
    private final SchemaPath path;

    private final String description;
    private final String reference;
    private final Status status;

    private final boolean augmenting;
    private final boolean addedByUses;
    ConstraintDefinition constraints;
    private ChoiceCaseNode original;

    ImmutableList<UnknownSchemaNode> unknownNodes;

    public CaseShorthandImpl(DataSchemaNode caseShorthandNode) {
        this.caseShorthandNode = caseShorthandNode;
        this.qName = caseShorthandNode.getQName();

        SchemaPath caseShorthandNodePath = caseShorthandNode.getPath();
        Iterable<QName> pathFromRoot = caseShorthandNodePath.getPathFromRoot();
        this.path = SchemaPath
                .create(Iterables.limit(pathFromRoot,
                        Iterables.size(pathFromRoot) - 1),
                        caseShorthandNodePath.isAbsolute());

        this.description = caseShorthandNode.getDescription();
        this.reference = caseShorthandNode.getReference();
        this.status = caseShorthandNode.getStatus();

        this.augmenting = caseShorthandNode.isAugmenting();
        this.addedByUses = caseShorthandNode.isAddedByUses();
        this.constraints = caseShorthandNode.getConstraints();
        this.unknownNodes = ImmutableList.copyOf(caseShorthandNode.getUnknownSchemaNodes());
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public boolean isConfiguration() {
        return false;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return constraints;
    }

    @Override
    public QName getQName() {
        return qName;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
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

    @Override
    public Optional<? extends SchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qName == null) ? 0 : qName.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CaseShorthandImpl other = (CaseShorthandImpl) obj;
        if (qName == null) {
            if (other.qName != null) {
                return false;
            }
        } else if (!qName.equals(other.qName)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                CaseShorthandImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=");
        sb.append(qName);
        sb.append("]");
        return sb.toString();
    }

}