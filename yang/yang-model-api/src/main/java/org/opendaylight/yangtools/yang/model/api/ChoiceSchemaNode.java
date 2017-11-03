/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A ChoiceSchemaNode defines a set of alternatives. It consists of a number of branches defined as
 * ChoiceCaseSchemaNode objects.
 */
public interface ChoiceSchemaNode extends DataSchemaNode, AugmentationTarget, MandatoryAware {
    /**
     * Returns cases of choice, keyed by their {@link SchemaNode#getQName()}. Returned map does not contain null keys
     * nor values.
     *
     * @return set of ChoiceCaseNode objects defined in this node which represents set of arguments of the YANG
     *         <code>case</code> substatement of the <code>choice</code> statement.
     */
    SortedMap<QName, ChoiceCaseNode> getCases();

    /**
     * Returns the concrete case according to specified Q name.
     *
     * @param qname
     *            QName of sought Choice Case Node
     * @return child case node of this Choice if child with given name is present, empty otherwise.
     * @throws NullPointerException if qname is null
     */
    default Optional<ChoiceCaseNode> findCase(final QName qname) {
        return Optional.ofNullable(getCases().get(requireNonNull(qname)));
    }

    /**
     * Returns the concrete cases according to specified name, disregarding their namespace.
     *
     * @param localname
     *            local name of sought child as String
     * @return child case nodes matching specified local name, empty list if no match is found.
     * @throws NullPointerException if localname is null
     */
    @Beta
    default List<ChoiceCaseNode> findCaseNodes(final String localname) {
        return getCases().values().stream().filter(node -> localname.equals(node.getQName().getLocalName()))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Find a specific data schema child, if present. This method searches among its {@link ChoiceCaseNode}s,
     * potentially recursing to nested choices.
     *
     * @param qname
     *            QName of sought data schema node
     * @return Matching node, or empty if no match is found
     * @throws NullPointerException if qname is null
     */
    @Beta
    default Optional<DataSchemaNode> findDataSchemaChild(final QName qname) {
        requireNonNull(qname);
        for (ChoiceCaseNode caseNode : getCases().values()) {
            final Optional<DataSchemaNode> child = caseNode.findDataChildByName(qname);
            if (child.isPresent()) {
                return child;
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the concrete case according to specified QName.
     *
     * @param qname
     *            QName of sought Choice Case Node
     * @return child case node of this Choice if child with given name is present, <code>null</code> otherwise.
     *
     * @deprecated Use either {@code getCases().get(name)} or #findCase(QName)
     */
    @Deprecated
    default ChoiceCaseNode getCaseNodeByName(final QName qname) {
        return getCases().get(qname);
    }

    /**
     * Returns name of case which is in the choice specified as default.
     *
     * @return string with the name of case which is specified in the argument of the YANG <code>default</code>
     *         substatement of <code>choice</code> statement.
     */
    Optional<ChoiceCaseNode> getDefaultCase();
}
