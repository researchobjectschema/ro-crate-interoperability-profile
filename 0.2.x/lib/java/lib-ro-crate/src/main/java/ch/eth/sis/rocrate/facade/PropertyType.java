package ch.eth.sis.rocrate.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropertyType implements IPropertyType
{
    List<IType> domainIncludes;

    List<IType> rangeIncludes;

    List<IDataType> rangeeIndlucesDataType;

    String id;

    List<String> ontologicalAnnotations = new ArrayList<>();


    String label;

    String comment;

    public PropertyType()
    {
        this.rangeIncludes = new ArrayList<>();
        this.rangeeIndlucesDataType = new ArrayList<>();
        this.ontologicalAnnotations = new ArrayList<>();

    }

    public List<IType> getDomainIncludes()
    {
        return domainIncludes;
    }

    public void setDomainIncludes(List<IType> domainIncludes)
    {
        this.domainIncludes = domainIncludes;
    }


    public String getId()
    {
        return id;
    }

    @Override
    public List<IType> getDomain()
    {
        return getDomainIncludes();
    }

    @Override
    public List<String> getRange()
    {
        System.out.println(this.id);
        Stream<String> a = rangeIncludes.stream().map(x -> x.getId());
        Stream<String> b = rangeeIndlucesDataType.stream().map(x -> x.getTypeName());
        return Stream.concat(a, b).collect(Collectors.toList());


    }

    @Override
    public List<String> getOntologicalAnnotations()
    {
        return ontologicalAnnotations;
    }

    @Override
    public String getComment()
    {
        return comment;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setOntologicalAnnotations(List<String> ontologicalAnnotations)
    {
        this.ontologicalAnnotations = ontologicalAnnotations;
    }

    public void setTypes(List<IDataType> types)
    {
        this.rangeeIndlucesDataType = new ArrayList<>(types);
    }

    public void addDataType(IDataType type)
    {
        if (this.rangeeIndlucesDataType == null)
        {
            this.rangeeIndlucesDataType = new ArrayList<>();
        }
        if (!rangeeIndlucesDataType.contains(type))
        {
            rangeeIndlucesDataType.add(type);
        }

    }

    public void addType(IType type)
    {
        if (this.rangeIncludes == null)
        {
            this.rangeIncludes = new ArrayList<>();
        }
        if (!this.rangeIncludes.contains(type))
        {
            this.rangeIncludes.add(type);
        }
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @Override
    public String toString()
    {
        return "PropertyType{" +
                "domainIncludes=" + domainIncludes +
                ", rangeIncludes=" + rangeIncludes +
                ", rangeeIndlucesDataType=" + rangeeIndlucesDataType +
                ", id='" + id + '\'' +
                ", ontologicalAnnotations=" + ontologicalAnnotations +
                ", label='" + label + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
