package com.cermati.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Solution {
    public static void main(String[] args) {
        try {
            // Here we create a document object and use JSoup to fetch the website
            Document doc = Jsoup.connect("https://www.cermati.com/karir").get();
            Map<String,String> jobPostingUrls = new HashMap<String, String>();
            Elements tabPane = doc.getElementsByClass("tab-pane");
            for(int i = 0;i < tabPane.size(); i++){
                String id = "tab"+i;
                Element eachDeptElement = doc.getElementById(id);
                Elements eachDeptClass = eachDeptElement.getElementsByClass("clickable-row row");
                String eachDeptName = eachDeptElement.select("div.dept-label").first().text();
                for (Element element : eachDeptClass) {
                    Element link = element.select("a").first();
                    jobPostingUrls.put(link.attr("href").split("-")[0],eachDeptName); // excluded the role names
                }
            }
            GetRoleDetailsParallel.crawl(jobPostingUrls);
            BuildJson.build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
