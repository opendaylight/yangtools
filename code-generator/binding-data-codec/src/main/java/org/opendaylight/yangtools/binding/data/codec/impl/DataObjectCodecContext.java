/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map.Entry;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

abstract class DataObjectCodecContext<T extends DataNodeContainer> extends DataContainerCodecContext<T> {

    protected final ImmutableMap<String, LeafNodeCodecContext> leafChild;
    protected final ImmutableMap<Type, Entry<Type, Type>> choiceCaseChildren;
    protected final ImmutableMap<AugmentationIdentifier, Type> augIdentifierToType;

    protected DataObjectCodecContext(final Class<?> cls, final QNameModule namespace, final T nodeSchema,
            final CodecContextFactory loader) {
        super(cls, namespace, nodeSchema, loader);
        this.leafChild = loader.getLeafNodes(cls, nodeSchema);
        this.choiceCaseChildren = factory.getRuntimeContext().getChoiceCaseChildren(schema);
        this.augIdentifierToType = factory.getRuntimeContext().getAvailableAugmentationTypes(nodeSchema);
    }

    @Override
    protected DataContainerCodecContext<?> getIdentifierChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        if (choiceCaseChildren.isEmpty()) {
            return super.getIdentifierChild(arg, builder);
        }
        // Lookup in choiceCase
        Class<? extends DataObject> argument = arg.getType();
        ReferencedTypeImpl ref = new ReferencedTypeImpl(argument.getPackage().getName(), argument.getSimpleName());
        Entry<Type, Type> cazeId = choiceCaseChildren.get(ref);
        if (cazeId == null) {
            return super.getIdentifierChild(arg, builder);
        }
        ClassLoadingStrategy loader = factory.getRuntimeContext().getStrategy();
        try {
            Class<?> choice = loader.loadClass(cazeId.getKey());
            Class<?> caze = loader.loadClass(cazeId.getValue());
            ChoiceNodeCodecContext choiceNode = (ChoiceNodeCodecContext) getStreamChild(choice);
            choiceNode.addYangPathArgument(arg, builder);
            CaseNodeCodecContext cazeNode = (CaseNodeCodecContext) choiceNode.getStreamChild(caze);
            cazeNode.addYangPathArgument(arg, builder);
            return cazeNode.getIdentifierChild(arg, builder);

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Required class not found.", e);
        }

    }

    @Override
    protected NodeCodecContext getYangIdentifierChild(final YangInstanceIdentifier.PathArgument arg) {
        if (arg instanceof YangInstanceIdentifier.AugmentationIdentifier) {
            return getChildByAugmentationIdentifier((YangInstanceIdentifier.AugmentationIdentifier) arg);
        } else {
            QName childQName = arg.getNodeType();
            DataSchemaNode childSchema = schema.getDataChildByName(childQName);
            Preconditions.checkArgument(childSchema != null, "Argument %s is not valid child of %s", arg, schema);
            if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceNode) {
                Class<?> childCls = factory.getRuntimeContext().getClassForSchema(childSchema);
                DataContainerCodecContext<?> childNode = getStreamChild(childCls);
                return childNode;
            } else {
                return getLeafChild(childQName.getLocalName());
            }
        }
    }

    protected NodeCodecContext getChildByAugmentationIdentifier(final YangInstanceIdentifier.AugmentationIdentifier arg) {
        final Type augType = augIdentifierToType.get(arg);
        try {
            Class<?> augClass = factory.getRuntimeContext().getStrategy().loadClass(augType);
            return getStreamChild(augClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load referenced augmentation.", e);
        }
    }

    protected final LeafNodeCodecContext getLeafChild(final String name) {
        final LeafNodeCodecContext value = leafChild.get(name);
        Preconditions.checkArgument(value != null, "Leaf %s is not valid for %s", name, bindingClass);
        return value;
    }

    @Override
    protected DataContainerCodecContext<?> loadChild(final Class<?> childClass) {
        if (Augmentation.class.isAssignableFrom(childClass)) {
            return loadAugmentation(childClass);
        }

            DataSchemaNode origDef = factory.getRuntimeContext().getSchemaDefinition(childClass);
            // Direct instantiation or use in same module in which grouping
            // was defined.
        DataSchemaNode sameName;
        try {
            sameName = schema.getDataChildByName(origDef.getQName());
        } catch (IllegalArgumentException e) {
            sameName = null;
        }
        final DataSchemaNode childSchema;
        if (sameName != null) {
            // Exactly same schema node
            if (origDef.equals(sameName)) {
                childSchema = sameName;
                // We check if instantiated node was added via uses
                // statement
                // and is instatiation of same grouping
            } else if (origDef.equals(SchemaNodeUtils.getRootOriginalIfPossible(sameName))) {
                childSchema = sameName;
            } else {
                // Node has same name, but clearly is different
                childSchema = null;
            }
        } else {
            // We are looking for instantiation via uses in other module
            QName instantiedName = QName.create(namespace, origDef.getQName().getLocalName());
            DataSchemaNode potential = schema.getDataChildByName(instantiedName);
            // We check if it is really instantiated from same
            // definition
            // as class was derived
            if (potential != null && origDef.equals(SchemaNodeUtils.getRootOriginalIfPossible(potential))) {
                childSchema = potential;
            } else {
                childSchema = null;
            }
        }
        Preconditions
                .checkArgument(childSchema != null, "Node %s does not have child named %s", schema, childClass);
        return DataContainerCodecContext.from(childClass, childSchema, factory);
    }

    @SuppressWarnings("rawtypes")
    private AugmentationNode loadAugmentation(final Class childClass) {
        Preconditions.checkArgument(schema instanceof AugmentationTarget);
        @SuppressWarnings("unchecked")
        Entry<AugmentationIdentifier, AugmentationSchema> augSchema = factory.getRuntimeContext()
                .getResolvedAugmentationSchema(schema, childClass);
        QNameModule namespace = Iterables.getFirst(augSchema.getKey().getPossibleChildNames(), null).getModule();
        return new AugmentationNode(childClass, namespace, augSchema.getKey(), augSchema.getValue(), factory);
    }

}