# RO-Crate Schema Plus - Origins, Specification, Implementation and Considerations

Juan Fuentes - Principal Software Developer, Scientific IT Services, ETHZ

This document guides the reader through the complete thought process done to reach the current RO-Crate Schema Plus specification.
This is done in one hand to attempt to explain its inner works to the less knowledgeable and on the another to allow the most knowledgeable to provide feedback.

## Origins
[RO-Crate Schema Plus](http://researchobjectschema.org/), is born from the simple idea of bundling not only files and metadata related to those files, something RO-Crate already does, but also the schema definition of such metadata.

At the end of 2024, Representatives of Scientific Information Services from the ETHZ and other representatives of ETHZ institutes like PSI and EMPA contacted the RO-Crate community since they wanted to use RO-Crate to exchange data and metadata together without the need of custom Profiles.

### Why is this necessary?
This is not a new idea, readers may be familiar with formats like CSV, when opening a CSV the user not only need to **guess** what are the content separators but also what are the data types of the columns of the CSV file, and if you have several CVS files, how are they connected between them. To **stop guessing** [CSVW](https://csvw.org/) was born, providing a secondary file that answers those questions.

The main RO-Crate Schema Plus goal is to provide the same extra information within the **ro-crate-metadata.json** file.

### JSON-LD limitations
RO-Crate provides a file, **ro-crate-metadata.json**, written using JSON-LD, it uses by default [schema.org]([http://schema.org) types plus some added by RO-Crate itself.

RO-Crate uses JSON-LD to represent its metadata and, as a result, also shares its limitations. JSON-LD provides a context, this context provides **semantics**, can tell you what things are, but it doesn't tell you their schema fields, data types, relationships, etc. In other words **semantics don't substitute schema**.

This shortcomings are best illustrated with an example.

*Example from [RO-Crate Specification](https://www.researchobject.org/ro-crate/specification/1.1/data-entities#example-linking-to-a-file-and-folders):*

```json
{ "@context": "https://w3id.org/ro/crate/1.1/context",
  "@graph": [
    {
      "@type": "CreativeWork",
      "@id": "ro-crate-metadata.json",
      "conformsTo": {"@id": "https://w3id.org/ro/crate/1.1"},
      "about": {"@id": "./"}
    },  
    {
      "@id": "./",
      "@type": [
        "Dataset"
      ],
      "hasPart": [
        {
          "@id": "cp7glop.ai"
        },
        {
          "@id": "lots_of_little_files/"
        }
      ]
    },
    {
      "@id": "cp7glop.ai",
      "@type": "File",
      "name": "Diagram showing trend to increase",
      "contentSize": "383766",
      "description": "Illustrator file for Glop Pot",
      "encodingFormat": "application/pdf"
    },
    {
      "@id": "lots_of_little_files/",
      "@type": "Dataset",
      "name": "Too many files",
      "description": "This directory contains many small files, that we're not going to describe in detail."
    }
  ]
}
```

On the example above, the provided URL on the context leads on this case to a second document [https://w3id.org/ro/crate/1.1/context](https://w3id.org/ro/crate/1.1/context). This second document provides another context, this time leading to schema.org URLs. Sadly URLs like ["http://schema.org/CreativeWork"]("http://schema.org/CreativeWork")  are not only **not machine actionable** but may be gone for the time a user wants to read those URLs, the so called **link rot** problem.

### Community Segregation
The shortcomings of the format illustrated on the previous section leads RO-Crate users to need to create their own contexts with their own types and requirements only custom parsers have knowledge about. This is what RO-Crate labels as [profiles](https://www.researchobject.org/ro-crate/profiles.html).

This could be considered a strength because it makes the format quite extensible, but in practice also makes it a weakness, to exchange general information within a domain a new Profile may be required to add new types to the context and such context would need to be available, creating a new sub community that will more likely be segregated from the main community.

## Specification
How can JSON-LD limitations and profile limitations be addressed? Or in other words, how could a schema/profile be specified within RO-Crate?

Since RO-Crate provides a single file, **ro-crate-metadata.json**, written using JSON-LD, it would be a requirement to be provided within such file and using JSON-LD.

Quickly looking at how schema.org distributes its schema, we find it [downloads](https://schema.org/docs/developers.html) using a variety of formats, including a [JSON-LD download](https://schema.org/version/latest/schemaorg-current-https.jsonld). How can they manage this? Because schema.org specifies its schema in RDF that is serializable in different formats including JSON-LD.

How this looks in practice?

*Example from [JSON-LD download](https://schema.org/version/latest/schemaorg-current-https.jsonld).*

```json
...
{
      "@id": "schema:CreativeWork",
      "@type": "rdfs:Class",
      "rdfs:comment": "The most generic kind of creative work, including books, movies, photographs, software programs, etc.",
      "rdfs:label": "CreativeWork",
      "rdfs:subClassOf": {
        "@id": "schema:Thing"
      },
      "schema:contributor": {
        "@id": "https://schema.org/docs/collab/rNews"
      }
}
...
{
      "@id": "schema:description",
      "@type": "rdf:Property",
      "owl:equivalentProperty": {
        "@id": "dcterms:description"
      },
      "rdfs:comment": "A description of the item.",
      "rdfs:label": "description",
      "schema:domainIncludes": {
        "@id": "schema:Thing"
      },
      "schema:rangeIncludes": [
        {
          "@id": "schema:Text"
        },
        {
          "@id": "schema:TextObject"
        }
      ]
...
}
```

As the example above shows, schema.org provides a list of RDF classes and properties formatted as JSON-LD. This content could easily fit into the RO-Crate object graph inside **ro-crate-metadata.json**, without additions to the current RO-Crate standard, so this addition is backwards compatible.

## Implementation

## Writing the schema
Here comes the most challenging part: both class and property descriptions in RDF are too complex for end users to be expected to understand. Furthermore if you would understand them RO-Crate imposes additional restrictions, like it's object graph should be a list of objects without nesting what makes definitions hardly human readable. This challenges make the use of extra tools unavoidable.

Ideally, users should rely on platforms that can import and export RO-Crate Schema Plus packages implementing this structure. Even though the complexity shifts to the system integrators of this extension, software libraries that provide an abstraction layer while hiding the underlying RDF details can greatly support adoption.

## Interpreting the schema
Considering two systems trying to integrate with each other, implementing correctly embedding schema withing their RO-Crate Schema Plus metadata files.  Now with different systems having the possibility to exchange different schemas on the fly, a disparity of schemas that other systems may or not be able to interpretate surfaces, at the end every system has its own internal data model that will be translated more or less directly to what is shared.

Assuming different schemas sharing similar information a currently acceptable way to deal with is by setting semantic ontology equivalencies.  
- "owl:equivalentClass" : means _“these two classes describe the same kind of thing.”_
- "owl:equivalentProperty" means “these two properties mean the same thing.”

In other words, **schema doesn't substitute semantics**.

In an ideal scenario, where a RO-Crate Schema Plus package includes the schema, metadata, and files, the only remaining task for system-to-system integration would be to define the equivalences between their schemas.

Finding these equivalencies can become now a point of contention when doing system integrations where there is not a well stablished ontology.

Since it seems to not to be a well stablished framework to make such choices, this choices end up being more political than technical choices.

## Considerations

### When importing a RO-Crate Schema Plus package by other system what is its expected behavior?
Different systems are intended for different purposes and their behavior depends on both their purpose and their limitations. We would classify systems under two criteria:

Purpose of the System:
- Specific: Registers only information from schemas and semantics recognize.
- General: Register all kinds of information, even if they don't recognize the purpose of their schema or semantics. 

System behavior when dealing with Exceptional Situations:
- Error: Any information found outside schemas and semantics the system recognizes leads to an error, canceling the complete registration.
- Ignore: Any information found outside schemas and semantics the system recognizes is ignored, completing a partial registration.

This is not a behavior limited to RO-Crate Schema plus, but to any kind of integration using a common format. As such, this is not defined by RO-Crate Schema plus.

### Packaging of data
RO-Crate schema plus doesn't define how to package data within RO-Crate.

Our current interpretation of the RO-Crate standard is that it already establishes that CreativeWork, DataSet, etc. are entities with hasPart and that they own the data. 

To attach data to an object in the graph, such object class should inherit from or intersect with those classes.

## Acknowledgments

RO-Crate Schema Plus specification: Andreas Meier (SIS, ETHZ), Juan Fuentes (SIS, ETHZ)

RO-Crate Schema Plus Reference Java library: Andreas Meier (SIS, ETHZ)

RO-Crate Schema Plus Python Library:  Pascal Su (EMPA) and Simone Baffelli (EMPA)

SciCat and SciLog RO-Crate Schema Plus ongoing integrations: Carlo Minotti (PSI), Frédéric Pontier (PSI), Omkar Zade (PSI)

Aiida RO-Crate Schema Plus planned integration: Edan Bainglass (PSI)