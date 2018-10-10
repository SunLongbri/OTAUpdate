package sl.heaven.ota.client;

import java.util.List;

public class ParamBean {
    private String serverIp;
    private List<MeshBean> meshBeans;

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public List<MeshBean> getMeshBeans() {
        return meshBeans;
    }

    public void setMeshBeans(List<MeshBean> meshBeans) {
        this.meshBeans = meshBeans;
    }
}
