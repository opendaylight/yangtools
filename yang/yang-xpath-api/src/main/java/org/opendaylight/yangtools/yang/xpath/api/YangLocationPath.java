/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.xpath;

import java.util.List;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

@Value.Immutable
public interface YangLocationPath extends YangExpr {
    interface Step extends YangPredicateAware {

        YangXPathAxis getAxis();
    }

    interface NameStep extends Step {

    }

    // match any name
    @Value.Immutable
    interface AnyNameStep extends NameStep {

    }

    // match a particular namespace
    @Value.Immutable
    interface NamespaceStep extends NameStep {

        QNameModule getNamespace();
    }

    @Value.Immutable
    interface QNameStep extends NameStep {

        QName getQName();
    }

    @Value.Immutable(intern = true)
    interface NodeTypeStep extends Step {

        YangXPathNodeType getNodeType();
    }

    @Value.Immutable
    interface ProcessingInstructionStep extends NameStep {

        String getName();
    }

    @Value.Default
    default boolean isAbsolute() {
        return false;
    }

    List<Step> getSteps();

    /**
     * The conceptual {@code root} {@link YangLocationPath}. This path is an absolute path and has no steps.
     *
     * @return Empty absolute {@link YangLocationPath}
     */
    static YangLocationPath root() {
        return PrivateConstants.ROOT_LOCATION;
    }

    /**
     * The conceptual {@code same} {@link YangLocationPath}. This path is a relative path and has no steps.
     *
     * @return Empty relative {@link YangLocationPath}
     */
    static YangLocationPath same() {
        return PrivateConstants.SAME_LOCATION;
    }
}
