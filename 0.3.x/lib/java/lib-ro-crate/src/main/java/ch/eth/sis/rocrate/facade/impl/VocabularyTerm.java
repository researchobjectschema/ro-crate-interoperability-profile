package ch.eth.sis.rocrate.facade.impl;

import ch.eth.sis.rocrate.facade.IVocabularyTerm;

public class VocabularyTerm implements IVocabularyTerm
{
    private final String id;

    private final String label;

    private final String description;

    public VocabularyTerm(String id, String label, String description)
    {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public String getDescription()
    {
        return description;
    }
}
