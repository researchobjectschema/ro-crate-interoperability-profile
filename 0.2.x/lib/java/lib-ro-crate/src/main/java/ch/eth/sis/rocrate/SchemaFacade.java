package ch.eth.sis.rocrate;

import ch.eth.sis.rocrate.facade.*;
import ch.eth.sis.rocrate.schemaorg.SchemaOrgInformation;
import ch.eth.sis.rocrate.schemaorg.SchemaOrgPropertyResolver;
import ch.eth.sis.rocrate.schemaorg.SchemaOrgReader;
import ch.eth.sis.rocrate.util.RoCrateValueUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaFacade implements ISchemaFacade
{

    private static Logger logger = LoggerFactory.getLogger(SchemaFacade.class);

    private final static String RDFS_CLASS = "rdfs:Class";

    private final static String RDF_PROPERTY = "rdf:Property";

    public static final String EQUIVALENT_CLASS = "owl:equivalentClass";

    public static final String EQUIVALENT_PROPERTY = "owl:equivalentProperty";



    String rangeIdentifier = "schema:rangeIncludes";

    String domainIdentifier = "schema:domainIncludes";

    public static final String OWL_MIN_CARDINALITY = "owl:minCardinality";

    public static final String OWL_MAX_CARDINALITY = "owl:maxCardinality";

    public static final String OWL_RESTRICTION = "owl:Restriction";

    public static final String OWL_RESTRICTION_PROPERTY = "owl:restriction";


    public static final String ON_PROPERTY = "owl:onProperty";

    public static final String RDFS_LABEL = "rdfs:label";

    public static final String RDFS_COMMENT = "rdfs:comment";

    Pattern p;

    String localPrefix = ":";

    private Map<String, IType> types;

    private Map<String, IPropertyType> propertyTypes;

    private Map<String, IMetadataEntry> metadataEntries;

    private Map<String, String> identifiersToEnlong;

    private Map<String, String> identifiersToShorten;

    private final RoCrate crate;

    private SchemaOrgInformation schema_org_information;

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
                "https://schema.org");
        roCrateBuilder.addValuePairToContext("owl",
                "http://www.w3.org/2002/07/owl#");
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

        List<DataEntity> fileEntities = crate.getAllDataEntities().stream()
                .filter(x -> RoCrateValueUtil.parseMultiValued(x, "@type").contains("File"))
                .toList();

        Map<String, DataEntity> idToFileNode =
                fileEntities.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));

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
            restrictionBuilder.addProperty("@type", OWL_RESTRICTION);
            restrictionBuilder.addIdProperty(ON_PROPERTY, restriction.getPropertyType().getId());
            restrictionBuilder.addProperty(OWL_MIN_CARDINALITY, restriction.getMinCardinality());
            restrictionBuilder.addProperty(OWL_MAX_CARDINALITY, restriction.getMaxCardinality());
            builder.addIdProperty(OWL_RESTRICTION_PROPERTY, restriction.getId());
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

        DataEntity builtProperty = builder.build();
        if (rdfsProperty.getRange().isEmpty())
        {
            builtProperty.addIdListProperties("schema:rangeIncludes", List.of(":Object "));
        }
        builtProperty.addIdListProperties("schema:rangeIncludes",
                rdfsProperty.getRange());

        builtProperty.addIdListProperties("schema:domainIncludes",
                rdfsProperty.getDomain().stream().map(x -> x.getId()).collect(Collectors.toList()));
        builtProperty.addIdListProperties(EQUIVALENT_PROPERTY,
                rdfsProperty.getOntologicalAnnotations());
        crate.addDataEntity(builtProperty);
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
            } else if (o instanceof String[])
            {
                for (String a : (String[]) o)
                {
                    builder.addProperty(s, a);
                }

            }
        });
        DataEntity dataEntity = builder.build();
        metaDataEntry.getReferences().forEach(dataEntity::addIdListProperties);
        for (String type : metaDataEntry.getTypes())
        {
            metadataEntries.put(type, metaDataEntry);
        }

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

        if (entry.getTypes().stream()
                .map(x -> Optional.ofNullable(identifiersToShorten.get(x)).orElse(x))
                .anyMatch(x -> x.equals(queryClassId)))
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
        ClassLoader classLoader = getClass().getClassLoader();
        classLoader.getName();


        if (schema_org_information == null)
        {
            try (InputStream inputStream = classLoader.getResourceAsStream(
                    "ch/eth/sis/rocrate/schemaorg/schemaorg-all-https-v29.0.ttl"))
            {
                schema_org_information = SchemaOrgReader.read(inputStream);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        localPrefix = getLocalPrefix(crate.getJsonMetadata());

        Map<String, String> keyValuePairs = getKeyValPairsFromMetadata(crate.getJsonMetadata());
        this.identifiersToEnlong = keyValuePairs.entrySet().stream()
                .filter(x -> x.getValue().contains("https://schema.org") || x.getValue()
                        .contains("http://schema.org"))
                .collect(Collectors.toMap(x -> x.getKey(), x -> "schema:" + x.getKey()));
        this.identifiersToShorten = keyValuePairs.entrySet().stream()
                .filter(x -> x.getValue().contains("https://schema.org") || x.getValue()
                        .contains("http://schema.org"))
                .collect(Collectors.toMap(x -> "schema:" + x.getKey(), x -> x.getKey()));

        Map<String, IPropertyType> properties = new LinkedHashMap<>();
        Map<String, IType> idsToTypes = new LinkedHashMap<>();
        Map<String, IMetadataEntry> entries = new LinkedHashMap<>();

        Map<String, Type> restrictionToTypeId = new LinkedHashMap<>();

        List<AbstractEntity> abstractEntities = new ArrayList<>();
        abstractEntities.addAll(crate.getAllContextualEntities());
        abstractEntities.addAll(crate.getAllDataEntities());
        for (AbstractEntity entity : abstractEntities)
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
                    myType.setSubClassOf(
                            RoCrateValueUtil.parseMultiValued(entity, "rdfs:subClassOf"));
                    myType.setOntologicalAnnotations(
                            RoCrateValueUtil.parseMultiValued(entity, EQUIVALENT_CLASS));
                    myType.setId(resolvePrefixSingleValue(id));
                    idsToTypes.put(resolvePrefixSingleValue(id), myType);
                    RoCrateValueUtil.parseMultiValued(entity, OWL_RESTRICTION_PROPERTY).forEach(
                            x -> restrictionToTypeId.put(x, myType));

                }

            }

        }

        for (AbstractEntity entity : abstractEntities)
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
                        RoCrateValueUtil.parseMultiValued(entity, EQUIVALENT_PROPERTY));

                List<String> rawRange =
                        Stream.concat(
                                        RoCrateValueUtil.parseMultiValued(entity, rangeIdentifier).stream(),
                                        RoCrateValueUtil.parseMultiValued(entity, "rangeIncludes").stream())
                                .collect(
                                Collectors.toList());

                List<IDataType> dataTypes = rawRange.stream()
                        .filter(LiteralType::isLiteralType)
                        .map(LiteralType::getByTypeName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                List<IType> types = rawRange.stream()
                        .filter(x -> !LiteralType.isLiteralType(x))
                        .map(this::resolvePrefixSingleValue)
                        .map(idsToTypes::get)
                        .collect(Collectors.toList());

                dataTypes.stream().forEach(rdfsProperty::addDataType);
                types.forEach(rdfsProperty::addType);

                Stream<String> domain =
                        Stream.concat(RoCrateValueUtil.parseMultiValued(entity, domainIdentifier)
                                        .stream(),
                                RoCrateValueUtil.parseMultiValued(entity, "domainIncludes")
                                        .stream());

                rdfsProperty.setDomainIncludes(

                        domain
                                .map(x -> resolvePrefixSingleValue(x))
                                .map(idsToTypes::get).collect(
                                        Collectors.toList()));
                properties.put(resolvePrefixSingleValue(id), rdfsProperty);

            }
        }

        for (AbstractEntity entity : abstractEntities)
        {
            String type = entity.getProperty("@type").asText();
            String id =
                    entity.getProperty("@id")
                            .asText();

            if (type.equalsIgnoreCase(OWL_RESTRICTION))
            {
                String onProperty = RoCrateValueUtil.parseMultiValued(entity, ON_PROPERTY).get(0);
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

        for (IType type : idsToTypes.values())
        {
            Type type1 = (Type) type;

            Set<IPropertyType> schemaOrgProperties =
                    SchemaOrgPropertyResolver.findSchemaOrgProperties(schema_org_information, type);
            for (IPropertyType propertyType : schemaOrgProperties)
            {
                if (!propertyType.getDomain().contains(type))
                {
                    type1.addProperty((PropertyType) propertyType);
                }
                properties.putIfAbsent(propertyType.getId(), propertyType);

            }

        }

        List<AbstractEntity> entities = new ArrayList<>();
        entities.addAll(crate.getAllDataEntities());
        entities.addAll(crate.getAllContextualEntities());
        List<DataEntity> allDataEntities =
                crate.getAllDataEntities().stream().collect(Collectors.toList());
        for (AbstractEntity entity : entities)
        {

            Set<String> type = parseTypes(entity);
            for (String typeId : type)
            {
                String schemafiedTypeId = "https://schema.org/" + typeId;

                if (!idsToTypes.containsKey(
                        typeId) && schema_org_information.getIdentifiersToDataTypes()
                        .containsKey(schemafiedTypeId))
                {
                    IType schemaOrgType = schema_org_information.getIdentifiersToDataTypes()
                            .get(schemafiedTypeId);
                    idsToTypes.put(typeId, schemaOrgType);
                    List<IPropertyType> propertyTypes1 =
                            schema_org_information.getTypeToProperties().get(schemaOrgType);
                    for (IPropertyType propertyType : propertyTypes1)
                    {
                        properties.putIfAbsent(propertyType.getId(), propertyType);
                    }
                    types.put(typeId, schemaOrgType);
                }
            }

            String id =
                    entity.getProperty("@id")
                            .asText();
            if (!doesTypeExist(schema_org_information, type, idsToTypes, localPrefix))
            {
                continue;
            }

            Map<String, Serializable> entryProperties = new LinkedHashMap<>();
            MetadataEntry entry = new MetadataEntry();
            if (entity instanceof DataEntity)
            {
                if (((DataEntity) entity).getPath() != null)
                {
                    entry.setPath(((DataEntity) entity).getPath());
                }
            }
            entry.setId(id);

            entry.setTypes(resolvePrefix(type));
            Map<String, List<String>> references = new LinkedHashMap<>();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Serializable> keyVals =
                    objectMapper.readValue(entity.getProperties().toString(), HashMap.class);
            for (Map.Entry<String, Serializable> a : keyVals.entrySet())
            {
                String key =
                        properties.containsKey(a.getKey()) ? a.getKey() : "schema:" + a.getKey();

                if (!a.getKey().equals("@type") && !a.getKey().equals("@id") && !a.getKey()
                        .equals("schema:hasPart"))
                {
                    IPropertyType property = properties.get(key);
                    if (property == null)
                    {
                        logger.warn(
                                "No PropertyType found for property " + key + " in entry " + entry.getId());
                        entryProperties.put(a.getKey(), a.getValue());
                    } else if (property.getRange().stream().anyMatch(x -> x.startsWith("xsd:")))
                    {
                        if (a.getValue() instanceof HashMap<?, ?>)
                        {
                            HashMap<?, ?> hashMap = (HashMap<?, ?>) a.getValue();
                            List<String> refs =
                                    RoCrateValueUtil.parseMultiValued(entity, a.getKey());
                            references.put(a.getKey(), refs);
                        } else
                        {

                            entryProperties.put(a.getKey(), a.getValue().toString());

                        }
                    } else
                    {
                        List<String> refs = RoCrateValueUtil.parseMultiValued(entity, a.getKey());
                        references.put(a.getKey(), refs);
                    }
                }
            }
            entry.setProps(entryProperties);
            entry.setReferences(references);
            setFileReferences(entity, entry, allDataEntities);
            entries.put(id, entry);
        }

        System.out.println("Done");
        this.types = idsToTypes;
        this.propertyTypes = properties;
        this.metadataEntries = entries;

    }

    private void setFileReferences(AbstractEntity abstractEntity, MetadataEntry metadataEntry,
            Collection<DataEntity> fileEntities)
    {
        assert abstractEntity.getId().equals(metadataEntry.getId());

        Map<String, DataEntity> idToFileEntity =
                fileEntities.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));

        abstractEntity.getProperty("schema:hasPart");

        List<DataEntity> collect =
                abstractEntity.getLinkedTo().stream().map(x -> idToFileEntity.get(x))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        metadataEntry.setFileEntitiesReferenced(collect);

    }


    private Set<String> resolvePrefix(Set<String> types)
    {
        Pattern placeholderPattern = Pattern.compile("^_:");

        LinkedHashSet newTypes = new LinkedHashSet();
        for (String type : types)
        {

            newTypes.add(resolvePrefixSingleValue(type));

        }
        return newTypes;
    }

    private String resolvePrefixSingleValue(String type)
    {
        if (identifiersToEnlong == null)
        {
            identifiersToEnlong = new LinkedHashMap<>();

        }
        if (identifiersToShorten == null)
        {
            identifiersToShorten = new LinkedHashMap<>();
        }

        if (identifiersToEnlong.containsKey(type))
        {
            return identifiersToEnlong.get(type);
        }
        Map<String, String> shorten = identifiersToEnlong.entrySet().stream()
                .collect(Collectors.toMap(x -> x.getValue(), x -> x.getKey()));
        if (shorten.containsKey(type))
        {
            return shorten.get(type);
        }

        Pattern placeholderPattern = Pattern.compile("^_:");

        return placeholderPattern.matcher(type).replaceAll(localPrefix);
    }

    private Set<String> parseTypes(AbstractEntity entity)
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
            if (entry.getValue().startsWith("_:"))
            {
                return entry.getKey() + ":";
            }

        }
        return "";
    }

    boolean doesTypeExist(SchemaOrgInformation schemaOrgInformation, Set<String> types,
            Map<String, IType> classes, String localPrefix)
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
            if (schemaOrgInformation.getIdentifiersToDataTypes().containsKey(type)
            )
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
