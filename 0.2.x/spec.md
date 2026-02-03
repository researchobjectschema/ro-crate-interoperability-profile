# Profile/Module : RO-Crate Interoperability Profile

AKA: Convention to include schemas and metadata inside `ro-crate-metadata.json`

**Index:**

### ro-crate-metadata.json Convention
- [Version](#version)
- [What's new?](#whats-new)
- [Definitions](#definitions)
- [Background](#background)
- [Goals](#goals)
- [Technologies and Usage](#technologies-and-usage)
  - [Schema Representation](#schema-representation)
    - [RDFS Class](#rdfs-class)
    - [RDFS Property](#rdfs-class)
  - [Metadata Representation](#metadata-representation)
    - [RDF Metadata Entry](#rdf-metadata-entry)
- [Reference Examples](#reference-examples-for-both-schema-and-entries)

### Reference API
- [API](#api)
  - [Schema Representation DTOs](#schema-representation-dtos)
  - [Metadata Representation DTOs](#metadata-representation-dtos)
  - [Additional RO-Crate API Methods](#additional-ro-crate-api-methods)
- [Primitive Data Types](#primitive-data-types)
- [API Reference Implementation in Java](#api-reference-implementation-in-java)
- [API Reference Examples in Java](#api-reference-examples-in-java)

### Organizational Information
- [Ongoing Work](#ongoing-work)
- [Possible Future Directions](#possible-future-directions)
- [People](#people)

# Changelog

### 0.2.0, compatible with RO-Crate 1.1

#### What's new?

- Cardinalities [RDFS Class](#rdfs-class), [OWl Restriction](#owl-restriction)
- Mandatory properties [RDFS Class](#rdfs-class), [OWl Restriction](#owl-restriction)
- Labels [RDFS Property](#rdfs-class), [RDFS Class](#rdfs-class)
- Human-readable comments[RDFS Property](#rdfs-class), [RDFS Class](#rdfs-class)
- Intersection types [RDF Metadata Entry](#rdf-metadata-entry)
- Better description of primitive data types [Primitive Data Types](#primitive-data-types)
- Ranges of properties are now built using this library's IType and LiteralType
- Updated future plans!  [Possible Future Directions](#possible-future-directions)
- Background section [Background](#background)

### 0.1.0, compatible with RO-Crate 1.1

- Initial version

# Definitions

We use the following definitions in our proposal.

- Schema: A logical design that defines the structure, organization and relationship between data.
- Metadata: data of a database adhering to the schema.
- Ontology: A set of concepts and the relationships between these concepts.

# Background


This convention has its roots in interoperability projects involving established electronic lab notebooks and data repositories.
For historical reasons, schemas for metadata may differ between systems, even if they cover similar concepts.

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
| @type               | MUST      | Is `rdfs:Class`                                                                                                                                   |
| owl:equivalentClass | MAY       | Ontological annotation  https://www.w3.org/TR/owl-ref/#equivalentClass-def                                                                        |
| rdfs:subClassOf     | MAY      | Used to indicate inheritance. Each entry has to inherit from something, this can be a base type.  https://www.w3.org/TR/rdf-schema/#ch_subclassof |
| rdfs:label          | MAY       | Label of the class                                                                                                                                |
| rdfs:comment        | MAY       | Human-readable description of this class                                                                                                          |
| owl:restriction     | MAY       | OWL restriction, a list of OWL restrictions, see [OWl Restriction](#owl-restriction)                                                              |


### OWL Restriction

These represent restrictions on properties. At the moment, they encode cardinalities. 
Cardinalities with a max of 0 and a min of 0 can be omitted.
A max cardinality of `0` represents an arbitrary, potentially infinite number of values. 

| Type/Property      | Required? | Description                                                                  |
|--------------------|-----------|------------------------------------------------------------------------------|
| @id                | MUST      | ID of the entry                                                              |
| @type              | MUST      | Is `owl:Restriction`                                                         |
| owl:onProperty     | MUST      | Describes the property tye his restriction belongs to                        |
| owl:minCardinality | MAY       | Indicates whether a property is mandatory (1) or not (0).                    |
| owl:maxCardinality | MAY       | Indicates whether a property may have multiple values (0) or only one (1).   |

### RDFS Property

RDFS Properties, these represent predicates in triples.
They also specify, which classes they can interact with.

| Type/Property          | Required? | Description                                                                                             |
|------------------------|-----------|---------------------------------------------------------------------------------------------------------|
| @id                    | MUST      | ID of the entry                                                                                         |
| @type                  | MUST      | Is `rdfs:Property`                                                                                      |
| owl:equivalentProperty | MAY       | Ontological annotation  https://www.w3.org/TR/owl-ref/#equivalentClass-def                              |
| schema:domainIncludes  | MUST      | Describes the possible types of the subject. This can be one or many.                                   |
| schema:rangeIncludes   | MUST      | Describes the possible types of the object. This can be one or many.                                    |
| rdfs:label             | MAY       | Label of the property                                                                                   |
| rdfs:comment           | MAY       | Human-readable description of the property                                                              |


## Metadata Representation

**Formal description:**

RO-Crate MUST include a graph description of the metadata entries.
This is expressed using 1 type:

- Metadata Entry

### RDF Metadata Entry

A metadata entry, described by a RDFS class.

| Type/Property | Required? | Description                                        |
|---------------|-----------|----------------------------------------------------|
| @id           | MUST      | ID of the entry                                    |
| @type         | MUST      | Type of the entry, MUST be at least one RDFS Class |

Further properties are included as specified in the RDFS description as fields.

# Reference Examples for both Schema and Entries

We created a small example. It can be found under:
`./examples/ro-crate-1.1/ro-crate-metadata/ro-crate-metadata.json.`
This describes the export
of `./examples/reference-openbis-export`.


# SHCL Schema 

A (embryonal) [SHACL](https://www.w3.org/TR/shacl/) shapes graph to validate exports is located in [`./schema.shacl`](./schema.shacl)

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

  /* Returns a human-readable description of this type */
  String getComment();

  /* Returns a human-readable label of this type */
  String getLabel();

  /* Get Restrictions placed on the properties of this type */
  List<IRestriction> getResstrictions();
  
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

  /* Returns whether this property has a min cardinality. 0 means optional, 1 means mandatory. */
  int getMinCardinality();

  /* Returns whether this property has a max cardinality. 0 means many values possible, 1 means only one is possible. */
  int getMaxCardinality();

  /* Returns a human-readable description of this type */
  String getComment();

  /* Returns a human-readable label of this type */
  String getLabel();
  
  
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
    
  /* Get the crate being worked on */
  RoCrate getCrate();

  /* Adds a single class */
  void addType(IType rdfsClass);

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

# Primitive Data Types

The following types from xsd are supported


| serialized as  | in library   | Usage                         |
|----------------|--------------|-------------------------------|
| xsd:integer    | INTEGER      | Any length of integer         |
| xsd:float      | FLOAT        | 32-bit floating point number  |
| xsd:double     | DOUBLE       | 64-bit floating point number  |
| xsd:decimal    | DECIMAL      | Arbitrary size decimal number |
| xsd:float      | FLOAT        | 32-bit floating point number  |
| xsd:datetime   | DATETIME     | Datetime                      |
| xsd:string     | STRING       | String                        |
| rdf:XMLLiteral | DATETIME     | XML                           |






# API Reference Implementation in Java

A working implementation of the API for Java (source and compiled) can be found
under: `./lib/src`.

A compiled jar can be found under: `./lib/java/bin`.
The dependencies are specified in the module's `build.gradle`
file: `./lib/java/src/build.gradle`.

# API Reference Examples in Java

Working examples of the API in java to read and write can be found
at: `./`, specifically the class
files

- `./lib/java/src/java/ch/eth/sis/rocrate/example/ReadExample.java`
- `./lib/java/src/java/ch/eth/sis/rocrate/example/WriteExample.java`



# Ongoing Work / Future Plans

## More serialization formats

We are planning to investigate different formats for serializing schema, metadata and ontological annotations.

## Including reference ontologies inside the RO-Crate

One issue is that ontologies in RO-Crate are links. These can be subject to link rot.
To address this, reference ontologies are included in the RO-Crate. This helps interpretability and allows it to be shared more easily.
Think of it as docker for data! 

## Separate files for schemas

At the moment, the graph inside the RO-Crate manifest can become large, unwieldy even. 
Moving this into one or more separate files and referencing them in the manifest should keep things more manageable. 



## Resolve schema.org types automagically

This allows using the schema.org without explicitly defining them in the schema. 
It makes it easier to use the convention that is common in RO-Crate. 


# People

- Andreas Meier (andreas.meier@ethz.ch)
- Juan Fuentes (juan.fuentes@id.ethz.ch)
