package ch.eth.sis.rocrate;

import ch.eth.sis.rocrate.facade.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SchemaFacade implements ISchemaFacade
{

    private final static String RDFS_CLASS = "rdfs:Class";

    private final static String RDF_PROPERTY = "rdf:Property";

    public static final String EQUIVALENT_CLASS = "owl:equivalentClass";

    public static final String EQUIVALENT_CONCEPT = "owl:equivalentProperty";

    public static final String TYPE_RESTRICTION = "owl:restriction";


    String rangeIdentifier = "schema:rangeIncludes";

    String domainIdentifier = "schema:domainIncludes";

    public static final String OWL_MIN_CARDINALITY = "owl:minCardinality";

    public static final String OWL_MAX_CARDINALITY = "owl:maxCardinality";

    public static final String OWL_RESTRICTION = "owl:restriction";

    public static final String ON_PROPERTY = "owl:onProperty";


    public static final String RDFS_LABEL = "rdfs:label";

    public static final String RDFS_COMMENT = "rdfs:comment";



    Pattern p;

    String localPrefix = ":";

    private Map<String, IType> types;

    private Map<String, IPropertyType> propertyTypes;

    private Map<String, IMetadataEntry> metadataEntries;

    private final RoCrate crate;

    @Override
    public RoCrate getCrate()
    {
        return crate;
    }

    public SchemaFacade(String name, String description, String dateString,
            String licenseIdentifier, Map<String, String> context)
    {
        RoCrate.RoCrateBuilder roCrateBuilder =
                new RoCrate.RoCrateBuilder(name, description, dateString,
                        licenseIdentifier);
        roCrateBuilder.addValuePairToContext("schema",
                "https://www.w3.org/TR/rdf-schema");
        roCrateBuilder.addValuePairToContext("owl",
                "https://www.w3.org/TR/owl-ref");
        for (Map.Entry<String, String> keyVal : context.entrySet()
        )
        {
            roCrateBuilder.addValuePairToContext(keyVal.getKey(), keyVal.getValue());
        }

        this.crate = roCrateBuilder.build();
        this.types = new LinkedHashMap<>();
        this.propertyTypes = new LinkedHashMap<>();
        this.metadataEntries = new LinkedHashMap<>();
    }

    public SchemaFacade(RoCrate crate)
    {
        this.crate = crate;
        this.types = new LinkedHashMap<>();
        this.propertyTypes = new LinkedHashMap<>();
        this.metadataEntries = new LinkedHashMap<>();
    }

    public static SchemaFacade of(RoCrate crate) throws JsonProcessingException
    {
        SchemaFacade schemaFacade = new SchemaFacade(crate);
        schemaFacade.parseEntities();
        return schemaFacade;

    }

    @Override
    public void addType(IType rdfsClass)
    {

        DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
        builder.addProperty("@id", rdfsClass.getId());
        builder.addProperty("@type", RDFS_CLASS);
        builder.addProperty(RDFS_LABEL, rdfsClass.getLabel());
        builder.addProperty(RDFS_COMMENT, rdfsClass.getComment());

        for (IRestriction restriction : rdfsClass.getResstrictions())
        {
            DataEntity.DataEntityBuilder restrictionBuilder = new DataEntity.DataEntityBuilder();
            restrictionBuilder.addProperty("@id", restriction.getId());
            restrictionBuilder.addProperty("@type", TYPE_RESTRICTION);
            restrictionBuilder.addIdProperty(ON_PROPERTY, restriction.getPropertyType().getId());
            restrictionBuilder.addProperty(OWL_MIN_CARDINALITY, restriction.getMinCardinality());
            restrictionBuilder.addProperty(OWL_MAX_CARDINALITY, restriction.getMaxCardinality());
            builder.addIdProperty(OWL_RESTRICTION, restriction.getId());
            crate.addDataEntity(restrictionBuilder.build());
        }


        rdfsClass.getSubClassOf().forEach(x -> builder.addIdProperty("rdfs:subClassOf", x));
        this.types.put(rdfsClass.getId(), rdfsClass);
        DataEntity entity = builder.build();
        entity.addIdListProperties(EQUIVALENT_CLASS, rdfsClass.getOntologicalAnnotations());
        crate.addDataEntity(entity);

    }

    @Override
    public List<IType> getTypes()
    {
        return this.types.values().stream().toList();
    }

    @Override
    public IType getTypes(String id)
    {
        return this.types.get(id);
    }

    @Override
    public void addPropertyType(IPropertyType rdfsProperty)
    {
        DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();

        builder.setId(rdfsProperty.getId());
        builder.addProperty("@type", RDF_PROPERTY);
        builder.addProperty(RDFS_LABEL, rdfsProperty.getLabel());
        builder.addProperty(RDFS_COMMENT, rdfsProperty.getComment());

        var stuff = builder.build();
        stuff.addIdListProperties("schema:rangeIncludes",
                rdfsProperty.getRange());
        stuff.addIdListProperties("schema:domainIncludes",
                rdfsProperty.getDomain().stream().map(x -> x.getId()).collect(Collectors.toList()));
        stuff.addIdListProperties(EQUIVALENT_CONCEPT,
                rdfsProperty.getOntologicalAnnotations());
        crate.addDataEntity(stuff);
        propertyTypes.put(rdfsProperty.getId(), rdfsProperty);

    }

    @Override
    public void addRestriction(IRestriction restriction)
    {

    }

    @Override
    public List<IPropertyType> getPropertyTypes()
    {
        return propertyTypes.values().stream().toList();
    }

    @Override
    public IPropertyType getPropertyType(String id)
    {
        return propertyTypes.get(id);
    }

    @Override
    public void addEntry(IMetadataEntry metaDataEntry)
    {
        DataEntity.DataEntityBuilder builder = new DataEntity.DataEntityBuilder();
        builder.setId(metaDataEntry.getId());
        for (String type : metaDataEntry.getTypes())
        {
            builder.addType(type);
        }
        ObjectMapper objectMapper = new ObjectMapper();

        metaDataEntry.getValues().forEach((s, o) -> {
            if (o instanceof Double)
            {
                builder.addProperty(s, (Double) o);
            } else if (o instanceof Integer)
            {
                builder.addProperty(s, (Integer) o);
            } else if (o instanceof Boolean)
            {
                builder.addProperty(s, (Boolean) o);
            } else if (o instanceof String)
            {
                builder.addProperty(s, o.toString());
            } else if (o == null)
            {
                builder.addProperty(s, objectMapper.nullNode());
            }
        });
        DataEntity dataEntity = builder.build();
        metaDataEntry.getReferences().forEach(dataEntity::addIdListProperties);

        crate.addDataEntity(dataEntity);

    }

    @Override
    public IMetadataEntry getEntry(String id)
    {
        return metadataEntries.get(id);
    }

    @Override
    public List<IMetadataEntry> getEntries(String rdfsClassId)
    {
        return metadataEntries.values().stream()
                .filter(x -> matchClasses(resolvePrefixSingleValue(rdfsClassId), x))
                .toList();
    }

    @Override
    public List<IRestriction> getRestrictions()
    {
        return null;
    }

    private boolean matchClasses(String queryClassId, IMetadataEntry entry)
    {
        if (entry.getTypes().stream().anyMatch(x -> x.equals(queryClassId)))
        {
            return true;
        }

        return entry.getTypes().stream()
                .map(x -> p.matcher(x))
                .map(x -> x.replaceAll("_:"))
                .anyMatch(x -> x.equals(queryClassId));

    }

    private void parseEntities() throws JsonProcessingException
    {

        localPrefix = getLocalPrefix(crate.getJsonMetadata());
        Map<String, String> keyValuePairs = getKeyValPairsFromMetadata(crate.getJsonMetadata());
        for (var keyValPair : keyValuePairs.entrySet())
        {
            if (keyValPair.getValue().equals("http://schema.org/rangeIncludes"))
            {
                rangeIdentifier = keyValPair.getKey();
            }
            if (keyValPair.getValue().equals("http://schema.org/domainIncludes"))
            {
                domainIdentifier = keyValPair.getKey();
            }
        }




        Map<String, IPropertyType> properties = new LinkedHashMap<>();
        Map<String, IType> classes = new LinkedHashMap<>();
        Map<String, IMetadataEntry> entries = new LinkedHashMap<>();

        Map<String, Type> restrictionToTypeId = new LinkedHashMap<>();



        for (DataEntity entity : crate.getAllDataEntities())
        {
            String type = entity
                    .getProperty("@type").asText();
            String id =
                    entity.getProperty("@id")
                            .asText();

            switch (type)
            {
                case "rdfs:Class" ->
                {
                    Type myType = new Type();
                    myType.setSubClassOf(parseMultiValued(entity, "rdfs:subClassOf"));
                    myType.setOntologicalAnnotations(
                            parseMultiValued(entity, EQUIVALENT_CLASS));
                    myType.setId(resolvePrefixSingleValue(id));
                    classes.put(resolvePrefixSingleValue(id), myType);
                    parseMultiValued(entity, OWL_RESTRICTION).forEach(
                            x -> restrictionToTypeId.put(x, myType));

                }


            }

        }

        for (DataEntity entity : crate.getAllDataEntities())
        {
            String type = entity
                    .getProperty("@type").asText();
            String id =
                    entity.getProperty("@id")
                            .asText();

        if (type.equals(RDF_PROPERTY) || type.equals("rdfs:Property"))
        {
                PropertyType rdfsProperty = new PropertyType();
                rdfsProperty.setId(resolvePrefixSingleValue(id));

                rdfsProperty.setOntologicalAnnotations(
                        parseMultiValued(entity, EQUIVALENT_CONCEPT));

                List<String> rawRange = parseMultiValued(entity, rangeIdentifier);

                List<IDataType> dataTypes = rawRange.stream()
                        .filter(LiteralType::isLiteralType)
                        .map(LiteralType::getByTypeName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                List<IType> types = rawRange.stream()
                        .filter(x -> !LiteralType.isLiteralType(x))
                        .map(this::resolvePrefixSingleValue)
                        .map(classes::get)
                        .collect(Collectors.toList());

                dataTypes.stream().forEach(rdfsProperty::addDataType);
                types.forEach(rdfsProperty::addType);

                rdfsProperty.setDomainIncludes(
                        parseMultiValued(entity, domainIdentifier).stream()
                                .map(x -> resolvePrefixSingleValue(x))
                                .map(classes::get).collect(
                                        Collectors.toList()));
                properties.put(resolvePrefixSingleValue(id), rdfsProperty);

            }
        }


        for (DataEntity entity : crate.getAllDataEntities())
        {
            String type = entity.getProperty("@type").asText();
            String id =
                    entity.getProperty("@id")
                            .asText();

            if (type.equals(OWL_RESTRICTION))
            {
                String onProperty = parseMultiValued(entity, ON_PROPERTY).get(0);
                int minCardinality =
                        entity.getProperty(OWL_MIN_CARDINALITY).numberValue().intValue();

                int maxCardinality =
                        entity.getProperty(OWL_MAX_CARDINALITY).numberValue().intValue();
                Restriction restriction =
                        new Restriction(id, properties.get(onProperty), minCardinality,
                                maxCardinality);
                restrictionToTypeId.get(id).addRestriction(restriction);
            }

        }


        for (var entity : crate.getAllDataEntities())
        {
            Set<String> type = parseTypes(entity);

            String id =
                    entity.getProperty("@id")
                            .asText();
            if (!doesTypeExist(type, classes, localPrefix))
            {
                continue;
            }

            Map<String, Serializable> entryProperties = new LinkedHashMap<>();
            MetadataEntry entry = new MetadataEntry();
            entry.setId(id);

            entry.setTypes(resolvePrefix(type));
            Map<String, List<String>> references = new LinkedHashMap<>();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Serializable> keyVals =
                    objectMapper.readValue(entity.getProperties().toString(), HashMap.class);
            for (Map.Entry<String, Serializable> a : keyVals.entrySet())
            {
                if (properties.containsKey(a.getKey()))
                {
                    IPropertyType property = properties.get(a.getKey());
                    if (property.getRange().stream().anyMatch(x -> x.startsWith("xsd:")))
                    {
                        entryProperties.put(a.getKey(), a.getValue().toString());
                    } else
                    {
                        List<String> refs = parseMultiValued(entity, a.getKey());
                        references.put(a.getKey(), refs);
                    }
                }
            }
            entry.setProps(entryProperties);
            entry.setReferences(references);
            entries.put(id, entry);
        }
        System.out.println("Done");
        this.types = classes;
        this.propertyTypes = properties;
        this.metadataEntries = entries;

    }

    private Set<String> resolvePrefix(Set<String> types)
    {
        Pattern placeholderPattern = Pattern.compile("^_:");

        LinkedHashSet newTypes = new LinkedHashSet();
        for (String type : types)
        {
            newTypes.add(placeholderPattern.matcher(type).replaceAll(localPrefix));

        }
        return newTypes;
    }

    private String resolvePrefixSingleValue(String type)
    {
        Pattern placeholderPattern = Pattern.compile("^_:");

        return placeholderPattern.matcher(type).replaceAll(localPrefix);
    }

    private List<String> parseMultiValued(DataEntity dataEntity, String key)
    {
        JsonNode node = dataEntity.getProperty(key);
        if (node instanceof ObjectNode)
        {
            return List.of(node.get("@id").textValue());
        }
        if (node instanceof ArrayNode arrayNode)
        {
            List<String> accumulator = new ArrayList<>();
            arrayNode.elements().forEachRemaining(
                    x -> accumulator.add(x.get("@id").textValue())
            );
            return accumulator;
        }
        return List.of();

    }

    private Set<String> parseTypes(DataEntity entity)
    {
        JsonNode typeResult = entity.getProperty("@type");
        if (typeResult.isTextual())
        {
            return Set.of(typeResult.textValue());
        }
        if (typeResult.isArray())
        {
            ArrayNode arrayNode = (ArrayNode) typeResult;
            Set<String> typeroos = new LinkedHashSet<>();
            arrayNode.forEach(x -> typeroos.add(x.textValue()));
            return typeroos;

        }
        throw new RuntimeException("Unknown node type for @type");

    }

    Map<String, String> getKeyValPairsFromMetadata(String metaDataJson)
            throws JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        LinkedHashMap vals = objectMapper.readValue(metaDataJson, LinkedHashMap.class);

        if (vals.get("@context") instanceof LinkedHashMap<?, ?>)
        {

            return (Map<String, String>) vals.get("@context");

        }

        if (vals.get("@context") instanceof String)
        {
            return Map.of();
        }

        List<Object> nodes = (List<Object>) vals.get("@context");
        Map key_vals = (Map) nodes.get(1);

        Map<String, String> result = new LinkedHashMap<>();
        for (Object a : key_vals.entrySet())
        {
            Map.Entry b = (Map.Entry) a;
            result.put(b.getKey().toString(), b.getValue().toString());
        }

        return result;
    }

    String getLocalPrefix(String jsonMetaData) throws JsonProcessingException
    {
        Map<String, String> keyVals = getKeyValPairsFromMetadata(jsonMetaData);
        for (Map.Entry<String, String> entry : keyVals.entrySet())
        {
            if (entry.getValue().equals("_:"))
            {
                return entry.getKey() + ":";
            }

        }
        return "";
    }

    boolean doesTypeExist(Set<String> types, Map<String, IType> classes, String localPrefix)
    {
        p = Pattern.compile("^" + localPrefix + ":", Pattern.CASE_INSENSITIVE);

        boolean somethingFound = false;
        for (String type : types)
        {
            boolean typeFound = false;

            Matcher m = p.matcher(type);
            if (classes.containsKey(type))
            {
                typeFound = true;
            }
            if (classes.containsKey(m.replaceAll("_:")))
            {
                typeFound = true;
            }

            if (!typeFound)
            {
                System.out.println("Type " + type + " does not seem to be part of the schema");
            }
            somethingFound = somethingFound || typeFound;
        }

        return somethingFound;

    }

}
