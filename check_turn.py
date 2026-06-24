import re

file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\TurnManagementService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace findByIdCuaHang(Long)
content = content.replace('campaignStoreRepo.findByIdCuaHang(request.getStoreId())', 'campaignStoreRepo.findByMaStore(request.getStoreId().toString())')

# Replace cs.getMaChienDich() because previously it might have been cs.getIdChienDich(). 
# Wait, my previous python script might have replaced getIdChienDich to getMaChienDich. Let's check what's there:
# Line 72: Long campaignId = cs.getMaChienDich(); 
# Oh wait, the compiler error says: "cannot find symbol symbol: method getIdChienDich()". Wait, if the compiler says that, my previous python script for TurnManagementService didn't run or didn't match.
# Wait, I ran update_services.py, but it was after I compiled. Let me run compilation again to see what errors remain.
