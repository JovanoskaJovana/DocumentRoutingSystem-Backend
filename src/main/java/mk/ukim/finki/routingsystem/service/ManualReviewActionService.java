package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.Routing.KeywordSuggestionDto;


import java.util.List;

public interface ManualReviewActionService {


    List<KeywordSuggestionDto> suggestKeyword(Long companyId, Long departmentId);

    List<KeywordSuggestionDto> suggestAllKeywords(Long companyId);

}
