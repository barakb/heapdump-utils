package com.gigaspaces;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class App {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.gigasapces:type=HeapDumpMBean");
        mbs.registerMBean(new HeapDump(), name);
        Thread.sleep(Integer.MAX_VALUE);
    }

}
