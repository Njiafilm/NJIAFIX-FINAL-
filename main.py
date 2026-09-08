# main.py - Njiafix Fundi
# App ya fundi yenye vitendo halisi (sio maelekezo tu):
#   - Simu: Factory Reset (Device Admin), Diagnostics, Fungua App Settings kwa cache
#   - TV/Sat: IR Remote (ikiwa simu ina IR blaster)
#
# MUHIMU: Faili hii inahitaji pyjnius (inasakinishwa kiotomatiki na buildozer
# kwenye Android build - haitafanya kazi ukijaribu kukimbiza kwenye desktop).

from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.popup import Popup
from kivy.metrics import dp

from android import mActivity
from jnius import autoclass, cast

# --- Android Java classes tunazohitaji ---
Context = autoclass('android.content.Context')
DevicePolicyManager = autoclass('android.app.admin.DevicePolicyManager')
ComponentName = autoclass('android.content.ComponentName')
Intent = autoclass('android.content.Intent')
Settings = autoclass('android.provider.Settings')
Uri = autoclass('android.net.Uri')
ConsumerIrManager = autoclass('android.hardware.ConsumerIrManager')
StatFs = autoclass('android.os.StatFs')
Environment = autoclass('android.os.Environment')
ActivityManager = autoclass('android.app.ActivityManager')
BatteryManager = autoclass('android.os.BatteryManager')
IntentFilter = autoclass('android.content.IntentFilter')

PACKAGE_NAME = "org.njiafix.fundi"  # BADILISHA kulingana na package yako halisi ya buildozer.spec
ADMIN_RECEIVER_CLASS = "org.njiafix.NjiafixDeviceAdminReceiver"


def show_popup(title, message):
    content = Label(text=message)
    popup = Popup(title=title, content=content, size_hint=(0.85, 0.4))
    popup.open()


def get_device_policy_manager():
    return cast('android.app.admin.DevicePolicyManager',
                mActivity.getSystemService(Context.DEVICE_POLICY_SERVICE))


def get_admin_component():
    return ComponentName(mActivity.getApplicationContext(), ADMIN_RECEIVER_CLASS)


# ---------------- SCREEN: Nyumbani (chagua Simu / TV-Sat) ----------------
class HomeScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(16))
        layout.add_widget(Label(text='🔧 Njiafix Fundi', font_size='26sp', size_hint_y=None, height=dp(50)))

        btn_simu = Button(text='📱 Simu\n(Vitendo halisi)', font_size='18sp')
        btn_simu.bind(on_release=lambda x: setattr(self.manager, 'current', 'simu'))
        layout.add_widget(btn_simu)

        btn_tv = Button(text='📺 TV / Sat\n(IR Remote)', font_size='18sp')
        btn_tv.bind(on_release=lambda x: setattr(self.manager, 'current', 'ir_remote'))
        layout.add_widget(btn_tv)

        self.add_widget(layout)


# ---------------- SCREEN: Simu - vitendo halisi ----------------
class SimuScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(14))
        layout.add_widget(Label(text='📱 Simu - Vitendo', font_size='22sp', size_hint_y=None, height=dp(44)))

        btn_diag = Button(text='📊 Diagnostics (Storage/RAM/Betri)')
        btn_diag.bind(on_release=self.run_diagnostics)
        layout.add_widget(btn_diag)

        btn_cache = Button(text='🧹 Fungua Cache Settings')
        btn_cache.bind(on_release=self.open_app_settings)
        layout.add_widget(btn_cache)

        btn_admin = Button(text='🔐 Washa Device Admin\n(hatua ya kwanza, mara moja)')
        btn_admin.bind(on_release=self.request_device_admin)
        layout.add_widget(btn_admin)

        btn_reset = Button(text='⚠️ FACTORY RESET\n(inafuta data zote)', background_color=(0.8, 0.1, 0.1, 1))
        btn_reset.bind(on_release=self.confirm_factory_reset)
        layout.add_widget(btn_reset)

        back = Button(text='← Rudi Nyumbani', size_hint_y=None, height=dp(44))
        back.bind(on_release=lambda x: setattr(self.manager, 'current', 'home'))
        layout.add_widget(back)

        self.add_widget(layout)

    def run_diagnostics(self, instance):
        try:
            # Storage
            path = Environment.getDataDirectory()
            stat = StatFs(path.getPath())
            block_size = stat.getBlockSizeLong()
            total = (stat.getBlockCountLong() * block_size) / (1024 ** 3)
            free = (stat.getAvailableBlocksLong() * block_size) / (1024 ** 3)

            # RAM
            am = cast('android.app.ActivityManager',
                      mActivity.getSystemService(Context.ACTIVITY_SERVICE))
            mem_info = autoclass('android.app.ActivityManager$MemoryInfo')()
            am.getMemoryInfo(mem_info)
            total_ram = mem_info.totalMem / (1024 ** 3)
            avail_ram = mem_info.availMem / (1024 ** 3)

            # Battery
            bm = cast('android.os.BatteryManager',
                      mActivity.getSystemService(Context.BATTERY_SERVICE))
            battery_pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            msg = (
                f"Storage: {free:.1f}GB huru / {total:.1f}GB jumla\n"
                f"RAM: {avail_ram:.1f}GB huru / {total_ram:.1f}GB jumla\n"
                f"Betri: {battery_pct}%"
            )
            show_popup("Diagnostics", msg)
        except Exception as e:
            show_popup("Hitilafu", f"Imeshindwa kupata diagnostics: {e}")

    def open_app_settings(self, instance):
        # Android haitoi "clear all cache" API tangu Android 6+.
        # Njia salama pekee ni kufungua Settings ya kila app kwa fundi kuchagua.
        try:
            intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mActivity.startActivity(intent)
        except Exception as e:
            show_popup("Hitilafu", f"Imeshindwa kufungua Settings: {e}")

    def request_device_admin(self, instance):
        try:
            intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, get_admin_component())
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                             "Njiafix Fundi inahitaji ruhusa hii kufanya Factory Reset kwa kitufe kimoja.")
            mActivity.startActivity(intent)
        except Exception as e:
            show_popup("Hitilafu", f"Imeshindwa kuomba ruhusa: {e}")

    def confirm_factory_reset(self, instance):
        content = BoxLayout(orientation='vertical', spacing=dp(10), padding=dp(10))
        content.add_widget(Label(text="Una uhakika? Hii itafuta DATA ZOTE za simu hii bila kurudi nyuma."))
        btn_row = BoxLayout(size_hint_y=None, height=dp(44), spacing=dp(10))
        popup = Popup(title="THIBITISHA FACTORY RESET", content=content, size_hint=(0.9, 0.4))

        btn_yes = Button(text="Ndiyo, Futa")
        btn_no = Button(text="Ghairi")
        btn_yes.bind(on_release=lambda x: (popup.dismiss(), self.do_factory_reset()))
        btn_no.bind(on_release=lambda x: popup.dismiss())
        btn_row.add_widget(btn_yes)
        btn_row.add_widget(btn_no)
        content.add_widget(btn_row)
        popup.open()

    def do_factory_reset(self):
        try:
            dpm = get_device_policy_manager()
            admin = get_admin_component()
            if not dpm.isAdminActive(admin):
                show_popup("Hitilafu", "Washa 'Device Admin' kwanza (kitufe cha juu).")
                return
            dpm.wipeData(0)
        except Exception as e:
            show_popup("Hitilafu", f"Reset imeshindwa: {e}")


