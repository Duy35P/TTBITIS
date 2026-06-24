import re

# 1. Update CampaignRepository
file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\repository\CampaignRepository.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()
if 'Optional<Campaign> findByMaChienDich(String maChienDich);' not in content:
    content = content.replace('}', '    Optional<Campaign> findByMaChienDich(String maChienDich);\n}')
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 2. Update PosService
file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\PosService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('campaignRepository.findById(cs.getMaChienDich())', 'campaignRepository.findByMaChienDich(cs.getMaChienDich())')
content = content.replace('findByIdChienDich', 'findByMaChienDich')
# customerTurnRepository.addCustomerTurnsSafe expects what?
# It takes (Long customerId, Long campaignId, ...). 
# Wait, addCustomerTurnsSafe signature in CustomerTurnRepository:
# @Query(value = "EXEC [dbo].[sp_AddCustomerTurns_Safe] :ma_khach_hang, :ma_chien_dich, :so_luong_cong, :nguon", nativeQuery = true)
# void addCustomerTurnsSafe(@Param("ma_khach_hang") String maKhachHang, @Param("ma_chien_dich") String maChienDich, @Param("so_luong_cong") Integer soLuong, @Param("nguon") String nguonThamChieu);
# Wait, CustomerTurnRepository signature must be updated!

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

# 3. Update TurnManagementService
file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\TurnManagementService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('campaignStoreRepo.findByIdCuaHang(request.getStoreId())', 'campaignStoreRepo.findByMaStore(request.getStoreId().toString())')
content = content.replace('Long campaignId = cs.getMaChienDich();', 'String campaignId = cs.getMaChienDich();')
content = content.replace('token.setIdHoaDon(invoice.getId());', 'token.setMaHoaDon(invoice.getMaHoaDon());')
content = content.replace('token.setIdKhachHangKichHoat(customer.getId());', 'token.setMaKhachHangKichHoat(customer.getMaKhachHang());')
content = content.replace('invoice.setIdCuaHang(request.getStoreId());', 'invoice.setMaStore(request.getStoreId().toString());')
content = content.replace('invoice.setIdKhachHang(customerId);', 'invoice.setMaKhachHang(customerRepo.findById(customerId).get().getMaKhachHang());')
# Wait, customerId is passed to saveInvoice. Let's just use customerId to fetch it, or change saveInvoice signature.
content = content.replace('private Invoice saveInvoice(InvoiceRequestDTO request, Long customerId)', 'private Invoice saveInvoice(InvoiceRequestDTO request, String maKhachHang)')
content = content.replace('saveInvoice(request, customer.getId());', 'saveInvoice(request, customer.getMaKhachHang());')
content = content.replace('invoice.setIdKhachHang(customerId);', 'invoice.setMaKhachHang(maKhachHang);')

# customerTurnRepo.addCustomerTurnsSafe in TurnManagementService
content = content.replace('customerTurnRepo.addCustomerTurnsSafe(\n                        customer.getId(),\n                        campaignId,', 'customerTurnRepo.addCustomerTurnsSafe(\n                        customer.getMaKhachHang(),\n                        campaignId,')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

# 4. Update PosService addCustomerTurnsSafe
file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\PosService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('customerTurnRepository.addCustomerTurnsSafe(\n                            customer.getId(),\n                            campaign.getId(),', 'customerTurnRepository.addCustomerTurnsSafe(\n                            customer.getMaKhachHang(),\n                            campaign.getMaChienDich(),')
content = content.replace('customerTurnRepository.addCustomerTurnsSafe(\n                            customer.getId(),\n                            campaign.getMaChienDich(),', 'customerTurnRepository.addCustomerTurnsSafe(\n                            customer.getMaKhachHang(),\n                            campaign.getMaChienDich(),')
content = content.replace('customerTurnRepository.addCustomerTurnsSafe(\n                            customer.getMaKhachHang(),\n                            campaign.getId(),', 'customerTurnRepository.addCustomerTurnsSafe(\n                            customer.getMaKhachHang(),\n                            campaign.getMaChienDich(),')
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

# 5. Update DeltaRuleEngine
file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\DeltaRuleEngine.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('public int calculateTurns(Long campaignId', 'public int calculateTurns(String campaignId')
content = content.replace('findByIdChienDich', 'findByMaChienDich')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

# 6. Update CustomerTurnRepository
file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\repository\CustomerTurnRepository.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('@Param("ma_khach_hang") Long maKhachHang', '@Param("ma_khach_hang") String maKhachHang')
content = content.replace('@Param("ma_chien_dich") Long maChienDich', '@Param("ma_chien_dich") String maChienDich')
content = content.replace('@Param("id_khach_hang") Long idKhachHang', '@Param("ma_khach_hang") String maKhachHang')
content = content.replace('@Param("id_chien_dich") Long idChienDich', '@Param("ma_chien_dich") String maChienDich')
content = content.replace(':id_khach_hang, :id_chien_dich', ':ma_khach_hang, :ma_chien_dich')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated multiple files")
