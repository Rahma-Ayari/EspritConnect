package tn.esprit.espritconnect2.Repository.emailBackOffice;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.MailingListMember;

import java.util.List;

public interface MailingListMemberRepository extends JpaRepository<MailingListMember, Long> {

    List<MailingListMember> findByMailingList_Id(Long mailingListId);

    void deleteByMailingList_Id(Long mailingListId);
}