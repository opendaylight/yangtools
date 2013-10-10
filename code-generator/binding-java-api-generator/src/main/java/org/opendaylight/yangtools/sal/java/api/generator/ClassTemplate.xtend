package org.opendaylight.yangtools.sal.java.api.generator

import java.util.List
import org.opendaylight.yangtools.binding.generator.util.TypeConstants
import org.opendaylight.yangtools.sal.binding.model.api.Constant
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import java.util.ArrayList
import java.util.Collections

/**
 * Template for generating JAVA class. 
 */
class ClassTemplate extends BaseTemplate {

    protected val List<GeneratedProperty> properties
    protected val List<GeneratedProperty> finalProperties
    protected val List<GeneratedProperty> parentProperties
    protected val Iterable<GeneratedProperty> allProperties;
    
    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    protected val List<Enumeration> enums
    
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    protected val List<Constant> consts
    
    /**
     * List of generated types which are enclosed inside <code>genType</code>
     */
    protected val List<GeneratedType> enclosedGeneratedTypes;
    
    
    protected val GeneratedTransferObject genTO;

    /**
     * Creates instance of this class with concrete <code>genType</code>.
     * 
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    new(GeneratedTransferObject genType) {
        super(genType)
        this.genTO = genType
        this.properties = genType.properties
        this.finalProperties = GeneratorUtil.resolveReadOnlyPropertiesFromTO(genTO.properties)
        this.parentProperties = GeneratorUtil.getPropertiesOfAllParents(genTO)

        var List<GeneratedProperty> sorted = new ArrayList<GeneratedProperty>();
        sorted.addAll(properties);
        sorted.addAll(parentProperties);
        Collections.sort(sorted, new PropertyComparator());
        
        this.allProperties = sorted
        this.enums = genType.enumerations
        this.consts = genType.constantDefinitions
        this.enclosedGeneratedTypes = genType.enclosedTypes
    }
    

    
    
    
    /**
     * Generates JAVA class source code (class body only).
     * 
     * @return string with JAVA class body source code
     */
    def CharSequence generateAsInnerClass() {
        return generateBody(true)
    }
    

    
    override protected body() {
        generateBody(false);
    }

    /**
     * Template method which generates class body.
     * 
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class source code in JAVA format
     */
    def protected generateBody(boolean isInnerClass) '''
        «type.comment.asJavadoc»
        «generateClassDeclaration(isInnerClass)» {
        	«innerClassesDeclarations»
            «enumDeclarations»
            «constantsDeclarations»
            «generateFields»
            «constructors»
            «FOR field : properties SEPARATOR "\n"»
                «field.getterMethod»
                «IF !field.readOnly»
                    «field.setterMethod»
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
    def protected innerClassesDeclarations() '''
        «IF !enclosedGeneratedTypes.empty»
            «FOR innerClass : enclosedGeneratedTypes SEPARATOR "\n"»
                «IF (innerClass instanceof GeneratedTransferObject)»
                    «val classTemplate = new ClassTemplate(innerClass as GeneratedTransferObject)»
                    «classTemplate.generateAsInnerClass»
                    
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''
    
    
    def protected constructors() '''
    «allValuesConstructor»
    «IF !allProperties.empty»
    «copyConstructor»
    «ENDIF»
    «IF properties.empty && !parentProperties.empty »
        «parentConstructor»
    «ENDIF»
    '''
    
    def protected allValuesConstructor() '''
    public «type.name»(«allProperties.asArgumentsDeclaration») {
        «IF false == parentProperties.empty»
            super(«parentProperties.asArguments»);
        «ENDIF»
        «FOR p : properties» 
            this.«p.fieldName» = «p.fieldName»;
        «ENDFOR»
    }
    '''
    
    
    def protected copyConstructor() '''
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public «type.name»(«type.name» source) {
        «IF false == parentProperties.empty»
            super(source);
        «ENDIF»
        «FOR p : properties» 
            this.«p.fieldName» = source.«p.fieldName»;
        «ENDFOR»
    }
    '''
    
