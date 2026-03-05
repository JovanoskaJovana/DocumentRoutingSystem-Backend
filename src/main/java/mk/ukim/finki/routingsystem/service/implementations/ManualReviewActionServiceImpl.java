package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.ManualReviewAction;
import mk.ukim.finki.routingsystem.model.dto.Routing.KeywordSuggestionDto;
import mk.ukim.finki.routingsystem.model.exceptions.DepartmentNotFoundException;
import mk.ukim.finki.routingsystem.repository.DepartmentRepository;
import mk.ukim.finki.routingsystem.repository.ManualReviewActionRepository;
import mk.ukim.finki.routingsystem.service.ManualReviewActionService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManualReviewActionServiceImpl implements ManualReviewActionService {

    private final int suggestionThreshold = 10;
    private final ManualReviewActionRepository manualReviewActionRepository;
    private final DepartmentRepository departmentRepository;

    public ManualReviewActionServiceImpl(ManualReviewActionRepository manualReviewActionRepository, DepartmentRepository departmentRepository) {
        this.manualReviewActionRepository = manualReviewActionRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<KeywordSuggestionDto> suggestKeyword(Long companyId, Long departmentId) {

        Map<String, Integer> keywordAppearance = new HashMap<>();
        List<ManualReviewAction> actions = manualReviewActionRepository.findAllByCompany_IdAndManualChosenDepartment_Id(companyId, departmentId);

        for (ManualReviewAction action : actions) {
            String[] words = (action.getDocumentTitle() + " " + action.getDocumentText())
                    .toLowerCase()
                    .replaceAll("[^\\w\\s]", " ")
                    .split("\\s+");

            for (String word : words) {
                if (!word.isBlank()) {
                    keywordAppearance.merge(word, 1, Integer::sum);
                }
            }
        }

        Department department = departmentRepository.findByIdAndCompany_Id(departmentId, companyId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

       return keywordAppearance.entrySet().stream()
                .filter(entry -> entry.getValue() >= suggestionThreshold)
                .map(entry -> new KeywordSuggestionDto(
                        entry.getKey(),
                        entry.getValue(),
                        department.getDepartmentKey(),
                        "'" + entry.getKey() + "' appeared " + entry.getValue() + " times in manually routed documents for " + department.getDepartmentKey() + " — consider adding it as a keyword"
                ))
                .toList();
    }

    @Override
    public List<KeywordSuggestionDto> suggestAllKeywords(Long companyId) {
        return departmentRepository.findAllByCompany_Id(companyId)
                .stream()
                .flatMap(dept -> suggestKeyword(companyId, dept.getId()).stream())
                .toList();
    }
}
