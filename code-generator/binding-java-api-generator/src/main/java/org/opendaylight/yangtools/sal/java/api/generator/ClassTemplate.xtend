package org.opendaylight.yangtools.sal.java.api.generator

import java.util.List
import java.util.Map
import org.opendaylight.yangtools.binding.generator.util.TypeConstants
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType

/**
 * Template for generating JAVA class. 
 */
class ClassTemplate {
    
    /**
     * Generated transfer object for which class JAVA file is generated
     */
    val GeneratedTransferObject genTO
    
    /**
     * Map of imports for this <code>genTO</code>.
     */
    val Map<String, String> imports
    
    /**
     * List of generated property instances which represents class attributes.
     */
    val List<GeneratedProperty> fields
    
    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    val List<Enumeration> enums
    
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    val List<Constant> consts
    
    /**
     * List of generated types which are enclosed inside <code>genType</code>
     */
    val List<GeneratedType> enclosedGeneratedTypes;
        
    /**
     * Creates instance of this class with concrete <code>genTO</code>.
     * 
     * @param genTO generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genTO) {
        if (genTO == null) {
            throw new IllegalArgumentException("Generated transfer object reference cannot be NULL!")
        }
        
        this.genTO = genTO
        this.imports = GeneratorUtil.createImports(genTO)
        this.fields = genTO.properties
        this.enums = genTO.enumerations
        this.consts = genTO.constantDefinitions
        this.enclosedGeneratedTypes = genTO.enclosedTypes
    }
    
    /**
     * Generates JAVA class source code (package name + class body).
     * 
     * @return string with JAVA class source code
     */
    def String generate() {
        val body = generateBody(false)
        val pkgAndImports = generatePkgAndImports
        return pkgAndImports.toString + body.toString
    }
    
    /**
     * Generates JAVA class source code (class body only).
     * 
     * @return string with JAVA class body source code
     */
    def generateAsInnerClass() {
        return generateBody(true)
    }
    
    /**
     * Template method which generates class body.
     * 
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class source code in JAVA format
     */
    def private generateBody(boolean isInnerClass) '''
        «genTO.comment.generateComment»
        «generateClassDeclaration(isInnerClass)» {
        	«generateInnerClasses»

            «generateEnums»
        
            «generateConstants»
        
            «generateFields»
        
            «generateConstructor»
        
            «FOR field : fields SEPARATOR "\n"»
                «field.generateGetter»
                «IF !field.readOnly»
                
                    «field.generateSetter»
                «ENDIF»
            «ENDFOR»
        
            «generateHashCode»
        
            «generateEquals»
        
            «generateToString»
        
        }
    '''
    
    
    /**
     * Template method which generates inner classes inside this interface.
     * 
     * @return string with the source code for inner classes in JAVA format
     */
    def private generateInnerClasses() '''
        «IF !enclosedGeneratedTypes.empty»
            «FOR innerClass : enclosedGeneratedTypes SEPARATOR "\n"»
                «IF (innerClass instanceof GeneratedTransferObject)»
                    «val classTemplate = new ClassTemplate(innerClass as GeneratedTransferObject)»
                    «classTemplate.generateAsInnerClass»
                    
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''
        
    /**
     * Template method which generates JAVA comments.
     * 
     * @param string with the comment for whole JAVA class
     * @return string with comment in JAVA format
     */
    def private generateComment(String comment) '''
        «IF comment != null && !comment.empty»
            /*
            «comment»
            */
        «ENDIF»
    '''
    
    /**
     * Template method which generates JAVA class declaration.
     * 
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class declaration in JAVA format
     */
    def private generateClassDeclaration(boolean isInnerClass) '''
        public«
        IF (isInnerClass)»«
            " static final "»«
        ELSEIF (genTO.abstract)»«
            " abstract "»«
        ELSE»«
            " "»«
        ENDIF»class «genTO.name»«
        IF (genTO.extends != null)»«
            " extends "»«genTO.extends.resolveName»«
        ENDIF»«
        IF (!genTO.implements.empty)»«
            " implements "»«
            FOR type : genTO.implements SEPARATOR ", "»«
                type.resolveName»«
            ENDFOR»«
        ENDIF
    »'''
    
    /**
     * Template method which generates JAVA enum type.
     * 
     * @return string with inner enum source code in JAVA format
     */
    def private generateEnums() '''
        «IF !enums.empty»
            «FOR e : enums SEPARATOR "\n"»
                «val enumTemplate = new EnumTemplate(e)»
                «enumTemplate.generateAsInnerClass»
            «ENDFOR»
        «ENDIF»
    '''
    
    /**
     * Template method wich generates JAVA constants.
     * 
     * @return string with constants in JAVA format 
     */
    def private generateConstants() '''
        «IF !consts.empty»
            «FOR c : consts»
                «IF c.name == TypeConstants.PATTERN_CONSTANT_NAME»
                    «val cValue = c.value»
                    «IF cValue instanceof List<?>»
                        «val cValues = cValue as List<?>»
                        private static final List<Pattern> «Constants.MEMBER_PATTERN_LIST» = new ArrayList<Pattern>();
                        public static final List<String> «TypeConstants.PATTERN_CONSTANT_NAME» = Arrays.asList(«
                        FOR v : cValues SEPARATOR ", "»«
                            IF v instanceof String»"«
                                v as String»"«
                            ENDIF»«
                        ENDFOR»);
                        