    def protected parentConstructor() '''
    /**
     * Creates a new instance from «genTO.superType.importedName»
     *
     * @param source Source object
     */
    public «type.name»(«genTO.superType.importedName» source) {
            super(source);
    }
    '''
    

    
    /**
     * Template method which generates JAVA class declaration.
     * 
     * @param isInnerClass boolean value which specify if generated class is|isn't inner
     * @return string with class declaration in JAVA format
     */
    def protected generateClassDeclaration(boolean isInnerClass) '''
        public«
        IF (isInnerClass)»«
            " static final "»«
        ELSEIF (type.abstract)»«
            " abstract "»«
        ELSE»«
            " "»«
        ENDIF»class «type.name»«
        IF (genTO.superType != null)»«
            " extends "»«genTO.superType.importedName»«
        ENDIF»
        «IF (!type.implements.empty)»«
            " implements "»«
            FOR type : type.implements SEPARATOR ", "»«
                type.importedName»«
            ENDFOR»«
        ENDIF
    »'''
    
    /**
     * Template method which generates JAVA enum type.
     * 
     * @return string with inner enum source code in JAVA format
     */
    def protected enumDeclarations() '''
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
    def protected constantsDeclarations() '''
        «IF !consts.empty»
            «FOR c : consts»
                «IF c.name == TypeConstants.PATTERN_CONSTANT_NAME»
                    «val cValue = c.value»
                    «IF cValue instanceof List<?>»
                        «val cValues = cValue as List<?>»
                        private static final List<Pattern> «Constants.MEMBER_PATTERN_LIST» = new ArrayList<Pattern>();
                        public static final List<String> «TypeConstants.PATTERN_CONSTANT_NAME» = java.util.Arrays.asList(«
                        FOR v : cValues SEPARATOR ", "»«
                            IF v instanceof String»"«
                                v as String»"«
                            ENDIF»«
                        ENDFOR»);

                        «generateStaticInicializationBlock»
                    «ENDIF»
                «ELSE»
                    public static final «c.type.importedName» «c.name» = «c.value»;
                «ENDIF»
            «ENDFOR»
        «ENDIF»
    '''

    /**
     * Template method which generates JAVA static initialization block.
     *
     * @return string with static initialization block in JAVA format
     */
    def protected generateStaticInicializationBlock() '''
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
    def protected generateFields() '''
        «IF !properties.empty»
            «FOR f : properties»
                «IF f.readOnly»final«ENDIF» private «f.returnType.importedName» «f.fieldName»;
            «ENDFOR»
        «ENDIF»
    '''


    /**
     * Template method which generates the method <code>hashCode()</code>.
     *
     * @return string with the <code>hashCode()</code> method definition in JAVA format
     */
    def protected generateHashCode() '''
        «IF !genTO.hashCodeIdentifiers.empty»
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                «FOR property : genTO.hashCodeIdentifiers»
                    «IF property.returnType.name.contains("[")»
                    result = prime * result + ((«property.fieldName» == null) ? 0 : java.util.Arrays.hashCode(«property.fieldName»));
                    «ELSE»
                    result = prime * result + ((«property.fieldName» == null) ? 0 : «property.fieldName».hashCode());
                    «ENDIF»
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
    def protected generateEquals() '''
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
                «type.name» other = («type.name») obj;
                «FOR property : genTO.equalsIdentifiers»
                    «val fieldName = property.fieldName»
                    if («fieldName» == null) {
                        if (other.«fieldName» != null) {
                            return false;
                        }
                    «IF property.returnType.name.contains("[")»
                    } else if(!java.util.Arrays.equals(«fieldName», other.«fieldName»)) {
                    «ELSE»
                    } else if(!«fieldName».equals(other.«fieldName»)) {
                    «ENDIF»
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
    def protected generateToString() '''
        «IF !genTO.toStringIdentifiers.empty»
            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                «val properties = genTO.toStringIdentifiers»
                builder.append("«type.name» [«properties.get(0).fieldName»=");
                «IF properties.get(0).returnType.name.contains("[")»
                    builder.append(java.util.Arrays.toString(«properties.get(0).fieldName»));
                «ELSE»
                    builder.append(«properties.get(0).fieldName»);
                «ENDIF»
                «FOR i : 1..<genTO.toStringIdentifiers.size»
                    builder.append(", «properties.get(i).fieldName»=");
                    «IF properties.get(i).returnType.name.contains("[")»
                        builder.append(java.util.Arrays.toString(«properties.get(i).fieldName»));
                    «ELSE»
                        builder.append(«properties.get(i).fieldName»);
                    «ENDIF»
                «ENDFOR»
                builder.append("]");
                return builder.toString();
            }
        «ENDIF»
    '''
    
}
