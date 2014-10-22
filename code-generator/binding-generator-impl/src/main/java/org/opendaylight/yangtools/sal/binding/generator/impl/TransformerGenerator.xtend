/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl

import com.google.common.base.Joiner
import java.io.File
import java.security.ProtectionDomain
import java.util.AbstractMap.SimpleEntry
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Map.Entry
import java.util.Set
import java.util.TreeSet
import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.sal.binding.generator.util.CodeGenerationException
import org.opendaylight.yangtools.sal.binding.generator.util.SourceCodeGenerator
import org.opendaylight.yangtools.sal.binding.generator.util.SourceCodeGeneratorFactory
import org.opendaylight.yangtools.sal.binding.generator.util.XtendHelper
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder
import org.opendaylight.yangtools.util.ClassLoaderUtils
import org.opendaylight.yangtools.yang.binding.Augmentation
import org.opendaylight.yangtools.yang.binding.BindingCodec
import org.opendaylight.yangtools.yang.binding.BindingDeserializer
import org.opendaylight.yangtools.yang.binding.BindingMapping
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode
import org.opendaylight.yangtools.yang.model.api.ChoiceNode
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition
import org.opendaylight.yangtools.yang.model.util.EnumerationType
import org.opendaylight.yangtools.yang.model.util.ExtendedType
import org.slf4j.LoggerFactory

import static com.google.common.base.Preconditions.*
import static javassist.Modifier.*
import static org.opendaylight.yangtools.sal.binding.generator.impl.CodecMapping.*

import static extension org.opendaylight.yangtools.sal.binding.generator.util.YangSchemaUtils.*

class TransformerGenerator extends AbstractTransformerGenerator {
    private static val LOG = LoggerFactory.getLogger(TransformerGenerator)

    public static val STRING = Types.typeForClass(String);
    public static val BOOLEAN = Types.typeForClass(Boolean);
    public static val INTEGER = Types.typeForClass(Integer);
    public static val INSTANCE_IDENTIFIER = Types.typeForClass(InstanceIdentifier);
    //public static val DECIMAL = Types.typeForClass(Decimal);
    public static val LONG = Types.typeForClass(Long);
    public static val CLASS_TYPE = Types.typeForClass(Class);

    @Property
    var File classFileCapturePath;

    val CtClass BINDING_CODEC
    val CtClass ctQName

    val SourceCodeGeneratorFactory sourceCodeGeneratorFactory = new SourceCodeGeneratorFactory();

    public new(TypeResolver typeResolver, ClassPool pool) {
        super(typeResolver, pool)

        BINDING_CODEC = BindingCodec.asCtClass;
        ctQName = QName.asCtClass
    }

    override transformerForImpl(Class inputType) {
        return runOnClassLoader(inputType.classLoader) [ |
            val ret = getGeneratedClass(inputType)
            if (ret !== null) {
                listener.onClassProcessed(inputType);
                return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
            }
            val ref = Types.typeForClass(inputType)
            val node = getSchemaNode(ref)
            createMapping(inputType, node, null)
            val typeSpecBuilder = getDefinition(ref)
            checkState(typeSpecBuilder !== null, "Could not find typedefinition for %s", inputType.name);
            val typeSpec = typeSpecBuilder.toInstance();
            val newret = generateTransformerFor(inputType, typeSpec, node);
            listener.onClassProcessed(inputType);
            return newret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        ]
    }

    def Class<? extends BindingCodec<Map<QName, Object>, Object>> transformerFor(Class<?> inputType, DataSchemaNode node) {
        return runOnClassLoader(inputType.classLoader) [ |
            createMapping(inputType, node, null)
            val ret = getGeneratedClass(inputType)
            if (ret !== null) {
                listener.onClassProcessed(inputType);
                return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
            }
            val ref = Types.typeForClass(inputType)
            var typeSpecBuilder = getDefinition(ref)
            if (typeSpecBuilder == null) {
                typeSpecBuilder = getTypeBuilder(node.path);
            }

            checkState(typeSpecBuilder !== null, "Could not find TypeDefinition for %s, $s", inputType.name, node);
            val typeSpec = typeSpecBuilder.toInstance();
            val newret = generateTransformerFor(inputType, typeSpec, node);
            listener.onClassProcessed(inputType);
            return newret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        ]
    }

    override augmentationTransformerForImpl(Class inputType) {
        return runOnClassLoader(inputType.classLoader) [ |

            val ret = getGeneratedClass(inputType)
            if (ret !== null) {
                return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
            }
            val ref = Types.typeForClass(inputType)
            val node = getAugmentation(ref)
            val typeSpecBuilder = getDefinition(ref)
            val typeSpec = typeSpecBuilder.toInstance();
            //mappingForNodes(node.childNodes, typeSpec.allProperties, bindingId)
            val newret = generateAugmentationTransformerFor(inputType, typeSpec, node);
            listener.onClassProcessed(inputType);
            return newret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        ]
    }

    override caseCodecForImpl(Class inputType, ChoiceCaseNode node) {
        return runOnClassLoader(inputType.classLoader) [ |
            createMapping(inputType, node, null)
            val ret = getGeneratedClass(inputType)
            if (ret !== null) {
                return ret as Class<? extends BindingCodec<Object, Object>>;
            }
            val ref = Types.typeForClass(inputType)
            val typeSpecBuilder = getDefinition(ref)
            val typeSpec = typeSpecBuilder.toInstance();
            val newret = generateCaseCodec(inputType, typeSpec, node);
            return newret as Class<? extends BindingCodec<Object, Object>>;
        ]
    }

    override keyTransformerForIdentifiableImpl(Class parentType) {
        return runOnClassLoader(parentType.classLoader) [ |
            val inputName = parentType.name + "Key";
            val inputType = loadClass(inputName);
            val ret = getGeneratedClass(inputType)
            if (ret !== null) {
                return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
            }
            val ref = Types.typeForClass(parentType)
            val node = getSchemaNode(ref) as ListSchemaNode
            val typeSpecBuilder = getDefinition(ref)
            val typeSpec = typeSpecBuilder.identifierDefinition;
            val newret = generateKeyTransformerFor(inputType, typeSpec, node);
            return newret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        ]
    }

    private def void createMapping(Class<?> inputType, SchemaNode node, InstanceIdentifier<?> parentId) {
        var ClassLoader cl = inputType.classLoader
        if (cl === null) {
            cl = Thread.currentThread.contextClassLoader
        }
        ClassLoaderUtils.withClassLoader(cl,
            [ |
                if (!(node instanceof DataNodeContainer)) {
                    return null
                }
                var InstanceIdentifier<?> bindingId = getBindingIdentifierByPath(node.path)
                if (bindingId != null) {
                    return null
                }
                val ref = Types.typeForClass(inputType)
                var typeSpecBuilder = getDefinition(ref)
                if (typeSpecBuilder == null) {
                    typeSpecBuilder = getTypeBuilder(node.path);
                }
                checkState(typeSpecBuilder !== null, "Could not find type definition for %s, $s", inputType.name, node);
                val typeSpec = typeSpecBuilder.toInstance();
                var InstanceIdentifier<?> parent
                if (parentId == null) {
                    bindingId = InstanceIdentifier.create(inputType as Class)
                    parent = bindingId
                    putPathToBindingIdentifier(node.path, bindingId)
                } else {
                    parent = putPathToBindingIdentifier(node.path, parentId, inputType)
                }
                val Map<String, Type> properties = typeSpec.allProperties
                if (node instanceof DataNodeContainer) {
                    mappingForNodes((node as DataNodeContainer).childNodes, properties, parent)
                } else if (node instanceof ChoiceNode) {
                    mappingForNodes((node as ChoiceNode).cases, properties, parent)
                }
                return null;
            ])
    }

