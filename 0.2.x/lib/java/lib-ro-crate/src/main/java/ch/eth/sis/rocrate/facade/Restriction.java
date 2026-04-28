package ch.eth.sis.rocrate.facade;

public class Restriction implements IRestriction
{

    String id;


    IPropertyType propertyType;

    int minCardinality;

    int maxCardinality;

    public Restriction(String id, IPropertyType propertyType, int minCardinality,
            int maxCardinality)
    {
        this.id = id;
        this.propertyType = propertyType;
        this.minCardinality = minCardinality;
        this.maxCardinality = maxCardinality;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public IPropertyType getPropertyType()
    {
        return propertyType;
    }

    @Override
    public int getMinCardinality()
    {
        return minCardinality;
    }

    @Override
    public int getMaxCardinality()
    {
        return maxCardinality;
    }

}
