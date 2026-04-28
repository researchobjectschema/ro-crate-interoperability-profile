package ch.eth.sis.rocrate.schemaorg;

import ch.eth.sis.rocrate.facade.IPropertyType;
import ch.eth.sis.rocrate.facade.IType;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SchemaOrgPropertyResolver
{

    public static Set<IPropertyType> findSchemaOrgProperties(
            SchemaOrgInformation schemaOrgInformation, IType type)
    {
        if (!schemaOrgInformation.getTypeToProperties().containsKey(type) && type.getSubClassOf()
                .stream()
                .noneMatch(x -> schemaOrgInformation.getIdentifiersToDataTypes()
                        .containsKey(schemaOrgify(x))))
        {
            return Set.of();
        }

        Set<IPropertyType> res = new LinkedHashSet<>();
        Optional.ofNullable(schemaOrgInformation.getTypeToProperties().get(type))
                .ifPresent(res::addAll);
        type.getSubClassOf().stream().map(x -> schemaOrgify(x))
                .map(x -> schemaOrgInformation.getIdentifiersToDataTypes().get(x))
                .filter(Objects::nonNull)
                .forEach(x -> {

                    res.addAll(schemaOrgInformation.getTypeToProperties().get(x));
                    schemaOrgInformation.getSuperTypes().get(x).stream().forEach(y ->
                            res.addAll(schemaOrgInformation.getTypeToProperties().get(y))
                    );

                });
        return res;
    }

    public static String schemaOrgify(String a)
    {
        if (a.contains(":"))
        {
            return a;
        }
        return "schema:" + a;

    }

}
