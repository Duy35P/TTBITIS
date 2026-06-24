import re

file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\controller\AdminCampaignController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace getStoreAllocationModal
content = content.replace(
    '''        List<Long> assignedStoreIds = campaignStoreRepository.findByIdChienDich(campaignId)
                .stream()
                .map(CampaignStore::getIdCuaHang)
                .collect(Collectors.toList());
                
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("activeStores", activeStores);
        model.addAttribute("assignedStoreIds", assignedStoreIds);''',
    '''        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        List<String> assignedStoreMas = campaignStoreRepository.findByMaChienDich(campaign.getMaChienDich())
                .stream()
                .map(CampaignStore::getMaStore)
                .collect(Collectors.toList());
                
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("activeStores", activeStores);
        model.addAttribute("assignedStoreMas", assignedStoreMas);'''
)

# Replace saveStoreAllocation
content = content.replace(
    '''    @PostMapping("/{campaignId}/stores/save")
    public String saveStoreAllocation(@PathVariable Long campaignId, @RequestParam(required = false) List<Long> storeIds) {
        // Delete old assignments
        campaignStoreRepository.deleteByIdChienDich(campaignId);
        
        // Save new ones
        if (storeIds != null) {
            for (Long storeId : storeIds) {
                CampaignStore mapping = new CampaignStore();
                mapping.setIdChienDich(campaignId);
                mapping.setIdCuaHang(storeId);
                campaignStoreRepository.save(mapping);
            }
        }''',
    '''    @PostMapping("/{campaignId}/stores/save")
    public String saveStoreAllocation(@PathVariable Long campaignId, @RequestParam(required = false) List<String> storeMas) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        String maChienDich = campaign.getMaChienDich();
        // Delete old assignments
        campaignStoreRepository.deleteByMaChienDich(maChienDich);
        
        // Save new ones
        if (storeMas != null) {
            for (String storeMa : storeMas) {
                CampaignStore mapping = new CampaignStore();
                mapping.setMaChienDich(maChienDich);
                mapping.setMaStore(storeMa);
                campaignStoreRepository.save(mapping);
            }
        }'''
)

# Replace getCampaignRulesModal
content = content.replace(
    '''    @GetMapping("/{campaignId}/rules")
    public String getCampaignRulesModal(@PathVariable Long campaignId, Model model) {
        CampaignRule rule = campaignRuleRepository.findByIdChienDich(campaignId).orElse(new CampaignRule());
        List<CampaignRulePayment> payments = campaignRulePaymentRepository.findByIdChienDich(campaignId);
        List<CampaignRuleSku> skus = campaignRuleSkuRepository.findByIdChienDich(campaignId);''',
    '''    @GetMapping("/{campaignId}/rules")
    public String getCampaignRulesModal(@PathVariable Long campaignId, Model model) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        String maChienDich = campaign.getMaChienDich();
        CampaignRule rule = campaignRuleRepository.findByMaChienDich(maChienDich).orElse(new CampaignRule());
        List<CampaignRulePayment> payments = campaignRulePaymentRepository.findByMaChienDich(maChienDich);
        List<CampaignRuleSku> skus = campaignRuleSkuRepository.findByMaChienDich(maChienDich);'''
)

# Replace saveCampaignRules
content = content.replace(
    '''            // 1. Delete old rules
            campaignRuleRepository.deleteByIdChienDich(campaignId);
            campaignRulePaymentRepository.deleteByIdChienDich(campaignId);
            campaignRuleSkuRepository.deleteByIdChienDich(campaignId);
            
            // 2. Save Basic Rule
            if (form.getGiaTriDonHangToiThieu() != null) {
                CampaignRule rule = new CampaignRule();
                rule.setIdChienDich(campaignId);
                rule.setGiaTriDonHangToiThieu(form.getGiaTriDonHangToiThieu());
                campaignRuleRepository.save(rule);
            }''',
    '''            Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
            String maChienDich = campaign.getMaChienDich();
            // 1. Delete old rules
            campaignRuleRepository.deleteByMaChienDich(maChienDich);
            campaignRulePaymentRepository.deleteByMaChienDich(maChienDich);
            campaignRuleSkuRepository.deleteByMaChienDich(maChienDich);
            
            // 2. Save Basic Rule
            if (form.getGiaTriDonHangToiThieu() != null) {
                CampaignRule rule = new CampaignRule();
                rule.setMaChienDich(maChienDich);
                rule.setGiaTriDonHangToiThieu(form.getGiaTriDonHangToiThieu());
                campaignRuleRepository.save(rule);
            }'''
)

content = content.replace('payment.setIdChienDich(campaignId);', 'payment.setMaChienDich(maChienDich);')
content = content.replace('ruleSku.setIdChienDich(campaignId);', 'ruleSku.setMaChienDich(maChienDich);')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated AdminCampaignController.java")
