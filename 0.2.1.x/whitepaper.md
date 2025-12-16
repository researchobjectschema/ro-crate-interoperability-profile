# RO-Crate Schema Plus - Origins, Specification, Implementation and Considerations

Juan Fuentes - Principal Software Developer, Scientific IT Services, ETHZ
Andreas Meier - Principal Software Developer, Scientific IT Services, ETHZ

This document guides the reader through the complete thought process done to reach the current RO-Crate Schema Plus specification.
This is done to explain its inner workings to less knowledgeable readers while also allowing more knowledgeable ones to provide feedback.

## Origins
[RO-Crate Schema Plus](http://researchobjectschema.org/), is born from the simple idea of bundling not only files and metadata related to those files, something RO-Crate already does, but also the schema definition of such metadata.

At the end of 2024, representatives of Scientific Information Services at ETHZ, along with representatives from other ETHZ institutes such as PSI and EMPA, contacted the RO-Crate community because they wanted to use RO-Crate to exchange data and metadata together without the need for custom profiles.

### Why is this necessary?
This is not a new idea. Readers may be familiar with formats like CSV. When opening a CSV file, users not only need to guess what the content separators are, but also what the data types of the columns are, and, if several CSV files are present, how they are connected to each other. To **stop guessing** [CSVW](https://csvw.org/) was created, providing a secondary file that answers those questions.

The main goal of RO-Crate Schema Plus is to provide the same kind of additional information within the ro-crate-metadata.json file.

### JSON-LD limitations
RO-Crate provides a file, **ro-crate-metadata.json**, written using JSON-LD. By default, it uses [schema.org]([http://schema.org) types, along with some types added by RO-Crate itself.

RO-Crate uses JSON-LD to represent its metadata and, as a result, also shares its limitations. JSON-LD uses a context to provides **semantics**, can describe you what things are, but it does not define their schema fields, data types, relationships, and so on. In other words **semantics do not substitute schema**.

RO-Crate provides a file, ro-crate-metadata.json, written using JSON-LD. By default, it uses schema.org types, along with some types added by RO-Crate itself.

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

In the example above, the provided URL in the context leads, in this case, to a second document [https://w3id.org/ro/crate/1.1/context](https://w3id.org/ro/crate/1.1/context). This second document provides another context, this time leading to schema.org URLs. Unfortunately, URLs such as ["http://schema.org/CreativeWork"]("http://schema.org/CreativeWork")  are not **machine-actionable** and may no longer be available when a user attempts to access them, a problem known as **link rot**.

### Community Segregation
The shortcomings of the format illustrated in the previous section lead RO-Crate users to create their own contexts, with their own types and requirements, which are only understood by custom parsers. This is what RO-Crate refers to as [profiles](https://www.researchobject.org/ro-crate/profiles.html).

This could be considered a strength, as it makes the format highly extensible. However, in practice, it also represents a weakness: to exchange general information within a domain, a new profile may be required to add new types to the context, and that context must be made available. This can result in the creation of a new subcommunity that is more likely to become segregated from the main community.

## Specification
How can the limitations of JSON-LD and profiles be addressed? In other words, how could a schema or profile be specified within RO-Crate?

Since RO-Crate provides a single file, **ro-crate-metadata.json**, written using JSON-LD, any such schema or profile would need to be provided within that file and expressed using JSON-LD.

Looking at how schema.org distributes its schema, we find that it [downloads](https://schema.org/docs/developers.html) in a variety of formats, including a [JSON-LD download](https://schema.org/version/latest/schemaorg-current-https.jsonld). How is this possible? Because schema.org specifies its schema in RDF, which can be serialized in different formats, including JSON-LD.

How does this look in practice?

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

As the example above shows, schema.org provides a list of RDF classes and properties formatted as JSON-LD. This content could easily fit into the RO-Crate object graph inside ro-crate-metadata.json without requiring any changes to the current RO-Crate standard, making it backwards compatible.

## Implementation

## Writing the schema
Here comes the most challenging part: both class and property descriptions in RDF are too complex for end users to be expected to understand. Furthermore, RO-Crate imposes additional restrictions, like it's object graph should be a list of objects without nesting what makes definitions hardly human readable. These challenges make the use of extra tools unavoidable.

Ideally, users should rely on platforms that can import and export RO-Crate Schema Plus packages implementing this structure. Although the complexity shifts to system integrators, software libraries that provide an abstraction layer while hiding the underlying RDF details can greatly support adoption.

## Interpreting the schema
Consider two systems attempting to integrate with each other, each correctly embedding a schema within their RO-Crate Schema Plus metadata files. With different systems now able to exchange schemas dynamically, disparities between schemas may arise that other systems may or may not be able to interpret. Ultimately, each system has its own internal data model, which is translated more or less directly into what is shared.

When different schemas share similar information, a currently acceptable way to handle this is by defining semantic ontology equivalences:
- "owl:equivalentClass": means _“these two classes describe the same kind of thing.”_
- "owl:equivalentProperty": means “these two properties mean the same thing.”

In other words, **schema does not substitute semantics**.

In an ideal scenario, where a RO-Crate Schema Plus package includes the schema, metadata, and files, the only remaining task for system-to-system integration would be to define the equivalences between their schemas.

Finding these equivalences can become a point of contention during system integration, especially when there is no well-established ontology.

Since there does not appear to be a well-established framework for making such decisions, these choices often end up being more political than technical.

## Considerations

### When importing a RO-Crate Schema Plus package into another system, what is its expected behavior?
Different systems are intended for different purposes, and their behavior depends on both their purpose and their limitations. We classify systems under two criteria:

Purpose of the system:
- Specific: Registers only information from schemas and semantics it recognizes.
- General: Registers all kinds of information, even if it does not recognize the purpose of the schema or semantics.

System behavior when dealing with exceptional situations:
- Error: Any information found outside schemas and semantics that the system recognizes leads to an error, canceling the entire registration.
- Ignore: Any information found outside schemas and semantics that the system recognizes is ignored, resulting in a partial registration.

This behavior is not limited to RO-Crate Schema Plus but applies to any integration using a common format. As such, it is not defined by RO-Crate Schema Plus.

### Packaging of data
RO-Crate Schema Plus does not define how data should be packaged within RO-Crate.

Our current interpretation of the RO-Crate standard is that it already establishes that CreativeWork, DataSet, etc., are entities with hasPart, and that they own the data.

To attach data to an object in the graph, such an object class should inherit from or intersect with those classes.

## Acknowledgments

RO-Crate Schema Plus specification: Andreas Meier (SIS, ETHZ), Juan Fuentes (SIS, ETHZ)

RO-Crate Schema Plus Reference Java library: Andreas Meier (SIS, ETHZ)

RO-Crate Schema Plus Python Library:  Pascal Su (EMPA), Simone Baffelli (EMPA)

SciCat and SciLog RO-Crate Schema Plus ongoing integrations: Carlo Minotti (PSI), Frédéric Pontier (PSI), Omkar Zade (PSI)

Aiida RO-Crate Schema Plus planned integration: Edan Bainglass (PSI)