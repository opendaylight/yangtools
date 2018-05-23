/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * A choice of child nodes within a parent container. This marker interface allows binding interfaces generated for
 * {@code choice} statements to their defining container, without implying {@link DataObject} and {@link ChildOf}
 * relationship.
 *
 * <p>
 * This marker interface is also inherited by interfaces generated for {@code case} statements, similarly marking those
 * interfaces as usable within scope when the statement which defined the {@code choice} statement. This allows us to
 * bind {@code grouping}s to their {@code uses} references within an (implicit or explicit) {@code case}, effectively
 * forming an addressing path from {@link DataRoot} or a {@link DataContainer} like this:
 *
 * <pre>
 * interface Grouping extends DataObject
 * interface GroupingChild extends ChildOf&lt;Grouping&gt;
 *
 * interface Parent extends DataContainer
 * interface Choice implements Choice&lt;Parent&gt;
 * interface Case extends Choice, Grouping
 *
 * Parent -&gt; choiceOf -&gt; Case -&gt; childOf-&gt; GroupingChild
 * </pre>
 *
 * @param <P> Parent container
 * @author Robert Varga
 */
public interface ChoiceIn<P> extends DataContainer {

}