# ---------------- SCREEN: IR Remote (TV/Sat) ----------------
class IrRemoteScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        layout = BoxLayout(orientation='vertical', padding=dp(20), spacing=dp(14))
        layout.add_widget(Label(text='📺 TV / Sat Remote', font_size='22sp', size_hint_y=None, height=dp(44)))

        self.status_label = Label(text=self.check_ir_support(), size_hint_y=None, height=dp(30))
        layout.add_widget(self.status_label)

        grid = GridLayout(cols=2, spacing=dp(10), size_hint_y=None, height=dp(160))
        btn_power = Button(text='⏻ Power')
        btn_power.bind(on_release=lambda x: self.send_ir('power'))
        btn_vol_up = Button(text='🔊 Vol +')
        btn_vol_up.bind(on_release=lambda x: self.send_ir('vol_up'))
        btn_vol_down = Button(text='🔉 Vol -')
        btn_vol_down.bind(on_release=lambda x: self.send_ir('vol_down'))
        btn_input = Button(text='📥 Source/Input')
        btn_input.bind(on_release=lambda x: self.send_ir('input'))
        grid.add_widget(btn_power)
        grid.add_widget(btn_input)
        grid.add_widget(btn_vol_up)
        grid.add_widget(btn_vol_down)
        layout.add_widget(grid)

        note = Label(
            text=("NB: Hii inatuma amri za kawaida za IR (NEC protocol). Vifaa vingine "
                  "vinahitaji code maalum ya chapa yao - matokeo yanaweza kutofautiana."),
            font_size='12sp', size_hint_y=None, height=dp(60)
        )
        layout.add_widget(note)

        back = Button(text='← Rudi Nyumbani', size_hint_y=None, height=dp(44))
        back.bind(on_release=lambda x: setattr(self.manager, 'current', 'home'))
        layout.add_widget(back)

        self.add_widget(layout)

    def check_ir_support(self):
        try:
            ir = cast('android.hardware.ConsumerIrManager',
                      mActivity.getSystemService(Context.CONSUMER_IR_SERVICE))
            if ir is not None and ir.hasIrEmitter():
                return "✅ Simu hii ina IR Blaster"
            return "❌ Simu hii HAINA IR Blaster - remote haitafanya kazi"
        except Exception:
            return "❌ Imeshindwa kuthibitisha IR hardware"

    def send_ir(self, action):
        try:
            ir = cast('android.hardware.ConsumerIrManager',
                      mActivity.getSystemService(Context.CONSUMER_IR_SERVICE))
            if ir is None or not ir.hasIrEmitter():
                show_popup("Haiwezekani", "Simu hii haina IR Blaster.")
                return

            # Frequency ya kawaida ya NEC protocol (38kHz). Pattern halisi
            # hutofautiana kwa chapa - haya ni MFANO tu wa muundo wa msingi.
            freq = 38000
            # placeholder pattern - badilisha na code halisi ya chapa husika ya TV/Sat
            pattern = [9000, 4500, 560, 560, 560, 1690, 560, 39975]
            from jnius import JavaClass, MetaJavaClass, java_method
            pattern_int_array = autoclass('java.lang.Integer').TYPE
            IntArray = pattern  # pyjnius kawaida inabadilisha list ya python kiotomatiki
            ir.transmit(freq, pattern)
            show_popup("Imetuma", f"Amri '{action}' imetumwa kwa IR.")
        except Exception as e:
            show_popup("Hitilafu", f"Imeshindwa kutuma IR: {e}")


class NjiafixFundiApp(App):
    def build(self):
        sm = ScreenManager()
        sm.add_widget(HomeScreen(name='home'))
        sm.add_widget(SimuScreen(name='simu'))
        sm.add_widget(IrRemoteScreen(name='ir_remote'))
        return sm


if __name__ == '__main__':
    NjiafixFundiApp().run()
