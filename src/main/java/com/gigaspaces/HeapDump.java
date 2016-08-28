/*
 * Copyright (c) 2008-2016, GigaSpaces Technologies, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gigaspaces;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * on 8/28/16.
 *
 * @since 12.0
 */
@SuppressWarnings("WeakerAccess")
public class HeapDump implements HeapDumpMBean {
    private boolean enabled = true;
    private int maxHeaps = 1;
    private volatile int currentHeaps = 0;
    private volatile long quietPeriodMillis = 1000 * 60 * 60 *24;
    volatile long lastDumpMillis = 0;
    private String [] cmd = new String[]{"jmap", "-dump:live,format=b,file=%file", "%pid"};
    private String outputFileName = "dumpfile_%pid.hprof";

    public long getPid() {
        return Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }

    public String [] getCmd() {
        return cmd;
    }

    public void setCmd(String [] cmd) {
        this.cmd = cmd;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String name) {
        this.outputFileName = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxHeaps() {
        return maxHeaps;
    }

    public void setMaxHeaps(int maxHeaps) {
        this.maxHeaps = maxHeaps;
    }

    public int getCurrentHeaps() {
        return currentHeaps;
    }

    public void setCurrentHeaps(int currentHeaps) {
        this.currentHeaps = currentHeaps;
    }

    public long getQuietPeriod() {
        return quietPeriodMillis;
    }

    public void setQuietPeriod(long millis) {
        this.quietPeriodMillis = millis;
    }




    public String createHeapDump() throws IOException, InterruptedException {
        File file = new File(subsPid(getOutputFileName()));
        if(file.exists()){
            System.out.println("Deleting file " + file.getAbsolutePath());
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        String[] cmd = subsCmd();
        System.out.println("Executing " + Arrays.asList(cmd));
        ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectErrorStream(true);
        Process process = processBuilder.start();
        InputStream is = process.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int ch;
        while((ch = is.read()) != -1){
            os.write((byte)ch);
        }
        String res = os.toString();
        System.out.println("res is: " + res);
        process.waitFor();
        return res;
    }

    public boolean createDumpIfAppropriate() {
        return createDumpIfAppropriate(System.currentTimeMillis());
    }

    boolean createDumpIfAppropriate(long currentTimeMillis) {
        if(!isEnabled()){
            return false;
        }
        if(maxHeaps <= currentHeaps){
            return false;
        }
        if(currentTimeMillis - lastDumpMillis < quietPeriodMillis){
            return false;
        }
        currentHeaps += 1;
        lastDumpMillis = currentTimeMillis;
        try {
            createHeapDump();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String [] subsCmd() {
        List<String> res = new ArrayList<String>();
        for (String c : cmd) {
            res.add(subsPid(subsFileName(c)));
        }
        return res.toArray(new String[res.size()]);
    }


    private String subsPid(String arg){
        return arg.replaceAll("%pid", String.valueOf(getPid()));
    }
    private String subsFileName(String arg){
        return arg.replaceAll("%file", getOutputFileName());
    }
}
