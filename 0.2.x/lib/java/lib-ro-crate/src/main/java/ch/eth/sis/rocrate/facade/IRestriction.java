package ch.eth.sis.rocrate.facade;

public interface IRestriction
{

    String getId();

    IPropertyType getPropertyType();

    int getMinCardinality();

    int getMaxCardinality();
}
