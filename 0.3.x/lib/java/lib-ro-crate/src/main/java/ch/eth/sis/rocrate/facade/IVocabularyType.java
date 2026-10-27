package ch.eth.sis.rocrate.facade;

import java.util.List;

public interface IVocabularyType
{
    String getId();

    String getDescription();

    List<IVocabularyTerm> getTerms();

}
