package com.bitis.luckydraw.service;

import com.bitis.luckydraw.model.Campaign;
import com.bitis.luckydraw.model.SystemAuditLog;
import com.bitis.luckydraw.repository.CampaignRepository;
import com.bitis.luckydraw.repository.SystemAuditLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CampaignStatusScheduler {

    private final CampaignRepository campaignRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;

    public CampaignStatusScheduler(CampaignRepository campaignRepository, SystemAuditLogRepository systemAuditLogRepository) {
        this.campaignRepository = campaignRepository;
        this.systemAuditLogRepository = systemAuditLogRepository;
    }

    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút ở giây số 0
    public void autoUpdateCampaignStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Campaign> campaigns = campaignRepository.findAll();
        for (Campaign campaign : campaigns) {
            // Tìm các chiến dịch chưa Kết thúc (khác 2) nhưng đã quá hạn
            if (campaign.getTrangThai() != null && campaign.getTrangThai() != 2) {
                if (campaign.getNgayKetThuc() != null && campaign.getNgayKetThuc().isBefore(now)) {
                    campaign.setTrangThai(2); // 2: Đã kết thúc
                    campaignRepository.save(campaign);

                    // Ghi log
                    SystemAuditLog log = new SystemAuditLog();
                    log.setStaffId(0L); // System
                    log.setActionType("UPDATE");
                    log.setTargetTable("campaign");
                    log.setTargetRecordId(campaign.getMaChienDich());
                    log.setDescription("Hệ thống tự động chuyển trạng thái chiến dịch sang Đã kết thúc (do hết thời hạn)");
                    log.setIpAddress("127.0.0.1");
                    systemAuditLogRepository.save(log);
                    
                    continue; // Chuyển sang chiến dịch tiếp theo
                }
            }
            
            // Tìm các chiến dịch đang Tạm ngưng (0) và vừa mới đến giờ bắt đầu (trong vòng 5 phút qua)
            if (campaign.getTrangThai() != null && campaign.getTrangThai() == 0) {
                if (campaign.getNgayBatDau() != null && 
                    !campaign.getNgayBatDau().isAfter(now) &&
                    campaign.getNgayBatDau().isAfter(now.minusMinutes(5))) {
                    
                    campaign.setTrangThai(1); // 1: Hoạt động
                    campaignRepository.save(campaign);

                    // Ghi log
                    SystemAuditLog log = new SystemAuditLog();
                    log.setStaffId(0L); // System
                    log.setActionType("UPDATE");
                    log.setTargetTable("campaign");
                    log.setTargetRecordId(campaign.getMaChienDich());
                    log.setDescription("Hệ thống tự động Kích hoạt chiến dịch (đã đến thời gian bắt đầu)");
                    log.setIpAddress("127.0.0.1");
                    systemAuditLogRepository.save(log);
                }
            }
        }
    }
}
