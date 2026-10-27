package ch.eth.sis.rocrate.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropertyType implements IPropertyType
{
    List<IType> domainIncludes;

    List<IType> rangeIncludes;

    List<IDataType> rangeIncludesDataType;

    List<IVocabularyType> rangeIncludesVocabularies;


    String id;

    List<String> ontologicalAnnotations = new ArrayList<>();


    String label;

    String comment;

    public PropertyType()
    {
        this.rangeIncludes = new ArrayList<>();
        this.rangeIncludesDataType = new ArrayList<>();
        this.ontologicalAnnotations = new ArrayList<>();
        this.rangeIncludesVocabularies = new ArrayList<>();

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
        Stream<String> a = rangeIncludes.stream().map(IType::getId);
        Stream<String> b = rangeIncludesDataType.stream().map(IDataType::getTypeName);
        Stream<String> c = rangeIncludesVocabularies.stream().map(IVocabularyType::getId);
        return Stream.concat(c, Stream.concat(a, b)).collect(Collectors.toList());


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
        this.rangeIncludesDataType = new ArrayList<>(types);
    }

    public void addDataType(IDataType type)
    {
        if (this.rangeIncludesDataType == null)
        {
            this.rangeIncludesDataType = new ArrayList<>();
        }
        if (!rangeIncludesDataType.contains(type))
        {
            rangeIncludesDataType.add(type);
        }

    }

    public void addVocabularyType(IVocabularyType type)
    {
        if (!this.rangeIncludesVocabularies.contains(type))
        {
            this.rangeIncludesVocabularies.add(type);
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

    public void addVocabularyType()
    {
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
                ", rangeIncludesDataType=" + rangeIncludesDataType +
                ", rangeIncludesVocabularies=" + rangeIncludesVocabularies +
                ", id='" + id + '\'' +
                ", ontologicalAnnotations=" + ontologicalAnnotations +
                ", label='" + label + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
