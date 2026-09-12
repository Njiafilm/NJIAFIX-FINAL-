-- Faili hii inaendeshwa kiotomatiki na Spring Boot wakati wa kuanza
-- (sawa na fixture ya Django: python manage.py loaddata fixture.json)

INSERT INTO device_category (id, name, icon_name, display_order) VALUES
  (1, 'Simu za Mkononi', '', 0),
  (2, 'Kompyuta Mpakato (Laptops)', '', 0),
  (3, 'Printer na Photocopy', '', 0),
  (4, 'Camera/CCTV', '', 0),
  (5, 'Gari (OBD2)', '', 0),
  (6, 'PC ya Mteja (Remote)', '', 0),
  (7, 'Router', '', 0),
  (8, 'UPS/Inverter', '', 0),
  (9, 'Solar System', '', 0);

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
   'easy', 0, CURRENT_TIMESTAMP, 'UPTIME_HIGH'),

  -- Printer / Photocopy
  (9, 3, 'Printer Imesimama (Stopped)', '', 'Printer state ni STOPPED',
   '1. Amri ya "Restart Spooler" itaisha na kuwasha upya huduma ya uchapishaji kiotomatiki.
2. Kama tatizo linaendelea, chomoa na unganisha tena waya wa USB/umeme wa printer.',
   'easy', 0, CURRENT_TIMESTAMP, 'PRINTER_STOPPED'),

  (10, 3, 'Printer Haikubali Kazi Mpya', '', 'PrinterIsAcceptingJobs = false',
   '1. Amri ya "Restart Spooler" mara nyingi hutatua hili.
2. Angalia foleni ya uchapishaji (Print Queue) na ufute kazi zilizokwama.',
   'easy', 0, CURRENT_TIMESTAMP, 'PRINTER_NOT_ACCEPTING'),

  (11, 3, 'Karatasi Imekwama', '', 'PrinterStateReason: media-jam',
   '1. Zima printer kwanza kabla ya kuondoa karatasi iliyokwama.
2. Fungua tray na eneo la nyuma la printer, ondoa karatasi taratibu kufuata mwelekeo wa asili.
3. Angalia kama kuna vipande vidogo vya karatasi vilivyobaki ndani.',
   'medium', 0, CURRENT_TIMESTAMP, 'PRINTER_PAPER_JAM'),

  (12, 3, 'Wino/Toner Kidogo', '', 'PrinterStateReason: toner-low/marker-supply-low',
   '1. Andaa cartridge/toner mpya kabla haijaisha kabisa.
2. Kama ni toner ya laser, itikisa kidogo kuongeza matumizi ya mabaki yaliyopo kwa muda.',
   'easy', 0, CURRENT_TIMESTAMP, 'PRINTER_LOW_TONER'),

  (13, 3, 'Kifuniko cha Printer Kiko Wazi', '', 'PrinterStateReason: door-open/cover-open',
   '1. Angalia na funga milango/vifuniko vyote vya printer vizuri.
2. Hakikisha hakuna kitu kinachozuia kifuniko kufunga vizuri.',
   'easy', 0, CURRENT_TIMESTAMP, 'PRINTER_COVER_OPEN'),

  (14, 3, 'Printer ya Mtandao Haifikiki', '', 'Ports 9100/631 hazifikiki',
   '1. Angalia kama printer imewashwa na iko kwenye mtandao sahihi (Wi-Fi/Ethernet).
2. Thibitisha IP address ya printer haijabadilika (angalia kwenye screen ya printer yenyewe).
3. Jaribu ku-ping IP hiyo kutoka kwenye kompyuta nyingine kuthibitisha mtandao.',
   'medium', 0, CURRENT_TIMESTAMP, 'NETWORK_PRINTER_UNREACHABLE'),

  -- Camera/CCTV
  (15, 4, 'Camera Haifikiki Kabisa', '', 'HTTP na RTSP zote hazifikiki',
   '1. Amri ya "Reboot Camera" itajaribu kuwasha upya kupitia mtandao (kama camera inasaidia hilo).
2. Angalia umeme wa camera na kebo za mtandao (Ethernet/PoE).
3. Thibitisha IP address ya camera haijabadilika kwenye router.',
   'medium', 0, CURRENT_TIMESTAMP, 'CAMERA_UNREACHABLE'),

  (16, 4, 'Video Stream (RTSP) Haifanyi Kazi', '', 'HTTP inafikika lakini RTSP haifikiki',
   '1. Camera iko hai kwenye mtandao lakini haitumi video - jaribu "Reboot Camera".
2. Angalia mipangilio ya RTSP kwenye dashboard ya wavuti ya camera (mara nyingi kupitia IP yake kwenye kivinjari).',
   'medium', 0, CURRENT_TIMESTAMP, 'CAMERA_STREAM_DOWN'),

  -- Gari (OBD2)
  (17, 5, 'Msimbo wa Hitilafu Umepatikana (DTC)', '', 'ECU imeripoti Diagnostic Trouble Code',
   '1. Andika msimbo halisi ulioonyeshwa (mfano P0301) kwa ajili ya utafiti zaidi.
2. Amri ya "Futa DTC" itazima taa ya Check Engine - lakini FANYA HIVI BAADA tu ya kutatua chanzo halisi cha tatizo.
3. Kama msimbo unarudi tena baada ya kufuta, tatizo la msingi bado lipo - halijatatuliwa.',
   'hard', 0, CURRENT_TIMESTAMP, 'DTC_FOUND'),

  -- PC ya Mteja (kupitia PcAgent)
  (18, 6, 'PC Agent Haifikiki', '', 'Haiwezi kuunganisha na PcAgent kwenye port 5556',
   '1. Hakikisha PcAgent inaendesha kwenye kompyuta ya mteja (java PcAgent).
2. Hakikisha kompyuta ya mteja na fundi ziko kwenye mtandao mmoja (Wi-Fi/LAN).
3. Angalia kama Firewall ya kompyuta ya mteja inazuia port 5556.',
   'medium', 0, CURRENT_TIMESTAMP, 'PC_AGENT_UNREACHABLE'),

  (19, 6, 'CPU Inatumika Sana', '', 'CPU load zaidi ya 90%',
   '1. Fungua Task Manager (Ctrl+Shift+Esc) kuona programu inayotumia CPU nyingi.
2. Zima programu zisizohitajika zinazoendesha nyuma (background).
3. Angalia kama kuna virus/malware inayotumia rasilimali - endesha antivirus scan.',
   'medium', 0, CURRENT_TIMESTAMP, 'PC_HIGH_CPU'),

  (20, 6, 'RAM Imejaa', '', 'RAM iliyobaki bure chini ya 10%',
   '1. Funga programu zisizohitajika zinazoendesha kwa sasa.
2. Angalia programu za "Startup" (Task Manager > Startup) na uzime zisizohitajika.
3. Kama tatizo linaendelea daima, kompyuta inaweza kuhitaji RAM ya ziada.',
   'easy', 0, CURRENT_TIMESTAMP, 'PC_LOW_RAM'),

  (21, 6, 'Hifadhi (Disk) Imejaa', '', 'Disk usage zaidi ya 90%',
   '1. Endesha Disk Cleanup kuondoa faili za muda (temporary files).
2. Ondoa programu/faili kubwa zisizohitajika.
3. Angalia folda ya Downloads na Recycle Bin mara kwa mara.',
   'easy', 0, CURRENT_TIMESTAMP, 'PC_LOW_DISK'),

  -- Router / UPS / Solar (ukaguzi wa jumla)
  (22, 7, 'Router Haifikiki', '', 'Router haijibu kwenye ports 80/443',
   '1. Angalia kama router ina umeme na taa zake za mbele zinawaka.
2. Jaribu kuzima na kuwasha upya router (zima kwa sekunde 10, kisha washa).
3. Thibitisha IP address ya router haijabadilika.',
   'easy', 0, CURRENT_TIMESTAMP, 'ROUTER_UNREACHABLE'),

  (23, 8, 'UPS/Inverter Haifikiki', '', 'Haijibu kwenye mtandao',
   '1. Angalia kama UPS/Inverter ina umeme wa AC unaoingia.
2. Thibitisha kebo ya mtandao (Ethernet) imeunganishwa vizuri kwenye kifaa.
3. Baadhi ya UPS/Inverter zinahitaji usanidi maalum wa IP kupitia dashboard yake - siyo zote zina DHCP.',
   'medium', 0, CURRENT_TIMESTAMP, 'UPS_UNREACHABLE'),

  (24, 9, 'Solar System Haifikiki', '', 'Haijibu kwenye mtandao',
   '1. Angalia jopo la kudhibiti (controller/inverter) la mfumo wa jua lina umeme.
2. Thibitisha muunganiko wa Wi-Fi/Ethernet wa kifaa cha ufuatiliaji (monitoring unit).
3. Baadhi ya mifumo ya jua hutumia app maalum ya simu badala ya IP ya moja kwa moja - angalia app ya brand husika.',
   'medium', 0, CURRENT_TIMESTAMP, 'SOLAR_UNREACHABLE');

-- Weka namba za IDENTITY zinazofuata sahihi baada ya kuingiza ID maalum
ALTER TABLE device_category ALTER COLUMN id RESTART WITH 10;
ALTER TABLE repair_guide ALTER COLUMN id RESTART WITH 25;
