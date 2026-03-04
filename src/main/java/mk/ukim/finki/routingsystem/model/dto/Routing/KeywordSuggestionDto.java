package mk.ukim.finki.routingsystem.model.dto.Routing;


public record KeywordSuggestionDto(

        String word,
        Integer count,
        String departmentKey,
        String message
) { }
