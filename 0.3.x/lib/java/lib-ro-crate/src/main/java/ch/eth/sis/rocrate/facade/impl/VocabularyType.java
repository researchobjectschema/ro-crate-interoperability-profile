package ch.eth.sis.rocrate.facade.impl;

import ch.eth.sis.rocrate.facade.IVocabularyTerm;
import ch.eth.sis.rocrate.facade.IVocabularyType;

import java.util.List;

public class VocabularyType implements IVocabularyType
{

    private final String id;

    private final String description;

    private final List<IVocabularyTerm> terms;

    public VocabularyType(String id, String description, List<IVocabularyTerm> terms)
    {
        this.id = id;
        this.terms = terms;
        this.description = description;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public List<IVocabularyTerm> getTerms()
    {
        return terms;
    }
}