    private def void mappingForNodes(Collection<? extends DataSchemaNode> childNodes, Map<String, Type> properties,
        InstanceIdentifier<?> parent) {
        for (DataSchemaNode child : childNodes) {
            val signature = properties.getFor(child)
            if (signature != null) {
                val Type childType = signature.value
                var Class<?> childTypeClass = null;
                if (child instanceof ListSchemaNode && childType instanceof ParameterizedType) {
                    childTypeClass = loadClass((childType as ParameterizedType).actualTypeArguments.get(0))
                } else {
                    childTypeClass = loadClass(childType)
                }
                createMapping(childTypeClass, child, parent)
            }
        }
    }

    def getIdentifierDefinition(GeneratedTypeBuilder builder) {
        val inst = builder.toInstance
        val keyMethod = inst.methodDefinitions.findFirst[name == "getKey"]
        return keyMethod.returnType as GeneratedTransferObject
    }

    override keyTransformerForIdentifierImpl(Class inputType) {
        return runOnClassLoader(inputType.classLoader) [ |
            val ret = getGeneratedClass(inputType)
            if (ret !== null) {
                return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
            }
            val ref = Types.typeForClass(inputType)
            val node = getSchemaNode(ref) as ListSchemaNode
            val typeSpecBuilder = getDefinition(ref)
            val typeSpec = typeSpecBuilder.toInstance();
            val newret = generateKeyTransformerFor(inputType, typeSpec, node);
            return newret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        ]
    }

    private def Class<?> keyTransformerFor(Class<?> inputType, GeneratedType type, ListSchemaNode schema) {
        return runOnClassLoader(inputType.classLoader) [ |
            val transformer = getGeneratedClass(inputType)
            if (transformer != null) {
                return transformer;
            }
            val newret = generateKeyTransformerFor(inputType, type, schema);
            return newret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        ]
    }

    private def Class<?> getGeneratedClass(Class<? extends Object> cls) {

        try {
            return loadClass(cls.codecClassName)
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private def Class<?> keyTransformer(GeneratedType type, ListSchemaNode node) {
        val cls = loadClass(type.resolvedName + "Key");
        keyTransformerFor(cls, type, node);
    }

    private def serializer(Type type, DataSchemaNode node) {
        val cls = loadClass(type.resolvedName);
        transformerFor(cls, node);
    }

    private def Class<?> valueSerializer(GeneratedTransferObject type, TypeDefinition<?> typeDefinition) {
        val cls = loadClass(type.resolvedName);
        val transformer = cls.generatedClass;
        if (transformer !== null) {
            return transformer;
        }
        var baseType = typeDefinition;
        while (baseType.baseType != null) {
            baseType = baseType.baseType;
        }
        val finalType = baseType;
        return runOnClassLoader(cls.classLoader) [ |
            val valueTransformer = generateValueTransformer(cls, type, finalType);
            return valueTransformer;
        ]
    }

    private def Class<?> valueSerializer(Enumeration type, TypeDefinition<?> typeDefinition) {
        val cls = loadClass(type.resolvedName);
        val transformer = cls.generatedClass;
        if (transformer !== null) {
            return transformer;
        }

        return runOnClassLoader(cls.classLoader) [ |
            val valueTransformer = generateValueTransformer(cls, type, typeDefinition);
            return valueTransformer;
        ]
    }

    private def generateKeyTransformerFor(Class<? extends Object> inputType, GeneratedType typeSpec, ListSchemaNode node) {
        try {

            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            //log.info("Generating DOM Codec for {} with {}", inputType, inputType.classLoader)
            val properties = typeSpec.allProperties;
            val ctCls = createClass(inputType.codecClassName) [
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                staticQNameField(node.QName, sourceGenerator);
                implementsType(BINDING_CODEC)
                method(Object, "toDomStatic", #[QName, Object]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            «QName.name» _resultName;
                            if($1 != null) {
                                _resultName = «QName.name».create($1,QNAME.getLocalName());
                            } else {
                                _resultName = QNAME;
                            }
                            java.util.List _childNodes = new java.util.ArrayList();
                            «inputType.resolvedName» value = («inputType.name») $2;
                            «FOR key : node.keyDefinition»
                                «val propertyName = key.getterName»
                                «val keyDef = node.getDataChildByName(key)»
                                «val property = properties.get(propertyName)»
                                «serializeProperty(keyDef, property, propertyName)»;
                            «ENDFOR»
                            return ($r) java.util.Collections.singletonMap(_resultName,_childNodes);
                        }
                    '''
                    setBodyChecked(body, sourceGenerator)
                ]
                method(Object, "fromDomStatic", #[QName, Object]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            if($2 == null){
                                return  null;
                            }
                            «QName.name» _localQName = $1;
                            java.util.Map _compositeNode = (java.util.Map) $2;
                            boolean _is_empty = true;
                            «FOR key : node.keyDefinition»
                                «val propertyName = key.getterName»
                                «val keyDef = node.getDataChildByName(key)»
                                «val property = properties.get(propertyName)»
                                «deserializeProperty(keyDef, property, propertyName)»;
                            «ENDFOR»
                            «inputType.resolvedName» _value = new «inputType.name»(«node.keyDefinition.
                            keyConstructorList»);
                            return _value;
                        }
                    '''
                    setBodyChecked(body, sourceGenerator)
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            java.util.Map.Entry _input =  (java.util.Map.Entry) $1;
                            «QName.name» _localQName = («QName.name») _input.getKey();
                            «inputType.name» _keyValue = («inputType.name») _input.getValue();
                            return toDomStatic(_localQName,_keyValue);
                        }
                    '''
                    setBodyChecked(body, sourceGenerator)
                ]
                method(Object, "deserialize", Object) [
                    val body = '''
                        {
                            «QName.name» _qname = QNAME;
                            if($1 instanceof java.util.Map.Entry) {
                                _qname = («QName.name») ((java.util.Map.Entry) $1).getKey();
                            }
                            return fromDomStatic(_qname,$1);
                        }
                    '''
                    setBodyChecked(body, sourceGenerator)
                ]
            ]
            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)
            sourceGenerator.outputGeneratedSource( ctCls )
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret as Class<? extends BindingCodec<Map<QName,Object>, ?>>;
        } catch (Exception e) {
            processException(inputType, e);
            return null;
        }
    }

    private def Class<? extends BindingCodec<Object, Object>> generateCaseCodec(Class<?> inputType, GeneratedType type,
        ChoiceCaseNode node) {
        try {
            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            //log.info("Generating DOM Codec for {} with {}, TCCL is: {}", inputType, inputType.classLoader,Thread.currentThread.contextClassLoader)
            val ctCls = createClass(type.codecClassName) [
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                implementsType(BINDING_CODEC)
                staticQNameField(node.QName, sourceGenerator);
                staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                staticField(it, AUGMENTATION_CODEC, BindingCodec, sourceGenerator)
                staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                method(Object, "toDomStatic", #[QName, Object]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            «QName.name» _resultName = «QName.name».create($1,QNAME.getLocalName());
                            java.util.List _childNodes = new java.util.ArrayList();
                            «type.resolvedName» value = («type.resolvedName») $2;
                            «transformDataContainerBody(type, type.allProperties, node)»
                            return ($r) _childNodes;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator)
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            java.util.Map.Entry _input = (java.util.Map.Entry) $1;
                            «QName.name» _localName = QNAME;
                            if(_input.getKey() != null) {
                                _localName = («QName.name») _input.getKey();
                            }
                            return toDomStatic(_localName,_input.getValue());
                        }
                    '''
                    setBodyChecked( body, sourceGenerator)
                ]
                method(Object, "fromDomStatic", #[QName, Object, InstanceIdentifier]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    setBodyChecked( deserializeBody(type, node, getBindingIdentifierByPath(node.path)),
                                    sourceGenerator )
                ]
                method(Object, "deserialize", #[Object, InstanceIdentifier]) [
                    val body = '''
                        {
                            //System.out.println("«type.name»#deserialize: " +$1);
                            java.util.Map.Entry _input = (java.util.Map.Entry) $1;
                            return fromDomStatic((«QName.name»)_input.getKey(),_input.getValue(),$2);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator)
                ]
            ]

            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)  as Class<? extends BindingCodec<Object, Object>>
            sourceGenerator.outputGeneratedSource( ctCls )
            listener?.onDataContainerCodecCreated(inputType, ret);
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret;
        } catch (Exception e) {
            processException(inputType, e);
            return null;
        }
    }

    private def dispatch  Class<? extends BindingCodec<Map<QName, Object>, Object>> generateTransformerFor(
        Class<?> inputType, GeneratedType typeSpec, SchemaNode node) {
        try {

            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            //log.info("Generating DOM Codec for {} with {}", inputType, inputType.classLoader)
            val ctCls = createClass(typeSpec.codecClassName) [
 
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                staticQNameField(node.QName, sourceGenerator);
                staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                staticField(it, AUGMENTATION_CODEC, BindingCodec, sourceGenerator)
                implementsType(BINDING_CODEC)

                method(Object, "toDomStatic", #[QName, Object]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    setBodyChecked( serializeBodyFacade(typeSpec, node), sourceGenerator )
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            java.util.Map.Entry _input = (java.util.Map.Entry) $1;
                            «QName.name» _localName = QNAME;
                            if(_input.getKey() != null) {
                                _localName = («QName.name») _input.getKey();
                            }
                            return toDomStatic(_localName,_input.getValue());
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]

                method(Object, "fromDomStatic", #[QName, Object, InstanceIdentifier]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    setBodyChecked( deserializeBody(typeSpec, node, getBindingIdentifierByPath(node.path)),
                                    sourceGenerator )
                ]

                method(Object, "deserialize", #[Object, InstanceIdentifier]) [
                    val body = '''
                        {
                            «QName.name» _qname = QNAME;
                            if($1 instanceof java.util.Map.Entry) {
                                _qname = («QName.name») ((java.util.Map.Entry) $1).getKey();
                            }
                            return fromDomStatic(_qname,$1,$2);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
            ]

            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain) as Class<? extends BindingCodec<Map<QName,Object>, Object>>

            sourceGenerator.outputGeneratedSource( ctCls )

            listener?.onDataContainerCodecCreated(inputType, ret);
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret;
        } catch (Exception e) {
            processException(inputType, e);
            return null;
        }
    }

