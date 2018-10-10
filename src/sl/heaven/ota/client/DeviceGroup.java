package sl.heaven.ota.client;

import iot.espressif.esp32.model.device.IEspDevice;

import java.util.List;

public class DeviceGroup {
    private int groupId;
    private List<IEspDevice> deviceList;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public List<IEspDevice> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<IEspDevice> deviceList) {
        this.deviceList = deviceList;
    }
}
