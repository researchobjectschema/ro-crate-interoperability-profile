package ch.eth.sis.rocrate.schemaorg;

import ch.eth.sis.rocrate.facade.IPropertyType;
import ch.eth.sis.rocrate.facade.IType;

import java.util.List;
import java.util.Map;

public class SchemaOrgInformation
{
    private final Map<String, IType> identifiersToDataTypes;

    private final Map<String, IPropertyType> identifiersToPropertyTypes;

    private final Map<IType, List<IPropertyType>> typeToProperties;

    private final Map<IType, List<IType>> superTypes;

    public SchemaOrgInformation(Map<String, IType> identifiersToDataTypes,
            Map<String, IPropertyType> identifiersToPropertyTypes,
            Map<IType, List<IPropertyType>> typeToProperties, Map<IType, List<IType>> superTypes)
    {
        this.identifiersToDataTypes = identifiersToDataTypes;
        this.identifiersToPropertyTypes = identifiersToPropertyTypes;
        this.typeToProperties = typeToProperties;
        this.superTypes = superTypes;
    }

    public Map<String, IType> getIdentifiersToDataTypes()
    {
        return identifiersToDataTypes;
    }

    public Map<String, IPropertyType> getIdentifiersToPropertyTypes()
    {
        return identifiersToPropertyTypes;
    }

    public Map<IType, List<IPropertyType>> getTypeToProperties()
    {
        return typeToProperties;
    }

    public Map<IType, List<IType>> getSuperTypes()
    {
        return superTypes;
    }
}
