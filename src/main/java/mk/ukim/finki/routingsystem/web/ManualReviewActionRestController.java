package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.Routing.KeywordSuggestionDto;
import mk.ukim.finki.routingsystem.security.EmployeePrincipal;
import mk.ukim.finki.routingsystem.service.ManualReviewActionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class ManualReviewActionRestController {

    private final ManualReviewActionService manualReviewActionService;

    public ManualReviewActionRestController(ManualReviewActionService manualReviewActionService) {
        this.manualReviewActionService = manualReviewActionService;
    }

    @GetMapping
    ResponseEntity<List<KeywordSuggestionDto>> getKeywordSuggestions(@AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        List<KeywordSuggestionDto> suggestions = manualReviewActionService.suggestAllKeywords(employeePrincipal.getCompanyId());

        return ResponseEntity.status(HttpStatus.OK).body(suggestions);
    }
}