                        «generateStaticInicializationBlock»
                    «ENDIF»
                «ELSE»
                    public static final «c.type.resolveName» «c.name» = «c.value»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''
    
    /**
     * Template method which generates JAVA static initialization block.
     * 
     * @return string with static initialization block in JAVA format
     */
    def private generateStaticInicializationBlock() '''
        static {
            for (String regEx : «TypeConstants.PATTERN_CONSTANT_NAME») {
                «Constants.MEMBER_PATTERN_LIST».add(Pattern.compile(regEx));
            }
        }
    '''
    
    /**
     * Template method which generates JAVA class attributes.
     * 
     * @return string with the class attributes in JAVA format
     */
    def private generateFields() '''
        «IF !fields.empty»
            «FOR f : fields»
                private «f.returnType.resolveName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
    '''
    
    /**
     * Template method which generates JAVA constructor(s).
     * 
     * @return string with the class constructor(s) in JAVA format
     */
    def private generateConstructor() '''
        «val genTOTopParent = GeneratorUtil.getTopParrentTransportObject(genTO)»
        «val properties = GeneratorUtil.resolveReadOnlyPropertiesFromTO(genTO.properties)»
        «val propertiesAllParents = GeneratorUtil.getPropertiesOfAllParents(genTO)»
        «IF !genTO.unionType»
«««            create constructor for every parent property
            «IF genTOTopParent != genTO && genTOTopParent.unionType»
                «FOR parentProperty : propertiesAllParents SEPARATOR "\n"»
                    «val parentPropertyAndProperties = properties + #[parentProperty]»
                    «if (genTO.abstract) "protected" else "public"» «genTO.name»(«parentPropertyAndProperties.generateParameters») {
                        super(«#[parentProperty].generateParameterNames»);
                        «FOR property : properties»
                            this.«property.fieldName» = «property.name»;
                        «ENDFOR»
                    }
                «ENDFOR»
«««            create one constructor
            «ELSE»
                «val propertiesAll = propertiesAllParents + properties»
                «if (genTO.abstract) "protected" else "public"» «genTO.name»(«propertiesAll.generateParameters») {
                    super(«propertiesAllParents.generateParameterNames()»);
                    «FOR property : properties»
                        this.«property.fieldName» = «property.fieldName»;
                    «ENDFOR»
                }
            «ENDIF»
«««        create constructor for every property
        «ELSE»
            «FOR property : properties SEPARATOR "\n"»
                «val propertyAndTopParentProperties = propertiesAllParents + #[property]»
                «if (genTO.abstract) "protected" else "public"» «genTO.name»(«propertyAndTopParentProperties.generateParameters») {
                    super(«propertiesAllParents.generateParameterNames()»);
                    this.«property.fieldName» = «property.fieldName»;
                }
            «ENDFOR»
        «ENDIF»
    '''
    
