import os
import re

html_file = r'd:\webquaymayrui\src\main\resources\templates\admin\campaign-list.html'
with open(html_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update data-start and data-end
content = content.replace(
    'th:data-start=""',
    'th:data-start=""'
)
content = content.replace(
    'th:data-end=""',
    'th:data-end=""'
)

# 2. Update inputs
content = content.replace(
    '<input type="datetime-local" class="form-control" name="ngayBatDau" id="campaignStart" required>',
    '<input type="text" class="form-control datetime-picker" name="ngayBatDau" id="campaignStart" placeholder="dd/mm/yyyy hh:mm" required>'
)
content = content.replace(
    '<input type="datetime-local" class="form-control" name="ngayKetThuc" id="campaignEnd" required>',
    '<input type="text" class="form-control datetime-picker" name="ngayKetThuc" id="campaignEnd" placeholder="dd/mm/yyyy hh:mm" required>'
)

# 3. Inject Flatpickr
flatpickr_assets = """
  <!-- Flatpickr for Date/Time standard Vietnam -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
  <script src="https://npmcdn.com/flatpickr/dist/l10n/vn.js"></script>
"""
content = content.replace('<!-- Modal Thêm/S?a Chi?n D?ch -->', flatpickr_assets + '\n  <!-- Modal Thêm/S?a Chi?n D?ch -->')

# 4. Initialize Flatpickr in script
init_script = """
    let startPicker, endPicker;
    document.addEventListener("DOMContentLoaded", function() {
      const config = {
          enableTime: true,
          dateFormat: "d/m/Y H:i",
          time_24hr: true,
          locale: "vn"
      };
      startPicker = flatpickr("#campaignStart", config);
      endPicker = flatpickr("#campaignEnd", config);
    });
"""
content = content.replace('<script>', '<script>' + init_script)

# 5. Fix JS assignments to trigger flatpickr
content = content.replace("document.getElementById('campaignStart').value = btn.getAttribute('data-start');", 
                          "startPicker.setDate(btn.getAttribute('data-start'));")
content = content.replace("document.getElementById('campaignEnd').value = btn.getAttribute('data-end');", 
                          "endPicker.setDate(btn.getAttribute('data-end'));")

content = content.replace("document.getElementById('campaignStart').value = '';", 
                          "startPicker.clear();")
content = content.replace("document.getElementById('campaignEnd').value = '';", 
                          "endPicker.clear();")

with open(html_file, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated campaign-list.html successfully")
