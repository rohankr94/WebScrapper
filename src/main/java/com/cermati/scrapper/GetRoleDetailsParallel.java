package com.cermati.scrapper;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class ScrappingEachDept implements Runnable {

    private Thread t;
    private Map<String,String> jobPostingUrls;
    ScrappingEachDept(Map<String,String> l) {
        jobPostingUrls = l;
    }

    public void run(){
            try {
                for (Map.Entry<String,String> entry : jobPostingUrls.entrySet()){
                    JSONObject obj = new JSONObject();
                    String url = entry.getKey();
                    String category = entry.getValue();
                    Document doc = Jsoup.connect(url).get();
                    // job role name
                    Elements rolePos = doc.getElementsByClass("job-title");
                    obj.put("title", rolePos.text());

                    // job role location
                    Elements roleLoc = doc.getElementsByClass("job-detail");
                    String locAndType = roleLoc.text();
                    String loc = locAndType.substring(0, locAndType.lastIndexOf(" "));
                    obj.put("location", loc);

                    //job role description
                    String job_desc = "";
                    try {
                        Element desc = doc.getElementById("st-jobDescription");
                        Elements adesc = desc.select("div.wysiwyg > ul");
                        if (!adesc.hasText()) {
                            adesc = desc.select("div.wysiwyg > p");
                        }
                        Elements aadesc = adesc.select("li");
                        if (!aadesc.hasText()) {
                            String text = adesc.first().html();
                            String[] brSplits = text.split("<br>");
                            for (int itr = 0; itr < brSplits.length; itr++) {
                                if (brSplits[itr].equals("")) {
                                    continue;
                                }
                                job_desc = job_desc + "\"" + brSplits[itr].replaceAll("^\"|\"$", "") + "\"" + ",";
                            }
                        } else {
                            for (int itr = 0; itr < aadesc.size(); itr++) {
                                job_desc = job_desc + "\"" + aadesc.get(itr).text() + "\"" + ",";
                            }
                        }
                        job_desc = job_desc.substring(0, job_desc.length() - 1);
                        obj.put("description", job_desc);
                    }catch (Exception e){
                        // Some job role dosen't have description
                        obj.put("description", job_desc);
                    }

                    // job role qualification
                    String job_qual = "";
                    try {
                        Element qual = doc.getElementById("st-qualifications");
                        Elements aqual = qual.select("div.wysiwyg > ul");
                        Elements aaqual = aqual.select("li");
                        for (int itr = 0; itr < aaqual.size(); itr++) {
                            job_qual = job_qual + "\"" + aaqual.get(itr).text() + "\"" + ",";
                        }
                        job_qual = job_qual.substring(0, job_qual.length() - 1);
                        obj.put("qualification", job_qual);
                    }
                    catch (Exception e){
                        // Some job role dosen't have qualification
                        obj.put("qualification", job_qual);
                    }

                    // job role poster
                    Elements jobPoster = doc.getElementsByClass("details-title font--medium");
                    obj.put("Posted by", jobPoster.text());

                    ArrayList<JSONObject> l = new ArrayList<JSONObject>();
                    if(GetRoleDetailsParallel.mp.containsKey(category)) {
                        l = GetRoleDetailsParallel.mp.get(category);
                    }
                    l.add(obj);
                    GetRoleDetailsParallel.mp.put(category, l);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }
}

public class GetRoleDetailsParallel {

    public static Map<String, ArrayList<JSONObject> > mp = new HashMap<String, ArrayList<JSONObject>>();
    protected static void crawl(Map<String,String> jobPostingUrls) throws InterruptedException {

        ExecutorService es = Executors.newCachedThreadPool();
        int size = jobPostingUrls.size();

        // Created 6 maps for 6 departments
        Map<String,String> engineeringPart = new HashMap<String, String>();
        Map<String,String> marketingPart = new HashMap<String, String>();
        Map<String,String> productPart = new HashMap<String, String>();
        Map<String,String> bdPart = new HashMap<String, String>();
        Map<String,String> boPart = new HashMap<String, String>();
        Map<String,String> poPart = new HashMap<String, String>();

        for (Map.Entry<String,String> entry : jobPostingUrls.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if(value.equals("Engineering")){
                engineeringPart.put(key,value);
            }
            else if(value.equals("Marketing & Content")){
                marketingPart.put(key,value);
            }
            else if(value.equals("Product")){
                productPart.put(key, value);
            }
            else if(value.equals("Business Development")){
                bdPart.put(key, value);
            }
            else if(value.equals("Business Operations")){
                boPart.put(key, value);
            }
            else if(value.equals("People Ops")){
                poPart.put(key, value);
            }
        }

        // 6 seperate threads for 6 departments
        ScrappingEachDept engineeringT = new ScrappingEachDept( engineeringPart );
        es.execute(engineeringT);
        ScrappingEachDept marketingT = new ScrappingEachDept( marketingPart );
        es.execute(marketingT);
        ScrappingEachDept productT = new ScrappingEachDept( productPart );
        es.execute(productT);
        ScrappingEachDept bdT = new ScrappingEachDept( bdPart );
        es.execute(bdT);
        ScrappingEachDept boT = new ScrappingEachDept( boPart );
        es.execute(boT);
        ScrappingEachDept poT = new ScrappingEachDept( poPart );
        es.execute(poT);

        es.shutdown();
        boolean finished = es.awaitTermination(2, TimeUnit.MINUTES);
    }
}