/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype.Builder;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Builder for {@link DataRootArchetype}.
 */
public abstract sealed class DataRootArchetypeBuilder extends AbstractGeneratedTypeBuilder<DataRootArchetype.Builder>
        implements DataRootArchetype.Builder {
    public static final class Codegen extends DataRootArchetypeBuilder {
        private String description;
        private String reference;
        private String moduleName;

        @NonNullByDefault
        public Codegen(final JavaTypeName typeName) {
            super(typeName);
        }

        @Override
        public void setDescription(final String description) {
            this.description = requireNonNull(description);
        }

        @Override
        public void setModuleName(final String moduleName) {
            this.moduleName = requireNonNull(moduleName);
        }

        @Override
        public void setReference(final String reference) {
            this.reference = requireNonNull(reference);
        }

        @Override
        public DataRootArchetype build() {

//            Codegen{identifier=org.opendaylight.yang.gen.v1.bar.norev.BarData,
//            constants=[],
//            enumerations=[],
//            methods=[
//              MethodSignatureBuilderImpl [
//                name=getBar,
//                returnType=GeneratedTypeImpl{identifier=org.opendaylight.yang.gen.v1.bar.norev.Bar,
//                annotations=[],
//                enclosedTypes=[],
//                enumerations=[],
//                constants=[
//                  Constant [
//                    type=ConcreteTypeImpl{identifier=org.opendaylight.yangtools.yang.common.QName},
//                    name=QNAME, value=org.opendaylight.yang.svc.v1.bar.norev.YangModuleInfoImpl=bar]],
//                methodSignatures=[
//                  MethodSignatureImpl [
//                    name=implementedInterface,
//                    comment=null,
//                    returnType=ParametrizedTypeImpl{identifier=java.lang.Class},
//                    params=[],
//                    annotations=[AnnotationTypeImpl{identifier=java.lang.Override, annotations=[], parameters=[]}]],
//                  MethodSignatureImpl [
//                    name=bindingHashCode,
//                    comment=null,
//                    returnType=ConcreteTypeImpl{identifier=int}, params=[], annotations=[]],
//                  MethodSignatureImpl [name=bindingEquals, comment=null, returnType=ConcreteTypeImpl{identifier=boolean}, params=[], annotations=[]], MethodSignatureImpl [name=bindingToString, comment=null, returnType=ConcreteTypeImpl{identifier=java.lang.String}, params=[], annotations=[]]]}, parameters=[], annotationBuilders=[], comment=null], MethodSignatureBuilderImpl [name=nonnullBar, returnType=GeneratedTypeImpl{identifier=org.opendaylight.yang.gen.v1.bar.norev.Bar, annotations=[], enclosedTypes=[], enumerations=[], constants=[Constant [type=ConcreteTypeImpl{identifier=org.opendaylight.yangtools.yang.common.QName}, name=QNAME, value=org.opendaylight.yang.svc.v1.bar.norev.YangModuleInfoImpl=bar]], methodSignatures=[MethodSignatureImpl [name=implementedInterface, comment=null, returnType=ParametrizedTypeImpl{identifier=java.lang.Class}, params=[], annotations=[AnnotationTypeImpl{identifier=java.lang.Override, annotations=[], parameters=[]}]], MethodSignatureImpl [name=bindingHashCode, comment=null, returnType=ConcreteTypeImpl{identifier=int}, params=[], annotations=[]], MethodSignatureImpl [name=bindingEquals, comment=null, returnType=ConcreteTypeImpl{identifier=boolean}, params=[], annotations=[]], MethodSignatureImpl [name=bindingToString, comment=null, returnType=ConcreteTypeImpl{identifier=java.lang.String}, params=[], annotations=[]]]}, parameters=[], annotationBuilders=[], comment=null]],
//            annotations=[],
//            implements=[ParametrizedTypeImpl{identifier=org.opendaylight.yangtools.binding.DataRoot}]}


            
            
            
            return new DataRootArchetype(getIdentifier(), getImplementsTypes(),
                getMethodDefinitions(),
                null,
                getEnumerations(),
                description,
                reference,
                moduleName);
            
//            super(builder.getIdentifier());
//            comment = builder.getComment();
//            annotations = toUnmodifiableAnnotations(builder.getAnnotations());
//            implementsTypes = makeUnmodifiable(builder.getImplementsTypes());
//            constants = makeUnmodifiable(builder.getConstants());
//            enumerations = List.copyOf(builder.getEnumerations());
//            methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
//            enclosedTypes = List.copyOf(builder.getEnclosedTransferObjects());
//            properties = toUnmodifiableProperties(builder.getProperties());
//            isAbstract = builder.isAbstract();
//            definition = builder.getYangSourceDefinition().orElse(null);

            // required:
            // addImplementsType
            // addMethod
            // addEnclosingTransferObject
            // addEnumeration

            // optional:
            // description
            // reference
            // moduleName

            // ignore everything else

            // FIXME: implement this
            throw new UnsupportedOperationException();
        }
    }

    public static final class Runtime extends DataRootArchetypeBuilder {
        @NonNullByDefault
        public Runtime(final JavaTypeName typeName) {
            super(typeName);
        }

        @Override
        public void setDescription(final String description) {
            // No-op
        }

        @Override
        public void setModuleName(final String moduleName) {
            // No-op
        }

        @Override
        public void setReference(final String reference) {
            // No-op
        }

        @Override
        public DataRootArchetype build() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException();
        }
    }

    @NonNullByDefault
    DataRootArchetypeBuilder(final JavaTypeName typeName) {
        super(typeName);
        addImplementsType(BindingTypes.dataRoot(this));
    }

    @Override
    protected final Builder thisInstance() {
        return this;
    }
}
