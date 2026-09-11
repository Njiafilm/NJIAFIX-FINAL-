-- Faili hii inaendeshwa kiotomatiki na Spring Boot wakati wa kuanza
-- (sawa na fixture ya Django: python manage.py loaddata fixture.json)

INSERT INTO device_category (id, name, icon_name, display_order) VALUES
  (1, 'Simu za Mkononi', '', 0),
  (2, 'Kompyuta Mpakato (Laptops)', '', 0);

INSERT INTO repair_guide (id, category_id, title, brand, symptom, solution_steps, difficulty, views, created_at, issue_code) VALUES
  (1, 1, 'Jinsi ya kubadilisha kioo cha simu', '', '',
   '1. Zima simu kwanza.
2. Tumia bunduki ya joto kulainisha gundi.
3. Fungua kioo cha zamani kwa uangalifu.',
   'easy', 0, CURRENT_TIMESTAMP, NULL),

  (2, 2, 'Jinsi ya kubadilisha betri ya Laptop', '', '',
   '1. Zima kompyuta na uchomoe chaja.
2. Fungua skrubu za chini ya casing.
3. Chomoa betri ya zamani taratibu.',
   'easy', 0, CURRENT_TIMESTAMP, NULL),

  -- Guides zinazolingana na matatizo yanayogunduliwa KIOTOMATIKI na DiagnosticService
  (3, 1, 'Betri Inaisha Haraka (Chini ya 20%)', '', 'Kiwango cha betri kimeshuka chini ya 20%',
   '1. Chaji kifaa hadi angalau 50% kabla ya kuendelea na uchunguzi mwingine.
2. Angalia afya ya betri: Mipangilio > Betri > Afya ya Betri (kama ipo).
3. Kama betri inaisha haraka hata baada ya kuchaji vizuri, inaweza kuhitaji kubadilishwa.',
   'easy', 0, CURRENT_TIMESTAMP, 'LOW_BATTERY'),

  (4, 1, 'Hifadhi ya Ndani Imejaa', '', 'Storage imefikia zaidi ya 90%',
   '1. Amri ya "Safisha Cache" itafanya kazi kiotomatiki ukibonyeza Rekebisha.
2. Kama tatizo linaendelea, angalia programu zinazotumia nafasi kubwa zaidi: Mipangilio > Hifadhi.
3. Ondoa video/picha zisizohitajika au uzihamishie kwenye SD Card/Cloud.',
   'easy', 0, CURRENT_TIMESTAMP, 'STORAGE_FULL'),

  (5, 1, 'Mfumo Haujawasha Kikamilifu (Boot Incomplete)', '', 'sys.boot_completed sio 1',
   '1. Amri ya "Reboot" itajaribu kuanzisha upya mfumo kiotomatiki.
2. Kama simu inaendelea kukwama, jaribu Recovery Mode (Power + Volume Down).
3. Kama tatizo linaendelea, inaweza kuhitaji kuwekwa upya mfumo (factory reset) - onya mteja kuhusu data kabla ya hatua hii.',
   'medium', 0, CURRENT_TIMESTAMP, 'BOOT_INCOMPLETE'),

  (6, 1, 'RAM Imejaa (Chini ya 10% Bure)', '', 'Free RAM chini ya 10% ya Total RAM',
   '1. Amri ya "Reboot" itasafisha programu zinazoendesha nyuma kiotomatiki.
2. Angalia programu zinazotumia RAM nyingi: Developer Options > Running Services.
3. Zima au ondoa programu za "Auto-start" zisizohitajika.',
   'easy', 0, CURRENT_TIMESTAMP, 'LOW_RAM'),

  (7, 1, 'Hakuna Muunganisho wa Intaneti', '', 'Ping kwenda 8.8.8.8 imeshindwa',
   '1. Amri ya "Toggle Wi-Fi" itazima na kuwasha Wi-Fi kiotomatiki.
2. Kama tatizo linaendelea, angalia kama data ya simu (mobile data) imewashwa.
3. Jaribu "Forget Network" kisha unganisha upya na password sahihi.',
   'easy', 0, CURRENT_TIMESTAMP, 'NO_INTERNET'),

  (8, 1, 'Kifaa Hakijazimwa kwa Muda Mrefu', '', 'Uptime zaidi ya siku 3',
   '1. Amri ya "Reboot" inashauriwa kusafisha kumbukumbu ya mfumo iliyojaa.
2. Reboot ya mara kwa mara (angalau mara moja kwa wiki) huzuia matatizo mengi ya utendaji.',
   'easy', 0, CURRENT_TIMESTAMP, 'UPTIME_HIGH');

-- Weka namba za IDENTITY zinazofuata sahihi baada ya kuingiza ID maalum
ALTER TABLE device_category ALTER COLUMN id RESTART WITH 3;
ALTER TABLE repair_guide ALTER COLUMN id RESTART WITH 9;
