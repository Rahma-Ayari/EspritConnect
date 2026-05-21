package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.MailingList;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.MailingListMember;
import tn.esprit.espritconnect2.Repository.emailBackOffice.MailingListMemberRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.MailingListRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MailingListServiceImpl implements IMailingListService {

    private final MailingListRepository listRepo;
    private final MailingListMemberRepository memberRepo;

    @Override
    @Transactional
    public MailingListResponseDTO create(MailingListRequestDTO dto) {
        if (listRepo.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Une liste avec ce nom existe déjà");
        }
        MailingList l = new MailingList();
        l.setName(dto.getName());
        l.setDescription(dto.getDescription());
        return toListDto(listRepo.save(l));
    }

    @Override
    @Transactional
    public MailingListResponseDTO update(Long id, MailingListRequestDTO dto) {
        MailingList l = listRepo.findById(id).orElseThrow(() -> new RuntimeException("Liste introuvable"));
        l.setName(dto.getName());
        l.setDescription(dto.getDescription());
        return toListDto(listRepo.save(l));
    }

    @Override
    @Transactional(readOnly = true)
    public MailingListResponseDTO get(Long id) {
        return toListDto(listRepo.findById(id).orElseThrow(() -> new RuntimeException("Liste introuvable")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MailingListResponseDTO> list() {
        return listRepo.findAll().stream().map(this::toListDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MailingListMemberResponseDTO addMember(Long listId, MailingListMemberRequestDTO dto) {
        MailingList l = listRepo.findById(listId).orElseThrow(() -> new RuntimeException("Liste introuvable"));
        MailingListMember m = MailingListMember.builder()
                .mailingList(l)
                .email(dto.getEmail())
                .nom(dto.getNom())
                .userUuid(dto.getUserUuid())
                .build();
        return toMemberDto(memberRepo.save(m));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MailingListMemberResponseDTO> listMembers(Long listId) {
        if (!listRepo.existsById(listId)) throw new RuntimeException("Liste introuvable");
        return memberRepo.findByMailingList_Id(listId).stream().map(this::toMemberDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(Long memberId) {
        memberRepo.deleteById(memberId);
    }

    private MailingListResponseDTO toListDto(MailingList l) {
        long count = memberRepo.findByMailingList_Id(l.getId()).size();
        return MailingListResponseDTO.builder()
                .id(l.getId())
                .name(l.getName())
                .description(l.getDescription())
                .createdAt(l.getCreatedAt() != null ? l.getCreatedAt().toString() : null)
                .membersCount(count)
                .build();
    }

    private MailingListMemberResponseDTO toMemberDto(MailingListMember m) {
        return MailingListMemberResponseDTO.builder()
                .id(m.getId())
                .mailingListId(m.getMailingList().getId())
                .email(m.getEmail())
                .nom(m.getNom())
                .userUuid(m.getUserUuid())
                .build();
    }
}