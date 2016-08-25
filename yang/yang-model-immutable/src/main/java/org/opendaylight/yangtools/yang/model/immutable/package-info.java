/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
@Value.Include(value = {
        org.opendaylight.yangtools.yang.model.api.ActionDefinition.class,
        org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode.class,
        org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode.class,
        org.opendaylight.yangtools.yang.model.api.ExtensionDefinition.class,
        org.opendaylight.yangtools.yang.model.api.FeatureDefinition.class,
        org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode.class,
        org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode.class,
        org.opendaylight.yangtools.yang.model.api.LeafSchemaNode.class,
        org.opendaylight.yangtools.yang.model.api.ModuleImport.class,
        org.opendaylight.yangtools.yang.model.api.RpcDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit.class,
        org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair.class,
        org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.LengthConstraint.class,
        org.opendaylight.yangtools.yang.model.api.type.PatternConstraint.class,
        org.opendaylight.yangtools.yang.model.api.type.RangeConstraint.class,
        org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition.class,
        org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition.class,
})
package org.opendaylight.yangtools.yang.model.immutable;

import org.immutables.value.Value;
