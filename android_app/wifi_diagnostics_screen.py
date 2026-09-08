# wifi_diagnostics_screen.py
# Ongeza faili hii karibu na main.py, kisha uifanye 'import' na
# kuiongeza kwenye ScreenManager ndani ya main.py (maelekezo chini).
#
# MUHIMU: Hii inatumia NjiafixAdbBridge.java (angalia faili hilo).
# Simu ya MTEJA lazima iwe na "Wireless debugging" imewashwa
# (Settings > Developer Options > Wireless debugging), na simu
# zote mbili ziwe kwenye WiFi moja.

from kivy.uix.screenmanager import Screen
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.scrollview import ScrollView
from kivy.uix.popup import Popup
from kivy.metrics import dp

from jnius import autoclass

NjiafixAdbBridge = autoclass('org.njiafix.NjiafixAdbBridge')


def show_popup(title, message):
    content = Label(text=message)
    popup = Popup(title=title, content=content, size_hint=(0.9, 0.5))
    popup.open()


class WifiDiagnosticsScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.bridge = None

        root = BoxLayout(orientation='vertical', padding=dp(16), spacing=dp(10))
        root.add_widget(Label(text='📱 Simu Nyingine (WiFi)', font_size='20sp',
                               size_hint_y=None, height=dp(40)))

        info = Label(
            text=("Kwenye simu ya mteja: Settings > Developer Options > "
                  "Wireless Debugging > 'Pair device with pairing code'. "
                  "Andika IP na Port ya ADB (sio ya pairing) hapa chini."),
            font_size='12sp', size_hint_y=None, height=dp(70)
        )
        root.add_widget(info)

        self.ip_input = TextInput(hint_text='IP, mfano 192.168.1.42', size_hint_y=None, height=dp(44))
        self.port_input = TextInput(hint_text='Port, mfano 37251', size_hint_y=None, height=dp(44))
        root.add_widget(self.ip_input)
        root.add_widget(self.port_input)

        connect_btn = Button(text='🔗 Unganisha', size_hint_y=None, height=dp(46))
        connect_btn.bind(on_release=self.do_connect)
        root.add_widget(connect_btn)

        self.status = Label(text='Hali: Bado hujaunganisha', size_hint_y=None, height=dp(30))
        root.add_widget(self.status)

        scan_btn = Button(text='🔍 Tafuta Matatizo', size_hint_y=None, height=dp(46))
        scan_btn.bind(on_release=self.scan_problems)
        root.add_widget(scan_btn)

        scroll = ScrollView()
        self.results_grid = GridLayout(cols=1, spacing=dp(8), size_hint_y=None)
        self.results_grid.bind(minimum_height=self.results_grid.setter('height'))
        scroll.add_widget(self.results_grid)
        root.add_widget(scroll)

        back = Button(text='← Rudi Nyumbani', size_hint_y=None, height=dp(44))
        back.bind(on_release=lambda x: setattr(self.manager, 'current', 'home'))
        root.add_widget(back)

        self.add_widget(root)

    def do_connect(self, instance):
        ip = self.ip_input.text.strip()
        port_text = self.port_input.text.strip()
        if not ip or not port_text:
            show_popup("Hitilafu", "Jaza IP na Port kwanza.")
            return
        try:
            port = int(port_text)
        except ValueError:
            show_popup("Hitilafu", "Port lazima iwe namba.")
            return

        self.bridge = NjiafixAdbBridge()
        # Weka path ya kuhifadhi funguo za crypto - kawaida app's private dir
        from android.storage import app_storage_path
        key_path = app_storage_path()
        ok_crypto = self.bridge.setupCrypto(key_path)
        if not ok_crypto:
            self.status.text = "Hali: Imeshindwa kutengeneza crypto keys"
            return

        ok_connect = self.bridge.connect(ip, port)
        if ok_connect:
            self.status.text = f"Hali: ✅ Imeunganishwa na {ip}:{port}"
        else:
            self.status.text = "Hali: ❌ Imeshindwa kuunganisha (angalia pairing/IP)"

    def scan_problems(self, instance):
        if not self.bridge:
            show_popup("Hitilafu", "Unganisha kwanza.")
            return

        self.results_grid.clear_widgets()
        self.results_grid.add_widget(Label(text="Inachambua...", size_hint_y=None, height=dp(30)))

        # Amri za dumpsys/shell zinazotumika kutambua matatizo ya kawaida
        battery_raw = self.bridge.runShellCommand("dumpsys battery")
        storage_raw = self.bridge.runShellCommand("df /data")
        packages_raw = self.bridge.runShellCommand("pm list packages -3")

        self.results_grid.clear_widgets()
        problems_found = self.parse_and_show(battery_raw, storage_raw, packages_raw)

        if not problems_found:
            self.results_grid.add_widget(
                Label(text="Hakuna matatizo makubwa yaliyogundulika.",
                      size_hint_y=None, height=dp(40))
            )

    def parse_and_show(self, battery_raw, storage_raw, packages_raw):
        found_any = False

        # --- Angalia afya ya betri ---
        if "health: 2" not in battery_raw:  # 2 = BATTERY_HEALTH_GOOD
            found_any = True
            self.add_problem_row(
                "⚠️ Betri: afya siyo nzuri (angalia dumpsys battery kwa undani)",
                fix_label="Fungua Battery Settings",
                fix_action=lambda: self.bridge.runShellCommand(
                    "am start -a android.settings.BATTERY_SAVER_SETTINGS")
            )

        # --- Angalia nafasi ya storage ---
        try:
            lines = storage_raw.strip().split("\n")
            if len(lines) >= 2:
                parts = lines[1].split()
                # df output columns tofautiana kwa kifaa - hii ni makadirio
                if len(parts) >= 5 and "%" in parts[-2]:
                    used_pct = int(parts[-2].replace("%", ""))
                    if used_pct >= 90:
                        found_any = True
                        self.add_problem_row(
                            f"⚠️ Storage imejaa ({used_pct}% imetumika)",
                            fix_label="Fungua Storage Settings",
                            fix_action=lambda: self.bridge.runShellCommand(
                                "am start -a android.settings.INTERNAL_STORAGE_SETTINGS")
                        )
        except Exception:
            pass

        # --- Onyesha apps za tatu (fundi anaweza kuchagua kuondoa) ---
        packages = [p.replace("package:", "").strip()
                    for p in packages_raw.strip().split("\n") if p.strip()]
        if packages:
            found_any = True
            self.results_grid.add_widget(
                Label(text=f"📦 Apps {len(packages)} zilizosakinishwa na mtumiaji - bonyeza kuondoa moja:",
                      size_hint_y=None, height=dp(40))
            )
            for pkg in packages[:15]:  # onyesha 15 za kwanza kuepuka orodha ndefu mno
                self.add_problem_row(
                    pkg,
                    fix_label="Ondoa (Uninstall)",
                    fix_action=lambda p=pkg: self.bridge.runShellCommand(f"pm uninstall {p}")
                )

        return found_any

    def add_problem_row(self, description, fix_label, fix_action):
        row = BoxLayout(orientation='horizontal', size_hint_y=None, height=dp(56), spacing=dp(6))
        row.add_widget(Label(text=description, font_size='13sp'))
        fix_btn = Button(text=fix_label, size_hint_x=0.4)

        def on_fix(instance):
            result = fix_action()
            show_popup("Matokeo", str(result) if result else "Imetekelezwa.")

        fix_btn.bind(on_release=on_fix)
        row.add_widget(fix_btn)
        self.results_grid.add_widget(row)
