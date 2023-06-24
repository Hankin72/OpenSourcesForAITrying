import os


def get_volume():
    volume = os.popen("osascript -e 'output volume of (get volume settings)'").read().strip()
    return int(volume)


def get_volume_range():
    return [0.0, 100.0, 0.03125]


def set_volume(volume):
    os.system(f"osascript -e 'set volume output volume {volume}'")


def get_volume_percentage():
    volume = os.popen("osascript -e 'output volume of (get volume settings)'").read().strip()
    return int(volume)


# 输出当前音量的百分比
print(get_volume_percentage())
