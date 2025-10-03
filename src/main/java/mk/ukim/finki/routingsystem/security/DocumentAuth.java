package mk.ukim.finki.routingsystem.security;

import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import org.springframework.stereotype.Component;

@Component("documentAuth")
public class DocumentAuth {

    private final DocumentRepository documentRepository;

    public DocumentAuth(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public boolean canSign(Long documentId, Object principalObj) {

        EmployeePrincipal principal = extractPrincipal(principalObj);

        if (principal == null) {
            return false;
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        if (document == null) {
            return false;
        }

        boolean sameDepartment = document.getRoutedToDepartment() != null &&
                principal.getDepartmentId() != null &&
                document.getRoutedToDepartment().getId().equals(principal.getDepartmentId());

        boolean isSignatory = principal.getEmployeeType() == EmployeeType.SIGNATORY;

        return sameDepartment && isSignatory;
    }

    public boolean canDownload(Long documentId, Object principalObj) {

        EmployeePrincipal principal = extractPrincipal(principalObj);

        if (principal == null) {
            return false;
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        if (document == null) {
            return false;
        }


        boolean sameDepartment = document.getRoutedToDepartment() != null &&
                principal.getDepartmentId() != null &&
                document.getRoutedToDepartment().getId().equals(principal.getDepartmentId());

        boolean isRoutedTo = document.getRoutedToEmployees().stream()
                .allMatch(employee -> employee.getId().equals(principal.getEmployeeId()));

        return sameDepartment && isRoutedTo ;

    }

    private EmployeePrincipal extractPrincipal(Object principalObj) {
        if (principalObj instanceof EmployeePrincipal ep) return ep;
        if (principalObj instanceof org.springframework.security.core.Authentication auth
                && auth.getPrincipal() instanceof EmployeePrincipal ep) return ep;
        return null;
    }
}

