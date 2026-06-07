package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.*;

import java.util.List;

public interface IMailingListService {
    MailingListResponseDTO create(MailingListRequestDTO dto);
    MailingListResponseDTO update(Long id, MailingListRequestDTO dto);
    MailingListResponseDTO get(Long id);
    List<MailingListResponseDTO> list();

    MailingListMemberResponseDTO addMember(Long listId, MailingListMemberRequestDTO dto);
    List<MailingListMemberResponseDTO> listMembers(Long listId);
    void removeMember(Long memberId);
}