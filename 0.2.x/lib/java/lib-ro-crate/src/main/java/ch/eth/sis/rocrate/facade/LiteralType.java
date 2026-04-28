package ch.eth.sis.rocrate.facade;

/**
 * List of primitives as supported by xsd
 * https://www.ibm.com/docs/en/jfsm/1.1.2.1?topic=queries-xsd-data-types
 */
public enum LiteralType implements IDataType
{

    ANY_URI("xsd:anyURI"),
    BOOLEAN("xsd:boolean"),
    INTEGER("xsd:integer"),
    DOUBLE("xsd:double"),

    DECIMAL("xsd:decimal"),
    FLOAT("xsd:float"),
    DATETIME("xsd:dateTime"),
    STRING("xsd:string"),

    XML_LITERAL("rdf:XMLLiteral");

    final String typeName;

    LiteralType(String typeName)
    {
        this.typeName = typeName;
    }

    @Override
    public String getTypeName()
    {
        return typeName;
    }

    public static boolean isLiteralType(String typeName)
    {
        for (LiteralType literalType : LiteralType.values())
        {
            if (typeName.equals(literalType.getTypeName()))
            {
                return true;
            }
        }
        return false;
    }

    public static LiteralType getByTypeName(String typeName)
    {
        for (LiteralType literalType : LiteralType.values())
        {
            if (typeName.equals(literalType.getTypeName()))
            {
                return literalType;
            }
        }
        throw new IllegalArgumentException("Unknown literal type: " + typeName);

    }

}
