package ch.eth.sis.rocrate.schemaorg;

import ch.eth.sis.rocrate.facade.IDataType;
import ch.eth.sis.rocrate.facade.LiteralType;

import java.util.Arrays;

public enum SchemaOrgDataTypeMapping
{
    BOOLEAN("https://schema.org/Boolean", LiteralType.BOOLEAN),
    DATETIME("https://schema.org/DateTime", LiteralType.DATETIME),
    TEXT("https://schema.org/Text", LiteralType.STRING),
    NUMBER("https://schema.org/Number", LiteralType.DECIMAL),
    TIME("https://schema.org/Time", LiteralType.STRING),
    DATE("https://schema.org/Date", LiteralType.STRING),
    FLOAT("https://schema.org/Float", LiteralType.DOUBLE),
    INTEGER("https://schema.org/Integer", LiteralType.INTEGER),
    URL("https://schema.org/URL", LiteralType.ANY_URI);

    private final IDataType dataType;

    private final String identifier;

    SchemaOrgDataTypeMapping(String identifier, IDataType dataType)
    {
        this.dataType = dataType;
        this.identifier = identifier;
    }

    public static SchemaOrgDataTypeMapping getByType(String identifier)
    {
        return
                Arrays.stream(SchemaOrgDataTypeMapping.values())
                        .filter(x -> x.identifier.equals(identifier)).findFirst().orElse(null);

    }

    public IDataType getDataType()
    {
        return dataType;
    }

    public String getIdentifier()
    {
        return identifier;
    }
}
