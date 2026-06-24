import re

file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\PosService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('invoice.setIdCuaHang(request.getStoreId());', 'invoice.setMaStore(request.getStoreId().toString());')
content = content.replace('invoice.setIdKhachHang(customer.getId());', 'invoice.setMaKhachHang(customer.getMaKhachHang());')
content = content.replace('List<CampaignStore> storeCampaigns = campaignStoreRepository.findByIdCuaHang(request.getStoreId());', 'List<CampaignStore> storeCampaigns = campaignStoreRepository.findByMaStore(request.getStoreId().toString());')

# calculateTurns(campaign.getId(), request); -> calculateTurns(campaign.getMaChienDich(), request);
content = content.replace('int turnsForThisCampaign = calculateTurns(campaign.getId(), request);', 'int turnsForThisCampaign = calculateTurns(campaign.getMaChienDich(), request);')
content = content.replace('customerTurnRepository.addCustomerTurnsSafe(', 'customerTurnRepository.addCustomerTurnsSafe(')
# Wait, addCustomerTurnsSafe expects what? It's mapped to @Query native. I'll need to update CustomerTurnRepository.java

content = content.replace('private int calculateTurns(Long campaignId, PosSyncRequest request) {', 'private int calculateTurns(String campaignId, PosSyncRequest request) {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated PosService.java")
