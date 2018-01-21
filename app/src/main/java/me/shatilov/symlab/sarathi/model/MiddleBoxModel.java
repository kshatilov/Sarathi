package me.shatilov.symlab.sarathi.model;

/**
 * Created by Kirill on 21-Jan-18.
 */

public class MiddleBoxModel {

    private String id;
    private String proxy;
    private String tcpOptimizer;
    private String ip;
    private String appName;

    public MiddleBoxModel(String id, String proxy, String tcpOptimizer, String ip, String appName) {
        this.id = id;
        this.proxy = proxy;
        this.tcpOptimizer = tcpOptimizer;
        this.ip = ip;
        this.appName = appName;
    }

    public MiddleBoxModel(String appName) {
        this.appName = appName;
    }

    public MiddleBoxModel() {}

    public String getId() {
        return id;
    }

    public String getProxy() {
        return proxy;
    }

    public String getTcpOptimizer() {
        return tcpOptimizer;
    }

    public String getIp() {
        return ip;
    }

    public String getAppName() {
        return appName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiddleBoxModel that = (MiddleBoxModel) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (proxy != null ? !proxy.equals(that.proxy) : that.proxy != null) return false;
        if (tcpOptimizer != null ? !tcpOptimizer.equals(that.tcpOptimizer) : that.tcpOptimizer != null)
            return false;
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) return false;
        return appName != null ? appName.equals(that.appName) : that.appName == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        result = 31 * result + (tcpOptimizer != null ? tcpOptimizer.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (appName != null ? appName.hashCode() : 0);
        return result;
    }
}
