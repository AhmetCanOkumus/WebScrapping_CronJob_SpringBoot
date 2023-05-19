package com.example.scrapping.services;

import com.example.scrapping.models.Product;
import com.example.scrapping.models.ProductDetail;
import com.example.scrapping.models.Result;
import com.example.scrapping.repositories.ProductDetailRepository;
import com.example.scrapping.repositories.ProductRepository;
import org.jsoup.Jsoup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;

@Service
public class ProductServices {


    ProductRepository productRepository;
    ProductDetailRepository productDetailRepository;

    public ProductServices(ProductRepository productRepository, ProductDetailRepository productDetailRepository) {
        this.productRepository = productRepository;
        this.productDetailRepository=productDetailRepository;
    }

    public Result ekle(String siteismi, String uruncinsi, int threadsayisi, int sayfaadeti) {

        Product dbo = new Product();
        Result model = new Result();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        int threadCount = threadsayisi;
        Thread[] threads = new Thread[threadCount];
        double[] cpuUsages = new double[threadCount];
        double[] trendyolmemoryUsages = new double[threadCount];

        long startTime = System.currentTimeMillis();
        long startTime2 = System.nanoTime();


        if (siteismi.equals("Trendyol")) {
            for (int i = 0; i < threadCount; i++) {
                final int index = i;

                threads[i] = new Thread(() -> {
                    for (int j = 2 + index; j < sayfaadeti; j += threadCount) {
                        final String url = "https://www.trendyol.com/cep-telefonu-x-c103498?pi=" + j;

                        try {
                            final Document document = Jsoup.connect(url).get();
                            final Elements temel = document.select("div.prdct-cntnr-wrppr");

                            for (Element x : temel.select("div.p-card-wrppr.with-campaign-view")) {
                                final String link = "https://www.trendyol.com/" +
                                        x.select("div.p-card-chldrn-cntnr.card-border a").attr("href");
                                final Document doc = Jsoup.connect(link).get();

                                final String fiyat = doc.select("div.product-price-container span.prc-dsc").text();
                                final String isim = doc.select("h1.pr-new-br span").text();

                                System.out.println(isim);
                                System.out.println(fiyat);


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
        for (int i = 0; i < sayfaadeti; i++) {
            totalRamUsage += trendyolmemoryUsages[i];
        }


        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        model.setTotalCpuUsage(totalCpuUsage);
        model.setTotalTime(totalTime);
        model.setTotalRamUsage(totalRamUsage);

        System.out.println("Kodun çalışma süresi: " + totalTime + " ms");
        System.out.println("CPU Kullanımı: " + totalCpuUsage + " ms");
        System.out.println("Ram Kullanımı: " + totalRamUsage + " ms");

        return model;
    }


    public void cronJob() {

        int threadCount = 2;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            threads[i] = new Thread(() -> {

                int j = index + 2;
                final String url = "https://www.trendyol.com/cep-telefonu-x-c103498?pi=" + j;

                try {
                    final Document document = Jsoup.connect(url).get();
                    final Elements temel = document.select("div.prdct-cntnr-wrppr");

                    for (Element x : temel.select("div.p-card-wrppr.with-campaign-view")) {
                        Product product = new Product();
                        final String link = "https://www.trendyol.com/" +
                                x.select("div.p-card-chldrn-cntnr.card-border a").attr("href");
                        final Document doc = Jsoup.connect(link).get();

                        final String fiyat = doc.select("div.product-price-container span.prc-dsc").text();
                        final String isim = doc.select("h1.pr-new-br span").text();

                        System.out.println(isim);
                        System.out.println(fiyat);

                        product.setName(isim);
                        product.setPrice(fiyat);
                        product.setDateTime(LocalDateTime.now());
                        productRepository.save(product);

                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

            });

            threads[i].start();
        }

        for (int i = 0; i < threadCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Join interrupted");
            }
        }
    }

    public void getProductDetail() {
        int threadCount = 2;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            threads[i] = new Thread(() -> {

                int j = index + 2;
                final String url = "https://www.trendyol.com/cep-telefonu-x-c103498?pi=" + j;

                try {
                    final Document document = Jsoup.connect(url).get();
                    final Elements temel = document.select("div.prdct-cntnr-wrppr");

                    for (Element x : temel.select("div.p-card-wrppr.with-campaign-view")) {
                        ProductDetail product = new ProductDetail();
                        final String link = "https://www.trendyol.com/" +
                                x.select("div.p-card-chldrn-cntnr.card-border a").attr("href");
                        final Document doc = Jsoup.connect(link).get();


                        final String dahiliHafiza = doc.select("li.detail-attr-item span b").get(1).text();
                        final String ramKapasitesi = doc.select("li.detail-attr-item span b").get(3).text();
                        final String isletimSistemi = doc.select("li.detail-attr-item span b").get(14).text();


                        final String isim = doc.select("h1.pr-new-br span").text();
                        final String fiyat = doc.select("div.product-price-container span.prc-dsc").text();


                        if(dahiliHafiza.contains("GB") && ramKapasitesi.contains("GB")){
                            product.setModel(isim);
                            product.setPrice(fiyat);
                            product.setInternalMemory(dahiliHafiza);
                            product.setRamCapacity(ramKapasitesi);
                            product.setOperatingSystem(isletimSistemi);

                            productDetailRepository.save(product);

                            System.out.println(isim);
                            System.out.println(fiyat);
                            System.out.println(dahiliHafiza);
                            System.out.println(ramKapasitesi);
                            System.out.println(isletimSistemi);
                        }





                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

            });

            threads[i].start();

        }


        for (int i = 0; i < threadCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Join interrupted");
            }
        }


    }

    public void webScrapping(){

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        int threadCount =3;
        Thread[] threads = new Thread[threadCount];
        double[] cpuUsages = new double[threadCount];
        double[] trendyolmemoryUsages = new double[threadCount];

        long startTime = System.currentTimeMillis();
        long startTime2 = System.nanoTime();



        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            threads[i] = new Thread(() -> {

                int j=index+2;
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

                        productRepository.save(product);






                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

                double cpuUsage = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() / 1000000.0;
                cpuUsages[index] += cpuUsage;

                MemoryUsage usedMemory = memoryMXBean.getHeapMemoryUsage();
                trendyolmemoryUsages[index] += usedMemory.getUsed();

            });

            threads[i].start();
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
    }






}
