/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.parseToClassName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.binding.generator.util.AbstractBaseType;
import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

public final class EnumerationBuilderImpl extends AbstractBaseType implements EnumBuilder {
    private final String packageName;
    private final String name;
    private final List<Enumeration.Pair> values;
    private final List<AnnotationTypeBuilder> annotationBuilders = new ArrayList<>();

    public EnumerationBuilderImpl(final String packageName, final String name) {
        super(packageName, name);
        this.packageName = packageName;
        this.name = name;
        values = new ArrayList<>();
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        if (packageName != null && name != null) {
            final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
            if (annotationBuilders.add(builder)) {
                return builder;
            }
        }
        return null;
    }

    @Override
    public void addValue(final String name, final Integer value) {
        values.add(new EnumPairImpl(name, value));
    }

    @Override
    public Enumeration toInstance(final Type definingType) {
        return new EnumerationImpl(definingType, annotationBuilders, packageName, name, values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EnumerationBuilderImpl other = (EnumerationBuilderImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        } else if (!packageName.equals(other.packageName)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnumerationBuilderImpl [packageName=");
        builder.append(packageName);
        builder.append(", name=");
        builder.append(name);
        builder.append(", values=");
        builder.append(values);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef) {
        final List<EnumPair> enums = enumTypeDef.getValues();
        if (enums != null) {
            int listIndex = 0;
            for (final EnumPair enumPair : enums) {
                if (enumPair != null) {
                    final String enumPairName = parseToClassName(enumPair.getName());
                    Integer enumPairValue = enumPair.getValue();

                    if (enumPairValue == null) {
                        enumPairValue = listIndex;
                    }
                    this.addValue(enumPairName, enumPairValue);
                    listIndex++;
                }
            }
        }

    }

    private static final class EnumPairImpl implements Enumeration.Pair {

        private final String name;
        private final Integer value;

        public EnumPairImpl(String name, Integer value) {
            super();
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            EnumPairImpl other = (EnumPairImpl) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("EnumPair [name=");
            builder.append(name);
            builder.append(", value=");
            builder.append(value);
            builder.append("]");
            return builder.toString();
        }
    }

    private static final class EnumerationImpl implements Enumeration {

        private final Type definingType;
        private final String packageName;
        private final String name;
        private final List<Pair> values;
        private List<AnnotationType> annotations = new ArrayList<>();

        public EnumerationImpl(final Type definingType, final List<AnnotationTypeBuilder> annotationBuilders,
                final String packageName, final String name, final List<Pair> values) {
            super();
            this.definingType = definingType;
            for (final AnnotationTypeBuilder builder : annotationBuilders) {
                annotations.add(builder.toInstance());
            }
            this.annotations = Collections.unmodifiableList(annotations);
            this.packageName = packageName;
            this.name = name;
            this.values = Collections.unmodifiableList(values);
        }

        @Override
        public Type getParentType() {
            return definingType;
        }

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFullyQualifiedName() {
            return packageName + "." + name;
        }

        @Override
        public List<Pair> getValues() {
            return values;
        }

        @Override
        public List<AnnotationType> getAnnotations() {
            return annotations;
        }

        @Override
        public String toFormattedString() {
            StringBuilder builder = new StringBuilder();
            builder.append("public enum");
            builder.append(" ");
            builder.append(name);
            builder.append(" {");
            builder.append("\n");

            int i = 0;
            for (final Enumeration.Pair valPair : values) {
                builder.append("\t");
                builder.append(" ");
                builder.append(valPair.getName());
                builder.append(" (");
                builder.append(valPair.getValue());

                if (i == (values.size() - 1)) {
                    builder.append(" );");
                } else {
                    builder.append(" ),");
                }
                ++i;
            }
            builder.append("\n}");
            return builder.toString();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
            result = prime * result + ((values == null) ? 0 : values.hashCode());

            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            EnumerationImpl other = (EnumerationImpl) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (packageName == null) {
                if (other.packageName != null) {
                    return false;
                }
            } else if (!packageName.equals(other.packageName)) {
                return false;
            }
            if (values == null) {
                if (other.values != null) {
                    return false;
                }
            } else if (!values.equals(other.values)) {
                return false;
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Enumeration [packageName=");
            builder.append(packageName);
            if (definingType != null) {
                builder.append(", definingType=");
                builder.append(definingType.getPackageName());
                builder.append(".");
                builder.append(definingType.getName());
            } else {
                builder.append(", definingType= null");
            }
            builder.append(", name=");
            builder.append(name);
            builder.append(", values=");
            builder.append(values);
            builder.append("]");
            return builder.toString();
        }

        @Override
        public String getComment() {
            return null;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public List<Type> getImplements() {
            return Collections.emptyList();
        }

        @Override
        public List<GeneratedType> getEnclosedTypes() {
            return Collections.emptyList();
        }

        @Override
        public List<Enumeration> getEnumerations() {
            return Collections.emptyList();
        }

        @Override
        public List<Constant> getConstantDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public List<MethodSignature> getMethodDefinitions() {
            // TODO Auto-generated method stub
            return Collections.emptyList();
        }
    }
}
