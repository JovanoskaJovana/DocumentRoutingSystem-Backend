package mk.ukim.finki.routingsystem.security;

import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("documentAuth")
public class DocumentAuth {

    private final DocumentRepository documentRepository;

    public DocumentAuth(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public boolean canSign(Long documentId, Long companyId, Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof EmployeePrincipal employeePrincipal)) {
            return false;
        }

        Document document = documentRepository.findByIdAndAndCompany_Id(documentId, companyId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        if (document == null) {
            return false;
        }

        boolean sameDepartment = document.getRoutedToDepartment() != null &&
                employeePrincipal.getDepartmentId() != null &&
                document.getRoutedToDepartment().getId().equals(employeePrincipal.getDepartmentId());

        boolean isSignatory = employeePrincipal.getEmployeeType() == EmployeeType.SIGNATORY;

        return sameDepartment && isSignatory;
    }

}

