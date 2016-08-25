/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Set;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A ChoiceSchemaNode defines a set of alternatives. It consists of a number of
 * branches defined as ChoiceCaseSchemaNode objects.
 */
@Value.Immutable
public interface ChoiceSchemaNode extends DataSchemaNode, AugmentationTarget {
    /**
     * Returns cases of choice.
     *
     * @return set of ChoiceCaseNode objects defined in this node which
     *         represents set of arguments of the YANG <code>case</code>
     *         substatement of the <code>choice</code> statement
     */
    Set<ChoiceCaseNode> getCases();

    /**
     *
     * Returns the concrete case according to specified Q name.
     *
     * @param name
     *            QName of seeked Choice Case Node
     * @return child case node of this Choice if child with given name is
     *         present, <code>null</code> otherwise
     */
    ChoiceCaseNode getCaseNodeByName(QName name);

    /**
     * Returns the concrete case according to specified name.
     *
     * @param name
     *            name of seeked child as String
     * @return child case node (or local name of case node) of this Choice if
     *         child with given name is present, <code>null</code> otherwise
     */
    ChoiceCaseNode getCaseNodeByName(String name);

    /**
     *
     * Returns name of case which is in the choice specified as default
     *
     * @return string with the name of case which is specified in the argument
     *         of the YANG <code>default</code> substatement of
     *         <code>choice</code> statement.
     */
    String getDefaultCase();

}
