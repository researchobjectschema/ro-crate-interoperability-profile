package ch.eth.sis.rocrate.schemaorg;

import ch.eth.sis.rocrate.facade.*;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class SchemaOrgReader
{

    public static final String HTTPS_SCHEMA_ORG_DOMAIN_INCLUDES =
            "https://schema.org/domainIncludes";

    public static final String HTTPS_SCHEMA_ORG_RANGE_INCLUDES =
            "https://schema.org/rangeIncludes";


    // https://github.com/schemaorg/schemaorg/blob/main/data/releases/29.0/schemaorg-all-http.ttl

    public static SchemaOrgInformation read(InputStream inputStream)
    {

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
        InfModel inf = ModelFactory.createInfModel(reasoner, ontModel);


        RDFDataMgr.read(ontModel,
                inputStream,
                Lang.TTL);


        Map<String, IType> idsToTypes = new LinkedHashMap<>();
        Map<String, IPropertyType> idsToProperties = new LinkedHashMap<>();
        Map<IType, Set<String>> inheritanceChainToResolve = new LinkedHashMap<>();

        for (String uri : getClassIdentifiers(ontModel))
        {
            Resource resource = ontModel.getBaseModel().getResource(uri);
            resource.listProperties().toList();

            Set<String> directSuperClasses = new LinkedHashSet<>();
            StmtIterator subClassStmts = resource.listProperties(RDFS.subClassOf);
            while (subClassStmts.hasNext())
            {
                Statement statement = subClassStmts.next();
                directSuperClasses.add(
                        "schema:" + statement.getObject().asResource().getLocalName());
            }

            Set<String> inheritanceChain = getUriPlusAllSuperClasses(inf, uri);


            Type type = new Type();
            Optional.ofNullable(resource.getProperty(RDFS.label)).map(Object::toString)
                    .ifPresent(type::setLabel);
            Optional.ofNullable(resource.getProperty(RDFS.comment)).map(Object::toString)
                    .ifPresent(type::setLabel);
            type.setOntologicalAnnotations(List.of(uri));
            type.setId("schema:" + resource.getLocalName());
            type.setOntologicalAnnotations(List.of(uri));
            idsToTypes.put(uri, type);
            Set<String> superClasses = new LinkedHashSet<>(inheritanceChain);
            superClasses.remove(type.getId());
            inheritanceChainToResolve.put(type, superClasses);

            type.setSubClassOf(directSuperClasses.stream().toList());

            idsToTypes.put("schema:" + resource.getLocalName(), type);

        }

        for (String identifier : getPropertyIdentifiers(inf))
        {
            Property curProperty = inf.getProperty(identifier);

            PropertyType propertyType = new PropertyType();
            propertyType.setId("schema:" + curProperty.getLocalName());
            propertyType.setOntologicalAnnotations(new ArrayList<>(List.of(identifier)));
            StmtIterator range =
                    curProperty.listProperties(inf.getProperty(HTTPS_SCHEMA_ORG_RANGE_INCLUDES));

            while (range.hasNext())
            {
                Statement rangeStatement = range.next();
                String rawRangeString = rangeStatement.getObject().toString();
                var
                        dataType = SchemaOrgDataTypeMapping.getByType(
                        rawRangeString);
                if (dataType != null)
                {
                    propertyType.addDataType(dataType.getDataType());
                } else
                {
                    String localName = inf.getProperty(rawRangeString).getLocalName();

                    propertyType.addType(idsToTypes.get("schema:" + localName));

                }

            }

            StmtIterator domain =
                    curProperty.listProperties(inf.getProperty(HTTPS_SCHEMA_ORG_DOMAIN_INCLUDES));
            while (domain.hasNext())
            {
                Statement domainStatement = domain.next();
                Type type1 = (Type) idsToTypes.get(domainStatement.getObject().toString());
                type1.addProperty(propertyType);
            }

            idsToProperties.put("schema:" + curProperty.getLocalName(), propertyType);



        }

        Map<IType, List<IType>> superTypes = new LinkedHashMap<>();
        for (var ineritanceChain : inheritanceChainToResolve.entrySet())
        {

            superTypes.put(ineritanceChain.getKey(), ineritanceChain.getValue().stream().map(
                    idsToTypes::get).collect(
                    Collectors.toList()));

        }

        Map<IType, List<IPropertyType>> typeToProperties = new LinkedHashMap<>();
        Map<IPropertyType, Set<IType>> propertyToSublcasses = new LinkedHashMap<>();
        for (IPropertyType propertyType : idsToProperties.values())
        {
            if (propertyType.getDomain() == null)
            {
                continue; // empty domains can happen for deprecated properties
            }

            for (IType type : propertyType.getDomain())
            {
                List<IPropertyType> propertyTypes =
                        typeToProperties.getOrDefault(type, new ArrayList<>());
                propertyTypes.add(propertyType);
                typeToProperties.put(type, propertyTypes);

                Set<String> allSubClassesIncludingTransitive =
                        getAllSubClassesIncludingTransitive(ontModel, type);
                for (String subTypeId : allSubClassesIncludingTransitive)
                {
                    IType subType = idsToTypes.get(subTypeId);
                    List<IPropertyType> subTypeProperties =
                            typeToProperties.getOrDefault(subType, new ArrayList<>());
                    if (!subTypeProperties.contains(propertyType))
                    {
                        subTypeProperties.add(propertyType);
                    }
                    typeToProperties.put(subType, subTypeProperties);

                }

                propertyToSublcasses.put(propertyType, new LinkedHashSet<>(
                        allSubClassesIncludingTransitive.stream().map(x -> idsToTypes.get(x))
                                .filter(Objects::nonNull) // there might be EnumerationMembers
                                .collect(
                                        Collectors.toList())));

            }

        }
        for (Map.Entry<IPropertyType, Set<IType>> propertyAndSubclasses : propertyToSublcasses.entrySet())
        {
            List<IType> domain = propertyAndSubclasses.getKey().getDomain();
            for (IType type : propertyAndSubclasses.getValue())
            {
                if (!domain.contains(type))
                {
                    domain.add(type);
                }
            }

        }

        for (IPropertyType propertyType : idsToProperties.values())
        {

            if (propertyType.getDomain() == null)
            {
                continue;
            }
            for (IType type : propertyType.getDomain())
            {

                Type type1 = (Type) type;
                type1.addRestriction(
                        new Restriction(UUID.randomUUID().toString(), propertyType, 0, 0));
            }
        }


        return new SchemaOrgInformation(idsToTypes, idsToProperties, typeToProperties, superTypes);
    }

    private static Set<String> getPropertyIdentifiers(InfModel infModel)
    {

        String query = """
                PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX owl:  <http://www.w3.org/2002/07/owl#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                                
                                
                SELECT DISTINCT ?x
                WHERE {
                    ?x a rdf:Property .
                    FILTER(STRSTARTS(STR(?x), "https://schema.org")) .

                }
                """;
        ResultSet resultSet =
                QueryExecutionFactory.create(query, infModel).execSelect();
        Set<String> solutions = new LinkedHashSet<>();
        while (resultSet.hasNext())
        {
            QuerySolution solution = resultSet.next();
            solutions.add(solution.get("x").toString());
        }
        return solutions;

    }

    private static Set<String> getClassIdentifiers(OntModel ontModel)
    {

        String query = """
                PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX owl:  <http://www.w3.org/2002/07/owl#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                                
                                
                SELECT DISTINCT ?x
                WHERE {
                    ?x a rdfs:Class .

                }
                """;
        ResultSet resultSet =
                QueryExecutionFactory.create(query, ontModel.getBaseModel()).execSelect();
        Set<String> solutions = new LinkedHashSet<>();
        while (resultSet.hasNext())
        {
            QuerySolution solution = resultSet.next();
            solutions.add(solution.get("x").toString());
        }
        return solutions;

    }

    private static Set<String> getUriPlusAllSuperClasses(InfModel inf, String classUri)
    {

        Resource resourceA = inf.getResource(classUri);
        var a = inf.listStatements(resourceA, RDFS.subClassOf, (RDFNode) null).toList();
        Set<String> res = new LinkedHashSet<>();
        a.stream().map(x -> x.getObject().asResource())
                .filter(x -> x.getURI().startsWith("https://schema.org"))
                .map(x -> "schema:" + x.getLocalName()).forEach(res::add);

        return res;

    }

    private static Set<String> getAllSubClassesIncludingTransitive(OntModel ontModel, IType type)
    {
        String query = """ 
                                PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX owl:  <http://www.w3.org/2002/07/owl#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX schema: <https://schema.org/>

                SELECT ?x
                WHERE {
                  ?x rdfs:subClassOf+ $typeId .
                  }
                """;
        ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString(query);
        parameterizedSparqlString.setIri("typeId",
                type.getId().replace("schema:", "https://schema.org/"));


        ResultSet resultSet =
                QueryExecutionFactory.create(parameterizedSparqlString.asQuery(),
                        ontModel.getBaseModel()).execSelect();
        Set<String> solutions = new LinkedHashSet<>();
        while (resultSet.hasNext())
        {
            QuerySolution solution = resultSet.next();
            solutions.add(solution.get("x").toString());
        }
        return solutions;

    }


}
