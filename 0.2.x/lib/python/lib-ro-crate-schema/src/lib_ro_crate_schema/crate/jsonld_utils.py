import tempfile
import json
from pathlib import Path
from lib_ro_crate_schema.crate.rdf import BASE
from lib_ro_crate_schema.crate.schema_facade import SchemaFacade
import pyld
from rocrate.rocrate import ROCrate
from rdflib import Graph

def get_context(g: Graph) -> dict[str, str]:
    """
    Dynamically generates JSON-LD @context based on the actual vocabularies and properties
    used in the RDF graph. Analyzes predicates, types, and values to determine needed namespaces.
    """
    from urllib.parse import urlparse
    import re
    
    context = {}
    used_namespaces = {}
    property_contexts = {}
    
    # Standard RO-Crate context base
    ro_crate_base = "https://w3id.org/ro/crate/1.1/context"
    
    # Collect all URIs used as predicates, types, and objects
    all_uris = set()
    
    for s, p, o in g:
        # Add predicate URIs
        if str(p).startswith('http'):
            all_uris.add(str(p))
        
        # Add type URIs from rdf:type triples
        if str(p) == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" and str(o).startswith('http'):
            all_uris.add(str(o))
            
        # Add object URIs that are references
        if str(o).startswith('http'):
            all_uris.add(str(o))
    
    # Analyze URIs to extract namespaces and common patterns
    namespace_prefixes = {
        "https://schema.org/": "schema",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#": "rdf", 
        "http://www.w3.org/2000/01/rdf-schema#": "rdfs",
        "http://www.w3.org/2002/07/owl#": "owl",
        "http://www.w3.org/2001/XMLSchema#": "xsd",
        "http://openbis.org/": "openbis",
        "http://example.com/": "base"
    }
    
    # Track which namespaces are actually used
    unknown_namespaces = {}  # Track URIs that don't match predefined namespaces
    
    for uri in all_uris:
        found_match = False
        # Check against predefined namespaces first
        for namespace_uri, prefix in namespace_prefixes.items():
            if uri.startswith(namespace_uri):
                used_namespaces[prefix] = namespace_uri
                found_match = True
                break
        
        # If no match found, this might be an unknown namespace
        if not found_match and uri.startswith('http'):
            # Extract potential namespace (everything up to the last '/' or '#')
            if '/' in uri:
                # Find the last meaningful separator
                parts = uri.split('/')
                if len(parts) > 3:  # http://domain.com/something
                    potential_ns = '/'.join(parts[:-1]) + '/'
                    # Only consider it if it looks like a namespace (has domain + path)
                    if '.' in parts[2]:  # Has a domain with dots
                        unknown_namespaces[potential_ns] = unknown_namespaces.get(potential_ns, 0) + 1
    
    # Auto-detect unknown namespaces that appear frequently enough
    for ns_uri, count in unknown_namespaces.items():
        if count >= 2:  # Only add namespaces used at least twice
            # Generate a prefix from the domain
            try:
                from urllib.parse import urlparse
                parsed = urlparse(ns_uri)
                domain_parts = parsed.netloc.split('.')
                
                # Use first part of domain as prefix (e.g., pokemon.org -> pokemon)
                if len(domain_parts) >= 2:
                    potential_prefix = domain_parts[0]
                    
                    # Make sure prefix doesn't conflict with existing ones
                    counter = 1
                    final_prefix = potential_prefix
                    while final_prefix in used_namespaces:
                        final_prefix = f"{potential_prefix}{counter}"
                        counter += 1
                    
                    used_namespaces[final_prefix] = ns_uri
                    
            except Exception:
                # If parsing fails, skip this namespace
                continue
    
    # Add base RO-Crate context first
    context = [ro_crate_base]
    
    # Add discovered namespaces as a second context layer
    namespace_context = {}
    
    # Add used vocabularies
    for prefix, namespace_uri in used_namespaces.items():
        namespace_context[prefix] = namespace_uri
    
    if namespace_context:
        context.append(namespace_context)
    
    # If no custom namespaces found, return simple context
    if len(context) == 1:
        return ro_crate_base
    
    return context


def add_schema_to_crate(schema: SchemaFacade, crate: ROCrate) -> ROCrate:
    """
    Emits triples from schema, builds a graph, converts to JSON-LD with dynamic context,
    and adds objects to the crate. Context is generated based on actual vocabulary usage.
    """
    metadata_graph = schema.to_graph()
    
    # Generate dynamic context based on actual content
    dynamic_context = get_context(metadata_graph)
    
    # Extract additional context (non-standard RO-Crate namespaces/properties)
    additional_context = {}
    if isinstance(dynamic_context, list) and len(dynamic_context) > 1:
        # Get the second layer which contains our custom namespaces
        additional_context = dynamic_context[1] if isinstance(dynamic_context[1], dict) else {}
    elif isinstance(dynamic_context, dict):
        additional_context = dynamic_context
    
    # Create serialization context with only namespace mappings (consistent with get_context)
    serialization_context = {
        "schema": "https://schema.org/",
        **additional_context,
    }
    
    try:
        # Serialize to JSON-LD with the combined context
        ld_ser = metadata_graph.serialize(format="json-ld", context=serialization_context)
        ld_obj = pyld.jsonld.json.loads(ld_ser)
        
    except Exception as e:
        print(f"Warning: Could not serialize with dynamic context, falling back to basic context: {e}")
        # Fallback to basic context
        basic_context = {"schema": "https://schema.org/"}
        ld_ser = metadata_graph.serialize(format="json-ld", context=basic_context)
        ld_obj = pyld.jsonld.json.loads(ld_ser)
    
    # Handle both @graph array and single object forms
    objects = ld_obj.get("@graph", [])
    if not objects and isinstance(ld_obj, dict) and "@id" in ld_obj:
        objects = [ld_obj]
    
    # Add each object in the graph to the crate
    for obj in objects:
        try:
            # Clean up objects that might cause issues with ROCrate
            cleaned_obj = {}
            for key, value in obj.items():
                if key == "@context":
                    continue  # Skip @context in individual objects
                elif isinstance(value, dict) and "@type" in value and "@value" in value:
                    # Handle typed literals that ROCrate might not like
                    cleaned_obj[key] = value["@value"]
                else:
                    cleaned_obj[key] = value
            
            if cleaned_obj and "@id" in cleaned_obj:  # Only add valid objects with IDs
                crate.add_jsonld(cleaned_obj)
        except Exception as e:
            # Skip objects that cause issues
            print(f"Warning: Could not add object {obj.get('@id', 'unknown')}: {e}")
            continue
    
    # Context is now handled at the ROCrate level via crate.metadata.extra_contexts
    # No need for post-processing enhancement here
    
    return crate
