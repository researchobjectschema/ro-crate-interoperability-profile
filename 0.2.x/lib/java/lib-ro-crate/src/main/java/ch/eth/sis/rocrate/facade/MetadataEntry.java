package ch.eth.sis.rocrate.facade;

import edu.kit.datamanager.ro_crate.entities.AbstractEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;

public class MetadataEntry implements IMetadataEntry
{
    String id;

    Set<String> types;

    Map<String, Serializable> props;

    Map<String, List<String>> references;

    List<String> childrenIdentifiers = new ArrayList<>();

    List<String> parentIdentifiers = new ArrayList<>();

    AbstractEntity abstractEntity;

    Path path;

    List<DataEntity> fileEntitiesReferenced = List.of();

    public MetadataEntry()
    {
    }

    public MetadataEntry(String id, Set<String> types, Map<String, Serializable> props,
            Map<String, List<String>> references)
    {
        this.id = id;
        this.types = types;
        this.props = props;
        this.references = references;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public Map<String, Serializable> getValues()
    {
        return props;
    }

    @Override
    public Map<String, List<String>> getReferences()
    {
        return references;
    }

    @Override
    public Optional<Path> getFileOrDirectory()
    {
        return Optional.ofNullable(path);
    }

    public void setFileEntitiesReferenced(
            List<DataEntity> fileEntitiesReferenced)
    {
        this.fileEntitiesReferenced = fileEntitiesReferenced;
    }

    @Override
    public List<DataEntity> getDataEntitiesReferenced()
    {
        return fileEntitiesReferenced;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public Set<String> getTypes()
    {
        return types;
    }

    public void setTypes(Set<String> types)
    {
        this.types = types;
    }

    public void addChildIdentifier(String a)
    {
        childrenIdentifiers.add(a);
    }

    public void addParentIdentifier(String a)
    {
        parentIdentifiers.add(a);
    }

    public List<String> getChildrenIdentifiers()
    {
        return childrenIdentifiers;
    }

    public List<String> getParentIdentifiers()
    {
        return parentIdentifiers;
    }

    public void setProps(Map<String, Serializable> props)
    {
        this.props = props;
    }

    public void setReferences(Map<String, List<String>> references)
    {
        this.references = references;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MetadataEntry entry = (MetadataEntry) o;
        return Objects.equals(id, entry.id) && Objects.equals(types, entry.types);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, types);
    }

    @Override
    public String toString()
    {
        return "MetadataEntry{" +
                "id='" + id + '\'' +
                ", types=" + types +
                ", props=" + props +
                ", references=" + references +
                ", childrenIdentifiers=" + childrenIdentifiers +
                ", parentIdentifiers=" + parentIdentifiers +
                '}';
    }
}
