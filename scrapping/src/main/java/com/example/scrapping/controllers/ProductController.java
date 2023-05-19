package com.example.scrapping.controllers;

import com.example.scrapping.models.Product;
import com.example.scrapping.models.ProductDetail;
import com.example.scrapping.models.Result;
import com.example.scrapping.models.User;
import com.example.scrapping.repositories.ProductDetailRepository;
import com.example.scrapping.repositories.ProductRepository;
import com.example.scrapping.services.ProductServices;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;

@Controller
@EnableScheduling
public class ProductController {

    ProductServices productServices;

    public ProductController(ProductServices productServices) {
        this.productServices = productServices;
    }



    @GetMapping("/")
    public String index(){
        return "index";
    }


    @RequestMapping(value ="/scrapping",method = RequestMethod.POST)
    public ModelAndView scrapping(@RequestParam("siteismi") String siteismi,@RequestParam("sayfaadeti") int sayfaadeti,
                                  @RequestParam("uruncinsi") String uruncinsi,
                                  @RequestParam("threadsayisi") int threadsayisi, @ModelAttribute Result result) {


        ModelAndView model= new ModelAndView();



       MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
       int threadCount =threadsayisi;
       Thread[] threads = new Thread[threadCount];
       double[] cpuUsages = new double[threadCount];
       double[] trendyolmemoryUsages = new double[threadCount];

       long startTime = System.currentTimeMillis();
       long startTime2 = System.nanoTime();


       if(siteismi.equals("Trendyol")){
           for (int i = 0; i < threadCount; i++) {
               final int index = i;

               threads[i] = new Thread(() -> {
                   for (int j = 2 + index; j < sayfaadeti; j += threadCount) {
                       final String url = "https://www.trendyol.com/cep-telefonu-x-c103498?pi=" + j;

                       try {
                           final Document document = Jsoup.connect(url).get();
                           final Elements temel = document.select("div.prdct-cntnr-wrppr");

                           for (Element x : temel.select("div.p-card-wrppr.with-campaign-view")) {
                               Product product=new Product();
                               final String link = "https://www.trendyol.com/" +
                                       x.select("div.p-card-chldrn-cntnr.card-border a").attr("href");
                               final Document doc = Jsoup.connect(link).get();

                               final String fiyat = doc.select("div.product-price-container span.prc-dsc").text();
                               final String isim = doc.select("h1.pr-new-br span").text();

                               System.out.println(isim);
                               System.out.println(fiyat);

                               product.setName(isim);
                               product.setPrice(fiyat);








                           }
                       } catch (IOException e) {
                           System.err.println(e.getMessage());
                       }

                       double cpuUsage = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / 1000000.0;
                       cpuUsages[index] += cpuUsage;

                       MemoryUsage usedMemory = memoryMXBean.getHeapMemoryUsage();
                       trendyolmemoryUsages[index] += usedMemory.getUsed();
                   }
               });

               threads[i].start();
           }


       }

       for (int i = 0; i < threadCount; i++) {
           try {
               threads[i].join();
           } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               System.err.println("Join interrupted");
           }
       }


       double totalCpuUsage = 0.0;
       for (int i = 0; i < threadCount; i++) {
           totalCpuUsage += cpuUsages[i];
       }


       double totalRamUsage = 0.0;
       for (int i = 0; i <threadCount; i++) {
           totalRamUsage += trendyolmemoryUsages[i];
       }


       long endTime = System.currentTimeMillis();
       long totalTime = endTime - startTime;
       result.setTotalCpuUsage(totalCpuUsage);
       result.setTotalTime(totalTime);
       result.setTotalRamUsage(totalRamUsage);





       System.out.println("Kodun çalışma süresi: " + totalTime + " ms");
       System.out.println("CPU Kullanımı: " + totalCpuUsage + " ms");
       System.out.println("Ram Kullanımı: " + totalRamUsage + " ms");


       model.setViewName("deneme");
       model.addObject("result",result);

       return model;


   }


   @PostMapping("/webscrapping")
    public void webScrapping(){
    productServices.webScrapping();

   }


   @PostMapping("/cronjob")
   @Scheduled(fixedDelay = 300000)
   public void cronJob(){

        productServices.cronJob();


   }

   @PostMapping("/productdetail")

   public void getProductDetatil(){

        productServices.getProductDetail();

       }
   }















