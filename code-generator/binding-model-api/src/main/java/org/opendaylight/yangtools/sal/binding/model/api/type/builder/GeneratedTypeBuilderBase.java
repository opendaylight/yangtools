/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api.type.builder;

import java.util.List;

import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;

public interface GeneratedTypeBuilderBase<T extends GeneratedTypeBuilderBase<T>> extends Type {

    /**
     * Adds new Enclosing Transfer Object into definition of Generated Type and
     * returns <code>new</code> Instance of Generated TO Builder. <br>
     * There is no need of specifying of Package Name because enclosing Type is
     * already defined inside Generated Type with specific package name. <br>
     * The name of enclosing Type cannot be same as Name of parent type and if
     * there is already defined enclosing type with the same name, the new
     * enclosing type will simply overwrite the older definition. <br>
     * If the name of enclosing type is <code>null</code> the method SHOULD
     * throw {@link IllegalArgumentException}
     *
     * @param name
     *            Name of Enclosing Type
     * @return <code>new</code> Instance of Generated Type Builder.
     */
    GeneratedTOBuilder addEnclosingTransferObject(String name);

    /**
     * Adds new Enclosing Transfer Object <code>genTOBuilder</code> into
     * definition of Generated Type
     *
     * <br>
     * There is no need of specifying of Package Name because enclosing Type is
     * already defined inside Generated Type with specific package name. <br>
     * The name of enclosing Type cannot be same as Name of parent type and if
     * there is already defined enclosing type with the same name, the new
     * enclosing type will simply overwrite the older definition. <br>
     * If the parameter <code>genTOBuilder</code> of enclosing type is
     * <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}
     *
     * @param genTOBuilder
     *            Name of Enclosing Type
     */
    T addEnclosingTransferObject(GeneratedTOBuilder genTOBuilder);

    /**
     * Adds String definition of comment into Method Signature definition. <br>
     * The comment String MUST NOT contain anny comment specific chars (i.e.
     * "/**" or "//") just plain String text description.
     *
     * @param comment
     *            Comment String.
     */
    T addComment(String comment);

    /**
     * The method creates new AnnotationTypeBuilder containing specified package
     * name an annotation name. <br>
     * Neither the package name or annotation name can contain <code>null</code>
     * references. In case that any of parameters contains <code>null</code> the
     * method SHOULD thrown {@link IllegalArgumentException}
     *
     * @param packageName
     *            Package Name of Annotation Type
     * @param name
     *            Name of Annotation Type
     * @return <code>new</code> instance of Annotation Type Builder.
     */
    AnnotationTypeBuilder addAnnotation(String packageName, String name);

    boolean isAbstract();

    /**
     * Sets the <code>abstract</code> flag to define Generated Type as
     * <i>abstract</i> type.
     *
     * @param isAbstract
     *            abstract flag
     */
    T setAbstract(boolean isAbstract);

    List<Type> getImplementsTypes();

    /**
     * Add Type to implements.
     *
     * @param genType
     *            Type to implement
     * @return <code>true</code> if the addition of type is successful.
     */
    T addImplementsType(Type genType);

    /**
     * Adds Constant definition and returns <code>new</code> Constant instance. <br>
     * By definition Constant MUST be defined by return Type, Name and assigned
     * value. The name SHOULD be defined with capital letters. Neither of method
     * parameters can be <code>null</code> and the method SHOULD throw
     * {@link IllegalArgumentException} if the contract is broken.
     *
     * @param type
     *            Constant Type
     * @param name
     *            Name of Constant
     * @param value
     *            Assigned Value
     * @return <code>new</code> Constant instance.
     */
    Constant addConstant(Type type, String name, Object value);

    /**
     * Adds new Enumeration definition for Generated Type Builder and returns
     * Enum Builder for specifying all Enum parameters. <br>
     * If there is already Enumeration stored with the same name, the old enum
     * will be simply overwritten byt new enum definition. <br>
     * Name of Enumeration cannot be <code>null</code>, if it is
     * <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}
     *
     * @param name
     *            Enumeration Name
     * @return <code>new</code> instance of Enumeration Builder.
     */
    EnumBuilder addEnumeration(String name);

    List<MethodSignatureBuilder> getMethodDefinitions();

    /**
     * Add new Method Signature definition for Generated Type Builder and
     * returns Method Signature Builder for specifying all Method parameters. <br>
     * Name of Method cannot be <code>null</code>, if it is <code>null</code>
     * the method SHOULD throw {@link IllegalArgumentException} <br>
     * By <i>Default</i> the MethodSignatureBuilder SHOULD be pre-set as
     * {@link MethodSignatureBuilder#setAbstract(boolean)},
     * {TypeMemberBuilder#setFinal(boolean)} and
     * {TypeMemberBuilder#setAccessModifier(boolean)}
     *
     * @param name
     *            Name of Method
     * @return <code>new</code> instance of Method Signature Builder.
     */
    MethodSignatureBuilder addMethod(String name);

    /**
     * Checks if GeneratedTypeBuilder contains method with name
     * <code>methodName</code>
     *
     * @param methodName
     *            is method name
     */
    boolean containsMethod(String methodName);

    List<GeneratedPropertyBuilder> getProperties();

    /**
     * Add new Generated Property definition for Generated Transfer Object
     * Builder and returns Generated Property Builder for specifying Property. <br>
     * Name of Property cannot be <code>null</code>, if it is <code>null</code>
     * the method SHOULD throw {@link IllegalArgumentException}
     *
     * @param name
     *            Name of Property
     * @return <code>new</code> instance of Generated Property Builder.
     */
    GeneratedPropertyBuilder addProperty(String name);

    /**
     * Check whether GeneratedTOBuilder contains property with name
     * <code>name</code>
     *
     * @param name
     *            of property which existance is checked
     * @return true if property <code>name</code> exists in list of properties.
     */
    boolean containsProperty(String name);

    /**
     * Set a string that contains a human-readable textual description of type
     * definition.
     *
     * @param description
     *            a string that contains a human-readable textual description of
     *            type definition.
     */
    void setDescription(String description);

    /**
     * Set the name of the module, in which generated type was specified.
     *
     * @param moduleName
     *            the name of the module
     */
    void setModuleName(String moduleName);

    /**
     * Set a list of QNames which represent schema path in schema tree from
     * actual concrete type to the root.
     *
     * @param schemaPath
     *            a list of QNames which represent schema path in schema tree
     */
    void setSchemaPath(Iterable<QName> schemaPath);

    /**
     * Set a string that is used to specify a textual cross-reference to an
     * external document, either another module that defines related management
     * information, or a document that provides additional information relevant
     * to this definition.
     *
     * @param reference
     *            a textual cross-reference to an external document.
     */
    void setReference(String reference);

}
