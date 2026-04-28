package ch.eth.sis.rocrate.facade;

public interface IDataType
{
    String getTypeName();

    static IDataType getArray(LiteralType literalType)
    { //TODO static IDataType getArray(LiteralType literalType)
        throw new UnsupportedOperationException("Feature incomplete. Contact assistance.");
    }

    static IDataType getEnumerationf(String... values)
    { //TODO static IDataType getEnumerationf(String... values)
        throw new UnsupportedOperationException("Feature incomplete. Contact assistance.");
    }

    static IDataType getCustomType(LiteralType basicType, String pattern)
    { //TODO static IDataType getCustomType(LiteralType basicType, String pattern)
        throw new UnsupportedOperationException("Feature incomplete. Contact assistance.");
    }
}
