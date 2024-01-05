/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Definition of structures and DOM like API of effected YANG schema.
 *
 * <p>
 * This package is structured into following logical units:
 * <dl>
 * <dt>YANG Meta model</dt>
 * <dd>Meta model of YANG, which defines basic concepts and building blocks of YANG models
 * such as {@link org.opendaylight.yangtools.yang.model.api.meta.ModelStatement}.</dd>
 * <dt>YANG Statement model</dt>
 * <dd>Concrete java model of YANG statements, which defines basic relationship between statements
 * and represents these statements.</dd>
 *
 * <dt>YANG Effective model</dt>
 * <dd>Effective model of processed YANG models, which represents semantic interpretation
 * of YANG models and provides convenience views for interpreting models.
 * </dd>
 * </dl>
 *
 *
 * <h2>YANG Effective model</h2>
 * <h3>Effective model statement mapping</h3>
 *
 * <dl>
 * <dt>anyxml
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode}
 *
 * <dt>argument
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ExtensionDefinition#getArgument()}
 *
 * <dt>augment
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode}
 *
 * <dt>base
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition#getIdentities()}
 *
 * <dt>belongs-to
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement#getBelongsTo()}
 *
 * <dt>bit
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition#getBits()}
 *
 * <dt>case
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.CaseSchemaNode}
 *
 * <dt>choice
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode}
 *
 * <dt>config
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.DataSchemaNode#isConfiguration()}
 *
 * <dt>contact
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Module#getContact()}
 *
 * <dt>container
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode}
 *
 * <dt>default
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.TypeDefinition#getDefaultValue()}
 *
 * <dt>description
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.SchemaNode#getDescription()}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition#getDescription()}
 *
 * <dt>enum
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition#getValues()}
 *
 * <dt>error-app-tag
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition#getErrorAppTag()}
 *
 * <dt>error-message
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition#getErrorMessage()}
 *
 * <dt>extension
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ExtensionDefinition}
 *
 * <dt>deviation
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Deviation}
 *
 * <dt>deviate
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.DeviateKind}
 *
 * <dt>feature
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.FeatureDefinition}
 *
 * <dt>fraction-digits
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition#getFractionDigits()}
 *
 * <dt>grouping
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.GroupingDefinition}
 *
 * <dt>identity
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode}
 *
 * <dt>if-feature
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement}
 *
 * <dt>import
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ModuleImport}
 *
 * <dt>include
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement}
 *
 * <dt>input
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.RpcDefinition#getInput()}
 *
 * <dt>key
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ListSchemaNode#getKeyDefinition()}
 *
 * <dt>leaf
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.LeafSchemaNode}
 *
 * <dt>leaf-list
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode}
 *
 * <dt>length
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.LengthConstraint}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition#getLengthConstraint()}
 *
 * <dt>list
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ListSchemaNode}
 *
 * <dt>mandatory
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.MandatoryAware#isMandatory()}
 *
 * <dt>max-elements
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ElementCountConstraint#getMinElements()}
 *
 * <dt>min-elements
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ElementCountConstraint#getMaxElements()}
 *
 * <dt>module
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Module}
 *
 * <dt>must
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.MustConstraintAware#getMustConstraints()}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.MustDefinition}
 *
 * <dt>namespace
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Module#getNamespace()}
 *
 * <dt>notification
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.NotificationDefinition}
 *
 * <dt>ordered-by
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ListSchemaNode#isUserOrdered()}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode#isUserOrdered()}
 *
 * <dt>organization
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Module#getOrganization()}
 *
 * <dt>output
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.RpcDefinition#getOutput()}
 *
 * <dt>path
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition#getPathStatement()}
 *
 * <dt>pattern
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.PatternConstraint}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition}
 *
 * <dt>position
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit#getPosition()}
 *
 * <dt>prefix
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Module#getPrefix()}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ModuleImport#getPrefix()}
 *
 * <dt>presence
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode#isPresenceContainer()}
 *
 * <dt>range
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.RangeConstraint}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition#getRangeConstraint()}
 *
 * <dt>reference
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.SchemaNode#getReference()}
 *
 * <dt>refine
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement}
 *
 * <dt>require-instance
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition#requireInstance()}
 *
 * <dt>revision
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Module#getRevision()}
 *
 * <dt>revision-date
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ModuleImport#getRevision()}
 *
 * <dt>rpc
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.RpcDefinition}
 *
 * <dt>status
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.SchemaNode#getStatus()}
 *
 * <dt>submodule
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement}
 *
 * <dt>type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.TypeDefinition}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.LeafSchemaNode#getType()}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode#getType()}
 *
 * <dt>typedef
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.TypeDefinition}
 *
 * <dt>unique
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement}
 *
 * <dt>units
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.TypeDefinition#getUnits()}
 *
 * <dt>uses
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.UsesNode}
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer#getUses()}
 *
 * <dt>value
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair#getValue()}
 *
 * <dt>when
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.WhenConditionAware#getWhenCondition()}
 *
 * <dt>yang-version
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Module#getYangVersion()}
 *
 * <dt>yin-element
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ExtensionDefinition#isYinElement()}
 *
 * <dt>add
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.DeviateKind#ADD}
 *
 * <dt>current
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Status#CURRENT}
 *
 * <dt>delete
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.DeviateKind#DELETE}
 *
 * <dt>deprecated
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Status#DEPRECATED}
 *
 * <dt>false
 *   <dd>{@link java.lang.Boolean#FALSE}
 *
 * <dt>max
 *   <dd>Not exposed
 *
 * <dt>min
 *   <dd>Not exposed
 *
 * <dt>not-supported
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.DeviateKind#NOT_SUPPORTED}
 *
 * <dt>obsolete
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.Status#OBSOLETE}
 *
 * <dt>replace
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.DeviateKind#REPLACE}
 *
 * <dt>system
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ListSchemaNode#isUserOrdered()}
 *
 * <dt>true
 *   <dd>{@link java.lang.Boolean#TRUE}
 *
 * <dt>unbounded
 *   <dd>Not exposed
 *
 * <dt>user
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.ListSchemaNode#isUserOrdered()}
 * </dl>
 *
 *
 * <h3>YANG Base Type Mapping</h3>
 *
 *
 * <dl>
 * <dt>Int8 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition}
 *
 * <dt>Int16 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition}
 *
 * <dt>Int32 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition}
 *
 * <dt>Int64 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition}
 *
 * <dt>Uint8 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition}
 *
 * <dt>Uint16 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition}
 *
 * <dt>Uint32 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition}
 *
 * <dt>Uint64 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition}
 *
 * <dt>Decimal64 built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition}
 *
 * <dt>Boolean built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition}
 *
 * <dt>Enumeration built-in type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition}
 *
 * <dt>Bits Built-In Type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition}
 *
 * <dt>The binary Built-In Type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition}
 *
 * <dt>The leafref Built-In Type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition}
 *
 * <dt>The identityref Built-In Type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition}
 *
 * <dt>The empty Built-In Type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition}
 *
 * <dt>The union Built-In Type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition}
 * <dt>The instance-identifier Built-In Type
 *   <dd>{@link org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition}
 *
 * </dl>
 */
@org.osgi.annotation.bundle.Export
package org.opendaylight.yangtools.yang.model.api;

