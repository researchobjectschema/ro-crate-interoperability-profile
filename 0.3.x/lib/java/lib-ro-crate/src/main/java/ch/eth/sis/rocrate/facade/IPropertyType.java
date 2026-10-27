package ch.eth.sis.rocrate.facade;

import java.util.List;

public interface IPropertyType
{
    /* Returns the ID of this property type */
    String getId();

    /* Return possible values for the subject of this property type */
    List<IType> getDomain();

    /* Return possible values for the object of this property type */
    List<String> getRange();

    /* Returns the ontological annotations of this property type */
    List<String> getOntologicalAnnotations();

    /* Returns a human-readable description of this type */
    String getComment();

    /* Returns a human-readable label of this type */
    String getLabel();

}