    private def Class<? extends BindingCodec<Map<QName, Object>, Object>> generateAugmentationTransformerFor(
        Class<?> inputType, GeneratedType type, AugmentationSchema node) {
        try {

            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            //log.info("Generating DOM Codec for {} with {}", inputType, inputType.classLoader)
            val properties = type.allProperties
            val ctCls = createClass(type.codecClassName) [
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                staticQNameField(node.augmentationQName, sourceGenerator);
                staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                staticField(it, AUGMENTATION_CODEC, BindingCodec, sourceGenerator)
                staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                implementsType(BINDING_CODEC)

                method(Object, "toDomStatic", #[QName, Object]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            ////System.out.println("Qname " + $1);
                            ////System.out.println("Value " + $2);
                            «QName.name» _resultName = «QName.name».create(QNAME,QNAME.getLocalName());
                            java.util.List _childNodes = new java.util.ArrayList();
                            «type.resolvedName» value = («type.resolvedName») $2;
                            «FOR child : node.childNodes»
                                «var signature = properties.getFor(child)»
                                ////System.out.println("«signature.key»" + value.«signature.key»());
                                «serializeProperty(child, signature.value, signature.key)»
                            «ENDFOR»
                            return ($r) _childNodes;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            java.util.Map.Entry _input = (java.util.Map.Entry) $1;
                            «QName.name» _localName = QNAME;
                            if(_input.getKey() != null) {
                                _localName = («QName.name») _input.getKey();
                            }
                            return toDomStatic(_localName,_input.getValue());
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]

                method(Object, "fromDomStatic", #[QName, Object, InstanceIdentifier]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            «QName.name» _localQName = QNAME;

                            if($2 == null) {
                            return null;
                            }
                            java.util.Map _compositeNode = (java.util.Map) $2;
                            //System.out.println(_localQName + " " + _compositeNode);
                            «type.builderName» _builder = new «type.builderName»();
                            boolean _is_empty = true;
                            «FOR child : node.childNodes»
                                «val signature = properties.getFor(child)»
                                «deserializeProperty(child, signature.value, signature.key)»
                                _builder.«signature.key.toSetter»(«signature.key»);
                            «ENDFOR»
                            if(_is_empty) {
                            return null;
                            }
                            return _builder.build();
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]

                method(Object, "deserialize", #[Object, InstanceIdentifier]) [
                    val body = '''
                        {
                            return fromDomStatic(QNAME,$1,$2);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
            ]

            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain) as Class<? extends BindingCodec<Map<QName,Object>, Object>>
            sourceGenerator.outputGeneratedSource( ctCls )
            listener?.onDataContainerCodecCreated(inputType, ret);
            return ret;
        } catch (Exception e) {
            processException(inputType, e);
            return null;
        }
    }

    private def dispatch  Class<? extends BindingCodec<Map<QName, Object>, Object>> generateTransformerFor(
        Class<?> inputType, GeneratedType typeSpec, ChoiceNode node) {
        try {

            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            //log.info("Generating DOM Codec for {} with {}", inputType, inputType.classLoader)
            val ctCls = createClass(typeSpec.codecClassName) [
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                //staticQNameField(inputType);
                staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                staticField(it, DISPATCH_CODEC, BindingCodec, sourceGenerator)
                //staticField(it,QNAME_TO_CASE_MAP,BindingCodec)
                implementsType(BINDING_CODEC)
                method(List, "toDomStatic", #[QName, Object]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            if($2 == null) {
                                return null;
                            }
                            if («DISPATCH_CODEC» == null) {
                                throw new «IllegalStateException.name»("Implementation of codec was not initialized.");
                            }
                            java.util.Map.Entry _input = new «SimpleEntry.name»($1,$2);
                            Object _ret =  «DISPATCH_CODEC».serialize(_input);
                            ////System.out.println("«typeSpec.name»#toDomStatic: " + _ret);
                            return («List.name») _ret;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            throw new «UnsupportedOperationException.name»("Direct invocation not supported.");
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "fromDomStatic", #[QName, Map, InstanceIdentifier]) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            if («DISPATCH_CODEC» == null) {
                                throw new «IllegalStateException.name»("Implementation of codec was not initialized.");
                            }
                            return «DISPATCH_CODEC».deserialize($2,$3);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "deserialize", #[Object, InstanceIdentifier]) [
                    val body = '''
                        {
                            throw new «UnsupportedOperationException.name»("Direct invocation not supported.");
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
            ]

            val rawRet = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)
            sourceGenerator.outputGeneratedSource( ctCls )
            val ret = rawRet as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
            listener?.onChoiceCodecCreated(inputType, ret, node);
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret;
        } catch (Exception e) {
            processException(inputType, e);
            return null;
        }
    }

    private def keyConstructorList(List<QName> qnames) {
        val names = new TreeSet<String>()
        for (name : qnames) {
            val fieldName = name.getterName;
            names.add(fieldName);
        }
        return Joiner.on(",").join(names);
    }

    private def serializeBodyFacade(GeneratedType type, SchemaNode node) {
        val ret = serializeBody(type, node);
        return ret;
    }

    private def String deserializeBody(GeneratedType type, SchemaNode node, InstanceIdentifier<?> bindingId) {
        val ret = deserializeBodyImpl(type, node, bindingId);
        return ret;
    }

    private def deserializeKey(GeneratedType type, ListSchemaNode node) {
        if (node.keyDefinition != null && !node.keyDefinition.empty) {
            return '''
                «type.resolvedName»Key getKey = («type.resolvedName»Key) «keyTransformer(type, node).canonicalName».fromDomStatic(_localQName,_compositeNode);
                _builder.setKey(getKey);
            ''';
        }
    }

    private def String deserializeBodyWithAugmentations(GeneratedType type, DataNodeContainer node, InstanceIdentifier<?> bindingId) '''
        {
            «QName.name» _localQName = «QName.name».create($1,QNAME.getLocalName());
            if($2 == null) {
                return null;
            }
            java.util.Map _compositeNode = (java.util.Map) $2;
            //System.out.println(_localQName + " " + _compositeNode);
            «type.builderName» _builder = new «type.builderName»();
            «deserializeDataNodeContainerBody(type, node, bindingId)»
            «type.deserializeAugmentations»
            return _builder.build();
        }
    '''

    private def dispatch String deserializeBodyImpl(GeneratedType type, SchemaNode node, InstanceIdentifier<?> bindingId) '''
        {
            «QName.name» _localQName = «QName.name».create($1,QNAME.getLocalName());

            if($2 == null) {
            return null;
            }
            java.util.Map _compositeNode = (java.util.Map) $2;
            «type.builderName» _builder = new «type.builderName»();
            return _builder.build();
        }
    '''

    private def dispatch String deserializeBodyImpl(GeneratedType type, ListSchemaNode node, InstanceIdentifier<?> bindingId) '''
        {
            «QName.name» _localQName = «QName.name».create($1,QNAME.getLocalName());
            if($2 == null) {
                return null;
            }
            java.util.Map _compositeNode = (java.util.Map) $2;
            //System.out.println(_localQName + " " + _compositeNode);
            «type.builderName» _builder = new «type.builderName»();
            «deserializeKey(type, node)»
            «deserializeDataNodeContainerBody(type, node, bindingId)»
            «type.deserializeAugmentations»
            return _builder.build();
        }
    '''

    private def dispatch String deserializeBodyImpl(GeneratedType type, ContainerSchemaNode node, InstanceIdentifier<?> bindingId) {
        return deserializeBodyWithAugmentations(type, node, bindingId);
    }

    private def dispatch String deserializeBodyImpl(GeneratedType type, NotificationDefinition node, InstanceIdentifier<?> bindingId) {
        return deserializeBodyWithAugmentations(type, node, bindingId);
    }

    private def dispatch String deserializeBodyImpl(GeneratedType type, ChoiceCaseNode node, InstanceIdentifier<?> bindingId) {
        return deserializeBodyWithAugmentations(type, node, bindingId);
    }

    private def deserializeDataNodeContainerBody(GeneratedType type, DataNodeContainer node, InstanceIdentifier<?> bindingId) {
        deserializeNodeContainerBodyImpl(type, type.allProperties, node, bindingId);
    }

    private def deserializeNodeContainerBodyImpl(GeneratedType type, HashMap<String, Type> properties,
        DataNodeContainer node, InstanceIdentifier<?> bindingId) {
        val ret = '''
            boolean _is_empty = true;
            «FOR child : node.childNodes»
                «val signature = properties.getFor(child)»
                «IF signature !== null»
                    «deserializeProperty(child, signature.value, signature.key)»
                    _builder.«signature.key.toSetter»(«signature.key»);
                «ENDIF»
            «ENDFOR»
        '''
        return ret;
    }

    def deserializeAugmentations(GeneratedType type) '''
        «InstanceIdentifier.resolvedName» iid = $3.builder().child(«type.resolvedName».class).build();
        java.util.Map _augmentation = (java.util.Map) «AUGMENTATION_CODEC».deserialize(_compositeNode,$3);
        if(_augmentation != null) {
            «Iterator.name» _entries = _augmentation.entrySet().iterator();
            while(_entries.hasNext()) {
                java.util.Map.Entry _entry = (java.util.Map.Entry) _entries.next();
                ////System.out.println("Aug. key:" + _entry.getKey());
                Class _type = (Class) _entry.getKey();
                «Augmentation.resolvedName» _value = («Augmentation.name») _entry.getValue();
                if(_value != null) {
                    _builder.addAugmentation(_type,_value);
                }
            }
        }
    '''

    private def dispatch CharSequence deserializeProperty(ListSchemaNode schema, ParameterizedType type,
        String propertyName) '''
        java.util.List _dom_«propertyName» = _compositeNode.get(«QName.name».create(_localQName,"«schema.QName.
            localName»"));
        ////System.out.println("«propertyName»#deCode"+_dom_«propertyName»);
        java.util.List «propertyName» = new java.util.ArrayList();
        if(_dom_«propertyName» != null) {
            java.util.List _serialized = new java.util.ArrayList();
            java.util.Iterator _iterator = _dom_«propertyName».iterator();
            boolean _hasNext = _iterator.hasNext();
            while(_hasNext) {
                Object _listItem = _iterator.next();
                _is_empty = false;
                ////System.out.println("  item" + _listItem);
                «val param = type.actualTypeArguments.get(0)»
                «InstanceIdentifier.resolvedName» iid = $3.builder().child(«param.resolvedName».class).build();
                Object _value = «type.actualTypeArguments.get(0).serializer(schema).resolvedName».fromDomStatic(_localQName,_listItem,iid);
                ////System.out.println("  value" + _value);
                «propertyName».add(_value);
                _hasNext = _iterator.hasNext();
            }
        }

        ////System.out.println(" list" + «propertyName»);
    '''

    private def dispatch CharSequence deserializeProperty(LeafListSchemaNode schema, ParameterizedType type,
        String propertyName) '''
        java.util.List _dom_«propertyName» = _compositeNode.get(«QName.name».create(_localQName,"«schema.QName.
            localName»"));
        java.util.List «propertyName» = new java.util.ArrayList();
        if(_dom_«propertyName» != null) {
            java.util.List _serialized = new java.util.ArrayList();
            java.util.Iterator _iterator = _dom_«propertyName».iterator();
            boolean _hasNext = _iterator.hasNext();
            while(_hasNext) {
                _is_empty = false;
                Object _listItem = _iterator.next();
                if(_listItem instanceof java.util.Map.Entry) {
                    Object _innerValue = ((java.util.Map.Entry) _listItem).getValue();
                    Object _value = «deserializeValue(type.actualTypeArguments.get(0), "_innerValue", schema.type)»;
                    «propertyName».add(_value);
                }
                _hasNext = _iterator.hasNext();
            }
        }
    '''

    private def dispatch CharSequence deserializeProperty(LeafSchemaNode schema, Type type, String propertyName) '''
        java.util.List _dom_«propertyName»_list =
            _compositeNode.get(«QName.name».create(_localQName,"«schema.QName.localName»"));
        «type.resolvedName» «propertyName» = null;
        if(_dom_«propertyName»_list != null && _dom_«propertyName»_list.size() > 0) {
            _is_empty = false;
            java.util.Map.Entry _dom_«propertyName» = (java.util.Map.Entry) _dom_«propertyName»_list.get(0);
            Object _inner_value = _dom_«propertyName».getValue();
            «propertyName» = «deserializeValue(type, "_inner_value", schema.type)»;
        }
    '''

    private def dispatch CharSequence deserializeProperty(ContainerSchemaNode schema, Type type,
        String propertyName) '''
        java.util.List _dom_«propertyName»_list =
            _compositeNode.get(«QName.name».create(_localQName,"«schema.QName.localName»"));
        «type.resolvedName» «propertyName» = null;
        if(_dom_«propertyName»_list != null && _dom_«propertyName»_list.size() > 0) {
            _is_empty = false;
            java.util.Map _dom_«propertyName» = (java.util.Map) _dom_«propertyName»_list.get(0);
            «InstanceIdentifier.resolvedName» iid = $3.builder().child(«type.resolvedName».class).build();
            «propertyName» =  «type.serializer(schema).resolvedName».fromDomStatic(_localQName,_dom_«propertyName»,iid);
        }
    '''

    private def dispatch CharSequence deserializeProperty(ChoiceNode schema, Type type, String propertyName) '''
        «type.resolvedName» «propertyName» = «type.serializer(schema).resolvedName».fromDomStatic(_localQName,_compositeNode,$3);
        if(«propertyName» != null) {
            _is_empty = false;
        }
    '''

    private def dispatch String deserializeValue(GeneratedTransferObject type, String domParameter,
        TypeDefinition<?> typeDefinition) '''
        («type.resolvedName») «type.valueSerializer(typeDefinition).resolvedName».fromDomValue(«domParameter»)
    '''

    private def dispatch String deserializeValue(Enumeration type, String domParameter, TypeDefinition<?> typeDefinition) '''
        («type.resolvedName») «type.valueSerializer(typeDefinition).resolvedName».fromDomValue(«domParameter»)
    '''

    private def dispatch String deserializeValue(Type type, String domParameter, TypeDefinition<?> typeDef) {
        if (INSTANCE_IDENTIFIER.equals(type)) {
            return '''(«InstanceIdentifier.name») «INSTANCE_IDENTIFIER_CODEC».deserialize(«domParameter»)'''
        } else if (CLASS_TYPE.equals(type)) {
            return '''(«Class.name») «IDENTITYREF_CODEC».deserialize(«domParameter»)'''
        } else if (typeDef!=null && typeDef instanceof EmptyTypeDefinition) {
            if(domParameter == null) {
                return ''' Boolean.FALSE '''
            } else {
                return ''' Boolean.TRUE '''
            }
        }
        return '''(«type.resolvedName») «domParameter»'''
    }

    private def dispatch Class<? extends BindingCodec<Map<QName, Object>, Object>> generateValueTransformer(
        Class<?> inputType, GeneratedTransferObject typeSpec, TypeDefinition<?> typeDef) {
        try {

            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            val returnType = typeSpec.valueReturnType;
            if (returnType == null) {
                val ctCls = createDummyImplementation(inputType, typeSpec, sourceGenerator);
                val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)
                sourceGenerator.outputGeneratedSource( ctCls )
                return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
            }

            val ctCls = createClass(typeSpec.codecClassName) [
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                if (inputType.isYangBindingAvailable) {
                    implementsType(BINDING_CODEC)
                    staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                    staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                    implementsType(BindingDeserializer.asCtClass)
                }
                method(Object, "toDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val ctSpec = typeSpec.asCtClass;
                    val body = '''
                        {
                            ////System.out.println("«inputType.simpleName»#toDomValue: "+$1);

                            if($1 == null) {
                            return null;
                            }
                            «typeSpec.resolvedName» _encapsulatedValue = («typeSpec.resolvedName») $1;
                            ////System.out.println("«inputType.simpleName»#toDomValue:Enc: "+_encapsulatedValue);
                            «returnType.resolvedName» _value =  _encapsulatedValue.getValue();
                            ////System.out.println("«inputType.simpleName»#toDomValue:DeEnc: "+_value);
                            Object _domValue = «serializeValue(returnType, "_value", null)»;
                            return _domValue;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            return toDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "fromDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            ////System.out.println("«inputType.simpleName»#fromDomValue: "+$1);

                            if($1 == null) {
                            return null;
                            }
                            «returnType.resolvedName» _simpleValue = «deserializeValue(returnType, "$1", null)»;
                            «typeSpec.resolvedName» _value = new «typeSpec.resolvedName»(_simpleValue);
                            return _value;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "deserialize", Object) [
                    val body = '''
                        {
                            return fromDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
            ]

            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)
            sourceGenerator.outputGeneratedSource( ctCls )
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        } catch (Exception e) {
            LOG.error("Cannot compile DOM Codec for {}", inputType, e);
            val exception = new CodeGenerationException("Cannot compile Transformator for " + inputType);
            exception.addSuppressed(e);
            throw exception;
        }
    }

    private def dispatch Class<? extends BindingCodec<Map<QName, Object>, Object>> generateValueTransformer(
        Class<?> inputType, GeneratedTransferObject typeSpec, UnionTypeDefinition typeDef) {
        try {
            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            val ctCls = createClass(typeSpec.codecClassName) [
                val properties = typeSpec.allProperties;
                val getterToTypeDefinition = XtendHelper.getTypes(typeDef).toMap[type|type.QName.getterName];
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                if (inputType.isYangBindingAvailable) {
                    implementsType(BINDING_CODEC)
                    staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                    staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                    implementsType(BindingDeserializer.asCtClass)
                }
                method(Object, "toDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val ctSpec = inputType.asCtClass;
                    val body = '''
                        {
                            ////System.out.println("«inputType.simpleName»#toDomValue: "+$1);

                            if($1 == null) {
                            return null;
                            }
                            «typeSpec.resolvedName» _value = («typeSpec.resolvedName») $1;
                            «FOR property : properties.entrySet»
                            «IF property.key != "getValue"»
                                «property.value.resolvedName» «property.key» = («property.value.resolvedName») _value.«property.
                            key»();
                                if(«property.key» != null) {
                                    return «serializeValue(property.value, property.key,
                            getterToTypeDefinition.get(property.key))»;
                                }
                            «ENDIF»
                            «ENDFOR»

                            return null;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            return toDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "fromDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            ////System.out.println("«inputType.simpleName»#fromDomValue: "+$1);

                            if($1 == null) {
                            return null;
                            }
                            if($1 instanceof String) {
                            String _simpleValue = (String) $1;
                            return new «typeSpec.resolvedName»(_simpleValue.toCharArray());
                            }
                            return null;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "deserialize", Object) [
                    val body = '''
                        {
                            return fromDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
            ]

            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)
            sourceGenerator.outputGeneratedSource( ctCls )
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        } catch (Exception e) {
            LOG.error("Cannot compile DOM Codec for {}", inputType, e);
            val exception = new CodeGenerationException("Cannot compile Transformator for " + inputType);
            exception.addSuppressed(e);
            throw exception;
        }
    }

    private def dispatch Class<? extends BindingCodec<Map<QName, Object>, Object>> generateValueTransformer(
        Class<?> inputType, GeneratedTransferObject typeSpec, BitsTypeDefinition typeDef) {
        try {
            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            val ctCls = createClass(typeSpec.codecClassName) [
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                if (inputType.isYangBindingAvailable) {
                    implementsType(BINDING_CODEC)
                    staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                    staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                    implementsType(BindingDeserializer.asCtClass)
                }
                method(Object, "toDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val ctSpec = typeSpec.asCtClass;
                    val body = '''
                        {
                            ////System.out.println("«inputType.simpleName»#toDomValue: "+$1);

                            if($1 == null) {
                            return null;
                            }
                            «typeSpec.resolvedName» _encapsulatedValue = («typeSpec.resolvedName») $1;
                            «HashSet.resolvedName» _value = new «HashSet.resolvedName»();
                            //System.out.println("«inputType.simpleName»#toDomValue:Enc: "+_encapsulatedValue);

                            «FOR bit : typeDef.bits»
                            «val getter = bit.getterName()»
                            if(Boolean.TRUE.equals(_encapsulatedValue.«getter»())) {
                                _value.add("«bit.name»");
                            }
                            «ENDFOR»
                            «Set.resolvedName» _domValue =  «Collections.resolvedName».unmodifiableSet(_value);
                            //System.out.println("«inputType.simpleName»#toDomValue:DeEnc: "+_domValue);

                            return _domValue;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            return toDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "fromDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val sortedBits = IterableExtensions.sort(typeDef.bits, [o1, o2|o1.propertyName.compareTo(o2.propertyName)])
                    val body = '''
                        {
                            //System.out.println("«inputType.simpleName»#fromDomValue: "+$1);

                            if($1 == null) {
                            return null;
                            }
                            «Set.resolvedName» _domValue = («Set.resolvedName») $1;
                            «FOR bit : sortedBits»
                            Boolean «bit.propertyName» = Boolean.valueOf(_domValue.contains("«bit.name»"));
                            «ENDFOR»

                            return new «inputType.resolvedName»(«FOR bit : sortedBits SEPARATOR ","»«bit.propertyName»«ENDFOR»);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "deserialize", Object) [
                    val body = '''
                        {
                            return fromDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
            ]

            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)
            sourceGenerator.outputGeneratedSource( ctCls )
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret as Class<? extends BindingCodec<Map<QName,Object>, Object>>;
        } catch (Exception e) {
            LOG.error("Cannot compile DOM Codec for {}", inputType, e);
            val exception = new CodeGenerationException("Cannot compile Transformator for " + inputType);
            exception.addSuppressed(e);
            throw exception;
        }
    }

    def String getPropertyName(Bit bit) {
        '''_«BindingMapping.getPropertyName(bit.name)»'''
    }

    def String getterName(Bit bit) {

        val paramName = BindingMapping.getPropertyName(bit.name);
        return '''is«paramName.toFirstUpper»''';
    }

    def boolean isYangBindingAvailable(Class<?> class1) {
        try {
            val bindingCodecClass = class1.classLoader.loadClass(BINDING_CODEC.name);
            return bindingCodecClass !== null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private def createDummyImplementation(Class<?> object, GeneratedTransferObject typeSpec,
                                          SourceCodeGenerator sourceGenerator ) {
        LOG.trace("Generating Dummy DOM Codec for {} with {}", object, object.classLoader)
        return createClass(typeSpec.codecClassName) [
            if (object.isYangBindingAvailable) {
                implementsType(BINDING_CODEC)
                staticField(it, INSTANCE_IDENTIFIER_CODEC, BindingCodec, sourceGenerator)
                staticField(it, IDENTITYREF_CODEC, BindingCodec, sourceGenerator)
                implementsType(BindingDeserializer.asCtClass)
            }
            //implementsType(BindingDeserializer.asCtClass)
            method(Object, "toDomValue", Object) [
                modifiers = PUBLIC + FINAL + STATIC
                val body = '''
                    {
                        if($1 == null) {
                            return null;
                        }
                        return $1.toString();
                    }'''
                setBodyChecked( body, sourceGenerator )
            ]
            method(Object, "serialize", Object) [
                val body = '''
                    {
                        return toDomValue($1);
                    }
                '''
                setBodyChecked( body, sourceGenerator )
            ]
            method(Object, "fromDomValue", Object) [
                modifiers = PUBLIC + FINAL + STATIC
                val body = '''return null;'''
                setBodyChecked( body, sourceGenerator )
            ]
            method(Object, "deserialize", Object) [
                val body = '''
                    {
                        return fromDomValue($1);
                    }
                    '''
                setBodyChecked( body, sourceGenerator )
            ]
        ]
    }

    private def Type getValueReturnType(GeneratedTransferObject object) {
        for (prop : object.properties) {
            if (prop.name == "value") {
                return prop.returnType;
            }
        }
        if (object.superType != null) {
            return getValueReturnType(object.superType);
        }
        return null;
    }

    private def dispatch Class<?> generateValueTransformer(Class<?> inputType, Enumeration typeSpec, TypeDefinition<?> type) {
        var EnumerationType enumSchemaType
        if (type instanceof EnumerationType) {
            enumSchemaType = type as EnumerationType
        } else {
            val typeRef = new ReferencedTypeImpl(typeSpec.packageName, typeSpec.name);
            val schema = getSchemaNode(typeRef) as ExtendedType;
            enumSchemaType = schema.baseType as EnumerationType;
        }
        val enumSchema = enumSchemaType;
        try {
            val SourceCodeGenerator sourceGenerator = sourceCodeGeneratorFactory.getInstance( null );

            //log.info("Generating DOM Codec for {} with {}", inputType, inputType.classLoader)
            val ctCls = createClass(typeSpec.codecClassName) [
                //staticField(Map,"AUGMENTATION_SERIALIZERS");
                //implementsType(BINDING_CODEC)
                method(Object, "toDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            if($1 == null) {
                                return null;
                            }
                            «typeSpec.resolvedName» _value = («typeSpec.resolvedName») $1;
                            «FOR en : enumSchema.values»
                            if(«typeSpec.resolvedName».«BindingMapping.getClassName(en.name)».equals(_value)) {
                                return "«en.name»";
                            }
                            «ENDFOR»
                            return null;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "serialize", Object) [
                    val body = '''
                        {
                            return toDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "fromDomValue", Object) [
                    modifiers = PUBLIC + FINAL + STATIC
                    val body = '''
                        {
                            if($1 == null) {
                                return null;
                            }
                            String _value = (String) $1;
                            «FOR en : enumSchema.values»
                                if("«en.name»".equals(_value)) {
                                    return «typeSpec.resolvedName».«BindingMapping.getClassName(en.name)»;
                                }
                            «ENDFOR»
                            return null;
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
                method(Object, "deserialize", Object) [
                    val body = '''
                        {
                            return fromDomValue($1);
                        }
                    '''
                    setBodyChecked( body, sourceGenerator )
                ]
            ]

            val ret = ctCls.toClassImpl(inputType.classLoader, inputType.protectionDomain)
            sourceGenerator.outputGeneratedSource( ctCls )
            LOG.debug("DOM Codec for {} was generated {}", inputType, ret)
            return ret;
        } catch (CodeGenerationException e) {
            throw new CodeGenerationException("Cannot compile Transformator for " + inputType, e);
        } catch (Exception e) {
            LOG.error("Cannot compile DOM Codec for {}", inputType, e);
            val exception = new CodeGenerationException("Cannot compile Transformator for " + inputType);
            exception.addSuppressed(e);
            throw exception;
        }

    }

    def Class<?> toClassImpl(CtClass newClass, ClassLoader loader, ProtectionDomain domain) {
        val cls = newClass.toClass(loader, domain);
        if (classFileCapturePath !== null) {
            newClass.writeFile(classFileCapturePath.absolutePath);
        }
        listener?.onCodecCreated(cls);
        return cls;
    }

    def debugWriteClass(CtClass class1) {
        val path = class1.name.replace(".", "/") + ".class"

        val captureFile = new File(classFileCapturePath, path);
        captureFile.createNewFile

    }

    /**
     * Default catch all
     *
     **/
    private def dispatch CharSequence deserializeProperty(DataSchemaNode container, Type type, String propertyName) '''
        «type.resolvedName» «propertyName» = null;
    '''

    private def dispatch CharSequence deserializeProperty(DataSchemaNode container, GeneratedTypeBuilder type,
        String propertyName) {
        _deserializeProperty(container, type.toInstance, propertyName)
    }

    static def toSetter(String it) {

        if (startsWith("is")) {
            return "set" + substring(2);
        } else if (startsWith("get")) {
            return "set" + substring(3);
        }
        return "set" + it;
    }

    /*
    private def dispatch CharSequence deserializeProperty(DataSchemaNode container,GeneratedType type, String propertyName) '''
        «type.resolvedName» «propertyName» = value.«propertyName»();
        if(«propertyName» != null) {
            Object domValue = «type.serializer».toDomStatic(QNAME,«propertyName»);
            _childNodes.add(domValue);
        }
    '''
    */
    private def getBuilderName(GeneratedType type) '''«type.resolvedName»Builder'''

    private def staticQNameField(CtClass it, QName node, SourceCodeGenerator sourceGenerator) {
        val field = new CtField(ctQName, "QNAME", it);
        field.modifiers = PUBLIC + FINAL + STATIC;
        val code = '''«QName.asCtClass.name».cachedReference(«QName.asCtClass.name».create("«node.namespace»","«node.formattedRevision»","«node.localName»"))'''
        addField(field, code )

        sourceGenerator.appendField( field, code );
    }

    private def String serializeBodyImpl(GeneratedType type, DataNodeContainer nodeContainer) '''
        {
            «QName.name» _resultName = «QName.name».create($1,QNAME.getLocalName());
            java.util.List _childNodes = new java.util.ArrayList();
            «type.resolvedName» value = («type.resolvedName») $2;
            «transformDataContainerBody(type, type.allProperties, nodeContainer)»
            «serializeAugmentations»
            return ($r) java.util.Collections.singletonMap(_resultName,_childNodes);
        }
    '''

    private def dispatch String serializeBody(GeneratedType type, ListSchemaNode node) {
        return serializeBodyImpl(type, node);
    }

    private def dispatch String serializeBody(GeneratedType type, NotificationDefinition node) {
        return serializeBodyImpl(type, node);
    }

    private def dispatch String serializeBody(GeneratedType type, ContainerSchemaNode node) {
        return serializeBodyImpl(type, node);
    }

    private def dispatch String serializeBody(GeneratedType type, ChoiceCaseNode node) {
        return serializeBodyImpl(type, node);
    }

    private def dispatch String serializeBody(GeneratedType type, SchemaNode node) '''
        {
        «QName.name» _resultName = «QName.name».create($1,QNAME.getLocalName());
            java.util.List _childNodes = new java.util.ArrayList();
            «type.resolvedName» value = («type.resolvedName») $2;
            return ($r) java.util.Collections.singletonMap(_resultName,_childNodes);
        }
    '''

    private def transformDataContainerBody(Type type, Map<String, Type> properties, DataNodeContainer node) {
        val ret = '''
            «FOR child : node.childNodes»
                «val signature = properties.getFor(child)»
                «IF signature !== null»
                    ////System.out.println("«type.name»#«signature.key»" + value.«signature.key»());
                    «serializeProperty(child, signature.value, signature.key)»
                «ENDIF»
            «ENDFOR»
        '''
        return ret;
    }

    private static def serializeAugmentations() '''
        java.util.List _augmentations = (java.util.List) «AUGMENTATION_CODEC».serialize(value);
        if(_augmentations != null) {
            _childNodes.addAll(_augmentations);
        }
    '''

    private static def Entry<String, Type> getFor(Map<String, Type> map, DataSchemaNode node) {
        var sig = map.get(node.getterName);
        if (sig != null) {
            return new SimpleEntry(node.getterName, sig);
        }
        sig = map.get(node.booleanGetterName);
        if (sig != null) {
            return new SimpleEntry(node.booleanGetterName, map.get(node.booleanGetterName));
        }
        return null;
    }

    private static def String getBooleanGetterName(DataSchemaNode node) {
        return "is" + BindingMapping.getPropertyName(node.QName.localName).toFirstUpper;
    }

    private static def String getGetterName(DataSchemaNode node) {
        return "get" + BindingMapping.getPropertyName(node.QName.localName).toFirstUpper;
    }

    private static def String getGetterName(QName node) {
        return "get" + BindingMapping.getPropertyName(node.localName).toFirstUpper;
    }

    private def dispatch CharSequence serializeProperty(ListSchemaNode schema, ParameterizedType type,
        String propertyName) '''
        «type.resolvedName» «propertyName» = value.«propertyName»();
        ////System.out.println("«propertyName»:" + «propertyName»);
        if(«propertyName» != null) {
            java.util.Iterator _iterator = «propertyName».iterator();
            boolean _hasNext = _iterator.hasNext();
            while(_hasNext) {
                Object _listItem = _iterator.next();
                Object _domValue = «type.actualTypeArguments.get(0).serializer(schema).resolvedName».toDomStatic(_resultName,_listItem);
                _childNodes.add(_domValue);
                _hasNext = _iterator.hasNext();
            }
        }
    '''

    private def dispatch CharSequence serializeProperty(LeafSchemaNode schema, Type type, String propertyName) '''
        «type.resolvedName» «propertyName» = value.«propertyName»();

        if(«propertyName» != null) {
            «QName.name» _qname = «QName.name».create(_resultName,"«schema.QName.localName»");
            Object _propValue = «serializeValue(type, propertyName, schema.type)»;
            if(_propValue != null) {
                Object _domValue = java.util.Collections.singletonMap(_qname,_propValue);
                _childNodes.add(_domValue);
            }
        }
    '''

    private def dispatch serializeValue(GeneratedTransferObject type, String parameter, TypeDefinition<?> typeDefinition) {
        '''«type.valueSerializer(typeDefinition).resolvedName».toDomValue(«parameter»)'''
    }

    private def dispatch serializeValue(Enumeration type, String parameter, TypeDefinition<?> typeDefinition) {
        '''«type.valueSerializer(typeDefinition).resolvedName».toDomValue(«parameter»)'''
    }

    private def dispatch serializeValue(Type type, String parameter, EmptyTypeDefinition typeDefinition) {
        '''(«parameter».booleanValue() ? "" : null)'''
    }

    private def dispatch serializeValue(Type signature, String property, TypeDefinition<?> typeDefinition) {
        serializeValue(signature,property)
    }

    private def dispatch serializeValue(Type signature, String property, Void typeDefinition) {
        serializeValue(signature,property)
    }

    private def dispatch serializeValue(Type signature, String property) {
        if (INSTANCE_IDENTIFIER == signature) {
            return '''«INSTANCE_IDENTIFIER_CODEC».serialize(«property»)'''
        } else if (CLASS_TYPE.equals(signature)) {
            return '''(«QName.resolvedName») «IDENTITYREF_CODEC».serialize(«property»)'''
        }
        if ("char[]" == signature.name) {
            return '''new String(«property»)''';
        }
        return '''«property»''';
    }

    private def dispatch CharSequence serializeProperty(LeafListSchemaNode schema, ParameterizedType type,
        String propertyName) '''
        «type.resolvedName» «propertyName» = value.«propertyName»();
        if(«propertyName» != null) {
            «QName.name» _qname = «QName.name».create(_resultName,"«schema.QName.localName»");
            java.util.Iterator _iterator = «propertyName».iterator();
            boolean _hasNext = _iterator.hasNext();
            while(_hasNext) {
                Object _listItem = _iterator.next();
                Object _propValue = «serializeValue(type.actualTypeArguments.get(0), "_listItem", schema.type)»;
                Object _domValue = java.util.Collections.singletonMap(_qname,_propValue);
                _childNodes.add(_domValue);
                _hasNext = _iterator.hasNext();
            }
        }
    '''

    private def dispatch CharSequence serializeProperty(ChoiceNode container, GeneratedType type,
        String propertyName) '''
        «type.resolvedName» «propertyName» = value.«propertyName»();
        if(«propertyName» != null) {
            java.util.List domValue = «type.serializer(container).resolvedName».toDomStatic(_resultName,«propertyName»);
            _childNodes.addAll(domValue);
        }
    '''

    /**
     * Default catch all
     *
     **/
    private def dispatch CharSequence serializeProperty(DataSchemaNode container, Type type, String propertyName) '''
        «type.resolvedName» «propertyName» = value.«propertyName»();
        if(«propertyName» != null) {
            Object domValue = «propertyName»;
            _childNodes.add(domValue);
        }
    '''

    private def dispatch CharSequence serializeProperty(DataSchemaNode container, GeneratedTypeBuilder type,
        String propertyName) {
        serializeProperty(container, type.toInstance, propertyName)
    }

    private def dispatch CharSequence serializeProperty(DataSchemaNode container, GeneratedType type,
        String propertyName) '''
        «type.resolvedName» «propertyName» = value.«propertyName»();
        if(«propertyName» != null) {
            Object domValue = «type.serializer(container).resolvedName».toDomStatic(_resultName,«propertyName»);
            _childNodes.add(domValue);
        }
    '''

    private def codecClassName(GeneratedType typeSpec) {
        return '''«typeSpec.resolvedName»$Broker$Codec$DOM'''
    }

    private def codecClassName(Class<?> typeSpec) {
        return '''«typeSpec.name»$Broker$Codec$DOM'''
    }

    private def HashMap<String, Type> getAllProperties(GeneratedType type) {
        val ret = new HashMap<String, Type>();
        type.collectAllProperties(ret);
        return ret;
    }

    private def dispatch void collectAllProperties(GeneratedType type, Map<String, Type> set) {
        for (definition : type.methodDefinitions) {
            set.put(definition.name, definition.returnType);
        }
        for (property : type.properties) {
            set.put(property.getterName, property.returnType);
        }
        for (parent : type.implements) {
            parent.collectAllProperties(set);
        }
    }

    def String getGetterName(GeneratedProperty property) {
        return "get" + property.name.toFirstUpper
    }

    private def dispatch void collectAllProperties(Type type, Map<String, Type> set) {
        // NOOP for generic type.
    }

    def String getResolvedName(Type type) {
        return type.asCtClass.name;
    }

    def String getResolvedName(Class<?> type) {
        return type.asCtClass.name;
    }

    def CtClass asCtClass(Type type) {
        val cls = loadClass(type.fullyQualifiedName)
        return cls.asCtClass;
    }

    private def dispatch processException(Class<?> inputType, CodeGenerationException e) {
        LOG.error("Cannot compile DOM Codec for {}. One of it's prerequisites was not generated.", inputType);
        throw e;
    }

    private def dispatch processException(Class<?> inputType, Exception e) {
        LOG.error("Cannot compile DOM Codec for {}", inputType, e);
        val exception = new CodeGenerationException("Cannot compile Transformator for " + inputType, e);
        throw exception;
    }

    private def setBodyChecked(CtMethod method, String body, SourceCodeGenerator sourceGenerator ) {
        try {
            method.setBody(body);

            sourceGenerator.appendMethod( method, body );
        } catch (CannotCompileException e) {
            LOG.error("Cannot compile method: {}#{} {}, Reason: {} Body: {}", method.declaringClass, method.name,
                method.signature, e.message, body)
            throw e;
        }
    }
}

@Data
class PropertyPair {

    String getterName;

    Type type;

    @Property
    Type returnType;
    @Property
    SchemaNode schemaNode;
}
