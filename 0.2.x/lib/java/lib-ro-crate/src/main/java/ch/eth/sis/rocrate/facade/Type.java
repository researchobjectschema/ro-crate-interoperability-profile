package ch.eth.sis.rocrate.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Type implements IType
{
    String id;

    String type;

    List<String> subClassOf;

    List<String> ontologicalAnnotations;

    List<PropertyType> rdfsProperties;

    String comment;

    String label;

    List<IRestriction> restrictions;

    public Type()
    {
        this.subClassOf = new ArrayList<>();
        this.ontologicalAnnotations = new ArrayList<>();
        this.rdfsProperties = new ArrayList<>();
        this.restrictions = new ArrayList<>();

    }

    public String getId()
    {
        return id;
    }

    @Override
    public List<String> getSubClassOf()
    {
        return subClassOf;
    }

    @Override
    public List<String> getOntologicalAnnotations()
    {
        return ontologicalAnnotations;
    }

    @Override
    public String getComment()
    {
        return null;
    }

    @Override
    public String getLabel()
    {
        return null;
    }

    @Override
    public List<IRestriction> getResstrictions()
    {
        return restrictions;
    }

    /**
     * This is a convenience method for adding a property to a class.
     *
     */
    public void addProperty(PropertyType rdfsProperty)
    {
        List<IType> domainIncludes = rdfsProperty.getDomainIncludes();
        if (domainIncludes == null)
        {
            domainIncludes = new ArrayList<>();
            rdfsProperty.setDomainIncludes(domainIncludes);
        }
        if (id == null)
        {
            throw new IllegalArgumentException("Class id is null");
        }
        domainIncludes.add(this);
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getType()
    {
        return "rdfs:Class";
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setSubClassOf(List<String> subClassOf)
    {
        this.subClassOf = subClassOf;
    }

    public void setOntologicalAnnotations(List<String> ontologicalAnnotations)
    {
        this.ontologicalAnnotations = ontologicalAnnotations;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void addRestriction(IRestriction restriction)
    {
        this.restrictions.add(restriction);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Type type = (Type) o;
        return Objects.equals(id, type.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    @Override
    public String toString()
    {
        return "Type{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", subClassOf=" + subClassOf +
                ", ontologicalAnnotations=" + ontologicalAnnotations +
                ", rdfsProperties=" + rdfsProperties +
                ", comment='" + comment + '\'' +
                ", label='" + label + '\'' +
                ", restrictions=" + restrictions +
                '}';
    }
}
