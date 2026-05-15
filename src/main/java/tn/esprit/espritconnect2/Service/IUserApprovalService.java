package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.UserApprovalDTO;
import tn.esprit.espritconnect2.DTO.UserApprovalStatsDTO;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.List;
import java.util.UUID;

public interface IUserApprovalService {
    
    List<UserApprovalDTO> getPendingUsers();
    
    List<UserApprovalDTO> getPendingUsersByRole(Role role);
    
    List<UserApprovalDTO> searchPendingUsers(String search, Role role);
    
    UserApprovalDTO approveUser(UUID userId);
    
    void declineUser(UUID userId);
    
    void deleteUser(UUID userId);
    
    List<UserApprovalDTO> bulkApprove(List<UUID> userIds);
    
    void bulkDecline(List<UUID> userIds);
    
    UserApprovalStatsDTO getApprovalStats();
    
    List<UserApprovalDTO> getAllUsers();
    
    List<UserApprovalDTO> getApprovedUsers();
}
