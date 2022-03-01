/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

/**
 * Object model representation of an {@code augment} definition. The {@code augment} statement allows a module or
 * submodule to add to the schema tree defined in an external module, or the current module and its submodules, and to
 * add to the nodes from a grouping in a {@code uses} statement.
 *
 * <p>
 * Implementations of this interface present two distinct views on {@link #getChildNodes()} and related methods:
 * <ol>
 *   <li>a primary view, as exposed from {@link AugmentationTarget#getAvailableAugmentations()}, shows children at the
 *       declaration site, e.g. as they are defined in YANG/YIN source. They do not reflect effects of other
 *       {@code augment} statements their instantiation at the target site (as identified by {@link #getTargetPath()}.
 *       Users can request view view via {@link #declaredView()}, and a</li>
 *   <li>a secondary view, exposed via {@link #targetView()}, which shows children at the target site, e.g. reflecting
 *       effects of any {@code augment}, {@code deviate} and similar statements affecting their instantiation at
 *       the particular {@link AugmentationTarget} instance.<li>
 * </ol>
 * Both of these views reflect effective statement hierarchy with respect to implicit {@code case} statements, e.g.
 * if this node's target is a {@link ChoiceSchemaNode}, declared substatements will be wrapped with case statements
 * as appropriate.
 *
 * <p>
 * An example demonstrating the difference between these views is this:
 * <pre>
 *   <code>
 *     container foo;
 *
 *     augment /foo {
 *       container bar;
 *     }
 *
 *     augment /foo/bar {
 *       container baz;
 *     }
 *   </code>
 * </pre>
 * The {@link AugmentationSchemaNode} returned for {@code augment /foo} contains bare {@code container bar}, e.g. it
 * does not show {@code augment /foo/bar} as an available augmentation -- this is only visible in {@code foo}'s schema
 * nodes.
 */
public interface AugmentationSchemaNode extends DataNodeContainer, NotificationNodeContainer, ActionNodeContainer,
        WhenConditionAware, WithStatus, EffectiveStatementEquivalent<AugmentEffectiveStatement> {
    /**
     * Returns augmentation target path.
     *
     * @return SchemaNodeIdentifier that identifies a node in the schema tree. This node is called the augment's target
     *         node. The target node MUST be either a container, list, choice, case, input, output, or anotification
     *         node. It is augmented with the nodes defined as child nodes of this AugmentationSchema.
     */
    default SchemaNodeIdentifier getTargetPath() {
        return asEffectiveStatement().argument();
    }

    /**
     * Returns Augmentation Definition from which this augmentation is derived if augmentation was added transitively
     * via augmented uses.
     *
     * @return Augmentation Definition from which this augmentation is derived if augmentation was added transitively
     *         via augmented uses.
     * @deprecated This method has only a single user, who should be able to do without it.
     */
    @Deprecated(since = "7.0.9", forRemoval = true)
    Optional<AugmentationSchemaNode> getOriginalDefinition();

    /**
     * Return the {@code declared view} of this object's children.
     *
     * @return Declared view of this node
     */
    @NonNull AugmentationSchemaNode declaredView();

    /**
     * Return the {@code target view} of this object's children. This view differs from {@link #declaredView()} in that
     * child nodes contain {@link AugmentationSchemaNode}s introduced at {@link #getTargetPath()}.
     *
     * @return Target's view of this node
     */
    @NonNull AugmentationSchemaNode targetView();
}
