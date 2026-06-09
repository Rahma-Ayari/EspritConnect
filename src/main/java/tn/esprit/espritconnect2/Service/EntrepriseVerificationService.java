package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.EntrepriseDocumentDTO;
import tn.esprit.espritconnect2.DTO.EntrepriseDocumentRequestDTO;
import tn.esprit.espritconnect2.DTO.EntrepriseVerificationDTO;
import tn.esprit.espritconnect2.Entitie.Entreprise;
import tn.esprit.espritconnect2.Entitie.EntrepriseDocument;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;
import tn.esprit.espritconnect2.exception.BusinessRuleException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.EntrepriseDocumentRepository;
import tn.esprit.espritconnect2.Repository.EntrepriseRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EntrepriseVerificationService {

    private static final Set<String> REQUIRED_TYPES = Set.of(
            "REGISTRE_COMMERCE",
            "CERTIFICAT_FISCAL",
            "ID_REPRESENTANT"
    );

    private final EntrepriseRepository entrepriseRepository;
    private final EntrepriseDocumentRepository documentRepository;

    public EntrepriseVerificationDTO getStatus(Long entrepriseId) {
        Entreprise e = load(entrepriseId);
        return toDto(e);
    }

    @Transactional
    public EntrepriseDocumentDTO addDocument(Long entrepriseId, EntrepriseDocumentRequestDTO dto) {
        Entreprise e = load(entrepriseId);
        EntrepriseDocument doc = EntrepriseDocument.builder()
                .entreprise(e)
                .documentType(dto.getDocumentType().trim().toUpperCase())
                .fileName(dto.getFileName())
                .fileUrl(dto.getFileUrl() != null ? dto.getFileUrl() : "uploaded://" + dto.getFileName())
                .build();
        if (e.getVerificationStatus() == VerificationStatus.VERIFIED) {
            e.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
        } else if (e.getVerificationStatus() == VerificationStatus.DOCUMENTS_REQUIRED
                || e.getVerificationStatus() == VerificationStatus.REJECTED) {
            e.setVerificationStatus(VerificationStatus.DOCUMENTS_REQUIRED);
        }
        entrepriseRepository.save(e);
        return toDocDto(documentRepository.save(doc));
    }

    @Transactional
    public EntrepriseVerificationDTO submitForReview(Long entrepriseId) {
        Entreprise e = load(entrepriseId);
        List<EntrepriseDocument> docs = documentRepository.findByEntrepriseIdEntrepriseOrderByUploadedAtDesc(entrepriseId);
        boolean hasAll = REQUIRED_TYPES.stream()
                .allMatch(type -> docs.stream().anyMatch(d -> type.equals(d.getDocumentType())));
        if (!hasAll) {
            throw new BusinessRuleException(
                    "Documents requis manquants : registre de commerce, certificat fiscal et piece d'identite du representant.");
        }
        e.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
        e.setVerificationNotes(null);
        entrepriseRepository.save(e);
        return toDto(e);
    }

    public boolean canPostOffers(Entreprise e) {
        return Boolean.TRUE.equals(e.getValide())
                && e.getVerificationStatus() == VerificationStatus.VERIFIED;
    }

    private Entreprise load(Long id) {
        return entrepriseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entreprise introuvable avec id: " + id));
    }

    private EntrepriseVerificationDTO toDto(Entreprise e) {
        List<EntrepriseDocumentDTO> docs = documentRepository
                .findByEntrepriseIdEntrepriseOrderByUploadedAtDesc(e.getIdEntreprise())
                .stream()
                .map(this::toDocDto)
                .toList();
        return EntrepriseVerificationDTO.builder()
                .entrepriseId(e.getIdEntreprise())
                .entrepriseNom(e.getNom())
                .valide(e.getValide())
                .verificationStatus(e.getVerificationStatus())
                .verificationNotes(e.getVerificationNotes())
                .documentsCount(docs.size())
                .canPostOffers(canPostOffers(e))
                .documents(docs)
                .build();
    }

    private EntrepriseDocumentDTO toDocDto(EntrepriseDocument d) {
        return EntrepriseDocumentDTO.builder()
                .id(d.getId())
                .documentType(d.getDocumentType())
                .fileName(d.getFileName())
                .fileUrl(d.getFileUrl())
                .uploadedAt(d.getUploadedAt())
                .build();
    }
}
