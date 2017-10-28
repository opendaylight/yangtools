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
public interface ChoiceSchemaNode extends DataSchemaNode, AugmentationTarget {
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
     * @param name
     *            QName of sought Choice Case Node
     * @return child case node of this Choice if child with given name is present, empty otherwise.
     * @throws NullPointerException if qname is null
     */
    default Optional<ChoiceCaseNode> findCase(final QName qname) {
        return Optional.ofNullable(getCases().get(requireNonNull(qname)));
    }

    /**
     * Returns the concrete case according to specified name.
     *
     * @param name
     *            name of seeked child as String
     * @return child case node (or local name of case node) of this Choice if
     *         child with given name is present, <code>null</code> otherwise
     */
    @Beta
    default List<ChoiceCaseNode> findCaseNodes(final String localname) {
        return getCases().values().stream().filter(node -> localname.equals(node.getQName().getLocalName()))
                .collect(ImmutableList.toImmutableList());
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
    // FIXME: this should return Optional<ChoiceCaseNode>
    String getDefaultCase();
}
