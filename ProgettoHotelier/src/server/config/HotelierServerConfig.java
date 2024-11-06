package server.config;

public class HotelierServerConfig {

    /**
     * La classe HotelierServerConfig contiene i config del client, ovvero:
     * • tcpPort, porta per socket Tcp
     * • rmiPort, porta per registro Rmi
     * • mcastPort, porta per socket mutlicast
     * • rankingInterval, secondi da intercorrere tra calcoli successivi dei rank degli hotel
     * • serverAddress, indirizzo socket Tcp/Rmi
     * • rmiRemoteReference, nome per reperire stub server dal registro rmi
     * • mcastAddress, indirizzo ip socket multicast
     */

    private final int tcpPort;
    private final int rmiPort;
    private final int mcastPort;
    private final int rankingInterval;
    private final String serverAddress;
    private final String rmiRemoteReference;
    private final String mcastAddress;

    public HotelierServerConfig(int tcpPort, int rmiPort, int mcastPort, int rankingInterval, String serverAddress, String rmiRemoteReference, String mcastAddress) {
        this.tcpPort = tcpPort;
        this.rmiPort = rmiPort;
        this.mcastPort = mcastPort;
        this.rankingInterval = rankingInterval;
        this.serverAddress = serverAddress;
        this.rmiRemoteReference = rmiRemoteReference;
        this.mcastAddress = mcastAddress;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public int getMcastPort() {
        return mcastPort;
    }

    public int getRankingInterval() {
        return rankingInterval;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getRmiRemoteReference() {
        return rmiRemoteReference;
    }

    public String getMcastAddress() {
        return mcastAddress;
    }

}