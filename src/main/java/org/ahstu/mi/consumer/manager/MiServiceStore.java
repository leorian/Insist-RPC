package org.ahstu.mi.consumer.manager;

import org.ahstu.mi.common.MiError;
import org.ahstu.mi.common.MiException;
import org.ahstu.mi.common.MiUtil;
import org.ahstu.mi.consumer.MiConsumerMeta;
import org.ahstu.mi.module.ServiceMeta;

import java.util.*;

/**
 * Created by renyueliang on 17/5/15.
 */
public class MiServiceStore {

    private static String MI_SERVICE_STORE_LOCK = UUID.randomUUID().toString();

    //manager all serviceName group version  service ip and port
    private static Map<String, MiServiceList> serviceStore = new HashMap<String, MiServiceList>();
    // manager all ipAndPort service
    private static Map<String, List<ServiceMeta>> ipAndPortServiceMetaStore = new Hashtable<String, List<ServiceMeta>>();

    public static void add(MiServiceList miServiceList) {
        serviceStore.put(miServiceList.getServiceNameGroupVersion(), miServiceList);
    }

    public static void addByIpAndPort(ServiceMeta serviceMeta) {
        String ipAndPort = MiUtil.ipAndPortCreateKey(serviceMeta);
        List<ServiceMeta> list = ipAndPortServiceMetaStore.get(ipAndPort);
        if (list == null) {
            synchronized (MI_SERVICE_STORE_LOCK) {
                list = ipAndPortServiceMetaStore.get(ipAndPort);
                if (list == null) {
                    list = new ArrayList<ServiceMeta>();
                    ipAndPortServiceMetaStore.put(ipAndPort, list);
                }
            }
        }
        list.add(serviceMeta);
    }

    public static List<ServiceMeta> getAllServiceMetaByIpAndPort(String ipAndPort){
        return ipAndPortServiceMetaStore.get(ipAndPort);
    }

    public static Collection<MiServiceList> getAll() {
        return serviceStore.values();

    }

    public synchronized static void delAllServiceByServiceMeta(ServiceMeta serviceMeta){
        String ipAndPort = MiUtil.ipAndPortCreateKey(serviceMeta);
        List<ServiceMeta> serviceMetaList = ipAndPortServiceMetaStore.get(ipAndPort);
        if(serviceMetaList==null){
            return ;
        }
        for(ServiceMeta delServiceMeta : serviceMetaList){
            delOneServiceByServiceMeta(delServiceMeta);
        }
        ipAndPortServiceMetaStore.remove(ipAndPort);

    }

    public static void delOneServiceByServiceMeta(ServiceMeta serviceMeta) {
        String serviceNameGroupVersion = MiUtil.serviceGroupVersionCreateKey(serviceMeta.getInterfaceName(),
                serviceMeta.getGroup(), serviceMeta.getVersion());
        MiServiceList miServiceList = serviceStore.get(serviceNameGroupVersion);

        miServiceList.delService(MiUtil.ipAndPortCreateKey(serviceMeta));

    }

    public static void del(String serviceNameGroupVersion) {
        serviceStore.remove(serviceNameGroupVersion);
    }

    public static MiServiceList get(String serviceNameGroupVersion) {

        return serviceStore.get(serviceNameGroupVersion);

    }


    public static ServiceMeta getServiceMeta(MiConsumerMeta meta) {
        String serviceNameGroupVersion = MiUtil.serviceGroupVersionCreateKey(meta.getInterfaceName(),
                meta.getGroup(), meta.getVersion());
        return getServiceMeta(serviceNameGroupVersion);
    }


    public static ServiceMeta getServiceMeta(String serviceNameGroupVersion) {

        MiServiceList serviceList = get(serviceNameGroupVersion);

        if (serviceList == null) {
            throw new MiException(MiError.NOT_FIND_SERVICE.getErrorCode(), serviceNameGroupVersion + " not find !");
        }

        return serviceList.getService();


    }


}
