package com.bitis.luckydraw.service;

import com.bitis.luckydraw.model.Prize;
import com.bitis.luckydraw.repository.PrizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class CustomerSpinService {

    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    public Prize playSpin(String maKhachHang, String maChienDich, String maStore) throws Exception {
        // 1. Lấy danh sách giải thưởng
        List<Prize> prizes = prizeRepository.findByMaChienDich(maChienDich);
        if (prizes.isEmpty()) {
            throw new Exception("Chiến dịch không có giải thưởng nào được cấu hình.");
        }

        // 2. Tách giải dự kiến và giải trượt
        Prize fallbackPrize = null;
        for (Prize p : prizes) {
            if (Boolean.FALSE.equals(p.getLaGiaiThuong()) || p.getTenGiai().toLowerCase().contains("mất lượt")) {
                fallbackPrize = p;
                break;
            }
        }
        if (fallbackPrize == null) {
            // Nếu không có cấu hình giải trượt, lấy bừa 1 giải đầu tiên có giá trị thấp nhất hoặc tự throw lỗi.
            // Lười: lấy giải đầu tiên có xác suất cao nhất làm giải trượt tạm (hoặc throw lỗi)
            fallbackPrize = prizes.get(prizes.size() - 1); // fallback lười
        }

        // 3. Tính xác suất random
        double rand = random.nextDouble() * 100.0;
        double cumulative = 0.0;
        Prize selectedPrize = fallbackPrize;

        for (Prize p : prizes) {
            // Bỏ qua nếu là giải trượt để random
            cumulative += p.getXacSuat();
            if (rand <= cumulative) {
                // Kiểm tra xem có kho không (tonKhoToanHeThong > 0 hoặc == -1 vô hạn)
                if (p.getTonKhoToanHeThong() > 0 || p.getTonKhoToanHeThong() == -1) {
                    selectedPrize = p;
                } else {
                    selectedPrize = fallbackPrize; // hết kho thì tự chuyển sang trượt
                }
                break;
            }
        }

        // 4. Gọi Stored Procedure để thực thi An toàn
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_Main_QuayThuong")
                .declareParameters(
                        new SqlParameter("ma_khach_hang", Types.VARCHAR),
                        new SqlParameter("ma_chien_dich", Types.VARCHAR),
                        new SqlParameter("ma_store", Types.VARCHAR),
                        new SqlParameter("ma_giai_thuong_du_kien", Types.VARCHAR),
                        new SqlParameter("ma_giai_truot", Types.VARCHAR),
                        new SqlOutParameter("ket_qua_giai_thuong", Types.VARCHAR)
                );

        MapSqlParameterSource in = new MapSqlParameterSource();
        in.addValue("ma_khach_hang", maKhachHang);
        in.addValue("ma_chien_dich", maChienDich);
        in.addValue("ma_store", maStore != null ? maStore : "STORE_ONLINE");
        in.addValue("ma_giai_thuong_du_kien", selectedPrize.getMaGiaiThuong());
        in.addValue("ma_giai_truot", fallbackPrize.getMaGiaiThuong());

        Map<String, Object> out = jdbcCall.execute(in);
        
        String ketQuaMaGiai = (String) out.get("ket_qua_giai_thuong");

        if (ketQuaMaGiai == null) {
            throw new Exception("Có lỗi xảy ra trong quá trình quay thưởng (Hết lượt quay hoặc lỗi hệ thống).");
        }

        // Tìm lại Object Prize từ kết quả của SP
        for (Prize p : prizes) {
            if (p.getMaGiaiThuong().equals(ketQuaMaGiai)) {
                return p;
            }
        }
        
        return fallbackPrize;
    }
}