    /**
     * Template method which generates the getter method for <code>field</code>
     * 
     * @param field 
     * generated property with data about field which is generated as the getter method
     * @return string with the getter method source code in JAVA format 
     */     
    def private generateGetter(GeneratedProperty field) {
        val prefix = if(field.returnType.equals(Types.typeForClass(Boolean))) "is" else "get"
    '''
        public «field.returnType.resolveName» «prefix»«field.name.toFirstUpper»() {
            return «field.fieldName»;

        }
    '''
    }
    /**
     * Template method which generates the setter method for <code>field</code>
     * 
     * @param field 
     * generated property with data about field which is generated as the setter method
     * @return string with the setter method source code in JAVA format 
     */
     def private generateSetter(GeneratedProperty field) '''
        «val type = field.returnType.resolveName»
        public void set«field.name.toFirstUpper»(«type» «field.fieldName») {
            this.«field.fieldName» = «field.fieldName»;
        }
    '''
    
    /**
     * Template method which generates method parameters with their types from <code>parameters</code>.
     * 
     * @param parameters
     * group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    def private generateParameters(Iterable<GeneratedProperty> parameters) '''«
        IF !parameters.empty»«
            FOR parameter : parameters SEPARATOR ", "»«
                parameter.returnType.resolveName» «parameter.fieldName»«
            ENDFOR»«
        ENDIF
    »'''
    
    /**
     * Template method which generates sequence of the names of the class attributes from <code>parameters</code>.
     * 
     * @param parameters 
     * group of generated property instances which are transformed to the sequence of parameter names
     * @return string with the list of the parameter names of the <code>parameters</code> 
     */
    def private generateParameterNames(Iterable<GeneratedProperty> parameters) '''«
        IF !parameters.empty»«
            FOR parameter : parameters SEPARATOR ", "»«
                parameter.fieldName»«
            ENDFOR»«
        ENDIF
    »'''
    
    /**
     * Template method which generates the method <code>hashCode()</code>.
     * 
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def private generateHashCode() '''
        «IF !genTO.hashCodeIdentifiers.empty»
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                «FOR property : genTO.hashCodeIdentifiers»
                    result = prime * result + ((«property.fieldName» == null) ? 0 : «property.fieldName».hashCode());
                «ENDFOR»
                return result;
            }
        «ENDIF»
    '''
    
    /**
     * Template method which generates the method <code>equals()</code>.
     * 
     * @return string with the <code>equals()</code> method definition in JAVA format     
     */
    def private generateEquals() '''
        «IF !genTO.equalsIdentifiers.empty»
            @Override
            public boolean equals(java.lang.Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                «genTO.name» other = («genTO.name») obj;
                «FOR property : genTO.equalsIdentifiers»
                    «val fieldName = property.fieldName»
                    if («fieldName» == null) {
                        if (other.«fieldName» != null) {
                            return false;
                        }
                    } else if(!«fieldName».equals(other.«fieldName»)) {
                        return false;
                    }
                «ENDFOR»
                return true;
            }
        «ENDIF»
    '''
    
    /**
     * Template method which generates the method <code>toString()</code>.
     * 
     * @return string with the <code>toString()</code> method definition in JAVA format     
     */
    def private generateToString() '''
        «IF !genTO.toStringIdentifiers.empty»
            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                «val properties = genTO.toStringIdentifiers»
                builder.append("«genTO.name» [«properties.get(0).fieldName»=");
                builder.append(«properties.get(0).fieldName»);
                «FOR i : 1..<genTO.toStringIdentifiers.size»
                    builder.append(", «properties.get(i).fieldName»=");
                    builder.append(«properties.get(i).fieldName»);
                «ENDFOR»
                builder.append("]");
                return builder.toString();
            }
        «ENDIF»
    '''
    
    /**
     * Template method which generate package name line and import lines.
     * 
     * @result string with package and import lines in JAVA format
     */
    def private generatePkgAndImports() '''
        package «genTO.packageName»;
        
        
        «IF !imports.empty»
            «FOR entry : imports.entrySet»
                import «entry.value».«entry.key»;
            «ENDFOR»
        «ENDIF»
        
    '''

    /**
     * Adds package to imports if it is necessary and returns necessary type name (with or without package name)
     * 
	 * @param type JAVA <code>Type</code> 
     * @return string with the type name (with or without package name)
     */    
    def private resolveName(Type type) {
        GeneratorUtil.putTypeIntoImports(genTO, type, imports);
        GeneratorUtil.getExplicitType(genTO, type, imports)
    }
    
    def private fieldName(GeneratedProperty property) {
        '''_«property.name»'''
    }
}
