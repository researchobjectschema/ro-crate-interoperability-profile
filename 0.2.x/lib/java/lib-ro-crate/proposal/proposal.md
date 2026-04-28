# Profile/Module - RO-Crate Convention to Include Schema and Metadata

**Index:**

- [Version](#version)
- [Definitions](#definitions)
- [Goals](#goals)
- [Technologies and Usage](#technologies-and-usage)
  - [Schema Representation](#schema-representation)
    - [RDFS Class](#rdfs-class)
    - [RDFS Property](#rdfs-class)
  - [Metadata Representation](#metadata-representation)
    - [RDF Metadata Entry](#rdf-metadata-entry)
- [Reference Examples](#reference-examples-for-both-schema-and-entries)
- [API](#api)
  - [Schema Representation DTOs](#schema-representation-dtos)
  - [Metadata Representation DTOs](#metadata-representation-dtos)
  - [Additional RO-Crate API Methods](#additional-ro-crate-api-methods)
- [API Reference Implementation in Java](#api-reference-implementation-in-java)
- [API Reference Examples in Java](#api-reference-examples-in-java)
- [Ongoing Work](#ongoing-work)
- [Possible Future Directions](#possible-future-directions)
- [People](#people)

# Version

0.0.1, initial version, compatible with RO-Crate 1.1

# Definitions

We use the following definitions in our proposal.

- Schema: A logical design that defines the structure, organization and relationship between data.
- Metadata: data of a database adhering to the schema.
- Ontology: A set of concepts and the relationships between these concepts.

# Goals

This proposal SHOULD allow the means to exchange a database schema and database contents in a
standardized way.

As consequence, Integrations SHOULD NOT need to parse individual files in non-standardized formats
anymore to obtain such information but MAY use the Ro-Crate API for such purpose.

Since the goal is that multiple established systems can adhere to it, this poses the 
additional problem that are multiple schemas in use for similar concepts.
To address this, we propose a way to annotate our schemas with ontological information.
The ontologies allow identification of shared concepts.
Knowing which concepts are shared allows easier integration for different schemas.

Establishing such a format for interoperability would also benefit independent interoperability
efforts, as they would be available for reuse in other interoperability projects.

This specification is made to be usable in Ro-Crate 1.1, as such:
- It SHOULD NOT add new keywords.
- It SHOULD establish a convention that can be used by the RO-Crate API to read/write the information.

# Technologies and Usage

- [RDF](https://www.w3.org/RDF/): Resource Description Framework is a specification developed by the
  World Wide Web
  Consortium (W3C) to provide a framework for representing and exchanging data on the web in a
  structured way. RDF allows information to be described in terms of subject-predicate-object
  triples, which form a graph of interconnected data. RDF can be serialized in different formats,
  including JSON-LD as used by RO-Crate.
- [RDFS](https://www.w3.org/TR/rdf-schema/): Resource Description Framework Schema is a
  specification developed by the World Wide Web Consortium (W3C) that extends RDF (Resource
  Description Framework). RDFS provides a way to define the structure and relationships of RDF data,
  allowing for the creation of vocabularies and the specification of classes, properties, and
  hierarchies in an RDF dataset.
- [OWL](https://www.w3.org/OWL/): Web Ontology Language is a formal language used to define and
  represent ontologies on the web.
- [XSD](https://www.w3.org/TR/xmlschema11-1/): XML Schema Definition is a language used to define
  the structure, content, and constraints of XML documents. It will be used in this specification to
  express primitive type.

## Schema Representation
Because the schema is graph-based this can be easily integrated into the RO-Crate graph.

The schema could also be included in a separate file in a future version of this specification.

Ontologies are added using OWL's `equivalentClass` and `equivalentProperty` properties.

What are the advantages of this?

- the format is backward compatible
- this only uses features that RO-Crate already provides, no additional keywords are required
- Common format for export that prevents `n * (n - 1)` integration situation
- Thorough description of metadata, better automated checking and read-in

**Formal description:**

RO-Crate MUST include a graph description of the schema.
This is expressed using 2 types:

- RDFS Class
- RDFS Property

### RDFS Class

Based on RDFS classes, these can be used as object and subjects of triples.

| Type/Property       | Required? | Description                                                                                                                                       |
|---------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| @id                 | MUST      | ID of the entry                                                                                                                                   |
| @type               | MUST      |  Is `rdfs:Class`                                                                                                                                  |
| owl:equivalentClass | MAY       | Ontological annotation  https://www.w3.org/TR/owl-ref/#equivalentClass-def                                                                        |
| rdfs:subClassOf     | MUST      | Used to indicate inheritance. Each entry has to inherit from something, this can be a base type.  https://www.w3.org/TR/rdf-schema/#ch_subclassof |

### RDFS Property

RDFS Properties, these represent predicates in triples.
They also specify, which classes they can interact with.

| Type/Property          | Required? | Description                                                                |
|------------------------|-----------|----------------------------------------------------------------------------|
| @id                    | MUST      | ID of the entry                                                            |
| @type                  | MUST      | Is `rdfs:Property`                                                         |
| owl:equivalentProperty | MAY       | Ontological annotation  https://www.w3.org/TR/owl-ref/#equivalentClass-def |
| schema:domainIncludes  | MUST      | Describes the possible types of the subject. This can be one or many.      |
| schema:rangeIncludes   | MUST      | Describes the possible types of the object. This can be one or many.       |

## Metadata Representation

**Formal description:**

RO-Crate MUST include a graph description of the metadata entries.
This is expressed using 1 type:

- Metadata Entry

### RDF Metadata Entry

A metadata entry, described by a RDFS class.

| Type/Property | Required? | Description                             |
|---------------|-----------|-----------------------------------------|
| @id           | MUST      | ID of the entry                         |
| @type         | MUST      | Type of the entry, MUST be a RDFS Class |

Further properties are included as specified in the RDFS description as fields.

# Reference Examples for both Schema and Entries

We created a small example. It can be found under:
https://sissource.ethz.ch/sispub/openbis/-/blob/master/lib-ro-crate/ro-crate-1.1/ro-crate-metadata/ro-crate-metadata.json.
This describes the export
of https://sissource.ethz.ch/sispub/openbis/-/tree/master/lib-ro-crate/reference-openbis-export.

# API

**Formal description:**

To be general, the API uses a lot of strings. This allows flexibility in the classes being used.

The interfaces are shown using Java since is a statically typed language, but they can be
implemented in most languages,
including Python and Javascript.

## Schema Representation DTOs

```Java

/* Represents a class, if we are talking about a schema, it is closely related with the definition of a table or type */
interface IType
{

  /* Returns the ID of this type */
  String getId();

  /* Returns IDs of the types this type inherits from */
  List<String> getSubClassOf();

  /* Returns the ontological annotations of this type */
  List<String> getOntologicalAnnotations();

}

/* Represents a property in a graph, if we are talking about a schema, is closely related with a table column or type property */
interface IPropertyType
{

  /* Returns the ID of this property type */
  String getId();

  /* Return possible values for the subject of this property type */
  List<String> getDomain();

  /* Return possible values for the object of this property type */
  List<String> getRange();

  /* Returns the ontological annotations of this property type */
  List<String> getOntologicalAnnotations();
    
    }
```

## Metadata Representation DTOs

```Java
/* Represents a metadata entity. It is described */
interface IMetadataEntry
{


    /**
    * Returns the ID of this entry
    */ 
    String getId();

  /* Returns the type ID of this entry */
  String getClassId();

  /* These are key-value pairs for serialization. These are single-valued.
   * Serializable classes are: String, Number and Boolean */
    Map<String, Serializable> getValues();

  /* These are references to other objects in the graph.
   * Each key may have one or more references */
  Map<String, List<String>> getReferences();
}
```

## Additional RO-Crate API Methods


```Java
/* The API to program against, this wraps around existing RO-Crate APIs. */
interface ISchemaFacade
{

  /* Adds a single class */
  void addType(IType type);

  /** Retrieves all Classes */
  List<IType> getTypes();

  /* Get a single type by its ID */
  IType getTypes(String id);

  /* Adds a single property */
  void addPropertyType(IPropertyType property);

  /* Get all Properties */
  List<IPropertyType> getPropertyTypes();

  /* Gets a single property by its ID. */
  IPropertyType getPropertyType(String id);

  /* Add a single metadata entry */
  void addEntry(IMetadataEntry entry);

  /* Get a single metadata entry by its ID */
  IMetadataEntry getEntry(String id);

  /* Get all metadata entities */
  List<IMetadataEntry> getEntries(String rdfsClassId);

}
```

# API Reference Implementation in Java

A working implementation of the API for Java can be found
at: https://sissource.ethz.ch/sispub/openbis/-/tree/master/lib-ro-crate/.

A compiled jar can be downloaded
at: https://sissource.ethz.ch/sispub/openbis/-/tree/master/lib-ro-crate/dist/lib-ro-crate.jar.
The dependencies are specified in the module's `build.gradle`
file: https://sissource.ethz.ch/sispub/openbis/-/tree/master/lib-ro-crate/build.gradle.

# API Reference Examples in Java

Working examples of the API in java to read and write can be found
at: https://sissource.ethz.ch/sispub/openbis/-/tree/master/lib-ro-crate/, specifically the class
files

- https://sissource.ethz.ch/sispub/openbis/-/blob/master/lib-ro-crate/src/main/java/ch/eth/sis/rocrate/example/ReadExample.java
- https://sissource.ethz.ch/sispub/openbis/-/blob/master/lib-ro-crate/src/main/java/ch/eth/sis/rocrate/example/WriteExample.java

# Ongoing Work

- Adding complex data types
- Using `rdfs:Label` to indicate the original name of a property (this could also help in resolving
  properties with the same name)
- Validation of data types expressed in the schema, e.g. enforcing ISO 8601 for dates
- Bundling ontologies in the RO-Crate

# Possible Future Directions

- We would like to store the schema and metadata information in separate files and indicate the
  format of the file in `ro-crate-metadata.json`
- Other serialization formats could be supported when using separate files
- Adding methods for deleting to facade to have all CRUD operations

# People

- Andreas Meier (andreas.meier@ethz.ch)
- Juan Fuentes (juan.fuentes@id.ethz.ch)